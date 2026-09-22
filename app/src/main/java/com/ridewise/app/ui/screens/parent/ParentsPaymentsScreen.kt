package com.ridewise.app.ui.screens.parent
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ParentPaymentsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var payments by remember { mutableStateOf<List<ParentPayment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    suspend fun reload() {
        loading = true
        payments = fetchParentPayments()
        loading = false
    }
    LaunchedEffect(Unit) { reload() }

    if (loading) { ParentLoadingState("Loading payments…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ParentSectionTitle("Payments (${payments.size})") }

        if (payments.isEmpty()) {
            item { ParentEmptyCard("💳", "No payments on record yet.") }
        } else {
            items(payments) { p ->
                ParentCard {
                    Text(
                        "${p.invoiceNumber?.let { "$it — " } ?: ""}${p.description ?: "${p.childName} ${p.childSurname}"}",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        color = ParentColors.Slate900
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${p.childName} ${p.childSurname}${p.driverName?.let { " · Driver: $it" } ?: ""}",
                        fontSize = 12.sp, color = ParentColors.Slate500
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
                            fontSize = 17.sp,
                            color = ParentColors.Green950
                        )
                        ParentStatusBadge(p.status)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (p.status != "paid") {
                        Button(
                            onClick = {
                                scope.launch {
                                    val result = initiateParentPayment(p.paymentId)
                                    if (result != null) {
                                        val (actionUrl, fields) = result
                                        // Build URL-encoded query for the PayFast action
                                        val query = buildString {
                                            fields.keys().forEach { key ->
                                                if (isNotEmpty()) append('&')
                                                append(Uri.encode(key))
                                                append('=')
                                                append(Uri.encode(fields.optString(key)))
                                            }
                                        }
                                        val url = "$actionUrl?$query"
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ParentColors.Accent),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Pay Now", color = Color.White) }
                    } else {
                        OutlinedButton(
                            onClick = { /* receipt download — needs WebView or PDF handler */ },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Receipt", color = ParentColors.Accent) }
                    }
                }
            }
        }
    }
}