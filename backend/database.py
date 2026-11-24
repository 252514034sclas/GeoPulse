from sqlalchemy import create_engine, text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import os
import logging

logger = logging.getLogger("geopulse.database")

try:
    from dotenv import load_dotenv
    load_dotenv(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env"))
except Exception:
    pass

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DEFAULT_DB_PATH = os.path.join(BASE_DIR, "geopulse.db").replace("\\", "/")
RAW_DATABASE_URL = os.getenv("DATABASE_URL", f"sqlite:///{DEFAULT_DB_PATH}")

# Normalize postgres URL to use psycopg2 driver if no driver explicitly specified
if RAW_DATABASE_URL.startswith("postgres://"):
    SQLALCHEMY_DATABASE_URL = RAW_DATABASE_URL.replace("postgres://", "postgresql+psycopg2://", 1)
elif RAW_DATABASE_URL.startswith("postgresql://"):
    SQLALCHEMY_DATABASE_URL = RAW_DATABASE_URL.replace("postgresql://", "postgresql+psycopg2://", 1)
else:
    SQLALCHEMY_DATABASE_URL = RAW_DATABASE_URL

IS_POSTGRES = SQLALCHEMY_DATABASE_URL.startswith("postgresql")

if IS_POSTGRES:
    # Production PostgreSQL with robust pooling & health checks
    # Strict fail-fast: Never fall back to SQLite when PostgreSQL is configured!
    try:
        engine = create_engine(
            SQLALCHEMY_DATABASE_URL,
            pool_size=10,
            max_overflow=20,
            pool_timeout=30,
            pool_recycle=3600,
            pool_pre_ping=True
        )
        # Immediate connection verification on startup
        with engine.connect() as test_conn:
            test_conn.execute(text("SELECT 1"))
        logger.info(f"[GeoPulse DB] Successfully connected to PostgreSQL: {engine.url.render_as_string(hide_password=True)}")
    except Exception as exc:
        logger.critical(f"[GeoPulse DB] FATAL: Failed to connect to PostgreSQL: {exc}")
        raise RuntimeError(f"PostgreSQL connection failed. Strict enforcement active - zero fallback to SQLite allowed. Error: {exc}") from exc
else:
    # SQLite configuration for standalone testing
    connect_args = {"check_same_thread": False}
    engine = create_engine(
        SQLALCHEMY_DATABASE_URL,
        connect_args=connect_args
    )
    logger.warning(f"[GeoPulse DB] Running on SQLite: {SQLALCHEMY_DATABASE_URL}")

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
