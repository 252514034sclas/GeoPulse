# GEOPULSE — PROJECT MASTER CONTEXT
**Last Updated:** November 23, 2026  
**Repository:** `252514034sclas/GeoPulse`  
**Package:** `com.geopulse.app`  
**Target Platform:** Native Android (Kotlin + Jetpack Compose) + Python (FastAPI) Geospatial Backend  
**Current Git Branch:** `main`  
**Current Git Commit:** `e0b1ead`  

---

## 1. PROJECT PURPOSE & OBJECTIVES

GeoPulse is a high-reliability, hardware-free, precision agricultural intelligence and geospatial analytics platform. It delivers actionable, scientific crop monitoring to smallholder and commercial farmers across India without requiring costly on-ground IoT sensors, soil probes, or drone hardware.

### Core Capabilities:
1. **Multi-Source Satellite Remote Sensing:** Ingests surface reflectance imagery from European Space Agency (ESA) Copernicus Sentinel-2C (MSI Level-2A) and Indian Space Research Organisation (ISRO) Bhoonidhi (Resourcesat-2A, EOS-04 SAR) to compute vegetative vigor (NDVI), canopy water absorption (NDMI / Gao NDWI), and dense canopy chlorophyll/nitrogen (NDRE).
2. **FAO-56 Soil Water Balance & Precision Irrigation:** Replaces arbitrary watering rules with standardized FAO Irrigation and Drainage Paper 56 single/dual crop coefficient modeling. Incorporates Table 22 depletion fractions ($p$), climate adjustments ($p_{\text{adj}} = p + 0.04(5 - ET_c)$), Available Water Capacity ($AWC$) across 12 soil textures, Readily Available Water ($RAW$) stress thresholds, and single-cycle hydraulic absorption limits (30mm drip / 35mm sprinkler) with multi-cycle split application scheduling.
3. **Hyperlocal Agrometeorology & Spray Windows:** Ingests Open-Meteo numerical weather prediction (NWP) data to evaluate real-time chemical spraying windows (wind drift velocity, rain wash-off risk, leaf scorch vaporization, thermal inversions).
4. **Fertilizer & Nutrient Advisory:** Calculates balanced Urea, DAP, and MOP dosages based on ICAR and TNAU packages of practices, adjusted for real-time satellite vigor and soil texture leaching risks (4-stage split for sandy soils).
5. **Certified Seed Sowing Engine:** Delivers certified seed variety recommendations (DKC 9108, HD 2967, BPT 5204) with recommended planting geometries, sowing depths, and seed rates (kg/ha and kg/acre).
6. **APMC Mandi Market Intelligence:** Integrates AGMARKNET / data.gov.in wholesale arrival prices across Indian commodity markets.
7. **Farmer-Centric Native Android Client:** 30 complete, integrated Jetpack Compose screens featuring MapLibre Native SDK interactive farm mapping, in-place notification dialogs, real-time unread badge counts, day/night theme switching, and English/Tamil bilingual localization.

---

## 2. TECHNOLOGY STACK & ENVIRONMENT

### A. Android Mobile Client
- **Programming Language:** Kotlin 2.0+
- **UI Toolkit:** Jetpack Compose (BOM 2024.04.01+) with Material 3 design system
- **Mapping SDK:** MapLibre Native Android SDK `11.11.0` (`org.maplibre.gl:android-sdk:11.11.0`)
- **Networking:** Retrofit `2.11.0`, OkHttp `4.12.0` with HttpLoggingInterceptor, Gson
- **Location:** Android Location SDK (`LocationManager`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
- **Android Target Configuration:**
  - `namespace`: `com.geopulse.app`
  - `compileSdk`: 37
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `targetSdk`: 37
  - `sourceCompatibility` / `targetCompatibility`: Java 11 / Java 17 toolchain
- **Build System:** Gradle 8.x with Android Gradle Plugin 8.7.x
- **Build Command:** `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleDebug`

### B. Python Geospatial Backend
- **Framework:** FastAPI `0.100+` (ASGI via Uvicorn)
- **Runtime:** Python 3.11 (`backend/venv/Scripts/python.exe`)
- **ORM & Database:** SQLAlchemy `2.1+` with native PostgreSQL 18.7 (`127.0.0.1:5432/geopulse_db`) via `psycopg2-binary` (v2.9.10)
- **Database Migrations:** Alembic versioned migrations (`backend/alembic/`)
- **Connection Pooling:** Pool size 10, max overflow 20, pre-ping enabled, strict fail-fast enforcement (zero silent fallback to SQLite)
- **Geospatial Processing:** Shapely `2.0+` (geodesic polygon parsing, point-in-polygon containment, WKT serialization, centroid calculation), NumPy, Tifffile (Sentinel-2 COG tile deflated raster decoding)
- **Security & Auth:** OAuth2 Bearer JWT (`python-jose`), PBKDF2 600,000-iteration password hashing, sliding-window rate limiting
- **HTTP Client:** HTTPX `0.24+` with connection pooling and timeouts
- **Backend Host:** `0.0.0.0:8000` (LAN IP: `172.23.176.200:8000`, Emulator: `10.0.2.2:8000` or `127.0.0.1:8000` via ADB reverse)

---

## 3. NON-NEGOTIABLE ARCHITECTURAL RULES

1. **No Synthetic or Mock Data in Production Workflows:** Every endpoint must connect to live external APIs (Copernicus STAC, ISRO Bhoonidhi, Open-Meteo, AGMARKNET) or real user-registered fields. No hardcoded mock responses or synthetic fallback values masquerading as real telemetry.
2. **Scientific Validity & Remote Sensing Transparency:**
   - Optical SWIR (Sentinel-2 Band 11 / 1.6 µm) reflects leaf canopy liquid water content and surface skin moisture; it does NOT measure 30cm root-zone soil moisture directly.
   - Root-zone soil moisture must be hydrologically modeled using FAO-56 Soil Water Balance or verified via C-band SAR dielectric backscatter models.
   - Weather forecasts are probabilistic (PoP); rain must never be treated as guaranteed water.
   - Depletion fractions must obey FAO-56 Table 22 and be climate-adjusted.
   - Net crop irrigation demand and gross pumped volume ($1\text{ mm} \times 1\text{ ha} = 10,000\text{ L}$) must be reported distinctly.
3. **Security by Design:**
   - All internal Android activities must have `android:exported="false"`. External navigation must route strictly through `MainActivity`'s authenticated router.
   - User database records must be filtered by authenticated `user_id`.
4. **Preserve All 30 Screens & Localization:**
   - Do not delete or consolidate screens into generic placeholders.
   - Preserve English (`values/strings.xml`) and Tamil (`values-ta/strings.xml`) resources.
5. **Incremental Context Maintenance:**
   - Update only affected documentation after code changes.
   - Never claim an unexecuted test passed.
