# GEOPULSE — RC-1 BASELINE FREEZE & RELEASE PRESERVATION RECORD

**Freeze Date:** November 24, 2026  
**Application:** GeoPulse  
**Package:** `com.geopulse.app`  
**Git Baseline Commit:** `2083d56afe126cc6d5182013f7c388b1cd5599ac`  
**Git Protected Release Tag:** `v1.0.0-rc1`  
**Operational Status:** **READY FOR RC-1 TESTING**  
**Priority:** Stability, Data Integrity, Security, and Preservation of Existing Functionality  

---

## 1. Executive Summary & Freeze Policy

This document establishes the official frozen baseline for **GeoPulse Release Candidate 1 (RC-1)**. 

### Preservation Directives:
1. **Design & Architecture Preservation:** All 30 Jetpack Compose screens, Figma layouts, navigation flows, and domain engines are frozen. No screens may be redesigned, removed, or replaced with generic placeholders.
2. **Database Schema Stability:** The native PostgreSQL 18.7 schema is locked at Alembic revision `34cf2b656a5e`. No schema changes are permitted without documented critical justification and an accompanying versioned migration.
3. **External Provider Integrity:** All genuine external integrations (ESA Copernicus STAC, ISRO Bhoonidhi, Open-Meteo, NASA POWER, TNAU/ICAR, AGMARKNET) are locked. Zero dummy data, synthetic fallback data, or mock responses may be introduced.
4. **Defect Threshold:** Only reproducible critical (P0) or high-severity (P1) defects may be modified. No unnecessary features or speculative refactorings are permitted.
5. **Data & Credential Protection:** All user accounts, parcel boundaries, telemetry records, release signing credentials, and environment configurations must be strictly preserved.
6. **Accurate Feature Labelling:** All unfinished, deferred, or partially implemented capabilities must remain explicitly and transparently disclosed in the UI and documentation.
7. **Production Boundary:** Production readiness must NOT be declared until the public HTTPS cloud backend (`https://api.geopulse.app/`), managed cloud PostgreSQL with SSL, automated backups, and production Android connectivity have been deployed and empirically validated.

---

## 2. Release Artifacts & Cryptographic Baseline

### 2.1 Release APK Identification
- **Build Variant:** Signed Release APK (`assembleRelease`)
- **APK Path:** `app/build/outputs/apk/release/app-release.apk`
- **File Size:** `70,787,669 bytes` (67.5 MB)
- **APK Checksums:**
  - **SHA-256:** `E5032AB83707A2B3B599A5BFC1814B7842B3AADDF76F3872735CDE182AF20FEB`
  - **MD5:** `2D6C9F0467B2D8E2E1A3E38375C9C6F2`

### 2.2 Signing Certificate Fingerprint & Verification
Verified via `apksigner verify --verbose --print-certs`:
- **Verification Result:** Passed (`Verifies: true`)
- **Signature Schemes:** APK Signature Scheme v2 (`true`), v1 (`false`), v3 (`false`), v4 (`false`)
- **Certificate DN:** `CN=GeoPulse Production Release, OU=Agricultural Engineering, O=GeoPulse Technologies, L=Chennai, ST=Tamil Nadu, C=IN`
- **Certificate SHA-256 Digest:** `464070a93c24bfd9bf67f4b6e9f777cf470aec4009ea3c710c3d8e8f849f8143`
- **Certificate SHA-1 Digest:** `ca65756e62bfb98fbcad73b1d921c5fd1b4184e1`
- **Certificate MD5 Digest:** `db6c588f8d2c83d4c476da180d9dc93a`
- **Public Key SHA-256 Digest:** `09926fa6ab32d37063de1625d54f77e99e69d3c8b9c8e63237b17c0f507b0010`
- **Key Algorithm:** RSA 2048-bit
- **Credential Storage:** Stored in git-ignored `release-keystore.jks`; credentials loaded strictly from environment variables or git-ignored `keystore.properties`. Zero secrets in tracked git history.

---

## 3. Database Engine & Data Integrity Baseline

### 3.1 Database Architecture
- **Engine:** Native PostgreSQL 18.7 x86_64-windows
- **Connection URL:** Environment-configured via `DATABASE_URL` (strict fail-fast; zero silent SQLite fallback permitted)
- **Driver:** `psycopg2-binary` (v2.9.10) with SQLAlchemy 2.1+
- **Connection Pool:** `pool_size=10`, `max_overflow=20`, `pool_pre_ping=True`
- **Alembic Head Revision:** `34cf2b656a5e` (`init_postgres_schema.py`)
- **Production Tables (11):** `users`, `fields`, `soil_moisture`, `mandi_prices`, `satellite_records`, `advisory_tasks`, `farm_expenses`, `weather`, `alembic_version`, plus associated indexes and sequences.

### 3.2 Migrated Data Fidelity & Records
- **Total Records Migrated:** 118 records across 8 data tables
- **Total Fields Compared:** 1,156 individual column values evaluated field-by-field
- **Unexpected Discrepancies:** **0 (100% Perfect Match)**
- **Runtime Variance:** Accounted for by real-time Open-Meteo NWP weather updates and PBKDF2 password verification during login.

### 3.3 Relational Constraints & Concurrency
- **Foreign Keys:** Enforced on `fields.user_id -> users.id` and `satellite_records.field_id -> fields.id` (verified with `ForeignKeyViolation`).
- **Unique Constraints:** Enforced on `users.email` (verified with `UniqueViolation`).
- **Sequences:** Auto-increment serial sequences synchronized across all tables (`users_id_seq`, `fields_id_seq`, etc.).
- **ACID Rollback:** Verified via uncommitted transaction exception testing; discarded cleanly.
- **Concurrency:** 5 parallel worker threads executed 50 concurrent CRUD transactions with **0 deadlocks, 0 timeouts, and 0 errors**.
- **Clean Migration:** Verified by building `geopulse_clean_mig_test` database from scratch with `alembic upgrade head`.
- **Zero SQLite Runtime Dependency:** Physically verified by isolating `geopulse.db`; live server executed all REST queries solely against PostgreSQL.

---

## 4. Frontend–Backend Screen Architecture Baseline

All 30 Figma-designed screens are fully implemented in Jetpack Compose:

| Screen # | Activity | Implementation Status | Functional Description | Verified Data Flow |
|---|---|---|---|---|
| **0** | `MainActivity.kt` | Operational | App Entry & Route Dispatcher | Initializes `ApiClient.initFromContext()`; routes to authenticated screen |
| **1** | `SplashActivity.kt` | Operational | Brand Splash & Health Probe | Verifies backend connectivity via `GET /health` |
| **2** | `WelcomeActivity.kt` | Operational | Welcome & Gateway Screen | Navigation to Login or Registration |
| **3** | `OnboardingActivity.kt` | Operational | Farmer Feature Walkthrough | 3-page interactive feature onboarding carousel |
| **4** | `LoginActivity.kt` | Operational | Secure Farmer Login | `POST /api/v1/auth/login` issuing Bearer JWT |
| **5** | `CreateAccountActivity.kt` | Operational | Farmer Account Registration | `POST /api/v1/auth/register` with server-side validation |
| **6** | `ForgotPasswordActivity.kt` | Operational | Transparent Password Recovery | Displays notice that automated SMTP is unconfigured; gives admin steps |
| **7** | `Screen7Activity.kt` | Operational | Main Farm Dashboard | `GET /api/v1/dashboard`; dynamic in-app notification dialog & unread badge |
| **8** | `Screen8Activity.kt` | Operational | Fullscreen GIS Map Engine | MapLibre Native SDK rendering parcel polygon layers via GeoJSON |
| **9** | `Screen9Activity.kt` | Operational | Spectral Band Knowledge Guide | Optical Sentinel-2 spectral band explanations (B2, B4, B8, B11) |
| **10** | `Screen10Activity.kt` | Operational | Multi-Parcel Switcher Modal | `GET /api/v1/fields` allowing quick active field switching |
| **11** | `Screen11Activity.kt` | Operational | Crop Vigor & NDVI Analysis | Sentinel-2 L2A surface reflectance via `GET /api/v1/satellite/health/{id}` |
| **12** | `Screen12Activity.kt` | Operational | Soil Moisture & NDMI Viewer | Open-Meteo & NDWI canopy moisture via `GET /api/v1/satellite/moisture/{id}` |
| **13** | `Screen13Activity.kt` | Operational | Precision Irrigation Advisory | FAO-56 daily water balance via `GET /api/v1/fields/{id}/irrigation` |
| **14** | `Screen14Activity.kt` | Operational | Agrometeorology & Spray Window | `GET /api/v1/weather/current` and `spray-window` (wind, rain, temp) |
| **15** | `Screen15Activity.kt` | Operational | Pest & Disease Protection Guide | TNAU / ICAR authoritative integrated pest management (IPM) guidelines |
| **16** | `Screen16Activity.kt` | Operational | Yield Estimator & Crop Planner | Biomass accumulation model via `GET /api/v1/satellite/health/{id}` |
| **17** | `Screen17Activity.kt` | Operational | GPS Polygon Boundary Tracing | `POST /api/v1/fields/draw` saving parcel coordinates to PostgreSQL |
| **18** | `Screen18Activity.kt` | Operational | Crop Calendar & Phenology Tracker| Dynamic DAS growth stages via `GET /api/v1/fields/{id}/sowing-recommendation`|
| **19** | `Screen19Activity.kt` | Operational | Organic Soil Health & Composting | Authoritative agronomic guide for composting and bio-fertilizers |
| **20** | `Screen20Activity.kt` | Operational | Balanced N-P-K Fertilizer Calculator| `POST /api/v1/fertilizer/calculate` with split application schedules |
| **21** | `Screen21Activity.kt` | Operational | Harvest Prep & Safe Grain Storage | Post-harvest moisture management; links to Mandi Rates & Yield Estimator |
| **22** | `Screen22Activity.kt` | Operational | Farm Labor & Field Work Tracking | Worker allocations & task completion via `POST /api/v1/advisory/tasks/{id}/complete`|
| **23** | `Screen23Activity.kt` | Operational | Multi-Field Parcel Master Overview | Master list of all user-owned parcels via `GET /api/v1/fields` |
| **24** | `Screen24Activity.kt` | Operational | Satellite Growth History | Bi-weekly multi-temporal greenness timeline linking to Vigor & Calendar |
| **25** | `Screen25Activity.kt` | Operational | Farm Operational Expense Ledger | Categorized expenses (Seeds, Labor, Fuel) via `GET /api/v1/budget/summary` |
| **26** | `Screen26Activity.kt` | Operational | Kisan Helpline & Agronomist Support| Direct telephony bridge to Kisan Call Center (1800-180-1551) |
| **27** | `Screen27Activity.kt` | Operational | Wholesale Mandi Commodity Rates | AGMARKNET benchmark wholesale rates via `GET /api/v1/mandi/rates` |
| **28** | `Screen28Activity.kt` | Operational | Weather & Crop Warning Center | Severe weather warning hub linking to 7-Day Weather & Irrigation |
| **29** | `Screen29Activity.kt` | Operational | Settings & Bilingual Preferences | English / Tamil language switching, theme selection, and sync status |
| **30** | `Screen30Activity.kt` | Operational | Data Export & Privacy Controls | Parcel GeoJSON export, CSV ledger download, and session clearing |

---

## 5. Verification Suite & Test Results Baseline

Every test suite was executed against the RC-1 baseline and confirmed passing:

| Test Suite | Purpose | Tests / Scope | Result | Status |
|---|---|---|---|---|
| `test_api.py` | Full REST API domain endpoints | 24 domain tests | 24 / 24 PASS | **100% PASS** |
| `test_multi_user_isolation.py` | Multi-user anti-IDOR security | 19 isolation checks | 19 / 19 PASS | **100% PASS** |
| `test_scientific_rigor.py` | Soil physics, FAO-56, NRCS CN | 12 calculation tests | 12 / 12 PASS | **100% PASS** |
| `compare_sqlite_postgres_field_level.py` | Database migration data parity | 1,156 fields across 118 rows | 0 discrepancies | **100% PASS** |
| `test_postgres_advanced_integrity.py` | FKs, ACID, Concurrency, Clean Migrations | 4 comprehensive phases | All phases passed | **100% PASS** |
| `verify_live_postgres_isolation.py` | Live PostgreSQL multi-tenant isolation | 11 live REST checks | 11 / 11 PASS | **100% PASS** |
| `test_zero_sqlite_runtime_dependency.py`| Zero SQLite runtime isolation | Live API with DB isolated | 0 SQLite touches | **100% PASS** |
| Real Device / Emulator Regression | Pixel 10 Pro (emulator-5554, API 35) | Full interactive user flows | 0 crashes | **100% PASS** |

---

## 6. Known Limitations & Transparent Boundary Disclosures

In accordance with GeoPulse transparency standards, all limitations are accurately documented and never obscured:

1. **Production Public Cloud Hosting:**
   - *Status:* Deferred to production deployment phase.
   - *Detail:* The RC-1 client is validated against the local development bridge (`127.0.0.1:8000` via ADB reverse). Production readiness requires deploying to an HTTPS cloud container with CA-signed TLS (`https://api.geopulse.app/`) and managed cloud PostgreSQL.
2. **Room Database Offline Sync:**
   - *Status:* In development (Post-RC-1).
   - *Detail:* The Android client utilizes `SharedPreferences` for session and notifications, and in-memory caches. Durable offline parcel storage across process termination requires Room DB integration. The UI explicitly states this in `Screen7Activity` and `Screen29Activity`.
3. **Automated Password Reset Email:**
   - *Status:* SMTP unconfigured.
   - *Detail:* The backend anti-enumeration endpoint `/api/v1/auth/forgot-password` is implemented, but production SMTP credentials are not provisioned. `ForgotPasswordActivity` transparently instructs users to contact administrator support.
4. **Sentinel-1 SAR Radar Ingestion Pipeline:**
   - *Status:* Documented as NOT IMPLEMENTED.
   - *Detail:* Synthetic Aperture Radar (SAR) backscatter processing is not active. The application uses optical Sentinel-2 MSI surface reflectance and FAO-56 hydrological water balance modeling.
5. **Mandi Market Data:**
   - *Status:* Benchmark Reference.
   - *Detail:* APMC market prices reflect verified AGMARKNET benchmark data (`2026-11-15`) and are explicitly labeled as `HISTORICAL_BENCHMARK_REFERENCE` (`is_live: False`).
6. **Agronomist Support:**
   - *Status:* Telephony Bridge.
   - *Detail:* `Screen26Activity` provides a direct telephony bridge to the official Kisan Call Center (1800-180-1551). Live WebRTC / AI chat server is not implemented in this release.

---

## 7. Change Control & Preservation Protocol

To maintain the frozen integrity of RC-1:
- **Branch Protection:** Any further commits must occur on branch `main` or specific bugfix branches with regression test verification.
- **Tag Immutability:** Tag `v1.0.0-rc1` represents the immutable milestone commit `2083d56afe126cc6d5182013f7c388b1cd5599ac`.
- **Pre-Commit Regression Gate:** Any approved critical bugfix must run and pass all test suites (`test_api.py`, `test_multi_user_isolation.py`, `test_scientific_rigor.py`, `test_postgres_advanced_integrity.py`) before committing.
- **Zero Mock Policy:** Under no circumstances may a test or mock adapter be substituted into the production codebase.
