package com.ridewise.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ParentProfileScreen(profile: ParentProfile?, onSaved: () -> Unit) {
    if (profile == null) {
        ParentLoadingState("Loading profile…")
        return
    }

    val scope = rememberCoroutineScope()
    var showEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ParentCard(title = "Profile Information") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).background(ParentColors.Green800, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.name.firstOrNull()?.uppercase() ?: "P",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "${profile.name} ${profile.surname}",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = ParentColors.Slate900
                    )
                    Text("Primary Guardian", fontSize = 13.sp, color = ParentColors.Slate500)
                }
            }
            Spacer(Modifier.height(16.dp))
            ParentInfoRow(Icons.Default.Email, profile.email)
            ParentInfoRow(Icons.Default.Phone, profile.phone)
            ParentInfoRow(Icons.Default.LocationOn, profile.address)
            ParentInfoRow(Icons.Default.CalendarToday, "Member since: ${profile.createdAt.take(10)}")

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showEdit = true },
                colors = ButtonDefaults.buttonColors(containerColor = ParentColors.Accent),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Edit Profile", color = Color.White) }
        }
    }

    if (showEdit) {
        ParentEditProfileDialog(
            initial = profile,
            onDismiss = { showEdit = false },
            onSave = { name, surname, phone, address ->
                scope.launch {
                    if (updateParentProfile(name, surname, phone, address)) {
                        showEdit = false
                        onSaved()
                    }
                }
            }
        )
    }
}

@Composable
private fun ParentEditProfileDialog(
    initial: ParentProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var surname by remember { mutableStateOf(initial.surname) }
    var phone by remember { mutableStateOf(initial.phone) }
    var address by remember { mutableStateOf(initial.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", color = ParentColors.Green950, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ParentField("Name", name) { name = it }
                ParentField("Surname", surname) { surname = it }
                ParentField("Phone", phone) { phone = it }
                ParentField("Address", address) { address = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, surname, phone, address) },
                colors = ButtonDefaults.buttonColors(containerColor = ParentColors.Accent)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
internal fun ParentField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ParentColors.Accent,
            unfocusedBorderColor = ParentColors.Slate200,
            focusedLabelColor = ParentColors.Accent,
            unfocusedLabelColor = ParentColors.Slate500
        )
    )
}