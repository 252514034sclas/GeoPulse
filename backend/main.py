from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from datetime import datetime
from typing import List
from shapely.geometry import Point
from shapely import wkt

import models
import schemas
from database import engine, get_db

models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="GeoPulse API",
    description="Python backend for GeoPulse agricultural intelligence app with geospatial PostGIS/Shapely capabilities.",
    version="1.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def seed_initial_data(db: Session):
    if db.query(models.FieldModel.id).first() is not None:
        return  # Already seeded

    # Seed Fields with WKT Polygon boundaries
    fields = [
        models.FieldModel(
            name="North Valley Plot",
            crop_type="Corn",
            area_acres=120.5,
            status="Optimal",
            latitude=34.0522,
            longitude=-118.2437,
            boundary="POLYGON((-118.26 34.04,-118.23 34.04,-118.23 34.07,-118.26 34.07,-118.26 34.04))"
        ),
        models.FieldModel(
            name="East Ridge Orchard",
            crop_type="Almonds",
            area_acres=85.0,
            status="Needs Attention",
            latitude=34.0622,
            longitude=-118.2537,
            boundary="POLYGON((-118.27 34.05,-118.24 34.05,-118.24 34.08,-118.27 34.08,-118.27 34.05))"
        ),
        models.FieldModel(
            name="South Creek Farm",
            crop_type="Soybeans",
            area_acres=210.0,
            status="Optimal",
            latitude=34.0422,
            longitude=-118.2337,
            boundary="POLYGON((-118.25 34.03,-118.22 34.03,-118.22 34.05,-118.25 34.05,-118.25 34.03))"
        )
    ]
    db.add_all(fields)
    db.commit()

    # Seed Alerts
    alerts = [
        models.AlertModel(field_id=2, severity="Medium", message="Soil moisture drop detected in East Ridge Orchard.", timestamp=datetime.utcnow(), is_resolved=False),
        models.AlertModel(field_id=1, severity="Low", message="Scheduled irrigation completed successfully.", timestamp=datetime.utcnow(), is_resolved=True)
    ]
    db.add_all(alerts)
    db.commit()

    # Seed Vegetation Health (NDVI - Demo with disclaimer)
    veg_health = [
        models.VegetationHealthModel(field_id=1, ndvi_value=0.78, health_status="Healthy", recorded_date="2025-03-01", is_sample=True, disclaimer="Demo sample NDVI data. Satellite imagery pipeline pending integration."),
        models.VegetationHealthModel(field_id=2, ndvi_value=0.52, health_status="Moderate Stress", recorded_date="2025-03-01", is_sample=True, disclaimer="Demo sample NDVI data. Satellite imagery pipeline pending integration."),
        models.VegetationHealthModel(field_id=3, ndvi_value=0.84, health_status="Very Healthy", recorded_date="2025-03-01", is_sample=True, disclaimer="Demo sample NDVI data. Satellite imagery pipeline pending integration.")
    ]
    db.add_all(veg_health)
    db.commit()

    # Seed Weather
    weather = models.WeatherModel(
        location="Central Valley Region",
        temperature_c=24.5,
        humidity_percent=45.0,
        precipitation_mm=0.0,
        wind_speed_kmh=12.5,
        status_message="Clear skies, sample demo weather conditions.",
        data_source="Simulated Demo Weather Service (Non-real-time)",
        updated_at=datetime.utcnow()
    )
    db.add(weather)
    db.commit()

    # Seed Soil Moisture
    moisture = [
        models.SoilMoistureModel(field_id=1, moisture_percentage=32.4, depth_cm=30.0, recorded_at="2025-03-01T10:00:00Z", data_source="Sample Sensor Reading (Demo)"),
        models.SoilMoistureModel(field_id=2, moisture_percentage=18.1, depth_cm=30.0, recorded_at="2025-03-01T10:00:00Z", data_source="Sample Sensor Reading (Demo)"),
        models.SoilMoistureModel(field_id=3, moisture_percentage=29.8, depth_cm=30.0, recorded_at="2025-03-01T10:00:00Z", data_source="Sample Sensor Reading (Demo)")
    ]
    db.add_all(moisture)
    db.commit()

@app.on_event("startup")
def startup_event():
    db = next(get_db())
    seed_initial_data(db)

@app.get("/health", response_model=schemas.HealthResponse)
def health_check():
    return {
        "status": "healthy",
        "version": "1.1.0",
        "timestamp": datetime.utcnow().isoformat()
    }

@app.get("/api/v1/dashboard", response_model=schemas.DashboardSummaryResponse)
def get_dashboard(db: Session = Depends(get_db)):
    fields = db.query(models.FieldModel).all()
    alerts = db.query(models.AlertModel).filter(models.AlertModel.is_resolved == False).all()
    moisture_records = db.query(models.SoilMoistureModel).all()
    avg_moisture = sum([m.moisture_percentage for m in moisture_records]) / len(moisture_records) if moisture_records else 0.0
    weather = db.query(models.WeatherModel).first()

    return {
        "total_fields": len(fields),
        "active_alerts_count": len(alerts),
        "average_soil_moisture": round(avg_moisture, 2),
        "system_status": "Operational (Demo/Sample Mode)",
        "data_disclaimer": "All dashboard metrics utilize sample seed data. Real-time telemetry and satellite NDVI integration are pending.",
        "fields": fields,
        "recent_alerts": alerts,
        "weather": weather
    }

@app.get("/api/v1/fields", response_model=List[schemas.FieldSchema])
def get_fields(db: Session = Depends(get_db)):
    return db.query(models.FieldModel).all()

@app.get("/api/v1/fields/spatial-query", response_model=List[schemas.FieldSchema])
def spatial_query_fields(lat: float, lon: float, db: Session = Depends(get_db)):
    """Spatial query endpoint performing point-in-polygon containment check using Shapely."""
    fields = db.query(models.FieldModel).all()
    point = Point(lon, lat)
    matching_fields = []
    for field in fields:
        if field.boundary:
            try:
                poly = wkt.loads(field.boundary)
                if poly.contains(point) or poly.touches(point):
                    matching_fields.append(field)
            except Exception:
                pass
    return matching_fields

@app.get("/api/v1/alerts", response_model=List[schemas.AlertSchema])
def get_alerts(db: Session = Depends(get_db)):
    return db.query(models.AlertModel).all()

@app.get("/api/v1/vegetation-health", response_model=List[schemas.VegetationHealthSchema])
def get_vegetation_health(db: Session = Depends(get_db)):
    return db.query(models.VegetationHealthModel).all()

@app.get("/api/v1/weather", response_model=schemas.WeatherSchema)
def get_weather(db: Session = Depends(get_db)):
    weather = db.query(models.WeatherModel).first()
    if not weather:
        raise HTTPException(status_code=404, detail="Weather information not found")
    return weather

@app.get("/api/v1/soil-moisture", response_model=List[schemas.SoilMoistureSchema])
def get_soil_moisture(db: Session = Depends(get_db)):
    return db.query(models.SoilMoistureModel).all()
