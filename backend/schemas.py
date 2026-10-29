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
    farm_name: Optional[str] = "Green Valley Farm"
    total_area_ha: Optional[float] = 12.5
    preferred_language: Optional[str] = "en"
    theme_preference: Optional[str] = "SYSTEM"

class UserLogin(BaseModel):
    email: str
    password: str

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
    area_acres: float = 120.4
    sowing_date: Optional[str] = "2026-04-15"
    latitude: float = 37.7749
    longitude: float = -122.4194
    boundary: Optional[str] = None

class FieldUpdate(BaseModel):
    name: Optional[str] = None
    crop_type: Optional[str] = None
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
    humidity_percent: float
    precipitation_mm: float
    wind_speed_kmh: float
    spray_safe: bool
    spray_advice: str
    status_message: str
    data_source: str
    updated_at: datetime

    class Config:
        from_attributes = True

class SprayWindowResponse(BaseModel):
    is_safe_now: bool
    current_wind_kmh: float
    current_humidity_pct: float
    current_rain_prob_pct: float
    verdict: str
    recommended_hours: List[str]
    cautions: List[str]

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
    farmer_name: str = "Nithish Kumar"
    farm_name: str = "Green Valley Farm"
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
