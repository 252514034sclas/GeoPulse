import asyncio
import logging
from datetime import datetime, time, timezone, timedelta
from typing import Dict, Any, List, Optional
from sqlalchemy.orm import Session

import models
from database import SessionLocal
from services.agricultural_decision_engine import AgriculturalDecisionEngine
from services.fcm_service import send_fcm_notification, is_simulation_mode
from adapters.open_meteo_adapter import OpenMeteoAdapter

logger = logging.getLogger("geopulse.notification_scheduler")

IST_TIMEZONE = timezone(timedelta(hours=5, minutes=30), name="Asia/Kolkata")


def to_ist(dt: Optional[datetime]) -> datetime:
    """Convert any datetime to Asia/Kolkata (IST)."""
    if dt is None:
        return datetime.now(IST_TIMEZONE)
    if dt.tzinfo is None:
        return dt.replace(tzinfo=IST_TIMEZONE)
    return dt.astimezone(IST_TIMEZONE)


def to_utc_naive(dt: Optional[datetime]) -> Optional[datetime]:
    """Convert datetime to naive UTC for PostgreSQL storage."""
    if dt is None:
        return None
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=IST_TIMEZONE)
    return dt.astimezone(timezone.utc).replace(tzinfo=None)


def is_in_quiet_hours(quiet_start: str, quiet_end: str, now_dt: Optional[datetime] = None) -> bool:
    """
    Check if the current time falls within farmer quiet hours (e.g., 22:00 to 06:00 IST).
    Evaluated strictly against Asia/Kolkata (IST) local time.
    """
    dt = to_ist(now_dt)
    current_time = dt.time()

    try:
        sh, sm = map(int, quiet_start.split(":"))
        eh, em = map(int, quiet_end.split(":"))
        start_time = time(sh, sm)
        end_time = time(eh, em)

        if start_time <= end_time:
            return start_time <= current_time < end_time
        else:
            # Over midnight window (e.g., 22:00 to 06:00 IST)
            return current_time >= start_time or current_time < end_time
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
    enforce deduplication and quiet-hour rules in Asia/Kolkata IST, and dispatch FCM push alerts.
    """
    now_ist = to_ist(now_dt)
    now_utc = to_utc_naive(now_ist)

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

    # Quiet hours check in IST
    in_quiet = False
    if pref.quiet_hours_enabled and not bypass_quiet_hours:
        in_quiet = is_in_quiet_hours(pref.quiet_hours_start, pref.quiet_hours_end, now_ist)

    # Query fields belonging strictly to this user (anti-IDOR enforcement)
    query = db.query(models.FieldModel).filter(models.FieldModel.user_id == user_id)
    if target_field_id is not None:
        query = query.filter(models.FieldModel.id == target_field_id)
    fields = query.all()

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
        # Fetch field-specific live or cached meteorological conditions
        field_weather = OpenMeteoAdapter.get_forecast(field.latitude, field.longitude)

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
            weather=field_weather,
            soil_moisture=soil_moisture,
            satellite_record=satellite_rec,
            current_time=now_ist
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
                models.NotificationEventModel.expires_at > now_utc
            ).first()

            if existing_event:
                evaluation_logs.append(f"Field {field.id}: Suppressed duplicate alert '{dedup_key}'.")
                continue

            # Calculate expiry time in UTC
            raw_expiry = alert.get("expires_at")
            expires_at_utc = to_utc_naive(raw_expiry) if raw_expiry else None

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
                created_at=now_utc,
                expires_at=expires_at_utc,
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
                    sent_at=now_utc
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


def evaluate_all_active_users(db: Session, now_dt: Optional[datetime] = None) -> Dict[str, Any]:
    """
    Periodic worker task: Evaluates all farmers with saved fields.
    """
    users = db.query(models.UserModel).all()
    total_eval = 0
    total_gen = 0
    total_sent = 0
    now_ist = to_ist(now_dt)

    for user in users:
        try:
            res = evaluate_user_fields(user.id, db, now_dt=now_ist)
            total_eval += res.get("evaluated_fields_count", 0)
            total_gen += res.get("notifications_generated_count", 0)
            total_sent += res.get("notifications_sent_count", 0)
        except Exception as e:
            logger.error(f"[Scheduler] Failed evaluating user {user.id}: {e}")

    return {
        "farmers_evaluated": len(users),
        "fields_evaluated": total_eval,
        "notifications_generated": total_gen,
        "notifications_sent": total_sent,
        "executed_at": now_ist.isoformat(),
        "timezone": "Asia/Kolkata"
    }


# ==============================================================================
# AUTOMATED SCHEDULER ENGINE & HEALTH TRACKER
# ==============================================================================

class NotificationSchedulerService:
    """
    Automated In-Process Scheduler Daemon for GeoPulse Agricultural Intelligence.
    Maintains persistent state, execution heartbeats, 06:00 IST morning job dispatch,
    periodic microclimate event polling, and error isolation.
    """

    def __init__(self):
        self._is_running = False
        self._task: Optional[asyncio.Task] = None
        self._heartbeat_at: Optional[datetime] = None
        self._last_run_at: Optional[datetime] = None
        self._last_daily_run_date: Optional[str] = None
        self._last_run_stats: Dict[str, Any] = {}
        self._execution_history: List[Dict[str, Any]] = []
        self._error_log: List[str] = []

    def get_status(self) -> Dict[str, Any]:
        """Return real-time scheduler state and telemetry."""
        now_ist = datetime.now(IST_TIMEZONE)
        next_0600 = now_ist.replace(hour=6, minute=0, second=0, microsecond=0)
        if now_ist >= next_0600:
            next_0600 = next_0600 + timedelta(days=1)

        return {
            "scheduler_operational": self._is_running,
            "cloud_scheduler_operational": False,
            "engine": "InProcessAsyncTimerDaemon",
            "timezone": "Asia/Kolkata",
            "current_time_ist": now_ist.strftime("%Y-%m-%d %H:%M:%S IST"),
            "heartbeat_at": self._heartbeat_at.isoformat() if self._heartbeat_at else None,
            "last_run_at": self._last_run_at.isoformat() if self._last_run_at else None,
            "next_daily_run_at": next_0600.strftime("%Y-%m-%d %H:%M:%S IST"),
            "daily_run_target": "06:00 IST",
            "last_run_stats": self._last_run_stats,
            "execution_history_count": len(self._execution_history),
            "recent_executions": self._execution_history[-5:],
            "deployment_status": "LOCAL_DEVELOPMENT_STANDALONE",
            "cloud_deployment_status": "PENDING_PRODUCTION_INFRASTRUCTURE",
            "fcm_delivery_mode": "SIMULATION_MODE" if is_simulation_mode() else "LIVE_HTTP_V1",
            "notes": (
                "Local in-process asyncio scheduler daemon is active and tracks 06:00 IST in Asia/Kolkata. "
                "Production cloud scheduler (AWS EventBridge / Cloud Scheduler / Celery Redis) and live remote FCM key "
                "remain pending deployment in the production cloud environment."
            )
        }

    def trigger_scheduled_run(self, db: Optional[Session] = None, force: bool = False) -> Dict[str, Any]:
        """Execute a deterministic scheduled evaluation pass across all farmers."""
        now_ist = datetime.now(IST_TIMEZONE)
        today_date = now_ist.strftime("%Y-%m-%d")

        if not force and self._last_daily_run_date == today_date:
            return {"status": "skipped", "reason": f"Already executed for date {today_date}"}

        close_session = False
        if db is None:
            db = SessionLocal()
            close_session = True

        try:
            stats = evaluate_all_active_users(db, now_dt=now_ist)
            self._last_run_at = now_ist
            self._last_daily_run_date = today_date
            self._last_run_stats = stats

            entry = {
                "timestamp": now_ist.isoformat(),
                "status": "success",
                "stats": stats
            }
            self._execution_history.append(entry)
            if len(self._execution_history) > 50:
                self._execution_history.pop(0)

            logger.info(f"[NotificationScheduler] Automated run completed at {now_ist.isoformat()}: {stats}")
            return stats
        except Exception as e:
            err_msg = f"[NotificationScheduler] Error during scheduled run at {now_ist.isoformat()}: {e}"
            logger.error(err_msg)
            self._error_log.append(err_msg)
            self._execution_history.append({
                "timestamp": now_ist.isoformat(),
                "status": "error",
                "error": str(e)
            })
            return {"status": "error", "error": str(e)}
        finally:
            if close_session:
                db.close()

    async def _scheduler_loop(self):
        """Async timer loop: checks heartbeats every 30s and triggers 06:00 IST run."""
        logger.info("[NotificationScheduler] Background loop started in Asia/Kolkata timezone.")
        while self._is_running:
            try:
                now_ist = datetime.now(IST_TIMEZONE)
                self._heartbeat_at = now_ist

                # Check if it is currently 06:00 IST (within 06:00 to 06:05) and not yet run today
                today_date = now_ist.strftime("%Y-%m-%d")
                if now_ist.hour == 6 and now_ist.minute < 5:
                    if self._last_daily_run_date != today_date:
                        logger.info(f"[NotificationScheduler] 06:00 IST trigger reached for {today_date}.")
                        self.trigger_scheduled_run(force=True)

                await asyncio.sleep(30)
            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"[NotificationScheduler] Loop iteration error: {e}")
                await asyncio.sleep(10)

    def start(self):
        """Start the background scheduler task."""
        if self._is_running:
            return
        self._is_running = True
        self._heartbeat_at = datetime.now(IST_TIMEZONE)
        try:
            loop = asyncio.get_event_loop()
            if loop.is_running():
                self._task = loop.create_task(self._scheduler_loop())
            else:
                logger.info("[NotificationScheduler] Event loop not running yet. Task will attach on FastAPI startup.")
        except RuntimeError:
            logger.info("[NotificationScheduler] No active event loop. Will attach on FastAPI startup.")

    def stop(self):
        """Stop the background scheduler task."""
        self._is_running = False
        if self._task and not self._task.done():
            self._task.cancel()
        logger.info("[NotificationScheduler] Background daemon stopped.")


# Singleton instance
notification_scheduler = NotificationSchedulerService()
