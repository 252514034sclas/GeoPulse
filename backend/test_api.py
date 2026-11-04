from fastapi.testclient import TestClient
from main import app

def test_full_api():
    with TestClient(app) as client:
        # 1. Health
        res = client.get("/health")
        assert res.status_code == 200
        assert res.json()["status"] == "healthy"
        print("[PASS] GET /health -> PASS")

        # 2. Auth: Login with demo credentials
        res = client.post("/api/v1/auth/login", json={"email": "demo@geopulse.com", "password": "geopulse123"})
        assert res.status_code == 200
        token_data = res.json()
        assert "access_token" in token_data
        token = token_data["access_token"]
        auth_headers = {"Authorization": f"Bearer {token}"}
        print("[PASS] POST /api/v1/auth/login -> PASS, token received")

        # 3. Auth: Current user profile
        res = client.get("/api/v1/auth/me", headers=auth_headers)
        assert res.status_code == 200
        user = res.json()
        assert user["email"] == "demo@geopulse.com"
        assert user["full_name"] == "Nithish Kumar"
        print("[PASS] GET /api/v1/auth/me -> PASS, farmer:", user["full_name"])

        # 4. Dashboard Summary
        res = client.get("/api/v1/dashboard", headers=auth_headers)
        assert res.status_code == 200
        dash = res.json()
        assert dash["total_fields"] >= 3
        assert "average_crop_vigor" in dash
        assert "average_soil_moisture" in dash
        assert len(dash["todays_tasks"]) > 0
        print("[PASS] GET /api/v1/dashboard -> PASS, fields:", dash["total_fields"], "tasks:", len(dash["todays_tasks"]))

        # 5. Field Management
        res = client.get("/api/v1/fields", headers=auth_headers)
        assert res.status_code == 200
        fields = res.json()
        assert len(fields) >= 3
        first_field_id = fields[0]["id"]
        print("[PASS] GET /api/v1/fields -> PASS, count:", len(fields))

        # 6. Spatial Point-in-Polygon Query
        res = client.post("/api/v1/fields/spatial-query?lat=37.7749&lon=-122.4194")
        assert res.status_code == 200
        matching = res.json()
        print("[PASS] POST /api/v1/fields/spatial-query -> PASS, matching parcels:", len(matching))

        # 7. Satellite Remote Sensing (Crop Health & Vigor)
        res = client.get(f"/api/v1/satellite/health/{first_field_id}")
        assert res.status_code == 200
        sat_health = res.json()
        assert sat_health["ndvi_value"] >= 0.0
        assert "layman_advice" in sat_health
        print("[PASS] GET /api/v1/satellite/health/{id} -> PASS, NDVI:", sat_health["ndvi_value"], "status:", sat_health["health_status"])

        # 8. Satellite Root-Zone Soil Moisture (NDWI)
        res = client.get(f"/api/v1/satellite/moisture/{first_field_id}")
        assert res.status_code == 200
        moisture = res.json()
        assert "moisture_percentage" in moisture
        print("[PASS] GET /api/v1/satellite/moisture/{id} -> PASS, moisture:", moisture["moisture_percentage"], "%")

        # 9. Satellite Growth History
        res = client.get(f"/api/v1/satellite/history/{first_field_id}")
        assert res.status_code == 200
        history = res.json()
        assert len(history["history"]) >= 4
        print("[PASS] GET /api/v1/satellite/history/{id} -> PASS, historical snapshots:", len(history["history"]))

        # 10. Weather & Safe Spraying Window
        res = client.get("/api/v1/weather/current")
        assert res.status_code == 200
        assert "temperature_c" in res.json()
        print("[PASS] GET /api/v1/weather/current -> PASS, temp:", res.json()["temperature_c"], "°C")

        res = client.get("/api/v1/weather/spray-window")
        assert res.status_code == 200
        spray = res.json()
        assert "is_safe_now" in spray
        assert len(spray["recommended_hours"]) > 0
        print("[PASS] GET /api/v1/weather/spray-window -> PASS, is_safe_now:", spray["is_safe_now"])

        # 11. Advisory Tasks
        res = client.get("/api/v1/advisory/tasks")
        assert res.status_code == 200
        tasks = res.json()
        assert len(tasks) > 0
        first_task_id = tasks[0]["id"]
        print("[PASS] GET /api/v1/advisory/tasks -> PASS, active tasks:", len(tasks))

        # Complete task
        res = client.post(f"/api/v1/advisory/tasks/{first_task_id}/complete")
        assert res.status_code == 200
        assert res.json()["is_completed"] == True
        print("[PASS] POST /api/v1/advisory/tasks/{id}/complete -> PASS")

        # 12. Mandi Wholesale Market Rates
        res = client.get("/api/v1/mandi/rates")
        assert res.status_code == 200
        mandi = res.json()
        assert len(mandi) >= 4
        print("[PASS] GET /api/v1/mandi/rates -> PASS, commodities listed:", len(mandi))

        # 13. Fertilizer Dosage Calculator
        fert_req = {
            "crop_type": "Corn (Dent)",
            "area_acres": 120.4,
            "vigor_status": "Optimal",
            "soil_dryness": "Normal"
        }
        res = client.post("/api/v1/fertilizer/calculate", json=fert_req)
        assert res.status_code == 200
        fert_resp = res.json()
        assert fert_resp["urea_total_kg"] > 0
        assert fert_resp["dap_total_kg"] > 0
        assert len(fert_resp["schedule"]) == 3
        print("[PASS] POST /api/v1/fertilizer/calculate -> PASS, Urea total:", fert_resp["urea_total_kg"], "kg, DAP:", fert_resp["dap_total_kg"], "kg")

        # 14. Farm Budget & Expenses
        res = client.get("/api/v1/budget/summary")
        assert res.status_code == 200
        budget = res.json()
        assert budget["total_expenses"] > 0
        assert "net_profit" in budget
        print("[PASS] GET /api/v1/budget/summary -> PASS, total expenses: INR ", budget["total_expenses"], "net profit: INR ", budget["net_profit"])

        # 15. Multi-Source Satellite Intelligence (ISRO Bhoonidhi + Copernicus)
        res = client.get(f"/api/v1/satellite/multi-source/{first_field_id}")
        assert res.status_code == 200
        multi_sat = res.json()
        assert "selected_observation" in multi_sat
        assert "candidates_summary" in multi_sat
        assert "multi_source_guarantee" in multi_sat
        assert multi_sat["total_candidate_observations_evaluated"] >= 2
        print("[PASS] GET /api/v1/satellite/multi-source/{id} -> PASS, candidates evaluated:", multi_sat["total_candidate_observations_evaluated"], "selected:", multi_sat["selected_observation"]["satellite_mission"])

        # 16. ISRO Bhoonidhi Collections
        res = client.get("/api/v1/satellite/bhoonidhi/collections")
        assert res.status_code == 200
        bhoonidhi_cols = res.json()
        assert bhoonidhi_cols["total_collections"] >= 4
        print("[PASS] GET /api/v1/satellite/bhoonidhi/collections -> PASS, collections:", bhoonidhi_cols["total_collections"])

        # 17. ISRO Bhoonidhi Search
        res = client.post("/api/v1/satellite/bhoonidhi/search?min_lon=80.19&min_lat=13.05&max_lon=80.23&max_lat=13.08")
        assert res.status_code == 200
        search_res = res.json()
        assert search_res["products_count"] >= 2
        print("[PASS] POST /api/v1/satellite/bhoonidhi/search -> PASS, products found:", search_res["products_count"])

        # 18. ISRO Bhoonidhi Auth Check
        res = client.post("/api/v1/satellite/bhoonidhi/auth-test")
        assert res.status_code == 200
        auth_test = res.json()
        assert "is_configured" in auth_test
        print("[PASS] POST /api/v1/satellite/bhoonidhi/auth-test -> PASS, configured:", auth_test["is_configured"])

        # 19. AGMARKNET Commodity Mandi Rates
        res = client.get("/api/v1/mandi/prices?commodity=Maize")
        assert res.status_code == 200
        prices = res.json()
        assert prices["total_records"] > 0
        print("[PASS] GET /api/v1/mandi/prices -> PASS, APMC records:", prices["total_records"], "source:", prices["source"])

        # 20. TNAU & ICAR Agricultural Advisory Guide
        res = client.get("/api/v1/knowledge/crop-advisory?crop=Corn%20(Dent)")
        assert res.status_code == 200
        advisory = res.json()
        assert "recommended_varieties" in advisory
        assert "fertilizer_schedule" in advisory
        assert "pest_and_disease_ipm" in advisory
        print("[PASS] GET /api/v1/knowledge/crop-advisory -> PASS, crop:", advisory["crop"], "source:", advisory["official_source"])

        # 21. Master Provider Registry
        res = client.get("/api/v1/providers/registry")
        assert res.status_code == 200
        reg = res.json()
        assert reg["total_registered_providers"] == 8
        print("[PASS] GET /api/v1/providers/registry -> PASS, registered providers:", reg["total_registered_providers"])

    print("\n=================================================================")
    print("ALL 21 DOMAIN TEST SUITES PASSED FLAWLESSLY (100% PASS)!")
    print("Multi-Source Bhoonidhi + Copernicus + AGMARKNET + TNAU Verified!")
    print("=================================================================")

if __name__ == "__main__":
    test_full_api()
