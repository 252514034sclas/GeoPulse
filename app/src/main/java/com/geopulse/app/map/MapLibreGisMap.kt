package com.geopulse.app.map

import android.graphics.Color as AndroidColor
import android.graphics.PointF
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.geopulse.app.GisZone
import com.geopulse.app.ui.theme.EmeraldPrimary
import com.geopulse.app.ui.theme.LimeAccent
import com.geopulse.app.ui.theme.PineDark
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polygon
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun rememberMapLibreViewWithLifecycle(): MapView {
    val context = LocalContext.current
    // Initialize MapLibre Native instance
    remember {
        MapLibre.getInstance(context)
    }

    val mapView = remember {
        MapView(context).apply {
            id = android.view.View.generateViewId()
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
    return mapView
}

@Composable
fun MapLibreGisMap(
    modifier: Modifier = Modifier,
    zones: List<GisZone>,
    selectedZone: GisZone?,
    onZoneSelected: (GisZone) -> Unit,
    isDrawingMode: Boolean,
    drawnPoints: List<LatLng>,
    onMapTappedInDrawingMode: (LatLng) -> Unit,
    activeBasemap: BasemapType = BasemapType.SATELLITE,
    onBasemapChange: (BasemapType) -> Unit = {}
) {
    val context = LocalContext.current
    val mapView = rememberMapLibreViewWithLifecycle()
    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentStyleType by remember { mutableStateOf(activeBasemap) }
    var showBasemapDropdown by remember { mutableStateOf(false) }

    // Use rememberUpdatedState so long-lived MapLibre listeners always access the freshest Compose state
    val currentDrawingMode by rememberUpdatedState(isDrawingMode)
    val currentOnMapTapped by rememberUpdatedState(onMapTappedInDrawingMode)
    val currentZones by rememberUpdatedState(zones)
    val currentOnZoneSelected by rememberUpdatedState(onZoneSelected)

    // Keep track of active map annotations
    val renderedPolygons = remember { mutableListOf<Polygon>() }
    val renderedPolylines = remember { mutableListOf<Polyline>() }
    val renderedMarkers = remember { mutableListOf<Marker>() }

    // Helper to redraw all parcel boundaries and active drawing onto the MapLibre map
    fun syncMapAnnotations(map: MapLibreMap) {
        // Clear previous annotations
        renderedPolygons.forEach { map.removePolygon(it) }
        renderedPolygons.clear()
        renderedPolylines.forEach { map.removePolyline(it) }
        renderedPolylines.clear()
        renderedMarkers.forEach { map.removeMarker(it) }
        renderedMarkers.clear()

        // 1. Render all saved field parcels
        zones.forEach { zone ->
            val points = zone.geoPoints
            if (points.size >= 3) {
                val isSelected = zone.id == selectedZone?.id
                val fillColor = if (isSelected) {
                    AndroidColor.argb(120, 129, 199, 132) // Translucent Lime
                } else {
                    AndroidColor.argb(80, 46, 125, 50)   // Translucent Forest Green
                }
                val strokeColor = if (isSelected) AndroidColor.WHITE else AndroidColor.parseColor("#81C784")

                val polygon = map.addPolygon(
                    PolygonOptions()
                        .addAll(points)
                        .fillColor(fillColor)
                        .strokeColor(strokeColor)
                )
                renderedPolygons.add(polygon)

                // Place Centroid pin with field label
                val centroid = GeospatialMath.getCentroid(points)
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(centroid)
                        .title(zone.name)
                        .snippet("${zone.acreage} · ${zone.crop}")
                )
                renderedMarkers.add(marker)
            }
        }

        // 2. Render active drawing points & connecting lines if in drawing mode
        if (drawnPoints.isNotEmpty()) {
            if (drawnPoints.size >= 3) {
                val previewPolygon = map.addPolygon(
                    PolygonOptions()
                        .addAll(drawnPoints)
                        .fillColor(AndroidColor.argb(90, 255, 207, 85)) // Translucent Gold
                        .strokeColor(AndroidColor.parseColor("#FFCF55"))
                )
                renderedPolygons.add(previewPolygon)
            } else if (drawnPoints.size == 2) {
                val polyline = map.addPolyline(
                    PolylineOptions()
                        .addAll(drawnPoints)
                        .color(AndroidColor.parseColor("#FFCF55"))
                        .width(3.0f)
                )
                renderedPolylines.add(polyline)
            }

            // Draw vertex handles
            drawnPoints.forEachIndexed { idx, pt ->
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(pt)
                        .title("Node #${idx + 1}")
                )
                renderedMarkers.add(marker)
            }
        }
    }

    // Effect: Re-apply style when basemap toggle changes
    LaunchedEffect(activeBasemap, mapLibreMapInstance) {
        val map = mapLibreMapInstance ?: return@LaunchedEffect
        if (currentStyleType != activeBasemap) {
            val styleBuilder = MapConfig.createStyleBuilder(context, activeBasemap)
            map.setStyle(styleBuilder) {
                syncMapAnnotations(map)
            }
            currentStyleType = activeBasemap
        }
    }

    // Effect: Sync annotations whenever zones, selectedZone, or drawnPoints change
    LaunchedEffect(zones, selectedZone, drawnPoints, mapLibreMapInstance) {
        val map = mapLibreMapInstance ?: return@LaunchedEffect
        syncMapAnnotations(map)
    }

    // Effect: Animate camera to selected field boundary
    LaunchedEffect(selectedZone, mapLibreMapInstance) {
        val map = mapLibreMapInstance ?: return@LaunchedEffect
        val zone = selectedZone ?: return@LaunchedEffect
        if (zone.geoPoints.size >= 3) {
            val bounds = GeospatialMath.getBounds(zone.geoPoints)
            if (bounds != null) {
                try {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64), 800)
                } catch (_: Exception) {
                    val centroid = GeospatialMath.getCentroid(zone.geoPoints)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(centroid, 14.5), 800)
                }
            }
        }
    }

    Box(
        modifier = modifier.pointerInput(currentDrawingMode, mapLibreMapInstance) {
            if (currentDrawingMode) {
                detectTapGestures { offset ->
                    val map = mapLibreMapInstance
                    if (map != null) {
                        val latLng = map.projection.fromScreenLocation(PointF(offset.x, offset.y))
                        android.util.Log.d("GeoPulseMap", "Compose tap converted to LatLng: lat=${latLng.latitude}, lon=${latLng.longitude}")
                        currentOnMapTapped(latLng)
                    }
                }
            }
        }
    ) {
        // 1. Real MapLibre Native Map View with Multi-Touch Gesture Protection
        AndroidView(
            factory = {
                mapView.apply {
                    // Prevent parent Compose LazyColumn and HorizontalPager from intercepting multi-touch pinch/pan gestures
                    setOnTouchListener { v, event ->
                        android.util.Log.d("GeoPulseTouch", "MapView touch: action=${event.actionMasked}, pointers=${event.pointerCount}, x=${event.x}, y=${event.y}")
                        when (event.actionMasked) {
                            android.view.MotionEvent.ACTION_DOWN,
                            android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                            android.view.MotionEvent.ACTION_UP,
                            android.view.MotionEvent.ACTION_CANCEL -> {
                                if (event.pointerCount <= 1) {
                                    v.parent?.requestDisallowInterceptTouchEvent(false)
                                }
                            }
                        }
                        false // Allow MapLibre's native touch detector to process all pinch, zoom, and pan events
                    }

                    getMapAsync { map ->
                        mapLibreMapInstance = map

                        // Configure map UI settings and enable full multi-touch gesture support
                        map.uiSettings.isRotateGesturesEnabled = true
                        map.uiSettings.isTiltGesturesEnabled = true
                        map.uiSettings.isZoomGesturesEnabled = true
                        map.uiSettings.isScrollGesturesEnabled = true
                        map.uiSettings.isDoubleTapGesturesEnabled = true
                        map.uiSettings.isQuickZoomGesturesEnabled = true
                        map.uiSettings.isCompassEnabled = true
                        map.uiSettings.isAttributionEnabled = false // Custom attribution rendered in Compose
                        map.uiSettings.isLogoEnabled = false

                        // Set initial camera to primary agricultural coordinates (Chennai Delta / Farm)
                        val initialCenter = selectedZone?.geoPoints?.let { GeospatialMath.getCentroid(it) }
                            ?: LatLng(MapConfig.DEFAULT_LATITUDE, MapConfig.DEFAULT_LONGITUDE)
                        map.cameraPosition = CameraPosition.Builder()
                            .target(initialCenter)
                            .zoom(MapConfig.DEFAULT_ZOOM)
                            .build()

                        // Load initial style
                        val styleBuilder = MapConfig.createStyleBuilder(context, activeBasemap)
                        map.setStyle(styleBuilder) {
                            syncMapAnnotations(map)
                        }

                        // Map click listener for interactive boundary drawing and parcel selection
                        map.addOnMapClickListener { latLng ->
                            android.util.Log.d("GeoPulseMap", "addOnMapClickListener: lat=${latLng.latitude}, lon=${latLng.longitude}, isDrawing=$currentDrawingMode")
                            if (currentDrawingMode) {
                                currentOnMapTapped(latLng)
                                true
                            } else {
                                // Find if a saved field parcel was tapped
                                val clickedZone = currentZones.firstOrNull { zone ->
                                    val pts = zone.geoPoints
                                    if (pts.size >= 3) {
                                        val centroid = GeospatialMath.getCentroid(pts)
                                        // Approximate proximity to centroid or boundary
                                        kotlin.math.abs(centroid.latitude - latLng.latitude) < 0.01 &&
                                                kotlin.math.abs(centroid.longitude - latLng.longitude) < 0.01
                                    } else false
                                }
                                if (clickedZone != null) {
                                    currentOnZoneSelected(clickedZone)
                                    true
                                } else false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Basemap Switcher Floating Pill (Top-Start)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PineDark.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LimeAccent),
                modifier = Modifier.clickable { showBasemapDropdown = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(activeBasemap.badge, fontSize = 14.sp)
                    Text(
                        text = activeBasemap.displayName,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Switch Basemap",
                        tint = LimeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showBasemapDropdown,
                onDismissRequest = { showBasemapDropdown = false },
                modifier = Modifier.background(PineDark)
            ) {
                BasemapType.values().forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.badge, fontSize = 14.sp)
                                Text(
                                    type.displayName,
                                    color = if (type == activeBasemap) LimeAccent else Color.White,
                                    fontWeight = if (type == activeBasemap) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = {
                            onBasemapChange(type)
                            showBasemapDropdown = false
                        }
                    )
                }
            }
        }

        // 3. Camera Control Buttons (Top-End)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Zoom In (+)
            FilledTonalIconButton(
                onClick = {
                    mapLibreMapInstance?.animateCamera(CameraUpdateFactory.zoomIn(), 300)
                },
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = PineDark.copy(alpha = 0.85f),
                    contentColor = LimeAccent
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
            }

            // Zoom Out (-)
            FilledTonalIconButton(
                onClick = {
                    mapLibreMapInstance?.animateCamera(CameraUpdateFactory.zoomOut(), 300)
                },
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = PineDark.copy(alpha = 0.85f),
                    contentColor = LimeAccent
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
            }

            // Recenter / Frame All Fields
            FilledTonalIconButton(
                onClick = {
                    val map = mapLibreMapInstance ?: return@FilledTonalIconButton
                    val allPoints = zones.flatMap { it.geoPoints }
                    if (allPoints.size >= 3) {
                        val bounds = GeospatialMath.getBounds(allPoints)
                        if (bounds != null) {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60), 600)
                        }
                    } else {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(13.06, 80.21), 13.5), 600)
                    }
                },
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = PineDark.copy(alpha = 0.85f),
                    contentColor = LimeAccent
                )
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map", modifier = Modifier.size(18.dp))
            }
        }

        // 4. Map Attribution Badge (Bottom-End)
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text(
                text = MapConfig.ATTRIBUTION_TEXT,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 8.5.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
