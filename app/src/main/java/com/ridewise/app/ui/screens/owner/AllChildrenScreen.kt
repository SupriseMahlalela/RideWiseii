package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AllChildrenScreen() {
    var children by remember { mutableStateOf<List<OwnerChild>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        children = fetchAllOwnerChildren()
        loading = false
    }

    if (loading) { LoadingState("Loading children…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("All Children (${children.size})") }

        if (children.isEmpty()) {
            item { EmptyCard("👥", "No children registered under your company yet.") }
        } else {
            items(children) { c ->
                OwnerCard(title = "${c.name} ${c.surname}") {
                    InfoLine("Grade / School", "${c.grade ?: "—"} · ${c.schoolName ?: "—"}")
                    InfoLine("Parent", "${c.parentName} ${c.parentSurname}")
                    InfoLine(
                        "Driver",
                        if (c.driverName != null) "${c.driverName} ${c.driverSurname}" else "Not assigned"
                    )
                    Spacer(Modifier.height(6.dp))
                    StatusBadge(c.assignmentStatus ?: "waiting")
                }
            }
        }
    }
}