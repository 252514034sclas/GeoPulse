import sqlite3, os

db_path = os.path.join(os.path.dirname(__file__), "geopulse.db")
if os.path.exists(db_path):
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()
    cur.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [t[0] for t in cur.fetchall() if not t[0].startswith('sqlite_')]
    print("SQLite Tables found:", tables)
    for t in tables:
        cur.execute(f"SELECT count(*) FROM {t}")
        print(f"  {t}: {cur.fetchone()[0]} rows")
    conn.close()
else:
    print("No SQLite db file found.")
