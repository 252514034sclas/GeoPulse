import uuid
import sys
import os

sys.path.insert(0, os.path.dirname(__file__))

from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def run_tests():
    print("=" * 80)
    print("GEOPULSE EMPTY-FIELD, MULTI-FIELD & ANTI-IDOR VERIFICATION SUITE")
    print("=" * 80)

    # -------------------------------------------------------------
    # 1. TEST 1: NEWLY REGISTERED ACCOUNT WITH ZERO FIELDS
    # -------------------------------------------------------------
    log("--- TEST 1: ZERO-FIELD ACCOUNT BEHAVIOR ---")
    zero_user_email = f"zero_field_{uuid.uuid4().hex[:8]}@geopulse.com"
    pwd = "SecureFarmerPassword123!"
    reg_payload = {
        "email": zero_user_email,
        "password": pwd,
        "full_name": "Zero Field Farmer",
        "district": "Thanjavur",
        "farm_name": "Fresh Greenfield Estate",
        "primary_crop": "Paddy / Rice"
    }
    r = client.post("/api/v1/auth/register", json=reg_payload)
    assert r.status_code == 200, f"Register failed: {r.text}"
    token_zero = r.json()["access_token"]
    headers_zero = {"Authorization": f"Bearer {token_zero}"}
    log(f"Registered fresh user: {zero_user_email}", "PASS")

    # Fetch fields
    r_fields = client.get("/api/v1/fields", headers=headers_zero)
    assert r_fields.status_code == 200
    fields_list = r_fields.json()
    assert len(fields_list) == 0, f"Expected 0 fields, got {len(fields_list)}"
    log("GET /api/v1/fields -> Returned exactly 0 fields", "PASS")

    # Verify that requesting Field 1 (or any unowned ID) is rejected with 403 / 404 (Anti-IDOR)
    r_idor_health = client.get("/api/v1/satellite/health/1", headers=headers_zero)
    assert r_idor_health.status_code in (403, 404), f"Expected 403/404, got {r_idor_health.status_code}"
    log(f"Zero-field account cannot access Field ID 1: HTTP {r_idor_health.status_code}", "PASS")

    r_idor_irri = client.get("/api/v1/fields/1/irrigation", headers=headers_zero)
    assert r_idor_irri.status_code in (403, 404), f"Expected 403/404, got {r_idor_irri.status_code}"
    log(f"Zero-field account cannot query irrigation for Field ID 1: HTTP {r_idor_irri.status_code}", "PASS")

    r_idor_sow = client.get("/api/v1/fields/1/sowing-recommendation", headers=headers_zero)
    assert r_idor_sow.status_code in (403, 404), f"Expected 403/404, got {r_idor_sow.status_code}"
    log(f"Zero-field account cannot query sowing for Field ID 1: HTTP {r_idor_sow.status_code}", "PASS")

    # -------------------------------------------------------------
    # 2. TEST 2: SINGLE-FIELD ACCOUNT
    # -------------------------------------------------------------
    log("\n--- TEST 2: SINGLE-FIELD REGISTRATION & TELEMETRY ---")
    single_user_email = f"single_field_{uuid.uuid4().hex[:8]}@geopulse.com"
    reg_payload_single = {
        "email": single_user_email,
        "password": pwd,
        "full_name": "Single Parcel Farmer",
        "district": "Tiruchirappalli",
        "farm_name": "Cauvery Delta Plot",
        "primary_crop": "Corn (Dent)"
    }
    r = client.post("/api/v1/auth/register", json=reg_payload_single)
    token_single = r.json()["access_token"]
    headers_single = {"Authorization": f"Bearer {token_single}"}
    log(f"Registered single-field user: {single_user_email}", "PASS")

    # Create field 1
    wkt_1 = "POLYGON ((78.680 10.790, 78.685 10.790, 78.685 10.795, 78.680 10.795, 78.680 10.790))"
    r_create = client.post(
        "/api/v1/fields/draw",
        headers=headers_single,
        params={"name": "Cauvery Corn Plot Alpha", "crop_type": "Corn (Dent)", "boundary_wkt": wkt_1}
    )
    assert r_create.status_code == 200, f"Field creation failed: {r_create.text}"
    field_single = r_create.json()
    field_single_id = field_single["id"]
    log(f"Created Field 1 (ID: {field_single_id}, Name: {field_single['name']}, Area: {field_single['area_acres']:.1f} ac)", "PASS")

    # Query fields list
    r_list = client.get("/api/v1/fields", headers=headers_single)
    assert len(r_list.json()) == 1
    log(f"GET /api/v1/fields -> Returned 1 field (ID: {field_single_id})", "PASS")

    # Query telemetry for that field
    r_h = client.get(f"/api/v1/satellite/health/{field_single_id}", headers=headers_single)
    assert r_h.status_code == 200
    log(f"Satellite Health for Field {field_single_id}: NDVI {r_h.json()['ndvi_value']}, Status: {r_h.json()['health_status']}", "PASS")

    r_m = client.get(f"/api/v1/satellite/moisture/{field_single_id}", headers=headers_single)
    assert r_m.status_code == 200
    log(f"Soil Moisture for Field {field_single_id}: {r_m.json()['moisture_percentage']}%", "PASS")

    r_i = client.get(f"/api/v1/fields/{field_single_id}/irrigation", headers=headers_single)
    assert r_i.status_code == 200
    log(f"Irrigation for Field {field_single_id}: {r_i.json()['irrigation_recommendation']}, Vol: {r_i.json()['total_water_volume_litres']} L", "PASS")

    r_s = client.get(f"/api/v1/fields/{field_single_id}/sowing-recommendation", headers=headers_single)
    assert r_s.status_code == 200
    log(f"Sowing Guide for Field {field_single_id}: Variety: {r_s.json()['recommended_variety']}, Seed: {r_s.json()['total_seed_required_kg']} kg", "PASS")

    # -------------------------------------------------------------
    # 3. TEST 3: MULTI-FIELD ACCOUNT & FIELD SWITCHING
    # -------------------------------------------------------------
    log("\n--- TEST 3: MULTI-FIELD ACCOUNT & SWITCHING TELEMETRY ---")
    multi_user_email = f"multi_field_{uuid.uuid4().hex[:8]}@geopulse.com"
    r = client.post("/api/v1/auth/register", json={
        "email": multi_user_email,
        "password": pwd,
        "full_name": "Multi Plot Landholder",
        "district": "Madurai",
        "farm_name": "Vaigai River Basin Holdings",
        "primary_crop": "Paddy / Rice"
    })
    token_multi = r.json()["access_token"]
    headers_multi = {"Authorization": f"Bearer {token_multi}"}
    log(f"Registered multi-field user: {multi_user_email}", "PASS")

    # Create Field 1: Paddy
    wkt_m1 = "POLYGON ((78.110 9.920, 78.115 9.920, 78.115 9.925, 78.110 9.925, 78.110 9.920))"
    r_m1 = client.post("/api/v1/fields/draw", headers=headers_multi, params={
        "name": "Vaigai Wet Paddy Lowland",
        "crop_type": "Paddy / Rice",
        "boundary_wkt": wkt_m1
    })
    f1 = r_m1.json()
    f1_id = f1["id"]
    log(f"Created Field A (ID: {f1_id}, Name: {f1['name']}, Crop: {f1['crop_type']})", "PASS")

    # Create Field 2: Cotton
    wkt_m2 = "POLYGON ((78.120 9.930, 78.130 9.930, 78.130 9.940, 78.120 9.940, 78.120 9.930))"
    r_m2 = client.post("/api/v1/fields/draw", headers=headers_multi, params={
        "name": "Vaigai Bt Cotton Upland",
        "crop_type": "Cotton",
        "boundary_wkt": wkt_m2
    })
    f2 = r_m2.json()
    f2_id = f2["id"]
    log(f"Created Field B (ID: {f2_id}, Name: {f2['name']}, Crop: {f2['crop_type']})", "PASS")

    # Query fields list
    r_multi_list = client.get("/api/v1/fields", headers=headers_multi)
    assert len(r_multi_list.json()) == 2
    log("GET /api/v1/fields -> Returned exactly 2 parcels", "PASS")

    # Test Field 1 Telemetry (Paddy)
    r_f1_irri = client.get(f"/api/v1/fields/{f1_id}/irrigation", headers=headers_multi).json()
    r_f1_sow = client.get(f"/api/v1/fields/{f1_id}/sowing-recommendation", headers=headers_multi).json()
    log(f"Field A (Paddy) -> Seed: {r_f1_sow['recommended_variety']}, Water Rec: {r_f1_irri['irrigation_recommendation']}, Vol: {r_f1_irri['total_water_volume_litres']} L", "PASS")

    # Test Field 2 Telemetry (Cotton - switched field)
    r_f2_irri = client.get(f"/api/v1/fields/{f2_id}/irrigation", headers=headers_multi).json()
    r_f2_sow = client.get(f"/api/v1/fields/{f2_id}/sowing-recommendation", headers=headers_multi).json()
    log(f"Field B (Cotton) -> Seed: {r_f2_sow['recommended_variety']}, Water Rec: {r_f2_irri['irrigation_recommendation']}, Vol: {r_f2_irri['total_water_volume_litres']} L", "PASS")

    # Confirm different calculations for switched parcels
    assert r_f1_sow["recommended_variety"] != r_f2_sow["recommended_variety"], "Varieties should differ between crops"
    assert r_f1_irri["total_water_volume_litres"] != r_f2_irri["total_water_volume_litres"], "Water volume must reflect distinct parcel acreages"
    log("Field switching verified: Distinct agronomic calculations returned for each parcel", "PASS")

    # -------------------------------------------------------------
    # 4. TEST 4: CROSS-ACCOUNT ANTI-IDOR PENETRATION
    # -------------------------------------------------------------
    log("\n--- TEST 4: CROSS-ACCOUNT ACCESS REJECTION (ANTI-IDOR) ---")
    endpoints_to_test = [
        ("GET", f"/api/v1/fields/{f1_id}"),
        ("GET", f"/api/v1/satellite/health/{f1_id}"),
        ("GET", f"/api/v1/satellite/moisture/{f1_id}"),
        ("GET", f"/api/v1/satellite/history/{f1_id}"),
        ("GET", f"/api/v1/satellite/multi-source/{f1_id}"),
        ("GET", f"/api/v1/fields/{f1_id}/irrigation"),
        ("GET", f"/api/v1/fields/{f1_id}/sowing-recommendation"),
        ("PUT", f"/api/v1/fields/{f1_id}"),
        ("DELETE", f"/api/v1/fields/{f1_id}"),
    ]

    for method, path in endpoints_to_test:
        if method == "GET":
            res = client.get(path, headers=headers_single)
        elif method == "PUT":
            res = client.put(path, headers=headers_single, json={"name": "Hacked Field Name"})
        elif method == "DELETE":
            res = client.delete(path, headers=headers_single)

        assert res.status_code == 403, f"IDOR Vulnerability! {method} {path} returned {res.status_code} instead of 403"
        log(f"ANTI-IDOR: {method} {path} strictly rejected -> HTTP 403 Forbidden", "PASS")

    # -------------------------------------------------------------
    # 5. TEST 5: UNAUTHENTICATED ACCESS REJECTION
    # -------------------------------------------------------------
    log("\n--- TEST 5: UNAUTHENTICATED ACCESS REJECTION ---")
    for method, path in endpoints_to_test[:7]:
        res = client.get(path)
        assert res.status_code == 401, f"Expected 401, got {res.status_code}"
        log(f"UNAUTH: {path} rejected -> HTTP 401 Unauthorized", "PASS")

    print("\n" + "=" * 80)
    print("ALL 5 TEST MATRIX PHASES COMPLETED WITH 100% PASS!")
    print("=" * 80)

if __name__ == "__main__":
    run_tests()
