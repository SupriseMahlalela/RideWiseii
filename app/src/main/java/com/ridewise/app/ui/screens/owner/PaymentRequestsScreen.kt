package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun PaymentRequestsScreen() {
    val scope = rememberCoroutineScope()
    var payments by remember { mutableStateOf<List<OwnerPayment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showRequest by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<OwnerPayment?>(null) }

    suspend fun reload() {
        loading = true
        payments = fetchOwnerPayments()
        loading = false
    }
    LaunchedEffect(Unit) { reload() }

    if (loading) { LoadingState("Loading payments…"); return }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Payment Requests (${payments.size})")
            Button(
                onClick = { showRequest = true },
                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Accent)
            ) { Text("+ Request Payment", color = Color.White) }
        }

        Spacer(Modifier.height(12.dp))

        if (payments.isEmpty()) {
            EmptyCard("💳", "No payment requests sent yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(payments) { p ->
                    OwnerCard {
                        Text(
                            "${p.invoiceNumber} — ${p.childName} ${p.childSurname}",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${p.description ?: "Transport fees"} · Parent: ${p.parentName} ${p.parentSurname}",
                            fontSize = 12.sp, color = OwnerColors.Slate500
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "R${"%.2f".format(p.amount)}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = OwnerColors.Orange950
                            )

                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (p.status == "pending") {
                                OutlinedButton(onClick = { editTarget = p }) { Text("Edit") }
                            }
                            // Receipt download would need a file downloader; keep placeholder
                            if (p.status == "paid") {
                                OutlinedButton(onClick = { /* download receipt */ }) { Text("Receipt") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRequest) {
        RequestPaymentDialog(
            onDismiss = { showRequest = false },
            onSubmit = { childId, amount, desc ->
                scope.launch {
                    if (requestPayment(childId, amount, desc)) {
                        showRequest = false
                        reload()
                    }
                }
            }
        )
    }
    editTarget?.let { p ->
        EditPaymentDialog(
            initial = p,
            onDismiss = { editTarget = null },
            onSubmit = { amount, desc ->
                scope.launch {
                    if (editPaymentRequest(p.paymentId, amount, desc)) {
                        editTarget = null
                        reload()
                    }
                }
            }
        )
    }
}

@Composable
private fun RequestPaymentDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, Double, String?) -> Unit
) {
    var children by remember { mutableStateOf<List<OwnerChild>>(emptyList()) }
    var payableIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedChildId by remember { mutableStateOf<Int?>(null) }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val all = fetchAllOwnerChildren()
        // Payable = has driver assigned
        children = all
        payableIds = all.filter { it.driverName != null }.map { it.childId }.toSet()
        selectedChildId = all.firstOrNull { payableIds.contains(it.childId) }?.childId
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Payment", color = OwnerColors.Orange950) },
        text = {
            Column {
                if (loading) Text("Loading children…")
                else {
                    Text(
                        "This creates an invoice the parent sees on their dashboard with a Pay Now button.",
                        fontSize = 12.sp, color = OwnerColors.Slate500
                    )
                    Spacer(Modifier.height(10.dp))

                    Text("Child", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    children.forEach { c ->
                        val payable = payableIds.contains(c.childId)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedChildId == c.childId,
                                enabled = payable,
                                onClick = { selectedChildId = c.childId }
                            )
                            Text(
                                "${c.name} ${c.surname} — " +
                                        if (payable) "driver: ${c.driverName} ${c.driverSurname}"
                                        else "no driver assigned yet",
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Amount (R)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc, onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (!loading) {
                Button(
                    onClick = {
                        val id = selectedChildId ?: return@Button
                        val amt = amount.toDoubleOrNull() ?: return@Button
                        onSubmit(id, amt, desc.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Orange950)
                ) { Text("Send", color = Color.White) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditPaymentDialog(
    initial: OwnerPayment,
    onDismiss: () -> Unit,
    onSubmit: (Double, String?) -> Unit
) {
    var amount by remember { mutableStateOf(initial.amount.toString()) }
    var desc by remember { mutableStateOf(initial.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Payment Request", color = OwnerColors.Orange950) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Amount (R)") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    onSubmit(amt, desc.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Orange950)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}