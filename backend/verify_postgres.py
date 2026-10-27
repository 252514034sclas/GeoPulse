import os
from sqlalchemy import create_engine, text

def verify_database_connection():
    database_url = os.getenv("DATABASE_URL")
    if not database_url:
        print("[-] DATABASE_URL environment variable is not set.")
        print("    Current default: SQLite (geopulse.db).")
        print("\n    To test PostgreSQL + PostGIS, set DATABASE_URL, e.g.:")
        print("    $env:DATABASE_URL=\"postgresql://postgres:password@localhost:5432/geopulse\"")
        return False

    print(f"[*] Attempting connection to: {database_url.split('@')[-1]} ...")
    try:
        engine = create_engine(database_url)
        with engine.connect() as connection:
            print("[+] Successfully connected to the database!")

            # Check PostGIS extension
            try:
                result = connection.execute(text("SELECT PostGIS_Full_Version();"))
                version = result.scalar()
                print(f"[+] PostGIS Extension is ENABLED! Version:\n    {version}")
            except Exception as postgis_err:
                print("[-] PostGIS extension query failed (PostGIS might not be enabled on this database).")
                print(f"    Error: {postgis_err}")
                print("    To enable PostGIS, run: CREATE EXTENSION postgis;")
                return False

            # Check spatial column on fields table if exists
            try:
                spatial_check = connection.execute(text("SELECT column_name, data_type FROM information_schema.columns WHERE table_name='fields' AND column_name='boundary';"))
                col = spatial_check.fetchone()
                if col:
                    print(f"[+] Spatial column 'boundary' found on 'fields' table (Type: {col[1]}).")
                else:
                    print("[!] 'boundary' spatial column not found on 'fields' table. Run migrations (alembic upgrade head).")
            except Exception as spatial_err:
                print(f"[!] Could not verify spatial columns: {spatial_err}")

        return True
    except Exception as err:
        print("[-] Failed to connect to PostgreSQL database.")
        print(f"    Error: {err}")
        print("\n    === PostgreSQL Windows Installation & Configuration Guide ===")
        print("    1. Download and install PostgreSQL from https://www.postgresql.org/download/windows/")
        print("    2. During installation, use Stack Builder to install PostGIS extension.")
        print("    3. Create a database in pgAdmin or via psql:")
        print("       CREATE DATABASE geopulse;")
        print("       \\c geopulse")
        print("       CREATE EXTENSION postgis;")
        print("    4. Set environment variable in PowerShell:")
        print("       $env:DATABASE_URL=\"postgresql://postgres:YOUR_PASSWORD@localhost:5432/geopulse\"")
        return False

if __name__ == "__main__":
    verify_database_connection()
