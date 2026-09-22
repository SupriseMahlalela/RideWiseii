package com.ridewise.app.ui.screens.driver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EditProfileDialogDriver(
    profile: DriverProfileData,
    onDismiss: () -> Unit,
    onSave: (name: String, surname: String, phone: String, licenseNumber: String) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var surname by remember { mutableStateOf(profile.surname ?: "") }
    var phone by remember { mutableStateOf(profile.phone ?: "") }
    var licenseNumber by remember { mutableStateOf(profile.licenseNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", color = DriverColors.Slate900, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DriverColors.Accent,
                        unfocusedBorderColor = DriverColors.Slate200
                    )
                )
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DriverColors.Accent,
                        unfocusedBorderColor = DriverColors.Slate200
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DriverColors.Accent,
                        unfocusedBorderColor = DriverColors.Slate200
                    )
                )
                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text("License Number") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DriverColors.Accent,
                        unfocusedBorderColor = DriverColors.Slate200
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, surname, phone, licenseNumber) },
                colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Navy900)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DriverColors.Slate900)
            }
        }
    )
}

@Composable
fun EditVehicleDialogDriver(
    vehicle: DriverVehicle,
    onDismiss: () -> Unit,
    onSave: (registration: String, capacity: Int) -> Unit
) {
    var registration by remember { mutableStateOf(vehicle.registration ?: "") }
    var capacity by remember { mutableStateOf(vehicle.capacity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Vehicle", color = DriverColors.Slate900, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = registration,
                    onValueChange = { registration = it },
                    label = { Text("Registration Number") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DriverColors.Accent,
                        unfocusedBorderColor = DriverColors.Slate200
                    )
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Seating Capacity") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DriverColors.Accent,
                        unfocusedBorderColor = DriverColors.Slate200
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacity.toIntOrNull() ?: 0
                    onSave(registration, cap)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Navy900)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DriverColors.Slate900)
            }
        }
    )
}