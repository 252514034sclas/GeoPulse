# GeoPulse Python Backend (Phase 1 & Architecture Support)

This directory contains the FastAPI backend for the GeoPulse agricultural intelligence application, supporting both local SQLite (default for development) and production-ready **PostgreSQL + PostGIS** for relational and geospatial data.

## Prerequisites
- Python 3.10+ installed
- (Optional for production) PostgreSQL with PostGIS extension enabled

## Setup Instructions

1. **Navigate to the backend directory**:
   ```bash
   cd backend
   ```

2. **Create and activate a virtual environment**:
   - On Windows (PowerShell):
     ```bash
     python -m venv venv
     .\venv\Scripts\Activate.ps1
     ```
   - On macOS/Linux:
     ```bash
     python3 -m venv venv
     source venv/bin/activate
     ```

3. **Install Dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Database Configuration (SQLite vs PostgreSQL + PostGIS)**:
   - **Default (SQLite)**: Automatically uses `geopulse.db` locally.
   - **Production (PostgreSQL + PostGIS)**: Set the `DATABASE_URL` environment variable before running the server:
     ```bash
     # Windows PowerShell
     $env:DATABASE_URL="postgresql://username:password@localhost:5432/geopulse_db"
     
     # Linux/macOS
     export DATABASE_URL="postgresql://username:password@localhost:5432/geopulse_db"
     ```

5. **Run the FastAPI Server**:
   ```bash
   uvicorn main:app --reload --host 127.0.0.1 --port 8000
   ```

## API Documentation & Testing
Once the server is running, you can access:
- **Interactive Swagger UI**: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
- **Alternative ReDoc UI**: [http://127.0.0.1:8000/redoc](http://127.0.0.1:8000/redoc)

## Implemented Endpoints
- `GET /health` — Backend health check and status.
- `GET /api/v1/dashboard` — Aggregated agricultural dashboard summary.
- `GET /api/v1/fields` — List of registered agricultural fields.
- `GET /api/v1/alerts` — Field alerts and monitoring notifications.
- `GET /api/v1/vegetation-health` — Vegetation health records (NDVI sample data with disclaimers).
- `GET /api/v1/weather` — Weather conditions and data source status.
- `GET /api/v1/soil-moisture` — Soil moisture measurements across fields.

## Data Integrity & Disclaimers
- All data returned by these endpoints consists of **sample/demo seed data**.
- Simulated sensor readings, weather reports, and NDVI results are explicitly flagged as demo/sample data. Real-time telemetry and actual satellite imagery calculation pipelines are not yet connected.

