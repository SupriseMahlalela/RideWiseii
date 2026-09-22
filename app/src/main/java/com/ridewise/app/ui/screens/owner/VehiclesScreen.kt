package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.json.JSONObject

@Composable
fun VehiclesScreen() {
    val scope = rememberCoroutineScope()
    var vehicles by remember { mutableStateOf<List<OwnerVehicle>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<OwnerVehicle?>(null) }
    var assignTarget by remember { mutableStateOf<OwnerVehicle?>(null) }

    suspend fun reload() {
        loading = true
        vehicles = fetchVehicles()
        loading = false
    }
    LaunchedEffect(Unit) { reload() }

    if (loading) { LoadingState("Loading vehicles…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Vehicles (${vehicles.size})")
                Button(
                    onClick = { showAdd = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Accent)
                ) { Text("+ Add Vehicle", color = Color.White) }
            }
        }

        if (vehicles.isEmpty()) {
            item { EmptyCard("🚚", "No vehicles added yet.\nAdd a vehicle, then assign it to an active driver.") }
        } else {
            items(vehicles) { v ->
                OwnerCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFBC02D), RoundedCornerShape(6.dp))
                                .border(1.5.dp, OwnerColors.Orange950, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                v.registration.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = OwnerColors.Orange950
                            )
                        }
                        Spacer(Modifier.weight(1f))

                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        listOfNotNull(v.make, v.model, v.year?.toString(), v.capacity?.let { "$it seats" })
                            .joinToString(" · "),
                        fontSize = 12.5.sp,
                        color = OwnerColors.Slate500
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (v.driverId != null)
                            "Driver: ${v.driverName} ${v.driverSurname} (${v.driverUniqueId ?: "N/A"})"
                        else "Not assigned to a driver",
                        fontSize = 12.5.sp,
                        color = OwnerColors.Slate700
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { editTarget = v }) { Text("Edit", fontSize = 12.sp) }
                        if (v.driverId == null) {
                            Button(
                                onClick = { assignTarget = v },
                                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.AccentDim, contentColor = OwnerColors.Accent)
                            ) { Text("Assign Driver", fontSize = 12.sp) }
                        } else {
                            Button(
                                onClick = {
                                    scope.launch { if (unassignVehicle(v.vehicleId)) reload() }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.DangerDim, contentColor = OwnerColors.Danger)
                            ) { Text("Unassign", fontSize = 12.sp) }
                        }
                        Button(
                            onClick = { scope.launch { if (deleteVehicle(v.vehicleId)) reload() } },
                            colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.DangerDim, contentColor = OwnerColors.Danger)
                        ) { Text("Delete", fontSize = 12.sp) }
                    }
                }
            }
        }
    }

    if (showAdd) {
        VehicleFormDialog(
            title = "Add Vehicle",
            initial = null,
            onDismiss = { showAdd = false },
            onSave = { payload ->
                scope.launch {
                    if (createVehicle(payload)) { showAdd = false; reload() }
                }
            }
        )
    }
    editTarget?.let { v ->
        VehicleFormDialog(
            title = "Edit Vehicle",
            initial = v,
            onDismiss = { editTarget = null },
            onSave = { payload ->
                scope.launch {
                    if (updateVehicle(v.vehicleId, payload)) { editTarget = null; reload() }
                }
            }
        )
    }
    assignTarget?.let { v ->
        AssignDriverDialog(
            vehicleLabel = v.registration,
            onDismiss = { assignTarget = null },
            onAssign = { driverId ->
                scope.launch {
                    if (assignVehicle(v.vehicleId, driverId)) { assignTarget = null; reload() }
                }
            }
        )
    }
}

// ---------- Dialogs ----------
@Composable
private fun VehicleFormDialog(
    title: String,
    initial: OwnerVehicle?,
    onDismiss: () -> Unit,
    onSave: (JSONObject) -> Unit
) {
    var registration by remember { mutableStateOf(initial?.registration ?: "") }
    var make by remember { mutableStateOf(initial?.make ?: "") }
    var model by remember { mutableStateOf(initial?.model ?: "") }
    var year by remember { mutableStateOf(initial?.year?.toString() ?: "") }
    var capacity by remember { mutableStateOf(initial?.capacity?.toString() ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = OwnerColors.Orange950) },
        text = {
            Column {
                OwnerField("Registration *", registration) { registration = it }
                OwnerField("Make", make) { make = it }
                OwnerField("Model", model) { model = it }
                OwnerField("Year", year) { year = it }
                OwnerField("Capacity (seats)", capacity) { capacity = it }
                OwnerField("Color", color) { color = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (registration.isBlank()) return@Button
                    val payload = JSONObject()
                        .put("registration", registration.trim())
                        .put("make", make.trim())
                        .put("model", model.trim())
                        .put("year", year.trim().ifBlank { JSONObject.NULL })
                        .put("capacity", capacity.trim().ifBlank { JSONObject.NULL })
                        .put("color", color.trim())
                    onSave(payload)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Orange950)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AssignDriverDialog(
    vehicleLabel: String,
    onDismiss: () -> Unit,
    onAssign: (Int) -> Unit
) {
    var drivers by remember { mutableStateOf<List<AvailableDriver>>(emptyList()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        drivers = fetchAvailableDriversForVehicle()
        selected = drivers.firstOrNull()?.driverId
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Driver to $vehicleLabel", color = OwnerColors.Orange950) },
        text = {
            when {
                loading -> Text("Loading drivers…")
                drivers.isEmpty() -> Text(
                    "No active drivers are currently available — every active driver already has a vehicle, or none are approved yet.",
                    fontSize = 13.sp
                )
                else -> Column {
                    drivers.forEach { d ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selected == d.driverId,
                                onClick = { selected = d.driverId }
                            )
                            Text("${d.name} ${d.surname} (${d.driverUniqueId ?: "N/A"})", fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!loading && drivers.isNotEmpty()) {
                Button(
                    onClick = { selected?.let(onAssign) },
                    colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Orange950)
                ) { Text("Assign", color = Color.White) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun OwnerField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true
    )
}