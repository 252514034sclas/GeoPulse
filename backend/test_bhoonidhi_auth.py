import os
import sys
from dotenv import load_dotenv

# Load .env
load_dotenv(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env"))

from adapters.isro_bhoonidhi_adapter import IsroBhoonidhiAdapter
from services.multi_source_satellite_service import MultiSourceSatelliteService

adapter = IsroBhoonidhiAdapter()
print(f"Is configured: {adapter.is_configured}")

token = adapter.authenticate()
if token:
    print(f"[PASS] Bhoonidhi Authentication: Token received! (length={len(token)})")
else:
    print("[FAIL] Bhoonidhi Authentication returned None")

collections = adapter.get_collections()
print(f"[PASS] Bhoonidhi Collections count: {len(collections)}")
for col in collections[:3]:
    print(f"  - {col.get('collection_id')}: {col.get('mission')} ({col.get('sensor')})")

# Test search over Coimbatore, Tamil Nadu (TNAU region: 11.0°N, 76.9°E)
products = adapter.search_products(
    min_lon=76.85,
    min_lat=10.95,
    max_lon=77.05,
    max_lat=11.15
)
print(f"[PASS] Bhoonidhi Products found: {len(products)}")
for prod in products:
    print(f"  * {prod.get('satellite')} / {prod.get('sensor')} | Cloud: {prod.get('cloud_cover_pct')}% | Status: {prod.get('download_status')}")

# Test MultiSourceSatelliteService
multi_svc = MultiSourceSatelliteService()
result = multi_svc.evaluate_field_observations(
    min_lon=76.85,
    min_lat=10.95,
    max_lon=77.05,
    max_lat=11.15,
    crop_type="Corn (Dent)",
    priority_need="crop_health"
)
sel = result.get("selected_observation", {})
print(f"\n[PASS] Multi-Source Router Winner:")
print(f"  Satellite: {sel.get('satellite_mission')}")
print(f"  Sensor: {sel.get('sensor')}")
print(f"  Provider: {sel.get('provider')}")
print(f"  Resolution: {sel.get('spatial_resolution')}")
print(f"  Rationale: {result.get('selection_rationale')}")
print(f"  Candidates Evaluated: {result.get('total_candidate_observations_evaluated')}")
for cand in result.get('candidates_summary', []):
    print(f"    - {cand.get('provider')}: {cand.get('mission')} ({cand.get('sensor')}) | Res: {cand.get('resolution')} | Cloud: {cand.get('cloud_pct')}%")
