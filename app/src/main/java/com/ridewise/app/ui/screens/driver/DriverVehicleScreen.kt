package com.ridewise.app.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverVehicleScreen(vehicle: DriverVehicle?, onEdit: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        DriverCard(
            title = "Assigned Vehicle",
            action = {
                TextButton(onClick = onEdit) {
                    Text("Edit", color = DriverColors.Accent, fontSize = 12.sp)
                }
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFFBC02D),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(2.dp, DriverColors.Navy900, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        vehicle?.registration?.uppercase() ?: "NOT SET",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = DriverColors.Navy900,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(DriverColors.AccentDim, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = DriverColors.Accent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("${vehicle?.capacity ?: 0}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900)
                        Text("Seats", fontSize = 12.sp, color = DriverColors.Slate500)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            DriverInfoRow("Make", vehicle?.make ?: "Not set")
            DriverInfoRow("Model", vehicle?.model ?: "Not set")
        }
    }
}