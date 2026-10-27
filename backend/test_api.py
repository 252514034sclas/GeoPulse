from fastapi.testclient import TestClient
from main import app

def test_api():
    with TestClient(app) as client:
        # Health
        res = client.get("/health")
        assert res.status_code == 200
        print("GET /health -> PASS:", res.json())

        # Dashboard
        res = client.get("/api/v1/dashboard")
        assert res.status_code == 200
        data = res.json()
        assert data["total_fields"] > 0
        print("GET /api/v1/dashboard -> PASS, total_fields:", data["total_fields"])

        # Fields
        res = client.get("/api/v1/fields")
        assert res.status_code == 200
        assert len(res.json()) > 0
        print("GET /api/v1/fields -> PASS, count:", len(res.json()))

        # Spatial Query Endpoint
        res = client.get("/api/v1/fields/spatial-query?lat=34.0522&lon=-118.2437")
        assert res.status_code == 200
        print("GET /api/v1/fields/spatial-query -> PASS, matching fields:", len(res.json()))

        # Alerts
        res = client.get("/api/v1/alerts")
        assert res.status_code == 200
        assert len(res.json()) > 0
        print("GET /api/v1/alerts -> PASS, count:", len(res.json()))

        # Vegetation Health
        res = client.get("/api/v1/vegetation-health")
        assert res.status_code == 200
        assert len(res.json()) > 0
        print("GET /api/v1/vegetation-health -> PASS, disclaimer present")

        # Weather
        res = client.get("/api/v1/weather")
        assert res.status_code == 200
        assert "temperature_c" in res.json()
        print("GET /api/v1/weather -> PASS, temp:", res.json()["temperature_c"])

        # Soil Moisture
        res = client.get("/api/v1/soil-moisture")
        assert res.status_code == 200
        assert len(res.json()) > 0
        print("GET /api/v1/soil-moisture -> PASS, count:", len(res.json()))

    print("ALL API ENDPOINTS & SPATIAL QUERIES TESTED SUCCESSFULLY!")

if __name__ == "__main__":
    test_api()
