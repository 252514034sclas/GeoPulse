package com.geopulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.geopulse.app.ui.theme.GeoPulseTheme

abstract class BaseScreenActivity : ComponentActivity() {
    abstract val imageRes: Int
    open val background: Color = Color(0xFFF0F1ED)
    open val hotspots: List<HotspotData> = emptyList()
    open val onTap: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPulseTheme {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(background)
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "GeoPulse screen",
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (onTap != null) {
                                    Modifier.clickable { onTap?.invoke() }
                                } else {
                                    Modifier
                                }
                            ),
                        contentScale = ContentScale.FillBounds
                    )

                    hotspots.forEach { hotspot ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = maxWidth * hotspot.x,
                                    y = maxHeight * hotspot.y
                                )
                                .size(
                                    width = maxWidth * hotspot.width,
                                    height = maxHeight * hotspot.height
                                )
                                .clickable { hotspot.action() }
                        )
                    }
                }
            }
        }
    }
}
