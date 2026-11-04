import os
import sys
import httpx
from dotenv import load_dotenv

load_dotenv(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), ".env"))

user_id = os.getenv("BHOONIDHI_USER_ID") or os.getenv("ISRO_BHOONIDHI_USERNAME") or ""
password = os.getenv("BHOONIDHI_PASSWORD") or os.getenv("ISRO_BHOONIDHI_PASSWORD") or ""

base_url = "https://bhoonidhi-api.nrsc.gov.in"
token = None

# 1. Authenticate
auth_url = f"{base_url}/auth/token"
payload = {"userId": user_id, "password": password, "grant_type": "password"}

with httpx.Client(timeout=15.0, verify=False) as client:
    r = client.post(auth_url, json=payload)
    if r.status_code == 200:
        token = r.json().get("access_token") or r.json().get("token")
        print(f"Authenticated! Token received (len={len(token)})")
    else:
        print(f"Auth failed: {r.status_code} {r.text[:200]}")
        sys.exit(1)

headers = {"Authorization": f"Bearer {token}", "Accept": "application/json"}

# 2. Probe endpoints
endpoints = [
    ("GET", "/v2/api-docs"),
    ("GET", "/v3/api-docs"),
    ("GET", "/swagger-resources"),
    ("GET", "/swagger-ui.html"),
    ("GET", "/data/collections"),
    ("GET", "/data/products"),
    ("GET", "/catalog"),
    ("GET", "/stac"),
    ("GET", "/user/profile"),
    ("GET", "/orders"),
    ("POST", "/data/search", {"bbox": [80.20, 13.06, 80.21, 13.07]}),
    ("POST", "/search", {"bbox": [80.20, 13.06, 80.21, 13.07]}),
]

with httpx.Client(timeout=10.0, verify=False) as client:
    for item in endpoints:
        method = item[0]
        ep = item[1]
        body = item[2] if len(item) > 2 else None
        url = f"{base_url}{ep}"
        try:
            if method == "GET":
                resp = client.get(url, headers=headers)
            else:
                resp = client.post(url, json=body, headers=headers)
            print(f"{method} {ep} -> Status: {resp.status_code}, Length: {len(resp.text)}, Content-Type: {resp.headers.get('content-type', '')[:40]}")
            if resp.status_code in (200, 201) and "json" in resp.headers.get("content-type", ""):
                txt = resp.text
                print(f"   Sample: {txt[:200]}...")
        except Exception as e:
            print(f"{method} {ep} -> Error: {type(e).__name__}: {e}")
