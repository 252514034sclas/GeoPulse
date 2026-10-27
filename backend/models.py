from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime
from datetime import datetime
from database import Base

class FieldModel(Base):
    __tablename__ = "fields"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    crop_type = Column(String)
    area_acres = Column(Float)
    status = Column(String)  # e.g., "Optimal", "Needs Attention"
    latitude = Column(Float)
    longitude = Column(Float)
    updated_at = Column(DateTime, default=datetime.utcnow)

    # Spatial boundary stored as WKT (Well-Known Text) Polygon, compatible with SQLite & PostGIS
    boundary = Column(String, nullable=True)

class AlertModel(Base):
    __tablename__ = "alerts"

    id = Column(Integer, primary_key=True, index=True)
    field_id = Column(Integer, index=True)
    severity = Column(String)  # "Low", "Medium", "High"
    message = Column(String)
    timestamp = Column(DateTime, default=datetime.utcnow)
    is_resolved = Column(Boolean, default=False)

class VegetationHealthModel(Base):
    __tablename__ = "vegetation_health"

    id = Column(Integer, primary_key=True, index=True)
    field_id = Column(Integer, index=True)
    ndvi_value = Column(Float)
    health_status = Column(String)
    recorded_date = Column(String)
    is_sample = Column(Boolean, default=True)
    disclaimer = Column(String, default="Sample data - NDVI pipeline pending actual satellite imagery integration.")

class WeatherModel(Base):
    __tablename__ = "weather"

    id = Column(Integer, primary_key=True, index=True)
    location = Column(String, index=True)
    temperature_c = Column(Float)
    humidity_percent = Column(Float)
    precipitation_mm = Column(Float)
    wind_speed_kmh = Column(Float)
    status_message = Column(String)
    data_source = Column(String, default="Simulated Demo Weather Service")
    updated_at = Column(DateTime, default=datetime.utcnow)

class SoilMoistureModel(Base):
    __tablename__ = "soil_moisture"

    id = Column(Integer, primary_key=True, index=True)
    field_id = Column(Integer, index=True)
    moisture_percentage = Column(Float)
    depth_cm = Column(Float)
    recorded_at = Column(String)
    data_source = Column(String, default="Sample Sensor Reading")
