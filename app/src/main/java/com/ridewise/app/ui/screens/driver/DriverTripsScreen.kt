package com.ridewise.app.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverTripsScreen() {
    var tab by remember { mutableStateOf("today") }
    var sessions by remember { mutableStateOf<List<DriverTripSession>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        sessions = fetchDriverTripSessions(limit = 50)
        loading = false
    }

    if (loading) {
        DriverLoadingState("Loading trips…")
        return
    }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todaySessions = sessions.filter { it.startTime.take(10) == today }
    val listed = if (tab == "today") todaySessions else sessions

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DriverSectionTitle("Trip History (${sessions.size})")
        Spacer(Modifier.height(10.dp))

        // Sub-tabs
        Row(
            modifier = Modifier
                .background(DriverColors.Slate100, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "today" to "Today (${todaySessions.size})",
                "all" to "All (${sessions.size})"
            ).forEach { (key, label) ->
                val selected = tab == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { tab = key }
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) DriverColors.Slate900 else DriverColors.Slate500,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (listed.isEmpty()) {
            DriverEmptyCard(
                "🕒",
                if (tab == "today") "No trips logged today yet.\nStart broadcasting on the Live Tracking screen to record a session."
                else "No trips logged yet."
            )
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listed) { s ->
                DriverTripRow(s)
            }
        }
    }
}

@Composable
private fun DriverTripRow(s: DriverTripSession) {
    val isActive = s.status == "active" || s.endTime == null

    DriverCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    formatDate(s.startTime),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = DriverColors.Slate900
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (s.endTime != null) {
                        "${formatTime(s.startTime)} → ${formatTime(s.endTime)}"
                    } else {
                        "Started ${formatTime(s.startTime)} — in progress"
                    },
                    fontSize = 12.sp,
                    color = DriverColors.Slate500
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                DriverBadge(if (isActive) "In Progress" else "Completed")
                Spacer(Modifier.height(4.dp))
                Text(
                    s.distanceM?.let { "${it.toInt()} m" } ?: "—",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DriverColors.Slate700
                )
            }
        }
    }
}

// ---------------- formatting helpers ----------------

private fun formatDate(iso: String): String = try {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    if (iso.endsWith("Z")) {
        parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
    val date = parser.parse(iso.take(19)) ?: Date()
    SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date)
} catch (_: Exception) {
    iso.take(10)
}

private fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        if (iso.endsWith("Z")) {
            parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso.take(19)) ?: return iso.takeLast(8).take(5)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        iso.takeLast(8).take(5)
    }
}
