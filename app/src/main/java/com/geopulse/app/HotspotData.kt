package com.geopulse.app

data class HotspotData(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val action: () -> Unit
)
