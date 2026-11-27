import os
import httpx
from datetime import datetime, timezone, timedelta
from typing import Dict, Any, Optional, List
from services.provider_health_monitor import ProviderHealthMonitor

OPEN_METEO_BASE_URL = os.getenv("OPEN_METEO_BASE_URL", "https://api.open-meteo.com/v1/forecast")

# WMO Weather Interpretation Codes (WW) with bilingual descriptions
WMO_WEATHER_CODES = {
    0: {"en": "Clear Sky", "ta": "தெளிவான வானம்"},
    1: {"en": "Mainly Clear", "ta": "பெரும்பாலும் தெளிவான வானம்"},
    2: {"en": "Partly Cloudy", "ta": "பகுதி மேகமூட்டம்"},
    3: {"en": "Overcast", "ta": "முழு மேகமூட்டம்"},
    45: {"en": "Foggy", "ta": "மூடுபனி"},
    48: {"en": "Depositing Rime Fog", "ta": "பனி மூட்டம்"},
    51: {"en": "Light Drizzle", "ta": "லேசான தூறல்"},
    53: {"en": "Moderate Drizzle", "ta": "மிதமான தூறல்"},
    55: {"en": "Dense Drizzle", "ta": "அடர்ந்த தூறல்"},
    61: {"en": "Slight Rain", "ta": "லேசான மழை"},
    63: {"en": "Moderate Rain", "ta": "மிதமான மழை"},
    65: {"en": "Heavy Rain", "ta": "கனமழை"},
    71: {"en": "Slight Snow Fall", "ta": "லேசான பனிப்பொழிவு"},
    73: {"en": "Moderate Snow Fall", "ta": "மிதமான பனிப்பொழிவு"},
    75: {"en": "Heavy Snow Fall", "ta": "கடும் பனிப்பொழிவு"},
    80: {"en": "Slight Rain Showers", "ta": "லேசான மழை பொழிவு"},
    81: {"en": "Moderate Rain Showers", "ta": "மிதமான மழை பொழிவு"},
    82: {"en": "Violent Rain Showers", "ta": "தீவிர மழை பொழிவு"},
    95: {"en": "Thunderstorm", "ta": "இடியுடன் கூடிய மழை"},
    96: {"en": "Thunderstorm with Slight Hail", "ta": "ஆலங்கட்டி இடிமழை"},
    99: {"en": "Thunderstorm with Heavy Hail", "ta": "தீவிர ஆலங்கட்டி இடிமழை"}
}


def validate_coordinates(latitude: Any, longitude: Any) -> bool:
    """Validate that coordinates are numeric and within standard WGS84 bounds."""
    if latitude is None or longitude is None:
        return False
    try:
        lat_f = float(latitude)
        lon_f = float(longitude)
    except (ValueError, TypeError):
        return False
    return (-90.0 <= lat_f <= 90.0) and (-180.0 <= lon_f <= 180.0)


def get_weather_condition_text(code: Optional[int]) -> tuple:
    """Return bilingual (English, Tamil) description for a WMO weather interpretation code."""
    if code is None or code not in WMO_WEATHER_CODES:
        return ("Unknown / Partly Cloudy", "பகுதி மேகமூட்டம்")
    entry = WMO_WEATHER_CODES[code]
    return (entry["en"], entry["ta"])


class OpenMeteoAdapter:
    """
    Live Adapter for Open-Meteo High-Resolution Numerical Weather Prediction (NWP) API.
    Retrieves atmospheric, land-surface, and agricultural meteorological parameters.
    Enforces strict coordinate validation, provenance attribution, cache management, and failure isolation.
    Model output: ECMWF IFS / DWD ICON. Never mislabels forecast models as in-situ sensor measurements.
    """

    # Persistent in-memory cache for last verified live observation per coordinate pair
    _verified_cache: Dict[str, Dict[str, Any]] = {}

    @classmethod
    def get_forecast(cls, latitude: float, longitude: float) -> Dict[str, Any]:
        """
        Fetch live meteorological conditions and forecast indicators for exact field coordinates.
        Coordinates must be in standard WGS84 bounds: lat in [-90.0, 90.0], lon in [-180.0, 180.0].
        """
        # 1. Coordinate Validation
        if latitude is None or longitude is None:
            return {
                "status": "INVALID_COORDINATES",
                "error": "Coordinates cannot be None.",
                "is_live": False,
                "is_cached": False,
                "temperature_c": None
            }

        try:
            lat_f = float(latitude)
            lon_f = float(longitude)
        except (ValueError, TypeError):
            return {
                "status": "INVALID_COORDINATES",
                "error": f"Coordinates must be numeric: lat={latitude}, lon={longitude}",
                "is_live": False,
                "is_cached": False,
                "temperature_c": None
            }

        if not (-90.0 <= lat_f <= 90.0) or not (-180.0 <= lon_f <= 180.0):
            return {
                "status": "INVALID_COORDINATES",
                "error": f"Coordinates out of bounds: lat={lat_f}, lon={lon_f}. Range: lat [-90, 90], lon [-180, 180].",
                "is_live": False,
                "is_cached": False,
                "temperature_c": None
            }

        coord_key = f"{round(lat_f, 3)},{round(lon_f, 3)}"

        params = {
            "latitude": lat_f,
            "longitude": lon_f,
            "current": [
                "temperature_2m",
                "apparent_temperature",
                "relative_humidity_2m",
                "precipitation",
                "rain",
                "weather_code",
                "cloud_cover",
                "wind_speed_10m",
                "wind_direction_10m",
                "wind_gusts_10m"
            ],
            "hourly": [
                "temperature_2m",
                "apparent_temperature",
                "precipitation_probability",
                "precipitation",
                "wind_speed_10m",
                "wind_gusts_10m",
                "cloud_cover",
                "uv_index",
                "weather_code",
                "et0_fao_evapotranspiration",
                "soil_moisture_0_to_7cm",
                "soil_moisture_7_to_28cm"
            ],
            "daily": [
                "temperature_2m_max",
                "temperature_2m_min",
                "apparent_temperature_max",
                "apparent_temperature_min",
                "precipitation_sum",
                "precipitation_probability_max",
                "wind_speed_10m_max",
                "wind_gusts_10m_max",
                "uv_index_max",
                "weather_code",
                "et0_fao_evapotranspiration"
            ],
            "timezone": "Asia/Kolkata",
            "forecast_days": 7
        }

        try:
            with httpx.Client(timeout=8.0) as client:
                resp = client.get(OPEN_METEO_BASE_URL, params=params)
                if resp.status_code == 200:
                    data = resp.json()
                    curr = data.get("current", {})
                    daily = data.get("daily", {})
                    hourly = data.get("hourly", {})

                    # Compute 24h and 48h expected rainfall accumulation
                    hourly_precip = hourly.get("precipitation", [])
                    rain_next_24h = sum(hourly_precip[:24]) if hourly_precip else 0.0
                    rain_next_48h = sum(hourly_precip[:48]) if hourly_precip else 0.0

                    et0_today = daily.get("et0_fao_evapotranspiration", [4.2])[0] or 4.2
                    pop_today = daily.get("precipitation_probability_max", [50.0])[0] if daily.get("precipitation_probability_max") else 50.0

                    obs_time_raw = curr.get("time", datetime.utcnow().strftime("%Y-%m-%dT%H:%M"))
                    utc_offset_sec = data.get("utc_offset_seconds", 19800)  # +05:30 default IST
                    tz_name = data.get("timezone", "Asia/Kolkata")

                    # Format ISO timestamp with explicit timezone offset (+05:30)
                    offset_hours = utc_offset_sec // 3600
                    offset_mins = abs(utc_offset_sec % 3600) // 60
                    tz_sign = "+" if utc_offset_sec >= 0 else "-"
                    tz_offset_str = f"{tz_sign}{abs(offset_hours):02d}:{offset_mins:02d}"
                    obs_time_iso = f"{obs_time_raw}:00{tz_offset_str}" if len(obs_time_raw) == 16 else f"{obs_time_raw}{tz_offset_str}"

                    topsoil_sm = hourly.get("soil_moisture_0_to_7cm", [0.32])[0] if hourly.get("soil_moisture_0_to_7cm") else 0.32
                    subsurface_sm = hourly.get("soil_moisture_7_to_28cm", [0.28])[0] if hourly.get("soil_moisture_7_to_28cm") else 0.28

                    weather_code = curr.get("weather_code", 0)
                    wmo_info = WMO_WEATHER_CODES.get(weather_code, {"en": "Fair Weather", "ta": "சாதகமான வானிலை"})

                    temp_c = curr.get("temperature_2m", 28.0)
                    apparent_temp_c = curr.get("apparent_temperature", temp_c)
                    temp_min_c = daily.get("temperature_2m_min", [22.0])[0] if daily.get("temperature_2m_min") else 22.0
                    temp_max_c = daily.get("temperature_2m_max", [32.0])[0] if daily.get("temperature_2m_max") else 32.0
                    uv_idx = daily.get("uv_index_max", [5.0])[0] if daily.get("uv_index_max") else 5.0
                    wind_speed = curr.get("wind_speed_10m", 8.5)
                    wind_gusts = curr.get("wind_gusts_10m", wind_speed)

                    # Build hourly forecast preview (next 24 hours)
                    hourly_times = hourly.get("time", [])[:24]
                    hourly_temps = hourly.get("temperature_2m", [])[:24]
                    hourly_pops = hourly.get("precipitation_probability", [])[:24]
                    hourly_winds = hourly.get("wind_speed_10m", [])[:24]
                    hourly_codes = hourly.get("weather_code", [])[:24]

                    hourly_list = []
                    for i in range(len(hourly_times)):
                        c_code = hourly_codes[i] if i < len(hourly_codes) else 0
                        w_sub = WMO_WEATHER_CODES.get(c_code, {"en": "Clear", "ta": "தெளிவானது"})
                        hourly_list.append({
                            "time": hourly_times[i],
                            "temperature_c": hourly_temps[i] if i < len(hourly_temps) else temp_c,
                            "precipitation_mm": hourly_precip[i] if i < len(hourly_precip) else 0.0,
                            "precipitation_prob_pct": hourly_pops[i] if i < len(hourly_pops) else 0.0,
                            "wind_speed_kmh": hourly_winds[i] if i < len(hourly_winds) else wind_speed,
                            "condition_text": w_sub["en"],
                            "condition_text_ta": w_sub["ta"]
                        })

                    # Build 7-day daily forecast summary
                    daily_dates = daily.get("time", [])
                    daily_maxs = daily.get("temperature_2m_max", [])
                    daily_mins = daily.get("temperature_2m_min", [])
                    daily_precips = daily.get("precipitation_sum", [])
                    daily_pops = daily.get("precipitation_probability_max", [])
                    daily_codes = daily.get("weather_code", [])

                    daily_list = []
                    for j in range(len(daily_dates)):
                        d_code = daily_codes[j] if j < len(daily_codes) else 0
                        d_sub = WMO_WEATHER_CODES.get(d_code, {"en": "Fair", "ta": "சாதகமானது"})
                        daily_list.append({
                            "date": daily_dates[j],
                            "temp_max_c": daily_maxs[j] if j < len(daily_maxs) else temp_max_c,
                            "temp_min_c": daily_mins[j] if j < len(daily_mins) else temp_min_c,
                            "precipitation_sum_mm": daily_precips[j] if j < len(daily_precips) else 0.0,
                            "precipitation_probability_pct": daily_pops[j] if j < len(daily_pops) else 0.0,
                            "condition_text": d_sub["en"],
                            "condition_text_ta": d_sub["ta"]
                        })

                    verified_record = {
                        "status": "LIVE_SUCCESS",
                        "provider": "Open-Meteo High-Resolution Numerical Weather Prediction (NWP)",
                        "attribution": "Weather data by Open-Meteo.com under CC BY 4.0",
                        "latitude": lat_f,
                        "longitude": lon_f,
                        "timestamp": obs_time_iso,
                        "timestamp_raw_local": obs_time_raw,
                        "timezone": tz_name,
                        "utc_offset_seconds": utc_offset_sec,
                        "data_freshness_label": ProviderHealthMonitor.calculate_freshness_label(obs_time_raw),
                        "temperature_c": temp_c,
                        "apparent_temperature_c": apparent_temp_c,
                        "temp_min_c": temp_min_c,
                        "temp_max_c": temp_max_c,
                        "humidity_percent": curr.get("relative_humidity_2m", 58.0),
                        "precipitation_mm": curr.get("precipitation", 0.0),
                        "rain_next_24h_mm": round(rain_next_24h, 1),
                        "rain_next_48h_mm": round(rain_next_48h, 1),
                        "precipitation_probability_pct": float(pop_today if pop_today is not None else 50.0),
                        "wind_speed_kmh": wind_speed,
                        "wind_direction_deg": curr.get("wind_direction_10m", 180),
                        "wind_gusts_kmh": wind_gusts,
                        "cloud_cover_pct": curr.get("cloud_cover", 0.0),
                        "uv_index": uv_idx,
                        "weather_code": weather_code,
                        "condition_text": wmo_info["en"],
                        "condition_text_ta": wmo_info["ta"],
                        "et0_fao_evapotranspiration_mm": round(et0_today, 2),
                        "soil_moisture_topsoil_0_7cm_m3m3": topsoil_sm,
                        "soil_moisture_subsurface_7_28cm_m3m3": subsurface_sm,
                        "soil_moisture_topsoil": topsoil_sm,
                        "soil_moisture_rootzone": subsurface_sm,
                        "soil_moisture_measurement_type": "NWP_LAND_SURFACE_MODELLED",
                        "soil_moisture_scientific_provenance": "ECMWF HTESSEL land-surface model via Open-Meteo (volumetric m³/m³; not an in-situ probe measurement)",
                        "data_provenance": "Forecast model output from ECMWF IFS / DWD ICON; not an in-situ physical weather station observation",
                        "is_live": True,
                        "is_cached": False,
                        "is_model_forecast": True,
                        "is_in_situ_observed": False,
                        "cached_at": datetime.utcnow().isoformat(),
                        "hourly_forecast": hourly_list,
                        "daily_forecast": daily_list
                    }
                    cls._verified_cache[coord_key] = verified_record
                    ProviderHealthMonitor.record_success("Open-Meteo NWP", acquisition_timestamp=obs_time_iso)
                    return verified_record
                else:
                    ProviderHealthMonitor.record_failure("Open-Meteo NWP", f"HTTP {resp.status_code}")
        except Exception as e:
            ProviderHealthMonitor.record_failure("Open-Meteo NWP", str(e))

        # 2. Cache Inspection: Check for genuine cached result from previous successful request
        if coord_key in cls._verified_cache:
            cached = cls._verified_cache[coord_key].copy()
            # Calculate cache staleness
            cached_at_str = cached.get("cached_at", "")
            is_stale = False
            if cached_at_str:
                try:
                    c_dt = datetime.fromisoformat(cached_at_str)
                    age_seconds = (datetime.utcnow() - c_dt).total_seconds()
                    if age_seconds > 86400:  # > 24 hours
                        is_stale = True
                except Exception:
                    pass

            cached["status"] = "CACHED_PREVIOUS_OBSERVATION"
            cached["is_live"] = False
            cached["is_cached"] = True
            cached["is_stale"] = is_stale
            if is_stale:
                cached["data_freshness_label"] = "Expired cache (>24h old - re-verifying upstream)"
            else:
                cached["data_freshness_label"] = "Cached data (Upstream server unreachable)"
            return cached

        # 3. If no verified observation exists, return unavailable state with clear error
        return {
            "status": "UNAVAILABLE",
            "provider": "Open-Meteo High-Resolution NWP",
            "attribution": "Open-Meteo API connection timeout",
            "latitude": lat_f,
            "longitude": lon_f,
            "timestamp": datetime.utcnow().isoformat(),
            "data_freshness_label": "Data temporarily unavailable",
            "temperature_c": None,
            "apparent_temperature_c": None,
            "temp_min_c": None,
            "temp_max_c": None,
            "humidity_percent": None,
            "precipitation_mm": 0.0,
            "rain_next_24h_mm": 0.0,
            "rain_next_48h_mm": 0.0,
            "precipitation_probability_pct": 0.0,
            "wind_speed_kmh": 0.0,
            "wind_direction_deg": 0,
            "wind_gusts_kmh": 0.0,
            "cloud_cover_pct": None,
            "uv_index": None,
            "weather_code": None,
            "condition_text": "Unavailable",
            "condition_text_ta": "தற்காலிகமாக கிடைக்கவில்லை",
            "et0_fao_evapotranspiration_mm": 4.0,
            "soil_moisture_topsoil": None,
            "soil_moisture_rootzone": None,
            "is_live": False,
            "is_cached": False,
            "is_model_forecast": True,
            "is_in_situ_observed": False,
            "error_note": "Upstream weather server unreachable. Retry when network is restored."
        }
