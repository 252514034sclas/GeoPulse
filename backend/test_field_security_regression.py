import unittest
from fastapi.testclient import TestClient
from main import app
from database import SessionLocal
import models
from services.auth_service import hash_password, create_access_token

class TestFieldSecurityRegression(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.client = TestClient(app)
        cls.db = SessionLocal()

        # Seed User 1 (Farmer Alpha)
        u1 = cls.db.query(models.UserModel).filter(models.UserModel.email == "farmer_alpha@geopulse.com").first()
        if not u1:
            u1 = models.UserModel(
                email="farmer_alpha@geopulse.com",
                full_name="Farmer Alpha",
                hashed_password=hash_password("PassAlpha123!"),
                farm_name="Alpha Green Acres",
                total_area_ha=15.0
            )
            cls.db.add(u1)
            cls.db.commit()
            cls.db.refresh(u1)
        cls.user1 = u1
        cls.token1 = create_access_token({"sub": u1.email, "id": u1.id})
        cls.headers1 = {"Authorization": f"Bearer {cls.token1}"}

        # Seed Field 1 for User 1
        f1 = cls.db.query(models.FieldModel).filter(models.FieldModel.user_id == u1.id).first()
        if not f1:
            f1 = models.FieldModel(
                user_id=u1.id,
                name="Alpha North Parcel",
                crop_type="Corn (Dent)",
                area_acres=12.5,
                soil_type="Loam",
                latitude=13.0827,
                longitude=80.2707,
                boundary="POLYGON ((80.27 13.08, 80.28 13.08, 80.28 13.09, 80.27 13.09, 80.27 13.08))"
            )
            cls.db.add(f1)
            cls.db.commit()
            cls.db.refresh(f1)
        cls.field1 = f1

        # Seed User 2 (Farmer Beta - Potential Adversary or Cross-User)
        u2 = cls.db.query(models.UserModel).filter(models.UserModel.email == "farmer_beta@geopulse.com").first()
        if not u2:
            u2 = models.UserModel(
                email="farmer_beta@geopulse.com",
                full_name="Farmer Beta",
                hashed_password=hash_password("PassBeta123!"),
                farm_name="Beta Delta Farmlands",
                total_area_ha=8.0
            )
            cls.db.add(u2)
            cls.db.commit()
            cls.db.refresh(u2)
        cls.user2 = u2
        cls.token2 = create_access_token({"sub": u2.email, "id": u2.id})
        cls.headers2 = {"Authorization": f"Bearer {cls.token2}"}

    @classmethod
    def tearDownClass(cls):
        cls.db.close()

    def test_arbitrary_field_id_returns_404(self):
        """Verify arbitrary non-existent field IDs return HTTP 404 across all field endpoints."""
        arbitrary_id = 999999

        endpoints_get = [
            f"/api/v1/fields/{arbitrary_id}",
            f"/api/v1/satellite/health/{arbitrary_id}",
            f"/api/v1/satellite/moisture/{arbitrary_id}",
            f"/api/v1/satellite/history/{arbitrary_id}",
            f"/api/v1/fields/{arbitrary_id}/irrigation",
            f"/api/v1/fields/{arbitrary_id}/sowing-recommendation",
            f"/api/v1/fields/{arbitrary_id}/satellite-live",
            f"/api/v1/satellite/multi-source/{arbitrary_id}",
            f"/api/v1/notifications?field_id={arbitrary_id}"
        ]

        for ep in endpoints_get:
            res = self.client.get(ep, headers=self.headers1)
            self.assertEqual(res.status_code, 404, f"Endpoint {ep} should return 404 for arbitrary field ID")

        # PUT and DELETE
        res_put = self.client.put(f"/api/v1/fields/{arbitrary_id}", headers=self.headers1, json={"name": "Hacked"})
        self.assertEqual(res_put.status_code, 404)

        res_del = self.client.delete(f"/api/v1/fields/{arbitrary_id}", headers=self.headers1)
        self.assertEqual(res_del.status_code, 404)

        # Notification evaluation with arbitrary field ID
        res_eval = self.client.post(f"/api/v1/notifications/evaluate?field_id={arbitrary_id}", headers=self.headers1)
        self.assertEqual(res_eval.status_code, 404)

    def test_cross_account_field_id_returns_403(self):
        """Verify Farmer Beta cannot query, modify, or evaluate Farmer Alpha's field (anti-IDOR)."""
        f1_id = self.field1.id

        cross_endpoints_get = [
            f"/api/v1/fields/{f1_id}",
            f"/api/v1/satellite/health/{f1_id}",
            f"/api/v1/satellite/moisture/{f1_id}",
            f"/api/v1/satellite/history/{f1_id}",
            f"/api/v1/fields/{f1_id}/irrigation",
            f"/api/v1/fields/{f1_id}/sowing-recommendation",
            f"/api/v1/fields/{f1_id}/satellite-live",
            f"/api/v1/satellite/multi-source/{f1_id}",
            f"/api/v1/notifications?field_id={f1_id}"
        ]

        for ep in cross_endpoints_get:
            res = self.client.get(ep, headers=self.headers2)
            self.assertEqual(res.status_code, 403, f"Endpoint {ep} must return 403 Forbidden for cross-account access")

        # Attempt to modify User 1's field as User 2
        res_put = self.client.put(f"/api/v1/fields/{f1_id}", headers=self.headers2, json={"name": "Beta Hijack"})
        self.assertEqual(res_put.status_code, 403)

        # Attempt to delete User 1's field as User 2
        res_del = self.client.delete(f"/api/v1/fields/{f1_id}", headers=self.headers2)
        self.assertEqual(res_del.status_code, 403)

        # Attempt to evaluate User 1's field as User 2
        res_eval = self.client.post(f"/api/v1/notifications/evaluate?field_id={f1_id}", headers=self.headers2)
        self.assertEqual(res_eval.status_code, 403)

        # Attempt to inject task on User 1's field as User 2
        res_task = self.client.post(
            "/api/v1/advisory/tasks",
            headers=self.headers2,
            json={
                "title": "Malicious Spray Task",
                "description": "Cross-user injection",
                "task_type": "Spraying",
                "field_id": f1_id
            }
        )
        self.assertEqual(res_task.status_code, 403)

        # Attempt to attach expense to User 1's field as User 2
        res_exp = self.client.post(
            "/api/v1/budget/expenses",
            headers=self.headers2,
            json={
                "category": "Fertilizer",
                "amount": 5000.0,
                "notes": "Unauthorized cross-account expense",
                "field_id": f1_id
            }
        )
        self.assertEqual(res_exp.status_code, 403)

    def test_unauthenticated_deep_links_return_401(self):
        """Verify unauthenticated requests to protected endpoints return 401 Unauthorized."""
        protected_endpoints = [
            "/api/v1/fields",
            f"/api/v1/fields/{self.field1.id}",
            f"/api/v1/satellite/health/{self.field1.id}",
            "/api/v1/notifications",
            "/api/v1/notifications/evaluate",
            "/api/v1/advisory/tasks",
            "/api/v1/budget/summary"
        ]

        for ep in protected_endpoints:
            res = self.client.get(ep) # No auth header
            self.assertEqual(res.status_code, 401, f"Protected endpoint {ep} must return 401 when unauthenticated")

    def test_authorized_user_can_access_own_field(self):
        """Verify Farmer Alpha can cleanly access their own field without errors."""
        res = self.client.get(f"/api/v1/fields/{self.field1.id}", headers=self.headers1)
        self.assertEqual(res.status_code, 200)
        data = res.json()
        self.assertEqual(data["name"], "Alpha North Parcel")
        self.assertEqual(data["user_id"], self.user1.id)

if __name__ == "__main__":
    unittest.main()
