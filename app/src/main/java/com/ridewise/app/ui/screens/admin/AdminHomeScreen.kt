package com.ridewise.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminHomeScreen(
    stats: AdminStats,
    reportStats: AdminReportStats,
    alerts: List<AdminAlert>,
    onViewAlerts: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Overview", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AdminColors.Slate900)
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    icon = Icons.Default.People,
                    value = stats.students.toString(),
                    label = "Students",
                    accent = AdminColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    icon = Icons.Default.DirectionsBus,
                    value = stats.drivers.toString(),
                    label = "Drivers",
                    accent = AdminColors.Accent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    icon = Icons.Default.HourglassEmpty,
                    value = stats.pendingDrivers.toString(),
                    label = "Pending Drivers",
                    accent = AdminColors.Warn,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    icon = Icons.Default.Business,
                    value = stats.owners.toString(),
                    label = "Owners",
                    accent = AdminColors.Accent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    icon = Icons.Default.DirectionsCar,
                    value = stats.vehicles.toString(),
                    label = "Vehicles",
                    accent = AdminColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    icon = Icons.Default.Today,
                    value = reportStats.tripsToday.toString(),
                    label = "Trips Today",
                    accent = AdminColors.Success,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            AdminCard(title = "System Activity") {
                AdminInfoRow("Total Trips", reportStats.totalTrips.toString())
                AdminInfoRow("Active Drivers", reportStats.activeDrivers.toString())
                AdminInfoRow("Pending Approvals", reportStats.pendingApprovals.toString())
                AdminInfoRow("Open Alerts", alerts.size.toString())
            }
        }

        item {
            AdminCard(
                title = "Recent Alerts",
                action = {
                    TextButton(onClick = onViewAlerts) {
                        Text("View all", color = AdminColors.Accent, fontSize = 12.sp)
                    }
                }
            ) {
                if (alerts.isEmpty()) {
                    Text("No alerts right now.", fontSize = 13.sp, color = AdminColors.Slate500)
                } else {
                    alerts.take(3).forEach { a ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(8.dp)
                                    .background(
                                        when (a.type) {
                                            "emergency" -> AdminColors.Danger
                                            "overload" -> AdminColors.Warn
                                            else -> AdminColors.Accent
                                        },
                                        RoundedCornerShape(50)
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(a.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AdminColors.Slate900)
                                Text(a.message, fontSize = 12.sp, color = AdminColors.Slate700)
                            }
                        }
                    }
                }
            }
        }
    }
}