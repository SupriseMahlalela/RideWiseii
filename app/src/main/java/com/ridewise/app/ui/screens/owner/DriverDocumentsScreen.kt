package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverDocumentsScreen() {
    var drivers by remember { mutableStateOf<List<OwnerDriver>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        drivers = fetchAllDrivers()
        loading = false
    }

    if (loading) { LoadingState("Loading driver documents…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("Driver Documents (${drivers.size})") }

        if (drivers.isEmpty()) {
            item { EmptyCard("📄", "No drivers registered yet.") }
        } else {
            items(drivers) { d ->
                OwnerCard(title = "${d.name} ${d.surname}") {
                    Text(
                        "${d.staffId ?: d.driverUniqueId ?: "N/A"} · ${d.email}",
                        fontSize = 12.sp, color = OwnerColors.Slate500
                    )
                    Spacer(Modifier.height(6.dp))
                    StatusBadge(d.status)
                    Spacer(Modifier.height(10.dp))
                    DocPill("License", d.licenseFile)
                    DocPill("PDP", d.pdpFile)
                    DocPill("Roadworthy", d.roadworthyFile)
                }
            }
        }
    }
}

@Composable
private fun DocPill(label: String, filename: String?) {
    val uploaded = filename != null
    val bg = if (uploaded) OwnerColors.SuccessDim else OwnerColors.Slate100
    val fg = if (uploaded) OwnerColors.Success else OwnerColors.Slate500
    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(end = 6.dp, top = 4.dp)
    ) {
        Text(
            if (uploaded) "$label: View" else "$label: Missing",
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}