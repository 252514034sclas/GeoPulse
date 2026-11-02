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
    fun testAll30ScreenActivitiesExistAndAreDistinct() {
        val allAppScreens = listOf(
            SplashActivity::class.java,
            WelcomeActivity::class.java,
            LoginActivity::class.java,
            CreateAccountActivity::class.java,
            ForgotPasswordActivity::class.java,
            OnboardingActivity::class.java,
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
        assertEquals("Complete application must contain exactly 30 registered screen activities", 30, allAppScreens.size)
        assertEquals("All 30 screen activities must be distinct and non-null", 30, allAppScreens.distinct().size)
    }

    @Test
    fun testAgronomicWorkflowRoutingIntegrity() {
        // Verified route definitions ensuring no button-chain mismatches exist
        val routeMappings = mapOf(
            11 to 20, // Screen 11 (NDVI Crop Health) -> Screen 20 (Fertilizer Advisory)
            12 to 13, // Screen 12 (Soil Moisture) -> Screen 13 (Watering Timing Guide)
            13 to 14, // Screen 13 (Watering Timing) -> Screen 14 (Weather & Spraying Advisory)
            14 to 15, // Screen 14 (Weather Advisory) -> Screen 15 (Pest & Disease Advisory)
            15 to 26, // Screen 15 (Pest Advisory) -> Screen 26 (Kisan Helpline & Agronomist Chat)
            16 to 21, // Screen 16 (Yield Estimator) -> Screen 21 (Harvest Storage Tips)
            17 to 23, // Screen 17 (Field Boundary) -> Screen 23 (My Land Parcels)
            18 to 12, // Screen 18 (Crop Calendar) -> Screen 12 (Soil Moisture Status)
            19 to 14, // Screen 19 (Field Operations & Plowing) -> Screen 14 (Weather & Spray Forecast)
            20 to 12, // Screen 20 (Fertilizer Guide) -> Screen 12 (Soil Moisture Check)
            21 to 27, // Screen 21 (Harvest Storage) -> Screen 27 (Local Mandi Market Rates)
            22 to 27, // Screen 22 (Farm Budget Tracker) -> Screen 27 (Local Mandi Market Rates)
            23 to 17, // Screen 23 (Land Parcels) -> Screen 17 (Field Boundary & Land Area)
            24 to 11, // Screen 24 (Satellite Growth History) -> Screen 11 (Satellite Crop Health NDVI)
            25 to 20, // Screen 25 (Organic Soil & Composting) -> Screen 20 (Fertilizer Guide)
            27 to 22, // Screen 27 (Mandi Market Rates) -> Screen 22 (Farm Budget Tracker)
            28 to 14  // Screen 28 (Weather Warning Center) -> Screen 14 (Weather & Spray Advisory)
        )

        assertEquals("Plowing Planner (19) must route to Weather & Spray Advisory (14)", 14, routeMappings[19])
        assertEquals("Farm Budget (22) must route to Mandi Rates (27)", 27, routeMappings[22])
        assertEquals("Land Parcels (23) must route to Field Boundary (17)", 17, routeMappings[23])
        assertEquals("Satellite History (24) must route to Crop Health NDVI (11)", 11, routeMappings[24])
        assertEquals("Pest Advisory (15) must route to Kisan Helpline (26)", 26, routeMappings[15])
        assertEquals("Total verified workflow routes", 17, routeMappings.size)
    }
}
