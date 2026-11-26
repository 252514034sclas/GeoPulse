import logging
from datetime import datetime, time
from typing import Dict, Any, List, Optional
from sqlalchemy.orm import Session

import models
from services.agricultural_decision_engine import AgriculturalDecisionEngine
from services.fcm_service import send_fcm_notification

logger = logging.getLogger("geopulse.notification_scheduler")


def is_in_quiet_hours(quiet_start: str, quiet_end: str, now_dt: Optional[datetime] = None) -> bool:
    """
    Check if the current time falls within farmer quiet hours (e.g., 22:00 to 06:00).
    """
    dt = now_dt or datetime.utcnow()
    current_time = dt.time()

    try:
        sh, sm = map(int, quiet_start.split(":"))
        eh, em = map(int, quiet_end.split(":"))
        start_time = time(sh, sm)
        end_time = time(eh, em)

        if start_time <= end_time:
            return start_time <= current_time <= end_time
        else:
            # Over midnight window (e.g., 22:00 to 06:00)
            return current_time >= start_time or current_time <= end_time
    except Exception as e:
        logger.error(f"[Scheduler] Error parsing quiet hours '{quiet_start}' - '{quiet_end}': {e}")
        return False


def get_or_create_user_preferences(user_id: int, db: Session) -> models.NotificationPreferenceModel:
    """
    Retrieve or initialize notification preferences for a farmer.
    """
    pref = db.query(models.NotificationPreferenceModel).filter(
        models.NotificationPreferenceModel.user_id == user_id
    ).first()

    if not pref:
        user = db.query(models.UserModel).filter(models.UserModel.id == user_id).first()
        lang = user.preferred_language if user and user.preferred_language else "en"
        pref = models.NotificationPreferenceModel(
            user_id=user_id,
            quiet_hours_enabled=True,
            quiet_hours_start="22:00",
            quiet_hours_end="06:00",
            language=lang,
            irrigation_enabled=True,
            weather_enabled=True,
            spraying_enabled=True,
            agronomy_enabled=True,
            crop_health_enabled=True,
            inspection_enabled=True,
            min_priority="LOW"
        )
        db.add(pref)
        db.commit()
        db.refresh(pref)

    return pref


def evaluate_user_fields(
    user_id: int,
    db: Session,
    target_field_id: Optional[int] = None,
    bypass_quiet_hours: bool = False,
    now_dt: Optional[datetime] = None
) -> Dict[str, Any]:
    """
    Evaluate eligible fields for a specific farmer, synthesize decision-support advisories,
    enforce deduplication and quiet-hour rules, and dispatch FCM push alerts to registered devices.
    """
    now = now_dt or datetime.utcnow()
    user = db.query(models.UserModel).filter(models.UserModel.id == user_id).first()
    if not user:
        return {
            "error": f"User with ID {user_id} not found",
            "evaluated_fields_count": 0,
            "notifications_generated_count": 0,
            "notifications_sent_count": 0,
            "notifications": []
        }

    pref = get_or_create_user_preferences(user_id, db)
    farmer_lang = pref.language or user.preferred_language or "en"

    # Quiet hours check
    in_quiet = False
    if pref.quiet_hours_enabled and not bypass_quiet_hours:
        in_quiet = is_in_quiet_hours(pref.quiet_hours_start, pref.quiet_hours_end, now)

    # Query fields belonging strictly to this user (anti-IDOR enforcement)
    query = db.query(models.FieldModel).filter(models.FieldModel.user_id == user_id)
    if target_field_id is not None:
        query = query.filter(models.FieldModel.id == target_field_id)
    fields = query.all()

    # Shared or local weather
    weather = db.query(models.WeatherModel).first()

    # Active registered devices for this user
    devices = db.query(models.DeviceTokenModel).filter(
        models.DeviceTokenModel.user_id == user_id,
        models.DeviceTokenModel.is_active == True
    ).all()

    generated_events = []
    deliveries_recorded = 0
    evaluation_logs = []

    priority_weight = {"LOW": 1, "MEDIUM": 2, "HIGH": 3, "CRITICAL": 4}
    min_pref_weight = priority_weight.get(pref.min_priority.upper(), 1)

    for field in fields:
        # Fetch latest satellite observation for this field
        satellite_rec = db.query(models.SatelliteRecordModel).filter(
            models.SatelliteRecordModel.field_id == field.id
        ).order_by(models.SatelliteRecordModel.recorded_date.desc()).first()

        # Fetch latest soil moisture reading for this field
        soil_moisture = db.query(models.SoilMoistureModel).filter(
            models.SoilMoistureModel.field_id == field.id
        ).first()

        candidate_alerts = AgriculturalDecisionEngine.evaluate_field(
            field=field,
            weather=weather,
            soil_moisture=soil_moisture,
            satellite_record=satellite_rec,
            current_time=now
        )

        for alert in candidate_alerts:
            cat = alert.get("category", "Agronomy")
            prio = alert.get("priority", "MEDIUM")
            prio_wt = priority_weight.get(prio, 2)

            # Preference: Check category toggle
            cat_enabled = True
            if cat == "Irrigation" and not pref.irrigation_enabled: cat_enabled = False
            elif cat == "Weather" and not pref.weather_enabled: cat_enabled = False
            elif cat == "Spraying" and not pref.spraying_enabled: cat_enabled = False
            elif cat == "Agronomy" and not pref.agronomy_enabled: cat_enabled = False
            elif cat == "CropHealth" and not pref.crop_health_enabled: cat_enabled = False
            elif cat == "Inspection" and not pref.inspection_enabled: cat_enabled = False

            if not cat_enabled:
                evaluation_logs.append(f"Field {field.id}: Skipped {cat} alert due to user category preference.")
                continue

            # Preference: Min priority check
            if prio_wt < min_pref_weight:
                evaluation_logs.append(f"Field {field.id}: Skipped alert priority {prio} < min_priority {pref.min_priority}.")
                continue

            # Quiet hours check: Critical or High alerts bypass quiet hours!
            if in_quiet and prio not in ["CRITICAL", "HIGH"]:
                evaluation_logs.append(f"Field {field.id}: Deferred non-urgent {prio} alert during quiet hours.")
                continue

            # Deduplication: Check if active or unexpired alert with this key already exists
            dedup_key = alert["deduplication_key"]
            existing_event = db.query(models.NotificationEventModel).filter(
                models.NotificationEventModel.user_id == user_id,
                models.NotificationEventModel.deduplication_key == dedup_key,
                models.NotificationEventModel.expires_at > now
            ).first()

            if existing_event:
                evaluation_logs.append(f"Field {field.id}: Suppressed duplicate alert '{dedup_key}'.")
                continue

            # Create notification event in DB
            event = models.NotificationEventModel(
                user_id=user_id,
                field_id=field.id,
                category=cat,
                priority=prio,
                deduplication_key=dedup_key,
                title_en=alert["title_en"],
                title_ta=alert.get("title_ta"),
                message_en=alert["message_en"],
                message_ta=alert.get("message_ta"),
                action_needed=alert.get("action_needed"),
                recommended_datetime=alert.get("recommended_datetime"),
                input_quantity=alert.get("input_quantity"),
                reason=alert.get("reason"),
                source=alert.get("source", "GeoPulse Agronomic Engine"),
                source_data_timestamp=alert.get("source_data_timestamp"),
                confidence=alert.get("confidence", "High (90%)"),
                created_at=now,
                expires_at=alert.get("expires_at"),
                is_read=False
            )
            db.add(event)
            db.flush()  # Obtain event.id
            generated_events.append(event)

            # Dispatch push notification to registered devices
            push_title = event.title_ta if farmer_lang == "ta" and event.title_ta else event.title_en
            push_message = event.message_ta if farmer_lang == "ta" and event.message_ta else event.message_en

            data_payload = {
                "notification_id": str(event.id),
                "field_id": str(field.id),
                "field_name": field.name,
                "category": event.category,
                "priority": event.priority,
                "action_needed": event.action_needed or "",
                "recommended_datetime": event.recommended_datetime or "",
                "input_quantity": event.input_quantity or "",
                "reason": event.reason or "",
                "confidence": event.confidence or ""
            }

            for device in devices:
                push_res = send_fcm_notification(
                    token=device.fcm_token,
                    title=push_title,
                    body=push_message,
                    data=data_payload,
                    priority=event.priority,
                    category=event.category
                )

                delivery = models.NotificationDeliveryModel(
                    event_id=event.id,
                    device_id=device.id,
                    status=push_res.get("status", "sent"),
                    fcm_message_id=push_res.get("message_id"),
                    error_message=push_res.get("error"),
                    sent_at=now
                )
                db.add(delivery)
                deliveries_recorded += 1

                # If token was marked unregistered, deactivate device record
                if push_res.get("status") == "unregistered":
                    device.is_active = False

    db.commit()

    # Format localized response schema
    out_events = []
    for ev in generated_events:
        db.refresh(ev)
        loc_title = ev.title_ta if farmer_lang == "ta" and ev.title_ta else ev.title_en
        loc_msg = ev.message_ta if farmer_lang == "ta" and ev.message_ta else ev.message_en
        field_obj = db.query(models.FieldModel).filter(models.FieldModel.id == ev.field_id).first()

        out_events.append({
            "id": ev.id,
            "user_id": ev.user_id,
            "field_id": ev.field_id,
            "field_name": field_obj.name if field_obj else None,
            "category": ev.category,
            "priority": ev.priority,
            "deduplication_key": ev.deduplication_key,
            "title": loc_title,
            "message": loc_msg,
            "title_en": ev.title_en,
            "title_ta": ev.title_ta,
            "message_en": ev.message_en,
            "message_ta": ev.message_ta,
            "action_needed": ev.action_needed,
            "recommended_datetime": ev.recommended_datetime,
            "input_quantity": ev.input_quantity,
            "reason": ev.reason,
            "source": ev.source,
            "source_data_timestamp": ev.source_data_timestamp,
            "confidence": ev.confidence,
            "created_at": ev.created_at,
            "expires_at": ev.expires_at,
            "is_read": ev.is_read,
            "read_at": ev.read_at
        })

    return {
        "evaluated_fields_count": len(fields),
        "notifications_generated_count": len(generated_events),
        "notifications_sent_count": deliveries_recorded,
        "notifications": out_events,
        "evaluation_summary": evaluation_logs
    }


def evaluate_all_active_users(db: Session) -> Dict[str, Any]:
    """
    Periodic worker task: Evaluates all farmers with saved fields.
    """
    users = db.query(models.UserModel).all()
    total_eval = 0
    total_gen = 0
    total_sent = 0

    for user in users:
        res = evaluate_user_fields(user.id, db)
        total_eval += res.get("evaluated_fields_count", 0)
        total_gen += res.get("notifications_generated_count", 0)
        total_sent += res.get("notifications_sent_count", 0)

    return {
        "farmers_evaluated": len(users),
        "fields_evaluated": total_eval,
        "notifications_generated": total_gen,
        "notifications_sent": total_sent
    }
