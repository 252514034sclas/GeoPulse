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
    farm_name = Column(String, default="My Farm")
    total_area_ha = Column(Float, default=0.0)
    preferred_language = Column(String, default="en")
    theme_preference = Column(String, default="SYSTEM")
    created_at = Column(DateTime, default=datetime.utcnow)

    # Relationships
    fields = relationship("FieldModel", back_populates="owner", cascade="all, delete-orphan")
    tasks = relationship("AdvisoryTaskModel", back_populates="user", cascade="all, delete-orphan")
    expenses = relationship("FarmExpenseModel", back_populates="user", cascade="all, delete-orphan")
    devices = relationship("DeviceTokenModel", back_populates="user", cascade="all, delete-orphan")
    notification_preferences = relationship("NotificationPreferenceModel", back_populates="user", uselist=False, cascade="all, delete-orphan")
    notifications = relationship("NotificationEventModel", back_populates="user", cascade="all, delete-orphan")


class FieldModel(Base):
    __tablename__ = "fields"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True, index=True)
    name = Column(String, index=True, nullable=False)
    crop_type = Column(String, default="Paddy (Rice)")
    area_acres = Column(Float, default=0.0)
    sowing_date = Column(String, default=lambda: datetime.utcnow().strftime("%Y-%m-%d"))
    status = Column(String, default="Monitored")  # "Healthy", "Needs Water", "Monitored"
    soil_type = Column(String, default="Loam")  # Loam, Clay Loam, Black Cotton, Sandy Loam, etc.
    latitude = Column(Float, default=0.0)
    longitude = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=datetime.utcnow)

    # Spatial boundary stored as WKT (Well-Known Text) Polygon
    boundary = Column(Text, nullable=True)

    # Relationships
    owner = relationship("UserModel", back_populates="fields")
    satellite_records = relationship("SatelliteRecordModel", back_populates="field", cascade="all, delete-orphan")
    alerts = relationship("AlertModel", back_populates="field", cascade="all, delete-orphan")
    notifications = relationship("NotificationEventModel", back_populates="field")


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


# ==============================================================================
# INTELLIGENT FARMER NOTIFICATION & FCM SUBSYSTEM MODELS
# ==============================================================================

class DeviceTokenModel(Base):
    __tablename__ = "registered_devices"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    fcm_token = Column(String(512), unique=True, index=True, nullable=False)
    device_type = Column(String(32), default="android")  # "android", "ios", "web"
    device_name = Column(String(128), nullable=True)  # e.g., "Pixel 10 Pro"
    app_version = Column(String(32), nullable=True)
    is_active = Column(Boolean, default=True, index=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Relationships
    user = relationship("UserModel", back_populates="devices")
    deliveries = relationship("NotificationDeliveryModel", back_populates="device")


class NotificationPreferenceModel(Base):
    __tablename__ = "notification_preferences"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False, index=True)
    quiet_hours_enabled = Column(Boolean, default=True)
    quiet_hours_start = Column(String(8), default="22:00")  # HH:MM (24-hour)
    quiet_hours_end = Column(String(8), default="06:00")    # HH:MM (24-hour)
    language = Column(String(8), default="en")             # "en", "ta"
    irrigation_enabled = Column(Boolean, default=True)
    weather_enabled = Column(Boolean, default=True)
    spraying_enabled = Column(Boolean, default=True)
    agronomy_enabled = Column(Boolean, default=True)
    crop_health_enabled = Column(Boolean, default=True)
    inspection_enabled = Column(Boolean, default=True)
    min_priority = Column(String(16), default="LOW")       # "LOW", "MEDIUM", "HIGH", "CRITICAL"
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Relationships
    user = relationship("UserModel", back_populates="notification_preferences")


class NotificationEventModel(Base):
    __tablename__ = "notification_events"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    field_id = Column(Integer, ForeignKey("fields.id", ondelete="SET NULL"), nullable=True, index=True)
    category = Column(String(32), nullable=False, index=True)  # Irrigation, Weather, Spraying, Agronomy, CropHealth, Inspection
    priority = Column(String(16), default="MEDIUM", index=True)  # LOW, MEDIUM, HIGH, CRITICAL
    deduplication_key = Column(String(255), nullable=False, index=True)

    title_en = Column(String(255), nullable=False)
    title_ta = Column(String(255), nullable=True)
    message_en = Column(Text, nullable=False)
    message_ta = Column(Text, nullable=True)

    action_needed = Column(String(255), nullable=True)
    recommended_datetime = Column(String(64), nullable=True)
    input_quantity = Column(String(128), nullable=True)  # e.g., "18 mm (72,840 L/ac)"
    reason = Column(Text, nullable=True)
    source = Column(String(128), default="GeoPulse Agronomic Intelligence")
    source_data_timestamp = Column(String(64), nullable=True)
    confidence = Column(String(32), default="High (90%)")

    created_at = Column(DateTime, default=datetime.utcnow, index=True)
    expires_at = Column(DateTime, nullable=True, index=True)
    is_read = Column(Boolean, default=False, index=True)
    read_at = Column(DateTime, nullable=True)

    # Relationships
    user = relationship("UserModel", back_populates="notifications")
    field = relationship("FieldModel", back_populates="notifications")
    deliveries = relationship("NotificationDeliveryModel", back_populates="event", cascade="all, delete-orphan")


class NotificationDeliveryModel(Base):
    __tablename__ = "notification_deliveries"

    id = Column(Integer, primary_key=True, index=True)
    event_id = Column(Integer, ForeignKey("notification_events.id", ondelete="CASCADE"), nullable=False, index=True)
    device_id = Column(Integer, ForeignKey("registered_devices.id", ondelete="SET NULL"), nullable=True, index=True)
    status = Column(String(32), default="sent", index=True)  # "sent", "simulated", "failed", "unregistered"
    fcm_message_id = Column(String(255), nullable=True)
    error_message = Column(Text, nullable=True)
    sent_at = Column(DateTime, default=datetime.utcnow)

    # Relationships
    event = relationship("NotificationEventModel", back_populates="deliveries")
    device = relationship("DeviceTokenModel", back_populates="deliveries")
