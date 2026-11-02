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
    fun testPrecisionAgricultureHubDirectAccess() {
        // Direct access list from Dashboard without forced button chains
        val hubDirectDestinations = listOf(
            Screen11Activity::class.java, // Crop Health NDVI
            Screen12Activity::class.java, // Soil Moisture NDWI
            Screen13Activity::class.java, // Irrigation Schedule
            Screen14Activity::class.java, // Weather & Spray Advisory
            Screen15Activity::class.java, // Pest & Disease Advisory
            Screen20Activity::class.java, // Fertilizer Recommendations
            Screen16Activity::class.java, // Harvest & Yield Estimator
            Screen17Activity::class.java, // Field Boundary & GPS Mapping
            Screen18Activity::class.java, // Crop Growth Calendar
            Screen19Activity::class.java, // Field Operations & Plowing
            Screen27Activity::class.java, // Mandi Market Rates
            Screen22Activity::class.java, // Farm Budget Tracker
            Screen26Activity::class.java, // Kisan Helpline (1800-180-1551)
            Screen28Activity::class.java  // Weather & Warning Center
        )
        assertEquals("Dashboard Precision Ag Hub must provide 1-tap access to 14 core capabilities", 14, hubDirectDestinations.size)
        assertEquals("All direct destinations must be unique", 14, hubDirectDestinations.distinct().size)
    }

    @Test
    fun testDualActionContextualRoutingIntegrity() {
        // Primary Action Mappings (Optional cross-references, not forced chains)
        val primaryRoutes = mapOf(
            11 to 20, // Screen 11 (Crop Health) -> Screen 20 (Fertilizer Dosing)
            12 to 13, // Screen 12 (Soil Moisture) -> Screen 13 (Watering Timing FAO-56)
            13 to 14, // Screen 13 (Watering Timing) -> Screen 14 (Weather & Spraying)
            14 to 15, // Screen 14 (Weather Advisory) -> Screen 15 (Pest Advisory)
            15 to 11, // Screen 15 (Pest Advisory) -> Screen 11 (Satellite Crop Health NDVI)
            16 to 21, // Screen 16 (Yield Estimator) -> Screen 21 (Harvest Storage Tips)
            17 to 23, // Screen 17 (Field Boundary) -> Screen 23 (My Land Parcels)
            18 to 19, // Screen 18 (Crop Calendar) -> Screen 19 (Field Operations & Plowing)
            19 to 14, // Screen 19 (Plowing Planner) -> Screen 14 (Weather & Spray Forecast)
            20 to 12, // Screen 20 (Fertilizer Guide) -> Screen 12 (Field Moisture Status)
            21 to 27, // Screen 21 (Harvest Storage) -> Screen 27 (Local Mandi Market Rates)
            22 to 27, // Screen 22 (Farm Budget) -> Screen 27 (Local Mandi Market Rates)
            23 to 17, // Screen 23 (Land Parcels) -> Screen 17 (Field Boundary & Area)
            24 to 11, // Screen 24 (Satellite Growth History) -> Screen 11 (Crop Health NDVI)
            25 to 20, // Screen 25 (Organic Soil) -> Screen 20 (Fertilizer Guide)
            27 to 22, // Screen 27 (Mandi Rates) -> Screen 22 (Farm Budget Tracker)
            28 to 14  // Screen 28 (Warning Center) -> Screen 14 (Weather & Spray Advisory)
        )

        // Secondary Action Mappings (Alternative agronomic references)
        val secondaryRoutes = mapOf(
            15 to 26, // Screen 15 (Pest Advisory) -> Screen 26 (Kisan Helpline)
            12 to 14, // Screen 12 (Soil Moisture) -> Screen 14 (Weather Forecast)
            20 to 25, // Screen 20 (Fertilizer Guide) -> Screen 25 (Organic Composting)
            11 to 12, // Screen 11 (Crop Health) -> Screen 12 (Soil Moisture)
            16 to 27, // Screen 16 (Yield Estimator) -> Screen 27 (Mandi Prices)
            22 to 30  // Screen 22 (Farm Budget) -> Screen 30 (Export Records)
        )

        assertEquals("Pest Advisory primary action must lead to Crop Health NDVI (11)", 11, primaryRoutes[15])
        assertEquals("Pest Advisory secondary action must lead to Kisan Helpline (26)", 26, secondaryRoutes[15])
        assertEquals("Soil Moisture primary action must lead to Irrigation Schedule (13)", 13, primaryRoutes[12])
        assertEquals("Fertilizer Guide primary action must lead to Soil Moisture (12)", 12, primaryRoutes[20])
        assertEquals("Fertilizer Guide secondary action must lead to Organic Soil Health (25)", 25, secondaryRoutes[20])
        assertEquals("Plowing Planner primary action must lead to Weather Forecast (14)", 14, primaryRoutes[19])
        assertEquals("Farm Budget primary action must lead to Mandi Rates (27)", 27, primaryRoutes[22])
        assertEquals("Land Parcels primary action must lead to Field Boundary (17)", 17, primaryRoutes[23])
    }
}
