"""
GEOPULSE — MULTI-USER DATA ISOLATION & SECURITY AUDIT TEST SUITE
Authoritative verification of Phase 1, Phase 2, and Phase 3:
- Real User Registration & Password Hashing
- Input Validation (Invalid Email, Short Password, Duplicate Accounts)
- Authentication & JWT Token Verification
- Expired Token Rejection
- Strict Multi-User Isolation (Account A vs Account B)
- Anti-IDOR (Insecure Direct Object Reference) Protection on all endpoints
- Ownership Checks across Fields, Satellite, Hydrology, Tasks, Budgets, and Alerts
"""

import uuid
import time
from fastapi.testclient import TestClient
from main import app
from database import engine, SessionLocal
import models
from services.auth_service import create_access_token

def run_multi_user_isolation_suite():
    print("=" * 80)
    print("GEOPULSE MULTI-USER DATA ISOLATION & SECURITY AUDIT SUITE")
    print("=" * 80)

    client = TestClient(app)
    db = SessionLocal()

    suffix_a = str(uuid.uuid4())[:8]
    suffix_b = str(uuid.uuid4())[:8]

    email_a = f"farmer_alpha_{suffix_a}@geopulse.com"
    email_b = f"farmer_beta_{suffix_b}@geopulse.com"
    pass_a = "AlphaPass123!"
    pass_b = "BetaPass123!"

    # -------------------------------------------------------------
    # PHASE 2: REGISTRATION INPUT VALIDATION & SECURITY
    # -------------------------------------------------------------
    print("\n--- PHASE 2: REGISTRATION & INPUT VALIDATION ---")

    # 1. Reject invalid email format
    res = client.post("/api/v1/auth/register", json={
        "email": "invalid-email-no-at",
        "password": "ValidPassword123!",
        "full_name": "Test Farmer"
    })
    assert res.status_code == 400
    print("[PASS] Registration rejected invalid email format (no @/.) -> 400 Bad Request")

    # 2. Reject short password (< 6 chars)
    res = client.post("/api/v1/auth/register", json={
        "email": f"short_pass_{suffix_a}@geopulse.com",
        "password": "123",
        "full_name": "Test Farmer"
    })
    assert res.status_code == 400
    print("[PASS] Registration rejected short password (<6 chars) -> 400 Bad Request")

    # 3. Reject short full name (< 2 chars)
    res = client.post("/api/v1/auth/register", json={
        "email": f"short_name_{suffix_a}@geopulse.com",
        "password": "ValidPassword123!",
        "full_name": "A"
    })
    assert res.status_code == 400
    print("[PASS] Registration rejected blank/short full name -> 400 Bad Request")

    # 4. Register Account A
    res_reg_a = client.post("/api/v1/auth/register", json={
        "email": email_a,
        "password": pass_a,
        "full_name": "Farmer Alpha (Thanjavur)",
        "phone": "+91 94431 11111",
        "farm_name": "Alpha Delta BioFarm",
        "total_area_ha": 25.0,
        "preferred_language": "ta"
    })
    assert res_reg_a.status_code == 200
    token_a = res_reg_a.json()["access_token"]
    user_a = res_reg_a.json()["user"]
    headers_a = {"Authorization": f"Bearer {token_a}"}
    print(f"[PASS] Account A registered successfully: {email_a} (User ID: {user_a['id']})")

    # 5. Reject duplicate registration for Account A
    res_dup = client.post("/api/v1/auth/register", json={
        "email": email_a,
        "password": "AnotherPassword123!",
        "full_name": "Imposter Farmer"
    })
    assert res_dup.status_code == 400
    print("[PASS] Registration rejected duplicate account email -> 400 Bad Request")

    # 6. Login validation: incorrect password
    res_bad_login = client.post("/api/v1/auth/login", json={
        "email": email_a,
        "password": "WrongPassword999!"
    })
    assert res_bad_login.status_code == 401
    print("[PASS] Login rejected incorrect password -> 401 Unauthorized")

    # 7. Login validation: valid credentials
    res_login_a = client.post("/api/v1/auth/login", json={
        "email": email_a,
        "password": pass_a
    })
    assert res_login_a.status_code == 200
    assert "access_token" in res_login_a.json()
    print("[PASS] Login succeeded with authentic credentials -> 200 OK + JWT")

    # 8. Register Account B (Completely Separate Farmer)
    res_reg_b = client.post("/api/v1/auth/register", json={
        "email": email_b,
        "password": pass_b,
        "full_name": "Farmer Beta (Tiruppur)",
        "phone": "+91 98422 22222",
        "farm_name": "Beta Organic Cotton Estate",
        "total_area_ha": 40.0,
        "preferred_language": "en"
    })
    assert res_reg_b.status_code == 200
    token_b = res_reg_b.json()["access_token"]
    user_b = res_reg_b.json()["user"]
    headers_b = {"Authorization": f"Bearer {token_b}"}
    print(f"[PASS] Account B registered successfully: {email_b} (User ID: {user_b['id']})")

    # 9. Token Expiration Security: Expired token rejection
    expired_token = create_access_token({"sub": email_a, "id": user_a["id"]}, expires_delta_seconds=-3600)
    res_exp = client.get("/api/v1/auth/me", headers={"Authorization": f"Bearer {expired_token}"})
    assert res_exp.status_code == 401
    print("[PASS] Expired JWT token properly rejected -> 401 Unauthorized")

    # -------------------------------------------------------------
    # PHASE 3: MULTI-USER DATA CREATION & ISOLATION
    # -------------------------------------------------------------
    print("\n--- PHASE 3: MULTI-USER RESOURCE CREATION & IDOR AUDIT ---")

    # Account A creates Field A
    poly_a = "POLYGON((79.130 10.780, 79.140 10.780, 79.140 10.790, 79.130 10.790, 79.130 10.780))"
    res_fa = client.post(
        f"/api/v1/fields/draw?name=Alpha+Samba+Paddy+Plot&crop_type=Paddy&boundary_wkt={poly_a}&sowing_date=2026-10-01",
        headers=headers_a
    )
    assert res_fa.status_code == 200
    field_a = res_fa.json()
    field_a_id = field_a["id"]
    assert field_a["user_id"] == user_a["id"]
    print(f"[PASS] Account A created Field A (ID: {field_a_id}, Name: '{field_a['name']}', Owner: {field_a['user_id']})")

    # Account A creates Advisory Task A
    res_ta = client.post(
        "/api/v1/advisory/tasks",
        json={
            "field_id": field_a_id,
            "task_type": "Irrigation",
            "title": "Alpha Private Task: Water Paddy Nursery",
            "description": "Maintain 2 inches standing water.",
            "time_badge": "Today",
            "priority": "High"
        },
        headers=headers_a
    )
    assert res_ta.status_code == 200
    task_a_id = res_ta.json()["id"]
    print(f"[PASS] Account A created Task A (ID: {task_a_id})")

    # Account A creates Expense A
    res_ea = client.post(
        "/api/v1/budget/expenses",
        json={
            "field_id": field_a_id,
            "category": "Seeds",
            "amount": 7500.0,
            "notes": "Alpha Certified Samba Seeds"
        },
        headers=headers_a
    )
    assert res_ea.status_code == 200
    expense_a_id = res_ea.json()["id"]
    print(f"[PASS] Account A created Expense A (ID: {expense_a_id}, Amount: INR 7500)")

    # Account B creates Field B
    poly_b = "POLYGON((77.340 11.100, 77.350 11.100, 77.350 11.110, 77.340 11.110, 77.340 11.100))"
    res_fb = client.post(
        f"/api/v1/fields/draw?name=Beta+Bt+Cotton+Quadrant&crop_type=Cotton&boundary_wkt={poly_b}&sowing_date=2026-10-15",
        headers=headers_b
    )
    assert res_fb.status_code == 200
    field_b = res_fb.json()
    field_b_id = field_b["id"]
    assert field_b["user_id"] == user_b["id"]
    print(f"[PASS] Account B created Field B (ID: {field_b_id}, Name: '{field_b['name']}', Owner: {field_b['user_id']})")

    # Account B creates Advisory Task B
    res_tb = client.post(
        "/api/v1/advisory/tasks",
        json={
            "field_id": field_b_id,
            "task_type": "Scouting",
            "title": "Beta Private Task: Bollworm Scouting",
            "description": "Inspect 20 random terminal buds.",
            "time_badge": "Tomorrow",
            "priority": "Medium"
        },
        headers=headers_b
    )
    assert res_tb.status_code == 200
    task_b_id = res_tb.json()["id"]
    print(f"[PASS] Account B created Task B (ID: {task_b_id})")

    # Account B creates Expense B
    res_eb = client.post(
        "/api/v1/budget/expenses",
        json={
            "field_id": field_b_id,
            "category": "Machinery",
            "amount": 3200.0,
            "notes": "Beta Tractor Tillage"
        },
        headers=headers_b
    )
    assert res_eb.status_code == 200
    expense_b_id = res_eb.json()["id"]
    print(f"[PASS] Account B created Expense B (ID: {expense_b_id}, Amount: INR 3200)")

    # -------------------------------------------------------------
    # ANTI-IDOR SECURITY AUDIT: ACCOUNT B ATTEMPTS TO ACCESS ACCOUNT A
    # -------------------------------------------------------------
    print("\n--- ANTI-IDOR PENETRATION VERIFICATION (ACCOUNT B -> ACCOUNT A RESOURCES) ---")

    # Test 1: Account B lists fields -> MUST only see Field B
    res = client.get("/api/v1/fields", headers=headers_b)
    assert res.status_code == 200
    fields_b = res.json()
    assert len(fields_b) == 1
    assert fields_b[0]["id"] == field_b_id
    assert all(f["id"] != field_a_id for f in fields_b)
    print(f"[PASS] GET /api/v1/fields -> PASS, Account B sees ONLY own field ({field_b_id}), 0 fields leaked from Account A")

    # Test 2: Account B attempts direct read of Field A by ID -> 403 Forbidden
    res = client.get(f"/api/v1/fields/{field_a_id}", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: GET /api/v1/fields/{field_a_id} with Account B token -> REJECTED: 403 Forbidden")

    # Test 3: Account B attempts modification of Field A -> 403 Forbidden
    res = client.put(
        f"/api/v1/fields/{field_a_id}",
        json={"name": "Tampered By Attacker B"},
        headers=headers_b
    )
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: PUT /api/v1/fields/{field_a_id} with Account B token -> REJECTED: 403 Forbidden")

    # Test 4: Account B attempts deletion of Field A -> 403 Forbidden
    res = client.delete(f"/api/v1/fields/{field_a_id}", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: DELETE /api/v1/fields/{field_a_id} with Account B token -> REJECTED: 403 Forbidden")

    # Test 5: Account B attempts reading Field A satellite health -> 403 Forbidden
    res = client.get(f"/api/v1/satellite/health/{field_a_id}", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: GET /api/v1/satellite/health/{field_a_id} with Account B token -> REJECTED: 403 Forbidden")

    # Test 6: Account B attempts reading Field A satellite moisture -> 403 Forbidden
    res = client.get(f"/api/v1/satellite/moisture/{field_a_id}", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: GET /api/v1/satellite/moisture/{field_a_id} with Account B token -> REJECTED: 403 Forbidden")

    # Test 7: Account B attempts reading Field A satellite history -> 403 Forbidden
    res = client.get(f"/api/v1/satellite/history/{field_a_id}", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: GET /api/v1/satellite/history/{field_a_id} with Account B token -> REJECTED: 403 Forbidden")

    # Test 8: Account B attempts reading Field A multi-source satellite -> 403 Forbidden
    res = client.get(f"/api/v1/satellite/multi-source/{field_a_id}", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: GET /api/v1/satellite/multi-source/{field_a_id} with Account B token -> REJECTED: 403 Forbidden")

    # Test 9: Account B attempts reading Field A FAO-56 irrigation advice -> 403 Forbidden
    res = client.get(f"/api/v1/fields/{field_a_id}/irrigation", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: GET /api/v1/fields/{field_a_id}/irrigation with Account B token -> REJECTED: 403 Forbidden")

    # Test 10: Account B attempts reading Field A sowing recommendation -> 403 Forbidden
    res = client.get(f"/api/v1/fields/{field_a_id}/sowing-recommendation", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: GET /api/v1/fields/{field_a_id}/sowing-recommendation with Account B token -> REJECTED: 403 Forbidden")

    # Test 11: Account B lists advisory tasks -> MUST only see Task B
    res = client.get("/api/v1/advisory/tasks", headers=headers_b)
    assert res.status_code == 200
    tasks_b = res.json()
    assert len(tasks_b) == 1
    assert tasks_b[0]["id"] == task_b_id
    assert all(t["id"] != task_a_id for t in tasks_b)
    print(f"[PASS] GET /api/v1/advisory/tasks -> PASS, Account B sees ONLY own task ({task_b_id}), 0 tasks leaked")

    # Test 12: Account B attempts to mark Task A complete -> 403 Forbidden
    res = client.post(f"/api/v1/advisory/tasks/{task_a_id}/complete", headers=headers_b)
    assert res.status_code == 403
    print(f"[PASS] IDOR TEST: POST /api/v1/advisory/tasks/{task_a_id}/complete with Account B token -> REJECTED: 403 Forbidden")

    # Test 13: Account B checks budget summary -> Strictly calculated from Field B & Expense B
    res = client.get("/api/v1/budget/summary", headers=headers_b)
    assert res.status_code == 200
    budget_b = res.json()
    assert budget_b["total_expenses"] == 3200.0  # Only Expense B, NOT 7500 or 10700
    print(f"[PASS] GET /api/v1/budget/summary -> PASS, Account B expenses: INR {budget_b['total_expenses']} (Strictly isolated)")

    # Test 14: Account B checks expenses list -> Only Expense B
    res = client.get("/api/v1/budget/expenses", headers=headers_b)
    assert res.status_code == 200
    expenses_b = res.json()
    assert len(expenses_b) == 1
    assert expenses_b[0]["id"] == expense_b_id
    print(f"[PASS] GET /api/v1/budget/expenses -> PASS, Account B sees ONLY own expense ({expense_b_id})")

    # Test 15: Account B checks alerts -> strictly scoped to Field B
    res = client.get("/api/v1/alerts", headers=headers_b)
    assert res.status_code == 200
    alerts_b = res.json()
    assert all(a["field_id"] == field_b_id for a in alerts_b)
    print(f"[PASS] GET /api/v1/alerts -> PASS, Account B receives ONLY alerts for Field B")

    # Test 16: Account B checks own profile -> No leakage of Account A
    res = client.get("/api/v1/auth/me", headers=headers_b)
    assert res.status_code == 200
    me_b = res.json()
    assert me_b["email"] == email_b
    assert me_b["full_name"] == "Farmer Beta (Tiruppur)"
    assert me_b["farm_name"] == "Beta Organic Cotton Estate"
    print(f"[PASS] GET /api/v1/auth/me -> PASS, authentic profile: {me_b['full_name']} ({me_b['farm_name']})")

    # Test 17: Non-existent / Manipulated ID -> 404 Not Found
    res = client.get("/api/v1/fields/999999", headers=headers_b)
    assert res.status_code == 404
    print("[PASS] Manipulated ID /api/v1/fields/999999 -> REJECTED: 404 Not Found")

    # Test 18: Unauthenticated access to protected endpoints -> 401 Unauthorized
    endpoints_to_test = [
        ("GET", "/api/v1/auth/me"),
        ("GET", "/api/v1/fields"),
        ("POST", f"/api/v1/fields/draw?name=Test&crop_type=Paddy&boundary_wkt={poly_b}"),
        ("GET", f"/api/v1/fields/{field_a_id}"),
        ("GET", f"/api/v1/satellite/health/{field_a_id}"),
        ("GET", f"/api/v1/satellite/moisture/{field_a_id}"),
        ("GET", f"/api/v1/satellite/history/{field_a_id}"),
        ("GET", f"/api/v1/fields/{field_a_id}/irrigation"),
        ("GET", "/api/v1/advisory/tasks"),
        ("POST", f"/api/v1/advisory/tasks/{task_a_id}/complete"),
        ("GET", "/api/v1/budget/summary"),
        ("GET", "/api/v1/budget/expenses"),
        ("GET", "/api/v1/alerts"),
    ]

    for method, path in endpoints_to_test:
        if method == "GET":
            r = client.get(path)
        else:
            r = client.post(path)
        assert r.status_code == 401, f"Expected 401 for unauthenticated {path}, got {r.status_code}"
    print(f"[PASS] Unauthenticated access across all {len(endpoints_to_test)} protected endpoints -> 100% REJECTED 401 Unauthorized")

    print("\n" + "=" * 80)
    print("ALL MULTI-USER DATA ISOLATION & ANTI-IDOR TESTS PASSED (100% PASS)!")
    print("=" * 80)

if __name__ == "__main__":
    run_multi_user_isolation_suite()
