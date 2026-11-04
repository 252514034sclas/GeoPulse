def test_tile_alignment():
    # 10m grid:
    # Origin: (399960.0, 1500000.0)
    # Tile size: 512 * 10 = 5120 m
    # Tile col 2 -> Easting: [399960 + 2*5120, 399960 + 3*5120] = [410200, 415320]
    # Tile row 10 -> Northing: [1500000 - 11*5120, 1500000 - 10*5120] = [1443680, 1448800]
    e_10_min, e_10_max = 399960.0 + 2 * 5120.0, 399960.0 + 3 * 5120.0
    n_10_min, n_10_max = 1500000.0 - 11 * 5120.0, 1500000.0 - 10 * 5120.0
    print(f"10m Tile (Col 2, Row 10) extent:")
    print(f"  Easting:  [{e_10_min}, {e_10_max}]")
    print(f"  Northing: [{n_10_min}, {n_10_max}]")

    # 20m grid:
    # Origin: (399960.0, 1500000.0)
    # Tile size: 512 * 20 = 10240 m
    # For field center (~413800, ~1444400):
    col_20m = int((413800 - 399960.0) / 20.0) # 692
    row_20m = int((1500000.0 - 1444400) / 20.0) # 2780
    tile_col_20 = col_20m // 512 # 692 // 512 = 1
    tile_row_20 = row_20m // 512 # 2780 // 512 = 5
    
    e_20_min, e_20_max = 399960.0 + tile_col_20 * 10240.0, 399960.0 + (tile_col_20 + 1) * 10240.0
    n_20_min, n_20_max = 1500000.0 - (tile_row_20 + 1) * 10240.0, 1500000.0 - tile_row_20 * 10240.0
    print(f"\n20m Tile (Col {tile_col_20}, Row {tile_row_20}) extent:")
    print(f"  Easting:  [{e_20_min}, {e_20_max}]")
    print(f"  Northing: [{n_20_min}, {n_20_max}]")

    # Where inside the 20m tile does the 10m tile lie?
    offset_x_m = e_10_min - e_20_min
    offset_y_m = n_20_max - n_10_max
    print(f"\nOffset of 10m tile within 20m tile:")
    print(f"  X offset: {offset_x_m} m -> {offset_x_m / 20.0} pixels")
    print(f"  Y offset: {offset_y_m} m -> {offset_y_m / 20.0} pixels")

if __name__ == "__main__":
    test_tile_alignment()
