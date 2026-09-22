package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun WaitingChildrenScreen() {
    val scope = rememberCoroutineScope()
    var children by remember { mutableStateOf<List<OwnerChild>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var assignChild by remember { mutableStateOf<OwnerChild?>(null) }

    suspend fun reload() {
        loading = true
        children = fetchWaitingChildren()
        loading = false
    }
    LaunchedEffect(Unit) { reload() }

    if (loading) { LoadingState("Loading children…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("Waiting Children (${children.size})") }

        if (children.isEmpty()) {
            item { EmptyCard("👤", "No children waiting for a driver right now.") }
        } else {
            items(children) { c ->
                OwnerCard(title = "${c.name} ${c.surname}") {
                    InfoLine("Grade / School", "${c.grade ?: "—"} · ${c.schoolName ?: "—"}")
                    InfoLine("Parent", "${c.parentName} ${c.parentSurname}${c.parentPhone?.let { " · $it" } ?: ""}")
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { assignChild = c },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OwnerColors.SuccessDim,
                            contentColor = OwnerColors.Success
                        )
                    ) { Text("Assign Driver") }
                }
            }
        }
    }

    assignChild?.let { c ->
        AssignChildDialog(
            child = c,
            onDismiss = { assignChild = null },
            onAssign = { driverId ->
                scope.launch {
                    if (assignChildToDriver(c.childId, driverId)) {
                        assignChild = null
                        reload()
                    }
                }
            }
        )
    }
}

@Composable
private fun AssignChildDialog(
    child: OwnerChild,
    onDismiss: () -> Unit,
    onAssign: (Int) -> Unit
) {
    var drivers by remember { mutableStateOf<List<AvailableDriver>>(emptyList()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        drivers = fetchAvailableDriversForChild()
        selected = drivers.firstOrNull()?.driverId
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Driver — ${child.name} ${child.surname}", color = OwnerColors.Orange950) },
        text = {
            when {
                loading -> Text("Loading drivers…")
                drivers.isEmpty() -> Text(
                    "You don't have any active drivers yet. Approve a driver first, then come back to assign this child.",
                    fontSize = 13.sp
                )
                else -> Column {
                    drivers.forEach { d ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selected == d.driverId,
                                onClick = { selected = d.driverId }
                            )
                            Text("${d.name} ${d.surname} (${d.driverUniqueId ?: "N/A"})")
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}