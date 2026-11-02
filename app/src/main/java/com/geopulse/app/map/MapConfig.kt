package com.geopulse.app.map

import android.content.Context
import android.content.SharedPreferences
import org.maplibre.android.maps.Style

enum class BasemapType(val displayName: String, val badge: String) {
    SATELLITE("Satellite Imagery", "🛰️"),
    DARK_GIS("Dark GIS", "🌙"),
    LIGHT_GIS("Light GIS", "☀️"),
    STREETS("Streets (OSM)", "🗺️")
}

object MapConfig {

    private const val PREFS_NAME = "geopulse_map_prefs"
    private const val KEY_CUSTOM_STYLE_URL = "custom_map_style_url"
    private const val KEY_MAP_API_KEY = "map_provider_api_key"
    private const val KEY_SELECTED_BASEMAP = "selected_basemap_type"

    // Official Free & Open Map Styles
    const val URL_DARK_GIS = "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"
    const val URL_LIGHT_GIS = "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
    const val URL_STREETS = "https://demotiles.maplibre.org/style.json"

    // Default Map Center (Chennai / Indian Agricultural Belt)
    const val DEFAULT_LATITUDE = 13.0600
    const val DEFAULT_LONGITUDE = 80.2100
    const val DEFAULT_ZOOM = 13.5

    const val ATTRIBUTION_TEXT = "© MapLibre | © OpenStreetMap | © Esri World Imagery | © CARTO"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedBasemap(context: Context): BasemapType {
        val name = getPrefs(context).getString(KEY_SELECTED_BASEMAP, BasemapType.SATELLITE.name)
        return try {
            BasemapType.valueOf(name ?: BasemapType.SATELLITE.name)
        } catch (_: Exception) {
            BasemapType.SATELLITE
        }
    }

    fun setSelectedBasemap(context: Context, type: BasemapType) {
        getPrefs(context).edit().putString(KEY_SELECTED_BASEMAP, type.name).apply()
    }

    fun getCustomStyleUrl(context: Context): String? {
        return getPrefs(context).getString(KEY_CUSTOM_STYLE_URL, null)
    }

    fun setCustomStyleUrl(context: Context, url: String?) {
        getPrefs(context).edit().putString(KEY_CUSTOM_STYLE_URL, url?.trim()).apply()
    }

    fun getMapApiKey(context: Context): String? {
        return getPrefs(context).getString(KEY_MAP_API_KEY, null)
    }

    fun setMapApiKey(context: Context, key: String?) {
        getPrefs(context).edit().putString(KEY_MAP_API_KEY, key?.trim()).apply()
    }

    /**
     * Builds MapLibre Style.Builder for the requested basemap type.
     * Generates an authenticated or open style definition without hardcoding private keys.
     */
    fun createStyleBuilder(context: Context, type: BasemapType = getSelectedBasemap(context)): Style.Builder {
        val customUrl = getCustomStyleUrl(context)
        if (!customUrl.isNullOrEmpty()) {
            val key = getMapApiKey(context)
            val finalUrl = if (!key.isNullOrEmpty() && !customUrl.contains("key=")) {
                if (customUrl.contains("?")) "$customUrl&key=$key" else "$customUrl?key=$key"
            } else {
                customUrl
            }
            return Style.Builder().fromUri(finalUrl)
        }

        return when (type) {
            BasemapType.SATELLITE -> {
                Style.Builder().fromJson(getEsriSatelliteStyleJson())
            }
            BasemapType.DARK_GIS -> {
                Style.Builder().fromUri(URL_DARK_GIS)
            }
            BasemapType.LIGHT_GIS -> {
                Style.Builder().fromUri(URL_LIGHT_GIS)
            }
            BasemapType.STREETS -> {
                Style.Builder().fromUri(URL_STREETS)
            }
        }
    }

    /**
     * Self-contained MapLibre GL style loading Esri World Imagery 0-20 zoom satellite photography.
     */
    fun getEsriSatelliteStyleJson(): String {
        return """
        {
          "version": 8,
          "name": "Esri World Imagery",
          "sources": {
            "esri-satellite": {
              "type": "raster",
              "tiles": [
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
              ],
              "tileSize": 256,
              "maxzoom": 19,
              "attribution": "© Esri, Maxar, Earthstar Geographics, USDA, USGS"
            }
          },
          "layers": [
            {
              "id": "esri-satellite-layer",
              "type": "raster",
              "source": "esri-satellite",
              "minzoom": 0,
              "maxzoom": 20
            }
          ]
        }
        """.trimIndent()
    }
}
