package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PendingDriversScreen() {
    val scope = rememberCoroutineScope()
    var drivers by remember { mutableStateOf<List<OwnerDriver>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    suspend fun reload() {
        loading = true
        drivers = fetchPendingDrivers()
        loading = false
    }
    LaunchedEffect(Unit) { reload() }

    if (loading) { LoadingState("Loading pending drivers…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("Pending Drivers (${drivers.size})") }

        if (drivers.isEmpty()) {
            item { EmptyCard("🕒", "No drivers awaiting approval right now.") }
        } else {
            items(drivers) { d ->
                OwnerCard(title = "${d.name} ${d.surname}") {
                    InfoLine("Driver ID", d.driverUniqueId ?: "—")
                    InfoLine("Email", d.email)
                    InfoLine("License", d.licenseNumber ?: "—")
                    InfoLine("Registered", d.createdAt?.take(10) ?: "—")

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    if (approveDriver(d.driverId)) reload()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OwnerColors.SuccessDim,
                                contentColor = OwnerColors.Success
                            )
                        ) { Text("Approve") }

                        Button(
                            onClick = {
                                scope.launch {
                                    if (rejectDriver(d.driverId)) reload()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OwnerColors.DangerDim,
                                contentColor = OwnerColors.Danger
                            )
                        ) { Text("Reject") }
                    }
                }
            }
        }
    }
}