package com.ridewise.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentHomeScreen(
    profile: ParentProfile?,
    children: List<ParentChild>,
    payments: List<ParentPayment>,
    announcements: List<ParentAnnouncement>,
    onViewAllChildren: () -> Unit
) {
    val hasActive = children.any { it.assignmentStatus == "active" }
    val pendingPayments = payments.count { it.status != "paid" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stat row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ParentStatCard(
                    icon = Icons.Default.People,
                    value = children.size.toString(),
                    label = "My Children",
                    accent = ParentColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                ParentStatCard(
                    icon = Icons.Default.DirectionsBus,
                    value = if (hasActive) "Active" else "Standby",
                    label = "Status",
                    accent = if (hasActive) ParentColors.Accent else ParentColors.Slate500,
                    modifier = Modifier.weight(1f)
                )
                ParentStatCard(
                    icon = Icons.Default.CreditCard,
                    value = pendingPayments.toString(),
                    label = "Pending",
                    accent = if (pendingPayments > 0) ParentColors.Warn else ParentColors.Slate500,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Profile summary card
        item {
            ParentCard(title = "Parent Profile") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(ParentColors.Green800, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            profile?.name?.firstOrNull()?.uppercase() ?: "P",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "${profile?.name ?: "Parent"} ${profile?.surname ?: ""}",
                            fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            color = ParentColors.Slate900
                        )
                        Text("Primary Guardian", fontSize = 12.5.sp, color = ParentColors.Slate500)
                    }
                }
                Spacer(Modifier.height(10.dp))
                ParentInfoRow(Icons.Default.Email, profile?.email ?: "Not set")
                ParentInfoRow(Icons.Default.Phone, profile?.phone ?: "Not provided")
                ParentInfoRow(Icons.Default.LocationOn, profile?.address ?: "Not provided")
            }
        }

        // Children summary
        item {
            ParentCard(title = "My Children (${children.size})") {
                if (children.isEmpty()) {
                    Text(
                        "No children added yet.",
                        fontSize = 13.sp, color = ParentColors.Slate500,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    children.take(3).forEach { c ->
                        ParentChildCompactRow(c)
                        Spacer(Modifier.height(6.dp))
                    }
                    if (children.size > 3) {
                        TextButton(
                            onClick = onViewAllChildren,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                "+${children.size - 3} more — View all",
                                color = ParentColors.Accent, fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Latest announcement
        if (announcements.isNotEmpty()) {
            item {
                val latest = announcements.first()
                ParentCard(title = "Latest Announcement") {
                    Text(latest.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ParentColors.Slate900)
                    Spacer(Modifier.height(4.dp))
                    Text(latest.message, fontSize = 12.5.sp, color = ParentColors.Slate700)
                    Spacer(Modifier.height(4.dp))
                    Text(latest.createdAt, fontSize = 11.sp, color = ParentColors.Slate500)
                }
            }
        }
    }
}

@Composable
private fun ParentStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ParentColors.White,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ParentColors.Slate200)
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ParentColors.Slate900)
            Text(label, fontSize = 11.sp, color = ParentColors.Slate500)
        }
    }
}

@Composable
private fun ParentChildCompactRow(child: ParentChild) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ParentColors.Slate50, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).background(ParentColors.Green800, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                child.name.firstOrNull()?.uppercase() ?: "?",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("${child.name} ${child.surname}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ParentColors.Slate900)
            Text(
                "${child.schoolName.ifBlank { "School not set" }}${if (child.grade.isNotBlank()) " · ${child.grade}" else ""}",
                fontSize = 11.5.sp, color = ParentColors.Slate500
            )
            Text(
                child.driverName?.let { "🚌 Driver: $it" } ?: "🚌 No driver assigned",
                fontSize = 11.5.sp,
                color = if (child.driverName.isNullOrBlank()) ParentColors.Slate500 else ParentColors.Accent
            )
        }
    }
}