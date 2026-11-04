import math
import numpy as np
from shapely.geometry import Polygon, Point, box
from shapely import wkt

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

def validate_field_geometry():
    wkt_str = "POLYGON((80.2020 13.0610, 80.2100 13.0615, 80.2090 13.0680, 80.2020 13.0675, 80.2020 13.0610))"
    poly_geo = wkt.loads(wkt_str)
    
    # 1. Project to UTM 44N
    utm_coords = [latlon_to_utm44n(lat, lon) for lon, lat in poly_geo.exterior.coords]
    poly_utm = Polygon(utm_coords)
    
    # Continuous geometric area
    geom_area_m2 = poly_utm.area
    geom_area_ha = geom_area_m2 / 10000.0
    geom_area_acres = geom_area_m2 * 0.000247105
    
    print("=== Geometric Polygon Area (Continuous) ===")
    print(f"Area in sq meters: {geom_area_m2:.2f} m²")
    print(f"Area in hectares:  {geom_area_ha:.4f} ha")
    print(f"Area in acres:     {geom_area_acres:.2f} acres")
    print(f"UTM 44N Bounds:    {poly_utm.bounds}")

    # 2. Raster Grid at 10m Native Sentinel-2 Alignment
    # Origin of T44PMV: Easting 399960.0, Northing 1500000.0
    tile_origin_x = 399960.0 + 1 * 512 * 10.0 # Col 1 = 405080.0
    # Wait, let's find the exact tile_col and tile_row for the field
    centroid_lat, centroid_lon = poly_geo.centroid.y, poly_geo.centroid.x
    c_x, c_y = latlon_to_utm44n(centroid_lat, centroid_lon)
    col_10m = int((c_x - 399960.0) / 10.0)
    row_10m = int((1500000.0 - c_y) / 10.0)
    tile_col_10m = col_10m // 512
    tile_row_10m = row_10m // 512
    t_orig_x = 399960.0 + tile_col_10m * 512 * 10.0
    t_orig_y = 1500000.0 - tile_row_10m * 512 * 10.0

    print(f"\nTile Index 10m: Row {tile_row_10m}, Col {tile_col_10m} (Origin X: {t_orig_x}, Y: {t_orig_y})")

    min_x, min_y, max_x, max_y = poly_utm.bounds
    col_min = max(0, int((min_x - t_orig_x) / 10.0))
    col_max = min(512, int((max_x - t_orig_x) / 10.0) + 1)
    row_min = max(0, int((t_orig_y - max_y) / 10.0))
    row_max = min(512, int((t_orig_y - min_y) / 10.0) + 1)

    print(f"Grid Scan Range: Cols [{col_min}, {col_max}], Rows [{row_min}, {row_max}]")

    centroid_inside_count = 0
    intersects_count = 0
    contained_count = 0
    fractional_area_sum = 0.0

    for r in range(row_min, row_max):
        # Pixel bounds in UTM
        top = t_orig_y - r * 10.0
        bottom = top - 10.0
        center_y = top - 5.0
        for c in range(col_min, col_max):
            left = t_orig_x + c * 10.0
            right = left + 10.0
            center_x = left + 5.0

            pixel_pt = Point(center_x, center_y)
            pixel_cell = box(left, bottom, right, top)

            if poly_utm.contains(pixel_pt):
                centroid_inside_count += 1

            if poly_utm.intersects(pixel_cell):
                intersects_count += 1
                inter = poly_utm.intersection(pixel_cell)
                fractional_area_sum += inter.area
                if inter.area >= 99.99:
                    contained_count += 1

    print(f"\n=== Rasterization Results (10m Resolution) ===")
    print(f"Centroid Point-in-Polygon (reported method): {centroid_inside_count} pixels")
    print(f"Pixel Area from Centroid Method:             {centroid_inside_count * 100.0:.2f} m² ({centroid_inside_count * 100.0 * 0.000247105:.2f} acres)")
    print(f"Fully Contained Interior Pixels:             {contained_count} pixels ({contained_count * 100.0:.2f} m²)")
    print(f"Any-Intersection Pixels (Touching boundary): {intersects_count} pixels ({intersects_count * 100.0:.2f} m²)")
    print(f"Exact Analytical Polygon Intersection Area:  {fractional_area_sum:.2f} m² ({fractional_area_sum * 0.000247105:.2f} acres)")
    
    diff_centroid_vs_geom = (centroid_inside_count * 100.0) - geom_area_m2
    diff_pct = (diff_centroid_vs_geom / geom_area_m2) * 100.0
    print(f"\nDifference (Centroid Raster Area - Continuous Geometric Area): {diff_centroid_vs_geom:+.2f} m² ({diff_pct:+.3f}%)")

    # 3. Native 20m Grid Analysis
    # Let's also check native 20m grid (for SCL and B11)
    col_20m = int((c_x - 399960.0) / 20.0)
    row_20m = int((1500000.0 - c_y) / 20.0)
    tile_col_20m = col_20m // 512
    tile_row_20m = row_20m // 512
    t20_orig_x = 399960.0 + tile_col_20m * 512 * 20.0
    t20_orig_y = 1500000.0 - tile_row_20m * 512 * 20.0

    col20_min = max(0, int((min_x - t20_orig_x) / 20.0))
    col20_max = min(512, int((max_x - t20_orig_x) / 20.0) + 1)
    row20_min = max(0, int((t20_orig_y - max_y) / 20.0))
    row20_max = min(512, int((t20_orig_y - min_y) / 20.0) + 1)

    scl_20m_centroid_count = 0
    for r in range(row20_min, row20_max):
        top = t20_orig_y - r * 20.0
        center_y = top - 10.0
        for c in range(col20_min, col20_max):
            left = t20_orig_x + c * 20.0
            center_x = left + 10.0
            if poly_utm.contains(Point(center_x, center_y)):
                scl_20m_centroid_count += 1

    print(f"\n=== Native 20m Grid Analysis (for SCL & B11) ===")
    print(f"Native 20m Intersecting Centroid Pixels: {scl_20m_centroid_count} pixels")
    print(f"Area at 400 m² per pixel: {scl_20m_centroid_count * 400.0:.2f} m² ({scl_20m_centroid_count * 400.0 * 0.000247105:.2f} acres)")

if __name__ == "__main__":
    validate_field_geometry()
