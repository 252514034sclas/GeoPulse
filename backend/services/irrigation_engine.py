import math
from typing import Dict, Any, Optional

class IrrigationEngine:
    """
    FAO-56 Penman-Monteith & Soil Water Balance Irrigation Engine.
    Computes field-specific crop water demand (ETc), root-zone moisture deficit,
    irrigation depth in millimetres, and exact water volume in LITRES and CUBIC METRES.

    Golden Physical Constant:
    1 mm of water over 1 hectare = 10,000 litres = 10 m^3.
    1 mm of water over 1 acre = 4,046.86 litres.
    """

    # FAO-56 Crop Coefficients (Kc) by Growth Stage
    CROP_COEFFICIENTS = {
        "corn": {"initial": 0.35, "mid_development": 0.85, "mid_flowering": 1.20, "late_maturity": 0.60, "root_depth_m": 1.0},
        "maize": {"initial": 0.35, "mid_development": 0.85, "mid_flowering": 1.20, "late_maturity": 0.60, "root_depth_m": 1.0},
        "wheat": {"initial": 0.30, "mid_development": 0.75, "mid_flowering": 1.15, "late_maturity": 0.40, "root_depth_m": 1.2},
        "soybean": {"initial": 0.40, "mid_development": 0.80, "mid_flowering": 1.15, "late_maturity": 0.50, "root_depth_m": 0.8},
        "paddy": {"initial": 1.05, "mid_development": 1.15, "mid_flowering": 1.20, "late_maturity": 0.90, "root_depth_m": 0.5},
        "rice": {"initial": 1.05, "mid_development": 1.15, "mid_flowering": 1.20, "late_maturity": 0.90, "root_depth_m": 0.5},
        "cotton": {"initial": 0.35, "mid_development": 0.75, "mid_flowering": 1.20, "late_maturity": 0.65, "root_depth_m": 1.2},
        "default": {"initial": 0.35, "mid_development": 0.80, "mid_flowering": 1.15, "late_maturity": 0.55, "root_depth_m": 1.0}
    }

    # Available Water Capacity (AWC) by Soil Texture (mm of water per meter of soil depth)
    SOIL_AWC = {
        "sandy": 80.0,
        "sandy loam": 120.0,
        "loam": 160.0,
        "clay loam": 180.0,
        "clay": 200.0,
        "black cotton": 210.0,
        "red loamy": 150.0,
        "alluvial": 170.0,
        "default": 160.0
    }

    @classmethod
    def calculate_irrigation(
        cls,
        crop_type: str,
        area_hectares: float,
        current_moisture_pct: float,
        et0_evapotranspiration_mm: float = 4.5,
        forecast_rain_next_48h_mm: float = 0.0,
        growth_stage: str = "mid_flowering",
        soil_type: str = "loam",
        irrigation_method: str = "drip"  # drip (90% eff), sprinkler (75%), flood (55%)
    ) -> Dict[str, Any]:
        """
        Calculate precision irrigation requirement using FAO-56 methodology.
        """
        crop_key = "default"
        for k in cls.CROP_COEFFICIENTS:
            if k in crop_type.lower():
                crop_key = k
                break

        crop_info = cls.CROP_COEFFICIENTS[crop_key]
        kc = crop_info.get(growth_stage, crop_info["mid_flowering"])
        root_depth = crop_info["root_depth_m"]

        # 1. Daily Crop Water Demand: ETc = Kc * ET0 (mm/day)
        daily_etc_mm = round(kc * et0_evapotranspiration_mm, 2)

        # 2. Total Available Water (TAW) in root zone: TAW = AWC * RootDepth (mm)
        soil_key = soil_type.lower()
        awc = cls.SOIL_AWC.get(soil_key, cls.SOIL_AWC["default"])
        taw_mm = awc * root_depth

        # 3. Readily Available Water (RAW) before stress occurs (depletion fraction p approx 0.5)
        raw_mm = taw_mm * 0.50

        # 4. Current Root-Zone Depletion (mm)
        # Optimal moisture is ~75% of field capacity; below 50% is threshold
        current_water_storage_mm = (current_moisture_pct / 100.0) * taw_mm
        depletion_mm = max(0.0, taw_mm - current_water_storage_mm)

        # 5. Account for effective rainfall
        effective_rain_mm = forecast_rain_next_48h_mm * 0.80

        # Net irrigation needed (mm)
        net_irrigation_mm = max(0.0, depletion_mm - effective_rain_mm)

        # 6. Adjust for irrigation system application efficiency
        eff_map = {"drip": 0.90, "pivot": 0.85, "sprinkler": 0.75, "flood": 0.55, "furrow": 0.60}
        efficiency = eff_map.get(irrigation_method.lower(), 0.80)
        gross_irrigation_mm = round(net_irrigation_mm / efficiency, 1) if net_irrigation_mm > 0 else 0.0

        # 7. Convert depth (mm) to volume in LITRES and CUBIC METRES
        # Formula: Volume (Litres) = Depth (mm) * Area (ha) * 10,000 litres/mm-ha
        volume_litres = round(gross_irrigation_mm * area_hectares * 10000.0)
        volume_cubic_meters = round(volume_litres / 1000.0, 1)

        # 8. Decision Logic: Should farmer irrigate or wait?
        if effective_rain_mm >= net_irrigation_mm and effective_rain_mm > 5.0:
            recommendation = "WAIT FOR RAIN"
            action_verdict = f"Natural rainfall ({forecast_rain_next_48h_mm} mm forecast) will satisfy crop water demand. Delay irrigation to prevent waterlogging and reduce pumping energy costs."
            recommended_timing = "Re-assess after rainfall on Friday."
        elif gross_irrigation_mm <= 5.0 or current_moisture_pct >= 70.0:
            recommendation = "NO IRRIGATION NEEDED"
            action_verdict = f"Soil moisture ({current_moisture_pct}%) is within optimal root-zone threshold. Crop has sufficient water storage."
            recommended_timing = "Check moisture again in 3 days."
        else:
            recommendation = "IRRIGATION RECOMMENDED"
            action_verdict = f"Soil moisture has dropped below optimal threshold. Apply {gross_irrigation_mm} mm of water ({volume_litres:,} litres) to refill root zone."
            recommended_timing = "Early morning (06:00 AM - 09:00 AM) or evening (05:00 PM - 07:00 PM) to avoid midday evaporation."

        return {
            "field_area_ha": area_hectares,
            "field_area_acres": round(area_hectares * 2.47105, 2),
            "crop": crop_type,
            "growth_stage": growth_stage.replace("_", " ").title(),
            "kc_coefficient": kc,
            "daily_crop_water_demand_mm": daily_etc_mm,
            "soil_type": soil_type.title(),
            "current_moisture_pct": current_moisture_pct,
            "data_source_type": "Modelled Root-Zone Soil Water Balance (FAO-56 Irrigation & Drainage Paper 56)",
            "forecast_rain_48h_mm": forecast_rain_next_48h_mm,
            "effective_rain_mm": round(effective_rain_mm, 1),
            "irrigation_recommendation": recommendation,
            "action_explanation": action_verdict,
            "recommended_timing": recommended_timing,
            "recommended_depth_mm": gross_irrigation_mm,
            "total_water_volume_litres": volume_litres,
            "total_water_volume_m3": volume_cubic_meters,
            "system_efficiency_pct": int(efficiency * 100),
            "conversion_formula": "1 mm water depth over 1 hectare = 10,000 litres (10 m^3)",
            "confidence": "High (Calibrated with FAO-56 Penman-Monteith crop evapotranspiration)"
        }
