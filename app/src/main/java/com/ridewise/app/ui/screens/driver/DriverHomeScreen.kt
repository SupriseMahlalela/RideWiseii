package com.ridewise.app.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverHomeScreen(
    profile: DriverProfileData?,
    vehicle: DriverVehicle?,
    children: List<DriverChild>,
    tripSessions: List<DriverTripSession>,
    documents: DriverDocument?,
    onEditProfile: () -> Unit,
    onEditVehicle: () -> Unit
) {
    val todayTrips = tripSessions.count { session ->
        session.startTime.take(10) == java.time.LocalDate.now().toString()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DriverStatCard(
                    icon = Icons.Default.Schedule,
                    value = todayTrips.toString(),
                    label = "Trips Today",
                    accent = DriverColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                DriverStatCard(
                    icon = Icons.Default.CheckCircle,
                    value = if (profile?.status == "active") "Active" else "Pending",
                    label = "Account",
                    accent = if (profile?.status == "active") DriverColors.Success else DriverColors.Warn,
                    modifier = Modifier.weight(1f)
                )
                DriverStatCard(
                    icon = Icons.Default.Person,
                    value = children.size.toString(),
                    label = "Children",
                    accent = DriverColors.Accent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Profile summary
        item {
            DriverCard(
                title = "Driver Profile",
                action = {
                    TextButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = DriverColors.Accent, modifier = Modifier.size(14.dp))
                        Text("Edit", color = DriverColors.Accent, fontSize = 11.5.sp)
                    }
                }
            ) {
                if (profile != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(DriverColors.AccentDim, CircleShape)
                                .border(2.dp, DriverColors.Slate200, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${profile.name.firstOrNull() ?: 'D'}${profile.surname?.firstOrNull() ?: 'R'}",
                                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DriverColors.Accent
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("${profile.name} ${profile.surname ?: ""}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900)
                            Text("RideWise Driver", fontSize = 12.5.sp, color = DriverColors.Slate500)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    DriverBadge(if (profile.status == "active") "Verified" else "Pending")
                    Spacer(Modifier.height(6.dp))
                    DriverInfoRow("Driver ID", profile.driverUniqueId ?: "Pending")
                    DriverInfoRow("Company", profile.companyName ?: "Not linked")
                    DriverInfoRow("Email", profile.email)
                    DriverInfoRow("Phone", profile.phone ?: "Not provided")
                    DriverInfoRow("Since", profile.createdAt.take(10))
                }
            }
        }

        // Vehicle
        item {
            DriverCard(
                title = "Vehicle",
                action = {
                    TextButton(onClick = onEditVehicle) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = DriverColors.Accent, modifier = Modifier.size(14.dp))
                        Text("Edit", color = DriverColors.Accent, fontSize = 11.5.sp)
                    }
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFBC02D),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(2.dp, DriverColors.Navy900, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            vehicle?.registration?.uppercase() ?: "NOT SET",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp, color = DriverColors.Navy900,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(DriverColors.AccentDim, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = DriverColors.Accent, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("${vehicle?.capacity ?: 0}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900)
                            Text("Seats", fontSize = 11.sp, color = DriverColors.Slate500)
                        }
                    }
                }
            }
        }

        // Assigned children summary
        item {
            DriverCard(title = "Assigned Children (${children.size})") {
                if (children.isEmpty()) {
                    Text(
                        "No children assigned yet.",
                        fontSize = 13.sp, color = DriverColors.Slate500,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    children.take(3).forEach { c ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape).background(
                                    when (c.assignmentStatus) {
                                        "active" -> DriverColors.Success
                                        "pending" -> DriverColors.Warn
                                        else -> DriverColors.Slate300
                                    }
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${c.name} ${c.surname ?: ""}", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = DriverColors.Slate900)
                                Text(
                                    "${c.grade ?: "—"} · ${c.schoolName ?: "—"}",
                                    fontSize = 11.sp, color = DriverColors.Slate500
                                )
                            }
                        }
                    }
                    if (children.size > 3) {
                        Text(
                            "+${children.size - 3} more children",
                            fontSize = 12.sp, color = DriverColors.Slate500,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Documents summary
        item {
            DriverCard(title = "My Documents") {
                DocumentStatusRow("Driver License", documents?.licenseFile)
                DocumentStatusRow("PDP", documents?.pdpFile)
                DocumentStatusRow("Roadworthy Certificate", documents?.roadworthyFile)
            }
        }
    }
}

@Composable
private fun DriverStatCard(
    icon: ImageVector, value: String, label: String,
    accent: Color, modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DriverColors.White,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DriverColors.Slate200)
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.size(30.dp).background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
            Text(label, fontSize = 10.5.sp, color = DriverColors.Slate500)
        }
    }
}

@Composable
internal fun DocumentStatusRow(label: String, fileName: String?) {
    val uploaded = fileName != null
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, contentDescription = null, tint = DriverColors.Slate500, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = DriverColors.Slate900)
                Text(
                    if (uploaded) "${fileName?.split(".")?.lastOrNull()?.uppercase() ?: "File"} uploaded" else "Not uploaded",
                    fontSize = 11.sp, color = DriverColors.Slate500
                )
            }
        }
        if (uploaded) {
            TextButton(onClick = { /* open file — TODO */ }) {
                Text("View", color = DriverColors.Accent, fontSize = 11.5.sp)
            }
        } else {
            Text("Missing", fontSize = 11.5.sp, color = DriverColors.Slate500)
        }
    }
}