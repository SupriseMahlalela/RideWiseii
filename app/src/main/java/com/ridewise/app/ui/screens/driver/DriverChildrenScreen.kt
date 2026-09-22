package com.ridewise.app.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverChildrenScreen(children: List<DriverChild>) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DriverSectionTitle("Assigned Children (${children.size})")
        Spacer(Modifier.height(10.dp))

        if (children.isEmpty()) {
            DriverEmptyCard("👥", "No children assigned yet.\nYour transport company will assign students to you.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(children) { c ->
                    DriverCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(DriverColors.AccentDim, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    c.name.firstOrNull()?.uppercase() ?: "?",
                                    fontWeight = FontWeight.Bold, color = DriverColors.Accent, fontSize = 16.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${c.name} ${c.surname ?: ""}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DriverColors.Slate900)
                                Text(
                                    "${c.grade ?: "—"} · ${c.schoolName ?: "—"}",
                                    fontSize = 12.sp, color = DriverColors.Slate500
                                )
                                Text(
                                    "Parent: ${c.parentName ?: ""} ${c.parentSurname ?: ""}",
                                    fontSize = 11.5.sp, color = DriverColors.Slate500
                                )
                            }
                            DriverBadge(c.assignmentStatus)
                        }
                    }
                }
            }
        }
    }
}