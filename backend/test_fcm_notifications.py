import unittest
from datetime import datetime, timedelta
import httpx

BASE_URL = "http://127.0.0.1:8000"

class TestIntelligentNotifications(unittest.TestCase):
    """
    Comprehensive verification suite for GeoPulse Intelligent Farmer Notification Subsystem:
    - 7 Agricultural decision rules
    - FCM token registration & lifecycle
    - Preference customization (quiet hours, languages, categories)
    - Anti-spam deduplication & cooldown
    - Strict Multi-tenant / Anti-IDOR user isolation
    - Dual English & Tamil localization
    - Delivery audit trail in PostgreSQL
    """

    @classmethod
    def setUpClass(cls):
        cls.client = httpx.Client(base_url=BASE_URL, timeout=10.0)

        # Farmer A (English)
        ts = int(datetime.utcnow().timestamp())
        cls.user_a_email = f"farmer_a_{ts}@geopulse.test"
        cls.user_a_password = "SecurePassword2026!"
        reg_a = cls.client.post("/api/v1/auth/register", json={
            "email": cls.user_a_email,
            "password": cls.user_a_password,
            "full_name": "Farmer Arumugam",
            "phone": "9840123456",
            "farm_name": "Arumugam Delta Farm",
            "preferred_language": "en"
        })
        assert reg_a.status_code in [200, 201], f"Farmer A register failed: {reg_a.text}"
        cls.token_a = reg_a.json()["access_token"]
        cls.headers_a = {"Authorization": f"Bearer {cls.token_a}"}

        # Farmer B (Tamil)
        cls.user_b_email = f"farmer_b_{ts}@geopulse.test"
        cls.user_b_password = "SecurePassword2026!"
        reg_b = cls.client.post("/api/v1/auth/register", json={
            "email": cls.user_b_email,
            "password": cls.user_b_password,
            "full_name": "Farmer Balan",
            "phone": "9840987654",
            "farm_name": "Balan Organic Farm",
            "preferred_language": "ta"
        })
        assert reg_b.status_code in [200, 201], f"Farmer B register failed: {reg_b.text}"
        cls.token_b = reg_b.json()["access_token"]
        cls.headers_b = {"Authorization": f"Bearer {cls.token_b}"}

        # Create a field for Farmer A (Paddy)
        field_a_res = cls.client.post("/api/v1/fields/draw", params={
            "name": "North Paddy Parcel",
            "crop_type": "Paddy (Rice)",
            "boundary_wkt": "POLYGON((79.13 10.78, 79.14 10.78, 79.14 10.79, 79.13 10.79, 79.13 10.78))",
            "sowing_date": (datetime.utcnow() - timedelta(days=22)).strftime("%Y-%m-%d")  # DAS 22: Active tillering
        }, headers=cls.headers_a)
        assert field_a_res.status_code in [200, 201], f"Field A create failed: {field_a_res.text}"
        cls.field_a_id = field_a_res.json()["id"]

        # Create a field for Farmer B (Cotton)
        field_b_res = cls.client.post("/api/v1/fields/draw", params={
            "name": "East Cotton Field",
            "crop_type": "Cotton",
            "boundary_wkt": "POLYGON((78.10 9.90, 78.11 9.90, 78.11 9.91, 78.10 9.91, 78.10 9.90))",
            "sowing_date": (datetime.utcnow() - timedelta(days=35)).strftime("%Y-%m-%d")
        }, headers=cls.headers_b)
        assert field_b_res.status_code in [200, 201], f"Field B create failed: {field_b_res.text}"
        cls.field_b_id = field_b_res.json()["id"]

    @classmethod
    def tearDownClass(cls):
        cls.client.close()

    def test_01_device_token_lifecycle(self):
        """Test registering, refreshing, and deactivating FCM push tokens."""
        test_fcm_token = "fcm_test_token_pixel10pro_abc123xyz"

        # Register token
        reg_res = self.client.post("/api/v1/notifications/devices/register", json={
            "fcm_token": test_fcm_token,
            "device_type": "android",
            "device_name": "Google Pixel 10 Pro",
            "app_version": "1.0.0"
        }, headers=self.headers_a)
        self.assertEqual(reg_res.status_code, 200)
        data = reg_res.json()
        self.assertEqual(data["fcm_token"], test_fcm_token)
        self.assertTrue(data["is_active"])

        # Re-register token (should refresh without duplicating row)
        refresh_res = self.client.post("/api/v1/notifications/devices/register", json={
            "fcm_token": test_fcm_token,
            "device_type": "android",
            "device_name": "Google Pixel 10 Pro (Refreshed)",
            "app_version": "1.0.1"
        }, headers=self.headers_a)
        self.assertEqual(refresh_res.status_code, 200)
        self.assertEqual(refresh_res.json()["id"], data["id"])

        # Unregister token
        unreg_res = self.client.post("/api/v1/notifications/devices/unregister", json={
            "fcm_token": test_fcm_token
        }, headers=self.headers_a)
        self.assertEqual(unreg_res.status_code, 200)
        self.assertEqual(unreg_res.json()["status"], "deactivated")

        # Reactivate for subsequent tests
        self.client.post("/api/v1/notifications/devices/register", json={
            "fcm_token": test_fcm_token,
            "device_type": "android"
        }, headers=self.headers_a)

    def test_02_notification_preferences(self):
        """Test retrieving and updating farmer notification preferences."""
        # Get defaults
        get_res = self.client.get("/api/v1/notifications/preferences", headers=self.headers_a)
        self.assertEqual(get_res.status_code, 200)
        prefs = get_res.json()
        self.assertTrue(prefs["quiet_hours_enabled"])
        self.assertEqual(prefs["quiet_hours_start"], "22:00")
        self.assertEqual(prefs["quiet_hours_end"], "06:00")
        self.assertEqual(prefs["language"], "en")

        # Update quiet hours and language
        update_res = self.client.put("/api/v1/notifications/preferences", json={
            "quiet_hours_start": "23:00",
            "quiet_hours_end": "05:30",
            "language": "en",
            "min_priority": "LOW"
        }, headers=self.headers_a)
        self.assertEqual(update_res.status_code, 200)
        up_prefs = update_res.json()
        self.assertEqual(up_prefs["quiet_hours_start"], "23:00")
        self.assertEqual(up_prefs["quiet_hours_end"], "05:30")

    def test_03_decision_engine_evaluation_and_deduplication(self):
        """Test agricultural decision engine evaluation, field targeting, and deduplication."""
        # Trigger evaluation for Farmer A
        eval_res = self.client.post("/api/v1/notifications/evaluate", params={
            "field_id": self.field_a_id,
            "bypass_quiet_hours": True
        }, headers=self.headers_a)
        self.assertEqual(eval_res.status_code, 200)
        eval_data = eval_res.json()

        self.assertEqual(eval_data["evaluated_fields_count"], 1)
        self.assertGreater(eval_data["notifications_generated_count"], 0)
        self.assertGreater(eval_data["notifications_sent_count"], 0)

        # Inspect generated notifications
        notifs = eval_data["notifications"]
        categories = [n["category"] for n in notifs]
        self.assertTrue(any(c in ["Irrigation", "Weather", "Agronomy", "CropHealth", "Spraying"] for c in categories))

        for n in notifs:
            self.assertEqual(n["field_id"], self.field_a_id)
            self.assertIsNotNone(n["title"])
            self.assertIsNotNone(n["message"])
            self.assertIsNotNone(n["deduplication_key"])
            self.assertIsNotNone(n["reason"])
            self.assertIsNotNone(n["source"])
            self.assertIsNotNone(n["confidence"])

        # Second immediate evaluation must NOT generate duplicate notifications!
        eval_dup_res = self.client.post("/api/v1/notifications/evaluate", params={
            "field_id": self.field_a_id,
            "bypass_quiet_hours": True
        }, headers=self.headers_a)
        self.assertEqual(eval_dup_res.status_code, 200)
        self.assertEqual(eval_dup_res.json()["notifications_generated_count"], 0, "Duplicate notifications were generated!")

    def test_04_tamil_language_localization(self):
        """Test authentic Tamil language rendering for Tamil-speaking farmer."""
        # Farmer B registered with preferred_language = 'ta'
        # Register a device for Farmer B
        self.client.post("/api/v1/notifications/devices/register", json={
            "fcm_token": "fcm_test_token_farmer_b_tamil",
            "device_type": "android"
        }, headers=self.headers_b)

        eval_b_res = self.client.post("/api/v1/notifications/evaluate", params={
            "field_id": self.field_b_id,
            "bypass_quiet_hours": True
        }, headers=self.headers_b)
        self.assertEqual(eval_b_res.status_code, 200)
        notifs_b = eval_b_res.json()["notifications"]

        self.assertGreater(len(notifs_b), 0)
        # Check that title and message are in Tamil
        sample_b = notifs_b[0]
        self.assertIsNotNone(sample_b["title_ta"])
        self.assertIsNotNone(sample_b["message_ta"])
        self.assertEqual(sample_b["title"], sample_b["title_ta"])
        self.assertEqual(sample_b["message"], sample_b["message_ta"])

    def test_05_anti_idor_and_user_isolation(self):
        """Test strict anti-IDOR isolation: Farmer A cannot access Farmer B's notifications."""
        # Get Farmer A's notifications
        res_a = self.client.get("/api/v1/notifications", headers=self.headers_a)
        self.assertEqual(res_a.status_code, 200)
        notifs_a = res_a.json()
        self.assertGreater(len(notifs_a), 0)
        notif_a_id = notifs_a[0]["id"]

        # Farmer B attempts to fetch Farmer A's notification detail -> MUST return 403 Forbidden!
        res_b_try_a = self.client.get(f"/api/v1/notifications/{notif_a_id}", headers=self.headers_b)
        self.assertEqual(res_b_try_a.status_code, 403, "IDOR Vulnerability: Farmer B could access Farmer A's notification!")

        # Farmer B attempts to mark Farmer A's notification as read -> MUST return 403 Forbidden!
        res_b_mark_a = self.client.post(f"/api/v1/notifications/{notif_a_id}/read", headers=self.headers_b)
        self.assertEqual(res_b_mark_a.status_code, 403, "IDOR Vulnerability: Farmer B could modify Farmer A's notification!")

        # Farmer B attempts to evaluate advisories on Farmer A's field -> MUST return 403 Forbidden!
        res_b_eval_a = self.client.post("/api/v1/notifications/evaluate", params={"field_id": self.field_a_id}, headers=self.headers_b)
        self.assertEqual(res_b_eval_a.status_code, 403, "IDOR Vulnerability: Farmer B could trigger evaluation on Farmer A's field!")

        # Farmer B attempts to query notifications for Farmer A's field -> MUST return 403 Forbidden!
        res_b_query_a = self.client.get("/api/v1/notifications", params={"field_id": self.field_a_id}, headers=self.headers_b)
        self.assertEqual(res_b_query_a.status_code, 403, "IDOR Vulnerability: Farmer B could query notifications for Farmer A's field!")

        # Non-existent field evaluation -> MUST return 404 Not Found!
        res_404_eval = self.client.post("/api/v1/notifications/evaluate", params={"field_id": 999999}, headers=self.headers_b)
        self.assertEqual(res_404_eval.status_code, 404)

    def test_06_mark_as_read_and_history(self):
        """Test mark as read and notification history querying."""
        res_a = self.client.get("/api/v1/notifications", headers=self.headers_a)
        notifs = res_a.json()
        self.assertGreater(len(notifs), 0)
        target = notifs[0]

        # Mark individual notification as read
        read_res = self.client.post(f"/api/v1/notifications/{target['id']}/read", headers=self.headers_a)
        self.assertEqual(read_res.status_code, 200)
        self.assertTrue(read_res.json()["is_read"])

        # Check detail reflects is_read = True
        detail_res = self.client.get(f"/api/v1/notifications/{target['id']}", headers=self.headers_a)
        self.assertTrue(detail_res.json()["is_read"])

        # Mark all read
        mark_all_res = self.client.post("/api/v1/notifications/read-all", headers=self.headers_a)
        self.assertEqual(mark_all_res.status_code, 200)

        # Unread filter returns 0
        unread_res = self.client.get("/api/v1/notifications", params={"unread_only": True}, headers=self.headers_a)
        self.assertEqual(len(unread_res.json()), 0)

    def test_07_delivery_audit_log(self):
        """Test PostgreSQL delivery audit trail."""
        del_res = self.client.get("/api/v1/notifications/deliveries", headers=self.headers_a)
        self.assertEqual(del_res.status_code, 200)
        deliveries = del_res.json()
        self.assertGreater(len(deliveries), 0)
        sample = deliveries[0]
        self.assertIn(sample["status"], ["sent", "simulated"])
        self.assertIsNotNone(sample["sent_at"])


if __name__ == "__main__":
    unittest.main()
