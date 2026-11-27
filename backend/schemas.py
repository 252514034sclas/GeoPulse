from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import datetime

# -------------------------------------------------------------
# Base Health & Status
# -------------------------------------------------------------
class HealthResponse(BaseModel):
    status: str
    version: str
    timestamp: str

# -------------------------------------------------------------
# Auth & User Schemas
# -------------------------------------------------------------
class UserCreate(BaseModel):
    email: str
    full_name: str
    password: str
    phone: Optional[str] = None
    farm_name: Optional[str] = "My Farm"
    total_area_ha: Optional[float] = 0.0
    preferred_language: Optional[str] = "en"
    theme_preference: Optional[str] = "SYSTEM"

class UserLogin(BaseModel):
    email: str
    password: str

class ForgotPasswordRequest(BaseModel):
    email: str

class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    phone: Optional[str] = None
    farm_name: Optional[str] = None
    total_area_ha: Optional[float] = None
    preferred_language: Optional[str] = None
    theme_preference: Optional[str] = None

class UserResponse(BaseModel):
    id: int
    email: str
    full_name: str
    phone: Optional[str] = None
    farm_name: str
    total_area_ha: float
    preferred_language: str
    theme_preference: str
    created_at: datetime

    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str
    user: UserResponse

class TokenData(BaseModel):
    email: Optional[str] = None

# -------------------------------------------------------------
# Field & Geospatial Schemas
# -------------------------------------------------------------
class FieldCreate(BaseModel):
    name: str
    crop_type: str = "Corn (Dent)"
    soil_type: Optional[str] = "Loam"
    area_acres: float = 120.4
    sowing_date: Optional[str] = "2026-04-15"
    latitude: float = 37.7749
    longitude: float = -122.4194
    boundary: Optional[str] = None

class FieldUpdate(BaseModel):
    name: Optional[str] = None
    crop_type: Optional[str] = None
    soil_type: Optional[str] = None
    area_acres: Optional[float] = None
    status: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    boundary: Optional[str] = None

class FieldSchema(BaseModel):
    id: int
    user_id: Optional[int] = None
    name: str
    crop_type: str
    soil_type: Optional[str] = "Loam"
    area_acres: float
    sowing_date: Optional[str] = None
    status: str
    latitude: float
    longitude: float
    boundary: Optional[str] = None
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True

# -------------------------------------------------------------
# Satellite Remote Sensing Schemas
# -------------------------------------------------------------
class SatelliteRecordSchema(BaseModel):
    id: int
    field_id: int
    recorded_date: str
    ndvi_value: float
    ndwi_value: float
    chlorophyll_spad: float
    canopy_cover_pct: float
    health_status: str
    layman_advice: str
    is_sample: bool
    disclaimer: str

    class Config:
        from_attributes = True

# Compatibility alias for legacy endpoint
VegetationHealthSchema = SatelliteRecordSchema

class SatelliteHistoryResponse(BaseModel):
    field_id: int
    field_name: str
    crop_type: str
    latest_ndvi: float
    growth_trend_pct: float
    history: List[SatelliteRecordSchema]

# -------------------------------------------------------------
# Weather & Spray Window Schemas
# -------------------------------------------------------------
class WeatherSchema(BaseModel):
    id: int
    location: str
    temperature_c: float
    apparent_temperature_c: Optional[float] = None
    humidity_percent: float
    precipitation_mm: float
    rain_next_24h_mm: Optional[float] = None
    rain_next_48h_mm: Optional[float] = None
    precipitation_probability_pct: Optional[float] = None
    wind_speed_kmh: float
    wind_direction_deg: Optional[float] = None
    wind_gusts_kmh: Optional[float] = None
    cloud_cover_pct: Optional[float] = None
    uv_index: Optional[float] = None
    temp_min_c: Optional[float] = None
    temp_max_c: Optional[float] = None
    weather_code: Optional[int] = None
    condition_text: Optional[str] = None
    condition_text_ta: Optional[str] = None
    spray_safe: bool
    spray_advice: str
    spray_advice_ta: Optional[str] = None
    status_message: str
    data_source: str
    data_freshness_label: Optional[str] = None
    is_live: bool = True
    is_cached: bool = False
    is_model_forecast: bool = True
    field_id: Optional[int] = None
    field_name: Optional[str] = None
    updated_at: datetime
    hourly_forecast: Optional[List[Dict[str, Any]]] = None
    daily_forecast: Optional[List[Dict[str, Any]]] = None

    class Config:
        from_attributes = True

class SprayWindowResponse(BaseModel):
    is_safe_now: bool
    current_wind_kmh: float
    current_humidity_pct: float
    current_rain_prob_pct: float
    verdict: str
    verdict_ta: Optional[str] = None
    recommended_hours: List[str]
    recommended_hours_ta: Optional[List[str]] = None
    cautions: List[str]
    cautions_ta: Optional[List[str]] = None
    current_wind_gusts_kmh: Optional[float] = None
    temperature_c: Optional[float] = None
    field_id: Optional[int] = None
    field_name: Optional[str] = None

# -------------------------------------------------------------
# Soil Moisture Schemas
# -------------------------------------------------------------
class SoilMoistureSchema(BaseModel):
    id: int
    field_id: int
    moisture_percentage: float
    depth_cm: float
    status: Optional[str] = "Optimal"
    watering_advice: Optional[str] = "Optimal moisture level."
    recorded_at: str
    data_source: str

    class Config:
        from_attributes = True

# -------------------------------------------------------------
# Alerts & Advisory Tasks Schemas
# -------------------------------------------------------------
class AlertSchema(BaseModel):
    id: int
    field_id: Optional[int] = None
    severity: str
    message: str
    category: Optional[str] = "Weather"
    timestamp: datetime
    is_resolved: bool

    class Config:
        from_attributes = True

class AdvisoryTaskCreate(BaseModel):
    field_id: Optional[int] = None
    task_type: str
    title: str
    description: str
    time_badge: Optional[str] = "Today"
    priority: Optional[str] = "High"

class AdvisoryTaskSchema(BaseModel):
    id: int
    user_id: Optional[int] = None
    field_id: Optional[int] = None
    task_type: str
    title: str
    description: str
    time_badge: str
    priority: str
    is_completed: bool
    created_at: datetime

    class Config:
        from_attributes = True

# -------------------------------------------------------------
# Mandi Wholesale Market Prices Schemas
# -------------------------------------------------------------
class MandiPriceSchema(BaseModel):
    id: int
    commodity: str
    mandi_name: str
    state: str
    min_price: float
    max_price: float
    modal_price: float
    price_unit: str
    trend_pct: float
    recorded_date: str

    class Config:
        from_attributes = True

# -------------------------------------------------------------
# Fertilizer Calculation Schemas
# -------------------------------------------------------------
class FertilizerCalculateRequest(BaseModel):
    crop_type: str
    area_acres: float
    vigor_status: Optional[str] = "Moderate"
    soil_dryness: Optional[str] = "Normal"
    soil_texture: Optional[str] = "loam"

class FertilizerCalculateResponse(BaseModel):
    crop_type: str
    area_acres: float
    urea_total_kg: float
    dap_total_kg: float
    mop_total_kg: float
    urea_per_acre_kg: float
    dap_per_acre_kg: float
    mop_per_acre_kg: float
    schedule: List[Dict[str, str]]
    safety_tips: List[str]
    soil_texture: Optional[str] = "Loam"
    elemental_nutrients_kg: Optional[Dict[str, float]] = None

# -------------------------------------------------------------
# Farm Budget & Expense Schemas
# -------------------------------------------------------------
class FarmExpenseCreate(BaseModel):
    field_id: Optional[int] = None
    category: str
    amount: float
    notes: Optional[str] = None
    expense_date: Optional[str] = None

class FarmExpenseSchema(BaseModel):
    id: int
    user_id: Optional[int] = None
    field_id: Optional[int] = None
    category: str
    amount: float
    notes: Optional[str] = None
    expense_date: str

    class Config:
        from_attributes = True

class BudgetSummaryResponse(BaseModel):
    total_expenses: float
    expected_revenue: float
    net_profit: float
    profit_margin_pct: float
    expenses_by_category: Dict[str, float]
    recent_expenses: List[FarmExpenseSchema]

# -------------------------------------------------------------
# Dashboard Aggregated Summary Schema
# -------------------------------------------------------------
class DashboardSummaryResponse(BaseModel):
    farmer_name: str = ""
    farm_name: str = ""
    total_fields: int
    active_alerts_count: int
    average_soil_moisture: float
    average_crop_vigor: float
    spraying_safe_today: bool
    system_status: str
    data_disclaimer: str
    fields: List[FieldSchema]
    recent_alerts: List[AlertSchema]
    todays_tasks: List[AdvisoryTaskSchema]
    weather: Optional[WeatherSchema] = None
    mandi_highlights: List[MandiPriceSchema] = []

    class Config:
        from_attributes = True


# -------------------------------------------------------------
# Intelligent Farmer Notification Subsystem Schemas
# -------------------------------------------------------------

class DeviceRegisterRequest(BaseModel):
    fcm_token: str
    device_type: Optional[str] = "android"
    device_name: Optional[str] = "Android Device"
    app_version: Optional[str] = "1.0.0"


class DeviceRegisterResponse(BaseModel):
    id: int
    fcm_token: str
    is_active: bool
    message: str

    class Config:
        from_attributes = True


class DeviceUnregisterRequest(BaseModel):
    fcm_token: str


class NotificationPreferenceSchema(BaseModel):
    quiet_hours_enabled: bool = True
    quiet_hours_start: str = "22:00"
    quiet_hours_end: str = "06:00"
    language: str = "en"
    irrigation_enabled: bool = True
    weather_enabled: bool = True
    spraying_enabled: bool = True
    agronomy_enabled: bool = True
    crop_health_enabled: bool = True
    inspection_enabled: bool = True
    min_priority: str = "LOW"

    class Config:
        from_attributes = True


class NotificationPreferenceUpdate(BaseModel):
    quiet_hours_enabled: Optional[bool] = None
    quiet_hours_start: Optional[str] = None
    quiet_hours_end: Optional[str] = None
    language: Optional[str] = None
    irrigation_enabled: Optional[bool] = None
    weather_enabled: Optional[bool] = None
    spraying_enabled: Optional[bool] = None
    agronomy_enabled: Optional[bool] = None
    crop_health_enabled: Optional[bool] = None
    inspection_enabled: Optional[bool] = None
    min_priority: Optional[str] = None


class NotificationEventSchema(BaseModel):
    id: int
    user_id: int
    field_id: Optional[int] = None
    field_name: Optional[str] = None
    category: str
    priority: str
    deduplication_key: str
    title: str
    message: str
    title_en: str
    title_ta: Optional[str] = None
    message_en: str
    message_ta: Optional[str] = None
    action_needed: Optional[str] = None
    recommended_datetime: Optional[str] = None
    input_quantity: Optional[str] = None
    reason: Optional[str] = None
    source: Optional[str] = None
    source_data_timestamp: Optional[str] = None
    confidence: Optional[str] = None
    created_at: datetime
    expires_at: Optional[datetime] = None
    is_read: bool = False
    read_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class NotificationDeliverySchema(BaseModel):
    id: int
    event_id: int
    device_id: Optional[int] = None
    status: str
    fcm_message_id: Optional[str] = None
    error_message: Optional[str] = None
    sent_at: datetime

    class Config:
        from_attributes = True


class NotificationEvaluationResponse(BaseModel):
    evaluated_fields_count: int
    notifications_generated_count: int
    notifications_sent_count: int
    notifications: List[NotificationEventSchema] = []
    evaluation_summary: List[str] = []
