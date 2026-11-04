import sys
import io
import zlib
import json
import httpx
import tifffile
import numpy as np

def inspect_scene_tiffs():
    scene_id = "S2C_MSIL2A_20261011T045721_R119_T44PMV_20261011T095104"
    granule = "L2A_T44PMV_A010957_20261011T050657"
    base_blob = f"https://sentinel2l2a01.blob.core.windows.net/sentinel2-l2/44/P/MV/2026/10/11/S2C_MSIL2A_20261011T045721_N0513_R119_T44PMV_20261011T095104.SAFE/GRANULE/{granule}/IMG_DATA"
    
    bands = {
        "B04 (Red 10m)": f"{base_blob}/R10m/T44PMV_20261011T045721_B04_10m.tif",
        "B08 (NIR 10m)": f"{base_blob}/R10m/T44PMV_20261011T045721_B08_10m.tif",
        "B11 (SWIR 20m)": f"{base_blob}/R20m/T44PMV_20261011T045721_B11_20m.tif",
        "SCL (Scene Class 20m)": f"{base_blob}/R20m/T44PMV_20261011T045721_SCL_20m.tif"
    }

    report = {}

    with httpx.Client(timeout=30.0) as client:
        for band_name, asset_url in bands.items():
            print(f"=== Inspecting {band_name} ===")
            sign_req = f"https://planetarycomputer.microsoft.com/api/sas/v1/sign?href={asset_url}"
            sign_resp = client.get(sign_req)
            if sign_resp.status_code != 200:
                print(f"Failed to sign: {sign_resp.status_code}")
                continue
            signed_url = sign_resp.json()["href"]

            # Fetch first 131,072 bytes (header and IFD)
            hdr_resp = client.get(signed_url, headers={"Range": "bytes=0-131071"})
            hdr_bytes = hdr_resp.content

            with tifffile.TiffFile(io.BytesIO(hdr_bytes)) as tif:
                page = tif.pages[0]
                tags = {tag.name: tag.value for tag in page.tags}
                
                band_info = {
                    "band": band_name,
                    "is_valid_tiff": True,
                    "is_geotiff": page.is_geotiff,
                    "imagewidth": page.imagewidth,
                    "imagelength": page.imagelength,
                    "tilewidth": page.tilewidth,
                    "tilelength": page.tilelength,
                    "bitspersample": page.bitspersample,
                    "sampleformat": str(page.sampleformat),
                    "compression": str(page.compression),
                    "predictor": page.predictor,
                    "fillorder": tags.get("FillOrder", "Not specified (default 1)"),
                    "photometric": str(page.photometric),
                    "planarconfig": str(page.planarconfig),
                    "nodata": tags.get("GDAL_NODATA", None),
                    "model_pixel_scale": list(page.tags["ModelPixelScaleTag"].value) if "ModelPixelScaleTag" in page.tags else None,
                    "model_tiepoint": list(page.tags["ModelTiepointTag"].value) if "ModelTiepointTag" in page.tags else None,
                    "total_tiles": len(page.tags["TileOffsets"].value) if "TileOffsets" in page.tags else 0,
                    "first_tile_offset": int(page.tags["TileOffsets"].value[0]) if "TileOffsets" in page.tags else None,
                    "first_tile_bytecount": int(page.tags["TileByteCounts"].value[0]) if "TileByteCounts" in page.tags else None
                }
                
                print(f"  Dimensions: {band_info['imagewidth']} x {band_info['imagelength']}")
                print(f"  Tile Shape: {band_info['tilewidth']} x {band_info['tilelength']}")
                print(f"  BitsPerSample: {band_info['bitspersample']}")
                print(f"  SampleFormat: {band_info['sampleformat']}")
                print(f"  Compression: {band_info['compression']}")
                print(f"  Predictor: {band_info['predictor']}")
                print(f"  Pixel Scale: {band_info['model_pixel_scale']}")
                print(f"  Tie Point: {band_info['model_tiepoint']}")
                print(f"  GDAL NoData: {band_info['nodata']}")

                report[band_name] = band_info

    with open("backend/tiff_inspection_report.json", "w") as f:
        json.dump(report, f, indent=2)

if __name__ == "__main__":
    inspect_scene_tiffs()
