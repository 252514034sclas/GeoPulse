# GeoPulse — Agricultural & Geospatial Intelligence Platform

GeoPulse is a cross-platform agricultural intelligence solution comprising an Android application (built with Kotlin and Jetpack Compose) and a Python FastAPI backend with SQLAlchemy, GeoAlchemy2, and Shapely spatial support.

---

## Architecture & Technology Stack

- **Android App (`/app`)**: Kotlin, Jetpack Compose, Material 3, 30-screen feature & navigation suite (`SplashActivity` to `Screen30Activity`), Bottom Navigation, and interactive dialogs.
- **Backend (`/backend`)**: Python FastAPI, SQLAlchemy ORM, Pydantic schemas, Alembic migrations, Shapely spatial operations, and SQLite fallback (with full PostgreSQL + PostGIS readiness).

---

## Home-Laptop Setup Guide

Follow these instructions to clone and run GeoPulse on your home laptop:

### 1. Prerequisites
- Install [Git](https://git-scm.com/)
- Install [Android Studio](https://developer.android.com/studio) (latest stable version with Android SDK 34+)
- Install [Python 3.10+](https://www.python.org/)

### 2. Clone the Repository
```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd GeoPulse
```

### 3. Open and Run the Android App
1. Open Android Studio.
2. Select **Open** and choose the `GeoPulse` root directory.
3. Allow Gradle to synchronize dependencies.
4. Start an Android Emulator (API 34+ recommended) or connect a physical Android device.
5. Click **Run 'app'** to build and deploy the app.

### 4. Run the Python FastAPI Backend
1. Open a terminal in the project root and navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python -m venv venv
   # On Windows (PowerShell):
   .\venv\Scripts\Activate.ps1
   # On macOS / Linux:
   source venv/bin/activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run the FastAPI server:
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```
5. Test API endpoints at `http://localhost:8000/docs`.

### 5. Environment Configuration & Database
- **SQLite (Default)**: Local development database (`geopulse.db`) is automatically initialized upon startup with demo sample data.
- **PostgreSQL + PostGIS (Optional Production Setup)**:
  - Install PostgreSQL and enable PostGIS extension.
  - Set the `DATABASE_URL` environment variable:
    ```bash
    # PowerShell example:
    $env:DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/geopulse"
    ```
- **Emulator Networking**: If running the Android emulator and FastAPI on the same machine, connect via `http://10.0.2.2:8000`. For physical devices, use your computer's local network IP address.
