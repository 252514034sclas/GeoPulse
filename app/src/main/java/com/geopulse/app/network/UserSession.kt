package com.geopulse.app.network

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREFS_NAME = "geopulse_user_session"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_FARM_NAME = "farm_name"
    private const val KEY_TOTAL_AREA_HA = "total_area_ha"
    private const val KEY_LANGUAGE = "preferred_language"
    private const val KEY_SELECTED_FIELD_ID = "selected_field_id"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(context: Context, token: String, user: UserDto) {
        ApiClient.authToken = token
        getPrefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_FULL_NAME, user.fullName)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_FARM_NAME, user.farmName ?: "My Farm")
            .putFloat(KEY_TOTAL_AREA_HA, (user.totalAreaHa ?: 10.0).toFloat())
            .putString(KEY_LANGUAGE, user.preferredLanguage ?: "en")
            .apply()
    }

    fun initSession(context: Context) {
        val token = getPrefs(context).getString(KEY_TOKEN, null)
        if (!token.isNullOrEmpty()) {
            ApiClient.authToken = token
        }
    }

    fun getToken(context: Context): String? = getPrefs(context).getString(KEY_TOKEN, null)

    fun getFullName(context: Context): String =
        getPrefs(context).getString(KEY_FULL_NAME, "") ?: ""

    fun getFarmName(context: Context): String =
        getPrefs(context).getString(KEY_FARM_NAME, "") ?: ""

    fun getEmail(context: Context): String =
        getPrefs(context).getString(KEY_EMAIL, "") ?: ""

    fun getUserId(context: Context): Int =
        getPrefs(context).getInt(KEY_USER_ID, -1)

    fun getLanguage(context: Context): String =
        getPrefs(context).getString(KEY_LANGUAGE, "en") ?: "en"

    fun setLanguage(context: Context, lang: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun getSelectedFieldId(context: Context): Int? {
        val id = getPrefs(context).getInt(KEY_SELECTED_FIELD_ID, -1)
        return if (id != -1) id else null
    }

    fun setSelectedFieldId(context: Context, fieldId: Int?) {
        val editor = getPrefs(context).edit()
        if (fieldId != null && fieldId > 0) {
            editor.putInt(KEY_SELECTED_FIELD_ID, fieldId)
        } else {
            editor.remove(KEY_SELECTED_FIELD_ID)
        }
        editor.apply()
    }

    fun resolveSelectedField(context: Context, availableFields: List<FieldDto>): FieldDto? {
        if (availableFields.isEmpty()) {
            setSelectedFieldId(context, null)
            return null
        }
        val savedId = getSelectedFieldId(context)
        val matched = availableFields.find { it.id == savedId }
        if (matched != null) return matched

        val fallback = availableFields.first()
        setSelectedFieldId(context, fallback.id)
        return fallback
    }

    fun isLoggedIn(context: Context): Boolean =
        !getToken(context).isNullOrEmpty()

    fun clearSession(context: Context) {
        ApiClient.authToken = null
        getPrefs(context).edit().clear().apply()
    }
}
