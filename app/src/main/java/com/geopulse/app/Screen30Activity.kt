package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import com.geopulse.app.ui.theme.GeoPulseTheme

class Screen30Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                GeoDetailScreenContent(
                    title = "Data Privacy & Deletion",
                    subtitle = "Manage your personal data, export farm records, or securely request account deletion.",
                    icon = Icons.Default.DeleteForever,
                    activity = this,
                    nextActivityClass = LoginActivity::class.java,
                    nextButtonText = "Return to Login"
                )
            }
        }
    }
}
