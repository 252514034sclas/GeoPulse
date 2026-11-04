# GeoPulse — Initial Architecture & Resource-Integration Audit

**Project:** GeoPulse (`com.geopulse.app`)  
**Branch:** `qa-repair-mission`  
**Date:** November 2026  
**Auditor:** Autonomous Systems & Agronomic Engineering Group  
**Target Reference Device:** Vivo V2037 (Android 13, 720 × 1600 px)

---

## 1. Executive Summary & Diagnostic Findings

This comprehensive audit inspects the existing GeoPulse codebase across Android (Kotlin + Jetpack Compose + MapLibre Native) and Backend (Python + FastAPI + SQLite/PostgreSQL).

### Diagnostic Assessment of Current Defects
1. **Language & Terminology Overload:**
   - Excessive remote-sensing and data-science jargon (e.g., *NDVI, NDWI, STAC, FAO-56 Penman-Monteith, GeoTIFF, Evapotranspiration, Geospatial Polygons, SpatioTemporal Asset Catalog*) is displayed directly on user-facing cards.
   - Ordinary Indian farmers require intuitive terminology (*Crop Health, Soil Water, Crop Water Use, Field Boundary, Fertilizer Amount, Satellite Image Date*).
2. **Dashboard Visual Hierarchy & Spacing:**
   - The homepage currently attempts to present too many features simultaneously (e.g., 14-tile grid plus hero plus insights plus action card), leading to visual clutter on small screens (720×1600 px).
   - The homepage must be restructured into a focused, decision-oriented layout: **Header → Satellite Farm Map with Satellite/Normal Toggle → 4 Core Farm Status Cards → Today's Advice (What/Why/When/How) → 6 Primary Quick Actions**.
3. **Map Rendering & Satellite Imagery Transparency:**
   - The map needs an explicit **Satellite / Normal Map** toggle switch.
   - Data provenance must distinguish between:
     1. Basemap imagery provider (`Esri World Imagery` / `MapLibre Streets`).
     2. Satellite observation date (`Oct 29, 2026`).
     3. Crop health analysis date (`Oct 29, 2026`).
     4. Last successful data refresh timestamp.
   - If clouds prevent reliable optical analysis, an explicit, polite message must inform the farmer rather than displaying stale imagery without explanation.
4. **Backend Architecture & Agricultural Knowledge:**
   - External API calls are isolated in the FastAPI backend (`backend/adapters/` and `backend/services/`).
   - The backend already contains working engines for FAO-56 irrigation balance, ICAR/TNAU sowing varieties, split-dosage fertilizer calculations, and Open-Meteo weather forecasts.
   - All backend credentials remain secured in `.env`, not embedded in the Android APK.

---

## 2. Master Approved Resource Register

The approved data and asset providers are verified and cataloged below:

| Resource Domain | Approved Provider | Specific Dataset / API | Access Method & Endpoint | Authentication & Keys | Licensing & Terms | Update Frequency | Integrated GeoPulse Module | Known Limitations & Fallbacks |
|:---|:---|:---|:---|:---|:---|:---|:---|:---|
| **Satellite Crop Health & Vigor** | **Copernicus Data Space Ecosystem / Planetary Computer** | Sentinel-2 Level-2A Surface Reflectance (B04 Red, B08 NIR, B02 Blue, B11 SWIR) | STAC API `POST /search` & Cloud-Optimized GeoTIFFs (COGs) | Free Open Data; dynamic SAS token for planetary computer | CC BY 4.0 (Copernicus Open Access) | 5-day revisit cycle | Screens 7, 8, 11, 24; Backend `copernicus_adapter.py` | Cloud obstruction (>20%) requires temporal fallback to previous clear scene; labeled explicitly |
| **Indian Earth Observation & SAR** | **ISRO NRSC (Bhoonidhi / Bhuvan)** | EOS-04 C-band SAR (soil moisture penetrating clouds) | Bhuvan Web Map Services & Bhoonidhi open data catalog | Open access for non-commercial research; registered portal | Govt. of India Open Data License | Bi-weekly / seasonal | Screen 12, Backend `isro_bhoonidhi_adapter.py` | Bhuvan WMS requires exact coordinate bounds; calibrated regional fallback provided |
| **Weather & Spray Forecast** | **Open-Meteo** | High-Resolution Numerical Weather Prediction (NWP) | REST API `GET https://api.open-meteo.com/v1/forecast` | No API key required for non-commercial tier (<10,000 req/day) | CC BY 4.0 | Hourly / 7-day forecast | Screens 7, 13, 14, 28; Backend `open_meteo_adapter.py` | Rate limit protection; cached forecast stored in SQLite/PostgreSQL |
| **National Weather Warnings** | **IMD (India Meteorological Department)** | Mausam Weather Bulletins & Agromet Advisories | Published regional Agromet Bulletins | Public open domain | Govt of India public domain | Daily at 08:30 & 17:30 IST | Screen 28 (Warning Center) | Machine-readable API has strict uptime; synthesized through Open-Meteo & IMD bulletins |
| **Crop Science & Seed Varieties** | **TNAU (Tamil Nadu Agricultural University) & ICAR** | TNAU Agritech Portal & KVK Package of Practices | Structured Knowledge Base (`seed_sowing_engine.py`) | Academic publication / Public Extension | Academic attribution (TNAU / ICAR-IIMR / IISR) | Seasonal | Screens 18, 19, 23; Backend `seed_sowing_engine.py` | Recommendations tailored to regional agro-climatic zones (Red, Black, Alluvial soils) |
| **Fertilizer Split-Dosage** | **TNAU Agritech Nutrient Management / ICAR** | NPK Fertilizer Dosages (Urea, DAP, MOP) based on soil test & crop stage | Algorithmic Calculator (`fertilizer_calculator.py`) | Public agronomic standards | Public Extension | Per crop growth stage | Screen 20; Backend `fertilizer_calculator.py` | Tailored to field greenness and soil moisture status |
| **Crop Water Balance & Irrigation** | **FAO-56 Irrigation Engine** | Penman-Monteith ET0, Crop Kc, Root-Zone Available Water Capacity | Algorithmic Engine (`irrigation_engine.py`) | Mathematical standard | Public Domain | Daily calculation | Screens 12, 13; Backend `irrigation_engine.py` | Physical units strictly converted to Litres and Cubic Metres ($1\text{ mm} \times 1\text{ ha} = 10,000\text{ L}$) |
| **Agricultural Market Prices** | **AGMARKNET / Data.gov.in** | Daily Wholesale Mandi Arrival Prices (Modal, Min, Max in ₹/Quintal) | OGD India API & Agmarknet Daily Wholesale Price Data | Open Government Data Platform (OGD) | National Data Sharing and Accessibility Policy (NDSAP) | Daily market close | Screen 27; Backend `main.py` | Markets closed on Sundays/holidays; displays last available date clearly |
| **Farmer Emergency Helpline** | **Government of India Kisan Call Centre** | Toll-Free Agricultural Assistance (`1800-180-1551`) | Android Native `ACTION_DIAL` (`tel:18001801551`) | Free Toll-Free Voice Call | Govt of India Ministry of Agriculture | 6:00 AM – 10:00 PM (22 languages) | Screen 26 (`Screen26Activity.kt`) | Requires cellular carrier / SIM dialer |
| **Map Rendering Engine** | **MapLibre Native Android SDK** | Vector & Raster MapLibre GL Native rendering | Android SDK `org.maplibre.gl:android-sdk:11.5.2` | Open-source BSD-3-Clause | MapLibre Open Source | Client-side 60 FPS | Screens 7, 8; `MapLibreGisMap.kt` | Native OpenGL/Vulkan surface requires lifecycle binding |
| **Basemap Tile Providers** | **CARTO / MapLibre Demotiles / Esri** | Satellite Imagery & Positron / Dark Matter Vector Styles | Tile endpoints in `MapConfig.kt` | Standard public tile tiers with proper attribution | MapLibre & CARTO open styles | Static basemap cache | Screens 7, 8, 29 | Compliant with tile usage policies |

---

## 3. Farmer-First Jargon Replacement Dictionary

All Android UI components and strings must strictly conform to the following language rules:

| Original Technical / Scientific Term | Farmer-Friendly Label (English) | Explanation for Ordinary Farmers |
|:---|:---|:---|
| **NDVI** (Normalized Difference Vegetation Index) | **Crop Health** | Simple score showing leaf greenness and plant vigor |
| **NDWI / NDMI** (Normalized Difference Water Index) | **Crop Water Status** | Measures moisture content inside crop leaves and topsoil |
| **STAC / Sentinel-2 L2A COG** | **Satellite Image Date: [Date]** | Identifies when the space observation was taken |
| **FAO-56 Penman-Monteith** | **Crop Water Balance** | Physical formula ensuring neither over-watering nor drought |
| **Evapotranspiration ($ET_0, ET_c$)** | **Crop Water Use (mm/day)** | Amount of water consumed by plants and evaporated by sun |
| **Geospatial Polygon / WKT** | **Field Boundary** | The boundary lines enclosing your farmland parcel |
| **Variable Rate Nitrogen Application** | **Fertilizer Amount** | Recommended bags or kilograms of Urea, DAP, and Potash |
| **Satellite Acquisition Timestamp** | **Image Date** | Date and time when the satellite passed over your field |
| **Acreage / Spatial Geometry** | **Land Area** | Area of your farm in Acres and Hectares |
| **Chlorosis / Necrosis** | **Leaf Yellowing / Leaf Damage** | Signs of nitrogen deficiency or disease requiring inspection |

---

## 4. Homepage Restructuring Specification

To prevent overwhelming the farmer, the homepage (`PremiumHomeScreen`) will be restructured into a clean 5-tier visual hierarchy:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Header: GeoPulse Logo | "Green Valley Farm" ▾ | 🔔 Avatar│
├─────────────────────────────────────────────────────────────┤
│ 2. Real Satellite Map Card (250dp)                          │
│    - Satellite / Normal Map Toggle Switch [🛰️ / 🗺️]         │
│    - Field 1 (12.5 ha · Corn) [Verified Boundary Overlay]   │
│    - Satellite Image Date: Oct 29, 2026 (3 days ago)        │
│    - Status: Live Copernicus STAC (Clear Sky)               │
├─────────────────────────────────────────────────────────────┤
│ 3. Farm Condition At A Glance (4 Status Cards, 2x2)         │
│    [🌱 Crop Health: 84% Good]   [💧 Soil Water: 72% Moist]   │
│    [🌦️ Weather: 28°C Calm]      [⚠️ Farm Alerts: 1 Active]   │
├─────────────────────────────────────────────────────────────┤
│ 4. Today's Advice (Actionable Decision Card)                 │
│    • What is happening: Good soil moisture, rain in 48 hrs  │
│    • What to do: Apply split Urea fertilizer now            │
│    • When: Today morning before 11:00 AM                    │
│    • Why: Rain will dissolve nutrients directly to roots    │
├─────────────────────────────────────────────────────────────┤
│ 5. Quick Actions (6 Primary Action Buttons)                 │
│    [💧 Watering]     [🌱 Crop Health]   [🌦️ Weather]        │
│    [🌾 Fertilizer]   [🐛 Pest Risk]     [📈 Mandi Prices]   │
│                                                             │
│    [🔗 View All Farm Tools & Calendar (More >)]             │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Screen-by-Screen Redesign Status Matrix

| Screen # | Activity Class | Redesigned Purpose | Core Information Displayed | Farmer Actions (Interactive) | Data Provenance Displayed |
|:---:|:---|:---|:---|:---|:---|
| **0** | `SplashActivity` | Fast brand entry & theme init | Antigravity pulsing logo, version | Auto-advance to Welcome | Local app assets |
| **1** | `WelcomeActivity` | Welcome & farm onboarding entry | Value proposition in simple language | "Get Started" (Onboarding), "Sign In" | Local app assets |
| **2** | `LoginActivity` | Farmer account sign-in | Phone / Email & password, demo fill | One-tap demo account, submit login | Encrypted auth token |
| **3** | `CreateAccountActivity` | Register farmer & farm profile | Full name, farm name, email, password | Register account with validation | PostgreSQL/SQLite user record |
| **4** | `ForgotPasswordActivity`| Password recovery | Recovery instructions & email field | Submit reset link request | Auth service |
| **5/6**| `OnboardingActivity` | 3-step farmer-friendly tour | 1: Satellite Maps, 2: Soil Water, 3: Harvest | Swipe tour, tap Next / Skip | Local illustrations |
| **7** | `Screen7Activity` | Central Dashboard (4 Core Tabs) | Tab 0: Home, Tab 1: GIS Map, Tab 2: Advisory, Tab 3: Profile | Swipe tabs, drag dock, tap quick actions | Live backend + Copernicus + Open-Meteo |
| **8** | `Screen8Activity` | Standalone Full GIS Map Screen | Full MapLibre GIS Map, Sentinel-2 tiles, GPS drawing | Toggle Satellite/Normal, draw field, zoom/pan | Copernicus Sentinel-2 MSI L2A |
| **9** | `Screen9Activity` | Standalone Advisory Hub | Hyperlocal tasks: Spraying, Watering, Fertilizer | Filter tasks, view actionable advice | TNAU / ICAR / Open-Meteo |
| **10**| `Screen10Activity`| Standalone Profile & Settings | Farmer profile, theme selector, parcel manager | Switch theme, export records, sign out | Local SharedPreferences + DB |
| **11**| `Screen11Activity`| Crop Health (NDVI) | Canopy greenness level, leaf vigor, fertilizer tip | Interactive vigor slider, Primary: Fertilizer | Sentinel-2 MSI (Oct 29, 2026) |
| **12**| `Screen12Activity`| Soil Water (NDWI) | Topsoil (72%) & root-zone (65%) water, watering advice | Depth toggle, Primary: Watering Timing | ISRO EOS-04 SAR + Sentinel-2 |
| **13**| `Screen13Activity`| Watering Advice (FAO-56) | Water requirement (mm/litres), pump run time, rain | View water calculation, Primary: Weather | FAO-56 Penman-Monteith Model |
| **14**| `Screen14Activity`| Weather & Spray Advisory | Wind speed (8 km/h), rain risk, safe spray window | Hourly forecast, Primary: Pest Risk | Open-Meteo High-Res NWP |
| **15**| `Screen15Activity`| Pest & Disease Advisory | Fall armyworm & leaf spot risk, neem bio-spray (5ml/L) | View prevention, Primary: Crop Health, Sec: Helpline | TNAU Crop Protection Guide |
| **16**| `Screen16Activity`| Harvest & Yield Predictor | Expected yield (34-36 Q/ac), maturity window | Harvest timing, Primary: Storage Tips | ICAR Crop Production Model |
| **17**| `Screen17Activity`| Field Boundary & Land Area | GPS perimeter (1,840m), 12.5 ha (30.8 ac), 4 GPS vertices | Inspect points, Primary: Parcels, Sec: Map | MapLibre Geodesic Engine |
| **18**| `Screen18Activity`| Crop Calendar & Sowing | Sowing date, growth stage (Tasseling), seed variety | Seed rate (20 kg/ha), Primary: Plowing | ICAR-IIMR Package of Practices |
| **19**| `Screen19Activity`| Plowing & Field Planner | Soil workability (workable), tillage timing | Plowing guide, Primary: Weather Forecast | TNAU Tillage Guidelines |
| **20**| `Screen20Activity`| Fertilizer Amount (NPK) | Urea (45 kg/ac), DAP (30 kg/ac), MOP (20 kg/ac) dosage | Adjust dosage slider, Primary: Soil Water | TNAU Soil Nutrient Schedule |
| **21**| `Screen21Activity`| Harvest Storage Tips | Safe grain moisture (<14%), sun-drying protocols | Storage guidelines, Primary: Mandi Rates | ICAR Post-Harvest Guide |
| **22**| `Screen22Activity`| Farm Budget & Net Profit | Input cost (₹18.5k/ac), Gross (₹42k), Net profit (+₹23.5k) | Profit calculator, Primary: Mandi Rates | Local Agricultural Economics |
| **23**| `Screen23Activity`| My Land Parcels & Crops | Registered plots: Field 1 (Corn), Field 2 (Soybeans) | Switch field, Primary: Edit Boundary | GeoPulse Field Database |
| **24**| `Screen24Activity`| Satellite Growth History | Bi-weekly vegetation greenness curve (July - Nov) | Review growth, Primary: Current Health | Sentinel-2 STAC Time-Series |
| **25**| `Screen25Activity`| Organic Soil Health | Cow manure (FYM), vermicompost, soil organic carbon | Organic practices, Primary: Fertilizer Guide | TNAU Organic Agriculture |
| **26**| `Screen26Activity`| Kisan Helpline & Expert | One-tap direct call to `1800-180-1551`, expert chat form | Tap to Call Govt Helpline, submit question | Govt of India Kisan Call Centre |
| **27**| `Screen27Activity`| Local Mandi Market Prices | Live market rates (Corn ₹2,380/Q, Soy ₹4,620/Q, Cotton ₹7,250/Q) | Compare prices, Primary: Farm Budget | AGMARKNET Daily Wholesale |
| **28**| `Screen28Activity`| Weather & Crop Warnings | Severe weather alerts, thunderstorms, frost warnings | Review warnings, Primary: 7-Day Forecast | IMD / Open-Meteo Alerts |
| **29**| `Screen29Activity`| Settings & Appearance | Theme (Light/Dark/System), Map basemap selector | Switch theme, configure GIS style | Android SharedPreferences |
| **30**| `Screen30Activity`| Farm Records & Export | Download field boundary, spray logs, fertilizer diary | Export GeoJSON, clear local cache | GeoJSON Data Serializer |

---

## 6. Implementation Execution Checklist

- [x] Phase 1: Architectural & Resource Register Audit (Completed).
- [ ] Phase 2: Design system update in `Theme.kt`, `Color.kt`, and `Type.kt`.
- [ ] Phase 3: MapLibre satellite basemap toggle & Sentinel-2 freshness overlay.
- [ ] Phase 4: Homepage redesign in `PremiumComponents.kt` with clean 5-section hierarchy and simplified farmer language.
- [ ] Phase 5: Redesign all 20 feature modules in `GeoComponents.kt` to replace technical jargon with farmer-friendly labels.
- [ ] Phase 6: Unit tests, Gradle compilation, debug APK build, and physical-device verification on Vivo V2037.
