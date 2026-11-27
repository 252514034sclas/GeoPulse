package com.geopulse.app.util

/**
 * GeoStrings: Authoritative bilingual strings catalog for GeoPulse Android Application.
 * Supports English and natural, farmer-friendly agricultural Tamil (தமிழ்).
 */
object GeoStrings {

    // Helper shorthand
    private fun t(en: String, ta: String): String = GeoLanguageManager.t(en, ta)

    // --- App & Branding ---
    val appName: String get() = t("GeoPulse", "ஜியோபல்ஸ்")
    val appTagline: String get() = t("Precision Geospatial Farm Intelligence", "துல்லிய புவிசார் விவசாய நுண்ணறிவு")
    val welcomeTitle: String get() = t("Welcome to GeoPulse", "ஜியோபல்ஸ்-க்கு வரவேற்கிறோம்")
    val welcomeDesc: String get() = t(
        "Monitor crop health, analyze soil moisture, track field activity, and optimize farm yields with real-time geospatial intelligence.",
        "செயற்கைக்கோள் தரவு மூலம் பயிர் நலம், மண் ஈரப்பதம் மற்றும் பாசன ஆலோசனைகளை உடனுக்குடன் பெற்று விளைச்சலை பெருக்குங்கள்."
    )
    val tnAgriTechBadge: String get() = t("TAMIL NADU PRECISION AGRI-TECH", "தமிழ்நாடு துல்லிய வேளாண் தொழில்நுட்பம்")
    val getStarted: String get() = t("Get Started", "தொடங்குங்கள்")
    val signIn: String get() = t("Sign In", "உள்நுழைக")
    val createAccount: String get() = t("Create Free Account", "புதிய கணக்கு தொடங்குங்கள்")
    val forgotPassword: String get() = t("Forgot Password?", "கடவுச்சொல் மறந்துவிட்டதா?")

    // --- Onboarding Slides ---
    val slide1Title: String get() = t("Precision Satellite Farm Mapping", "துல்லிய செயற்கைக்கோள் நில வரைபடம்")
    val slide1Desc: String get() = t(
        "Trace your field boundaries and monitor crop health directly from European Space Agency (Sentinel-2) and ISRO satellites.",
        "உங்கள் வயல் எல்லைகளை வரைந்து, ஐரோப்பிய விண்வெளி நிறுவனம் மற்றும் இஸ்ரோ செயற்கைக்கோள்கள் மூலம் பயிர் நலனை கண்காணிக்கவும்."
    )
    val slide2Title: String get() = t("Smart Soil Moisture & Irrigation", "துல்லிய மண் ஈரப்பதம் & பாசனம்")
    val slide2Desc: String get() = t(
        "Track root-zone soil moisture from orbit. Receive daily watering alerts so you only irrigate when your crops truly need it.",
        "வேர் மண்டல மண் ஈரப்பதத்தை விண்வெளியில் இருந்து கண்காணித்து, பயிர்களுக்கு தேவைப்படும் போது மட்டும் நீர் பாய்ச்சுங்கள்."
    )
    val slide3Title: String get() = t("Practical Crop & Yield Advisory", "பயிர் பாதுகாப்பு & மகசூல் வழிகாட்டி")
    val slide3Desc: String get() = t(
        "Receive practical guidance on fertilizer dosage, safe weather spraying windows, and local mandi prices to protect your harvest.",
        "உர அளவு, மருந்து தெளிக்க உகந்த வானிலை மற்றும் உள்ளூர் மண்டி சந்தை விலை குறித்த நடைமுறை வழிகாட்டல்களை பெறுங்கள்."
    )
    val step1of3: String get() = t("STEP 1 OF 3", "படி 1 / 3")
    val step2of3: String get() = t("STEP 2 OF 3", "படி 2 / 3")
    val step3of3: String get() = t("STEP 3 OF 3", "படி 3 / 3")
    val skip: String get() = t("Skip", "தவிர்")
    val next: String get() = t("Next", "அடுத்து")

    // --- Authentication & Account ---
    val emailLabel: String get() = t("Email Address", "மின்னஞ்சல் முகவரி")
    val passwordLabel: String get() = t("Password", "கடவுச்சொல்")
    val fullNameLabel: String get() = t("Full Name", "முழு பெயர்")
    val phoneLabel: String get() = t("Mobile Phone Number", "கைபேசி எண்")
    val farmNameLabel: String get() = t("Farm Name", "பண்ணை பெயர்")
    val districtLabel: String get() = t("District / Region", "மாவட்டம் / பகுதி")
    val totalAreaLabel: String get() = t("Total Land Area (Acres)", "மொத்த நிலப்பரப்பு (ஏக்கர்)")
    val invalidEmailError: String get() = t("Please enter a valid email address.", "சரியான மின்னஞ்சல் முகவரியை உள்ளிடவும்.")
    val shortPasswordError: String get() = t("Password must be at least 6 characters.", "கடவுச்சொல் குறைந்தது 6 எழுத்துகள் இருக்க வேண்டும்.")
    val blankNameError: String get() = t("Please enter your full name.", "உங்கள் முழு பெயரை உள்ளிடவும்.")
    val termsAgreement: String get() = t("I agree to GeoPulse Terms & Farmer Privacy Policy", "விவசாயி தனியுரிமை மற்றும் பயன்பாட்டு விதிமுறைகளை ஏற்கிறேன்")
    val alreadyHaveAccount: String get() = t("Already have an account? Sign In", "ஏற்கனவே கணக்கு உள்ளதா? உள்நுழைக")
    val dontHaveAccount: String get() = t("Don't have an account? Create one", "கணக்கு இல்லையா? புதிய கணக்கு தொடங்குங்கள்")
    val signOut: String get() = t("Sign Out", "வெளியேறு")
    val signOutConfirmTitle: String get() = t("Sign Out of GeoPulse?", "ஜியோபல்ஸ்-லிருந்து வெளியேறவா?")
    val signOutConfirmText: String get() = t(
        "You will need your registered credentials to access your satellite farm telemetry.",
        "உங்கள் செயற்கைக்கோள் பண்ணை தகவல்களை அணுக மீண்டும் உள்நுழைய வேண்டும்."
    )
    val cancel: String get() = t("Cancel", "ரத்து செய்")
    val confirm: String get() = t("Confirm", "உறுதிசெய்")
    val save: String get() = t("Save Changes", "சேமிக்கவும்")
    val close: String get() = t("Close", "மூடு")

    // --- Bottom Navigation & Main Tabs ---
    val tabOverview: String get() = t("Overview", "மேலோட்டம்")
    val tabGisMap: String get() = t("GIS Map", "வரைபடம்")
    val tabTasks: String get() = t("Advisory", "பணிகள்")
    val tabProfile: String get() = t("Settings", "அமைப்புகள்")

    // --- Screen Titles & Subtitles (Screens 7-30) ---
    val screen7Title: String get() = t("GeoPulse Farm Dashboard", "ஜியோபல்ஸ் பண்ணை டாஷ்போர்டு")
    val screen7Subtitle: String get() = t("Live satellite telemetry, soil moisture & farm advisories", "செயற்கைக்கோள் கண்காணிப்பு, மண் ஈரப்பதம் & ஆலோசனைகள்")

    val screen8Title: String get() = t("Geospatial Field Map", "புவிசார் நில வரைபடம்")
    val screen8Subtitle: String get() = t("Sentinel-2 satellite observation & live GPS boundaries", "சென்டினல்-2 செயற்கைக்கோள் பார்வை & ஜிபிஎஸ் எல்லைகள்")

    val screen9Title: String get() = t("Precision Agriculture Advisory", "துல்லிய விவசாய ஆலோசனைகள்")
    val screen9Subtitle: String get() = t("Hyperlocal farm tasks, weather forecasts & spray timing", "பண்ணைப் பணிகள், வானிலை முன்னறிவிப்பு & மருந்து தெளிப்பு")

    val screen10Title: String get() = t("Farm Profile & Settings", "பண்ணை சுயவிவரம் & அமைப்புகள்")
    val screen10Subtitle: String get() = t("Farmer credentials, app appearance & farm parcels", "விவசாயி விவரங்கள், தோற்றம் & நிலத் தகவல்கள்")

    val screen11Title: String get() = t("Crop Health & Vegetation Vigor", "பயிர் நலம் & பசுமை குறியீடு (NDVI)")
    val screen11Subtitle: String get() = t("Copernicus Sentinel-2 10m multispectral vegetation analysis", "சென்டினல்-2 செயற்கைக்கோள் 10மீ பலவண்ண பயிர் ஆய்வு")

    val screen12Title: String get() = t("Soil Moisture & Root-Zone Physics", "மண் ஈரப்பதம் & வேர் மண்டல ஆய்வு")
    val screen12Subtitle: String get() = t("FAO-56 Soil water balance & available water capacity", "எஃப்.ஏ.ஓ-56 மண் நீர் இருப்பு & கிடைக்கும் ஈரப்பதம்")

    val screen13Title: String get() = t("Smart Irrigation Timing & Volume", "துல்லிய நீர்ப்பாசன வழிகாட்டி")
    val screen13Subtitle: String get() = t("Crop water requirements, litres needed & split scheduling", "பயிருக்கான நீர் தேவை, லிட்டர் கணக்கீடு & பாசன அட்டவணை")

    val screen14Title: String get() = t("Weather Forecast & Spray Safety Window", "வானிலை & மருந்து தெளிக்கும் நேரம்")
    val screen14Subtitle: String get() = t("Hyperlocal temperature, rain probability & wind drift", "மழை வாய்ப்பு, காற்றின் வேகம் & பாதுகாப்பான தெளிப்பு நேரம்")

    val screen15Title: String get() = t("Pest & Disease Diagnosis", "பூச்சி & நோய் மேலாண்மை")
    val screen15Subtitle: String get() = t("Integrated Pest Management (IPM) & symptom guide", "ஒருங்கிணைந்த பயிர்ப் பாதுகாப்பு & அறிகுறிகள் வழிகாட்டி")

    val screen16Title: String get() = t("Biomass Harvest & Yield Estimator", "விளைச்சல் & அறுவடை மதிப்பீடு")
    val screen16Subtitle: String get() = t("NDVI-based yield forecasting & biomass estimation", "பசுமை குறியீட்டு அடிப்படையிலான மகசூல் மதிப்பீடு")

    val screen17Title: String get() = t("GPS Field Boundary Survey & Land Area", "ஜிபிஎஸ் வயல் எல்லை & நிலப்பரப்பு")
    val screen17Subtitle: String get() = t("Interactive GPS polygon tracing & acre calculation", "ஜிபிஎஸ் மூலம் வயல் எல்லை வரைதல் & ஏக்கர் கணக்கீடு")

    val screen18Title: String get() = t("Crop Phenology Calendar & Certified Sowing", "பயிர் வளர்ச்சி நாள்காட்டி & விதைப்பு")
    val screen18Subtitle: String get() = t("Days after sowing, variety selection & seed rate", "விதைத்த பின் நாட்கள், ரகம் தேர்வு & விதை அளவு")

    val screen19Title: String get() = t("Field Operations & Machinery Tracking", "பண்ணைப் பணிகள் & உழவு இயந்திரங்கள்")
    val screen19Subtitle: String get() = t("Tractor hours, labor management & field operations", "டிராக்டர் பயன்பாடு, ஆட்கள் கூலி & பணிப் பதிவுகள்")

    val screen20Title: String get() = t("Split Fertilization & Nutrient Plan", "பிரித்து உரமிடுதல் & ஊட்டச்சத்து திட்டம்")
    val screen20Subtitle: String get() = t("Crop-specific guideline dosages for Urea, DAP, MOP", "யூரியா, டி.ஏ.பி, பொட்டாஷ் பயிர் வழிகாட்டி அளவுகள்")

    val screen21Title: String get() = t("Post-Harvest Storage & Logistics", "அறுவடைக்கு பின் தானிய சேமிப்பு")
    val screen21Subtitle: String get() = t("Moisture monitoring, grain drying & warehouse storage", "தானிய ஈரப்பதம், உலர் களம் & கிடங்கு பாதுகாப்பு")

    val screen22Title: String get() = t("Farm Budget & Financial Ledger", "பண்ணை வரவு செலவு கணக்கு")
    val screen22Subtitle: String get() = t("Input costs, labor expenses, market returns & profit", "உர செலவு, ஆட்கள் கூலி, வரவு & லாப நட்ட கணக்கீடு")

    val screen23Title: String get() = t("Multi-Farm Parcel Management", "பல வயல் நிலங்கள் மேலாண்மை")
    val screen23Subtitle: String get() = t("Switch and manage multiple agricultural parcels", "உங்கள் பல்வேறு வயல் நிலங்களை தேர்ந்தெடுத்து நிர்வகிக்கவும்")

    val screen24Title: String get() = t("Satellite Historical Observations", "செயற்கைக்கோள் வரலாற்று பதிவுகள்")
    val screen24Subtitle: String get() = t("Multi-temporal Sentinel-2 vegetation index trends", "காலவரிசைப்படியான பயிர் பசுமை மாற்றங்கள்")

    val screen25Title: String get() = t("Organic Soil Carbon & Composting", "மண் கரிம கார்பன் & இயற்கை உரம்")
    val screen25Subtitle: String get() = t("Soil organic matter, vermicompost & green manure", "மண் வளம், மண்புழு உரம் & பசுந்தாள் உர மேலாண்மை")

    val screen26Title: String get() = t("Agronomist Helpline & TNAU Advisory", "விவசாய வல்லுநர் உதவி மையம்")
    val screen26Subtitle: String get() = t("Kisan Call Center 1800-180-1551 & extension guidance", "கிசான் உதவி எண் 1800-180-1551 & வேளாண் அலுவலர் தொடர்பு")

    val screen27Title: String get() = t("AGMARKNET Mandi Arrival Rates", "அக்மார்க்நெட் மண்டி விலை நிலவரம்")
    val screen27Subtitle: String get() = t("Wholesale APMC arrival rates and benchmark pricing", "ஒழுங்குமுறை விற்பனைக்கூட மொத்த விலை நிலவரம்")

    val screen28Title: String get() = t("Severe Weather & Extreme Warning Center", "தீவிர வானிலை எச்சரிக்கை மையம்")
    val screen28Subtitle: String get() = t("Heavy rain flood risk, heatwave & wind hazard alerts", "கனமழை வெள்ள அபாயம், வெப்ப அலை & காற்று எச்சரிக்கை")

    val screen29Title: String get() = t("System Settings & Offline Cache", "அமைப்பு அமைப்புகள் & ஆஃப்லைன் சேமிப்பு")
    val screen29Subtitle: String get() = t("Language preference, theme mode, and offline cache", "மொழி தேர்வு, தோற்றம் & ஆஃப்லைன் வரைபட சேமிப்பு")

    val screen30Title: String get() = t("Data Privacy & Account Rights", "தரவு பாதுகாப்பு & விவசாயி உரிமைகள்")
    val screen30Subtitle: String get() = t("Export parcel data, privacy controls & account deletion", "நில வரைபட தரவு ஏற்றுமதி & கணக்கு நீக்க உரிமைகள்")

    // --- Action Labels ---
    val viewRelatedAdvisory: String get() = t("View Related Advisory", "தொடர்புடைய ஆலோசனையைக் காண்க")
    val returnToDashboard: String get() = t("Return to Farm Dashboard", "பண்ணை டாஷ்போர்டுக்கு திரும்புக")
    val viewFullMap: String get() = t("Open Interactive Map", "வரைபடத்தைத் திறக்கவும்")
    val refreshData: String get() = t("Refresh Live Telemetry", "தரவை புதுப்பிக்கவும்")
    val markAsRead: String get() = t("Mark All as Read", "அனைத்தையும் படித்ததாக குறிக்கவும்")
    val addField: String get() = t("Add New Field", "புதிய வயலைச் சேர்க்கவும்")
    val switchField: String get() = t("Switch Field", "வயலை மாற்றவும்")

    // --- Common Agricultural Terms ---
    val cropPaddy: String get() = t("Paddy (Rice)", "நெல்")
    val cropCotton: String get() = t("Cotton", "பருத்தி")
    val cropMaize: String get() = t("Maize (Corn)", "மக்காச்சோளம்")
    val cropGroundnut: String get() = t("Groundnut", "நிலக்கடலை")
    val cropSugarcane: String get() = t("Sugarcane", "கரும்பு")

    val soilLoam: String get() = t("Loam", "வண்டல் மண்")
    val soilClayLoam: String get() = t("Clay Loam", "களிமண் கலந்த வண்டல்")
    val soilSandyLoam: String get() = t("Sandy Loam", "மணல் கலந்த வண்டல்")
    val soilBlackCotton: String get() = t("Black Cotton Soil", "கரிசல் மண்")
    val soilRed: String get() = t("Red Soil", "செம்மண்")

    val stageInitial: String get() = t("Initial / Germination", "முளைப்புப் பருவம்")
    val stageTillering: String get() = t("Tillering Stage", "தூர்கட்டும் பருவம்")
    val stagePanicle: String get() = t("Panicle Initiation", "கதிர் உருவாகும் பருவம்")
    val stageFlowering: String get() = t("Flowering / Heading", "பூக்கும் பருவம்")
    val stageMaturity: String get() = t("Ripening / Maturity", "முதிர்ச்சிப் பருவம்")

    val irrigationNeeded: String get() = t("IRRIGATION RECOMMENDED", "நீர்ப்பாசனம் பரிந்துரைக்கப்படுகிறது")
    val noIrrigationNeeded: String get() = t("NO IRRIGATION NEEDED", "நீர்ப்பாசனம் தேவையில்லை")

    val spraySafe: String get() = t("SAFE TO SPRAY NOW", "மருந்து தெளிக்க உகந்த நேரம்")
    val sprayUnsafe: String get() = t("UNSAFE FOR SPRAYING", "மருந்து தெளிக்க உகந்த நேரமல்ல")

    val languageToggleLabel: String get() = t("தமிழ்", "English")
    val englishLabel: String get() = t("English", "ஆங்கிலம்")
    val tamilLabel: String get() = t("Tamil (தமிழ்)", "தமிழ்")
    val farmerLanguagePrompt: String get() = t("Farmer Preferred Language", "விவசாயி விரும்பும் மொழி")

    fun getCropName(crop: String, isTamil: Boolean): String {
        val lower = crop.lowercase().trim()
        return when {
            lower.contains("paddy") || lower.contains("rice") -> if (isTamil) "நெல்" else "Paddy (Rice)"
            lower.contains("cotton") -> if (isTamil) "பருத்தி" else "Cotton"
            lower.contains("maize") || lower.contains("corn") -> if (isTamil) "மக்காச்சோளம்" else "Maize (Corn)"
            lower.contains("groundnut") || lower.contains("peanut") -> if (isTamil) "நிலக்கடலை" else "Groundnut"
            lower.contains("sugarcane") -> if (isTamil) "கரும்பு" else "Sugarcane"
            else -> crop
        }
    }

    fun getSoilName(soil: String, isTamil: Boolean): String {
        val lower = soil.lowercase().trim()
        return when {
            lower.contains("clay loam") -> if (isTamil) "களிமண் கலந்த வண்டல்" else "Clay Loam"
            lower.contains("sandy loam") -> if (isTamil) "மணல் கலந்த வண்டல்" else "Sandy Loam"
            lower.contains("black cotton") || lower.contains("black") -> if (isTamil) "கரிசல் மண்" else "Black Cotton"
            lower.contains("red") -> if (isTamil) "செம்மண்" else "Red Soil"
            lower.contains("loam") -> if (isTamil) "வண்டல் மண்" else "Loam"
            else -> soil
        }
    }
}
