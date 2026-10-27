from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime

class HealthResponse(BaseModel):
    status: str
    version: str
    timestamp: str

class FieldSchema(BaseModel):
    id: int
    name: str
    crop_type: str
    area_acres: float
    status: str
    latitude: float
    longitude: float
    boundary: Optional[str] = None

    class Config:
        from_attributes = True

class AlertSchema(BaseModel):
    id: int
    field_id: int
    severity: str
    message: str
    timestamp: datetime
    is_resolved: bool

    class Config:
        from_attributes = True

class VegetationHealthSchema(BaseModel):
    id: int
    field_id: int
    ndvi_value: float
    health_status: str
    recorded_date: str
    is_sample: bool
    disclaimer: str

    class Config:
        from_attributes = True

class WeatherSchema(BaseModel):
    id: int
    location: str
    temperature_c: float
    humidity_percent: float
    precipitation_mm: float
    wind_speed_kmh: float
    status_message: str
    data_source: str
    updated_at: datetime

    class Config:
        from_attributes = True

class SoilMoistureSchema(BaseModel):
    id: int
    field_id: int
    moisture_percentage: float
    depth_cm: float
    recorded_at: str
    data_source: str

    class Config:
        from_attributes = True

class DashboardSummaryResponse(BaseModel):
    total_fields: int
    active_alerts_count: int
    average_soil_moisture: float
    system_status: str
    data_disclaimer: str
    fields: List[FieldSchema]
    recent_alerts: List[AlertSchema]
    weather: Optional[WeatherSchema] = None

    class Config:
        from_attributes = True
