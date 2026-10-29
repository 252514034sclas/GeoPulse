from typing import Dict, Any, List

def calculate_ndvi(nir: float, red: float) -> float:
    """Calculate Normalized Difference Vegetation Index: (NIR - RED) / (NIR + RED)"""
    denom = nir + red
    if denom == 0:
        return 0.0
    ndvi = (nir - red) / denom
    return round(max(0.0, min(1.0, ndvi)), 3)

def calculate_ndwi(nir: float, swir: float) -> float:
    """Calculate Normalized Difference Water Index: (NIR - SWIR) / (NIR + SWIR)"""
    denom = nir + swir
    if denom == 0:
        return 0.0
    ndwi = (nir - swir) / denom
    return round(max(0.0, min(1.0, ndwi)), 3)

def interpret_ndvi(ndvi: float) -> Dict[str, Any]:
    """Convert raw NDVI value into layman agricultural status and farmer guidance."""
    if ndvi >= 0.75:
        return {
            "status": "Optimal Vigor",
            "health_tier": "High",
            "canopy_cover_pct": 88.0,
            "chlorophyll_spad": 44.5,
            "advice": "Excellent crop vigor and dense canopy. Plants are absorbing solar energy efficiently. Maintain regular moisture.",
            "action_required": False
        }
    elif ndvi >= 0.55:
        return {
            "status": "Healthy / Good Vigor",
            "health_tier": "Moderate",
            "canopy_cover_pct": 74.0,
            "chlorophyll_spad": 38.0,
            "advice": "Steady vegetative growth. Consider split dose Urea nitrogen top-dressing to boost canopy spread before flowering.",
            "action_required": True
        }
    elif ndvi >= 0.35:
        return {
            "status": "Moderate Stress",
            "health_tier": "Low",
            "canopy_cover_pct": 52.0,
            "chlorophyll_spad": 29.5,
            "advice": "Mild yellowing or delayed emergence detected in quadrants. Check for weed competition or topsoil moisture deficit.",
            "action_required": True
        }
    else:
        return {
            "status": "Severe Under-Vigor",
            "health_tier": "Deficient",
            "canopy_cover_pct": 28.0,
            "chlorophyll_spad": 18.0,
            "advice": "Critical vegetation stress. Immediate field scouting recommended to rule out stem borers, leaf blight, or root drought.",
            "action_required": True
        }

def interpret_ndwi(ndwi: float) -> Dict[str, Any]:
    """Translate NDWI soil moisture into practical watering recommendation."""
    moisture_pct = round(ndwi * 100, 1)
    if moisture_pct >= 70.0:
        return {
            "moisture_pct": moisture_pct,
            "rating": "Adequate / Optimal",
            "advice": "Root-zone moisture is sufficient. Delay irrigation to avoid waterlogging and root fungal diseases."
        }
    elif moisture_pct >= 45.0:
        return {
            "moisture_pct": moisture_pct,
            "rating": "Moderate",
            "advice": "Topsoil drying normally. Schedule safe watering within the next 24 to 48 hours."
        }
    else:
        return {
            "moisture_pct": moisture_pct,
            "rating": "Dry / Moisture Deficit",
            "advice": "Soil is dry at root depth (30cm). Water field early in the morning before sun peaks."
        }
