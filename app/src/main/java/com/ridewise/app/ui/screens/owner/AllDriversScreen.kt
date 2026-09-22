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
import kotlinx.coroutines.launch

@Composable
fun AllDriversScreen() {
    val scope = rememberCoroutineScope()
    var all by remember { mutableStateOf<List<OwnerDriver>>(emptyList()) }
    var filter by remember { mutableStateOf("all") }
    var loading by remember { mutableStateOf(true) }

    suspend fun reload() {
        loading = true
        all = fetchAllDrivers()
        loading = false
    }
    LaunchedEffect(Unit) { reload() }

    if (loading) { LoadingState("Loading drivers…"); return }

    val filtered = if (filter == "all") all else all.filter { it.status == filter }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle("All Drivers (${all.size})")
        Spacer(Modifier.height(10.dp))

        // Filter row
        Row(
            modifier = Modifier
                .background(OwnerColors.Slate100, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf("all", "pending", "active", "inactive").forEach { key ->
                val selected = filter == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { filter = key }
                ) {
                    Text(
                        key.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) OwnerColors.Slate900 else OwnerColors.Slate500,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            EmptyCard("👥", "No drivers found with status \"$filter\".")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { d ->
                    OwnerCard(title = "${d.name} ${d.surname}") {
                        InfoLine("Staff ID", d.staffId ?: d.driverUniqueId ?: "—")
                        InfoLine("Email", d.email)
                        InfoLine(
                            "Vehicle",
                            if (d.vehicleRegistration != null)
                                "${d.vehicleMake.orEmpty()} ${d.vehicleModel.orEmpty()} · ${d.vehicleRegistration}"
                            else "Not assigned"
                        )
                        Spacer(Modifier.height(6.dp))
                        StatusBadge(d.status)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (d.status) {
                                "pending" -> {
                                    Btn("Approve", OwnerColors.Success, OwnerColors.SuccessDim) {
                                        scope.launch { if (approveDriver(d.driverId)) reload() }
                                    }
                                    Btn("Reject", OwnerColors.Danger, OwnerColors.DangerDim) {
                                        scope.launch { if (rejectDriver(d.driverId)) reload() }
                                    }
                                }
                                "active" -> Btn("Deactivate", OwnerColors.Danger, OwnerColors.DangerDim) {
                                    scope.launch { if (rejectDriver(d.driverId)) reload() }
                                }
                                else -> Btn("Activate", OwnerColors.Success, OwnerColors.SuccessDim) {
                                    scope.launch { if (approveDriver(d.driverId)) reload() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

