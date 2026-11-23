from typing import Dict, Any, List

def calculate_ndvi(nir: float, red: float) -> float:
    """Calculate Normalized Difference Vegetation Index: (NIR - RED) / (NIR + RED)"""
    denom = nir + red
    if denom == 0:
        return 0.0
    ndvi = (nir - red) / denom
    return round(max(0.0, min(1.0, ndvi)), 3)

def calculate_evi(nir: float, red: float, blue: float) -> float:
    """
    Calculate Enhanced Vegetation Index (EVI):
    Formula: 2.5 * ((NIR - RED) / (NIR + 6*RED - 7.5*BLUE + 1.0))
    Reduces atmospheric noise and canopy background interference.
    """
    denom = nir + (6.0 * red) - (7.5 * blue) + 1.0
    if denom == 0:
        return 0.0
    evi = 2.5 * ((nir - red) / denom)
    return round(max(-1.0, min(1.0, evi)), 3)

def calculate_ndwi(nir: float, swir: float) -> float:
    """
    Calculate Normalized Difference Water / Moisture Index (NDMI / Gao NDWI):
    Formula: (NIR - SWIR) / (NIR + SWIR)
    Uses Sentinel-2 Band 8 (NIR 842nm) and Band 11 (SWIR 1610nm).
    Sensitive to liquid water molecules stored inside leaf mesophyll cell walls.
    """
    denom = nir + swir
    if denom == 0:
        return 0.0
    ndwi = (nir - swir) / denom
    return round(max(-1.0, min(1.0, ndwi)), 3)

def calculate_ndre(nir: float, red_edge: float) -> float:
    """
    Calculate Normalized Difference Red Edge Index (NDRE):
    Formula: (NIR - RE) / (NIR + RE)
    Uses Sentinel-2 Band 8 (NIR) and Band 5 (Red Edge 705nm).
    Critical for mid-to-late growth stages where standard NDVI saturates.
    Directly correlates with leaf chlorophyll content and nitrogen status.
    """
    denom = nir + red_edge
    if denom == 0:
        return 0.0
    ndre = (nir - red_edge) / denom
    return round(max(-1.0, min(1.0, ndre)), 3)

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
    """
    Translate NDMI (Canopy Moisture Index) into practical hydration and irrigation guidance.
    Physical Note: Optical SWIR (Sentinel-2 B8/B11) measures canopy liquid water absorption
    and topsoil moisture proxy. Root-zone moisture balance requires FAO-56 hydrological modeling.
    """
    hydration_pct = round(max(0.0, min(100.0, (ndwi + 0.2) / 0.8 * 100.0)), 1)
    if ndwi >= 0.35:
        return {
            "moisture_pct": hydration_pct,
            "canopy_hydration_pct": hydration_pct,
            "rating": "Optimal Canopy Hydration",
            "advice": "Abundant foliage water content and cellular turgor. No moisture stress detected in canopy.",
            "scientific_note": "Optical SWIR reflects canopy liquid water absorption. Deep root-zone water balance is modeled separately."
        }
    elif ndwi >= 0.15:
        return {
            "moisture_pct": hydration_pct,
            "canopy_hydration_pct": hydration_pct,
            "rating": "Moderate / Adequate Hydration",
            "advice": "Normal transpiration rate. Topsoil skin layer is drying; monitor root-zone water balance for scheduled irrigation.",
            "scientific_note": "Canopy is transpiring adequately; check FAO-56 root depletion to schedule replenishment."
        }
    elif ndwi >= -0.05:
        return {
            "moisture_pct": hydration_pct,
            "canopy_hydration_pct": hydration_pct,
            "rating": "Mild Canopy Dehydration",
            "advice": "Canopy water absorption indicates early moisture stress. Re-hydrate field within 24 to 48 hours.",
            "scientific_note": "Decreasing cellular turgor detected in canopy reflectance. Soil water replenishment needed."
        }
    else:
        return {
            "moisture_pct": hydration_pct,
            "canopy_hydration_pct": hydration_pct,
            "rating": "Severe Water Stress",
            "advice": "Canopy is severely dehydrated. Urgent irrigation required to prevent permanent wilting and yield loss.",
            "scientific_note": "Critical leaf desiccation. Verify root-zone irrigation requirement immediately."
        }

def interpret_ndre(ndre: float) -> Dict[str, Any]:
    """
    Interpret Red Edge Index (NDRE) for chlorophyll concentration and nitrogen nutrition,
    especially effective in dense canopies where NDVI saturates (NDVI > 0.70).
    """
    if ndre >= 0.40:
        return {
            "status": "High Nitrogen & Chlorophyll",
            "nitrogen_status": "Optimal",
            "advice": "Canopy is rich in active chlorophyll. Withhold extra nitrogen to avoid vegetative overgrowth or lodging."
        }
    elif ndre >= 0.25:
        return {
            "status": "Adequate Chlorophyll",
            "nitrogen_status": "Sufficient",
            "advice": "Balanced nitrogen levels. Proceed with planned standard crop maintenance."
        }
    else:
        return {
            "status": "Low Chlorophyll / Nitrogen Chlorosis",
            "nitrogen_status": "Deficient",
            "advice": "Canopy shows signs of early nitrogen chlorosis. Schedule split dose nitrogen top-dressing."
        }
