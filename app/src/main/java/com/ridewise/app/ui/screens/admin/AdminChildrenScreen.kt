package com.ridewise.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminChildrenScreen(children: List<AdminChild>) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AdminSectionTitle("All Children (${children.size})")
        Spacer(Modifier.height(12.dp))

        if (children.isEmpty()) {
            AdminEmptyCard("👥", "No children registered yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(children) { c ->
                    AdminCard {
                        Text("${c.name} ${c.surname}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminColors.Slate900)
                        Text(
                            "${c.grade ?: "—"} · ${c.schoolName ?: "—"}",
                            fontSize = 12.sp, color = AdminColors.Slate500
                        )
                        Text(
                            "Parent: ${c.parentName ?: "—"} ${c.parentSurname ?: ""}",
                            fontSize = 12.sp, color = AdminColors.Slate500
                        )
                        Text(
                            "Driver: ${c.driverName ?: "Not assigned"}",
                            fontSize = 12.sp,
                            color = if (c.driverName == null) AdminColors.Warn else AdminColors.Slate700
                        )
                        Spacer(Modifier.height(6.dp))
                        AdminBadge(c.assignmentStatus ?: "waiting")
                    }
                }
            }
        }
    }
}
