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
| **SEC-01** | `AndroidManifest.xml` | **`android:allowBackup="true"`:** Enabled by default in debug manifest, allowing ADB backup extraction of application data. | Low (Debug) / Medium (Release) | Safe for development; must be changed to `android:allowBackup="false"` before publishing production release APK to Google Play. |
| **SEC-02** | `AndroidManifest.xml` | **`android:usesCleartextTraffic="true"`:** Enabled globally to allow HTTP connection to local development server (`http://10.0.2.2:8000/` or LAN IP `http://172.23.176.200:8000/`). | Low (Debug) / High (Release) | Must be replaced with a `res/xml/network_security_config.xml` that permits cleartext only for internal development IP addresses while enforcing HTTPS for all production domains. |
| **SEC-03** | `ApiClient.kt` | **Hardcoded Workstation LAN IP Fallback:** Base URL defaults to `http://172.23.176.200:8000/`. When testing on an emulator without reverse port forwarding, network calls to LAN IP may time out unless changed to `10.0.2.2:8000`. | Low | `ApiClient.baseUrl` is mutable and can be dynamically set via `Screen29Activity` (Settings) or during application startup. |

---

## 3. REGRESSION RISKS TO MONITOR

1. **MapLibre Native SDK Surface Destruction:** When switching between full-screen map mode and dialogs, Compose must preserve MapView lifecycle hooks (`onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`) to prevent OpenGL surface recreation crashes.
2. **Database Migration Consistency:** Any future modification to SQLAlchemy models in `backend/models.py` must either be reflected in SQLite tables via Alembic migrations or require dropping and regenerating `geopulse.db` during development.
3. **External API Rate Limiting:** While Open-Meteo and NASA POWER do not enforce strict API keys, high-frequency polling can result in HTTP 429 Too Many Requests. Backend caching ensures repeat requests within a 15-minute window use cached observations.
