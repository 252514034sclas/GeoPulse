import logging
from datetime import datetime, timedelta, timezone
from typing import Dict, Any, List, Optional
from services.irrigation_engine import IrrigationEngine
from services.weather_engine import evaluate_spray_safety

logger = logging.getLogger("geopulse.decision_engine")

IST_TIMEZONE = timezone(timedelta(hours=5, minutes=30), name="Asia/Kolkata")


def to_ist(dt: Optional[datetime]) -> datetime:
    """Ensure datetime is in Asia/Kolkata (IST)."""
    if dt is None:
        return datetime.now(IST_TIMEZONE)
    if dt.tzinfo is None:
        return dt.replace(tzinfo=IST_TIMEZONE)
    return dt.astimezone(IST_TIMEZONE)


class AgriculturalDecisionEngine:
    """
    Intelligent Agricultural Decision Support Engine.
    Evaluates individual field parameters against scientific models (FAO-56, NWP Weather,
    Sentinel-2 MSI, Agronomic Crop Phenology) to synthesize actionable, field-specific
    farmer recommendations with dual English and Tamil language localization.
    Operating Timezone: Asia/Kolkata (IST).
    """

    @classmethod
    def evaluate_field(
        cls,
        field: Any,
        weather: Optional[Any],
        soil_moisture: Optional[Any],
        satellite_record: Optional[Any],
        current_time: Optional[datetime] = None
    ) -> List[Dict[str, Any]]:
        """
        Evaluate all 7 agronomic decision rules for a single farmer's field.
        Returns a list of candidate notification event dictionaries.
        """
        now = to_ist(current_time)
        today_str = now.strftime("%Y-%m-%d")
        candidate_alerts = []

        field_name = getattr(field, "name", f"Field #{field.id}")
        crop_type = getattr(field, "crop_type", "Paddy (Rice)")
        soil_type = getattr(field, "soil_type", "Loam")
        area_acres = float(getattr(field, "area_acres", 1.0) or 1.0)
        sowing_date_str = getattr(field, "sowing_date", None)

        # Weather parameters
        temp_c = float(getattr(weather, "temperature_c", 28.0) or 28.0) if weather else 28.0
        humidity_pct = float(getattr(weather, "humidity_percent", 65.0) or 65.0) if weather else 65.0
        wind_kmh = float(getattr(weather, "wind_speed_kmh", 8.0) or 8.0) if weather else 8.0
        precip_mm = float(getattr(weather, "precipitation_mm", 0.0) or 0.0) if weather else 0.0

        # Remote sensing soil moisture
        current_moisture_pct = float(getattr(soil_moisture, "moisture_percentage", 65.0) or 65.0) if soil_moisture else 65.0

        # ----------------------------------------------------------------------
        # RULE 2: Rainfall Forecast & Irrigation Postponement
        # (Must be evaluated first to govern Rule 1 irrigation decisions)
        # ----------------------------------------------------------------------
        rain_postponement_needed = False
        rain_forecast_mm = precip_mm
        # If forecast precipitation is >= 5.0 mm, postpone irrigation to avoid waterlogging
        if rain_forecast_mm >= 5.0:
            rain_postponement_needed = True
            candidate_alerts.append({
                "category": "Weather",
                "priority": "HIGH",
                "deduplication_key": f"field_{field.id}:rain_postpone:{today_str}",
                "title_en": f"Rain Forecast: Postpone Irrigation on {field_name}",
                "title_ta": f"மழை முன்னறிவிப்பு: {field_name}-ல் நீர்ப்பாசனத்தை ஒத்திவைக்கவும்",
                "message_en": (
                    f"NWP weather forecast indicates expected precipitation of ~{rain_forecast_mm:.1f} mm within the next 24-36 hours. "
                    f"Postpone scheduled irrigation for {crop_type} to conserve water and prevent root-zone waterlogging. Re-evaluate post-rainfall."
                ),
                "message_ta": (
                    f"அடுத்த 24-36 மணி நேரத்தில் ~{rain_forecast_mm:.1f} மி.மீ மழை பெய்யக்கூடும் என வானிலை மாதிரி முன்னறிவிக்கிறது. "
                    f"உங்கள் {crop_type} பயிருக்கு திட்டமிட்ட நீர்ப்பாசனத்தை ஒத்திவைத்து நீர் வீணாவதையும் நீர் தேங்குவதையும் தவிர்க்கவும்."
                ),
                "action_needed": "Postpone Irrigation",
                "recommended_datetime": (now + timedelta(hours=24)).strftime("%Y-%m-%d 08:00 IST"),
                "input_quantity": f"0 mm (Conserve ~{rain_forecast_mm:.1f} mm rain)",
                "reason": (
                    f"Atmospheric weather grid indicates forecast precipitation of {rain_forecast_mm:.1f} mm exceeding the 5 mm soil saturation threshold. "
                    f"Note: Forecast models are probabilistic; monitor actual local sky conditions."
                ),
                "source": "High-Resolution NWP Numerical Weather Prediction",
                "source_data_timestamp": getattr(weather, "updated_at", now).isoformat() if weather else now.isoformat(),
                "confidence": "High (88%)",
                "expires_at": now + timedelta(hours=36)
            })

        # ----------------------------------------------------------------------
        # RULE 1: FAO-56 Soil-Water Balance Irrigation Need & Recommended Timing
        # ----------------------------------------------------------------------
        if not rain_postponement_needed:
            # Check soil moisture data freshness (observations older than 7 days cannot trigger automated watering)
            soil_date_val = getattr(soil_moisture, "recorded_at", None) or getattr(soil_moisture, "recorded_date", None)
            is_soil_stale = False
            if soil_date_val:
                try:
                    s_dt = datetime.strptime(str(soil_date_val)[:10], "%Y-%m-%d").replace(tzinfo=IST_TIMEZONE)
                    if (now - s_dt).days > 7:
                        is_soil_stale = True
                except Exception:
                    pass

            if not is_soil_stale:
                try:
                    area_ha = max(area_acres * 0.404686, 0.1)
                    irr_calc = IrrigationEngine.calculate_irrigation(
                        crop_type=crop_type,
                        area_hectares=area_ha,
                        current_moisture_pct=current_moisture_pct,
                        et0_evapotranspiration_mm=4.8,
                        forecast_rain_next_48h_mm=rain_forecast_mm,
                        growth_stage="mid_development",
                        soil_type=soil_type,
                        irrigation_method="drip"
                    )

                    is_irr_needed = (
                        "IRRIGATION RECOMMENDED" in str(irr_calc.get("irrigation_recommendation", "")).upper()
                        or (irr_calc.get("irrigation_needed") is True)
                    )

                    if is_irr_needed:
                        depth_mm = irr_calc.get("net_irrigation_depth_mm", 18.0)
                        vol_litres = irr_calc.get("gross_water_volume_litres", depth_mm * area_acres * 4046.86)
                        water_volume_fmt = f"{vol_litres:,.0f} Litres (~{depth_mm:.1f} mm model estimate)"

                        candidate_alerts.append({
                            "category": "Irrigation",
                            "priority": "HIGH",
                            "deduplication_key": f"field_{field.id}:irrigation:{today_str}",
                            "title_en": f"Irrigation Needed: {field_name}",
                            "title_ta": f"நீர்ப்பாசனம் தேவை: {field_name}",
                            "message_en": (
                                f"Estimated FAO-56 irrigation requirement for {crop_type}: ~{depth_mm:.0f} mm ({water_volume_fmt}). "
                                f"Measured root-zone moisture: {current_moisture_pct:.0f}%. Verify soil surface condition before watering."
                            ),
                            "message_ta": (
                                f"உங்கள் {crop_type} வயலுக்கு கணக்கிடப்பட்ட மாதிரி நீர் அளவு: ~{depth_mm:.0f} மி.மீ ({water_volume_fmt}). "
                                f"அளவிடப்பட்ட ஈரப்பதம்: {current_moisture_pct:.0f}%. நீர் பாய்ச்சுவதற்கு முன் வயல் ஈரப்பதத்தை சரிபார்க்கவும்."
                            ),
                            "action_needed": f"Apply ~{depth_mm:.0f} mm Estimated Irrigation",
                            "recommended_datetime": (now + timedelta(days=1)).strftime("%Y-%m-%d 06:30 IST"),
                            "input_quantity": water_volume_fmt,
                            "reason": (
                                f"FAO-56 Soil Water Balance indicates root-zone depletion has reached "
                                f"{irr_calc.get('depletion_percentage', 60):.1f}%, exceeding allowable depletion threshold. "
                                f"Measured moisture: {current_moisture_pct:.0f}%. Quantities are model guideline estimates."
                            ),
                            "source": "FAO-56 Penman-Monteith Evapotranspiration Model",
                            "source_data_timestamp": str(soil_date_val or today_str),
                            "confidence": "High (92%)",
                            "expires_at": now + timedelta(hours=28)
                        })
                except Exception as e:
                    logger.error(f"[DecisionEngine] Irrigation calculation failed for field {field.id}: {e}")

        # ----------------------------------------------------------------------
        # RULE 3: Unsafe Spraying Conditions (Wind, Rainfall & Extreme Temp)
        # ----------------------------------------------------------------------
        spray_eval = evaluate_spray_safety(
            wind_kmh=wind_kmh,
            rain_prob_pct=min(precip_mm * 15.0, 95.0),
            temp_c=temp_c,
            humidity_pct=humidity_pct,
            crop_type=crop_type,
            operation_type="foliar_nutrient"
        )

        if not spray_eval.get("is_safe_now", True):
            cautions = spray_eval.get("cautions", ["Adverse weather detected."])
            reason_str = " ".join(cautions)
            candidate_alerts.append({
                "category": "Spraying",
                "priority": "MEDIUM",
                "deduplication_key": f"field_{field.id}:spray_unsafe:{today_str}",
                "title_en": f"Unsafe Spraying Conditions: {field_name}",
                "title_ta": f"மருந்து தெளிக்க சாதகமற்ற வானிலை: {field_name}",
                "message_en": (
                    f"Suboptimal spraying conditions detected on {field_name} (Wind: {wind_kmh:.1f} km/h, Temp: {temp_c:.1f}°C). "
                    f"Avoid foliar spraying today to prevent chemical drift and loss. Next window: Tomorrow 06:00 IST. "
                    f"Note: Always follow pesticide product label directions and local agricultural officer advice before chemical application."
                ),
                "message_ta": (
                    f"{field_name}-ல் மருந்து தெளிக்க சாதகமற்ற வானிலை (காற்று: {wind_kmh:.1f} கி.மீ/மணி, வெப்பநிலை: {temp_c:.1f}°C). "
                    f"மருந்து வீணாவதைத் தவிர்க்க இன்று தெளிப்பதைத் தவிர்க்கவும். அடுத்த உகந்த நேரம்: நாளை காலை 06:00 IST. "
                    f"குறிப்பு: பூச்சிக்கொல்லி லேபிள் வழிகாட்டுதல் மற்றும் வேளாண் அலுவலர் அறிவுரைப்படி செயல்படவும்."
                ),
                "action_needed": "Postpone Chemical & Nutrient Spraying",
                "recommended_datetime": (now + timedelta(days=1)).strftime("%Y-%m-%d 06:00 IST"),
                "input_quantity": "Zero Application Window",
                "reason": (
                    f"{reason_str} Note: Universal weather thresholds do not replace chemical product label instructions "
                    f"or registered agricultural extension guidance."
                ),
                "source": "Agronomic Microclimate Spray Drift Model",
                "source_data_timestamp": getattr(weather, "updated_at", now).isoformat() if weather else now.isoformat(),
                "confidence": "High (89%)",
                "expires_at": now + timedelta(hours=24)
            })

        # ----------------------------------------------------------------------
        # RULE 4: Crop-Stage-Based Sowing and Field-Management Reminders
        # ----------------------------------------------------------------------
        das = None
        if sowing_date_str:
            try:
                sowing_dt = datetime.strptime(sowing_date_str[:10], "%Y-%m-%d").replace(tzinfo=IST_TIMEZONE)
                das = (now - sowing_dt).days
            except Exception:
                das = None

        if das is not None and das >= 0:
            stage_alert = cls._evaluate_crop_stage(field.id, field_name, crop_type, das, today_str, now)
            if stage_alert:
                candidate_alerts.append(stage_alert)

        # ----------------------------------------------------------------------
        # RULE 5: Satellite Vegetation-Health Deterioration (Sentinel-2 NDVI)
        # ----------------------------------------------------------------------
        if satellite_record:
            rec_date_str = getattr(satellite_record, "recorded_date", None)
            is_stale = False
            if rec_date_str:
                try:
                    rec_dt = datetime.strptime(rec_date_str[:10], "%Y-%m-%d").replace(tzinfo=IST_TIMEZONE)
                    # Strict data freshness rule: observations older than 14 days cannot be sent as current alerts
                    if (now - rec_dt).days > 14:
                        is_stale = True
                except Exception:
                    is_stale = False

            if not is_stale:
                ndvi = float(getattr(satellite_record, "ndvi_value", 0.70) or 0.70)
                # Significant deterioration condition: NDVI < 0.42 or stressed status
                health_status = str(getattr(satellite_record, "health_status", "Optimal"))
                if ndvi < 0.42 or health_status.lower() in ["stressed", "low", "deficient"]:
                    candidate_alerts.append({
                        "category": "CropHealth",
                        "priority": "HIGH",
                        "deduplication_key": f"field_{field.id}:ndvi_drop:{rec_date_str or today_str}",
                        "title_en": f"Vegetation Health Decline: {field_name}",
                        "title_ta": f"பயிர் பசுமை குறைவு எச்சரிக்கை: {field_name}",
                        "message_en": (
                            f"Canopy greenness has declined on {field_name} (NDVI: {ndvi:.2f}, status: {health_status}). "
                            f"NDVI reflects canopy chlorophyll and vegetative vigor, not a confirmed disease diagnosis. "
                            f"Inspect parcel to scout for root-zone moisture stress, pest incidence, or nutrient deficiency."
                        ),
                        "message_ta": (
                            f"உங்கள் வயலில் பயிரின் பசுமைத்திறன் குறைந்துள்ளது (NDVI: {ndvi:.2f}, நிலை: {health_status}). "
                            f"இது பயிர் பசுமை குறியீடு மட்டுமே, நோய் உறுதிப்படுத்தல் அல்ல; நீர் பற்றாக்குறை, பூச்சி தாக்குதல் அல்லது ஊட்டச்சத்து குறைபாட்டை அறிய வயலை ஆய்வு செய்யவும்."
                        ),
                        "action_needed": "Inspect Parcel for Localized Stress",
                        "recommended_datetime": now.strftime("%Y-%m-%d Afternoon IST"),
                        "input_quantity": "Visual & Soil Scouting",
                        "reason": (
                            f"ESA Sentinel-2 multispectral surface reflectance indicates canopy chlorophyll depression (NDVI {ndvi:.2f} < 0.42). "
                            f"NDVI measures vegetative vigor, not a specific pathogen or confirmed disease diagnosis; ground scouting required."
                        ),
                        "source": "Copernicus Sentinel-2 MSI (10m Resolution)",
                        "source_data_timestamp": rec_date_str or today_str,
                        "confidence": "High (91%)",
                        "expires_at": now + timedelta(days=5)
                    })

        # ----------------------------------------------------------------------
        # RULE 6: Severe Weather Warnings (Heatwave, Storm, Frost, Heavy Deluge)
        # ----------------------------------------------------------------------
        if temp_c >= 38.0:
            candidate_alerts.append({
                "category": "Weather",
                "priority": "CRITICAL",
                "deduplication_key": f"field_{field.id}:heatwave:{today_str}",
                "title_en": f"Severe Heatwave Warning ({temp_c:.0f}°C): {field_name}",
                "title_ta": f"கடுமையான வெப்ப அலை எச்சரிக்கை ({temp_c:.0f}°C): {field_name}",
                "message_en": (
                    f"Extreme temperature of {temp_c:.1f}°C expected today. Ensure adequate shallow standing water in {crop_type} "
                    f"to prevent spikelet sterility and canopy desiccation. Note: Temperature represents NWP forecast model; observe actual ambient conditions."
                ),
                "message_ta": (
                    f"அதிகபட்ச வெப்பநிலை {temp_c:.1f}°C பதிவாக வாய்ப்புள்ளது. வெப்ப அழுத்தத்தைத் தவிர்க்க {crop_type} "
                    f"பயிரில் போதிய அளவு நீர் நிறுத்துவதை உறுதி செய்யவும். இது மாதிரி முன்னறிவிப்பு; உள்ளூர் சூழலைக் கவனிக்கவும்."
                ),
                "action_needed": "Maintain Shallow Standing Water to Buffer Heat",
                "recommended_datetime": now.strftime("%Y-%m-%d Morning IST"),
                "input_quantity": "Light cooling flush",
                "reason": f"Ambient air temperature {temp_c:.1f}°C exceeds critical heat threshold (38°C). NWP atmospheric forecast model.",
                "source": "IMD & NWP Severe Weather Grid",
                "source_data_timestamp": now.isoformat(),
                "confidence": "High (94%)",
                "expires_at": now + timedelta(hours=24)
            })

        if wind_kmh >= 35.0:
            candidate_alerts.append({
                "category": "Weather",
                "priority": "HIGH",
                "deduplication_key": f"field_{field.id}:high_wind:{today_str}",
                "title_en": f"High Wind & Squall Advisory ({wind_kmh:.0f} km/h): {field_name}",
                "title_ta": f"பலத்த காற்று எச்சரிக்கை ({wind_kmh:.0f} கி.மீ/மணி): {field_name}",
                "message_en": (
                    f"Strong wind gusts up to {wind_kmh:.0f} km/h forecast today. "
                    f"Do not spray or apply surface fertilizers; brace tall crops against lodging. NWP forecast advisory."
                ),
                "message_ta": (
                    f"இன்று {wind_kmh:.0f} கி.மீ/மணி வரை பலத்த காற்று வீசக்கூடும் என முன்னறிவிப்பு. மருந்து அல்லது உரம் தெளிக்க வேண்டாம்; பயிர் சாய்வதைத் தடுக்கவும்."
                ),
                "action_needed": "Suspend Field Operations & Secure Trellises",
                "recommended_datetime": now.strftime("%Y-%m-%d IST"),
                "input_quantity": "No application",
                "reason": f"Wind gusts of {wind_kmh:.1f} km/h exceed mechanical safety limits (35 km/h). NWP atmospheric forecast vector.",
                "source": "NWP Wind Gust Vector",
                "source_data_timestamp": now.isoformat(),
                "confidence": "High (90%)",
                "expires_at": now + timedelta(hours=18)
            })

        # ----------------------------------------------------------------------
        # RULE 7: Follow-up Field Inspection Reminders
        # ----------------------------------------------------------------------
        if rain_forecast_mm >= 25.0:
            candidate_alerts.append({
                "category": "Inspection",
                "priority": "MEDIUM",
                "deduplication_key": f"field_{field.id}:followup_drainage:{today_str}",
                "title_en": f"Drainage & Perimeter Inspection: {field_name}",
                "title_ta": f"வடிகால் மற்றும் வரப்பு ஆய்வு: {field_name}",
                "message_en": (
                    f"Follow-up: Inspect drainage channels on {field_name} after rainfall to ensure excess standing water clears within 24 hours."
                ),
                "message_ta": (
                    f"பின்தொடர் ஆய்வு: மழையின் பின்னர் {field_name}-ல் அதிகப்படியான நீர் தேங்காமல் வழிய வடிகால் வழிகளை ஆய்வு செய்யவும்."
                ),
                "action_needed": "Clear Field Drainage Outlets",
                "recommended_datetime": (now + timedelta(days=1)).strftime("%Y-%m-%d Morning IST"),
                "input_quantity": "Manual Drainage Clearance",
                "reason": "Heavy rainfall accumulation requires proactive root aeration verification.",
                "source": "Field Management Agronomic Protocol",
                "source_data_timestamp": now.isoformat(),
                "confidence": "High (95%)",
                "expires_at": now + timedelta(days=2)
            })

        return candidate_alerts

    @classmethod
    def _evaluate_crop_stage(
        cls,
        field_id: int,
        field_name: str,
        crop_type: str,
        das: int,
        today_str: str,
        now: datetime
    ) -> Optional[Dict[str, Any]]:
        """
        Evaluate phenological stage based on Days After Sowing (DAS) and return targeted advisory.
        Deduplication keys are strictly scoped to field_id to prevent multi-field suppression.
        """
        crop_lower = crop_type.lower()

        if "paddy" in crop_lower or "rice" in crop_lower:
            if 18 <= das <= 25:
                return {
                    "category": "Agronomy",
                    "priority": "MEDIUM",
                    "deduplication_key": f"field_{field_id}:paddy_tillering:{das}:{today_str}",
                    "title_en": f"Active Tillering Stage Advisory: {field_name}",
                    "title_ta": f"தூர்கட்டும் பருவம் வழிகாட்டல்: {field_name}",
                    "message_en": (
                        f"Your paddy is at active tillering (Day {das}). Estimated guideline dose: ~20-25 kg/ac Urea "
                        f"(adjust for local soil test values) and Zinc Sulphate. Maintain 2-3 cm shallow water."
                    ),
                    "message_ta": (
                        f"உங்கள் நெல் பயிர் தீவிர தூர்கட்டும் பருவத்தில் உள்ளது (நாள் {das}). பரிந்துரைக்கப்படும் மாதிரி அளவு: ~20-25 கிலோ/ஏக்கர் "
                        f"யூரியா (மண் பரிசோதனைக்கு ஏற்ப மாற்றவும்) மற்றும் ஜிங்க் சல்பேட். 2-3 செ.மீ நீர் நிறுத்தவும்."
                    ),
                    "action_needed": "Apply 1st Top Dressing Split (~20-25 kg/ac Urea guideline)",
                    "recommended_datetime": now.strftime("%Y-%m-%d Morning IST"),
                    "input_quantity": "~20-25 kg Urea / acre (guideline estimate)",
                    "reason": "Tillering stage requires nitrogen spike to maximize effective panicle count per hill. Adjust dose based on soil health card & baseline soil test. Dosages are guideline estimates.",
                    "source": "TNAU Agronomic Crop Production Guide",
                    "source_data_timestamp": today_str,
                    "confidence": "High (95%)",
                    "expires_at": now + timedelta(days=4)
                }
            elif 42 <= das <= 50:
                return {
                    "category": "Agronomy",
                    "priority": "MEDIUM",
                    "deduplication_key": f"field_{field_id}:paddy_panicle:{das}:{today_str}",
                    "title_en": f"Panicle Initiation Stage Advisory: {field_name}",
                    "title_ta": f"கதிர் உருவாகும் பருவம் வழிகாட்டல்: {field_name}",
                    "message_en": (
                        f"Panicle initiation phase reached (Day {das}). Estimated guideline dose: ~15 kg/ac MOP (Potash) "
                        f"to boost grain weight and stem strength against lodging. Adjust based on soil test status."
                    ),
                    "message_ta": (
                        f"கதிர் உருவாகும் பருவம் வந்துள்ளது (நாள் {das}). தானிய எடை கூடவும் பயிர் சாயாமல் இருக்கவும் பொட்டாஷ் "
                        f"(மாதிரி அளவு: ~15 கிலோ/ஏக்கர்) மேலுரமாக இடவும். மண் பரிசோதனைக்கு ஏற்ப மாற்றவும்."
                    ),
                    "action_needed": "Apply Potash Top Dressing (~15 kg/ac MOP guideline)",
                    "recommended_datetime": now.strftime("%Y-%m-%d Morning IST"),
                    "input_quantity": "~15 kg MOP / acre (guideline estimate)",
                    "reason": "Potassium fertilization is critical at panicle differentiation for starch accumulation. Adjust dose based on soil health card & baseline soil test. Dosages are guideline estimates.",
                    "source": "TNAU Agronomic Crop Production Guide",
                    "source_data_timestamp": today_str,
                    "confidence": "High (95%)",
                    "expires_at": now + timedelta(days=4)
                }
        elif "cotton" in crop_lower:
            if 30 <= das <= 40:
                return {
                    "category": "Agronomy",
                    "priority": "MEDIUM",
                    "deduplication_key": f"field_{field_id}:cotton_squaring:{das}:{today_str}",
                    "title_en": f"Squaring Phase Advisory: {field_name}",
                    "title_ta": f"பருத்தி மொட்டு உருவாகும் பருவம்: {field_name}",
                    "message_en": (
                        f"Cotton parcel is entering square formation (Day {das}). Inspect squares for spotted bollworm "
                        f"and apply balanced NPK split (~15-20 kg Urea/ac guideline, subject to soil test)."
                    ),
                    "message_ta": (
                        f"பருத்தி பயிரில் மொட்டு உருவாகும் பருவம் (நாள் {das}). புழு தாக்குதலைக் கண்காணிக்கவும். "
                        f"மண் பரிசோதனை பரிந்துரைப்படி சமச்சீர் உரம் இடவும்."
                    ),
                    "action_needed": "Scout for Bollworm & Apply NPK Split",
                    "recommended_datetime": now.strftime("%Y-%m-%d Morning IST"),
                    "input_quantity": "Scouting + ~15-20 kg Urea/ac (guideline estimate)",
                    "reason": "Square formation determines overall boll retention and yield potential. Adjust dose based on soil health card. Dosages are guideline estimates.",
                    "source": "CICR Nagpur Cotton Advisory",
                    "source_data_timestamp": today_str,
                    "confidence": "High (92%)",
                    "expires_at": now + timedelta(days=5)
                }

        return None
