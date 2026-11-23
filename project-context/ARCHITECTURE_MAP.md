# GEOPULSE — ARCHITECTURE MAP
**Last Updated:** November 23, 2026  
**Repository:** `252514034sclas/GeoPulse`  

---

## 1. ANDROID CLIENT ARCHITECTURE

### A. Screen & Activity Catalog (All 30 Activities)
All activities are located in `app/src/main/java/com/geopulse/app/`:

| Screen # | Activity Name | Title & Functionality | Upstream / Downstream Navigation |
|---|---|---|---|
| **0** | `MainActivity.kt` | Authenticated router & entry point | Inspects `TARGET_SCREEN` extra; launches target activity or `SplashActivity` |
| **1** | `SplashActivity.kt` | Official branding & app initialization | Directs to `WelcomeActivity` |
| **2** | `WelcomeActivity.kt` | Brand introduction & entry actions | "Get Started" -> `OnboardingActivity`; "Sign In" -> `LoginActivity` |
| **3** | `OnboardingActivity.kt` | 3-slide value carousel (Satellite, Moisture, Advisory) | "Continue" -> `CreateAccountActivity` |
| **4** | `LoginActivity.kt` | Email & password authentication | "Sign In" -> `Screen7Activity`; "Create Account" -> `CreateAccountActivity` |
| **5** | `CreateAccountActivity.kt` | User registration & farm acreage setup | "Create Account" -> `Screen7Activity` |
| **6** | `ForgotPasswordActivity.kt` | Password reset & email instructions | "Back to Sign In" -> `LoginActivity` |
| **7** | `Screen7Activity.kt` | Main Farm Dashboard with MapLibre satellite preview | Top bar notification bell (dialog overlay); Bottom nav: Home, Map (`Screen8`), Activity (`Screen18`), Profile (`Screen29`) |
| **8** | `Screen8Activity.kt` | Full Geospatial Field Map & GIS Layer Manager | Map overlay actions: Vegetation (`Screen11`), Boundary (`Screen17`) |
| **9** | `Screen9Activity.kt` | Map Layer & GIS Spectral Selector | Switches NDVI, NDMI, NDRE raster layers on map |
| **10** | `Screen10Activity.kt` | Field Parcel Selection & Multi-Field Switcher | Parcel selector modal; navigates to selected field details |
| **11** | `Screen11Activity.kt` | Crop Vigor & NDVI Remote Sensing Inspection | Detailed vegetative vigor analysis & SPAD chlorophyll |
| **12** | `Screen12Activity.kt` | Soil Moisture & Water Band (NDMI) Analysis | Topsoil moisture & canopy hydration evaluation |
| **13** | `Screen13Activity.kt` | FAO-56 Safe Watering & Evaporation Guide | Actionable watering depths (mm) & volumes (Litres/m³); links to `Screen14` & `Screen12` |
| **14** | `Screen14Activity.kt` | Weather & Hyperlocal Safe Spraying Forecast | Chemical drift & spray safety windows; links to `Screen15` & `Screen13` |
| **15** | `Screen15Activity.kt` | Pest & Disease Risk Advisory | Insect scouting & pathogen prevention |
| **16** | `Screen16Activity.kt` | Yield Estimator & Harvest Planner | Historical biomass yield projection |
| **17** | `Screen17Activity.kt` | Field Boundary Mapping & Land Area Measurement | Geodesic acreage calculation; links to `Screen23` & `Screen8` |
| **18** | `Screen18Activity.kt` | Crop Calendar & Phenology Stage Tracker | Growth phase monitoring & sowing timelines |
| **19** | `Screen19Activity.kt` | Soil Health & Organic Composting Guide | Soil conservation, bio-fertilizers & organic matter |
| **20** | `Screen20Activity.kt` | Fertilizer & Nutrient Guide (Urea, DAP, MOP) | Tailored N-P-K dosages, split timing & soil texture leaching advice |
| **21** | `Screen21Activity.kt` | Farm Machinery & Equipment Maintenance Ledger | Tractor & implement operating hours and service logs |
| **22** | `Screen22Activity.kt` | Farm Labor & Field Work Tracking | Worker task allocations & daily wage tracking |
| **23** | `Screen23Activity.kt` | Multi-Field Parcel Management & Overview | Master list of all registered farm parcels |
| **24** | `Screen24Activity.kt` | Scouting Journal & Geotagged Field Notes | Agronomic photo scouting & problem quadrant notes |
| **25** | `Screen25Activity.kt` | Farm Budget & Operational Expense Ledger | Expense categorization (Seeds, Fertilizer, Fuel, Labor) & Net Profit |
| **26** | `Screen26Activity.kt` | Agronomist Advisory & Expert Support Chat | Live expert guidance & extension advice |
| **27** | `Screen27Activity.kt` | Wholesale Mandi Market Commodity Rates | Real-time APMC arrivals & price trends per quintal |
| **28** | `Screen28Activity.kt` | Weather & Crop Warning Center / Notification Center | Real-time weather warnings, unread indicators, mark-as-read sync |
| **29** | `Screen29Activity.kt` | Settings, Language Preferences & Offline Sync | English/Tamil language selector, dark/light theme, network sync status |
| **30** | `Screen30Activity.kt` | Data Export, Cloud Backup & Privacy Controls | Parcel GeoJSON export, CSV ledger download, account deletion |

---

### B. Core Compose Components
- `InteractiveSatelliteFarmMap.kt`: Host composable for MapLibre Native SDK (`MapView`). Manages raster tile layers, vector polygon boundary overlays, GPS user location marker (`LocationManager`), GIS layer switching, zoom controls, and compass bearing.
- `GeoComponents.kt`: Reusable design system elements: `GeoTopBar`, `GeoMetricCard`, `GeoDetailCard`, `GeoActionCard`, `GeoNotificationDialog`, `GeoFloatingButton`.
- `FullGisMapTab.kt`: Full-screen map tab with floating GIS layer selection pills.
- `GeoTheme.kt` & `ThemePreferences.kt`: Material 3 light/dark theme system with persistent SharedPreferences state.
- `NotificationRepository.kt`: SQLite Room / Local state management for in-place notification dialogs and live badge count synchronization.

---

### C. Android Data & Networking Layer
- `ApiClient.kt`: Retrofit 2 singleton with OkHttp connection pooling, 6s read/write timeouts, and dynamic endpoint configuration (`http://172.23.176.200:8000/` or `http://10.0.2.2:8000/`).
- `GeoPulseApiService.kt`: Declarative Retrofit interface defining all HTTP operations.
- `GeoPulseRepository.kt`: Coroutine-based repository dispatching network calls on `Dispatchers.IO` with resilient offline fallbacks.
- `NetworkModels.kt`: Strongly typed DTO data classes with Gson `@SerializedName` annotations (`FieldDto`, `WeatherDto`, `IrrigationDto`, `SoilMoistureDto`, `SatelliteRecordDto`, `MandiPriceDto`, `DashboardSummaryDto`).

---

## 2. PYTHON FASTAPI BACKEND ARCHITECTURE

### A. Core Services & Adapters

```
backend/
├── main.py                     # Primary FastAPI application and route controllers
├── models.py                   # SQLAlchemy ORM entity definitions
├── schemas.py                  # Pydantic v2 validation and serialization schemas
├── database.py                 # SQLite database connection pool and session generator
├── adapters/
│   ├── open_meteo_adapter.py   # High-resolution NWP weather & root-zone soil physics
│   ├── copernicus_adapter.py   # ESA Copernicus Sentinel-2 MSI L2A STAC query
│   ├── isro_bhoonidhi_adapter.py # ISRO / NRSC Bhoonidhi Indian EO STAC search
│   ├── agmarknet_adapter.py    # Official APMC wholesale commodity mandi prices
│   └── nasa_power_adapter.py   # Solar radiation & agroclimatology GDD
└── services/
    ├── irrigation_engine.py    # FAO-56 Penman-Monteith crop water balance & hydraulic limits
    ├── satellite_engine.py     # NDVI, EVI, NDMI (Canopy Moisture), NDRE (Chlorophyll)
    ├── weather_engine.py       # Agronomic spray safety windows
    ├── fertilizer_calculator.py# ICAR/TNAU NPK dosages & soil texture leaching adjustments
    ├── geospatial_engine.py    # Shapely geodesic polygon area & centroid calculation
    ├── seed_sowing_engine.py   # Certified seed rate & planting geometry
    ├── raster_processing_service.py # Sentinel-2C COG tile clipping & SCL cloud masking
    ├── multi_source_satellite_service.py # Dual Copernicus + ISRO Bhoonidhi evaluation
    ├── provider_registry.py    # Quality ledger for 8 integrated agricultural providers
    ├── agri_knowledge_service.py # Official TNAU & ICAR packages of practices
    └── auth_service.py         # OAuth2 Bearer JWT issuance & bcrypt verification
```

---

### B. Backend REST API Endpoints

| Category | HTTP Method | Route Path | Downstream Engine / Adapter | Purpose |
|---|---|---|---|---|
| **System** | `GET` | `/health` | Core | System health check and uptime status |
| **Auth** | `POST` | `/api/v1/auth/register` | `auth_service.py` | Create farmer account with bcrypt hash |
| **Auth** | `POST` | `/api/v1/auth/login` | `auth_service.py` | Validate credentials, issue Bearer JWT |
| **Auth** | `GET` | `/api/v1/auth/me` | `auth_service.py` | Retrieve authenticated farmer profile |
| **Auth** | `PUT` | `/api/v1/auth/me` | `auth_service.py` | Update farmer profile and preferences |
| **Dashboard** | `GET` | `/api/v1/dashboard` | `open_meteo_adapter`, `models` | Live dashboard summary for user's GPS/fields |
| **Fields** | `GET` | `/api/v1/fields` | `models.FieldModel` | List all registered parcels |
| **Fields** | `POST` | `/api/v1/fields` | `models.FieldModel` | Register a new farm field |
| **Fields** | `GET` | `/api/v1/fields/geojson` | `models.FieldModel` | GeoJSON FeatureCollection of field boundaries |
| **Fields** | `GET` | `/api/v1/fields/{id}` | `models.FieldModel` | Get field parcel by ID |
| **Fields** | `PUT` | `/api/v1/fields/{id}` | `models.FieldModel` | Update field properties and boundary |
| **Fields** | `DELETE` | `/api/v1/fields/{id}` | `models.FieldModel` | Delete field parcel |
| **Fields** | `POST` | `/api/v1/fields/draw` | `geospatial_engine.py` | Register drawn polygon boundary from map |
| **Fields** | `POST` | `/api/v1/fields/spatial-query` | `geospatial_engine.py` | Point-in-polygon containment check |
| **Satellite** | `GET` | `/api/v1/satellite/health/{id}` | `copernicus_adapter.py` | Live Sentinel-2 NDVI calculation for field |
| **Satellite** | `GET` | `/api/v1/satellite/moisture/{id}` | `open_meteo_adapter.py` | Live root-zone soil moisture for field |
| **Satellite** | `GET` | `/api/v1/satellite/history/{id}` | `copernicus_adapter.py` | Multi-temporal 90-day STAC history for field |
| **Satellite** | `GET` | `/api/v1/satellite/multi-source/{id}` | `multi_source_satellite_service.py` | Dual ISRO Bhoonidhi + Copernicus evaluation |
| **Satellite** | `GET` | `/api/v1/satellite/bhoonidhi/collections` | `isro_bhoonidhi_adapter.py` | List 63 Indian EO satellite collections |
| **Satellite** | `POST` | `/api/v1/satellite/bhoonidhi/search` | `isro_bhoonidhi_adapter.py` | Geographic search for Indian satellite products |
| **Satellite** | `POST` | `/api/v1/satellite/bhoonidhi/auth-test` | `isro_bhoonidhi_adapter.py` | Verify ISRO Bhoonidhi authentication status |
| **Hydrology** | `GET` | `/api/v1/fields/{id}/irrigation` | `irrigation_engine.py` | FAO-56 crop water demand, depth (mm) & volume (L) |
| **Agronomy** | `GET` | `/api/v1/fields/{id}/sowing-recommendation` | `seed_sowing_engine.py` | Certified variety, geometry & seed rate (kg/ha) |
| **Weather** | `GET` | `/api/v1/weather/current` | `open_meteo_adapter.py` | Live weather observation for device GPS |
| **Weather** | `GET` | `/api/v1/weather/spray-window` | `weather_engine.py` | Evaluate safe chemical spraying window |
| **Weather** | `GET` | `/api/v1/weather/live` | `open_meteo_adapter.py` | Live numerical weather prediction |
| **Agronomy** | `POST` | `/api/v1/fertilizer/calculate` | `fertilizer_calculator.py` | NPK dosages, sandy soil split & safety caps |
| **Agronomy** | `GET` | `/api/v1/knowledge/crop-advisory` | `agri_knowledge_service.py` | Official TNAU & ICAR packages of practices |
| **Market** | `GET` | `/api/v1/mandi/rates` | `models.MandiPriceModel` | APMC mandi market commodity rates |
| **Market** | `GET` | `/api/v1/mandi/prices` | `agmarknet_adapter.py` | Live AGMARKNET mandi rates by commodity/district |
| **Budget** | `GET` | `/api/v1/budget/summary` | `models.FarmExpenseModel` | Operational expense ledger & net profit |
| **Budget** | `POST` | `/api/v1/budget/expenses` | `models.FarmExpenseModel` | Add operational farm expense |
| **Advisory** | `GET` | `/api/v1/advisory/tasks` | `models.AdvisoryTaskModel` | List active field advisory tasks |
| **Advisory** | `POST` | `/api/v1/advisory/tasks/{id}/complete` | `models.AdvisoryTaskModel` | Mark advisory task complete |
| **Registry** | `GET` | `/api/v1/providers/registry` | `provider_registry.py` | Quality status for all 8 integrated providers |

---

## 3. DATABASE SCHEMA & MODELS

All models inherit from `database.Base` in `backend/models.py`:

```
                 +-------------------+
                 |    UserModel      |
                 |-------------------|
                 | id (PK)           |
                 | email (Unique)    |
                 | full_name         |
                 | hashed_password   |
                 | farm_name         |
                 | preferred_lang    |
                 +---------+---------+
                           |
        +------------------+------------------+
        | 1:N                                 | 1:N
+-------v-----------+             +-----------v-----------+
|    FieldModel     |             |   AdvisoryTaskModel   |
|-------------------|             |-----------------------|
| id (PK)           |             | id (PK)               |
| user_id (FK)      |             | user_id (FK)          |
| name              |             | field_id (FK)         |
| crop_type         |             | task_type             |
| area_acres        |             | title, description    |
| latitude          |             | priority, is_complete |
| longitude         |             +-----------------------+
| boundary (WKT)    |
+-------+-----------+
        |
        +------------------+------------------+
        | 1:N                                 | 1:N
+-------v-------------------+     +-----------v-----------+
|   SatelliteRecordModel    |     |      AlertModel       |
|---------------------------|     |-----------------------|
| id (PK)                   |     | id (PK)               |
| field_id (FK)             |     | field_id (FK)         |
| recorded_date             |     | severity, message     |
| ndvi_value                |     | category, timestamp   |
| ndwi_value                |     | is_resolved           |
| chlorophyll_spad          |     +-----------------------+
| health_status             |
+---------------------------+
```
