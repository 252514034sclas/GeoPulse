import unittest
import os
import sys
from fastapi.testclient import TestClient
from main import app
from services.auth_service import hash_password, verify_password, password_needs_rehash, DEFAULT_PBKDF2_ITERATIONS
from database import SessionLocal
import models
from adapters.agmarknet_adapter import AgmarknetAdapter
from adapters.isro_bhoonidhi_adapter import IsroBhoonidhiAdapter

class TestFinalReleaseSecurity(unittest.TestCase):
    def setUp(self):
        self.client = TestClient(app)
        self.db = SessionLocal()

    def tearDown(self):
        self.db.close()

    def test_pbkdf2_600k_iterations_format(self):
        """Verify modern password hashing uses 600,000 iterations and explicit format."""
        plain = "FarmerStrongPass2026!"
        h = hash_password(plain)
        self.assertTrue(h.startswith("pbkdf2:sha256:600000$"))
        self.assertTrue(verify_password(plain, h))
        self.assertFalse(verify_password("wrongpass", h))
        self.assertFalse(password_needs_rehash(h))

    def test_legacy_password_verification_and_migration(self):
        """Verify backward compatibility with 100,000 iteration hashes and automatic upgrade on login."""
        import hashlib
        salt = os.urandom(16)
        legacy_key = hashlib.pbkdf2_hmac("sha256", b"LegacyPass123", salt, 100000)
        legacy_hash = salt.hex() + "$" + legacy_key.hex()

        # 1. Verify legacy hash is accepted
        self.assertTrue(verify_password("LegacyPass123", legacy_hash))
        self.assertFalse(verify_password("WrongPass", legacy_hash))
        self.assertTrue(password_needs_rehash(legacy_hash))

        # 2. Test migration in database on login
        test_email = "migration_test_farmer@geopulse.com"
        user = self.db.query(models.UserModel).filter(models.UserModel.email == test_email).first()
        if not user:
            user = models.UserModel(
                email=test_email,
                full_name="Migration Test User",
                hashed_password=legacy_hash,
                farm_name="Test Farm"
            )
            self.db.add(user)
            self.db.commit()
            self.db.refresh(user)
        else:
            user.hashed_password = legacy_hash
            self.db.commit()

        # Login with legacy password
        res = self.client.post("/api/v1/auth/login", json={"email": test_email, "password": "LegacyPass123"})
        self.assertEqual(res.status_code, 200)

        # Verify password in DB has been upgraded to 600k iterations
        self.db.refresh(user)
        self.assertTrue(user.hashed_password.startswith("pbkdf2:sha256:600000$"))
        self.assertFalse(password_needs_rehash(user.hashed_password))

    def test_rate_limiter_enforcement(self):
        """Verify rate limiter throws HTTP 429 when max requests are exceeded."""
        # 11 rapid login attempts on dummy endpoint
        status_codes = []
        for i in range(15):
            res = self.client.post("/api/v1/auth/login", json={"email": "ratelimit@test.com", "password": "wrong"})
            status_codes.append(res.status_code)

        # Must contain at least one 429 Too Many Requests
        self.assertIn(429, status_codes)

    def test_logout_endpoint_documentation(self):
        """Verify logout endpoint returns statutory token expiration documentation."""
        res = self.client.post("/api/v1/auth/logout")
        self.assertEqual(res.status_code, 200)
        data = res.json()
        self.assertEqual(data["status"], "LOGGED_OUT")
        self.assertEqual(data["server_jwt_revocation"], "STATUTORY_EXPIRATION_ONLY")

    def test_forgot_password_transparent_status(self):
        """Verify forgot-password endpoint never claims email was sent when SMTP is unconfigured."""
        res = self.client.post("/api/v1/auth/forgot-password", json={"email": "farmer@example.com"})
        self.assertEqual(res.status_code, 200)
        data = res.json()
        self.assertEqual(data["status"], "DELIVERY_UNAVAILABLE")
        self.assertFalse(data["email_sent"])
        self.assertIn("not configured", data["message"].lower())

    def test_agmarknet_historical_benchmark_transparency(self):
        """Verify AGMARKNET adapter labels fallback data as HISTORICAL_BENCHMARK_REFERENCE, not live."""
        adapter = AgmarknetAdapter()
        prices = adapter.get_mandi_prices()
        self.assertTrue(len(prices) > 0)
        for p in prices:
            if not adapter.is_configured:
                self.assertFalse(p["is_live"])
                self.assertEqual(p["data_status"], "HISTORICAL_BENCHMARK_REFERENCE")
                self.assertEqual(p["arrival_date"], "2026-11-15")

    def test_bhoonidhi_radar_not_implemented(self):
        """Verify ISRO adapter explicitly reports radar processing as NOT IMPLEMENTED."""
        adapter = IsroBhoonidhiAdapter()
        status_info = adapter.get_status()
        self.assertIn("NOT IMPLEMENTED", status_info["radar_processing_status"])
        eos04 = adapter.query_eos04_soil_moisture(10.96, 79.38)
        self.assertEqual(eos04["status"], "NOT_IMPLEMENTED")
        self.assertIsNone(eos04["measured_value"])

if __name__ == "__main__":
    unittest.main()
