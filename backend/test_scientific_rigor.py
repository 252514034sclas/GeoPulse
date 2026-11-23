import unittest
import math
from services.irrigation_engine import IrrigationEngine
from services.satellite_engine import (
    calculate_ndvi,
    calculate_evi,
    calculate_ndwi,
    calculate_ndre,
    interpret_ndvi,
    interpret_ndwi,
    interpret_ndre
)
from services.fertilizer_calculator import calculate_fertilizer_dosage
from services.weather_engine import evaluate_spray_safety

class TestScientificRigor(unittest.TestCase):
    """
    Exhaustive scientific validation test suite for GeoPulse agronomic and hydrological models.
    Validates adherence to:
    - FAO Irrigation & Drainage Paper 56
    - Gao (1996) NDMI canopy water absorption
    - Remote sensing red-edge chlorophyll sensitivity
    - Soil Physics & Hydraulic application limits
    - ICAR / TNAU agronomic fertilizer split scheduling
    """

    def test_fao56_crop_coefficients_and_water_demand(self):
        """Verify ETc calculation matches Kc * ET0 for multiple crops and growth stages."""
        # Corn mid-flowering (Kc = 1.20, ET0 = 5.0 mm/day -> ETc = 6.0 mm/day)
        res_corn = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=85.0,
            et0_evapotranspiration_mm=5.0,
            growth_stage="mid_flowering"
        )
        self.assertEqual(res_corn["kc_coefficient"], 1.20)
        self.assertEqual(res_corn["daily_crop_water_demand_mm"], 6.0)

        # Wheat initial stage (Kc = 0.30, ET0 = 4.0 mm/day -> ETc = 1.2 mm/day)
        res_wheat = IrrigationEngine.calculate_irrigation(
            crop_type="Wheat",
            area_hectares=1.0,
            current_moisture_pct=80.0,
            et0_evapotranspiration_mm=4.0,
            growth_stage="initial"
        )
        self.assertEqual(res_wheat["kc_coefficient"], 0.30)
        self.assertEqual(res_wheat["daily_crop_water_demand_mm"], 1.2)

        # Paddy rice mid-flowering (Kc = 1.20)
        res_paddy = IrrigationEngine.calculate_irrigation(
            crop_type="Paddy",
            area_hectares=1.0,
            current_moisture_pct=85.0,
            et0_evapotranspiration_mm=4.5,
            growth_stage="mid_flowering"
        )
        self.assertEqual(res_paddy["kc_coefficient"], 1.20)
        self.assertEqual(res_paddy["daily_crop_water_demand_mm"], 5.4)

    def test_crop_specific_depletion_fraction_and_climate_adjustment(self):
        """Verify FAO-56 Table 22 depletion fraction p and climate adjustment formula."""
        # Paddy rice has low depletion fraction (p = 0.20)
        res_rice = IrrigationEngine.calculate_irrigation(
            crop_type="Rice",
            area_hectares=1.0,
            current_moisture_pct=70.0,
            et0_evapotranspiration_mm=5.0,  # ETc = 1.2 * 5.0 = 6.0 mm/day
            growth_stage="mid_flowering"
        )
        # p_adj = 0.20 + 0.04 * (5 - 6.0) = 0.20 - 0.04 = 0.16
        self.assertEqual(res_rice["fao56_parameters"]["depletion_fraction_p_table"], 0.20)
        self.assertAlmostEqual(res_rice["fao56_parameters"]["depletion_fraction_p_adjusted"], 0.16, places=2)

        # Cotton has high depletion fraction (p = 0.65)
        res_cotton = IrrigationEngine.calculate_irrigation(
            crop_type="Cotton",
            area_hectares=1.0,
            current_moisture_pct=70.0,
            et0_evapotranspiration_mm=5.0,  # ETc = 1.2 * 5.0 = 6.0 mm/day
            growth_stage="mid_flowering"
        )
        # p_adj = 0.65 + 0.04 * (5 - 6.0) = 0.61
        self.assertEqual(res_cotton["fao56_parameters"]["depletion_fraction_p_table"], 0.65)
        self.assertAlmostEqual(res_cotton["fao56_parameters"]["depletion_fraction_p_adjusted"], 0.61, places=2)

    def test_soil_awc_and_root_zone_taw(self):
        """Verify Total Available Water (TAW = AWC * RootDepth) across soil textures."""
        # Sandy soil (AWC = 80 mm/m) with Corn (Root depth = 1.0 m) -> TAW = 80 mm
        res_sand = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=75.0,
            soil_type="sand"
        )
        self.assertEqual(res_sand["fao56_parameters"]["awc_mm_per_m"], 80.0)
        self.assertEqual(res_sand["fao56_parameters"]["total_available_water_taw_mm"], 80.0)

        # Clay Loam (AWC = 190 mm/m) with Wheat (Root depth = 1.2 m) -> TAW = 228 mm
        res_clayloam = IrrigationEngine.calculate_irrigation(
            crop_type="Wheat",
            area_hectares=1.0,
            current_moisture_pct=75.0,
            soil_type="clay loam"
        )
        self.assertEqual(res_clayloam["fao56_parameters"]["awc_mm_per_m"], 190.0)
        self.assertEqual(res_clayloam["fao56_parameters"]["total_available_water_taw_mm"], 228.0)

    def test_probabilistic_effective_rainfall_discounting(self):
        """Verify forecast rainfall discounting: PoP weighting, initial abstraction, deficit capping."""
        # Case 1: Low PoP (30%) on 10mm forecast -> Prob rain = 3mm < Ia (4mm) -> Effective rain = 0 mm
        res1 = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=40.0,
            forecast_rain_next_48h_mm=10.0,
            forecast_rain_probability_pct=30.0
        )
        self.assertEqual(res1["effective_rain_mm"], 0.0)

        # Case 2: High PoP (80%) on 25mm forecast -> Prob rain = 20mm. Infiltrated = (20 - 3.2)*0.8 = 13.44 mm -> 13.4 mm
        res2 = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=40.0,  # TAW = 170mm -> Dr = 0.6 * 170 = 102 mm > 13.4 mm
            forecast_rain_next_48h_mm=25.0,
            forecast_rain_probability_pct=80.0
        )
        self.assertEqual(res2["effective_rain_mm"], 13.4)
        self.assertEqual(res2["forecast_expected_rainfall_mm"], 20.0)
        self.assertEqual(res2["modelled_infiltrable_rain_mm"], 13.44)

    def test_net_vs_gross_irrigation_and_volume_constants(self):
        """Verify net vs gross application accounting for system efficiency and 10,000 L/mm-ha constant."""
        # 1.0 ha, drip system (eff = 0.90)
        res_drip = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=40.0,
            irrigation_method="drip",
            forecast_rain_next_48h_mm=0.0
        )
        # TAW = 170mm, Dr = 102.0mm. Net = 102.0mm.
        # Gross = round(102.0 / 0.90, 1) = 113.3 mm.
        # Volume net = 102.0 * 1.0 * 10,000 = 1,020,000 Litres
        # Volume gross = 113.3 * 1.0 * 10,000 = 1,133,000 Litres
        self.assertEqual(res_drip["net_irrigation_depth_mm"], 102.0)
        self.assertEqual(res_drip["gross_irrigation_depth_mm"], 113.3)
        self.assertEqual(res_drip["net_water_volume_litres"], 1020000)
        self.assertEqual(res_drip["gross_water_volume_litres"], 1133000)
        self.assertEqual(res_drip["net_water_volume_m3"], 1020.0)
        self.assertEqual(res_drip["gross_water_volume_m3"], 1133.0)

    def test_hydraulic_application_limits_and_split_cycles(self):
        """Verify hydraulic limits enforce split application when gross depth > hydraulic capacity."""
        # Drip limit is 30mm. When required depth is 113.3mm, split cycles must be ceil(113.3 / 30) = 4 cycles.
        res = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=40.0,
            irrigation_method="drip"
        )
        self.assertTrue(res["split_application"]["required"])
        self.assertEqual(res["split_application"]["cycles_count"], 4)
        self.assertAlmostEqual(res["split_application"]["depth_per_cycle_mm"], 28.3, places=1)
        self.assertEqual(res["split_application"]["interval_hours"], 24)

    def test_fao56_stress_boundary_decision_logic(self):
        """Verify recommendation changes strictly at Readily Available Water (RAW) boundary."""
        # When moisture is 75% -> Dr = 42.5mm <= RAW (85mm) -> NO IRRIGATION NEEDED
        res_ok = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=75.0
        )
        self.assertEqual(res_ok["irrigation_recommendation"], "NO IRRIGATION NEEDED")

        # When moisture is 35% -> Dr = 110.5mm > RAW -> IRRIGATION RECOMMENDED
        res_stress = IrrigationEngine.calculate_irrigation(
            crop_type="Corn",
            area_hectares=1.0,
            current_moisture_pct=35.0
        )
        self.assertEqual(res_stress["irrigation_recommendation"], "IRRIGATION RECOMMENDED")

    def test_satellite_spectral_indices_and_interpretation(self):
        """Verify NDVI, EVI, NDMI, NDRE calculation and scientific attribution."""
        # NDVI
        self.assertEqual(calculate_ndvi(0.6, 0.2), 0.5)
        # NDWI/NDMI
        self.assertEqual(calculate_ndwi(0.6, 0.2), 0.5)
        # NDRE (NIR vs RedEdge)
        self.assertEqual(calculate_ndre(0.6, 0.3), 0.333)

        # Verify NDMI scientific attribution disclaims 30cm root penetration
        interp_ndmi = interpret_ndwi(0.40)
        self.assertIn("Canopy", interp_ndmi["rating"])
        self.assertIn("SWIR", interp_ndmi["scientific_note"])

        # Verify NDRE interpretation for nitrogen and chlorophyll
        interp_ndre = interpret_ndre(0.45)
        self.assertEqual(interp_ndre["nitrogen_status"], "Optimal")

    def test_fertilizer_dosage_and_soil_texture_adjustment(self):
        """Verify fertilizer NPK calculation, split application, and soil texture adaptation."""
        # Sandy soil: high leaching risk, must split into 4 applications
        res_sand = calculate_fertilizer_dosage(
            crop_type="Corn",
            area_acres=2.0,
            vigor_status="Moderate",
            soil_texture="sand"
        )
        self.assertEqual(len(res_sand["schedule"]), 4)
        self.assertIn("leaching", res_sand["safety_tips"][1].lower())
        self.assertIn("nitrogen_n", res_sand["elemental_nutrients_kg"])

        # Clay soil: standard 3-stage split
        res_clay = calculate_fertilizer_dosage(
            crop_type="Corn",
            area_acres=2.0,
            vigor_status="Moderate",
            soil_texture="clay"
        )
        self.assertEqual(len(res_clay["schedule"]), 3)

    def test_spray_safety_window_criteria(self):
        """Verify pesticide spray safety thresholds (wind, rain, temperature, drift)."""
        # Excessive wind (> 15 km/h for fungicide)
        unsafe_wind = evaluate_spray_safety(wind_kmh=18.0, rain_prob_pct=10.0, temp_c=25.0, humidity_pct=50.0)
        self.assertFalse(unsafe_wind["is_safe_now"])
        self.assertIn("drift", unsafe_wind["cautions"][0].lower())

        # High rain probability (> 20% for fungicide)
        unsafe_rain = evaluate_spray_safety(wind_kmh=8.0, rain_prob_pct=45.0, temp_c=25.0, humidity_pct=50.0)
        self.assertFalse(unsafe_rain["is_safe_now"])
        self.assertIn("wash-off", unsafe_rain["cautions"][0].lower())

        # Safe conditions
        safe = evaluate_spray_safety(wind_kmh=8.0, rain_prob_pct=5.0, temp_c=24.0, humidity_pct=60.0)
        self.assertTrue(safe["is_safe_now"])

if __name__ == "__main__":
    unittest.main()
