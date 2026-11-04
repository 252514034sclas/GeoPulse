import os
import sys
import math
import zlib
import io
import time
import json
import httpx
import numpy as np
from shapely.geometry import Polygon, Point, box
from shapely import wkt
import tifffile

def latlon_to_utm44n(lat: float, lon: float):
    a = 6378137.0
    f = 1.0 / 298.257223563
    e2 = 2 * f - f ** 2
    e_prime2 = e2 / (1 - e2)
    lat_rad = math.radians(lat)
    lon_rad = math.radians(lon)
    lon0_rad = math.radians(81.0)
    
    k0 = 0.9996
    N = a / math.sqrt(1 - e2 * math.sin(lat_rad) ** 2)
    T = math.tan(lat_rad) ** 2
    C = e_prime2 * math.cos(lat_rad) ** 2
    A = (lon_rad - lon0_rad) * math.cos(lat_rad)
    
    M = a * ((1 - e2 / 4 - 3 * e2 ** 2 / 64 - 5 * e2 ** 3 / 256) * lat_rad
             - (3 * e2 / 8 + 3 * e2 ** 2 / 32 + 45 * e2 ** 3 / 1024) * math.sin(2 * lat_rad)
             + (15 * e2 ** 2 / 256 + 45 * e2 ** 3 / 1024) * math.sin(4 * lat_rad)
             - (35 * e2 ** 3 / 3072) * math.sin(6 * lat_rad))
             
    easting = k0 * N * (A + (1 - T + C) * A ** 3 / 6 + (5 - 18 * T + T ** 2 + 72 * C - 58 * e_prime2) * A ** 5 / 120) + 500000.0
    northing = k0 * (M + N * math.tan(lat_rad) * (A ** 2 / 2 + (5 - T + 9 * C + 4 * C ** 2) * A ** 4 / 24 + (61 - 58 * T + T ** 2 + 600 * C - 330 * e_prime2) * A ** 6 / 720))
    return easting, northing

def unpack_15bit(data_bytes: bytes, target_shape=(512, 512)) -> np.ndarray:
    b = np.frombuffer(data_bytes, dtype=np.uint8).reshape(-1, 15)
    out = np.zeros((b.shape[0], 8), dtype=np.uint16)
    out[:, 0] = (b[:, 0].astype(np.uint16) << 7) | (b[:, 1].astype(np.uint16) >> 1)
    out[:, 1] = ((b[:, 1].astype(np.uint16) & 0x01) << 14) | (b[:, 2].astype(np.uint16) << 6) | (b[:, 3].astype(np.uint16) >> 2)
    out[:, 2] = ((b[:, 3].astype(np.uint16) & 0x03) << 13) | (b[:, 4].astype(np.uint16) << 5) | (b[:, 5].astype(np.uint16) >> 3)
    out[:, 3] = ((b[:, 5].astype(np.uint16) & 0x07) << 12) | (b[:, 6].astype(np.uint16) << 4) | (b[:, 7].astype(np.uint16) >> 4)
    out[:, 4] = ((b[:, 7].astype(np.uint16) & 0x0F) << 11) | (b[:, 8].astype(np.uint16) << 3) | (b[:, 9].astype(np.uint16) >> 5)
    out[:, 5] = ((b[:, 9].astype(np.uint16) & 0x1F) << 10) | (b[:, 10].astype(np.uint16) << 2) | (b[:, 11].astype(np.uint16) >> 6)
    out[:, 6] = ((b[:, 11].astype(np.uint16) & 0x3F) << 9) | (b[:, 12].astype(np.uint16) << 1) | (b[:, 13].astype(np.uint16) >> 7)
    out[:, 7] = ((b[:, 13].astype(np.uint16) & 0x7F) << 8) | b[:, 14].astype(np.uint16)
    return out.reshape(target_shape)

def run_independent_validation():
    # Field boundary
    boundary_wkt = "POLYGON((80.2020 13.0610, 80.2100 13.0615, 80.2090 13.0680, 80.2020 13.0675, 80.2020 13.0610))"
    poly_geo = wkt.loads(boundary_wkt)
    utm_coords = [latlon_to_utm44n(lat, lon) for lon, lat in poly_geo.exterior.coords]
    poly_utm = Polygon(utm_coords)
    min_utm_x, min_utm_y, max_utm_x, max_utm_y = poly_utm.bounds

    granule = "L2A_T44PMV_A010957_20261011T050657"
    base_blob = f"https://sentinel2l2a01.blob.core.windows.net/sentinel2-l2/44/P/MV/2026/10/11/S2C_MSIL2A_20261011T045721_N0513_R119_T44PMV_20261011T095104.SAFE/GRANULE/{granule}/IMG_DATA"
    
    b04_url = f"{base_blob}/R10m/T44PMV_20261011T045721_B04_10m.tif"
    b08_url = f"{base_blob}/R10m/T44PMV_20261011T045721_B08_10m.tif"
    b11_url = f"{base_blob}/R20m/T44PMV_20261011T045721_B11_20m.tif"
    scl_url = f"{base_blob}/R20m/T44PMV_20261011T045721_SCL_20m.tif"

    with httpx.Client(timeout=30.0) as client:
        # Download tiles
        def fetch_tile(url, tile_idx, is_20m=False, is_8bit=False):
            sign_req = f"https://planetarycomputer.microsoft.com/api/sas/v1/sign?href={url}"
            signed_url = client.get(sign_req).json()["href"]
            hdr = client.get(signed_url, headers={"Range": "bytes=0-131071"}).content
            with tifffile.TiffFile(io.BytesIO(hdr)) as tif:
                page = tif.pages[0]
                offset = page.tags["TileOffsets"].value[tile_idx]
                count = page.tags["TileByteCounts"].value[tile_idx]
            tile_bytes = client.get(signed_url, headers={"Range": f"bytes={offset}-{offset + count - 1}"}).content
            decomp = zlib.decompress(tile_bytes)
            if is_8bit:
                return np.frombuffer(decomp, dtype=np.uint8).reshape((512, 512))
            return unpack_15bit(decomp, (512, 512))

        print("Fetching B04 (10m)...")
        b04 = fetch_tile(b04_url, 222)
        print("Fetching B08 (10m)...")
        b08 = fetch_tile(b08_url, 222)
        print("Fetching B11 (20m)...")
        b11 = fetch_tile(b11_url, 56, is_20m=True)
        print("Fetching SCL (20m)...")
        scl = fetch_tile(scl_url, 56, is_20m=True, is_8bit=True)

    # 10m grid: Tile Col 2, Row 10 -> Origin (410200.0, 1448800.0)
    tile_orig_x, tile_orig_y = 410200.0, 1448800.0
    xs = tile_orig_x + np.arange(512) * 10.0 + 5.0
    ys = tile_orig_y - np.arange(512) * 10.0 - 5.0

    mask_10m = np.zeros((512, 512), dtype=bool)
    col_min = max(0, int((min_utm_x - tile_orig_x) / 10.0))
    col_max = min(512, int((max_utm_x - tile_orig_x) / 10.0) + 1)
    row_min = max(0, int((tile_orig_y - max_utm_y) / 10.0))
    row_max = min(512, int((tile_orig_y - min_utm_y) / 10.0) + 1)

    for r in range(row_min, row_max):
        py = ys[r]
        for c in range(col_min, col_max):
            px = xs[c]
            if poly_utm.contains(Point(px, py)):
                mask_10m[r, c] = True

    # 20m grid native mask
    # Tile Col 1, Row 5 -> Origin (410200.0, 1448800.0)
    xs_20 = 410200.0 + np.arange(512) * 20.0 + 10.0
    ys_20 = 1448800.0 - np.arange(512) * 20.0 - 10.0
    mask_20m = np.zeros((512, 512), dtype=bool)
    col20_min = max(0, int((min_utm_x - 410200.0) / 20.0))
    col20_max = min(512, int((max_utm_x - 410200.0) / 20.0) + 1)
    row20_min = max(0, int((1448800.0 - max_utm_y) / 20.0))
    row20_max = min(512, int((1448800.0 - min_utm_y) / 20.0) + 1)

    for r in range(row20_min, row20_max):
        py = ys_20[r]
        for c in range(col20_min, col20_max):
            px = xs_20[c]
            if poly_utm.contains(Point(px, py)):
                mask_20m[r, c] = True

    native_scl_pixels = scl[mask_20m]
    print(f"\n=== Native 20m SCL Cloud & Classification Audit ===")
    print(f"Total Native 20m Pixels: {len(native_scl_pixels)}")
    classes, counts = np.unique(native_scl_pixels, return_counts=True)
    class_names = {
        0: "NO_DATA", 1: "SATURATED", 2: "DARK_AREA", 3: "CLOUD_SHADOW",
        4: "VEGETATION", 5: "NOT_VEGETATED (Soil)", 6: "WATER", 7: "UNCLASSIFIED",
        8: "CLOUD_MED_PROB", 9: "CLOUD_HIGH_PROB", 10: "THIN_CIRRUS", 11: "SNOW"
    }
    cloud_classes = [3, 8, 9, 10]
    native_cloud_count = 0
    for cls, cnt in zip(classes, counts):
        name = class_names.get(cls, f"Class {cls}")
        pct = cnt / len(native_scl_pixels) * 100.0
        print(f"  Class {cls} ({name}): {cnt} pixels ({pct:.2f}%)")
        if cls in cloud_classes:
            native_cloud_count += cnt
    print(f"Native SCL Cloud/Shadow Count: {native_cloud_count} ({native_cloud_count/len(native_scl_pixels)*100.0:.2f}%)")

    # Resample 20m to 10m grid:
    # 1. Nearest neighbor (repeat 2x)
    b11_nearest_10m = np.repeat(np.repeat(b11[:256, :256], 2, axis=0), 2, axis=1)
    scl_nearest_10m = np.repeat(np.repeat(scl[:256, :256], 2, axis=0), 2, axis=1)

    # 2. Bilinear resampling for B11 using pure NumPy
    b11_crop = b11[:256, :256].astype(np.float64)
    # Pad borders
    padded = np.pad(b11_crop, ((0, 1), (0, 1)), mode='edge')
    # 4 sub-pixels per 20m pixel:
    top_left = 0.5625 * padded[:-1, :-1] + 0.1875 * padded[:-1, 1:] + 0.1875 * padded[1:, :-1] + 0.0625 * padded[1:, 1:]
    top_right = 0.1875 * padded[:-1, :-1] + 0.5625 * padded[:-1, 1:] + 0.0625 * padded[1:, :-1] + 0.1875 * padded[1:, 1:]
    bot_left = 0.1875 * padded[:-1, :-1] + 0.0625 * padded[:-1, 1:] + 0.5625 * padded[1:, :-1] + 0.1875 * padded[1:, 1:]
    bot_right = 0.0625 * padded[:-1, :-1] + 0.1875 * padded[:-1, 1:] + 0.1875 * padded[1:, :-1] + 0.5625 * padded[1:, 1:]
    b11_bilinear_10m = np.zeros((512, 512), dtype=np.float64)
    b11_bilinear_10m[0::2, 0::2] = top_left
    b11_bilinear_10m[0::2, 1::2] = top_right
    b11_bilinear_10m[1::2, 0::2] = bot_left
    b11_bilinear_10m[1::2, 1::2] = bot_right

    # Cloud mask on 10m:
    cloud_mask_10m = np.isin(scl_nearest_10m, cloud_classes)
    valid_mask_10m = mask_10m & (~cloud_mask_10m) & (b04 > 0) & (b08 > 0)
    print(f"\n=== 10m Field Pixel Validation ===")
    print(f"Total 10m Boundary Pixels: {np.sum(mask_10m)}")
    print(f"Valid Clear 10m Pixels:    {np.sum(valid_mask_10m)}")

    # Extract raw DN values
    dn_b04 = b04[valid_mask_10m].astype(np.float64)
    dn_b08 = b08[valid_mask_10m].astype(np.float64)
    dn_b11_nn = b11_nearest_10m[valid_mask_10m].astype(np.float64)
    dn_b11_bi = b11_bilinear_10m[valid_mask_10m].astype(np.float64)

    print(f"\nRaw DN Statistics over Field (5,901 pixels):")
    print(f"  B04 (Red):  Min={np.min(dn_b04)}, Max={np.max(dn_b04)}, Mean={np.mean(dn_b04):.1f}")
    print(f"  B08 (NIR):  Min={np.min(dn_b08)}, Max={np.max(dn_b08)}, Mean={np.mean(dn_b08):.1f}")
    print(f"  B11 (SWIR): Min={np.min(dn_b11_nn)}, Max={np.max(dn_b11_nn)}, Mean={np.mean(dn_b11_nn):.1f}")

    # Method 1: Previous uncorrected calculation (DN / 10000.0 without -1000 offset)
    ndvi_m1 = (dn_b08 - dn_b04) / (dn_b08 + dn_b04 + 1e-6)
    ndmi_m1 = (dn_b08 - dn_b11_nn) / (dn_b08 + dn_b11_nn + 1e-6)

    # Method 2: Baseline 04.00+ Official ESA Corrected Reflectance (DN - 1000) / 10000.0
    refl_b04_corr = (dn_b04 - 1000.0) / 10000.0
    refl_b08_corr = (dn_b08 - 1000.0) / 10000.0
    refl_b11_corr_nn = (dn_b11_nn - 1000.0) / 10000.0
    refl_b11_corr_bi = (dn_b11_bi - 1000.0) / 10000.0

    denom_ndvi = refl_b08_corr + refl_b04_corr
    valid_denom_ndvi = denom_ndvi > 1e-5
    ndvi_corr = np.zeros_like(denom_ndvi)
    ndvi_corr[valid_denom_ndvi] = (refl_b08_corr[valid_denom_ndvi] - refl_b04_corr[valid_denom_ndvi]) / denom_ndvi[valid_denom_ndvi]

    denom_ndmi_nn = refl_b08_corr + refl_b11_corr_nn
    valid_denom_ndmi_nn = denom_ndmi_nn > 1e-5
    ndmi_corr_nn = np.zeros_like(denom_ndmi_nn)
    ndmi_corr_nn[valid_denom_ndmi_nn] = (refl_b08_corr[valid_denom_ndmi_nn] - refl_b11_corr_nn[valid_denom_ndmi_nn]) / denom_ndmi_nn[valid_denom_ndmi_nn]

    denom_ndmi_bi = refl_b08_corr + refl_b11_corr_bi
    valid_denom_ndmi_bi = denom_ndmi_bi > 1e-5
    ndmi_corr_bi = np.zeros_like(denom_ndmi_bi)
    ndmi_corr_bi[valid_denom_ndmi_bi] = (refl_b08_corr[valid_denom_ndmi_bi] - refl_b11_corr_bi[valid_denom_ndmi_bi]) / denom_ndmi_bi[valid_denom_ndmi_bi]

    print("\n" + "=" * 70)
    print("COMPARATIVE STATISTICAL AUDIT (5,901 VALID PIXELS)")
    print("=" * 70)
    print("--- Method 1: Raw DN without -1000 Baseline Offset (Previously Reported) ---")
    print(f"NDVI Mean: {np.mean(ndvi_m1):.4f} | Min: {np.min(ndvi_m1):.4f} | Max: {np.max(ndvi_m1):.4f} | Std: {np.std(ndvi_m1):.4f}")
    print(f"NDMI Mean: {np.mean(ndmi_m1):.4f} | Min: {np.min(ndmi_m1):.4f} | Max: {np.max(ndmi_m1):.4f} | Std: {np.std(ndmi_m1):.4f}")

    print("\n--- Method 2: ESA Baseline 05.13 BOA_ADD_OFFSET (-1000 DN) Corrected (True Physical Reflectance) ---")
    print(f"NDVI Mean: {np.mean(ndvi_corr):.4f} | Min: {np.min(ndvi_corr):.4f} | Max: {np.max(ndvi_corr):.4f} | Std: {np.std(ndvi_corr):.4f}")
    print(f"NDMI Mean (NN): {np.mean(ndmi_corr_nn):.4f} | Min: {np.min(ndmi_corr_nn):.4f} | Max: {np.max(ndmi_corr_nn):.4f} | Std: {np.std(ndmi_corr_nn):.4f}")
    print(f"NDMI Mean (Bilinear): {np.mean(ndmi_corr_bi):.4f} | Min: {np.min(ndmi_corr_bi):.4f} | Max: {np.max(ndmi_corr_bi):.4f} | Std: {np.std(ndmi_corr_bi):.4f}")

    audit_stats = {
        "valid_pixel_count": int(np.sum(valid_mask_10m)),
        "native_20m_scl_pixels": int(len(native_scl_pixels)),
        "cloud_pct_native_scl": round(native_cloud_count / len(native_scl_pixels) * 100.0, 2),
        "method_1_raw_dn": {
            "ndvi": {"mean": round(float(np.mean(ndvi_m1)), 4), "min": round(float(np.min(ndvi_m1)), 4), "max": round(float(np.max(ndvi_m1)), 4), "std": round(float(np.std(ndvi_m1)), 4)},
            "ndmi": {"mean": round(float(np.mean(ndmi_m1)), 4), "min": round(float(np.min(ndmi_m1)), 4), "max": round(float(np.max(ndmi_m1)), 4), "std": round(float(np.std(ndmi_m1)), 4)}
        },
        "method_2_esa_offset_corrected": {
            "ndvi": {"mean": round(float(np.mean(ndvi_corr)), 4), "min": round(float(np.min(ndvi_corr)), 4), "max": round(float(np.max(ndvi_corr)), 4), "std": round(float(np.std(ndvi_corr)), 4)},
            "ndmi_nearest": {"mean": round(float(np.mean(ndmi_corr_nn)), 4), "min": round(float(np.min(ndmi_corr_nn)), 4), "max": round(float(np.max(ndmi_corr_nn)), 4), "std": round(float(np.std(ndmi_corr_nn)), 4)},
            "ndmi_bilinear": {"mean": round(float(np.mean(ndmi_corr_bi)), 4), "min": round(float(np.min(ndmi_corr_bi)), 4), "max": round(float(np.max(ndmi_corr_bi)), 4), "std": round(float(np.std(ndmi_corr_bi)), 4)}
        }
    }

    with open("backend/independent_audit_stats.json", "w") as f:
        json.dump(audit_stats, f, indent=2)

if __name__ == "__main__":
    run_independent_validation()
