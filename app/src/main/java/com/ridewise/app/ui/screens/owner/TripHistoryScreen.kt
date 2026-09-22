package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TripHistoryScreen() {
    var sessions by remember { mutableStateOf<List<OwnerTripSession>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        sessions = fetchOwnerTripSessions()
        loading = false
    }

    if (loading) { LoadingState("Loading trips…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionTitle("Trip History (${sessions.size})") }

        if (sessions.isEmpty()) {
            item { EmptyCard("🕒", "No trips logged yet.") }
        } else {
            items(sessions) { s ->
                OwnerCard {
                    Text(
                        "${s.driverName} ${s.driverSurname} (${s.driverUniqueId ?: "N/A"})",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${s.startTime}${s.endTime?.let { " → $it" } ?: " (in progress)"}",
                        fontSize = 12.sp,
                        color = OwnerColors.Slate500
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Distance: ${s.distanceM?.let { "$it m" } ?: "—"}  ·  Status: ${s.status}",
                        fontSize = 12.sp,
                        color = OwnerColors.Slate700
                    )
                }
            }
        }
    }
}