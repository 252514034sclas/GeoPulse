import unittest
from datetime import datetime, date, time, timezone, timedelta
from typing import Optional

from services.agricultural_decision_engine import (
    AgriculturalDecisionEngine,
    IST_TIMEZONE,
    to_ist
)
from services.notification_scheduler import (
    is_in_quiet_hours,
    evaluate_user_fields,
    notification_scheduler
)


class MockField:
    def __init__(self, field_id: int, name: str, crop_type: str = "Paddy (Rice)", soil_type: str = "Loam", area_acres: float = 2.5, sowing_date: Optional[str] = None):
        self.id = field_id
        self.user_id = 1
        self.name = name
        self.crop_type = crop_type
        self.soil_type = soil_type
        self.area_acres = area_acres
        self.sowing_date = sowing_date


class MockWeather:
    def __init__(self, temp_c: float = 28.0, humidity_pct: float = 65.0, wind_kmh: float = 8.0, precip_mm: float = 0.0):
        self.temperature_c = temp_c
        self.humidity_percent = humidity_pct
        self.wind_speed_kmh = wind_kmh
        self.precipitation_mm = precip_mm
        self.updated_at = datetime.now(IST_TIMEZONE)


class MockSoilMoisture:
    def __init__(self, moisture_pct: float = 65.0, recorded_date: Optional[str] = None):
        self.moisture_percentage = moisture_pct
        self.recorded_date = recorded_date or datetime.now(IST_TIMEZONE).strftime("%Y-%m-%d")


class MockSatelliteRecord:
    def __init__(self, ndvi: float = 0.70, status: str = "Optimal", recorded_date: Optional[str] = None):
        self.ndvi_value = ndvi
        self.health_status = status
        self.recorded_date = recorded_date or datetime.now(IST_TIMEZONE).strftime("%Y-%m-%d")


class TestAgriculturalNotificationRules(unittest.TestCase):
    """
    Exhaustive deterministic test suite verifying every agricultural notification rule,
    threshold boundary, data freshness constraint, timing condition, and agronomic disclaimer.
    """

    def setUp(self):
        self.simulated_clock = datetime(2026, 11, 27, 7, 0, 0, tzinfo=IST_TIMEZONE)  # 07:00 IST
        self.field = MockField(field_id=101, name="Cauvery Delta Parcel", crop_type="Paddy (Rice)")

    # --------------------------------------------------------------------------
    # A. IRRIGATION TESTS
    # --------------------------------------------------------------------------
    def test_irrigation_depletion_trigger(self):
        """Rule 1: Verify irrigation alert is generated when soil moisture depletes."""
        weather = MockWeather(precip_mm=0.0)
        low_soil = MockSoilMoisture(moisture_pct=42.0)  # Depleted root-zone
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=low_soil,
            satellite_record=None,
            current_time=self.simulated_clock
        )
        irr_alerts = [a for a in alerts if a["category"] == "Irrigation"]
        self.assertEqual(len(irr_alerts), 1)
        alert = irr_alerts[0]
        self.assertEqual(alert["priority"], "HIGH")
        self.assertIn("Irrigation Needed", alert["title_en"])
        self.assertIn("Litres", alert["input_quantity"])
        self.assertIn("guideline estimates", alert["reason"])
        self.assertIn("FAO-56", alert["source"])

    def test_irrigation_adequate_moisture_no_alert(self):
        """Rule 1 Boundary: Adequate soil moisture must not trigger irrigation alert."""
        weather = MockWeather(precip_mm=0.0)
        high_soil = MockSoilMoisture(moisture_pct=85.0)  # Wet / saturated
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=high_soil,
            satellite_record=None,
            current_time=self.simulated_clock
        )
        irr_alerts = [a for a in alerts if a["category"] == "Irrigation"]
        self.assertEqual(len(irr_alerts), 0)

    def test_irrigation_suppressed_by_rain_forecast(self):
        """Rule 1 & 2: Rain forecast >= 5.0 mm must suppress irrigation and trigger Rain Postpone."""
        weather = MockWeather(precip_mm=6.5)  # 6.5 mm rain forecast
        low_soil = MockSoilMoisture(moisture_pct=40.0)  # Even if soil is dry!
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=low_soil,
            satellite_record=None,
            current_time=self.simulated_clock
        )
        irr_alerts = [a for a in alerts if a["category"] == "Irrigation"]
        rain_alerts = [a for a in alerts if a["category"] == "Weather" and "Postpone" in a["title_en"]]
        self.assertEqual(len(irr_alerts), 0, "Irrigation should be suppressed when rain >= 5.0 mm is forecast")
        self.assertEqual(len(rain_alerts), 1, "Rain postponement alert must be generated")

    def test_irrigation_stale_soil_moisture_suppression(self):
        """Rule 1 Freshness: Soil moisture readings older than 7 days must NOT trigger irrigation."""
        weather = MockWeather(precip_mm=0.0)
        eight_days_ago = (self.simulated_clock - timedelta(days=8)).strftime("%Y-%m-%d")
        stale_soil = MockSoilMoisture(moisture_pct=35.0, recorded_date=eight_days_ago)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=stale_soil,
            satellite_record=None,
            current_time=self.simulated_clock
        )
        irr_alerts = [a for a in alerts if a["category"] == "Irrigation"]
        self.assertEqual(len(irr_alerts), 0, "Stale soil moisture (> 7 days) must not generate irrigation recommendations")

    # --------------------------------------------------------------------------
    # B. RAINFALL FORECAST TESTS
    # --------------------------------------------------------------------------
    def test_rain_forecast_boundary_below_5mm(self):
        """Rule 2 Boundary: Forecast rain of 4.9 mm does not trigger postponement alert."""
        weather = MockWeather(precip_mm=4.9)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=75.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        rain_alerts = [a for a in alerts if "Postpone" in a.get("title_en", "")]
        self.assertEqual(len(rain_alerts), 0)

    def test_rain_forecast_boundary_at_5mm(self):
        """Rule 2 Boundary: Forecast rain of exactly 5.0 mm triggers postponement alert."""
        weather = MockWeather(precip_mm=5.0)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=75.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        rain_alerts = [a for a in alerts if "Postpone" in a.get("title_en", "")]
        self.assertEqual(len(rain_alerts), 1)
        self.assertIn("NWP", rain_alerts[0]["message_en"])
        self.assertIn("probabilistic", rain_alerts[0]["reason"])

    # --------------------------------------------------------------------------
    # C. SPRAYING SAFETY & PESTICIDE DISCLAIMER TESTS
    # --------------------------------------------------------------------------
    def test_spraying_safe_weather(self):
        """Rule 3: Optimal microclimate conditions (calm wind, mild temp) produce no spray warning."""
        weather = MockWeather(wind_kmh=8.0, temp_c=25.0, humidity_pct=65.0, precip_mm=0.0)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        spray_alerts = [a for a in alerts if a["category"] == "Spraying"]
        self.assertEqual(len(spray_alerts), 0)

    def test_spraying_high_wind_unsafe(self):
        """Rule 3: Wind speed exceeding threshold triggers spray warning with pesticide label disclaimer."""
        weather = MockWeather(wind_kmh=18.0, temp_c=28.0)  # Wind > 15 km/h
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        spray_alerts = [a for a in alerts if a["category"] == "Spraying"]
        self.assertEqual(len(spray_alerts), 1)
        alert = spray_alerts[0]
        self.assertIn("Unsafe Spraying Conditions", alert["title_en"])
        self.assertIn("pesticide product label directions", alert["message_en"])
        self.assertIn("not replace chemical product label", alert["reason"])

    # --------------------------------------------------------------------------
    # D. CROP HEALTH (SENTINEL-2 NDVI) TESTS
    # --------------------------------------------------------------------------
    def test_crop_health_ndvi_drop(self):
        """Rule 5: NDVI < 0.42 triggers vegetation decline alert with non-disease disclaimer."""
        weather = MockWeather()
        sat_rec = MockSatelliteRecord(ndvi=0.36, status="Stressed")
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=sat_rec,
            current_time=self.simulated_clock
        )
        health_alerts = [a for a in alerts if a["category"] == "CropHealth"]
        self.assertEqual(len(health_alerts), 1)
        alert = health_alerts[0]
        self.assertIn("Vegetation Health Decline", alert["title_en"])
        self.assertIn("not a confirmed disease diagnosis", alert["message_en"])
        self.assertIn("not a specific pathogen", alert["reason"])
        self.assertIn("Copernicus Sentinel-2", alert["source"])

    def test_crop_health_optimal_ndvi_no_alert(self):
        """Rule 5 Boundary: Healthy NDVI (0.74, Optimal) produces zero alerts."""
        weather = MockWeather()
        sat_rec = MockSatelliteRecord(ndvi=0.74, status="Optimal")
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=sat_rec,
            current_time=self.simulated_clock
        )
        health_alerts = [a for a in alerts if a["category"] == "CropHealth"]
        self.assertEqual(len(health_alerts), 0)

    def test_crop_health_stale_satellite_scene(self):
        """Rule 5 Freshness: Satellite observation older than 14 days must NOT trigger alert."""
        weather = MockWeather()
        fifteen_days_ago = (self.simulated_clock - timedelta(days=15)).strftime("%Y-%m-%d")
        sat_rec = MockSatelliteRecord(ndvi=0.35, status="Stressed", recorded_date=fifteen_days_ago)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=sat_rec,
            current_time=self.simulated_clock
        )
        health_alerts = [a for a in alerts if a["category"] == "CropHealth"]
        self.assertEqual(len(health_alerts), 0, "Satellite data older than 14 days must be suppressed")

    # --------------------------------------------------------------------------
    # E. SEVERE WEATHER TESTS
    # --------------------------------------------------------------------------
    def test_heatwave_boundary_below_38c(self):
        """Rule 6 Boundary: Temperature 37.9°C does not trigger heatwave alert."""
        weather = MockWeather(temp_c=37.9)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        heat_alerts = [a for a in alerts if "Heatwave" in a.get("title_en", "")]
        self.assertEqual(len(heat_alerts), 0)

    def test_heatwave_trigger_at_38c(self):
        """Rule 6 Boundary: Temperature 38.0°C triggers CRITICAL heatwave warning."""
        weather = MockWeather(temp_c=38.0)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        heat_alerts = [a for a in alerts if "Heatwave" in a.get("title_en", "")]
        self.assertEqual(len(heat_alerts), 1)
        self.assertEqual(heat_alerts[0]["priority"], "CRITICAL")
        self.assertIn("NWP forecast model", heat_alerts[0]["message_en"])

    def test_high_wind_trigger_at_35kmh(self):
        """Rule 6 Boundary: Wind speed 35.0 km/h triggers HIGH high wind advisory."""
        weather = MockWeather(wind_kmh=35.0)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=self.field,
            weather=weather,
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        wind_alerts = [a for a in alerts if "High Wind" in a.get("title_en", "")]
        self.assertEqual(len(wind_alerts), 1)
        self.assertEqual(wind_alerts[0]["priority"], "HIGH")

    # --------------------------------------------------------------------------
    # F. CROP STAGE & PHENOLOGY TESTS
    # --------------------------------------------------------------------------
    def test_crop_stage_paddy_tillering(self):
        """Rule 4: Paddy at DAS 22 generates Active Tillering advisory with guideline fertilizer dosage."""
        sowing = (self.simulated_clock - timedelta(days=22)).strftime("%Y-%m-%d")
        field = MockField(field_id=102, name="South Delta", crop_type="Paddy (Rice)", sowing_date=sowing)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=field,
            weather=MockWeather(),
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        ag_alerts = [a for a in alerts if a["category"] == "Agronomy"]
        self.assertEqual(len(ag_alerts), 1)
        alert = ag_alerts[0]
        self.assertIn("Active Tillering", alert["title_en"])
        self.assertIn("guideline estimate", alert["input_quantity"])
        self.assertIn("field_102:paddy_tillering", alert["deduplication_key"])

    def test_crop_stage_paddy_panicle(self):
        """Rule 4: Paddy at DAS 46 generates Panicle Initiation advisory with Potash dosage."""
        sowing = (self.simulated_clock - timedelta(days=46)).strftime("%Y-%m-%d")
        field = MockField(field_id=103, name="West Delta", crop_type="Paddy (Rice)", sowing_date=sowing)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=field,
            weather=MockWeather(),
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        ag_alerts = [a for a in alerts if a["category"] == "Agronomy"]
        self.assertEqual(len(ag_alerts), 1)
        self.assertIn("Panicle Initiation", ag_alerts[0]["title_en"])
        self.assertIn("MOP", ag_alerts[0]["input_quantity"])

    def test_crop_stage_cotton_squaring(self):
        """Rule 4: Cotton at DAS 34 generates Squaring Phase advisory with bollworm scouting."""
        sowing = (self.simulated_clock - timedelta(days=34)).strftime("%Y-%m-%d")
        field = MockField(field_id=104, name="Cotton Block", crop_type="Cotton", sowing_date=sowing)
        alerts = AgriculturalDecisionEngine.evaluate_field(
            field=field,
            weather=MockWeather(),
            soil_moisture=MockSoilMoisture(moisture_pct=70.0),
            satellite_record=None,
            current_time=self.simulated_clock
        )
        ag_alerts = [a for a in alerts if a["category"] == "Agronomy"]
        self.assertEqual(len(ag_alerts), 1)
        self.assertIn("Squaring Phase", ag_alerts[0]["title_en"])

    def test_multi_field_distinct_deduplication_keys(self):
        """Rule 4 Multi-field: Two paddy fields at DAS 20 must have distinct deduplication keys."""
        sowing = (self.simulated_clock - timedelta(days=20)).strftime("%Y-%m-%d")
        f1 = MockField(field_id=201, name="Field Alpha", crop_type="Paddy", sowing_date=sowing)
        f2 = MockField(field_id=202, name="Field Beta", crop_type="Paddy", sowing_date=sowing)

        a1 = AgriculturalDecisionEngine.evaluate_field(f1, MockWeather(), MockSoilMoisture(70.0), None, self.simulated_clock)
        a2 = AgriculturalDecisionEngine.evaluate_field(f2, MockWeather(), MockSoilMoisture(70.0), None, self.simulated_clock)

        key1 = [a for a in a1 if a["category"] == "Agronomy"][0]["deduplication_key"]
        key2 = [a for a in a2 if a["category"] == "Agronomy"][0]["deduplication_key"]

        self.assertIn("field_201", key1)
        self.assertIn("field_202", key2)
        self.assertNotEqual(key1, key2, "Deduplication keys between different fields must not collide")

    # --------------------------------------------------------------------------
    # G. TIMEZONE & QUIET HOURS TIMING TESTS
    # --------------------------------------------------------------------------
    def test_quiet_hours_in_ist(self):
        """Verify quiet hours (22:00 to 06:00 IST) correctly evaluate across day/night boundaries."""
        # 23:00 IST -> In quiet hours
        t_2300 = datetime(2026, 11, 27, 23, 0, 0, tzinfo=IST_TIMEZONE)
        self.assertTrue(is_in_quiet_hours("22:00", "06:00", t_2300))

        # 05:45 IST -> In quiet hours
        t_0545 = datetime(2026, 11, 27, 5, 45, 0, tzinfo=IST_TIMEZONE)
        self.assertTrue(is_in_quiet_hours("22:00", "06:00", t_0545))

        # 06:00 IST -> Boundary (morning operational run)
        t_0600 = datetime(2026, 11, 27, 6, 0, 0, tzinfo=IST_TIMEZONE)
        self.assertFalse(is_in_quiet_hours("22:00", "06:00", t_0600))

        # 14:30 IST -> Afternoon (not in quiet hours)
        t_1430 = datetime(2026, 11, 27, 14, 30, 0, tzinfo=IST_TIMEZONE)
        self.assertFalse(is_in_quiet_hours("22:00", "06:00", t_1430))

        # Test with UTC timestamp: 17:30 UTC = 23:00 IST -> Must be True!
        t_utc = datetime(2026, 11, 27, 17, 30, 0, tzinfo=timezone.utc)
        self.assertTrue(is_in_quiet_hours("22:00", "06:00", t_utc))

    # --------------------------------------------------------------------------
    # H. SCHEDULER ENGINE & STATUS TESTS
    # --------------------------------------------------------------------------
    def test_scheduler_operational_telemetry(self):
        """Verify the background scheduler daemon exposes valid operational status."""
        status = notification_scheduler.get_status()
        self.assertIn("scheduler_operational", status)
        self.assertEqual(status["timezone"], "Asia/Kolkata")
        self.assertEqual(status["daily_run_target"], "06:00 IST")
        self.assertIn("next_daily_run_at", status)
        self.assertEqual(status["deployment_status"], "LOCAL_DEVELOPMENT_STANDALONE")


if __name__ == "__main__":
    unittest.main()
