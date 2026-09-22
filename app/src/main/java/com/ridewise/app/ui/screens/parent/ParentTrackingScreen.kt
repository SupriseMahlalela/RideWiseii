package com.ridewise.app.ui.screens.parent

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentTrackingScreen(children: List<ParentChild>) {
    val activeChildren = children.filter { it.assignmentStatus == "active" }

    if (activeChildren.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            ParentEmptyCard("📍", "No active driver assigned yet.\nTracking appears once a driver is assigned to one of your children.")
        }
        return
    }

    var selectedChildId by remember { mutableStateOf(activeChildren.first().childId) }
    var location by remember { mutableStateOf<ChildLocation?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    val trail = remember { mutableStateListOf<GeoPoint>() }
    var trailLine by remember { mutableStateOf<Polyline?>(null) }

    // Reset trail when switching children
    LaunchedEffect(selectedChildId) {
        trail.clear()
        val map = mapView
        if (map != null) {
            trailLine?.let { map.overlays.remove(it) }
            trailLine = null
            marker?.let { map.overlays.remove(it) }
            marker = null
            map.invalidate()
        }
    }

    LaunchedEffect(selectedChildId) {
        while (true) {
            val loc = fetchChildLocation(selectedChildId)
            location = loc
            val map = mapView
            if (map != null && loc != null && loc.isBroadcasting && loc.latitude != null && loc.longitude != null) {
                val point = GeoPoint(loc.latitude, loc.longitude)

                // Marker
                val m = marker
                if (m == null) {
                    val newMarker = Marker(map).apply {
                        position = point
                        title = "${loc.driverName ?: "Driver"} ${loc.driverSurname ?: ""}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(newMarker)
                    marker = newMarker
                    map.controller.setZoom(14.0)
                    map.controller.setCenter(point)
                } else {
                    m.position = point
                }

                // Trail — append only if the position actually changed
                val last = trail.lastOrNull()
                if (last == null || last.latitude != point.latitude || last.longitude != point.longitude) {
                    trail.add(point)
                }

                // Polyline
                val line = trailLine
                if (line == null && trail.isNotEmpty()) {
                    val newLine = Polyline(map).apply {
                        outlinePaint.color = AndroidColor.parseColor("#2F9E5E")
                        outlinePaint.strokeWidth = 8f
                        outlinePaint.isAntiAlias = true
                        setPoints(trail.toList())
                    }
                    map.overlays.add(newLine)
                    trailLine = newLine
                } else {
                    line?.setPoints(trail.toList())
                }

                map.invalidate()
            }
            delay(15000)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ParentSectionTitle("Live GPS Tracking")
        Spacer(Modifier.height(8.dp))

        if (activeChildren.size > 1) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = activeChildren.firstOrNull { it.childId == selectedChildId }
                        ?.let { "${it.name} ${it.surname}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tracking") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    activeChildren.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.name} ${c.surname}") },
                            onClick = {
                                selectedChildId = c.childId
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        val loc = location
        Surface(
            color = if (loc?.isBroadcasting == true) ParentColors.AccentDim else ParentColors.Slate100,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    loc == null -> "Loading location…"
                    !loc.hasDriver -> "No driver assigned for this child yet"
                    !loc.isBroadcasting -> "${loc.driverName ?: "Driver"} is not currently broadcasting"
                    else -> "Tracking ${loc.driverName ?: ""} ${loc.driverSurname ?: ""}"
                },
                fontSize = 12.5.sp,
                color = if (loc?.isBroadcasting == true) ParentColors.Accent else ParentColors.Slate700,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(-25.4658, 30.9853))
                    background = ColorDrawable(AndroidColor.WHITE)
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}