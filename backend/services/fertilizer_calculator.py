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
    elif "low" in vigor_status.lower() or "stressed" in vigor_status.lower() or "deficient" in vigor_status.lower():
        factor = 1.15
        tip = "Low canopy vigor detected. Boost split top-dressing of Urea to revive chlorophyll synthesis."
    else:
        factor = 1.00
        tip = "Moderate vigor. Follow standard balanced split application."

    # Soil texture adjustments for leaching and cation exchange capacity (CEC)
    is_sandy = "sand" in texture_lower
    is_clay = "clay" in texture_lower or "black" in texture_lower

    if is_sandy:
        # High percolation, low CEC; risk of nitrogen leaching into groundwater
        urea_per_acre = round(base_urea * factor, 1)
        soil_advice = "Coarse/Sandy texture has high leaching risk. Split nitrogen into 3 to 4 smaller doses rather than 2."
    elif is_clay:
        # High CEC, slower infiltration; high nutrient retention
        urea_per_acre = round(base_urea * factor, 1)
        soil_advice = "Fine/Clay texture has high nutrient retention. Ensure soil is moist during application to prevent ammonia volatilization."
    else:
        urea_per_acre = round(base_urea * factor, 1)
        soil_advice = "Medium loam texture provides balanced nutrient holding and aeration."

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
    else:
        schedule = [
            {"stage": "Basal Application (At Sowing)", "timing": "Day 0", "recommendation": f"Full dose DAP ({total_dap:.1f} kg) + 50% MOP ({total_mop * 0.5:.1f} kg)."},
            {"stage": "First Top-Dressing (Tillering Stage)", "timing": "Day 25 - 35", "recommendation": f"50% Urea ({total_urea * 0.5:.1f} kg) applied when soil is damp."},
            {"stage": "Second Top-Dressing (Pre-Flowering)", "timing": "Day 50 - 60", "recommendation": f"Remaining 50% Urea ({total_urea * 0.5:.1f} kg) + 50% MOP ({total_mop * 0.5:.1f} kg)."}
        ]

    safety_tips = [
        tip,
        soil_advice,
        "Never broadcast Urea onto dry, cracked soil; apply after light watering or before gentle forecasted rainfall.",
        "Maintain balanced NPK ratio: Excess nitrogen without phosphorus and potassium causes weak, brittle stems and lodging.",
        "Maximum single application: Do not apply more than 45 kg Urea per acre in a single pass to avoid root scorch."
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
        "safety_tips": safety_tips
    }
