import httpx
import json

def check_stac_properties():
    # Planetary computer STAC search for this specific item
    url = "https://planetarycomputer.microsoft.com/api/stac/v1/collections/sentinel-2-l2a/items/S2C_MSIL2A_20261011T045721_R119_T44PMV_20261011T095104"
    with httpx.Client(timeout=30.0) as client:
        r = client.get(url)
        print("Status:", r.status_code)
        if r.status_code == 200:
            data = r.json()
            props = data.get("properties", {})
            print("Baseline:", props.get("s2:processing_baseline"))
            print("Product type:", props.get("s2:product_type"))
            
            # Check assets properties
            assets = data.get("assets", {})
            for b in ["B04", "B08", "B11", "SCL"]:
                if b in assets:
                    print(f"\n--- Asset {b} ---")
                    asset_props = assets[b]
                    print("raster:bands:", asset_props.get("raster:bands"))
                    print("eo:bands:", asset_props.get("eo:bands"))
                    print("type:", asset_props.get("type"))

if __name__ == "__main__":
    check_stac_properties()
