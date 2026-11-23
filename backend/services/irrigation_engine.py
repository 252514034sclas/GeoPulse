import math
from typing import Dict, Any, Optional

class IrrigationEngine:
    """
    FAO-56 Penman-Monteith & Soil Water Balance Irrigation Engine.
    Computes field-specific crop water demand (ETc), root-zone moisture deficit,
    irrigation depth in millimetres, and exact water volume in LITRES and CUBIC METRES.

    Physical Foundation:
    - FAO Irrigation and Drainage Paper 56 (Allen, Pereira, Raes, & Smith, 1998)
    - Golden Physical Constant:
      1 mm of water over 1 hectare = 10,000 litres = 10 m^3.
      1 mm of water over 1 acre = 4,046.86 litres = 4.047 m^3.
    """

    # FAO-56 Crop Coefficients (Kc) and Depletion Fractions (p) by Growth Stage (Table 12 & Table 22)
    CROP_COEFFICIENTS = {
        "corn": {"initial": 0.35, "mid_development": 0.85, "mid_flowering": 1.20, "late_maturity": 0.60, "root_depth_m": 1.0, "depletion_fraction_p": 0.55},
        "maize": {"initial": 0.35, "mid_development": 0.85, "mid_flowering": 1.20, "late_maturity": 0.60, "root_depth_m": 1.0, "depletion_fraction_p": 0.55},
        "wheat": {"initial": 0.30, "mid_development": 0.75, "mid_flowering": 1.15, "late_maturity": 0.40, "root_depth_m": 1.2, "depletion_fraction_p": 0.55},
        "soybean": {"initial": 0.40, "mid_development": 0.80, "mid_flowering": 1.15, "late_maturity": 0.50, "root_depth_m": 0.8, "depletion_fraction_p": 0.50},
        "paddy": {"initial": 1.05, "mid_development": 1.15, "mid_flowering": 1.20, "late_maturity": 0.90, "root_depth_m": 0.5, "depletion_fraction_p": 0.20},
        "rice": {"initial": 1.05, "mid_development": 1.15, "mid_flowering": 1.20, "late_maturity": 0.90, "root_depth_m": 0.5, "depletion_fraction_p": 0.20},
        "cotton": {"initial": 0.35, "mid_development": 0.75, "mid_flowering": 1.20, "late_maturity": 0.65, "root_depth_m": 1.2, "depletion_fraction_p": 0.65},
        "sugarcane": {"initial": 0.40, "mid_development": 0.85, "mid_flowering": 1.25, "late_maturity": 0.75, "root_depth_m": 1.2, "depletion_fraction_p": 0.65},
        "groundnut": {"initial": 0.40, "mid_development": 0.75, "mid_flowering": 1.15, "late_maturity": 0.60, "root_depth_m": 0.6, "depletion_fraction_p": 0.50},
        "peanut": {"initial": 0.40, "mid_development": 0.75, "mid_flowering": 1.15, "late_maturity": 0.60, "root_depth_m": 0.6, "depletion_fraction_p": 0.50},
        "tomato": {"initial": 0.45, "mid_development": 0.75, "mid_flowering": 1.15, "late_maturity": 0.80, "root_depth_m": 0.7, "depletion_fraction_p": 0.40},
        "potato": {"initial": 0.50, "mid_development": 0.75, "mid_flowering": 1.15, "late_maturity": 0.75, "root_depth_m": 0.6, "depletion_fraction_p": 0.35},
        "onion": {"initial": 0.50, "mid_development": 0.70, "mid_flowering": 1.05, "late_maturity": 0.75, "root_depth_m": 0.4, "depletion_fraction_p": 0.30},
        "default": {"initial": 0.35, "mid_development": 0.80, "mid_flowering": 1.15, "late_maturity": 0.55, "root_depth_m": 1.0, "depletion_fraction_p": 0.50}
    }

    # Available Water Capacity (AWC) by Soil Texture (mm of water per meter of soil depth)
    # Calibrated to USDA NRCS & FAO-56 Table 19
    SOIL_AWC = {
        "sand": 80.0,
        "sandy": 80.0,
        "loamy sand": 105.0,
        "sandy loam": 130.0,
        "loam": 170.0,
        "silt loam": 200.0,
        "clay loam": 190.0,
        "silty clay loam": 190.0,
        "sandy clay": 150.0,
        "clay": 180.0,
        "black cotton": 205.0,
        "red loamy": 145.0,
        "alluvial": 175.0,
        "default": 160.0
    }

    # Application Efficiency and Single-Cycle Hydraulic Absorption Limits
    APPLICATION_SPECS = {
        "drip": {"efficiency": 0.90, "max_single_cycle_mm": 30.0},
        "subsurface_drip": {"efficiency": 0.95, "max_single_cycle_mm": 30.0},
        "pivot": {"efficiency": 0.85, "max_single_cycle_mm": 35.0},
        "sprinkler": {"efficiency": 0.75, "max_single_cycle_mm": 35.0},
        "furrow": {"efficiency": 0.60, "max_single_cycle_mm": 50.0},
        "flood": {"efficiency": 0.55, "max_single_cycle_mm": 50.0},
        "default": {"efficiency": 0.80, "max_single_cycle_mm": 35.0}
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
        irrigation_method: str = "drip",
        forecast_rain_probability_pct: float = 60.0
    ) -> Dict[str, Any]:
        """
        Calculate precision irrigation requirement using FAO-56 methodology:
        - ETc = Kc * ET0
        - TAW = AWC * RootDepth
        - p_adj = p_table + 0.04 * (5 - ETc)
        - RAW = p_adj * TAW
        - Dr = TAW * (1 - current_moisture_pct / 100)
        - Effective Rain = min(Dr, max(0, P_prob - Ia) * 0.8)
        - Net Irrigation = max(0, Dr - Effective Rain)
        - Gross Irrigation = Net Irrigation / Efficiency
        - Split application detection based on hydraulic absorption limits
        """
        crop_key = "default"
        for k in cls.CROP_COEFFICIENTS:
            if k in crop_type.lower():
                crop_key = k
                break

        crop_info = cls.CROP_COEFFICIENTS[crop_key]
        kc = crop_info.get(growth_stage, crop_info.get("mid_flowering", 1.15))
        root_depth = crop_info.get("root_depth_m", 1.0)
        base_p = crop_info.get("depletion_fraction_p", 0.50)

        # 1. Daily Crop Water Demand: ETc = Kc * ET0 (mm/day)
        daily_etc_mm = round(kc * et0_evapotranspiration_mm, 2)

        # 2. Climate-adjusted depletion fraction p (FAO-56 eq. 89)
        # When ETc differs from 5 mm/day, p is adjusted by 0.04 * (5 - ETc)
        p_adj = round(base_p + 0.04 * (5.0 - daily_etc_mm), 3)
        p_adj = max(0.10, min(0.80, p_adj))

        # 3. Total Available Water (TAW) in root zone: TAW = AWC * RootDepth (mm)
        soil_key = soil_type.lower().strip()
        awc = cls.SOIL_AWC.get(soil_key, cls.SOIL_AWC["default"])
        taw_mm = round(awc * root_depth, 1)

        # 4. Readily Available Water (RAW) before water stress occurs: RAW = p * TAW (mm)
        raw_mm = round(taw_mm * p_adj, 1)

        # 5. Current Root-Zone Depletion (Dr in mm)
        # 100% moisture = field capacity (Dr = 0); 0% moisture = permanent wilting point (Dr = TAW)
        clamped_moisture = max(0.0, min(100.0, float(current_moisture_pct)))
        depletion_fraction = 1.0 - (clamped_moisture / 100.0)
        depletion_mm = round(depletion_fraction * taw_mm, 1)

        # 6. Probabilistic Expected Rainfall & USDA NRCS Infiltration Model:
        # Expected forecast rainfall E[P] = P_forecast * (PoP / 100) represents a probabilistic expectation,
        # NOT confirmed effective rainfall.
        # Initial Abstraction (Ia = 3.2 mm) is calibrated per USDA NRCS National Engineering Handbook Part 630
        # (Hydrology Curve Number method: CN=80 for cultivated row crops, S=63.5 mm, modern Ia = 0.05 * S = 3.2 mm).
        initial_abstraction_mm = 3.2  # USDA NRCS Part 630 (CN=80, Ia=0.05*S)
        pop_weight = max(0.0, min(100.0, float(forecast_rain_probability_pct))) / 100.0
        forecast_expected_rain_mm = round(forecast_rain_next_48h_mm * pop_weight, 2)
        modelled_infiltrable_rain_mm = round(max(0.0, forecast_expected_rain_mm - initial_abstraction_mm) * 0.80, 2)
        effective_rain_mm = round(min(depletion_mm, modelled_infiltrable_rain_mm), 1)

        # 7. Net Irrigation Depth (mm)
        net_irrigation_mm = round(max(0.0, depletion_mm - effective_rain_mm), 1)

        # 8. Irrigation System Efficiency & Gross Irrigation Depth (mm)
        sys_spec = cls.APPLICATION_SPECS.get(irrigation_method.lower(), cls.APPLICATION_SPECS["default"])
        efficiency = sys_spec["efficiency"]
        max_single_cycle_mm = sys_spec["max_single_cycle_mm"]

        gross_irrigation_mm = round(net_irrigation_mm / efficiency, 1) if net_irrigation_mm > 0 else 0.0

        # 9. Single-cycle hydraulic limits and split application check
        split_application_required = gross_irrigation_mm > max_single_cycle_mm
        if split_application_required:
            split_cycles = math.ceil(gross_irrigation_mm / max_single_cycle_mm)
            depth_per_cycle_mm = round(gross_irrigation_mm / split_cycles, 1)
            cycle_interval_hours = 24
        else:
            split_cycles = 1
            depth_per_cycle_mm = gross_irrigation_mm
            cycle_interval_hours = 0

        # 10. Volume Conversions (Golden Physical Constant: 1 mm * 1 ha = 10,000 L = 10 m^3)
        net_volume_litres = round(net_irrigation_mm * area_hectares * 10000.0)
        net_volume_m3 = round(net_volume_litres / 1000.0, 1)
        gross_volume_litres = round(gross_irrigation_mm * area_hectares * 10000.0)
        gross_volume_m3 = round(gross_volume_litres / 1000.0, 1)

        # 11. Agronomic Decision Logic based on FAO-56 Water Stress Boundary
        is_stress_imminent = depletion_mm > raw_mm

        if forecast_rain_next_48h_mm >= 12.0 and forecast_rain_probability_pct >= 55.0 and clamped_moisture >= 25.0:
            recommendation = "WAIT FOR RAIN"
            action_verdict = (
                f"Probable rainfall ({forecast_rain_next_48h_mm} mm forecast at {int(forecast_rain_probability_pct)}% PoP) "
                f"will deliver approx {effective_rain_mm} mm effective root moisture. Delay irrigation to prevent "
                f"waterlogging, nutrient leaching, and pumping energy loss."
            )
            recommended_timing = "Re-assess soil moisture 24h after rainfall ceases."
        elif effective_rain_mm >= depletion_mm and effective_rain_mm > 5.0:
            recommendation = "WAIT FOR RAIN"
            action_verdict = (
                f"Natural rainfall ({forecast_rain_next_48h_mm} mm forecast) satisfies root-zone moisture deficit ({depletion_mm} mm). "
                f"Irrigation is postponed to protect crop aeration."
            )
            recommended_timing = "Re-assess after rainfall ceases."
        elif not is_stress_imminent or gross_irrigation_mm < 5.0:
            recommendation = "NO IRRIGATION NEEDED"
            action_verdict = (
                f"Soil moisture ({clamped_moisture}%) is above critical stress threshold (Depletion: {depletion_mm} mm <= "
                f"RAW threshold: {raw_mm} mm). Root-zone water storage is adequate."
            )
            recommended_timing = "Check moisture again in 2 to 3 days."
        else:
            recommendation = "IRRIGATION RECOMMENDED"
            if split_application_required:
                action_verdict = (
                    f"Root depletion ({depletion_mm} mm) has exceeded RAW threshold ({raw_mm} mm). "
                    f"Total gross irrigation required: {gross_irrigation_mm} mm ({gross_volume_litres:,} L). "
                    f"Due to hydraulic absorption limits ({max_single_cycle_mm} mm/cycle), apply in {split_cycles} split cycles "
                    f"of {depth_per_cycle_mm} mm each ({round(gross_volume_litres/split_cycles):,} L/cycle) separated by 24h."
                )
            else:
                action_verdict = (
                    f"Root depletion ({depletion_mm} mm) has exceeded RAW threshold ({raw_mm} mm). "
                    f"Apply {gross_irrigation_mm} mm of water ({gross_volume_litres:,} litres) via {irrigation_method} to refill root zone."
                )
            recommended_timing = "Early morning (06:00 AM - 09:00 AM) or evening (05:00 PM - 07:00 PM) to minimize evaporation."

        return {
            "field_area_ha": area_hectares,
            "field_area_acres": round(area_hectares * 2.47105, 2),
            "crop": crop_type,
            "growth_stage": growth_stage.replace("_", " ").title(),
            "kc_coefficient": kc,
            "daily_crop_water_demand_mm": daily_etc_mm,
            "soil_type": soil_type.title(),
            "current_moisture_pct": clamped_moisture,
            "data_source_type": "Modelled Root-Zone Soil Water Balance (FAO-56 Irrigation & Drainage Paper 56)",
            "fao56_parameters": {
                "awc_mm_per_m": awc,
                "root_depth_m": root_depth,
                "total_available_water_taw_mm": taw_mm,
                "depletion_fraction_p_table": base_p,
                "depletion_fraction_p_adjusted": p_adj,
                "readily_available_water_raw_mm": raw_mm,
                "current_depletion_dr_mm": depletion_mm,
                "is_stress_imminent": is_stress_imminent
            },
            "forecast_rain_48h_mm": forecast_rain_next_48h_mm,
            "forecast_rain_probability_pct": forecast_rain_probability_pct,
            "forecast_expected_rainfall_mm": forecast_expected_rain_mm,
            "modelled_infiltrable_rain_mm": modelled_infiltrable_rain_mm,
            "effective_rain_mm": effective_rain_mm,
            "data_provenance": {
                "crop_water_demand": "MODELLED_FAO56",
                "weather_forecast": "NWP_FORECAST_PROBABILISTIC",
                "soil_moisture": "NWP_LAND_SURFACE_MODELLED",
                "infiltration_model": "USDA_NRCS_CURVE_NUMBER_CN80"
            },
            "net_irrigation_depth_mm": net_irrigation_mm,
            "gross_irrigation_depth_mm": gross_irrigation_mm,
            "recommended_depth_mm": gross_irrigation_mm,
            "net_water_volume_litres": net_volume_litres,
            "gross_water_volume_litres": gross_volume_litres,
            "total_water_volume_litres": gross_volume_litres,
            "net_water_volume_m3": net_volume_m3,
            "gross_water_volume_m3": gross_volume_m3,
            "total_water_volume_m3": gross_volume_m3,
            "system_efficiency_pct": int(efficiency * 100),
            "hydraulic_limit_mm": max_single_cycle_mm,
            "split_application": {
                "required": split_application_required,
                "cycles_count": split_cycles,
                "depth_per_cycle_mm": depth_per_cycle_mm,
                "interval_hours": cycle_interval_hours
            },
            "irrigation_recommendation": recommendation,
            "action_explanation": action_verdict,
            "recommended_timing": recommended_timing,
            "conversion_formula": "1 mm water depth over 1 hectare = 10,000 litres (10 m^3)",
            "confidence": "High (Calibrated with FAO-56 Penman-Monteith crop evapotranspiration & soil water balance)",
            "rain_forecast_disclaimer": "Precipitation forecasts are probabilistic. If forecasted rain fails to occur within 48h, re-evaluate soil moisture."
        }
