package com.ridewise.app.ui.screens.parent

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
fun ParentNotificationsScreen() {
    var tab by remember { mutableStateOf("announcements") }
    var announcements by remember { mutableStateOf<List<ParentAnnouncement>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<ParentAlert>>(emptyList()) }
    var system by remember { mutableStateOf<List<SystemNotification>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        announcements = fetchParentAnnouncements()
        alerts = fetchParentTripAlerts()
        system = fetchParentSystemNotifications()
        loading = false
    }

    if (loading) { ParentLoadingState("Loading notifications…"); return }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ParentSectionTitle("Notifications")
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .background(ParentColors.Slate100, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "announcements" to "Announcements (${announcements.size})",
                "alerts" to "Trip Alerts (${alerts.size})",
                "system" to "System (${system.size})"
            ).forEach { (key, label) ->
                val selected = tab == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { tab = key }
                ) {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) ParentColors.Slate900 else ParentColors.Slate500,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (tab) {
                "announcements" -> {
                    if (announcements.isEmpty()) {
                        item { ParentEmptyCard("📢", "No announcements right now.") }
                    } else {
                        items(announcements) { a ->
                            ParentCard {
                                Text(a.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ParentColors.Slate900)
                                Spacer(Modifier.height(4.dp))
                                Text(a.message, fontSize = 12.5.sp, color = ParentColors.Slate700)
                                Spacer(Modifier.height(4.dp))
                                Text(a.createdAt, fontSize = 11.sp, color = ParentColors.Slate500)
                            }
                        }
                    }
                }
                "alerts" -> {
                    if (alerts.isEmpty()) {
                        item { ParentEmptyCard("🔔", "No trip alerts yet.") }
                    } else {
                        items(alerts) { a ->
                            ParentCard {
                                Text(a.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = ParentColors.Slate900)
                                Spacer(Modifier.height(4.dp))
                                Text(a.message, fontSize = 12.5.sp, color = ParentColors.Slate700)
                                a.time?.let { Text(it, fontSize = 11.sp, color = ParentColors.Slate500) }
                            }
                        }
                    }
                }
                "system" -> {
                    if (system.isEmpty()) {
                        item { ParentEmptyCard("⚙️", "No system notifications yet.") }
                    } else {
                        items(system) { n ->
                            ParentCard {
                                Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = ParentColors.Slate900)
                                Spacer(Modifier.height(4.dp))
                                Text(n.message, fontSize = 12.5.sp, color = ParentColors.Slate700)
                                Text(n.createdAt, fontSize = 11.sp, color = ParentColors.Slate500)
                            }
                        }
                    }
                }
            }
        }
    }
}