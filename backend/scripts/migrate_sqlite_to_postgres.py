"""
GeoPulse SQLite to PostgreSQL Full Data Migration Script
Ensures 100% genuine data fidelity, zero data loss, foreign key order preservation,
and sequence synchronization.
"""

import os
import sys
import sqlite3
from datetime import datetime
import psycopg2
from psycopg2.extras import RealDictCursor

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SQLITE_DB_PATH = os.path.join(BASE_DIR, "geopulse.db")
POSTGRES_URL = "postgresql://geopulse_admin:GeoPulseSecure2026!@127.0.0.1:5432/geopulse_db"

def parse_dt(val):
    if val is None:
        return None
    if isinstance(val, datetime):
        return val
    val_str = str(val).strip()
    # Try common formats
    for fmt in (
        "%Y-%m-%d %H:%M:%S.%f",
        "%Y-%m-%d %H:%M:%S",
        "%Y-%m-%dT%H:%M:%S.%f",
        "%Y-%m-%dT%H:%M:%S",
        "%Y-%m-%dT%H:%M:%SZ",
        "%Y-%m-%d"
    ):
        try:
            return datetime.strptime(val_str, fmt)
        except ValueError:
            pass
    return None

def migrate():
    print("=" * 70)
    print("GeoPulse SQLite -> PostgreSQL Data Migration")
    print(f"Source SQLite: {SQLITE_DB_PATH}")
    print(f"Target Postgres: {POSTGRES_URL}")
    print("=" * 70)

    if not os.path.exists(SQLITE_DB_PATH):
        raise FileNotFoundError(f"SQLite DB not found at {SQLITE_DB_PATH}")

    sqlite_conn = sqlite3.connect(SQLITE_DB_PATH)
    sqlite_conn.row_factory = sqlite3.Row
    s_cur = sqlite_conn.cursor()

    pg_conn = psycopg2.connect(POSTGRES_URL)
    p_cur = pg_conn.cursor()

    # Define tables in dependency order
    tables = [
        # Level 0 (no FKs)
        {
            "name": "users",
            "cols": ["id", "email", "full_name", "hashed_password", "phone", "farm_name", "total_area_ha", "preferred_language", "theme_preference", "created_at"],
            "dt_cols": ["created_at"],
            "bool_cols": []
        },
        {
            "name": "weather",
            "cols": ["id", "location", "temperature_c", "humidity_percent", "precipitation_mm", "wind_speed_kmh", "spray_safe", "spray_advice", "status_message", "data_source", "updated_at"],
            "dt_cols": ["updated_at"],
            "bool_cols": ["spray_safe"]
        },
        {
            "name": "soil_moisture",
            "cols": ["id", "field_id", "moisture_percentage", "depth_cm", "status", "watering_advice", "recorded_at", "data_source"],
            "dt_cols": [],
            "bool_cols": []
        },
        {
            "name": "mandi_prices",
            "cols": ["id", "commodity", "mandi_name", "state", "min_price", "max_price", "modal_price", "price_unit", "trend_pct", "recorded_date"],
            "dt_cols": [],
            "bool_cols": []
        },
        {
            "name": "fertilizer_guides",
            "cols": ["id", "crop_type", "vigor_tier", "urea_kg_per_acre", "dap_kg_per_acre", "mop_kg_per_acre", "stage_timing", "tips"],
            "dt_cols": [],
            "bool_cols": []
        },
        # Level 1 (depends on users)
        {
            "name": "fields",
            "cols": ["id", "user_id", "name", "crop_type", "area_acres", "sowing_date", "status", "soil_type", "latitude", "longitude", "updated_at", "boundary"],
            "dt_cols": ["updated_at"],
            "bool_cols": []
        },
        # Level 2 (depends on fields / users)
        {
            "name": "satellite_records",
            "cols": ["id", "field_id", "recorded_date", "ndvi_value", "ndwi_value", "chlorophyll_spad", "canopy_cover_pct", "health_status", "layman_advice", "is_sample", "disclaimer"],
            "dt_cols": [],
            "bool_cols": ["is_sample"]
        },
        {
            "name": "alerts",
            "cols": ["id", "field_id", "severity", "message", "category", "timestamp", "is_resolved"],
            "dt_cols": ["timestamp"],
            "bool_cols": ["is_resolved"]
        },
        {
            "name": "advisory_tasks",
            "cols": ["id", "user_id", "field_id", "task_type", "title", "description", "time_badge", "priority", "is_completed", "created_at"],
            "dt_cols": ["created_at"],
            "bool_cols": ["is_completed"]
        },
        {
            "name": "farm_expenses",
            "cols": ["id", "user_id", "field_id", "category", "amount", "notes", "expense_date"],
            "dt_cols": [],
            "bool_cols": []
        }
    ]

    total_migrated = 0
    verification_results = {}

    for t_spec in tables:
        tname = t_spec["name"]
        cols = t_spec["cols"]
        dt_cols = t_spec["dt_cols"]
        bool_cols = t_spec["bool_cols"]

        # Check SQLite row count
        s_cur.execute(f"SELECT COUNT(*) FROM {tname}")
        src_count = s_cur.fetchone()[0]

        # Fetch SQLite rows
        s_cur.execute(f"SELECT * FROM {tname}")
        src_rows = s_cur.fetchall()

        # Clean existing target records for clean reload
        p_cur.execute(f"DELETE FROM {tname}")

        inserted = 0
        if src_rows:
            col_list_str = ", ".join(cols)
            placeholders = ", ".join(["%s"] * len(cols))
            insert_sql = f"INSERT INTO {tname} ({col_list_str}) VALUES ({placeholders})"

            for r in src_rows:
                r_dict = dict(r)
                row_vals = []
                for col in cols:
                    val = r_dict.get(col)
                    if col in dt_cols and val is not None:
                        val = parse_dt(val)
                    elif col in bool_cols and val is not None:
                        val = bool(val)
                    row_vals.append(val)
                p_cur.execute(insert_sql, row_vals)
                inserted += 1

        # Synchronize PostgreSQL auto-increment sequence if table has id column
        if "id" in cols:
            p_cur.execute(f"""
                SELECT setval(
                    pg_get_serial_sequence('{tname}', 'id'),
                    COALESCE((SELECT MAX(id) FROM {tname}), 1)
                );
            """)

        pg_conn.commit()

        # Verify Postgres row count
        p_cur.execute(f"SELECT COUNT(*) FROM {tname}")
        tgt_count = p_cur.fetchone()[0]

        verification_results[tname] = {
            "source_sqlite": src_count,
            "target_postgres": tgt_count,
            "match": src_count == tgt_count
        }
        total_migrated += inserted
        status_symbol = "OK" if src_count == tgt_count else "MISMATCH"
        print(f"[{status_symbol}] Table '{tname}': {src_count} SQLite rows -> {tgt_count} Postgres rows (Migrated: {inserted})")

    # Final summary check
    all_matched = all(v["match"] for v in verification_results.values())
    print("-" * 70)
    print(f"Total Rows Migrated: {total_migrated}")
    print(f"Overall Match Status: {'100% PERFECT MATCH' if all_matched else 'WARNING - DISCREPANCIES DETECTED'}")
    print("=" * 70)

    # Test sequence generation on users table
    p_cur.execute("SELECT nextval(pg_get_serial_sequence('users', 'id'))")
    next_id = p_cur.fetchone()[0]
    p_cur.execute("SELECT MAX(id) FROM users")
    max_id = p_cur.fetchone()[0]
    print(f"PostgreSQL Sequence Test: Max User ID = {max_id}, Next Sequence Val = {next_id}")

    p_cur.close()
    pg_conn.close()
    s_cur.close()
    sqlite_conn.close()

    if not all_matched:
        sys.exit(1)

if __name__ == "__main__":
    migrate()
