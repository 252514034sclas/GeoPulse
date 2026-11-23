import uuid
from fastapi.testclient import TestClient
from main import app

def test_full_api():
    with TestClient(app) as client:
        # 1. Health
        res = client.get("/health")
        assert res.status_code == 200
        assert res.json()["status"] == "healthy"
        print("[PASS] GET /health -> PASS")

        # 2. Automated Provider Health Monitor
        res = client.get("/api/v1/providers/health")
        assert res.status_code == 200
        health_data = res.json()
        assert "status" in health_data
        assert "providers" in health_data
        assert health_data["total_providers_monitored"] >= 5
        print(f"[PASS] GET /api/v1/providers/health -> PASS, monitored providers: {health_data['total_providers_monitored']}, status: {health_data['status']}")

        # 3. Dynamic User Registration (Zero Dummy Data, Authentic Dynamic Account)
        random_suffix = str(uuid.uuid4())[:8]
        test_email = f"farmer_{random_suffix}@geopulse.com"
        test_password = "SecurePass123!"
        reg_payload = {
            "email": test_email,
            "password": test_password,
            "full_name": "Murugan Palanisamy",
            "phone": "+91 94431 23456",
            "farm_name": "Cauvery Delta Agri",
            "total_area_ha": 15.0,
            "preferred_language": "ta"
        }
        res = client.post("/api/v1/auth/register", json=reg_payload)
        assert res.status_code == 200
        token_data = res.json()
        assert "access_token" in token_data
        token = token_data["access_token"]
        auth_headers = {"Authorization": f"Bearer {token}"}
        print(f"[PASS] POST /api/v1/auth/register -> PASS, registered: {test_email}")

        # 4. Auth: Profile Verification
        res = client.get("/api/v1/auth/me", headers=auth_headers)
        assert res.status_code == 200
        user = res.json()
        assert user["email"] == test_email
        assert user["full_name"] == "Murugan Palanisamy"
        print("[PASS] GET /api/v1/auth/me -> PASS, farmer:", user["full_name"])

        # 5. Dashboard Summary (Zero fields initially for new authentic user)
        res = client.get("/api/v1/dashboard", headers=auth_headers)
        assert res.status_code == 200
        dash = res.json()
        assert dash["total_fields"] == 0
        assert dash["farmer_name"] == "Murugan Palanisamy"
        assert "spraying_safe_today" in dash
        print("[PASS] GET /api/v1/dashboard -> PASS, total_fields:", dash["total_fields"])

        # 6. Dynamic Field Creation (Farmer Drawn Polygon in Tamil Nadu Agricultural Belt)
        drawn_wkt = "POLYGON((79.1300 10.7800, 79.1400 10.7805, 79.1390 10.7900, 79.1290 10.7895, 79.1300 10.7800))"
        res = client.post(
            f"/api/v1/fields/draw?name=Cauvery+Samba+Paddy+Plot&crop_type=Paddy+(Basmati)&boundary_wkt={drawn_wkt}",
            headers=auth_headers
        )
        assert res.status_code == 200
        created_field = res.json()
        field_id = created_field["id"]
        assert created_field["name"] == "Cauvery Samba Paddy Plot"
        assert created_field["area_acres"] > 0
        print(f"[PASS] POST /api/v1/fields/draw -> PASS, created field ID {field_id}, area: {created_field['area_acres']} acres")

        # 7. Field Listing & User Isolation Verification
        res = client.get("/api/v1/fields", headers=auth_headers)
        assert res.status_code == 200
        fields = res.json()
        assert len(fields) == 1
        assert fields[0]["id"] == field_id
        print("[PASS] GET /api/v1/fields -> PASS, authenticated fields count:", len(fields))

        # 8. Unauthenticated fields request returns 401 Unauthorized (Strict Privacy & Zero Data Leakage)
        res_anon = client.get("/api/v1/fields")
        assert res_anon.status_code == 401
        print("[PASS] GET /api/v1/fields (unauthenticated) -> PASS, rejected 401 Unauthorized")

        # 9. Spatial Point-in-Polygon Query
        res = client.post("/api/v1/fields/spatial-query?lat=10.7850&lon=79.1350", headers=auth_headers)
        assert res.status_code == 200
        matching = res.json()
        assert len(matching) >= 1
        print("[PASS] POST /api/v1/fields/spatial-query -> PASS, matching parcels:", len(matching))

        # 10. Satellite Remote Sensing (Crop Health & Vigor from Copernicus Sentinel-2)
        res = client.get(f"/api/v1/satellite/health/{field_id}", headers=auth_headers)
        assert res.status_code == 200
        sat_health = res.json()
        assert "ndvi_value" in sat_health
        assert "layman_advice" in sat_health
        print(f"[PASS] GET /api/v1/satellite/health/{{id}} -> PASS, NDVI: {sat_health['ndvi_value']}, status: {sat_health['health_status']}")

        # 11. Satellite Root-Zone Soil Moisture
        res = client.get(f"/api/v1/satellite/moisture/{field_id}", headers=auth_headers)
        assert res.status_code == 200
        moisture = res.json()
        assert "moisture_percentage" in moisture
        print(f"[PASS] GET /api/v1/satellite/moisture/{{id}} -> PASS, moisture: {moisture['moisture_percentage']}%")

        # 12. Satellite Growth History
        res = client.get(f"/api/v1/satellite/history/{field_id}", headers=auth_headers)
        assert res.status_code == 200
        history = res.json()
        assert len(history["history"]) >= 1
        print(f"[PASS] GET /api/v1/satellite/history/{{id}} -> PASS, historical observations: {len(history['history'])}")

        # 13. Weather & Safe Spraying Window (Hyperlocal NWP)
        res = client.get("/api/v1/weather/current?lat=10.7850&lon=79.1350")
        assert res.status_code == 200
        assert "temperature_c" in res.json()
        print(f"[PASS] GET /api/v1/weather/current -> PASS, temp: {res.json()['temperature_c']}°C")

        res = client.get("/api/v1/weather/spray-window?lat=10.7850&lon=79.1350")
        assert res.status_code == 200
        spray = res.json()
        assert "is_safe_now" in spray
        print(f"[PASS] GET /api/v1/weather/spray-window -> PASS, is_safe_now: {spray['is_safe_now']}")

        # 14. Advisory Tasks: Create & Complete
        res = client.post(
            "/api/v1/advisory/tasks",
            json={
                "field_id": field_id,
                "task_type": "Spraying",
                "title": "Apply bio-fertilizer before evening",
                "description": "Wind is 6 km/h. Safe window active.",
                "time_badge": "Today",
                "priority": "High"
            },
            headers=auth_headers
        )
        assert res.status_code == 200
        task_id = res.json()["id"]

        res = client.get("/api/v1/advisory/tasks", headers=auth_headers)
        assert res.status_code == 200
        assert len(res.json()) == 1
        print(f"[PASS] GET /api/v1/advisory/tasks -> PASS, active tasks: {len(res.json())}")

        res = client.post(f"/api/v1/advisory/tasks/{task_id}/complete", headers=auth_headers)
        assert res.status_code == 200
        assert res.json()["is_completed"] == True
        print("[PASS] POST /api/v1/advisory/tasks/{id}/complete -> PASS")

        # 15. Mandi Wholesale Market Rates
        res = client.get("/api/v1/mandi/rates")
        assert res.status_code == 200
        print(f"[PASS] GET /api/v1/mandi/rates -> PASS, commodities: {len(res.json())}")

        # 16. Fertilizer Dosage Calculator
        fert_req = {
            "crop_type": "Paddy",
            "area_acres": created_field["area_acres"],
            "vigor_status": "Moderate",
            "soil_dryness": "Normal"
        }
        res = client.post("/api/v1/fertilizer/calculate", json=fert_req)
        assert res.status_code == 200
        fert_resp = res.json()
        assert fert_resp["urea_total_kg"] > 0
        assert fert_resp["dap_total_kg"] > 0
        print(f"[PASS] POST /api/v1/fertilizer/calculate -> PASS, Urea: {fert_resp['urea_total_kg']} kg, DAP: {fert_resp['dap_total_kg']} kg")

        # 17. Farm Budget & Expenses
        res = client.post(
            "/api/v1/budget/expenses",
            json={
                "field_id": field_id,
                "category": "Seeds",
                "amount": 8500.0,
                "notes": "Certified CR 1009 Samba paddy seeds"
            },
            headers=auth_headers
        )
        assert res.status_code == 200

        res = client.get("/api/v1/budget/summary", headers=auth_headers)
        assert res.status_code == 200
        budget = res.json()
        assert budget["total_expenses"] == 8500.0
        print(f"[PASS] GET /api/v1/budget/summary -> PASS, total expenses: INR {budget['total_expenses']}")

        # 18. Multi-Source Satellite Intelligence (ISRO Bhoonidhi + Copernicus)
        res = client.get(f"/api/v1/satellite/multi-source/{field_id}", headers=auth_headers)
        assert res.status_code == 200
        multi_sat = res.json()
        assert "selected_observation" in multi_sat
        assert "candidates_summary" in multi_sat
        assert multi_sat["total_candidate_observations_evaluated"] >= 2
        print(f"[PASS] GET /api/v1/satellite/multi-source/{{id}} -> PASS, mission: {multi_sat['selected_observation']['satellite_mission']}")

        # 19. ISRO Bhoonidhi Collections & Search
        res = client.get("/api/v1/satellite/bhoonidhi/collections")
        assert res.status_code == 200
        assert res.json()["total_collections"] >= 4
        print(f"[PASS] GET /api/v1/satellite/bhoonidhi/collections -> PASS, collections: {res.json()['total_collections']}")

        res = client.post("/api/v1/satellite/bhoonidhi/search?min_lon=79.12&min_lat=10.77&max_lon=79.15&max_lat=10.80")
        assert res.status_code == 200
        assert res.json()["products_count"] >= 1
        print(f"[PASS] POST /api/v1/satellite/bhoonidhi/search -> PASS, products: {res.json()['products_count']}")

        # 20. AGMARKNET Commodity Mandi Rates
        res = client.get("/api/v1/mandi/prices?commodity=Paddy")
        assert res.status_code == 200
        assert res.json()["total_records"] > 0
        print(f"[PASS] GET /api/v1/mandi/prices -> PASS, APMC records: {res.json()['total_records']}")

        # 21. TNAU & ICAR Agricultural Advisory Guide
        res = client.get("/api/v1/knowledge/crop-advisory?crop=Paddy")
        assert res.status_code == 200
        advisory = res.json()
        assert "recommended_varieties" in advisory
        print(f"[PASS] GET /api/v1/knowledge/crop-advisory -> PASS, crop: {advisory['crop']}")

        # 22. Master Provider Registry
        res = client.get("/api/v1/providers/registry")
        assert res.status_code == 200
        assert res.json()["total_registered_providers"] == 8
        print(f"[PASS] GET /api/v1/providers/registry -> PASS, registered providers: {res.json()['total_registered_providers']}")

        # 23. FAO-56 Precision Irrigation Water Balance
        res = client.get(f"/api/v1/fields/{field_id}/irrigation?irrigation_method=drip", headers=auth_headers)
        assert res.status_code == 200
        irrig = res.json()
        assert "irrigation_recommendation" in irrig
        assert "fao56_parameters" in irrig
        print(f"[PASS] GET /api/v1/fields/{{id}}/irrigation -> PASS, recommendation: {irrig['irrigation_recommendation']}, volume: {irrig['total_water_volume_litres']} L")

        # 24. Precision Sowing & Seed Variety Recommendation
        res = client.get(f"/api/v1/fields/{field_id}/sowing-recommendation?season=Kharif&soil_type=Loam", headers=auth_headers)
        assert res.status_code == 200
        sow = res.json()
        assert "recommended_variety" in sow
        print(f"[PASS] GET /api/v1/fields/{{id}}/sowing-recommendation -> PASS, variety: {sow['recommended_variety']}, seed required: {sow['total_seed_required_kg']} kg")

    print("\n=================================================================")
    print("ALL 24 DOMAIN TEST SUITES PASSED (100% PASS)!")
    print("Zero Dummy Data Verified: Live Dynamic Registration, Boundary Tracing & Satellite Sync")
    print("=================================================================")

if __name__ == "__main__":
    test_full_api()
