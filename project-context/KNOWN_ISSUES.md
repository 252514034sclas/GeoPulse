# GEOPULSE — KNOWN ISSUES & LIMITATIONS
**Last Updated:** November 23, 2026  
**Repository:** `252514034sclas/GeoPulse`  

---

## 1. SCIENTIFIC & PHYSICAL LIMITATIONS

| ID | Domain | Limitation Description | Impact & Severity | Mitigation Strategy |
|---|---|---|---|---|
| **SCI-01** | Remote Sensing | **Monsoon Cloud Occlusion:** Heavy cloud cover (>80% cloud pixels) during active monsoon periods prevents optical Sentinel-2 MSI surface reflectance observation. | High (Seasonal) | The backend incorporates SCL (Scene Classification Layer) cloud masking to automatically filter corrupted pixels. When optical cloud interference exceeds 35%, the system tags the scene as cloudy and falls back to modeled FAO-56 Soil Water Balance calculations. Future upgrade: Sentinel-1 C-band SAR backscatter fusion. |
| **SCI-02** | Remote Sensing | **Optical/SWIR Penetration Depth:** Optical and short-wave infrared (SWIR Band 11 / 1.6 µm) sensors perceive only a microscopic optical skin depth of the foliage canopy and topsoil layer. They cannot penetrate 30–120 cm into the root zone. | Medium | Clarified scientific provenance across all endpoints and UI cards. NDMI is accurately labeled as "Canopy Liquid Water Content Index". Root-zone moisture is hydrologically modeled using FAO-56 Penman-Monteith water balance. |
| **SCI-03** | Hydrology | **Precipitation Forecast Uncertainty:** Meteorological rain forecasts represent numerical model probabilities (PoP), not guaranteed water arrival. Treating forecast rain as certain water causes crop drought if the storm misses the parcel. | High | The irrigation engine implements probabilistic effective rainfall discounting: $P_{\text{prob}} = P_{\text{forecast}} \times (\text{PoP} / 100)$, deducts 4.0 mm initial abstraction ($I_a$), and caps infiltration at available root storage deficit. Advisory explicitly prompts farmers: "Re-evaluate soil moisture 24h after rainfall ceases." |
| **SCI-04** | Soil Physics | **Soil Texture Heterogeneity:** Available Water Capacity (AWC) is generalized across 12 standard USDA/FAO soil classes (e.g., 170 mm/m for loam, 80 mm/m for sand). Real-world sub-field variance and compaction hardpans cannot be detected from orbit. | Low-Medium | Provides soil texture selection parameter across irrigation and fertilizer engines. Recommends local extension soil testing where high compaction is suspected. |

---

## 2. ANDROID CLIENT & SECURITY HARDENING ITEMS

| ID | Component | Finding / Issue | Severity | Status / Recommendation |
|---|---|---|---|---|
| **SEC-01** | `AndroidManifest.xml` | **`android:allowBackup="false"`:** Disabled to prevent unauthorized ADB backup extraction of application data. | Low | **RESOLVED** (Commit `8a07587`). Configured `android:allowBackup="false"` in `AndroidManifest.xml`. |
| **SEC-02** | `network_security_config.xml` | **Production Cleartext Traffic Exception:** Whitelisting development IPs in release build creates security vulnerability. | High (Release) | **RESOLVED** (Post-Audit Task). Created `app/src/debug/res/xml/network_security_config.xml` for debug loopbacks (`10.0.2.2`, `127.0.0.1`), while `src/main` strictly enforces HTTPS `cleartextTrafficPermitted="false"`. |
| **SEC-03** | `ApiClient.kt` | **Workstation LAN / Emulator IP Handling:** Base URL defaults to `http://10.0.2.2:8000/` or local workstation LAN. | Low | `ApiClient.baseUrl` is mutable and dynamically configurable via `Screen29Activity` (Settings) or during application startup. |
| **SEC-04** | `ForgotPasswordActivity` | **Password Reset Backend & Email Delivery:** Password reset UI is client-side mock; backend lacks `/api/v1/auth/forgot-password` and SMTP email delivery service. | Medium | **DOCUMENTED RELEASE BLOCKER**. Recommend adding SMTP/SES integration or OTP SMS pipeline post-MVP. |
| **SEC-05** | `auth_service.py` | **Server-Side Token Revocation / Denylist:** Logout is implemented strictly on client by clearing `SharedPreferences`; backend has no denylist table or `/api/v1/auth/logout`. | Low-Medium | Token has 24h expiration (`ACCESS_TOKEN_EXPIRE_MINUTES = 1440`). Server-side Redis/DB denylist recommended for high-security environments. |
| **SEC-06** | `backend/routers/auth.py` | **API Rate Limiting Middleware:** No request throttling middleware on `/login` or `/register` to prevent brute-force attacks. | Medium | Pydantic schema validation is enforced; recommend adding `slowapi` or Redis token bucket middleware. |
| **NOT-01** | Push Notifications | **FCM Push Notification Daemon:** Alert engine calculates weather/irrigation alerts dynamically on REST fetch; no Firebase Cloud Messaging (FCM) push daemon exists. | Medium | In-app pull notifications are functional; FCM integration required for out-of-app background push alerts. |
| **SAT-01** | Remote Sensing | **Sentinel-1 SAR Radar Ingestion:** Radar backscatter (VV/VH) processing is not implemented in raster pipeline; system relies on Sentinel-2 optical MSI and NWP soil model. | Low | Optical NDMI and FAO-56 moisture models active and validated. SAR pipeline planned for monsoon all-weather tracking. |
| **ARC-01** | Client Persistence | **Room Database Architecture Gap:** Android client currently relies on `SharedPreferences` (`geopulse_user_session`, `geopulse_notifications`) and in-memory caches rather than a Room SQLite database. Fields do not survive process kill in airplane mode. | Medium (Architectural) | **DOCUMENTED** (Task 10 & Post-Audit). While adequate for sessions and preferences, migration to Room (`FieldEntity`, `SatelliteCacheEntity`) is recommended for offline multi-parcel persistence. |

---

## 3. REGRESSION RISKS TO MONITOR

1. **MapLibre Native SDK Surface Destruction:** When switching between full-screen map mode and dialogs, Compose must preserve MapView lifecycle hooks (`onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`) to prevent OpenGL surface recreation crashes.
2. **Database Migration Consistency:** Any future modification to SQLAlchemy models in `backend/models.py` must either be reflected in SQLite tables via Alembic migrations or require dropping and regenerating `geopulse.db` during development.
3. **External API Rate Limiting:** While Open-Meteo and NASA POWER do not enforce strict API keys, high-frequency polling can result in HTTP 429 Too Many Requests. Backend caching ensures repeat requests within a 15-minute window use cached observations.
