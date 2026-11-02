package com.geopulse.app.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.*

data class GeodesicAreaResult(
    val areaSqMeters: Double,
    val areaHectares: Double,
    val areaAcres: Double
)

object GeospatialMath {

    /**
     * True geodesic surface area calculation on WGS-84 Earth ellipsoid/sphere.
     * Uses spherical excess formula with mean radius R = 6,371,008.8 meters,
     * identical to backend GeospatialEngine.
     */
    fun calculateGeodesicArea(points: List<LatLng>): GeodesicAreaResult {
        if (points.size < 3) return GeodesicAreaResult(0.0, 0.0, 0.0)

        val r = 6371008.8 // Mean Earth radius in meters
        var total = 0.0
        val n = points.size

        for (i in 0 until n) {
            val p1 = points[i]
            val p2 = points[(i + 1) % n]

            val phi1 = Math.toRadians(p1.latitude)
            val phi2 = Math.toRadians(p2.latitude)
            val deltaLambda = Math.toRadians(p2.longitude - p1.longitude)

            total += (deltaLambda * (2.0 + sin(phi1) + sin(phi2)))
        }

        val areaSqM = abs(total * (r * r) / 4.0)
        val areaHa = areaSqM / 10000.0
        val areaAcres = areaSqM / 4046.8564224

        return GeodesicAreaResult(
            areaSqMeters = round(areaSqM * 100.0) / 100.0,
            areaHectares = round(areaHa * 1000.0) / 1000.0,
            areaAcres = round(areaAcres * 100.0) / 100.0
        )
    }

    /**
     * Converts a list of LatLng vertices into Well-Known Text (WKT) Polygon format.
     */
    fun latLngListToWkt(points: List<LatLng>): String {
        if (points.isEmpty()) return ""
        val coords = points.toMutableList()
        // Ensure polygon exterior ring is closed
        if (coords.first() != coords.last()) {
            coords.add(coords.first())
        }
        val formatted = coords.joinToString(", ") { "${it.longitude} ${it.latitude}" }
        return "POLYGON (($formatted))"
    }

    /**
     * Parses WKT Polygon into List<LatLng> for MapLibre rendering.
     */
    fun parseWktToLatLngList(wkt: String): List<LatLng> {
        try {
            val cleaned = wkt.trim()
                .replace("POLYGON", "", ignoreCase = true)
                .replace("(", "")
                .replace(")", "")
                .trim()
            if (cleaned.isEmpty()) return emptyList()

            return cleaned.split(",").mapNotNull { pair ->
                val parts = pair.trim().split("\\s+".toRegex())
                if (parts.size >= 2) {
                    val lon = parts[0].toDoubleOrNull()
                    val lat = parts[1].toDoubleOrNull()
                    if (lat != null && lon != null) LatLng(lat, lon) else null
                } else null
            }
        } catch (_: Exception) {
            return emptyList()
        }
    }

    /**
     * Computes arithmetic centroid of polygon vertices.
     */
    fun getCentroid(points: List<LatLng>): LatLng {
        if (points.isEmpty()) return LatLng(13.06, 80.21)
        val avgLat = points.map { it.latitude }.average()
        val avgLon = points.map { it.longitude }.average()
        return LatLng(avgLat, avgLon)
    }

    /**
     * Computes LatLngBounds to fit camera to polygon.
     */
    fun getBounds(points: List<LatLng>): LatLngBounds? {
        if (points.isEmpty()) return null
        val builder = LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        return try {
            builder.build()
        } catch (_: Exception) {
            null
        }
    }
}
