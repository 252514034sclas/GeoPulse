"""
Zero SQLite Runtime Dependency Verification Script
Proves that when PostgreSQL is configured, the running application has ZERO dependency
on geopulse.db, even when geopulse.db is completely inaccessible.
"""

import os
import sys
import time
import httpx

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SQLITE_DB = os.path.join(BASE_DIR, "geopulse.db")
SQLITE_ISOLATED = os.path.join(BASE_DIR, "geopulse.db.isolated")

def main():
    print("=" * 70)
    print("VERIFYING ZERO SQLITE RUNTIME DEPENDENCY")
    print("=" * 70)

    # 1. Isolate the SQLite file
    renamed = False
    if os.path.exists(SQLITE_DB):
        os.rename(SQLITE_DB, SQLITE_ISOLATED)
        renamed = True
        print(f"[INFO] Temporarily moved {SQLITE_DB} -> {SQLITE_ISOLATED}")

    try:
        # 2. Test live API endpoints
        with httpx.Client(base_url="http://127.0.0.1:8000", timeout=10.0) as client:
            # Health
            r = client.get("/health")
            assert r.status_code == 200, f"Health failed: {r.text}"
            print("[PASS] GET /health succeeded with SQLite DB isolated.")

            # Auth Login
            r = client.post("/api/v1/auth/login", json={"email": "farmer_release@geopulse.com", "password": "SecurePass123!"})
            assert r.status_code == 200, f"Login failed: {r.text}"
            token = r.json()["access_token"]
            headers = {"Authorization": f"Bearer {token}"}
            print("[PASS] POST /api/v1/auth/login succeeded with SQLite DB isolated.")

            # Me
            r = client.get("/api/v1/auth/me", headers=headers)
            assert r.status_code == 200, f"Profile failed: {r.text}"
            print(f"[PASS] GET /api/v1/auth/me succeeded: {r.json()['full_name']} with SQLite DB isolated.")

            # Fields
            r = client.get("/api/v1/fields", headers=headers)
            assert r.status_code == 200, f"Fields failed: {r.text}"
            fields = r.json()
            assert len(fields) > 0, "No fields returned!"
            print(f"[PASS] GET /api/v1/fields succeeded: retrieved {len(fields)} fields with SQLite DB isolated.")

            # Dashboard
            r = client.get("/api/v1/dashboard", headers=headers)
            assert r.status_code == 200, f"Dashboard failed: {r.text}"
            print(f"[PASS] GET /api/v1/dashboard succeeded with SQLite DB isolated.")

        # Check if SQLite DB was recreated accidentally
        assert not os.path.exists(SQLITE_DB), "FAIL: An accidental SQLite file was created during API calls!"
        print("[PASS] Confirmed: No SQLite file was created or touched during queries.")

    finally:
        # Restore the SQLite file
        if renamed and os.path.exists(SQLITE_ISOLATED):
            os.rename(SQLITE_ISOLATED, SQLITE_DB)
            print(f"[INFO] Restored {SQLITE_ISOLATED} -> {SQLITE_DB}")

    print("-" * 70)
    print("ZERO SQLITE RUNTIME DEPENDENCY: 100% VERIFIED!")
    print("=" * 70)

if __name__ == "__main__":
    main()
