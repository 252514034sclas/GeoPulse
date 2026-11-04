import os
import sys
import json
import httpx
from datetime import datetime, timedelta

bbox = [80.202, 13.061, 80.210, 13.068]
end_date = datetime.utcnow()
start_date = end_date - timedelta(days=60)

payload = {
    "collections": ["sentinel-2-l2a"],
    "bbox": bbox,
    "datetime": f"{start_date.strftime('%Y-%m-%d')}/{end_date.strftime('%Y-%m-%d')}",
    "query": {"eo:cloud_cover": {"lt": 50}},
    "sortby": [{"field": "datetime", "direction": "desc"}],
    "limit": 5
}

print(f"Searching Sentinel-2 L2A for bbox {bbox} from {start_date.strftime('%Y-%m-%d')} to {end_date.strftime('%Y-%m-%d')}...")
with httpx.Client(timeout=20.0) as client:
    r = client.post("https://planetarycomputer.microsoft.com/api/stac/v1/search", json=payload)
    print(f"STAC Search Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()
        features = data.get("features", [])
        print(f"Found {len(features)} matching Sentinel-2 scenes.")
        for idx, f in enumerate(features):
            props = f.get("properties", {})
            assets = f.get("assets", {})
            print(f"\n--- Scene {idx+1}: {f.get('id')} ---")
            print(f"Platform: {props.get('platform')}")
            print(f"Constellation: {props.get('constellation')}")
            print(f"Datetime: {props.get('datetime')}")
            print(f"Cloud Cover: {props.get('eo:cloud_cover')}%")
            print(f"S2 SCL Available: {'SCL' in assets}")
            print(f"B04 (Red 10m): {'B04' in assets}")
            print(f"B08 (NIR 10m): {'B08' in assets}")
            print(f"B11 (SWIR 20m): {'B11' in assets}")
            if "B04" in assets:
                print(f"B04 href: {assets['B04'].get('href')}")
    else:
        print(f"Error: {r.text[:300]}")
