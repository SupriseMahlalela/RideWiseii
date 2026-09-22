package com.ridewise.app.ui.screens.admin

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
fun AdminAlertsScreen(alerts: List<AdminAlert>) {
    var tab by remember { mutableStateOf("emergency") }

    val groups = mapOf(
        "emergency" to alerts.filter { it.type == "emergency" },
        "overload" to alerts.filter { it.type == "overload" },
        "trips" to alerts.filter { it.type == "trip_started" || it.type == "trip_ended" },
        "system" to alerts.filter { it.type !in listOf("emergency", "overload", "trip_started", "trip_ended") }
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AdminSectionTitle("Alerts (${alerts.size})")
        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.background(AdminColors.Slate100, RoundedCornerShape(8.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "emergency" to "Emergency",
                "overload" to "Overload",
                "trips" to "Trips",
                "system" to "System"
            ).forEach { (key, label) ->
                val selected = tab == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { tab = key }
                ) {
                    Text(
                        "$label (${groups[key]?.size ?: 0})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) AdminColors.Slate900 else AdminColors.Slate500,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        val current = groups[tab].orEmpty()
        if (current.isEmpty()) {
            AdminEmptyCard("🔔", "No ${tab} alerts.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(current) { a ->
                    AdminCard {
                        Text(a.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = AdminColors.Slate900)
                        Spacer(Modifier.height(4.dp))
                        Text(a.message, fontSize = 12.5.sp, color = AdminColors.Slate700)
                        a.driverName?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("Driver: $it", fontSize = 11.5.sp, color = AdminColors.Slate500)
                        }
                        Text(a.createdAt, fontSize = 11.sp, color = AdminColors.Slate500)
                    }
                }
            }
        }
    }
}