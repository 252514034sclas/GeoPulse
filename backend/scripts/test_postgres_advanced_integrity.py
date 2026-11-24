"""
PostgreSQL Advanced Integrity, Foreign Keys, Concurrency, and Clean Migration Test Suite
"""

import os
import sys
import threading
import time
import psycopg2
from psycopg2 import sql
from psycopg2.errors import ForeignKeyViolation, UniqueViolation
from sqlalchemy import create_engine, text
from alembic.config import Config
from alembic import command

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
POSTGRES_BASE_URL = "postgresql://geopulse_admin:GeoPulseSecure2026!@127.0.0.1:5432"
APP_DB_URL = f"{POSTGRES_BASE_URL}/geopulse_db"
CLEAN_TEST_DB = "geopulse_clean_mig_test"

def test_foreign_keys_and_constraints():
    print("--- 1. TESTING FOREIGN KEYS & CONSTRAINTS ---")
    conn = psycopg2.connect(APP_DB_URL)
    conn.autocommit = False
    cur = conn.cursor()

    # 1.1 Test Invalid user_id in fields table
    fk_caught = False
    try:
        cur.execute("""
            INSERT INTO fields (user_id, name, crop_type, area_acres, status, soil_type, latitude, longitude)
            VALUES (999999, 'Invalid FK Field', 'Rice', 10.0, 'Monitored', 'Loam', 10.0, 79.0)
        """)
        conn.commit()
    except ForeignKeyViolation:
        conn.rollback()
        fk_caught = True
        print("[PASS] Foreign Key Constraint: Inserting field with non-existent user_id=999999 rejected (ForeignKeyViolation).")
    except Exception as e:
        conn.rollback()
        print(f"[FAIL] Unexpected exception for invalid FK: {e}")

    assert fk_caught, "FAIL: Foreign Key violation was NOT caught on fields.user_id!"

    # 1.2 Test Invalid field_id in satellite_records table
    fk_sat_caught = False
    try:
        cur.execute("""
            INSERT INTO satellite_records (field_id, recorded_date, ndvi_value)
            VALUES (999999, '2026-11-24', 0.75)
        """)
        conn.commit()
    except ForeignKeyViolation:
        conn.rollback()
        fk_sat_caught = True
        print("[PASS] Foreign Key Constraint: Inserting satellite record with non-existent field_id=999999 rejected (ForeignKeyViolation).")
    except Exception as e:
        conn.rollback()
        print(f"[FAIL] Unexpected exception for invalid FK: {e}")

    assert fk_sat_caught, "FAIL: Foreign Key violation was NOT caught on satellite_records.field_id!"

    # 1.3 Test Unique Email Constraint on users table
    unique_caught = False
    try:
        cur.execute("""
            INSERT INTO users (email, full_name, hashed_password)
            VALUES ('farmer_release@geopulse.com', 'Duplicate Name', 'hash123')
        """)
        conn.commit()
    except UniqueViolation:
        conn.rollback()
        unique_caught = True
        print("[PASS] Unique Constraint: Inserting duplicate email 'farmer_release@geopulse.com' rejected (UniqueViolation).")
    except Exception as e:
        conn.rollback()
        print(f"[FAIL] Unexpected exception for duplicate email: {e}")

    assert unique_caught, "FAIL: Unique violation was NOT caught on users.email!"

    cur.close()
    conn.close()

def test_transaction_rollback():
    print("--- 2. TESTING TRANSACTION ROLLBACK (ACID) ---")
    conn = psycopg2.connect(APP_DB_URL)
    conn.autocommit = False
    cur = conn.cursor()

    test_email = "rollback_test_user@geopulse.com"

    # Clean up if existed
    cur.execute("DELETE FROM users WHERE email = %s", (test_email,))
    conn.commit()

    try:
        # Begin insert in transaction
        cur.execute("""
            INSERT INTO users (email, full_name, hashed_password)
            VALUES (%s, 'Rollback Test Farmer', 'hash123')
        """, (test_email,))
        # Simulate business logic error before commit
        raise RuntimeError("Simulated failure trigger for rollback test")
    except RuntimeError:
        conn.rollback()
        print("[INFO] Simulated exception triggered transaction rollback.")

    # Verify record does NOT exist
    cur.execute("SELECT COUNT(*) FROM users WHERE email = %s", (test_email,))
    count = cur.fetchone()[0]
    cur.close()
    conn.close()

    assert count == 0, f"FAIL: Rolled back transaction persisted! Found {count} records."
    print("[PASS] Transaction Rollback: Uncommitted record was cleanly discarded.")

def test_concurrent_database_operations():
    print("--- 3. TESTING CONCURRENT DATABASE OPERATIONS ---")
    num_threads = 5
    errors = []

    def worker(thread_idx):
        try:
            c = psycopg2.connect(APP_DB_URL)
            c.autocommit = True
            cur = c.cursor()
            for i in range(10):
                # Read operation
                cur.execute("SELECT count(*) FROM users")
                cnt = cur.fetchone()[0]
                # Light write and delete
                t_email = f"concurrency_t{thread_idx}_{i}_{time.time()}@geopulse.com"
                cur.execute("INSERT INTO users (email, full_name, hashed_password) VALUES (%s, 'Worker', 'hash')", (t_email,))
                cur.execute("DELETE FROM users WHERE email = %s", (t_email,))
            cur.close()
            c.close()
        except Exception as exc:
            errors.append(f"Thread {thread_idx} error: {exc}")

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(num_threads)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    assert len(errors) == 0, f"FAIL: Concurrency errors detected: {errors}"
    print(f"[PASS] Concurrency: {num_threads} simultaneous threads completed 50 concurrent transactions with 0 errors/deadlocks.")

def test_clean_database_migrations():
    print("--- 4. TESTING MIGRATIONS FROM A CLEAN DATABASE ---")
    # Connect to PostgreSQL to create temporary database
    admin_conn = psycopg2.connect(f"{POSTGRES_BASE_URL}/geopulse_db")
    admin_conn.autocommit = True
    admin_cur = admin_conn.cursor()

    # Drop existing test db if any
    admin_cur.execute(f"DROP DATABASE IF EXISTS {CLEAN_TEST_DB}")
    admin_cur.execute(f"CREATE DATABASE {CLEAN_TEST_DB} OWNER geopulse_admin")
    print(f"[INFO] Created clean test database '{CLEAN_TEST_DB}'.")

    clean_db_url = f"{POSTGRES_BASE_URL}/{CLEAN_TEST_DB}"
    os.environ["ALEMBIC_DB_URL"] = clean_db_url

    # Configure Alembic to point to the clean test db
    alembic_cfg_path = os.path.join(BASE_DIR, "alembic.ini")
    alembic_cfg = Config(alembic_cfg_path)
    alembic_cfg.set_main_option("script_location", os.path.join(BASE_DIR, "alembic"))
    alembic_cfg.set_main_option("sqlalchemy.url", clean_db_url)

    # Run alembic upgrade head
    command.upgrade(alembic_cfg, "head")
    print("[INFO] Applied 'alembic upgrade head' on clean database.")

    # Remove env var
    os.environ.pop("ALEMBIC_DB_URL", None)

    # Verify all 11 tables exist in the clean database
    test_conn = psycopg2.connect(clean_db_url)
    test_cur = test_conn.cursor()
    test_cur.execute("""
        SELECT table_name FROM information_schema.tables 
        WHERE table_schema = 'public' ORDER BY table_name;
    """)
    tables = [r[0] for r in test_cur.fetchall()]
    test_cur.close()
    test_conn.close()

    expected_tables = [
        'advisory_tasks', 'alembic_version', 'alerts', 'farm_expenses',
        'fertilizer_guides', 'fields', 'mandi_prices', 'satellite_records',
        'soil_moisture', 'users', 'weather'
    ]
    assert tables == expected_tables, f"FAIL: Tables mismatch in clean db: {tables} vs {expected_tables}"
    print(f"[PASS] Clean Database Migrations: All 11 tables successfully generated from scratch.")

    # Drop the temporary clean test database
    admin_cur.execute(f"DROP DATABASE {CLEAN_TEST_DB}")
    admin_cur.close()
    admin_conn.close()
    print(f"[INFO] Clean test database '{CLEAN_TEST_DB}' dropped cleanly.")

def main():
    print("=" * 80)
    print("GEOPULSE ADVANCED POSTGRESQL INTEGRITY & CONCURRENCY AUDIT")
    print("=" * 80)
    test_foreign_keys_and_constraints()
    test_transaction_rollback()
    test_concurrent_database_operations()
    test_clean_database_migrations()
    print("=" * 80)
    print("ALL ADVANCED POSTGRESQL INTEGRITY TESTS PASSED (100% PASS)!")
    print("=" * 80)

if __name__ == "__main__":
    main()
