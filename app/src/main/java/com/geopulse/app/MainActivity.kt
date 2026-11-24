package com.geopulse.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.geopulse.app.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePreferences.init(this)
        com.geopulse.app.network.ApiClient.initFromContext(this)

        val target = intent?.getStringExtra("TARGET_SCREEN")
        if (!target.isNullOrEmpty()) {
            try {
                val clazz = Class.forName("com.geopulse.app.$target")
                startActivity(Intent(this, clazz).apply {
                    putExtras(intent)
                })
                finish()
                return
            } catch (_: Exception) {}
        }

        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}
