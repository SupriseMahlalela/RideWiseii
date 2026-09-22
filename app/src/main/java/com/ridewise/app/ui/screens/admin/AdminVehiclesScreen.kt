package com.ridewise.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AdminVehiclesScreen(
    vehicles: List<AdminVehicle>,
    onChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<AdminVehicle?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminSectionTitle("Vehicles (${vehicles.size})")
            Button(
                onClick = { showAdd = true },
                colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Accent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", color = Color.White)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (vehicles.isEmpty()) {
            AdminEmptyCard("🚚", "No vehicles registered.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vehicles) { v ->
                    AdminCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${v.make ?: "Unknown"} ${v.model ?: ""}".trim(),
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminColors.Slate900
                                )
                                Text(
                                    v.registration ?: "—",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp, color = AdminColors.Slate700
                                )
                                Text("Capacity: ${v.capacity} seats", fontSize = 12.sp, color = AdminColors.Slate500)
                                v.driverName?.let { Text("Driver: $it", fontSize = 12.sp, color = AdminColors.Slate500) }
                                v.companyName?.let { Text("Company: $it", fontSize = 12.sp, color = AdminColors.Slate500) }
                            }
                            AdminBadge(v.status)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { editTarget = v }) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = AdminColors.Accent, modifier = Modifier.size(16.dp))
                                Text("Edit", color = AdminColors.Accent, fontSize = 12.sp)
                            }
                            TextButton(onClick = {
                                scope.launch { if (deleteAdminVehicle(v.vehicleId)) onChanged() }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = AdminColors.Danger, modifier = Modifier.size(16.dp))
                                Text("Delete", color = AdminColors.Danger, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AdminVehicleDialog(
            initial = null,
            onDismiss = { showAdd = false },
            onSave = { make, model, reg, cap ->
                scope.launch {
                    if (addAdminVehicle(make, model, reg, cap)) {
                        showAdd = false
                        onChanged()
                    }
                }
            }
        )
    }
    editTarget?.let { v ->
        AdminVehicleDialog(
            initial = v,
            onDismiss = { editTarget = null },
            onSave = { make, model, reg, cap ->
                scope.launch {
                    if (updateAdminVehicle(v.vehicleId, make, model, reg, cap)) {
                        editTarget = null
                        onChanged()
                    }
                }
            }
        )
    }
}

@Composable
private fun AdminVehicleDialog(
    initial: AdminVehicle?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var make by remember { mutableStateOf(initial?.make ?: "") }
    var model by remember { mutableStateOf(initial?.model ?: "") }
    var registration by remember { mutableStateOf(initial?.registration ?: "") }
    var capacity by remember { mutableStateOf(initial?.capacity?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Vehicle" else "Edit Vehicle", color = AdminColors.Navy900, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                AdminField("Make", make) { make = it }
                AdminField("Model", model) { model = it }
                AdminField("Registration", registration) { registration = it }
                AdminField("Capacity", capacity) { capacity = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(make, model, registration, capacity.toIntOrNull() ?: 0) },
                colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Navy900)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}