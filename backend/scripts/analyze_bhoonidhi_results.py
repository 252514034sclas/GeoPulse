import os
import sys
import json
import httpx
from dotenv import load_dotenv

load_dotenv(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), ".env"))

user_id = os.getenv("BHOONIDHI_USER_ID") or os.getenv("ISRO_BHOONIDHI_USERNAME") or ""
password = os.getenv("BHOONIDHI_PASSWORD") or os.getenv("ISRO_BHOONIDHI_PASSWORD") or ""

base_url = "https://bhoonidhi-api.nrsc.gov.in"

# 1. Authenticate
auth_url = f"{base_url}/auth/token"
payload = {"userId": user_id, "password": password, "grant_type": "password"}

with httpx.Client(timeout=15.0, verify=False) as client:
    r = client.post(auth_url, json=payload)
    token = r.json().get("access_token") or r.json().get("token")

headers = {"Authorization": f"Bearer {token}", "Accept": "application/json"}

# 2. Search for Field 1 in Tamil Nadu
search_body = {
    "bbox": [80.202, 13.061, 80.210, 13.068],
    "limit": 10
}

print("Querying Bhoonidhi /data/search for Field 1...")
with httpx.Client(timeout=20.0, verify=False) as client:
    resp = client.post(f"{base_url}/data/search", json=search_body, headers=headers)
    print(f"Search HTTP Status: {resp.status_code}")
    data = resp.json()
    context = data.get("context", {})
    features = data.get("features", [])
    print(f"Context: {context}")
    print(f"Features returned: {len(features)}")

    for i, feat in enumerate(features[:5]):
        print(f"\n--- Feature {i+1} ---")
        print(f"ID: {feat.get('id')}")
        print(f"Collection: {feat.get('collection')}")
        props = feat.get("properties", {})
        print(f"Datetime: {props.get('datetime')}")
        print(f"Satellite / Platform: {props.get('platform') or props.get('satellite')}")
        print(f"Sensor / Instrument: {props.get('instruments') or props.get('sensor')}")
        print(f"Cloud Cover: {props.get('eo:cloud_cover')}")
        print(f"Spatial Resolution: {props.get('gsd') or props.get('spatial_resolution')}")
        assets = feat.get("assets", {})
        print(f"Assets keys: {list(assets.keys())}")
        for asset_key, asset_val in assets.items():
            print(f"   Asset '{asset_key}': type={asset_val.get('type')}, href={asset_val.get('href')}")
        links = feat.get("links", [])
        print(f"Links count: {len(links)}")
        for l in links[:3]:
            print(f"   Link rel={l.get('rel')}: href={l.get('href')}")
