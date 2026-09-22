package com.ridewise.app.ui.screens.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// ---------------------------------------------------------------------
// Tuning constants
// ---------------------------------------------------------------------

// Minimum distance (metres) a step must clear before it counts as movement.
// Below this, we treat it as GPS jitter. 12 m is the practical floor for
// consumer GPS — fitness apps use 10, ride-hail apps use 15.
private const val MOVEMENT_THRESHOLD_M = 12.0

// Maximum reported accuracy we'll trust (metres). Anything worse than this
// is almost always a network fix or a cold-start GPS fix before satellite lock.
private const val MAX_ACCURACY_M = 30f

// Minimum reported speed (m/s) to accept a fix as real movement. Values
// below this are stationary. 0.5 m/s ≈ 1.8 km/h, brisk walking pace.
private const val MIN_SPEED_MPS = 0.5f

// How often we want the OS to hand us a new fix (ms) and how far you must
// move before one is emitted (metres). Slightly larger than 0 so a parked
// vehicle doesn't burn battery on jitter.
private const val GPS_MIN_TIME_MS = 3_000L
private const val GPS_MIN_DISTANCE_M = 3f

// How often we push the current position to the backend (ms).
private const val SERVER_PUSH_INTERVAL_MS = 8_000L

@Composable
fun DriverTrackingScreen(
    broadcastStatus: BroadcastStatus?,
    onBroadcastChanged: (BroadcastStatus) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeSession by remember { mutableStateOf<ActiveTripSession?>(null) }
    var isStarting by remember { mutableStateOf(false) }
    var isEnding by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var currentLat by remember { mutableDoubleStateOf(-25.4658) }
    var currentLng by remember { mutableDoubleStateOf(30.9853) }
    var hasFix by remember { mutableStateOf(false) }

    var distanceM by remember { mutableDoubleStateOf(0.0) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    var lastAcceptedLat by remember { mutableStateOf<Double?>(null) }
    var lastAcceptedLng by remember { mutableStateOf<Double?>(null) }
    var lastPushMs by remember { mutableLongStateOf(0L) }

    val trail = remember { mutableStateListOf<GeoPoint>() }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var driverMarker by remember { mutableStateOf<Marker?>(null) }
    var trailLine by remember { mutableStateOf<Polyline?>(null) }

    // ---------- Permission launcher ----------
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            scope.launch {
                startTrip(context, onSessionStart = { session ->
                    activeSession = session
                    distanceM = 0.0
                    elapsedSeconds = 0L
                    lastAcceptedLat = null
                    lastAcceptedLng = null
                    lastPushMs = 0L
                    trail.clear()
                }) { isStarting = false }
            }
        } else {
            statusMessage = "Location permission is required to start a trip."
            isStarting = false
        }
    }

    fun requestAndStart() {
        if (isStarting || isEnding) return
        isStarting = true
        statusMessage = null

        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine) {
            scope.launch {
                startTrip(context, onSessionStart = { session ->
                    activeSession = session
                    distanceM = 0.0
                    elapsedSeconds = 0L
                    lastAcceptedLat = null
                    lastAcceptedLng = null
                    lastPushMs = 0L
                    trail.clear()
                }) { isStarting = false }
            }
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // ---------- Restore an in-progress session on entry ----------
    LaunchedEffect(Unit) {
        val existing = fetchActiveTripSession()
        if (existing != null) {
            activeSession = existing
            distanceM = existing.distanceM
            elapsedSeconds = computeElapsedSeconds(existing.startTime)
        }
    }

    // ---------- Live GPS updates (accuracy-filtered, GPS-only) ----------
    DisposableEffect(activeSession?.sessionId) {
        val session = activeSession
        if (session == null) return@DisposableEffect onDispose { }

        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine) return@DisposableEffect onDispose { }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Drop fixes worse than the accuracy ceiling — these are
                // almost always network/cell-tower fixes at a completely
                // different position from where the vehicle actually is.
                if (!location.hasAccuracy() || location.accuracy > MAX_ACCURACY_M) return

                // Drop fixes that report a real speed and it's essentially zero.
                // This is how we tell "parked" from "moving" without needing
                // to inspect the raw coordinates.
                if (location.hasSpeed() && location.speed < MIN_SPEED_MPS) {
                    // Still update the visual marker so the driver sees their
                    // location, but don't feed this into distance accumulation.
                    currentLocation = location
                    return
                }

                currentLocation = location
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        try {
            // GPS only. NETWORK_PROVIDER's ~500–2000 m error is exactly what
            // causes phantom distance accumulation.
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    GPS_MIN_TIME_MS,
                    GPS_MIN_DISTANCE_M,
                    listener
                )
            }
            // Seed the marker with whatever the OS already has so the map
            // doesn't sit blank until the first live fix arrives.
            val seed = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (seed != null) currentLocation = seed
        } catch (_: SecurityException) {
            // Permission revoked mid-session — the effect will re-run and bail.
        }

        onDispose {
            try { lm.removeUpdates(listener) } catch (_: Exception) { }
        }
    }

    // ---------- React to each accepted fix ----------
    LaunchedEffect(currentLocation, activeSession?.sessionId) {
        val session = activeSession ?: return@LaunchedEffect
        val loc = currentLocation ?: return@LaunchedEffect

        val lat = loc.latitude
        val lng = loc.longitude
        currentLat = lat
        currentLng = lng
        hasFix = true

        // Distance accumulation with accuracy slack.
        val lastL = lastAcceptedLat
        val lastG = lastAcceptedLng

        if (lastL != null && lastG != null) {
            val raw = haversineMeters(lastL, lastG, lat, lng)

            // A "10 m move" reported by a fix with ±8 m accuracy is not a
            // real 10 m move — it's within the noise band of the same spot.
            // Subtract half the reported accuracy before deciding.
            val accuracy = if (loc.hasAccuracy()) loc.accuracy.toDouble() else 0.0
            val slack = (accuracy / 2.0).coerceAtMost(raw)
            val adjusted = raw - slack

            if (adjusted >= MOVEMENT_THRESHOLD_M) {
                distanceM += adjusted
                lastAcceptedLat = lat
                lastAcceptedLng = lng
                trail.add(GeoPoint(lat, lng))
            }
        } else {
            // First fix: seed the baseline, don't accumulate anything yet.
            lastAcceptedLat = lat
            lastAcceptedLng = lng
            trail.add(GeoPoint(lat, lng))
        }

        // Push to server, throttled.
        val now = System.currentTimeMillis()
        if (now - lastPushMs >= SERVER_PUSH_INTERVAL_MS) {
            lastPushMs = now
            pushDriverLocation(lat, lng, true)
            onBroadcastChanged(BroadcastStatus(true, lat, lng))
        }
    }

    // ---------- One-second timer ----------
    LaunchedEffect(activeSession?.sessionId) {
        val session = activeSession ?: return@LaunchedEffect
        val startedAtMs = parseIsoMillis(session.startTime)
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - startedAtMs) / 1000L)
                .coerceAtLeast(0L)
            delay(1_000L)
        }
    }

    // ---------- Marker + trail redraw ----------
    LaunchedEffect(currentLat, currentLng, hasFix, trail.size) {
        val map = mapView ?: return@LaunchedEffect
        if (!hasFix) return@LaunchedEffect

        val point = GeoPoint(currentLat, currentLng)

        val m = driverMarker
        if (m == null) {
            val newMarker = Marker(map).apply {
                position = point
                title = "You"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            map.overlays.add(newMarker)
            driverMarker = newMarker
        } else {
            m.position = point
        }

        if (trail.isNotEmpty()) {
            val line = trailLine
            if (line == null) {
                val newLine = Polyline(map).apply {
                    outlinePaint.color = AndroidColor.parseColor("#2F6FED")
                    outlinePaint.strokeWidth = 8f
                    outlinePaint.isAntiAlias = true
                    setPoints(trail.toList())
                }
                map.overlays.add(newLine)
                trailLine = newLine
            } else {
                line.setPoints(trail.toList())
            }
        }

        // Follow the driver, but don't fight them if they've zoomed out.
        map.controller.setCenter(point)
        if (map.zoomLevelDouble < 15.0) map.controller.setZoom(15.0)
        map.invalidate()
    }

    // ---------- UI ----------
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DriverCard(title = "Trip Control") {
            val session = activeSession

            if (session == null) {
                Text(
                    "No active trip",
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DriverColors.Slate900
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Start a trip to begin broadcasting your location and unlock the online register.",
                    fontSize = 12.5.sp, color = DriverColors.Slate500
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { requestAndStart() },
                    enabled = !isStarting,
                    colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Success),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isStarting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White, strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        if (isStarting) "Starting…" else "Start Trip",
                        color = Color.White, fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(DriverColors.Success, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Trip in progress",
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DriverColors.Success
                    )
                }
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Duration", fontSize = 11.sp, color = DriverColors.Slate500)
                        Text(
                            formatElapsed(elapsedSeconds),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Distance", fontSize = 11.sp, color = DriverColors.Slate500)
                        Text(
                            formatDistance(distanceM),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900
                        )
                    }
                }

                if (!hasFix) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Waiting for GPS fix…",
                        fontSize = 11.5.sp, color = DriverColors.Slate500
                    )
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        isEnding = true
                        scope.launch {
                            val lat = if (hasFix) currentLat else null
                            val lng = if (hasFix) currentLng else null
                            val ok = endDriverTripSession(session.sessionId, lat, lng, distanceM)
                            if (ok) {
                                pushDriverLocation(0.0, 0.0, false)
                                activeSession = null
                                onBroadcastChanged(BroadcastStatus(false, null, null))
                                lastAcceptedLat = null
                                lastAcceptedLng = null
                                lastPushMs = 0L
                            } else {
                                statusMessage = "Could not end trip. Please try again."
                            }
                            isEnding = false
                        }
                    },
                    enabled = !isEnding,
                    colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Danger),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isEnding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White, strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        if (isEnding) "Ending…" else "End Trip",
                        color = Color.White, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            statusMessage?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = DriverColors.DangerDim,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        msg,
                        fontSize = 12.sp,
                        color = DriverColors.Danger,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Surface(
            color = DriverColors.White,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DriverColors.Slate200),
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = ctx.packageName
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(currentLat, currentLng))
                        background = ColorDrawable(AndroidColor.WHITE)
                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ---------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------

private suspend fun startTrip(
    context: Context,
    onSessionStart: (ActiveTripSession) -> Unit,
    onDone: () -> Unit
) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val loc = try {
        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } catch (_: SecurityException) { null }

    val session = startDriverTripSession(loc?.latitude, loc?.longitude)
    if (session != null) onSessionStart(session)
    onDone()
}

private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2.0)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.toInt()} m" else "%.2f km".format(meters / 1000.0)

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun parseIsoMillis(iso: String): Long = try {
    val trimmed = iso.take(19)
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    // ISO strings ending in 'Z' are UTC. MySQL's "YYYY-MM-DD HH:MM:SS"
    // (no Z) is treated as local time, which is what we want for it.
    if (iso.endsWith("Z")) {
        parser.timeZone = TimeZone.getTimeZone("UTC")
    }
    parser.parse(trimmed)?.time ?: System.currentTimeMillis()
} catch (_: Exception) {
    System.currentTimeMillis()
}

private fun computeElapsedSeconds(startIso: String): Long {
    val started = parseIsoMillis(startIso)
    return ((System.currentTimeMillis() - started) / 1000L).coerceAtLeast(0L)
}