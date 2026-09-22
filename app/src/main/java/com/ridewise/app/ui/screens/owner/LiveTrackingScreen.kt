package com.ridewise.app.ui.screens.owner

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


@Composable
fun LiveTrackingScreen() {
    var drivers by remember { mutableStateOf<List<DriverLocation>>(emptyList()) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val markers = remember { mutableMapOf<Int, Marker>() }
    val trails = remember { mutableMapOf<Int, MutableList<GeoPoint>>() }
    val polylines = remember { mutableMapOf<Int, Polyline>() }

    LaunchedEffect(Unit) {
        while (true) {
            drivers = fetchDriverLocations()
            val map = mapView ?: run { delay(2000); return@LaunchedEffect }
            val broadcasting = drivers.filter { it.isBroadcasting && it.latitude != null && it.longitude != null }

            // Update or add markers + trails
            broadcasting.forEach { d ->
                val point = GeoPoint(d.latitude!!, d.longitude!!)

                // --- Marker ---
                val existing = markers[d.driverId]
                if (existing != null) {
                    existing.position = point
                } else {
                    val m = Marker(map).apply {
                        position = point
                        title = "${d.name} ${d.surname} (${d.driverUniqueId ?: "N/A"})"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(m)
                    markers[d.driverId] = m
                }

                // --- Trail ---
                val trailList = trails.getOrPut(d.driverId) { mutableListOf() }
                val last = trailList.lastOrNull()
                if (last == null || last.latitude != point.latitude || last.longitude != point.longitude) {
                    trailList.add(point)
                }

                // --- Polyline (owner orange) ---
                val existingLine = polylines[d.driverId]
                if (existingLine == null && trailList.isNotEmpty()) {
                    val newLine = Polyline(map).apply {
                        outlinePaint.color = AndroidColor.parseColor("#E8720C")
                        outlinePaint.strokeWidth = 8f
                        outlinePaint.isAntiAlias = true
                        setPoints(trailList.toList())
                    }
                    map.overlays.add(newLine)
                    polylines[d.driverId] = newLine
                } else {
                    existingLine?.setPoints(trailList.toList())
                }
            }

            // Remove markers + trails for drivers who've stopped broadcasting
            val activeIds = broadcasting.map { it.driverId }.toSet()
            markers.keys.filter { it !in activeIds }.forEach { id ->
                markers.remove(id)?.let { map.overlays.remove(it) }
                polylines.remove(id)?.let { map.overlays.remove(it) }
                trails.remove(id)
            }

            map.invalidate()

            // Auto-fit once on the first batch of the session
            if (broadcasting.isNotEmpty() && markers.size == broadcasting.size) {
                val box = org.osmdroid.util.BoundingBox.fromGeoPoints(
                    broadcasting.map { GeoPoint(it.latitude!!, it.longitude!!) }
                )
                map.zoomToBoundingBox(box, true, 80)
            }

            delay(15_000)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("Live Tracking")
        Spacer(Modifier.height(6.dp))
        Text(
            "${drivers.count { it.isBroadcasting }} of ${drivers.size} drivers broadcasting",
            color = OwnerColors.Slate500,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))

        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(11.0)
                    controller.setCenter(GeoPoint(-25.4658, 30.9853))
                    background = ColorDrawable(AndroidColor.WHITE)
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}