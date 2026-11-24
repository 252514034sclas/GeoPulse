import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

def setup():
    conn = psycopg2.connect(host='127.0.0.1', port=5432, user='postgres', dbname='postgres')
    conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = conn.cursor()

    cur.execute("SELECT 1 FROM pg_roles WHERE rolname='geopulse_admin';")
    if not cur.fetchone():
        cur.execute("CREATE USER geopulse_admin WITH PASSWORD 'GeoPulseSecure2026!';")
        print("Created user geopulse_admin")
    else:
        cur.execute("ALTER USER geopulse_admin WITH PASSWORD 'GeoPulseSecure2026!';")
        print("Updated user geopulse_admin")

    cur.execute("SELECT 1 FROM pg_database WHERE datname='geopulse_db';")
    if not cur.fetchone():
        cur.execute("CREATE DATABASE geopulse_db OWNER geopulse_admin;")
        print("Created database geopulse_db")
    else:
        print("Database geopulse_db already exists")

    cur.execute("GRANT ALL PRIVILEGES ON DATABASE geopulse_db TO geopulse_admin;")
    cur.close()
    conn.close()

    # Now verify connection as geopulse_admin to geopulse_db
    conn_app = psycopg2.connect(
        host='127.0.0.1',
        port=5432,
        user='geopulse_admin',
        password='GeoPulseSecure2026!',
        dbname='geopulse_db'
    )
    cur_app = conn_app.cursor()
    cur_app.execute("SELECT current_user, current_database();")
    u, db = cur_app.fetchone()
    print(f"Verified connection as '{u}' to database '{db}'!")
    cur_app.close()
    conn_app.close()

if __name__ == "__main__":
    setup()
