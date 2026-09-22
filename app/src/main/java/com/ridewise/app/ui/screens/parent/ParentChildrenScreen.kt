package com.ridewise.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun ParentChildrenScreen(
    children: List<ParentChild>,
    onChanged: () -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<ParentChild?>(null) }
    var companies by remember { mutableStateOf<List<ParentCompany>>(emptyList()) }

    LaunchedEffect(Unit) {
        companies = fetchParentCompanies()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ParentSectionTitle("My Children (${children.size})")
            Button(
                onClick = { showAdd = true },
                colors = ButtonDefaults.buttonColors(containerColor = ParentColors.Accent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", color = Color.White)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (children.isEmpty()) {
            ParentEmptyCard("👥", "No children added yet.\nTap Add to register your child.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(children) { child ->
                    ParentChildFullRow(child, onEdit = { editTarget = child })
                }
            }
        }
    }

    if (showAdd) {
        ParentChildFormDialog(
            title = "Add Child",
            initial = null,
            companies = companies,
            onDismiss = { showAdd = false },
            onSaved = { showAdd = false; onChanged() }
        )
    }
    editTarget?.let { c ->
        ParentChildFormDialog(
            title = "Edit Child",
            initial = c,
            companies = companies,
            onDismiss = { editTarget = null },
            onSaved = { editTarget = null; onChanged() }
        )
    }
}

@Composable
private fun ParentChildFullRow(child: ParentChild, onEdit: () -> Unit) {
    Surface(
        color = ParentColors.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ParentColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).background(ParentColors.Green800, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        child.name.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${child.name} ${child.surname}",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = ParentColors.Slate900
                    )
                    Text(
                        "${child.schoolName.ifBlank { "School not set" }}${if (child.grade.isNotBlank()) " · ${child.grade}" else ""}",
                        fontSize = 12.sp, color = ParentColors.Slate500
                    )
                }
                TextButton(onClick = onEdit) {
                    Text("Edit", color = ParentColors.Accent, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = ParentColors.Slate100)
            Spacer(Modifier.height(8.dp))
            ParentInfoLine("Company", child.companyName ?: "Not selected")
            ParentInfoLine("Pickup", child.pickupLocation.ifBlank { "—" })
            ParentInfoLine("Dropoff", child.dropoffLocation.ifBlank { "—" })
            ParentInfoLine("Driver", child.driverName?.let { "$it ${child.driverSurname ?: ""}" } ?: "Not assigned")
            Spacer(Modifier.height(6.dp))
            ParentStatusBadge(child.assignmentStatus.ifBlank { "pending" })
        }
    }
}

@Composable
private fun ParentChildFormDialog(
    title: String,
    initial: ParentChild?,
    companies: List<ParentCompany>,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var surname by remember { mutableStateOf(initial?.surname ?: "") }
    var grade by remember { mutableStateOf(initial?.grade ?: "") }
    var school by remember { mutableStateOf(initial?.schoolName ?: "") }
    var pickup by remember { mutableStateOf(initial?.pickupLocation ?: "") }
    var dropoff by remember { mutableStateOf(initial?.dropoffLocation ?: "") }
    var ownerId by remember { mutableStateOf(initial?.ownerId) }
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = ParentColors.Green950, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ParentField("First Name *", name) { name = it }
                ParentField("Surname *", surname) { surname = it }
                ParentField("Grade", grade) { grade = it }
                ParentField("School", school) { school = it }
                ParentField("Pickup Location", pickup) { pickup = it }
                ParentField("Dropoff Location", dropoff) { dropoff = it }

                if (companies.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Transport Company", fontSize = 12.sp, color = ParentColors.Slate500)
                    companies.forEach { c ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = ownerId == c.ownerId,
                                onClick = { ownerId = c.ownerId }
                            )
                            Text(c.companyName, fontSize = 13.sp)
                        }
                    }
                    TextButton(onClick = { ownerId = null }) {
                        Text("Clear selection", color = ParentColors.Slate500, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || surname.isBlank()) return@Button
                    saving = true
                    scope.launch {
                        val payload = org.json.JSONObject().apply {
                            put("name", name.trim())
                            put("surname", surname.trim())
                            put("grade", grade.trim())
                            put("school_name", school.trim())
                            put("pickup_location", pickup.trim())
                            put("dropoff_location", dropoff.trim())
                            ownerId?.let { put("owner_id", it) }
                        }
                        val ok = if (initial == null) {
                            addParentChild(payload) != null
                        } else {
                            updateParentChild(initial.childId, payload)
                        }
                        saving = false
                        if (ok) onSaved()
                    }
                },
                enabled = !saving && name.isNotBlank() && surname.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ParentColors.Accent)
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save", color = Color.White)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}