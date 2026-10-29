from typing import Dict, Any, List

def calculate_fertilizer_dosage(
    crop_type: str,
    area_acres: float,
    vigor_status: str = "Moderate",
    soil_dryness: str = "Normal"
) -> Dict[str, Any]:
    """
    Calculate tailored Urea, DAP, and MOP fertilizer requirements per acre and field total.
    Adjusts dosages based on satellite crop greenness (NDVI tier).
    """
    crop_lower = crop_type.lower()

    # Base dosages per acre (kg)
    if "corn" in crop_lower or "maize" in crop_lower:
        base_urea = 50.0
        base_dap = 35.0
        base_mop = 20.0
    elif "wheat" in crop_lower:
        base_urea = 45.0
        base_dap = 30.0
        base_mop = 15.0
    elif "soybean" in crop_lower:
        base_urea = 15.0  # Legume fixes atmospheric nitrogen
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
    else:
        base_urea = 40.0
        base_dap = 30.0
        base_mop = 20.0

    # Satellite Vigor adjustment factor
    if "optimal" in vigor_status.lower() or "high" in vigor_status.lower():
        # High biomass; maintain normal or slightly reduced maintenance dose
        factor = 0.9
        tip = "High canopy vigor detected by satellite. Avoid excess nitrogen to prevent lodging and succulence."
    elif "low" in vigor_status.lower() or "stressed" in vigor_status.lower() or "deficient" in vigor_status.lower():
        # Nitrogen chlorosis / deficient; boost split dose
        factor = 1.2
        tip = "Low canopy vigor detected. Boost split top-dressing of Urea to revive chlorophyll synthesis."
    else:
        factor = 1.0
        tip = "Moderate vigor. Follow standard balanced split application."

    urea_per_acre = round(base_urea * factor, 1)
    dap_per_acre = round(base_dap, 1)
    mop_per_acre = round(base_mop, 1)

    total_urea = round(urea_per_acre * area_acres, 1)
    total_dap = round(dap_per_acre * area_acres, 1)
    total_mop = round(mop_per_acre * area_acres, 1)

    schedule = [
        {"stage": "Basal Application (At Sowing)", "timing": "Day 0", "recommendation": f"Full dose DAP ({total_dap * 0.8:.1f} kg) + 50% MOP ({total_mop * 0.5:.1f} kg)."},
        {"stage": "First Top-Dressing (Tillering Stage)", "timing": "Day 25 - 35", "recommendation": f"50% Urea ({total_urea * 0.5:.1f} kg) when soil is damp."},
        {"stage": "Second Top-Dressing (Pre-Flowering)", "timing": "Day 50 - 60", "recommendation": f"Remaining 50% Urea ({total_urea * 0.5:.1f} kg) + 50% MOP ({total_mop * 0.5:.1f} kg)."}
    ]

    safety_tips = [
        tip,
        "Never broadcast Urea onto dry cracked soil; apply after light watering or before forecasted rain.",
        "Maintain balanced NPK ratio: Excess Urea without DAP causes brittle stems."
    ]

    return {
        "crop_type": crop_type,
        "area_acres": area_acres,
        "urea_total_kg": total_urea,
        "dap_total_kg": total_dap,
        "mop_total_kg": total_mop,
        "urea_per_acre_kg": urea_per_acre,
        "dap_per_acre_kg": dap_per_acre,
        "mop_per_acre_kg": mop_per_acre,
        "schedule": schedule,
        "safety_tips": safety_tips
    }
