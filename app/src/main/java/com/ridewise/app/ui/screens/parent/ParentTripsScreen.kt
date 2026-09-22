package com.ridewise.app.ui.screens.parent

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

@Composable
fun ParentTripsScreen() {
    var trips by remember { mutableStateOf<List<ParentTrip>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<ParentAlert>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var filter by remember { mutableStateOf("all") }

    LaunchedEffect(Unit) {
        trips = fetchParentTrips()
        alerts = fetchParentTripAlerts()
        loading = false
    }

    if (loading) { ParentLoadingState("Loading trips…"); return }

    val filtered = when (filter) {
        "completed" -> trips.filter { it.status == "Completed" }
        "inprogress" -> trips.filter { it.status == "In Progress" }
        else -> trips
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ParentSectionTitle("Trip History (${trips.size})") }

        item {
            Row(
                modifier = Modifier
                    .background(ParentColors.Slate100, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(
                    "all" to "All (${trips.size})",
                    "completed" to "Completed (${trips.count { it.status == "Completed" }})",
                    "inprogress" to "In Progress (${trips.count { it.status == "In Progress" }})"
                ).forEach { (key, label) ->
                    val selected = filter == key
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (selected) Color.White else Color.Transparent,
                        modifier = Modifier.clickable { filter = key }
                    ) {
                        Text(
                            label, fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) ParentColors.Slate900 else ParentColors.Slate500,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item { ParentEmptyCard("🕒", "No trips yet.") }
        } else {
            items(filtered) { t ->
                ParentCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(t.route ?: "Trip", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ParentColors.Slate900)
                            Text(t.date, fontSize = 11.5.sp, color = ParentColors.Slate500)
                            t.child?.let { Text("Child: $it", fontSize = 11.5.sp, color = ParentColors.Slate500) }
                        }
                        ParentStatusBadge(t.status)
                    }
                    Spacer(Modifier.height(8.dp))
                    ParentInfoLine("Driver", t.driver ?: "Not assigned")
                    ParentInfoLine("Distance", t.distance ?: "—")
                    ParentInfoLine("Duration", t.duration ?: "—")
                }
            }
        }

        if (alerts.isNotEmpty()) {
            item { ParentSectionTitle("Trip Alerts (${alerts.size})") }
            items(alerts) { a ->
                Surface(
                    color = ParentColors.WarnDim,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(a.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ParentColors.Slate900)
                        Text(a.message, fontSize = 12.5.sp, color = ParentColors.Slate700)
                        a.time?.let { Text(it, fontSize = 11.sp, color = ParentColors.Slate500) }
                    }
                }
            }
        }
    }
}