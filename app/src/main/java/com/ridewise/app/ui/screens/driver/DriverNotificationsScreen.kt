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
import kotlinx.coroutines.launch

@Composable
fun DriverNotificationsScreen() {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("announcements") }
    var announcements by remember { mutableStateOf<List<DriverAnnouncement>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<DriverAlert>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        loading = true
        announcements = fetchDriverAnnouncements()
        alerts = fetchDriverAlerts()
        loading = false
    }

    LaunchedEffect(Unit) { reload() }

    if (loading) {
        DriverLoadingState("Loading notifications…")
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DriverSectionTitle("Notifications")
        Spacer(Modifier.height(10.dp))

        // Sub-tabs
        Row(
            modifier = Modifier
                .background(DriverColors.Slate100, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "announcements" to "Announcements (${announcements.size})",
                "alerts" to "Alerts (${alerts.size})"
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

        // Post form — only in the announcements tab
        if (tab == "announcements") {
            DriverCard(title = "Post Announcement") {
                DriverTextField(
                    label = "Title",
                    value = title,
                    onChange = { title = it }
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DriverColors.Accent,
                        unfocusedBorderColor = DriverColors.Slate200,
                        focusedLabelColor = DriverColors.Accent,
                        unfocusedLabelColor = DriverColors.Slate500,
                        cursorColor = DriverColors.Accent
                    )
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (title.isBlank() || message.isBlank()) return@Button
                        scope.launch {
                            if (postDriverAnnouncement(title.trim(), message.trim())) {
                                title = ""
                                message = ""
                                reload()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Navy900),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Post", color = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Lists
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (tab == "announcements") {
                if (announcements.isEmpty()) {
                    item { DriverEmptyCard("📣", "No announcements right now.") }
                } else {
                    items(announcements) { a ->
                        DriverCard {
                            Text(
                                a.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DriverColors.Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(a.message, fontSize = 12.5.sp, color = DriverColors.Slate700)
                            Spacer(Modifier.height(4.dp))
                            Text(a.createdAt, fontSize = 11.sp, color = DriverColors.Slate500)
                        }
                    }
                }
            } else {
                if (alerts.isEmpty()) {
                    item {
                        DriverEmptyCard(
                            "🔔",
                            "No alerts right now.\nNew child assignments and system alerts appear here."
                        )
                    }
                } else {
                    items(alerts) { a ->
                        DriverCard {
                            Text(
                                a.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = DriverColors.Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(a.message, fontSize = 12.5.sp, color = DriverColors.Slate700)
                            Spacer(Modifier.height(4.dp))
                            Text(a.createdAt, fontSize = 11.sp, color = DriverColors.Slate500)
                        }
                    }
                }
            }
        }
    }
}