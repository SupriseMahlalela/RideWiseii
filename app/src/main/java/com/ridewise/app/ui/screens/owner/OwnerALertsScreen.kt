package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OwnerAlertsScreen() {
    var alerts by remember { mutableStateOf<List<OwnerAlert>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var tab by remember { mutableStateOf("emergency") }

    LaunchedEffect(Unit) {
        alerts = fetchOwnerAlerts()
        loading = false
    }

    if (loading) { LoadingState("Loading alerts…"); return }

    val groups = mapOf(
        "emergency" to alerts.filter { it.type == "emergency" },
        "overload" to alerts.filter { it.type == "overload" },
        "trips" to alerts.filter { it.type == "trip_started" || it.type == "trip_ended" },
        "system" to alerts.filter { it.type !in listOf("emergency", "overload", "trip_started", "trip_ended") }
    )

    val tabs = listOf(
        "emergency" to "Emergency",
        "overload" to "Overload",
        "trips" to "Trips",
        "system" to "System"
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("Alerts (${alerts.size})")
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .background(OwnerColors.Slate100, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            tabs.forEach { (key, label) ->
                val selected = tab == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { tab = key }
                ) {
                    Text(
                        "$label (${groups[key]?.size ?: 0})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) OwnerColors.Slate900 else OwnerColors.Slate500,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        val current = groups[tab].orEmpty()
        if (current.isEmpty()) {
            EmptyCard("⚠️", "No $tab alerts yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current) { a ->
                    OwnerCard {
                        Text(a.title, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(a.message, fontSize = 12.5.sp, color = OwnerColors.Slate700)
                        Spacer(Modifier.height(4.dp))
                        Text(a.createdAt, fontSize = 11.sp, color = OwnerColors.Slate500)
                    }
                }
            }
        }
    }
}