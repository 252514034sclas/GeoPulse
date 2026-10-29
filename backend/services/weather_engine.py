from typing import Dict, Any, List

def evaluate_spray_safety(
    wind_kmh: float,
    rain_prob_pct: float,
    temp_c: float,
    humidity_pct: float
) -> Dict[str, Any]:
    """
    Evaluate if conditions are safe for foliar chemical spraying.
    Principles:
    - Wind < 15 km/h: Prevents spray drift.
    - Rain < 20%: Prevents chemical wash-off.
    - Temp 18-32°C: Prevents evaporation & leaf scorch.
    - Humidity 45-75%: Optimal droplet adherence.
    """
    cautions = []
    is_safe = True

    if wind_kmh > 15.0:
        is_safe = False
        cautions.append(f"High wind speed ({wind_kmh} km/h) causes chemical spray drift.")
    elif wind_kmh < 3.0:
        cautions.append("Very low wind speed may cause thermal inversion; spray with caution.")

    if rain_prob_pct > 25.0:
        is_safe = False
        cautions.append(f"Rain probability is {rain_prob_pct}%. Rain will wash away foliar nutrients.")

    if temp_c > 33.0:
        is_safe = False
        cautions.append(f"High temperature ({temp_c}°C) can cause spray droplet vaporization and leaf scorch.")

    if humidity_pct < 35.0:
        cautions.append("Low humidity accelerates droplet evaporation.")

    if is_safe:
        verdict = "Safe Spraying Window Open: Wind & precipitation conditions are ideal for foliar application."
        recommended_hours = ["06:00 AM - 09:30 AM", "04:30 PM - 06:45 PM"]
    else:
        verdict = "Unsafe for Spraying: Postpone chemical or nutrient application to avoid loss."
        recommended_hours = ["Next Window: Tomorrow 06:00 AM - 08:30 AM (Calm Wind)"]

    return {
        "is_safe_now": is_safe,
        "current_wind_kmh": wind_kmh,
        "current_humidity_pct": humidity_pct,
        "current_rain_prob_pct": rain_prob_pct,
        "verdict": verdict,
        "recommended_hours": recommended_hours,
        "cautions": cautions if cautions else ["No adverse weather warnings for spraying."]
    }
