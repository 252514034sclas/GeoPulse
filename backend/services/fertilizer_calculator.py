from typing import Dict, Any, List

def calculate_fertilizer_dosage(
    crop_type: str,
    area_acres: float,
    vigor_status: str = "Moderate",
    soil_dryness: str = "Normal",
    soil_texture: str = "loam"
) -> Dict[str, Any]:
    """
    Calculate tailored Urea, DAP, and MOP fertilizer requirements per acre and field total.
    Adjusts dosages based on satellite crop greenness (NDVI tier), soil texture leaching risks,
    and agronomic split application standards.
    """
    crop_lower = crop_type.lower()
    texture_lower = soil_texture.lower()

    # Base dosages per acre (kg) based on ICAR / TNAU recommended rates
    if "corn" in crop_lower or "maize" in crop_lower:
        base_urea = 50.0
        base_dap = 35.0
        base_mop = 20.0
    elif "wheat" in crop_lower:
        base_urea = 45.0
        base_dap = 30.0
        base_mop = 15.0
    elif "soybean" in crop_lower:
        base_urea = 15.0  # Legume fixes atmospheric nitrogen symbiotically
        base_dap = 40.0
        base_mop = 25.0
    elif "paddy" in crop_lower or "rice" in crop_lower:
        base_urea = 55.0
        base_dap = 35.0
        base_mop = 20.0
    elif "cotton" in crop_lower:
        base_urea = 45.0
        base_dap = 25.0
        base_mop = 25.0
    elif "sugarcane" in crop_lower:
        base_urea = 90.0
        base_dap = 45.0
        base_mop = 40.0
    else:
        base_urea = 40.0
        base_dap = 30.0
        base_mop = 20.0

    # Satellite Vigor adjustment factor
    if "optimal" in vigor_status.lower() or "high" in vigor_status.lower():
        factor = 0.90
        tip = "High canopy vigor detected by satellite. Avoid excessive nitrogen to prevent lodging and vegetative overgrowth."
        tip_ta = "செயற்கைக்கோள் மூலம் பயிர் பசுமை அதிகமாக கண்டறியப்பட்டுள்ளது. பயிர் சாய்வதைத் தடுக்க அதிகப்படியான தழைச்சத்தை தவிர்க்கவும்."
    elif "low" in vigor_status.lower() or "stressed" in vigor_status.lower() or "deficient" in vigor_status.lower():
        factor = 1.15
        tip = "Low canopy vigor detected. Boost split top-dressing of Urea to revive chlorophyll synthesis."
        tip_ta = "பயிர் பசுமை குறைவாக உள்ளது. பச்சையம் உற்பத்தியை அதிகரிக்க யூரியாவை பிரித்து மேலுரமாக இடவும்."
    else:
        factor = 1.00
        tip = "Moderate vigor. Follow standard balanced split application."
        tip_ta = "மிதமான பயிர் பசுமை. நிலையான சீரான உர அட்டவணையைப் பின்பற்றவும்."

    # Soil texture adjustments for leaching and cation exchange capacity (CEC)
    is_sandy = "sand" in texture_lower
    is_clay = "clay" in texture_lower or "black" in texture_lower

    if is_sandy:
        # High percolation, low CEC; risk of nitrogen leaching into groundwater
        urea_per_acre = round(base_urea * factor, 1)
        soil_advice = "Coarse/Sandy texture has high leaching risk. Split nitrogen into 3 to 4 smaller doses rather than 2."
        soil_advice_ta = "மணல் பாங்கான மண்ணில் உரக்கசிவு அபாயம் அதிகம். தழைச்சத்தை 3 முதல் 4 தவணைகளாக பிரித்து இடவும்."
    elif is_clay:
        # High CEC, slower infiltration; high nutrient retention
        urea_per_acre = round(base_urea * factor, 1)
        soil_advice = "Fine/Clay texture has high nutrient retention. Ensure soil is moist during application to prevent ammonia volatilization."
        soil_advice_ta = "களிமண் நிலத்தில் சத்துக்களை தக்கவைக்கும் திறன் அதிகம். உரமிடும் போது நிலத்தில் போதிய ஈரப்பதம் இருப்பதை உறுதி செய்யவும்."
    else:
        urea_per_acre = round(base_urea * factor, 1)
        soil_advice = "Medium loam texture provides balanced nutrient holding and aeration."
        soil_advice_ta = "வண்டல் மண் சீரான ஊட்டச்சத்து சேமிப்பு மற்றும் நல்ல காற்றோட்டத்தை வழங்குகிறது."

    dap_per_acre = round(base_dap, 1)
    mop_per_acre = round(base_mop, 1)

    total_urea = round(urea_per_acre * area_acres, 1)
    total_dap = round(dap_per_acre * area_acres, 1)
    total_mop = round(mop_per_acre * area_acres, 1)

    # Pure Elemental N-P-K content (Urea: 46% N; DAP: 18% N, 46% P2O5; MOP: 60% K2O)
    elemental_n = round(total_urea * 0.46 + total_dap * 0.18, 1)
    elemental_p2o5 = round(total_dap * 0.46, 1)
    elemental_k2o = round(total_mop * 0.60, 1)

    # Split Application Schedule tailored to soil texture
    if is_sandy:
        schedule = [
            {"stage": "Basal Application (At Sowing)", "timing": "Day 0", "recommendation": f"Full DAP ({total_dap:.1f} kg) + 33% MOP ({total_mop * 0.33:.1f} kg) + 25% Urea ({total_urea * 0.25:.1f} kg)."},
            {"stage": "First Top-Dressing (Early Vegetative)", "timing": "Day 20 - 25", "recommendation": f"25% Urea ({total_urea * 0.25:.1f} kg)."},
            {"stage": "Second Top-Dressing (Tillering / Grand Growth)", "timing": "Day 40 - 45", "recommendation": f"25% Urea ({total_urea * 0.25:.1f} kg) + 33% MOP ({total_mop * 0.33:.1f} kg)."},
            {"stage": "Third Top-Dressing (Panicle / Pre-Flowering)", "timing": "Day 60 - 65", "recommendation": f"Remaining 25% Urea ({total_urea * 0.25:.1f} kg) + 34% MOP ({total_mop * 0.34:.1f} kg)."}
        ]
        schedule_ta = [
            {"stage": "அடி உரம் (விதைப்பின் போது)", "timing": "நாள் 0", "recommendation": f"முழு டிஏபி ({total_dap:.1f} கிகி) + 33% பொட்டாஷ் ({total_mop * 0.33:.1f} கிகி) + 25% யூரியா ({total_urea * 0.25:.1f} கிகி)."},
            {"stage": "முதல் மேலுரம் (ஆரம்ப வளர்ச்சி நிலை)", "timing": "நாள் 20 - 25", "recommendation": f"25% யூரியா ({total_urea * 0.25:.1f} கிகி)."},
            {"stage": "இரண்டாம் மேலுரம் (தூர்கட்டும் நிலை)", "timing": "நாள் 40 - 45", "recommendation": f"25% யூரியா ({total_urea * 0.25:.1f} கிகி) + 33% பொட்டாஷ் ({total_mop * 0.33:.1f} கிகி)."},
            {"stage": "மூன்றாம் மேலுரம் (கதிர் உருவாகும் நிலை)", "timing": "நாள் 60 - 65", "recommendation": f"மீதமுள்ள 25% யூரியா ({total_urea * 0.25:.1f} கிகி) + 34% பொட்டாஷ் ({total_mop * 0.34:.1f} கிகி)."}
        ]
    else:
        schedule = [
            {"stage": "Basal Application (At Sowing)", "timing": "Day 0", "recommendation": f"Full dose DAP ({total_dap:.1f} kg) + 50% MOP ({total_mop * 0.5:.1f} kg)."},
            {"stage": "First Top-Dressing (Tillering Stage)", "timing": "Day 25 - 35", "recommendation": f"50% Urea ({total_urea * 0.5:.1f} kg) applied when soil is damp."},
            {"stage": "Second Top-Dressing (Pre-Flowering)", "timing": "Day 50 - 60", "recommendation": f"Remaining 50% Urea ({total_urea * 0.5:.1f} kg) + 50% MOP ({total_mop * 0.5:.1f} kg)."}
        ]
        schedule_ta = [
            {"stage": "அடி உரம் (விதைப்பின் போது)", "timing": "நாள் 0", "recommendation": f"முழு அளவு டிஏபி ({total_dap:.1f} கிகி) + 50% பொட்டாஷ் ({total_mop * 0.5:.1f} கிகி)."},
            {"stage": "முதல் மேலுரம் (தூர்கட்டும் நிலை)", "timing": "நாள் 25 - 35", "recommendation": f"மண் ஈரமாக இருக்கும்போது 50% யூரியா ({total_urea * 0.5:.1f} கிகி) இடவும்."},
            {"stage": "இரண்டாம் மேலுரம் (பூக்கும் முன் நிலை)", "timing": "நாள் 50 - 60", "recommendation": f"மீதமுள்ள 50% யூரியா ({total_urea * 0.5:.1f} கிகி) + 50% பொட்டாஷ் ({total_mop * 0.5:.1f} கிகி)."}
        ]

    safety_tips = [
        tip,
        soil_advice,
        "Never broadcast Urea onto dry, cracked soil; apply after light watering or before gentle forecasted rainfall.",
        "Maintain balanced NPK ratio: Excess nitrogen without phosphorus and potassium causes weak, brittle stems and lodging.",
        "Maximum single application: Do not apply more than 45 kg Urea per acre in a single pass to avoid root scorch."
    ]
    safety_tips_ta = [
        tip_ta,
        soil_advice_ta,
        "காய்ந்த அல்லது வெடிப்புற்ற நிலத்தில் நேரடியாக யூரியாவை இட வேண்டாம்; லேசான பாசனத்திற்குப் பின் அல்லது மிதமான மழைக்கு முன் இடவும்.",
        "சீரான NPK விகிதத்தைப் பராமரிக்கவும்: சாம்பல் மற்றும் மணிச்சத்து இல்லாமல் அதிக தழைச்சத்து இடுவது தண்டு பலவீனமடைந்து பயிர் சாய வழிவகுக்கும்.",
        "வேர் கருகலைத் தவிர்க்க ஒரு ஏக்கருக்கு ஒரே முறையில் 45 கிலோவுக்கு மேல் யூரியா இட வேண்டாம்."
    ]

    return {
        "crop_type": crop_type,
        "area_acres": area_acres,
        "soil_texture": soil_texture.title(),
        "urea_total_kg": total_urea,
        "dap_total_kg": total_dap,
        "mop_total_kg": total_mop,
        "urea_per_acre_kg": urea_per_acre,
        "dap_per_acre_kg": dap_per_acre,
        "mop_per_acre_kg": mop_per_acre,
        "elemental_nutrients_kg": {
            "nitrogen_n": elemental_n,
            "phosphorus_p2o5": elemental_p2o5,
            "potassium_k2o": elemental_k2o
        },
        "schedule": schedule,
        "schedule_ta": schedule_ta,
        "safety_tips": safety_tips,
        "safety_tips_ta": safety_tips_ta
    }
