package com.ridewise.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AdminOwnersScreen(
    owners: List<AdminOwner>,
    onChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf("all") }

    val filtered = when (filter) {
        "all" -> owners
        else -> owners.filter { it.status == filter }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AdminSectionTitle("Transport Owners (${owners.size})")
        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.background(AdminColors.Slate100, RoundedCornerShape(8.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "all" to "All (${owners.size})",
                "pending" to "Pending (${owners.count { it.status == "pending" }})",
                "active" to "Active (${owners.count { it.status == "active" }})"
            ).forEach { (key, label) ->
                val selected = filter == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { filter = key }
                ) {
                    Text(
                        label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                        color = if (selected) AdminColors.Slate900 else AdminColors.Slate500,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            AdminEmptyCard("🏢", "No owners in this filter.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { o ->
                    AdminCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(o.companyName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminColors.Slate900)
                                Text(o.email, fontSize = 12.sp, color = AdminColors.Slate500)
                                o.phone?.let { Text(it, fontSize = 12.sp, color = AdminColors.Slate500) }
                            }
                            AdminBadge(o.status)
                        }
                        o.inviteCode?.let { code ->
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = AdminColors.Slate50,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("INVITE CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AdminColors.Slate500)
                                    Text(
                                        code,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AdminColors.Navy900
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (o.status) {
                                "pending" -> {
                                    Button(
                                        onClick = { scope.launch { if (approveAdminOwner(o.ownerId)) onChanged() } },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Success)
                                    ) { Text("Approve", color = Color.White, fontSize = 12.sp) }
                                    Button(
                                        onClick = { scope.launch { if (rejectAdminOwner(o.ownerId)) onChanged() } },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Danger)
                                    ) { Text("Reject", color = Color.White, fontSize = 12.sp) }
                                }
                                "active" -> Button(
                                    onClick = { scope.launch { if (rejectAdminOwner(o.ownerId)) onChanged() } },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminColors.DangerDim, contentColor = AdminColors.Danger)
                                ) { Text("Suspend", fontSize = 12.sp) }
                                else -> Button(
                                    onClick = { scope.launch { if (approveAdminOwner(o.ownerId)) onChanged() } },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminColors.SuccessDim, contentColor = AdminColors.Success)
                                ) { Text("Reactivate", fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}