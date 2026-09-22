package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompanyProfileScreen(
    profile: OwnerProfileData?,
    isActive: Boolean,
    stats: OwnerStats? = null
) {
    if (profile == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Could not load company profile.",
                color = OwnerColors.Danger,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "The server may be unreachable, your session may have expired, or your account may not be an owner.",
                color = OwnerColors.Slate500,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---- Welcome + status ----
        item {
            OwnerCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(OwnerColors.AccentDim, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            profile.companyName.firstOrNull()?.uppercase() ?: "O",
                            color = OwnerColors.Accent,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            profile.companyName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = OwnerColors.Slate900
                        )
                        Text(
                            "Owner Account",
                            fontSize = 12.sp,
                            color = OwnerColors.Slate500
                        )
                    }
                    StatusPill(profile.status)
                }
            }
        }

        // ---- Stats grid (only when the account is active) ----
        if (isActive && stats != null) {
            item {
                Column {
                    Text(
                        "Company Overview",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OwnerColors.Slate900,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    OwnerStatsGrid(stats)
                }
            }
        }

        // ---- Company details ----
        item {
            OwnerCard(title = "Company Profile") {
                ProfileLine("Email", profile.email)
                ProfileLine("Phone", profile.contactPhone ?: "Not provided")
                ProfileLine("Address", profile.contactAddress ?: "Not provided")
                ProfileLine("Business Reg. No.", profile.businessRegistrationNumber ?: "Not provided")
                ProfileLine("Operating License No.", profile.operatingLicenseNumber ?: "Not provided")
            }
        }

        // ---- Invite code ----
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OwnerColors.Slate50, RoundedCornerShape(10.dp))
                    .border(1.dp, OwnerColors.Slate300, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        "DRIVER INVITE CODE",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = OwnerColors.Slate500,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        profile.inviteCode ?: "—",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = OwnerColors.Orange950,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Share this code with drivers so they can register under your company.",
                        fontSize = 11.sp,
                        color = OwnerColors.Slate500
                    )
                }
            }
        }

        // ---- Pending approval banner ----
        if (!isActive) {
            item {
                Surface(
                    color = OwnerColors.WarnDim,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Pending admin approval",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = OwnerColors.Slate900
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Driver management features unlock once RideWise admin approves your company.",
                                fontSize = 12.5.sp,
                                color = OwnerColors.Slate700
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Stats grid
// ================================================================

@Composable
private fun OwnerStatsGrid(stats: OwnerStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OwnerStatCard(
                icon = Icons.Default.HourglassEmpty,
                label = "Pending Drivers",
                value = stats.pendingDrivers.toString(),
                accentColor = OwnerColors.Warn,
                modifier = Modifier.weight(1f)
            )
            OwnerStatCard(
                icon = Icons.Default.CheckCircle,
                label = "Active Drivers",
                value = stats.activeDrivers.toString(),
                accentColor = OwnerColors.Success,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OwnerStatCard(
                icon = Icons.Default.People,
                label = "Total Children",
                value = stats.totalChildren.toString(),
                accentColor = OwnerColors.Accent,
                modifier = Modifier.weight(1f)
            )
            OwnerStatCard(
                icon = Icons.Default.Schedule,
                label = "Waiting Children",
                value = stats.waitingChildren.toString(),
                accentColor = OwnerColors.Warn,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OwnerStatCard(
                icon = Icons.Default.DirectionsCar,
                label = "Vehicles",
                value = stats.totalVehicles.toString(),
                accentColor = OwnerColors.Accent,
                modifier = Modifier.weight(1f)
            )
            OwnerStatCard(
                icon = Icons.Default.CreditCard,
                label = "Pending Payments",
                value = stats.pendingPayments.toString(),
                accentColor = OwnerColors.Danger,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OwnerStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = OwnerColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, OwnerColors.Slate200)
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = OwnerColors.Slate900
            )
            Text(label, fontSize = 11.5.sp, color = OwnerColors.Slate500)
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val (bg, fg, label) = when (status) {
        "active" -> Triple(OwnerColors.SuccessDim, OwnerColors.Success, "APPROVED")
        "pending" -> Triple(OwnerColors.WarnDim, OwnerColors.Warn, "PENDING")
        else -> Triple(OwnerColors.DangerDim, OwnerColors.Danger, "REJECTED")
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            label,
            color = fg,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}