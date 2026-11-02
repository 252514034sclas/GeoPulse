from typing import Dict, Any, List

def evaluate_spray_safety(
    wind_kmh: float,
    rain_prob_pct: float,
    temp_c: float,
    humidity_pct: float,
    crop_type: str = "general",
    operation_type: str = "foliar_nutrient"  # "herbicide", "fungicide", "foliar_nutrient", "insecticide"
) -> Dict[str, Any]:
    """
    Evaluate if conditions are safe for spraying based on operation-specific agronomic criteria:
    - Herbicide: Sensitive to drift; wind must be < 10 km/h.
    - Fungicide/Nutrients: Wind < 15 km/h, Rain < 20%, Temp 18-32°C.
    - Extreme Heat (> 33°C): Accelerates vaporization and can scorch tender foliage.
    """
    cautions = []
    is_safe = True

    op = operation_type.lower()
    max_wind = 10.0 if "herbicide" in op else 15.0
    max_temp = 30.0 if "herbicide" in op else 33.0
    max_rain = 15.0 if "fungicide" in op else 25.0

    if wind_kmh > max_wind:
        is_safe = False
        cautions.append(f"High wind ({wind_kmh} km/h > {max_wind} km/h threshold) causes off-target spray drift.")
    elif wind_kmh < 3.0:
        cautions.append("Very low wind (< 3 km/h) indicates thermal inversion risk; droplets may remain suspended.")

    if rain_prob_pct > max_rain:
        is_safe = False
        cautions.append(f"Rain probability is {rain_prob_pct}% (threshold: {max_rain}%). Risk of chemical wash-off.")

    if temp_c > max_temp:
        is_safe = False
        cautions.append(f"High temperature ({temp_c}°C > {max_temp}°C) causes droplet vaporization and phytotoxic leaf scorch.")

    if humidity_pct < 35.0:
        cautions.append(f"Low humidity ({humidity_pct}%) accelerates droplet evaporation before plant absorption.")

    if is_safe:
        verdict = f"Safe Spraying Window Open for {crop_type.title()}: Weather is favorable for {operation_type.replace('_', ' ')}."
        recommended_hours = ["06:00 AM - 09:30 AM", "04:30 PM - 06:45 PM"]
    else:
        verdict = f"Unsafe for Spraying ({operation_type.replace('_', ' ').title()}): Postpone application to prevent drift or loss."
        recommended_hours = ["Next Window: Tomorrow 06:00 AM - 08:30 AM (Calm Wind)"]

    return {
        "is_safe_now": is_safe,
        "crop_type": crop_type,
        "operation_type": operation_type,
        "current_wind_kmh": wind_kmh,
        "current_humidity_pct": humidity_pct,
        "current_rain_prob_pct": rain_prob_pct,
        "verdict": verdict,
        "recommended_hours": recommended_hours,
        "cautions": cautions if cautions else ["No adverse weather warnings for spraying."]
    }
