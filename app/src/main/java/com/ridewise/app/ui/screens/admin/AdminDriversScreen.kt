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
import kotlinx.coroutines.launch

@Composable
fun AdminDriversScreen(
    drivers: List<AdminDriver>,
    onChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf("all") }

    val filtered = when (filter) {
        "all" -> drivers
        else -> drivers.filter { it.status == filter }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AdminSectionTitle("Drivers (${drivers.size})")
        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.background(AdminColors.Slate100, RoundedCornerShape(8.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "all" to "All (${drivers.size})",
                "pending" to "Pending (${drivers.count { it.status == "pending" }})",
                "active" to "Active (${drivers.count { it.status == "active" }})"
            ).forEach { (key, label) ->
                val selected = filter == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { filter = key }
                ) {
                    Text(
                        label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                        color = if (selected) AdminColors.Slate900 else AdminColors.Slate500,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            AdminEmptyCard("👤", "No drivers in this filter.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { d ->
                    AdminCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("${d.name} ${d.surname}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminColors.Slate900)
                                Text(d.email, fontSize = 12.sp, color = AdminColors.Slate500)
                                Text("License: ${d.licenseNumber ?: "—"}", fontSize = 12.sp, color = AdminColors.Slate500)
                                d.companyName?.let { Text("Company: $it", fontSize = 12.sp, color = AdminColors.Slate500) }
                            }
                            AdminBadge(d.status)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (d.status) {
                                "pending" -> {
                                    Button(
                                        onClick = { scope.launch { if (approveAdminDriver(d.driverId)) onChanged() } },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Success)
                                    ) { Text("Approve", color = Color.White, fontSize = 12.sp) }
                                    Button(
                                        onClick = { scope.launch { if (rejectAdminDriver(d.driverId)) onChanged() } },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Danger)
                                    ) { Text("Reject", color = Color.White, fontSize = 12.sp) }
                                }
                                "active" -> Button(
                                    onClick = { scope.launch { if (rejectAdminDriver(d.driverId)) onChanged() } },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminColors.DangerDim, contentColor = AdminColors.Danger)
                                ) { Text("Deactivate", fontSize = 12.sp) }
                                else -> Button(
                                    onClick = { scope.launch { if (approveAdminDriver(d.driverId)) onChanged() } },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminColors.SuccessDim, contentColor = AdminColors.Success)
                                ) { Text("Activate", fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}