# GeoPulse — Comprehensive 30-Screen Audit & Navigation Repair Plan

**Branch:** `qa-repair-mission`  
**Package:** `com.geopulse.app`  
**Target Device Tested:** Vivo V2037 (Android 13 / API 33, 720×1600 px, 300 dpi)  
**Date:** November 2026

---

## 1. Executive Summary & Architecture Overview

An exhaustive code-level and physical-device audit was conducted across all 30 screens of the GeoPulse precision agriculture application. The app's architecture comprises:
- **Authentication & Onboarding (Screens 0–6):** Splash, Welcome, Login, Create Account, Forgot Password, and 3-step Onboarding.
- **Core Dashboard & GIS Engine (Screens 7–10):** Home Overview, Full Interactive MapLibre Native GIS Map, Precision Advisory Hub, and Farmer Profile.
- **Precision Agriculture Advisory & Field Operations (Screens 11–30):** 20 dedicated agronomic tools spanning satellite NDVI, root-zone NDWI, FAO-56 watering schedules, spray forecasts, pest management, APMC Mandi rates, and government Kisan helpline services.

### Identified Systemic Defects Prior to Repair
1. **Button-Chain Miswirings:**
   - **Screen 7 (Home):** "Crop Health" metric pill navigated to `Screen15Activity` (Pest & Disease Guide) instead of `Screen11Activity` (Crop Health & Vigor NDVI).
   - **Screen 15 (Pest Guide):** Action button labeled *"Check Satellite Crop Health"* pointed to `Screen26Activity` (Kisan Helpline).
   - **Screen 19 (Plowing Planner):** Action button labeled *"Check Weather Forecast"* pointed to `Screen13Activity` (Watering Timing Guide) instead of `Screen14Activity` (Weather).
   - **Screen 22 (Farm Budget):** Action button labeled *"View Mandi Market Prices"* pointed to `Screen30Activity` (Data Export) instead of `Screen27Activity` (Local Mandi Rates).
   - **Screen 23 (Land Parcels):** Action button labeled *"Edit Field Boundaries"* pointed to `Screen16Activity` (Yield Estimator) instead of `Screen17Activity` (Field Boundary & Land Area).
   - **Screen 24 (Growth History):** Action button labeled *"View Current Crop Vigor"* pointed to `Screen18Activity` (Crop Calendar) instead of `Screen11Activity` (Crop Health NDVI).
2. **Screens 8, 9, 10 Stub Redirect Defect:**
   - `Screen8Activity`, `Screen9Activity`, and `Screen10Activity` were bare stubs executing `startActivity(Screen7Activity, TARGET_TAB=x); finish()`. This destroyed the caller's back-stack and produced UI flicker.
3. **Helpline Module Actionability:**
   - `Screen26Activity` (Kisan Helpline) contained a simulated text field but lacked direct one-tap dialing to the Government of India Kisan Call Center (`1800-180-1551`).

---

## 2. Exhaustive 30-Screen Audit Matrix

| Screen # | Activity Class | Title / Module | UI & Responsiveness Audit | Current Button & Target | Audit Verdict & Target Fix |
|:---:|:---|:---|:---|:---|:---|
| **0** | `SplashActivity` | Animated Pulse Splash | 900ms pulse animation, clean status bars, safe auto-advance. | None (auto-advance to Screen 1) | **PASS** — Flawless launch animation. |
| **1** | `WelcomeActivity` | Welcome to GeoPulse | Glassmorphic card, radial gradient background, tablet width-constrained. | "Get Started" -> Screen 5/6<br>"Sign In" -> Screen 2 | **PASS** — Clean entry point with explicit auth options. |
| **2** | `LoginActivity` | Farmer Login & Demo | Email/password validation, demo credentials shortcut, error feedback. | "Login" -> Screen 7<br>"Sign Up" -> Screen 3<br>"Forgot" -> Screen 4 | **PASS** — Responsive, keyboard-safe (IME padding). |
| **3** | `CreateAccountActivity` | Farmer Registration | Multi-field registration form (Name, Farm, Email, Password), match checks. | "Register" -> Screen 7<br>"Sign In" -> finish() | **PASS** — Full validation; returns to login cleanly. |
| **4** | `ForgotPasswordActivity` | Password Reset | Email recovery form, validation alert, success confirmation state. | "Send Link" -> inline success<br>"Back to Sign In" -> finish() | **PASS** — Self-contained feedback loop. |
| **5/6**| `OnboardingActivity` | 3-Step Feature Tour | Animated slide transitions (Map, Soil, AI), step indicators, skip button. | "Next / Get Started" -> Screen 2<br>"Skip" -> Screen 2 | **PASS** — Smooth pager, dismisses safely to Login. |
| **7** | `Screen7Activity` | Core Dashboard (4 Tabs) | Swipeable HorizontalPager, MapLibre Native GIS map, Draggable bottom dock. | Crop Health pill -> `Screen15` (**BUG**) | **REPAIR**: Wire Crop Health pill to `Screen11Activity`. |
| **8** | `Screen8Activity` | Full GIS Map Screen | Previously redirect stub destroying back-stack. | Redirect to Screen 7 Tab 1 (**BUG**) | **REPAIR**: Render full MapLibre GIS tab with TopBar & back navigation. |
| **9** | `Screen9Activity` | Precision Advisory Hub | Previously redirect stub destroying back-stack. | Redirect to Screen 7 Tab 2 (**BUG**) | **REPAIR**: Render full Advisory tab with TopBar & back navigation. |
| **10**| `Screen10Activity`| Farm Profile & Settings | Previously redirect stub destroying back-stack. | Redirect to Screen 7 Tab 3 (**BUG**) | **REPAIR**: Render full Profile tab with TopBar & back navigation. |
| **11**| `Screen11Activity`| Satellite Crop Health NDVI | Interactive chlorophyll slider, biomass chips, recommendation card. | "View Fertilizer" -> `Screen20` | **PASS** — Direct logical progression to nitrogen dosing. |
| **12**| `Screen12Activity`| Soil Moisture Report | Depth selector (0-15cm vs 15-40cm), root-zone hydration analysis. | "Watering Schedule" -> `Screen13` | **PASS** — Directly feeds irrigation decision-making. |
| **13**| `Screen13Activity`| Safe Watering Timing Guide | Real FAO-56 Penman-Monteith crop water balance from backend. | "Rain & Spray" -> `Screen14` | **PASS** — Prevents waterlogging and waste. |
| **14**| `Screen14Activity`| Weather & Spraying Advisory | Wind speed, rain probability, safe spray window guidelines. | "Pest & Disease" -> `Screen15` | **PASS** — Rain & humidity directly drive disease alerts. |
| **15**| `Screen15Activity`| Pest & Disease Advisory | Fall armyworm & leaf spot risk, organic neem oil dosage instructions. | "Check Crop Health" -> `Screen26` (**BUG**) | **REPAIR**: Label as *"Consult Kisan Helpline"* -> `Screen26` + secondary link to `Screen11`. |
| **16**| `Screen16Activity`| Harvest & Yield Estimator | Production estimator (34-36 Q/ac), maturity window calculation. | "Harvest Storage" -> `Screen21` | **PASS** — Direct agronomic progression to post-harvest. |
| **17**| `Screen17Activity`| Field Boundary & Land Area | Acreage calculation, perimeter delineation, GPS coordinates. | "View All Parcels" -> `Screen23` | **PASS** — Logical transition to multi-field inventory. |
| **18**| `Screen18Activity`| Crop Calendar & Stages | Tillering, vegetative, flowering milestones with care steps. | "Soil Moisture" -> `Screen12` | **PASS** — Growth stages dictate hydration requirements. |
| **19**| `Screen19Activity`| Operations & Plowing Planner| Tillage, sowing, harrowing timing based on soil workability. | "Check Weather" -> `Screen13` (**BUG**) | **REPAIR**: Target changed to `Screen14Activity` (Weather). |
| **20**| `Screen20Activity`| Fertilizer & Nutrient Guide | Variable-rate Urea (N), DAP (P), Potash (K) dosing per acre. | "Field Moisture" -> `Screen12` | **PASS** — Dosing requires adequate soil moisture before split-application. |
| **21**| `Screen21Activity`| Harvest Storage Tips | Grain moisture thresholds (<14%), drying methods, silo pest prevention. | "Mandi Prices" -> `Screen27` | **PASS** — Direct transition to market selling. |
| **22**| `Screen22Activity`| Farm Budget & Expense | Seed, fertilizer, diesel, and labor balance sheet with profit margin. | "Mandi Prices" -> `Screen30` (**BUG**) | **REPAIR**: Target changed to `Screen27Activity` (Mandi). |
| **23**| `Screen23Activity`| My Land Parcels & Crops | Multi-parcel management, survey numbers, active crop assignments. | "Edit Boundaries" -> `Screen16` (**BUG**) | **REPAIR**: Target changed to `Screen17Activity` (Boundary). |
| **24**| `Screen24Activity`| Satellite Growth History | Bi-weekly NDVI time-series curves and historical vigor comparison. | "Current Vigor" -> `Screen18` (**BUG**) | **REPAIR**: Target changed to `Screen11Activity` (Crop Health). |
| **25**| `Screen25Activity`| Organic Soil Health | Vermicomposting, green manuring, soil organic carbon guidance. | "Fertilizer Guide" -> `Screen20` | **PASS** — Natural complement to chemical fertilization. |
| **26**| `Screen26Activity`| Kisan Helpline & Chat | Direct toll-free Kisan Call Center dialer (`1800-180-1551`), chat interface. | "Return to Dashboard" | **REPAIR**: Add direct phone dialer intent for emergency agronomy assistance. |
| **27**| `Screen27Activity`| Local Mandi Market Rates | Live wholesale commodity prices (Maize, Soy, Cotton, Paddy) in ₹/Quintal. | "Farm Budget" -> `Screen22` | **PASS** — Real pricing feeds into profitability calculator. |
| **28**| `Screen28Activity`| Weather & Warning Center | Severe weather warnings, thunderstorm alerts, frost alerts. | "Weather Forecast" -> `Screen14` | **PASS** — Direct access to 7-day meteorological trends. |
| **29**| `Screen29Activity`| App Settings & GIS Config | Dark/Light theme switching, MapLibre style selection (Raster/Vector). | "Return to Dashboard" | **PASS** — Real-time persistence via `ThemePreferences`. |
| **30**| `Screen30Activity`| Farm Records & Data Export | Field history download (PDF/Excel), data privacy & account clearing. | "Return to Dashboard" | **PASS** — Compliant data export & account controls. |

---

## 3. Detailed Architectural Fixes

### Fix 1: Resolve Screen 8, 9, 10 Stub Defect
- Convert `Screen8Activity`, `Screen9Activity`, and `Screen10Activity` into independent, fully styled activities.
- Each activity renders its respective dashboard tab inside a `Scaffold` with `GeoTopBar(title, subtitle, onBackClick = { finish() })`.
- When launched standalone or via external deep-links/notifications, back navigation cleanly returns to the previous screen without clearing the back-stack.

### Fix 2: Repair Miswired Activity Target Buttons
1. **`Screen15Activity.kt`**: Update button text to `"Consult Kisan Helpline / Expert"` (Target: `Screen26Activity::class.java`).
2. **`Screen19Activity.kt`**: Change target from `Screen13Activity` to `Screen14Activity::class.java` (`nextButtonText = "Check Weather & Spray Forecast"`).
3. **`Screen22Activity.kt`**: Change target from `Screen30Activity` to `Screen27Activity::class.java` (`nextButtonText = "View Mandi Wholesale Rates"`).
4. **`Screen23Activity.kt`**: Change target from `Screen16Activity` to `Screen17Activity::class.java` (`nextButtonText = "Edit Field Boundaries & Area"`).
5. **`Screen24Activity.kt`**: Change target from `Screen18Activity` to `Screen11Activity::class.java` (`nextButtonText = "View Current Crop Vigor (NDVI)"`).
6. **`Screen7Activity.kt`**: Fix `onCropHealthClick` in `Screen7Activity:135` to navigate to `Screen11Activity::class.java`.

### Fix 3: Enhance `AgronomistChatModule` with Kisan Call Center Dialer
- Add prominent `Button` with `Icons.Default.Phone` labeled `"Call Toll-Free Kisan Helpline (1800-180-1551)"`.
- Trigger `Intent(Intent.ACTION_DIAL, Uri.parse("tel:18001801551"))` with context `LocalContext.current`.

### Fix 4: Universal Dual-Action Navigation Footer in `GeoDetailScreenContent`
- Every detail screen now features:
  - **Primary Action Button:** The contextually correct next step in the agricultural workflow.
  - **Secondary Action Button:** `"Return to Dashboard"` with `FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP`, preventing stack accumulation.
  - **Top Bar Back Button:** Explicit `activity.finish()` for immediate return to the preceding screen.
