import os
import logging
import uuid
from typing import Dict, Any, Optional, List
from datetime import datetime

logger = logging.getLogger("geopulse.fcm")

# Flag for Firebase Admin initialization
_firebase_initialized = False
_is_simulation_mode = True

try:
    import firebase_admin
    from firebase_admin import credentials, messaging
    _has_firebase_sdk = True
except ImportError:
    _has_firebase_sdk = False
    logger.warning("[FCM Service] firebase_admin not installed. Running in simulation mode.")


def initialize_firebase() -> bool:
    """
    Initialize Firebase Admin SDK with service account credentials if provided.
    If credentials file does not exist, fall back safely to Local Simulation Mode.
    """
    global _firebase_initialized, _is_simulation_mode

    if _firebase_initialized:
        return not _is_simulation_mode

    cred_path = os.getenv("FIREBASE_CREDENTIALS") or os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    
    if _has_firebase_sdk and cred_path and os.path.exists(cred_path):
        try:
            cred = credentials.Certificate(cred_path)
            firebase_admin.initialize_app(cred)
            _firebase_initialized = True
            _is_simulation_mode = False
            logger.info(f"[FCM Service] Firebase Admin SDK initialized successfully with credentials: {cred_path}")
            return True
        except Exception as e:
            logger.error(f"[FCM Service] Failed to initialize Firebase Admin SDK: {e}. Falling back to simulation mode.")
            _firebase_initialized = True
            _is_simulation_mode = True
            return False
    else:
        _firebase_initialized = True
        _is_simulation_mode = True
        if not cred_path:
            logger.info("[FCM Service] FIREBASE_CREDENTIALS not configured. Running in Local Test / Simulation Mode.")
        else:
            logger.warning(f"[FCM Service] Credential file not found at '{cred_path}'. Running in Local Test / Simulation Mode.")
        return False


def is_simulation_mode() -> bool:
    """Return whether FCM service is running in local simulation mode."""
    if not _firebase_initialized:
        initialize_firebase()
    return _is_simulation_mode


def send_fcm_notification(
    token: str,
    title: str,
    body: str,
    data: Optional[Dict[str, str]] = None,
    priority: str = "HIGH",
    category: str = "Irrigation"
) -> Dict[str, Any]:
    """
    Send an intelligent push notification to an individual device FCM token.
    
    Android Channels:
    - geopulse_irrigation: Soil moisture & FAO-56 irrigation advisories
    - geopulse_weather: Severe weather, spray windows & rain warnings
    - geopulse_crop_health: Sentinel-2 NDVI & vegetation decline alerts
    - geopulse_agronomy: Fertilizer, sowing & field inspection tasks
    """
    if not _firebase_initialized:
        initialize_firebase()

    if not token or not token.strip():
        return {
            "status": "failed",
            "error": "Empty or invalid FCM device registration token",
            "message_id": None
        }

    # Map category to specific Android notification channel
    channel_mapping = {
        "Irrigation": "geopulse_irrigation",
        "Weather": "geopulse_weather",
        "Spraying": "geopulse_weather",
        "CropHealth": "geopulse_crop_health",
        "Agronomy": "geopulse_agronomy",
        "Inspection": "geopulse_agronomy"
    }
    channel_id = channel_mapping.get(category, "geopulse_advisories")

    # Stringify all data values for FCM compliance
    safe_data = {str(k): str(v) for k, v in (data or {}).items()}
    safe_data["category"] = category
    safe_data["priority"] = priority
    safe_data["click_action"] = "OPEN_NOTIFICATION_CENTER"

    if _is_simulation_mode or not _has_firebase_sdk:
        # Local Development & Testing Simulation Mode
        simulated_msg_id = f"projects/geopulse-farm/messages/sim-{uuid.uuid4().hex[:12]}"
        logger.info(
            f"[FCM SIMULATION] Push dispatched successfully!\n"
            f"  Target Token: {token[:20]}...\n"
            f"  Channel: {channel_id} | Priority: {priority}\n"
            f"  Title: {title}\n"
            f"  Body: {body}\n"
            f"  Data Payload: {safe_data}\n"
            f"  Simulated Message ID: {simulated_msg_id}"
        )
        return {
            "status": "simulated",
            "message_id": simulated_msg_id,
            "channel_id": channel_id,
            "error": None
        }

    try:
        # Construct Android notification payload
        android_config = messaging.AndroidConfig(
            priority="high" if priority in ["HIGH", "CRITICAL"] else "normal",
            notification=messaging.AndroidNotification(
                channel_id=channel_id,
                icon="ic_stat_geopulse_leaf",
                color="#2E7D32",
                sound="default",
                click_action="OPEN_NOTIFICATION_CENTER",
                tag=f"{category.lower()}_{data.get('field_id', 'general')}"
            )
        )

        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body
            ),
            data=safe_data,
            token=token,
            android=android_config
        )

        response = messaging.send(message)
        logger.info(f"[FCM Service] FCM message delivered successfully to token {token[:15]}... Message ID: {response}")
        return {
            "status": "sent",
            "message_id": response,
            "channel_id": channel_id,
            "error": None
        }

    except messaging.UnregisteredError as ue:
        logger.warning(f"[FCM Service] Stale/unregistered token detected: {token[:15]}... - {ue}")
        return {
            "status": "unregistered",
            "message_id": None,
            "error": f"Token unregistered or app uninstalled: {ue}"
        }
    except messaging.SenderIdMismatchError as se:
        logger.error(f"[FCM Service] Sender ID mismatch for token: {se}")
        return {
            "status": "unregistered",
            "message_id": None,
            "error": f"Sender ID mismatch: {se}"
        }
    except Exception as exc:
        logger.error(f"[FCM Service] FCM delivery failure: {exc}")
        return {
            "status": "failed",
            "message_id": None,
            "error": str(exc)
        }
