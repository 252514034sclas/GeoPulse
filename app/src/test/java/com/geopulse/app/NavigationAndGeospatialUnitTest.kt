package com.geopulse.app

import com.geopulse.app.map.BasemapType
import com.geopulse.app.map.GeospatialMath
import com.geopulse.app.map.MapConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class NavigationAndGeospatialUnitTest {

    @Test
    fun testGeodesicAreaCalculation() {
        // Test polygon of known farm field in Tamil Nadu agricultural delta (~13.06 N, 80.21 E)
        val vertices = listOf(
            LatLng(13.0610, 80.2020),
            LatLng(13.0615, 80.2100),
            LatLng(13.0680, 80.2090),
            LatLng(13.0675, 80.2020)
        )
        val areaResult = GeospatialMath.calculateGeodesicArea(vertices)

        assertTrue("Area in sq meters should be positive", areaResult.areaSqMeters > 0)
        assertTrue("Area in hectares should be within realistic farm scale (20-40 ha)", areaResult.areaHectares in 20.0..40.0)
        assertTrue("Area in acres should correspond to ~2.471 * ha", areaResult.areaAcres in 50.0..90.0)
    }

    @Test
    fun testWktParsingAndFormatting() {
        val originalPoints = listOf(
            LatLng(13.060, 80.200),
            LatLng(13.065, 80.200),
            LatLng(13.065, 80.205),
            LatLng(13.060, 80.205)
        )
        val wkt = GeospatialMath.latLngListToWkt(originalPoints)
        assertTrue("WKT should start with POLYGON", wkt.startsWith("POLYGON (("))
        assertTrue("WKT should end with ))", wkt.endsWith("))"))

        val parsed = GeospatialMath.parseWktToLatLngList(wkt)
        assertEquals("Parsed points should match original vertices plus closing point", 5, parsed.size)
        assertEquals("First and last point must match in closed polygon ring", parsed.first().latitude, parsed.last().latitude, 0.0001)
        assertEquals("First and last point must match in closed polygon ring", parsed.first().longitude, parsed.last().longitude, 0.0001)
    }

    @Test
    fun testCentroidAndBoundsCalculation() {
        val points = listOf(
            LatLng(13.050, 80.200),
            LatLng(13.070, 80.220)
        )
        val centroid = GeospatialMath.getCentroid(points)
        assertEquals("Centroid latitude should be midpoint", 13.060, centroid.latitude, 0.001)
        assertEquals("Centroid longitude should be midpoint", 80.210, centroid.longitude, 0.001)

        val bounds = GeospatialMath.getBounds(points)
        assertNotNull("Bounds should be generated", bounds)
        assertEquals("Southwest lat", 13.050, bounds!!.latitudeSouth, 0.001)
        assertEquals("Northeast lat", 13.070, bounds.latitudeNorth, 0.001)
    }

    @Test
    fun testBasemapPresetsConfig() {
        val satelliteStyle = MapConfig.getEsriSatelliteStyleJson()
        assertTrue("Satellite style must contain Esri tile server URL", satelliteStyle.contains("server.arcgisonline.com"))
        assertTrue("Satellite style must configure raster source", satelliteStyle.contains("\"type\": \"raster\""))

        assertEquals("Dark GIS URL should point to CARTO Dark Matter", MapConfig.URL_DARK_GIS, "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json")
        assertEquals("Light GIS URL should point to CARTO Positron", MapConfig.URL_LIGHT_GIS, "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
        assertEquals("Streets URL should point to MapLibre Demotiles", MapConfig.URL_STREETS, "https://demotiles.maplibre.org/style.json")

        assertEquals("Default basemap badge", "🛰️", BasemapType.SATELLITE.badge)
        assertEquals("Dark GIS badge", "🌙", BasemapType.DARK_GIS.badge)
        assertEquals("Light GIS badge", "☀️", BasemapType.LIGHT_GIS.badge)
        assertEquals("Streets badge", "🗺️", BasemapType.STREETS.badge)
    }

    @Test
    fun testScreenActivitiesExistAndAreDistinct() {
        val screenClasses = listOf(
            Screen7Activity::class.java,
            Screen8Activity::class.java,
            Screen9Activity::class.java,
            Screen10Activity::class.java,
            Screen11Activity::class.java,
            Screen12Activity::class.java,
            Screen13Activity::class.java,
            Screen14Activity::class.java,
            Screen15Activity::class.java,
            Screen16Activity::class.java,
            Screen17Activity::class.java,
            Screen18Activity::class.java,
            Screen19Activity::class.java,
            Screen20Activity::class.java,
            Screen21Activity::class.java,
            Screen22Activity::class.java,
            Screen23Activity::class.java,
            Screen24Activity::class.java,
            Screen25Activity::class.java,
            Screen26Activity::class.java,
            Screen27Activity::class.java,
            Screen28Activity::class.java,
            Screen29Activity::class.java,
            Screen30Activity::class.java
        )
        assertEquals("All 24 screen activities (7 to 30) must be loaded", 24, screenClasses.size)
        assertEquals("All activities must be unique", 24, screenClasses.distinct().size)
    }
}
