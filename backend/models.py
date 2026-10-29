from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from datetime import datetime
from database import Base

class UserModel(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    full_name = Column(String, nullable=False)
    hashed_password = Column(String, nullable=False)
    phone = Column(String, nullable=True)
    farm_name = Column(String, default="Green Valley Farm")
    total_area_ha = Column(Float, default=12.5)
    preferred_language = Column(String, default="en")
    theme_preference = Column(String, default="SYSTEM")
    created_at = Column(DateTime, default=datetime.utcnow)

    # Relationships
    fields = relationship("FieldModel", back_populates="owner", cascade="all, delete-orphan")
    tasks = relationship("AdvisoryTaskModel", back_populates="user", cascade="all, delete-orphan")
    expenses = relationship("FarmExpenseModel", back_populates="user", cascade="all, delete-orphan")


class FieldModel(Base):
    __tablename__ = "fields"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True, index=True)
    name = Column(String, index=True, nullable=False)
    crop_type = Column(String, default="Corn (Dent)")
    area_acres = Column(Float, default=120.4)
    sowing_date = Column(String, default="2026-04-15")
    status = Column(String, default="Healthy")  # "Healthy", "Needs Water", "Monitored"
    latitude = Column(Float, default=37.7749)
    longitude = Column(Float, default=-122.4194)
    updated_at = Column(DateTime, default=datetime.utcnow)

    # Spatial boundary stored as WKT (Well-Known Text) Polygon
    boundary = Column(Text, nullable=True)

    # Relationships
    owner = relationship("UserModel", back_populates="fields")
    satellite_records = relationship("SatelliteRecordModel", back_populates="field", cascade="all, delete-orphan")
    alerts = relationship("AlertModel", back_populates="field", cascade="all, delete-orphan")


class SatelliteRecordModel(Base):
    __tablename__ = "satellite_records"

    id = Column(Integer, primary_key=True, index=True)
    field_id = Column(Integer, ForeignKey("fields.id"), index=True, nullable=False)
    recorded_date = Column(String, index=True)  # YYYY-MM-DD
    ndvi_value = Column(Float)  # 0.0 to 1.0 (Greenness / vigor)
    ndwi_value = Column(Float, default=0.45)  # Water index / root-zone moisture
    chlorophyll_spad = Column(Float, default=42.5)  # SPAD chlorophyll estimation
    canopy_cover_pct = Column(Float, default=85.0)  # Canopy cover percentage
    health_status = Column(String, default="Optimal")  # Optimal, Moderate, Stressed
    layman_advice = Column(String, default="Canopy greenness is optimal. Leaf chlorophyll indicates healthy photosynthesis.")
    is_sample = Column(Boolean, default=False)
    disclaimer = Column(String, default="Derived from Sentinel-2 MSI multispectral surface reflectance bands.")

    field = relationship("FieldModel", back_populates="satellite_records")


# Compatibility alias for legacy endpoint
VegetationHealthModel = SatelliteRecordModel


class WeatherModel(Base):
    __tablename__ = "weather"

    id = Column(Integer, primary_key=True, index=True)
    location = Column(String, index=True, default="Chennai, Tamil Nadu")
    temperature_c = Column(Float, default=28.0)
    humidity_percent = Column(Float, default=58.0)
    precipitation_mm = Column(Float, default=0.0)
    wind_speed_kmh = Column(Float, default=8.5)
    spray_safe = Column(Boolean, default=True)
    spray_advice = Column(String, default="Safe Spraying Window Open: Wind < 15 km/h, 0% rain probability.")
    status_message = Column(String, default="Clear and calm conditions. Ideal for morning field operations.")
    data_source = Column(String, default="High-Resolution Satellite Meteorological Grid")
    updated_at = Column(DateTime, default=datetime.utcnow)


class SoilMoistureModel(Base):
    __tablename__ = "soil_moisture"

    id = Column(Integer, primary_key=True, index=True)
    field_id = Column(Integer, index=True)
    moisture_percentage = Column(Float, default=72.0)
    depth_cm = Column(Float, default=30.0)
    status = Column(String, default="Optimal")  # Dry, Optimal, Saturated
    watering_advice = Column(String, default="Adequate root-zone moisture. No immediate irrigation needed.")
    recorded_at = Column(String, default="2026-10-29T08:00:00Z")
    data_source = Column(String, default="Satellite Remote Sensing Soil Moisture (NDWI)")


class AlertModel(Base):
    __tablename__ = "alerts"

    id = Column(Integer, primary_key=True, index=True)
    field_id = Column(Integer, ForeignKey("fields.id"), index=True, nullable=True)
    severity = Column(String, default="Medium")  # "Low", "Medium", "High"
    message = Column(String, nullable=False)
    category = Column(String, default="Weather")  # Weather, Moisture, Pest, Fertilizer
    timestamp = Column(DateTime, default=datetime.utcnow)
    is_resolved = Column(Boolean, default=False)

    field = relationship("FieldModel", back_populates="alerts")


class AdvisoryTaskModel(Base):
    __tablename__ = "advisory_tasks"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), index=True, nullable=True)
    field_id = Column(Integer, ForeignKey("fields.id"), index=True, nullable=True)
    task_type = Column(String, default="Spraying")  # Spraying, Irrigation, Fertilizer, Pest, Harvest
    title = Column(String, nullable=False)
    description = Column(String, nullable=False)
    time_badge = Column(String, default="Today")
    priority = Column(String, default="High")  # Urgent, High, Medium, Low
    is_completed = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("UserModel", back_populates="tasks")


class MandiPriceModel(Base):
    __tablename__ = "mandi_prices"

    id = Column(Integer, primary_key=True, index=True)
    commodity = Column(String, index=True, nullable=False)  # Wheat, Corn, Cotton, Paddy, Mustard, Soybean
    mandi_name = Column(String, nullable=False)
    state = Column(String, default="Tamil Nadu")
    min_price = Column(Float, nullable=False)
    max_price = Column(Float, nullable=False)
    modal_price = Column(Float, nullable=False)  # ₹ per quintal (100 kg)
    price_unit = Column(String, default="₹ / Quintal")
    trend_pct = Column(Float, default=2.5)  # e.g., +2.5%
    recorded_date = Column(String, default="2026-10-29")


class FarmExpenseModel(Base):
    __tablename__ = "farm_expenses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), index=True, nullable=True)
    field_id = Column(Integer, ForeignKey("fields.id"), index=True, nullable=True)
    category = Column(String, nullable=False)  # Seeds, Fertilizer, Fuel, Labor, Machinery, Pesticides
    amount = Column(Float, nullable=False)
    notes = Column(String, nullable=True)
    expense_date = Column(String, default="2026-10-29")

    user = relationship("UserModel", back_populates="expenses")


class FertilizerGuideModel(Base):
    __tablename__ = "fertilizer_guides"

    id = Column(Integer, primary_key=True, index=True)
    crop_type = Column(String, index=True, nullable=False)  # Corn, Wheat, Soybean, Paddy, Cotton
    vigor_tier = Column(String, default="Moderate")  # High/Optimal, Moderate, Low/Deficient
    urea_kg_per_acre = Column(Float, default=45.0)
    dap_kg_per_acre = Column(Float, default=30.0)
    mop_kg_per_acre = Column(Float, default=20.0)
    stage_timing = Column(String, default="Tillering / Vegetative Stage (Day 35-50)")
    tips = Column(String, default="Apply split dosage before expected rainfall for maximum root uptake.")
