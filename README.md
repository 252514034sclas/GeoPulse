# 🌿 GeoPulse — Agricultural & Geospatial Intelligence Platform

[![Android - Kotlin & Compose](https://img.shields.io/badge/Android-Kotlin%20%26%20Jetpack%20Compose-3DDC84?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Backend - FastAPI & SQLAlchemy](https://img.shields.io/badge/Backend-FastAPI%20%26%20SQLAlchemy-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![Database - PostgreSQL & PostGIS](https://img.shields.io/badge/Database-PostgreSQL%20%26%20PostGIS-4169E1?logo=postgresql&logoColor=white)](https://postgis.net/)
[![License - MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

GeoPulse is a production-grade, full-stack agricultural intelligence solution combining a high-performance **Android application** (Kotlin & Jetpack Compose with a cinematic climate-tech glassmorphic UI) and a robust **Python FastAPI backend** with SQLAlchemy, GeoAlchemy2, and Shapely spatial support.

---

## 🚀 Core Features

- **30-Screen Agricultural Suite**: Comprehensive workflows spanning authentication, real-time dashboards, NDVI vegetation health, soil moisture telemetry, weather radar, pest detection, yield analytics, equipment tracking, and farm management.
- **Cinematic Climate-Tech UI**: Designed with a high-end dark/light glassmorphic aesthetic, custom radial ambient lighting meshes, smooth spring animations, and crisp typography.
- **Decoupled Floating Navigation Dock**: A modern, floating bottom navigation overlay that stays anchored independently of scrolling list views.
- **Advanced Spatial Intelligence**: Built-in WKT Polygon boundary mapping and Shapely point-in-polygon spatial querying, fully compatible with PostGIS geospatial databases.
- **Dark & Light Mode Toggle**: Instant runtime switching between Dark Mode and Light Mode preferences.

---

## 🛠️ Technology Stack

### Mobile Client (`/app`)
- **Language**: Kotlin 2.2+
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: Modern declarative UI with state preservation, decoupled navigation, and API-safe surface styling.

### Backend Intelligence Service (`/backend`)
- **Framework**: Python FastAPI (Async-ready, high performance)
- **ORM**: SQLAlchemy 2.0
- **Spatial Engine**: GeoAlchemy2 & Shapely (Point-in-polygon spatial queries)
- **Migrations**: Alembic
- **Database**: SQLite (local development default) with full PostgreSQL + PostGIS production support.

---

## 📂 Project Structure

```text
GeoPulse/
├── app/                  # Android Application (Kotlin / Jetpack Compose)
│   ├── src/main/java/com/geopulse/app/
│   │   ├── ui/theme/     # Color tokens, Theme, and ThemePreferences
│   │   ├── ui/premium/   # Premium HomeScreen and Ag-Tech Components
│   │   └── *.kt          # 30 Screen Activities (Splash through Screen30)
│   └── build.gradle.kts
├── backend/              # Python FastAPI Backend
│   ├── alembic/          # Database migrations
│   ├── database.py       # SQLAlchemy database connection
│   ├── main.py           # FastAPI app & seed endpoints
│   ├── models.py         # ORM models (Fields, Alerts, Weather, Soil)
│   ├── schemas.py        # Pydantic validation schemas
│   ├── requirements.txt  # Python dependencies
│   ├── test_api.py       # API test suite
│   └── verify_postgres.py# PostGIS verification script
└── README.md
```

---

## 💻 Quick Start & Home-Laptop Setup Guide

Follow these instructions to continue development on your home laptop:

### 1. Prerequisites
- [Git](https://git-scm.com/)
- [Android Studio](https://developer.android.com/studio) (Ladybug / Jellyfish or newer with SDK 34+)
- [Python 3.10+](https://www.python.org/)

### 2. Clone the Repository
```bash
git clone https://github.com/252514034sclas/GeoPulse.git
cd GeoPulse
```

### 3. Build & Run the Android App
1. Open Android Studio and select **Open** -> choose the `GeoPulse` root folder.
2. Wait for Gradle sync to complete.
3. Launch an Android Emulator (API 34+ recommended).
4. Click **Run 'app'** (`Shift + F10`).

### 4. Run the Python FastAPI Backend
```bash
cd backend

# Create and activate virtual environment
python -m venv venv
# Windows PowerShell:
.\venv\Scripts\Activate.ps1
# macOS / Linux:
# source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the API server
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```
- Access Interactive API Docs: [http://localhost:8000/docs](http://localhost:8000/docs)
- Run API Test Suite: `python test_api.py`

---

## 🌐 API Endpoints Overview

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/health` | API health check & version info |
| `GET` | `/api/v1/dashboard` | Unified summary (fields, alerts, soil moisture, weather) |
| `GET` | `/api/v1/fields` | List all agricultural fields and geometries |
| `GET` | `/api/v1/fields/spatial-query` | Spatial point-in-polygon containment query (`?lat=&lon=`) |
| `GET` | `/api/v1/alerts` | Active farm alert notifications |
| `GET` | `/api/v1/vegetation-health`| NDVI vegetation index telemetry |
| `GET` | `/api/v1/weather` | Meteorological conditions |
| `GET` | `/api/v1/soil-moisture` | Sub-surface soil moisture sensor readings |

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.
