package com.geopulse.app.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.geopulse.app.network.UserSession
import java.util.Locale

/**
 * GeoLanguageManager: Centralized reactive language & localization manager for GeoPulse.
 * Provides instant reactive recomposition across all Jetpack Compose screens,
 * synchronizes with UserSession and SharedPreferences, and updates Android system Configuration.
 */
object GeoLanguageManager {
    private const val PREFS_NAME = "geopulse_language_prefs"
    private const val KEY_LANGUAGE = "preferred_language"

    // Reactive Compose state for instant recomposition
    var currentLanguage by mutableStateOf("en")

    val isTamil: Boolean
        get() = currentLanguage == "ta"

    /**
     * Translates an inline string pair reactively based on current language state.
     */
    fun t(en: String, ta: String): String = if (isTamil) ta else en

    /**
     * Initialize language state from persistent storage during app startup.
     */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(KEY_LANGUAGE, null)
            ?: UserSession.getLanguage(context)
        currentLanguage = if (savedLang == "ta") "ta" else "en"
        applySystemLocale(context, currentLanguage)
    }

    /**
     * Switch language dynamically, updating reactive state and persistent storage.
     */
    fun setLanguage(context: Context, lang: String) {
        val normalized = if (lang.lowercase().startsWith("ta")) "ta" else "en"
        currentLanguage = normalized

        // Persist to SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, normalized)
            .apply()

        // Synchronize with UserSession
        UserSession.setLanguage(context, normalized)

        // Update Android system locale configuration
        applySystemLocale(context, normalized)
    }

    /**
     * Update Android system Locale and Configuration.
     */
    fun applySystemLocale(context: Context, lang: String) {
        try {
            val locale = if (lang == "ta") Locale("ta", "IN") else Locale.ENGLISH
            Locale.setDefault(locale)
            val resources = context.resources
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (_: Exception) {
            // Fallback gracefully on older Android versions or sandboxed environments
        }
    }

    // --- Agricultural Units & Formatting Helpers ---

    fun formatArea(acres: Double): String =
        if (isTamil) "%.2f ஏக்கர்".format(Locale.US, acres) else "%.2f acres".format(Locale.US, acres)

    fun formatVolume(litres: Long): String =
        if (isTamil) "%,d லிட்டர்".format(Locale.US, litres) else "%,d Litres".format(Locale.US, litres)

    fun formatRain(mm: Double): String =
        if (isTamil) "%.1f மி.மீ".format(Locale.US, mm) else "%.1f mm".format(Locale.US, mm)

    fun formatTemperature(tempC: Double): String =
        "%.1f°C".format(Locale.US, tempC)

    fun formatWindSpeed(kmh: Double): String =
        if (isTamil) "%.1f கி.மீ/மணி".format(Locale.US, kmh) else "%.1f km/h".format(Locale.US, kmh)

    fun formatPrice(inr: Double): String =
        if (isTamil) "₹%.0f / குவின்டால்".format(Locale.US, inr) else "₹%.0f / quintal".format(Locale.US, inr)

    fun formatWeightKg(kg: Double): String =
        if (isTamil) "%.1f கிலோ".format(Locale.US, kg) else "%.1f kg".format(Locale.US, kg)
}
