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
    private const val KEY_OWNED_FIELD_IDS = "owned_field_ids"

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

    fun saveOwnedFieldIds(context: Context, fieldIds: Collection<Int>) {
        val stringSet = fieldIds.map { it.toString() }.toSet()
        getPrefs(context).edit().putStringSet(KEY_OWNED_FIELD_IDS, stringSet).apply()
    }

    fun getOwnedFieldIds(context: Context): Set<Int> {
        val stringSet = getPrefs(context).getStringSet(KEY_OWNED_FIELD_IDS, null) ?: return emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun isFieldOwned(context: Context, fieldId: Int): Boolean {
        if (fieldId <= 0) return false
        val owned = getOwnedFieldIds(context)
        return validateFieldOwnership(fieldId, owned)
    }

    /**
     * Pure validation logic for field ownership.
     * Ensures arbitrary or cross-account field IDs are rejected.
     */
    fun validateFieldOwnership(fieldId: Int, ownedIds: Set<Int>): Boolean {
        if (fieldId <= 0) return false
        return ownedIds.contains(fieldId)
    }

    /**
     * Sanitizes requested field ID against owned IDs and current selection.
     * Prevents invalid or unauthorized IDs from replacing valid selected fields.
     */
    fun sanitizeSelectedFieldId(requestedId: Int?, ownedIds: Set<Int>, currentSelectedId: Int?): Int? {
        if (requestedId == null || requestedId <= 0) return null
        if (validateFieldOwnership(requestedId, ownedIds)) {
            return requestedId
        }
        // Refuse to overwrite valid selection with untrusted ID
        return if (currentSelectedId != null && validateFieldOwnership(currentSelectedId, ownedIds)) {
            currentSelectedId
        } else {
            ownedIds.minOrNull()
        }
    }

    fun setSelectedFieldIdIfOwned(context: Context, fieldId: Int?): Boolean {
        if (fieldId == null || fieldId <= 0) {
            setSelectedFieldId(context, null)
            return true
        }
        val owned = getOwnedFieldIds(context)
        if (owned.isNotEmpty() && validateFieldOwnership(fieldId, owned)) {
            setSelectedFieldId(context, fieldId)
            return true
        }
        return false
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
        saveOwnedFieldIds(context, availableFields.map { it.id })
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
