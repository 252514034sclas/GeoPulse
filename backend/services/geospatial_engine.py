import math
from typing import Dict, Any, List, Tuple, Optional
from shapely.geometry import Polygon, Point, mapping
from shapely import wkt
import json

class GeospatialEngine:
    """
    Precision Geospatial Calculation Engine for GeoPulse.
    Handles user-drawn field polygons, accurate geodesic acreage & hectare calculation,
    centroid determination, and bounding box generation for satellite remote sensing pipelines.
    """

    @staticmethod
    def parse_polygon(wkt_or_geojson: str) -> Polygon:
        """Parse WKT or GeoJSON string into a Shapely Polygon."""
        text = wkt_or_geojson.strip()
        if text.startswith("{"):
            data = json.loads(text)
            geom_type = data.get("type", "")
            if geom_type == "Polygon":
                coords = data.get("coordinates", [[]])[0]
                return Polygon(coords)
            elif geom_type == "Feature":
                coords = data.get("geometry", {}).get("coordinates", [[]])[0]
                return Polygon(coords)
            else:
                raise ValueError(f"Unsupported GeoJSON geometry type: {geom_type}")
        else:
            return wkt.loads(text)

    @staticmethod
    def calculate_geodesic_area(polygon: Polygon) -> Dict[str, float]:
        """
        Calculate true surface area of a polygon defined in WGS84 coordinates (Latitude, Longitude).
        Uses spherical Earth approximation with mean radius R = 6,371,008 meters.
        """
        coords = list(polygon.exterior.coords)
        if len(coords) < 4:
            return {"area_sq_meters": 0.0, "area_hectares": 0.0, "area_acres": 0.0}

        # Spherical excess / polygon geodesic area algorithm
        total = 0.0
        n = len(coords) - 1
        r = 6371008.8  # Earth radius in meters

        for i in range(n):
            lon1, lat1 = coords[i]
            lon2, lat2 = coords[i + 1]

            phi1 = math.radians(lat1)
            phi2 = math.radians(lat2)
            delta_lambda = math.radians(lon2 - lon1)

            total += (delta_lambda * (2 + math.sin(phi1) + math.sin(phi2)))

        area_sq_m = abs(total * (r * r) / 4.0)

        # Standard agricultural conversion constants
        # 1 Hectare = 10,000 square meters
        # 1 Acre = 4,046.8564224 square meters
        # 1 Hectare = 2.47105 acres
        area_ha = area_sq_m / 10000.0
        area_acres = area_sq_m / 4046.8564224

        return {
            "area_sq_meters": round(area_sq_m, 2),
            "area_hectares": round(area_ha, 3),
            "area_acres": round(area_acres, 2)
        }

    @staticmethod
    def get_centroid_and_bbox(polygon: Polygon) -> Dict[str, Any]:
        """Compute centroid and bounding box for satellite scene querying."""
        centroid = polygon.centroid
        min_lon, min_lat, max_lon, max_lat = polygon.bounds

        return {
            "centroid_latitude": round(centroid.y, 6),
            "centroid_longitude": round(centroid.x, 6),
            "bbox": [round(min_lon, 6), round(min_lat, 6), round(max_lon, 6), round(max_lat, 6)]
        }

    @staticmethod
    def to_geojson(polygon: Polygon, properties: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Export polygon as standard RFC 7946 GeoJSON Feature."""
        return {
            "type": "Feature",
            "geometry": mapping(polygon),
            "properties": properties or {}
        }
