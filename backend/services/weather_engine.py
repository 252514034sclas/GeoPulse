from typing import Dict, Any, List

def evaluate_spray_safety(
    wind_kmh: float,
    rain_prob_pct: float,
    temp_c: float,
    humidity_pct: float,
    crop_type: str = "general",
    operation_type: str = "foliar_nutrient",  # "herbicide", "fungicide", "foliar_nutrient", "insecticide"
    wind_gusts_kmh: float = 0.0
) -> Dict[str, Any]:
    """
    Evaluate if conditions are safe for spraying based on operation-specific agronomic criteria:
    - Herbicide: Sensitive to drift; wind must be < 10 km/h.
    - Fungicide/Nutrients: Wind < 15 km/h, Rain < 20%, Temp 18-32°C.
    - Extreme Heat (> 33°C): Accelerates vaporization and can scorch tender foliage.
    - Wind Gusts (> 20 km/h): Triggers off-target drift even if 10m average wind is calm.
    - Low Temp (< 12°C): Inhibits systemic chemical absorption.
    """
    cautions = []
    cautions_ta = []
    is_safe = True

    op = operation_type.lower()
    max_wind = 10.0 if "herbicide" in op else 15.0
    max_gusts = 14.0 if "herbicide" in op else 21.0
    max_temp = 30.0 if "herbicide" in op else 33.0
    max_rain = 15.0 if "fungicide" in op else 25.0

    if wind_kmh > max_wind:
        is_safe = False
        cautions.append(f"High wind ({wind_kmh:.1f} km/h > {max_wind} km/h threshold) causes off-target spray drift.")
        cautions_ta.append(f"அதிவேக காற்று ({wind_kmh:.1f} கி.மீ/மணி > {max_wind} கி.மீ/மணி வரம்பு) மருந்தை காற்றில் அடித்துச் சென்று விரயமாக்கும்.")
    elif wind_kmh < 3.0:
        cautions.append("Very low wind (< 3 km/h) indicates thermal inversion risk; droplets may remain suspended.")
        cautions_ta.append("மிகக் குறைந்த காற்று (< 3 கி.மீ/மணி) துளிகள் காற்றில் மிதந்து வீணாக வழிவகுக்கும்.")

    if wind_gusts_kmh > max_gusts:
        is_safe = False
        cautions.append(f"Peak wind gusts ({wind_gusts_kmh:.1f} km/h > {max_gusts} km/h threshold) cause erratic drift pulses.")
        cautions_ta.append(f"பலத்த காற்று வீச்சு ({wind_gusts_kmh:.1f} கி.மீ/மணி > {max_gusts} கி.மீ/மணி வரம்பு) மருந்து விலகலை ஏற்படுத்தும்.")

    if rain_prob_pct > max_rain:
        is_safe = False
        cautions.append(f"Rain probability is {rain_prob_pct:.0f}% (threshold: {max_rain}%). Risk of chemical wash-off.")
        cautions_ta.append(f"மழை வாய்ப்பு {rain_prob_pct:.0f}% (வரம்பு: {max_rain}%). மருந்து நீரில் அடித்துச் செல்லப்படும் அபாயம் உள்ளது.")

    if temp_c > max_temp:
        is_safe = False
        cautions.append(f"High temperature ({temp_c:.1f}°C > {max_temp}°C) causes droplet vaporization and phytotoxic leaf scorch.")
        cautions_ta.append(f"அதிக வெப்பநிலை ({temp_c:.1f}°C > {max_temp}°C) மருந்தை ஆவியாக்கி இலைகளை கருகச் செய்யும் அபாயம்.")
    elif temp_c < 12.0:
        is_safe = False
        cautions.append(f"Low temperature ({temp_c:.1f}°C < 12°C) reduces stomatal uptake and chemical efficacy.")
        cautions_ta.append(f"குறைந்த வெப்பநிலை ({temp_c:.1f}°C < 12°C) தாவர உறிஞ்சும் திறனை குறைத்து மருந்தின் பலனைக் குறைக்கும்.")

    if humidity_pct < 35.0:
        cautions.append(f"Low humidity ({humidity_pct:.0f}%) accelerates droplet evaporation before plant absorption.")
        cautions_ta.append(f"குறைந்த ஈரப்பதம் ({humidity_pct:.0f}%) இலைகள் உறிஞ்சுவதற்கு முன்பே மருந்து ஆவியாக வழிவகுக்கும்.")

    if is_safe:
        verdict = f"Safe Spraying Window Open for {crop_type.title()}: Weather is favorable for {operation_type.replace('_', ' ')}."
        verdict_ta = f"{crop_type.title()} பயிருக்கு தெளிப்புக்கான சாதகமான சூழல்: {operation_type.replace('_', ' ')} பணிக்கு வானிலை உகந்தது."
        recommended_hours = ["06:00 AM - 09:30 AM", "04:30 PM - 06:45 PM"]
        recommended_hours_ta = ["காலை 06:00 - 09:30", "மாலை 04:30 - 06:45"]
    else:
        verdict = f"Unsafe for Spraying ({operation_type.replace('_', ' ').title()}): Postpone application to prevent drift or loss."
        verdict_ta = f"தெளிப்புக்கு உகந்த நேரமல்ல ({operation_type.replace('_', ' ').title()}): மருந்து விரயம் அல்லது காற்றில் அடித்துச் செல்லப்படுவதைத் தவிர்க்க ஒத்திவைக்கவும்."
        recommended_hours = ["Next Window: Tomorrow 06:00 AM - 08:30 AM (Calm Wind)"]
        recommended_hours_ta = ["அடுத்த உகந்த நேரம்: நாளை காலை 06:00 - 08:30 (மிதமான காற்று)"]

    return {
        "is_safe_now": is_safe,
        "crop_type": crop_type,
        "operation_type": operation_type,
        "current_wind_kmh": wind_kmh,
        "current_wind_gusts_kmh": wind_gusts_kmh,
        "current_humidity_pct": humidity_pct,
        "current_rain_prob_pct": rain_prob_pct,
        "temperature_c": temp_c,
        "verdict": verdict,
        "verdict_ta": verdict_ta,
        "recommended_hours": recommended_hours,
        "recommended_hours_ta": recommended_hours_ta,
        "cautions": cautions if cautions else ["No adverse weather warnings for spraying."],
        "cautions_ta": cautions_ta if cautions_ta else ["தெளிப்புக்கு சாதகமற்ற வானிலை எச்சரிக்கைகள் எதுவும் இல்லை."]
    }
