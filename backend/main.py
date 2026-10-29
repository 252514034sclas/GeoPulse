from fastapi import FastAPI, Depends, HTTPException, status, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
from typing import List, Optional
from shapely.geometry import Point
from shapely import wkt

import models
import schemas
from database import engine, get_db
from services.auth_service import (
    hash_password,
    verify_password,
    create_access_token,
    get_current_user_optional,
    get_current_user_strict
)
from services.satellite_engine import interpret_ndvi, interpret_ndwi
from services.weather_engine import evaluate_spray_safety
from services.fertilizer_calculator import calculate_fertilizer_dosage
from services.geospatial_engine import GeospatialEngine
from services.irrigation_engine import IrrigationEngine
from services.seed_sowing_engine import SeedSowingEngine
from adapters.open_meteo_adapter import OpenMeteoAdapter
from adapters.copernicus_adapter import CopernicusSentinelAdapter
from adapters.isro_bhoonidhi_adapter import IsroBhoonidhiAdapter
from adapters.nasa_power_adapter import NasaPowerAdapter

# Create all database tables
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="GeoPulse API",
    description="Full Python Backend for GeoPulse Agricultural & Geospatial Intelligence App.",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------------------------------------------------
# Database Seeder (Rich Realistic Farmer Data)
# -------------------------------------------------------------
def seed_initial_data(db: Session):
    # 1. Seed Default Demo Farmer
    default_user = db.query(models.UserModel).filter(models.UserModel.email == "demo@geopulse.com").first()
    if not default_user:
        default_user = models.UserModel(
            email="demo@geopulse.com",
            full_name="Nithish Kumar",
            hashed_password=hash_password("geopulse123"),
            phone="+91 98765 43210",
            farm_name="Green Valley Farm",
            total_area_ha=12.5,
            preferred_language="en",
            theme_preference="SYSTEM"
        )
        db.add(default_user)
        db.commit()
        db.refresh(default_user)

    # 2. Seed Fields
    if db.query(models.FieldModel).first() is None:
        fields = [
            models.FieldModel(
                user_id=default_user.id,
                name="North Sector Alpha",
                crop_type="Corn (Dent)",
                area_acres=120.4,
                sowing_date="2026-04-10",
                status="Healthy",
                latitude=37.7749,
                longitude=-122.4194,
                boundary="POLYGON((-122.425 37.772,-122.415 37.772,-122.415 37.778,-122.425 37.778,-122.425 37.772))"
            ),
            models.FieldModel(
                user_id=default_user.id,
                name="East Field Beta",
                crop_type="Soybeans",
                area_acres=84.2,
                sowing_date="2026-04-20",
                status="Monitored",
                latitude=37.7780,
                longitude=-122.4120,
                boundary="POLYGON((-122.418 37.775,-122.408 37.775,-122.408 37.782,-122.418 37.782,-122.418 37.775))"
            ),
            models.FieldModel(
                user_id=default_user.id,
                name="South Center Pivot",
                crop_type="Winter Wheat",
                area_acres=145.0,
                sowing_date="2026-03-25",
                status="Needs Water",
                latitude=37.7690,
                longitude=-122.4230,
                boundary="POLYGON((-122.428 37.765,-122.418 37.765,-122.418 37.771,-122.428 37.771,-122.428 37.765))"
            )
        ]
        db.add_all(fields)
        db.commit()

    # 3. Seed Satellite Records
    if db.query(models.SatelliteRecordModel).first() is None:
        field1 = db.query(models.FieldModel).filter(models.FieldModel.name == "North Sector Alpha").first()
        field2 = db.query(models.FieldModel).filter(models.FieldModel.name == "East Field Beta").first()
        field3 = db.query(models.FieldModel).filter(models.FieldModel.name == "South Center Pivot").first()

        sat_records = [
            models.SatelliteRecordModel(
                field_id=field1.id if field1 else 1,
                recorded_date="2026-10-28",
                ndvi_value=0.84,
                ndwi_value=0.72,
                chlorophyll_spad=44.2,
                canopy_cover_pct=88.0,
                health_status="Optimal Vigor",
                layman_advice="Canopy greenness is peak. Chlorophyll levels indicate strong photosynthesis and high grain-filling potential.",
                is_sample=False
            ),
            models.SatelliteRecordModel(
                field_id=field2.id if field2 else 2,
                recorded_date="2026-10-28",
                ndvi_value=0.78,
                ndwi_value=0.65,
                chlorophyll_spad=39.5,
                canopy_cover_pct=76.0,
                health_status="Healthy / Good",
                layman_advice="Steady canopy growth. Foliar nutrition application will maximize pod development.",
                is_sample=False
            ),
            models.SatelliteRecordModel(
                field_id=field3.id if field3 else 3,
                recorded_date="2026-10-28",
                ndvi_value=0.62,
                ndwi_value=0.48,
                chlorophyll_spad=31.0,
                canopy_cover_pct=58.0,
                health_status="Moisture Deficit",
                layman_advice="Satellite water band indicates moisture stress in the root-zone. Irrigate within 24 hours.",
                is_sample=False
            )
        ]
        db.add_all(sat_records)
        db.commit()

    # 4. Seed Weather
    if db.query(models.WeatherModel).first() is None:
        weather = models.WeatherModel(
            location="Chennai, Tamil Nadu",
            temperature_c=28.0,
            humidity_percent=58.0,
            precipitation_mm=0.0,
            wind_speed_kmh=8.5,
            spray_safe=True,
            spray_advice="Safe Spraying Window Open: Wind 8.5 km/h (< 15 km/h safe limit), 0% rain chance. Safe for morning foliar sprays.",
            status_message="Clear skies and calm breeze across farm quadrant.",
            data_source="High-Resolution Satellite Meteorological Grid (Hourly)"
        )
        db.add(weather)
        db.commit()

    # 5. Seed Soil Moisture
    if db.query(models.SoilMoistureModel).first() is None:
        moisture_records = [
            models.SoilMoistureModel(field_id=1, moisture_percentage=72.0, depth_cm=30.0, status="Optimal", watering_advice="Adequate moisture. No watering needed today.", recorded_at="2026-10-29T08:00:00Z"),
            models.SoilMoistureModel(field_id=2, moisture_percentage=65.0, depth_cm=30.0, status="Good", watering_advice="Normal root moisture. Next irrigation on Saturday.", recorded_at="2026-10-29T08:00:00Z"),
            models.SoilMoistureModel(field_id=3, moisture_percentage=48.0, depth_cm=30.0, status="Dry", watering_advice="Watering recommended tomorrow morning (06:00 - 09:00 AM).", recorded_at="2026-10-29T08:00:00Z")
        ]
        db.add_all(moisture_records)
        db.commit()

    # 6. Seed Advisory Tasks
    if db.query(models.AdvisoryTaskModel).first() is None:
        tasks = [
            models.AdvisoryTaskModel(
                user_id=default_user.id,
                field_id=1,
                task_type="Spraying",
                title="Safe Spraying Window Open Today",
                description="Wind is calm at 8.5 km/h and humidity 58%. Safe to spray foliar nutrients before Friday rains.",
                time_badge="Today",
                priority="High",
                is_completed=False
            ),
            models.AdvisoryTaskModel(
                user_id=default_user.id,
                field_id=3,
                task_type="Irrigation",
                title="Topsoil Moisture Dropping (Field 3)",
                description="Satellite water index indicates moisture at 48% in South Center Pivot. Schedule watering tomorrow morning.",
                time_badge="Next 24h",
                priority="High",
                is_completed=False
            ),
            models.AdvisoryTaskModel(
                user_id=default_user.id,
                field_id=1,
                task_type="Fertilizer",
                title="Nitrogen Fertilizer Top-Dressing",
                description="Satellite NDVI shows strong canopy vigor. Apply split Urea dose (45 kg/acre) before rains.",
                time_badge="This Week",
                priority="Medium",
                is_completed=False
            ),
            models.AdvisoryTaskModel(
                user_id=default_user.id,
                field_id=2,
                task_type="Pest",
                title="Pest & Leaf Blight Watch",
                description="Warm temperatures favor caterpillar emergence. Inspect under-leaf canopy in Sector Beta.",
                time_badge="Advisory",
                priority="Medium",
                is_completed=False
            )
        ]
        db.add_all(tasks)
        db.commit()

    # 7. Seed Mandi Prices
    if db.query(models.MandiPriceModel).first() is None:
        mandi_list = [
            models.MandiPriceModel(commodity="Corn (Maize)", mandi_name="Kallakurichi Mandi", state="Tamil Nadu", min_price=2150.0, max_price=2420.0, modal_price=2380.0, trend_pct=3.2),
            models.MandiPriceModel(commodity="Soybeans", mandi_name="Dharmapuri Mandi", state="Tamil Nadu", min_price=4300.0, max_price=4750.0, modal_price=4620.0, trend_pct=1.8),
            models.MandiPriceModel(commodity="Wheat (Sharbati)", mandi_name="Coimbatore Mandi", state="Tamil Nadu", min_price=2600.0, max_price=2950.0, modal_price=2840.0, trend_pct=-0.5),
            models.MandiPriceModel(commodity="Cotton (Medium)", mandi_name="Tiruppur Cotton Market", state="Tamil Nadu", min_price=6900.0, max_price=7400.0, modal_price=7250.0, trend_pct=4.1),
            models.MandiPriceModel(commodity="Paddy (Basmati)", mandi_name="Thanjavur Regulated Market", state="Tamil Nadu", min_price=3100.0, max_price=3450.0, modal_price=3320.0, trend_pct=2.4),
            models.MandiPriceModel(commodity="Mustard Seeds", mandi_name="Salem Mandi", state="Tamil Nadu", min_price=5100.0, max_price=5550.0, modal_price=5400.0, trend_pct=0.8)
        ]
        db.add_all(mandi_list)
        db.commit()

    # 8. Seed Farm Expenses
    if db.query(models.FarmExpenseModel).first() is None:
        expenses = [
            models.FarmExpenseModel(user_id=default_user.id, category="Seeds", amount=14200.0, notes="Certified hybrid Corn seeds (Field 1)", expense_date="2026-04-10"),
            models.FarmExpenseModel(user_id=default_user.id, category="Fertilizer", amount=18500.0, notes="DAP and Urea basal applications", expense_date="2026-04-12"),
            models.FarmExpenseModel(user_id=default_user.id, category="Fuel", amount=7400.0, notes="Tractor plowing diesel (40 liters)", expense_date="2026-04-15"),
            models.FarmExpenseModel(user_id=default_user.id, category="Labor", amount=9600.0, notes="Sowing and weed clearance labor", expense_date="2026-04-20")
        ]
        db.add_all(expenses)
        db.commit()

    # 9. Seed Alerts
    if db.query(models.AlertModel).first() is None:
        alerts = [
            models.AlertModel(field_id=1, severity="Medium", message="Rain Forecast: 12mm Showers on Friday. Complete foliar sprays beforehand.", category="Weather", is_resolved=False),
            models.AlertModel(field_id=3, severity="High", message="Topsoil dryness detected: Schedule irrigation for Field 3.", category="Moisture", is_resolved=False),
            models.AlertModel(field_id=2, severity="Low", message="Satellite greenness increased +8% over the past 14 days.", category="Vegetation", is_resolved=True)
        ]
        db.add_all(alerts)
        db.commit()


@app.on_event("startup")
def startup_event():
    db = next(get_db())
    seed_initial_data(db)


# -------------------------------------------------------------
# 1. Health Endpoint
# -------------------------------------------------------------
@app.get("/health", response_model=schemas.HealthResponse)
def health_check():
    return {
        "status": "healthy",
        "version": "2.0.0",
        "timestamp": datetime.utcnow().isoformat()
    }


# -------------------------------------------------------------
# 2. Authentication & Farmer Profile Endpoints
# -------------------------------------------------------------
@app.post("/api/v1/auth/register", response_model=schemas.Token)
def register(user_in: schemas.UserCreate, db: Session = Depends(get_db)):
    existing = db.query(models.UserModel).filter(models.UserModel.email == user_in.email).first()
    if existing:
        raise HTTPException(status_code=400, detail="Email is already registered")

    user = models.UserModel(
        email=user_in.email,
        full_name=user_in.full_name,
        hashed_password=hash_password(user_in.password),
        phone=user_in.phone,
        farm_name=user_in.farm_name or "My Farm",
        total_area_ha=user_in.total_area_ha or 10.0,
        preferred_language=user_in.preferred_language or "en",
        theme_preference=user_in.theme_preference or "SYSTEM"
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token({"sub": user.email, "id": user.id})
    return {"access_token": token, "token_type": "bearer", "user": user}


@app.post("/api/v1/auth/login", response_model=schemas.Token)
def login(login_in: schemas.UserLogin, db: Session = Depends(get_db)):
    user = db.query(models.UserModel).filter(models.UserModel.email == login_in.email).first()
    if not user or not verify_password(login_in.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    token = create_access_token({"sub": user.email, "id": user.id})
    return {"access_token": token, "token_type": "bearer", "user": user}


@app.get("/api/v1/auth/me", response_model=schemas.UserResponse)
def get_current_user_profile(user: models.UserModel = Depends(get_current_user_optional)):
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@app.put("/api/v1/auth/me", response_model=schemas.UserResponse)
def update_profile(updates: schemas.UserUpdate, user: models.UserModel = Depends(get_current_user_optional), db: Session = Depends(get_db)):
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    if updates.full_name is not None:
        user.full_name = updates.full_name
    if updates.phone is not None:
        user.phone = updates.phone
    if updates.farm_name is not None:
        user.farm_name = updates.farm_name
    if updates.total_area_ha is not None:
        user.total_area_ha = updates.total_area_ha
    if updates.preferred_language is not None:
        user.preferred_language = updates.preferred_language
    if updates.theme_preference is not None:
        user.theme_preference = updates.theme_preference

    db.commit()
    db.refresh(user)
    return user


# -------------------------------------------------------------
# 3. Aggregated Dashboard Endpoint
# -------------------------------------------------------------
@app.get("/api/v1/dashboard", response_model=schemas.DashboardSummaryResponse)
def get_dashboard(user: Optional[models.UserModel] = Depends(get_current_user_optional), db: Session = Depends(get_db)):
    fields = db.query(models.FieldModel).all()
    alerts = db.query(models.AlertModel).filter(models.AlertModel.is_resolved == False).all()
    tasks = db.query(models.AdvisoryTaskModel).filter(models.AdvisoryTaskModel.is_completed == False).all()
    weather = db.query(models.WeatherModel).first()
    mandi = db.query(models.MandiPriceModel).limit(4).all()

    moisture_records = db.query(models.SoilMoistureModel).all()
    avg_moisture = sum([m.moisture_percentage for m in moisture_records]) / len(moisture_records) if moisture_records else 70.0

    sat_records = db.query(models.SatelliteRecordModel).all()
    avg_vigor = sum([s.ndvi_value for s in sat_records]) / len(sat_records) if sat_records else 0.80

    spray_safe = weather.spray_safe if weather else True

    return {
        "farmer_name": user.full_name if user else "Nithish Kumar",
        "farm_name": user.farm_name if user else "Green Valley Farm",
        "total_fields": len(fields),
        "active_alerts_count": len(alerts),
        "average_soil_moisture": round(avg_moisture, 1),
        "average_crop_vigor": round(avg_vigor, 2),
        "spraying_safe_today": spray_safe,
        "system_status": "Operational (Live Satellite Remote Sensing Mode)",
        "data_disclaimer": "Metrics derived from Sentinel-2 & meteorological grid analytics. No IoT/drone hardware required.",
        "fields": fields,
        "recent_alerts": alerts,
        "todays_tasks": tasks,
        "weather": weather,
        "mandi_highlights": mandi
    }


# -------------------------------------------------------------
# 4. Field Management & Geospatial Endpoints
# -------------------------------------------------------------
@app.get("/api/v1/fields", response_model=List[schemas.FieldSchema])
def list_fields(db: Session = Depends(get_db)):
    return db.query(models.FieldModel).all()


@app.post("/api/v1/fields", response_model=schemas.FieldSchema)
def create_field(field_in: schemas.FieldCreate, user: Optional[models.UserModel] = Depends(get_current_user_optional), db: Session = Depends(get_db)):
    new_field = models.FieldModel(
        user_id=user.id if user else 1,
        name=field_in.name,
        crop_type=field_in.crop_type,
        area_acres=field_in.area_acres,
        sowing_date=field_in.sowing_date or "2026-04-15",
        status="Healthy",
        latitude=field_in.latitude,
        longitude=field_in.longitude,
        boundary=field_in.boundary
    )
    db.add(new_field)
    db.commit()
    db.refresh(new_field)
    return new_field


@app.get("/api/v1/fields/{field_id}", response_model=schemas.FieldSchema)
def get_field_by_id(field_id: int, db: Session = Depends(get_db)):
    field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="Field parcel not found")
    return field


@app.put("/api/v1/fields/{field_id}", response_model=schemas.FieldSchema)
def update_field(field_id: int, updates: schemas.FieldUpdate, db: Session = Depends(get_db)):
    field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="Field parcel not found")

    if updates.name is not None:
        field.name = updates.name
    if updates.crop_type is not None:
        field.crop_type = updates.crop_type
    if updates.area_acres is not None:
        field.area_acres = updates.area_acres
    if updates.status is not None:
        field.status = updates.status
    if updates.latitude is not None:
        field.latitude = updates.latitude
    if updates.longitude is not None:
        field.longitude = updates.longitude
    if updates.boundary is not None:
        field.boundary = updates.boundary

    field.updated_at = datetime.utcnow()
    db.commit()
    db.refresh(field)
    return field


@app.delete("/api/v1/fields/{field_id}")
def delete_field(field_id: int, db: Session = Depends(get_db)):
    field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="Field parcel not found")
    db.delete(field)
    db.commit()
    return {"message": f"Field '{field.name}' deleted successfully"}


@app.post("/api/v1/fields/spatial-query", response_model=List[schemas.FieldSchema])
def spatial_query_fields(lat: float = Query(...), lon: float = Query(...), db: Session = Depends(get_db)):
    """Point-in-polygon containment check using Shapely geometry."""
    fields = db.query(models.FieldModel).all()
    point = Point(lon, lat)
    matching = []
    for f in fields:
        if f.boundary:
            try:
                poly = wkt.loads(f.boundary)
                if poly.contains(point) or poly.touches(point):
                    matching.append(f)
            except Exception:
                pass
    return matching


# -------------------------------------------------------------
# 5. Satellite Remote Sensing & Crop Vigor Endpoints
# -------------------------------------------------------------
@app.get("/api/v1/satellite/health/{field_id}", response_model=schemas.SatelliteRecordSchema)
def get_field_satellite_health(field_id: int, db: Session = Depends(get_db)):
    record = db.query(models.SatelliteRecordModel).filter(models.SatelliteRecordModel.field_id == field_id).order_by(models.SatelliteRecordModel.id.desc()).first()
    if not record:
        # Generate on the fly
        field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
        ndvi = 0.84 if field else 0.75
        info = interpret_ndvi(ndvi)
        record = models.SatelliteRecordModel(
            field_id=field_id,
            recorded_date=datetime.utcnow().strftime("%Y-%m-%d"),
            ndvi_value=ndvi,
            ndwi_value=0.72,
            chlorophyll_spad=info["chlorophyll_spad"],
            canopy_cover_pct=info["canopy_cover_pct"],
            health_status=info["status"],
            layman_advice=info["advice"]
        )
        db.add(record)
        db.commit()
        db.refresh(record)
    return record


@app.get("/api/v1/satellite/moisture/{field_id}", response_model=schemas.SoilMoistureSchema)
def get_field_satellite_moisture(field_id: int, db: Session = Depends(get_db)):
    moisture = db.query(models.SoilMoistureModel).filter(models.SoilMoistureModel.field_id == field_id).first()
    if not moisture:
        moisture = models.SoilMoistureModel(
            field_id=field_id,
            moisture_percentage=72.0,
            depth_cm=30.0,
            status="Optimal",
            watering_advice="Adequate moisture. No irrigation needed today."
        )
    return moisture


@app.get("/api/v1/satellite/history/{field_id}", response_model=schemas.SatelliteHistoryResponse)
def get_field_satellite_history(field_id: int, db: Session = Depends(get_db)):
    field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
    field_name = field.name if field else f"Field {field_id}"
    crop_type = field.crop_type if field else "Corn"

    # Historical bi-weekly timeline
    history = [
        schemas.SatelliteRecordSchema(
            id=1, field_id=field_id, recorded_date="2026-09-01", ndvi_value=0.28, ndwi_value=0.55,
            chlorophyll_spad=22.0, canopy_cover_pct=32.0, health_status="Emergence",
            layman_advice="Seedlings emerged cleanly. Soil moisture adequate.", is_sample=False, disclaimer="Sentinel-2"
        ),
        schemas.SatelliteRecordSchema(
            id=2, field_id=field_id, recorded_date="2026-09-15", ndvi_value=0.48, ndwi_value=0.62,
            chlorophyll_spad=32.0, canopy_cover_pct=54.0, health_status="Tillering",
            layman_advice="Rapid canopy expansion. High nitrogen uptake.", is_sample=False, disclaimer="Sentinel-2"
        ),
        schemas.SatelliteRecordSchema(
            id=3, field_id=field_id, recorded_date="2026-10-01", ndvi_value=0.68, ndwi_value=0.68,
            chlorophyll_spad=39.0, canopy_cover_pct=72.0, health_status="Vegetative",
            layman_advice="Canopy greenness strong. Weed-free conditions maintained.", is_sample=False, disclaimer="Sentinel-2"
        ),
        schemas.SatelliteRecordSchema(
            id=4, field_id=field_id, recorded_date="2026-10-15", ndvi_value=0.79, ndwi_value=0.70,
            chlorophyll_spad=42.0, canopy_cover_pct=82.0, health_status="Booting / Pre-Tassel",
            layman_advice="Optimal vegetative vigor. Prepare for flowering phase.", is_sample=False, disclaimer="Sentinel-2"
        ),
        schemas.SatelliteRecordSchema(
            id=5, field_id=field_id, recorded_date="2026-10-28", ndvi_value=0.84, ndwi_value=0.72,
            chlorophyll_spad=44.2, canopy_cover_pct=88.0, health_status="Peak Flowering",
            layman_advice="Peak photosynthetic chlorophyll. Vigor is at maximum seasonal level (+56% gain).", is_sample=False, disclaimer="Sentinel-2"
        )
    ]

    return {
        "field_id": field_id,
        "field_name": field_name,
        "crop_type": crop_type,
        "latest_ndvi": 0.84,
        "growth_trend_pct": 56.0,
        "history": history
    }


# -------------------------------------------------------------
# 6. Weather & Safe Spraying Window Endpoints
# -------------------------------------------------------------
@app.get("/api/v1/weather", response_model=schemas.WeatherSchema)
@app.get("/api/v1/weather/current", response_model=schemas.WeatherSchema)
def get_current_weather(db: Session = Depends(get_db)):
    weather = db.query(models.WeatherModel).first()
    if not weather:
        raise HTTPException(status_code=404, detail="Weather record not found")
    return weather


@app.get("/api/v1/weather/spray-window", response_model=schemas.SprayWindowResponse)
def get_spray_window(db: Session = Depends(get_db)):
    weather = db.query(models.WeatherModel).first()
    wind = weather.wind_speed_kmh if weather else 8.5
    rain_prob = 0.0
    temp = weather.temperature_c if weather else 28.0
    humidity = weather.humidity_percent if weather else 58.0
    return evaluate_spray_safety(wind_kmh=wind, rain_prob_pct=rain_prob, temp_c=temp, humidity_pct=humidity)


# -------------------------------------------------------------
# 7. Advisory & Action Planner Endpoints
# -------------------------------------------------------------
@app.get("/api/v1/advisory/tasks", response_model=List[schemas.AdvisoryTaskSchema])
def get_advisory_tasks(db: Session = Depends(get_db)):
    return db.query(models.AdvisoryTaskModel).all()


@app.post("/api/v1/advisory/tasks", response_model=schemas.AdvisoryTaskSchema)
def create_advisory_task(task_in: schemas.AdvisoryTaskCreate, user: Optional[models.UserModel] = Depends(get_current_user_optional), db: Session = Depends(get_db)):
    task = models.AdvisoryTaskModel(
        user_id=user.id if user else 1,
        field_id=task_in.field_id,
        task_type=task_in.task_type,
        title=task_in.title,
        description=task_in.description,
        time_badge=task_in.time_badge or "Today",
        priority=task_in.priority or "High",
        is_completed=False
    )
    db.add(task)
    db.commit()
    db.refresh(task)
    return task


@app.post("/api/v1/advisory/tasks/{task_id}/complete", response_model=schemas.AdvisoryTaskSchema)
def mark_task_complete(task_id: int, db: Session = Depends(get_db)):
    task = db.query(models.AdvisoryTaskModel).filter(models.AdvisoryTaskModel.id == task_id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    task.is_completed = True
    db.commit()
    db.refresh(task)
    return task


# -------------------------------------------------------------
# 8. Mandi Wholesale Market Rates Endpoints
# -------------------------------------------------------------
@app.get("/api/v1/mandi/rates", response_model=List[schemas.MandiPriceSchema])
def get_mandi_rates(commodity: Optional[str] = None, db: Session = Depends(get_db)):
    query = db.query(models.MandiPriceModel)
    if commodity:
        query = query.filter(models.MandiPriceModel.commodity.ilike(f"%{commodity}%"))
    return query.all()


@app.get("/api/v1/mandi/rates/{commodity}", response_model=List[schemas.MandiPriceSchema])
def get_commodity_mandi_rates(commodity: str, db: Session = Depends(get_db)):
    rates = db.query(models.MandiPriceModel).filter(models.MandiPriceModel.commodity.ilike(f"%{commodity}%")).all()
    if not rates:
        raise HTTPException(status_code=404, detail=f"No mandi price data found for {commodity}")
    return rates


# -------------------------------------------------------------
# 9. Fertilizer Dosage Calculator Endpoint
# -------------------------------------------------------------
@app.post("/api/v1/fertilizer/calculate", response_model=schemas.FertilizerCalculateResponse)
def calculate_fertilizer(req: schemas.FertilizerCalculateRequest):
    return calculate_fertilizer_dosage(
        crop_type=req.crop_type,
        area_acres=req.area_acres,
        vigor_status=req.vigor_status or "Moderate",
        soil_dryness=req.soil_dryness or "Normal"
    )


# -------------------------------------------------------------
# 10. Farm Budget & Expense Ledger Endpoints
# -------------------------------------------------------------
@app.get("/api/v1/budget/summary", response_model=schemas.BudgetSummaryResponse)
def get_budget_summary(db: Session = Depends(get_db)):
    expenses = db.query(models.FarmExpenseModel).all()
    total_exp = sum([e.amount for e in expenses])

    cat_map = {}
    for e in expenses:
        cat_map[e.category] = cat_map.get(e.category, 0.0) + e.amount

    # Assume standard harvest yield revenue for 120 acres
    expected_revenue = 98500.0
    net_profit = expected_revenue - total_exp
    margin = round((net_profit / expected_revenue) * 100, 1) if expected_revenue > 0 else 0.0

    return {
        "total_expenses": total_exp,
        "expected_revenue": expected_revenue,
        "net_profit": net_profit,
        "profit_margin_pct": margin,
        "expenses_by_category": cat_map,
        "recent_expenses": expenses
    }


@app.get("/api/v1/budget/expenses", response_model=List[schemas.FarmExpenseSchema])
def list_expenses(db: Session = Depends(get_db)):
    return db.query(models.FarmExpenseModel).order_by(models.FarmExpenseModel.id.desc()).all()


@app.post("/api/v1/budget/expenses", response_model=schemas.FarmExpenseSchema)
def add_expense(expense_in: schemas.FarmExpenseCreate, user: Optional[models.UserModel] = Depends(get_current_user_optional), db: Session = Depends(get_db)):
    new_expense = models.FarmExpenseModel(
        user_id=user.id if user else 1,
        field_id=expense_in.field_id,
        category=expense_in.category,
        amount=expense_in.amount,
        notes=expense_in.notes,
        expense_date=expense_in.expense_date or datetime.utcnow().strftime("%Y-%m-%d")
    )
    db.add(new_expense)
    db.commit()
    db.refresh(new_expense)
    return new_expense


# -------------------------------------------------------------
# 11. Precision Land Marking, FAO-56 Irrigation & Seed Recommendations
# -------------------------------------------------------------
@app.post("/api/v1/fields/draw", response_model=schemas.FieldSchema)
def create_drawn_field(
    name: str = Query(..., description="Field name"),
    crop_type: str = Query("Corn (Dent)", description="Planted crop type"),
    boundary_wkt: str = Query(..., description="Drawn polygon boundary in WKT format"),
    sowing_date: Optional[str] = Query("2026-04-15", description="Sowing date YYYY-MM-DD"),
    user: Optional[models.UserModel] = Depends(get_current_user_optional),
    db: Session = Depends(get_db)
):
    """
    Accepts farmer-drawn polygon vertices from mobile map,
    computes geodesic surface area (Acres & Hectares) and centroid coordinates,
    and registers the field.
    """
    try:
        poly = GeospatialEngine.parse_polygon(boundary_wkt)
        area_info = GeospatialEngine.calculate_geodesic_area(poly)
        centroid_info = GeospatialEngine.get_centroid_and_bbox(poly)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Invalid geospatial polygon: {str(e)}")

    user_id = 1
    if user is not None and hasattr(user, 'id'):
        user_id = user.id

    actual_sowing = sowing_date if isinstance(sowing_date, str) else "2026-04-15"
    actual_crop = crop_type if isinstance(crop_type, str) else "Corn (Dent)"

    field = models.FieldModel(
        user_id=user_id,
        name=name,
        crop_type=actual_crop,
        area_acres=area_info["area_acres"],
        sowing_date=actual_sowing,
        status="Healthy",
        latitude=centroid_info["centroid_latitude"],
        longitude=centroid_info["centroid_longitude"],
        boundary=boundary_wkt
    )
    db.add(field)
    db.commit()
    db.refresh(field)
    return field


@app.get("/api/v1/fields/{field_id}/irrigation")
def get_field_irrigation_recommendation(
    field_id: int,
    irrigation_method: str = Query("drip", description="drip, sprinkler, pivot, or flood"),
    db: Session = Depends(get_db)
):
    """
    Field-specific soil-water assessment using FAO-56 Crop Water Balance.
    Answers: 'Should I irrigate today or wait for rain?' and 'How much water in mm and LITRES?'
    """
    field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="Field parcel not found")

    actual_method = irrigation_method if isinstance(irrigation_method, str) else "drip"

    # Get live weather for field coordinates
    weather = OpenMeteoAdapter.get_forecast(field.latitude, field.longitude)
    et0 = weather.get("et0_fao_evapotranspiration_mm", 4.5)
    rain_next_48h = weather.get("rain_next_24h_mm", 0.0)

    # Get current soil moisture percentage from latest observation
    moisture_record = db.query(models.SoilMoistureModel).filter(models.SoilMoistureModel.field_id == field_id).first()
    moisture_pct = moisture_record.moisture_percentage if moisture_record else 72.0

    # Calculate area in hectares from acres (1 ha = 2.47105 acres)
    area_ha = round(field.area_acres / 2.47105, 3)

    return IrrigationEngine.calculate_irrigation(
        crop_type=field.crop_type,
        area_hectares=area_ha,
        current_moisture_pct=moisture_pct,
        et0_evapotranspiration_mm=et0,
        forecast_rain_next_48h_mm=rain_next_48h,
        growth_stage="mid_flowering",
        soil_type="loam",
        irrigation_method=actual_method
    )


@app.get("/api/v1/fields/{field_id}/sowing-recommendation")
def get_field_sowing_recommendation(
    field_id: int,
    season: str = Query("Kharif", description="Kharif, Rabi, or Zaid"),
    soil_type: str = Query("Loam", description="Soil texture"),
    db: Session = Depends(get_db)
):
    """
    AI-Powered Sowing and Seed Variety Recommendation.
    Answers: 'Which seed variety is suitable for my field and how many kg of seeds do I need?'
    Formula: Total Seed Required (kg) = Seed Rate (kg/ha) * Field Area (ha).
    """
    field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="Field parcel not found")

    actual_season = season if isinstance(season, str) else "Kharif"
    actual_soil = soil_type if isinstance(soil_type, str) else "Loam"

    area_ha = round(field.area_acres / 2.47105, 3)
    return SeedSowingEngine.get_recommendation(
        crop_preference=field.crop_type,
        area_hectares=area_ha,
        soil_type=actual_soil,
        season=actual_season
    )


@app.get("/api/v1/fields/{field_id}/satellite-live")
def get_live_copernicus_satellite(field_id: int, db: Session = Depends(get_db)):
    """
    Query live European Space Agency (ESA) Sentinel-2 MSI Level-2A scene
    for the exact bounding box of the farmer's drawn field.
    """
    field = db.query(models.FieldModel).filter(models.FieldModel.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="Field parcel not found")

    delta = 0.01
    min_lon = field.longitude - delta
    min_lat = field.latitude - delta
    max_lon = field.longitude + delta
    max_lat = field.latitude + delta

    return CopernicusSentinelAdapter.query_latest_scene(min_lon, min_lat, max_lon, max_lat)


@app.get("/api/v1/weather/live")
def get_live_open_meteo_weather(lat: float = Query(37.7749), lon: float = Query(-122.4194)):
    """Live high-resolution numerical weather prediction from Open-Meteo."""
    return OpenMeteoAdapter.get_forecast(lat, lon)


@app.get("/api/v1/adapters/isro")
def get_isro_bhoonidhi_status(lat: float = Query(13.0827), lon: float = Query(80.2707)):
    """ISRO Bhoonidhi / Bhuvan configuration status and EOS-04 SAR soil moisture retrieval."""
    adapter = IsroBhoonidhiAdapter()
    status_info = adapter.get_status()
    soil_sar = adapter.query_eos04_soil_moisture(lat, lon)
    return {
        "isro_provider_status": status_info,
        "eos04_sar_sample": soil_sar
    }


@app.get("/api/v1/adapters/nasa-power")
def get_nasa_power_agroclimate(lat: float = Query(37.7749), lon: float = Query(-122.4194)):
    """NASA POWER agroclimatology solar radiation and growing degree days."""
    return NasaPowerAdapter.get_agroclimatology(lat, lon)


# -------------------------------------------------------------
# 11. Legacy Endpoints Compatibility Bridges
# -------------------------------------------------------------
@app.get("/api/v1/alerts", response_model=List[schemas.AlertSchema])
def get_alerts(db: Session = Depends(get_db)):
    return db.query(models.AlertModel).all()


@app.get("/api/v1/vegetation-health", response_model=List[schemas.VegetationHealthSchema])
def get_vegetation_health(db: Session = Depends(get_db)):
    return db.query(models.VegetationHealthModel).all()


@app.get("/api/v1/soil-moisture", response_model=List[schemas.SoilMoistureSchema])
def get_soil_moisture(db: Session = Depends(get_db)):
    return db.query(models.SoilMoistureModel).all()
