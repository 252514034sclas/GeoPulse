package com.geopulse.app

import android.app.Application
import com.geopulse.app.network.ApiClient
import com.geopulse.app.network.UserSession
import com.geopulse.app.ui.theme.ThemePreferences

class GeoPulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.initFromContext(this)
        UserSession.initSession(this)
        ThemePreferences.init(this)
    }
}
