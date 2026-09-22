package com.ridewise.app.ui.screens.driver

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun DriverProfileScreen(profile: DriverProfileData?, onEdit: () -> Unit) {
    if (profile == null) { DriverLoadingState("Loading profile…"); return }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photoOverride by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val filename = uploadDriverProfilePhoto(uri, context)
                if (filename != null) photoOverride = filename
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DriverCard(title = "Profile Information") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(72.dp).background(DriverColors.AccentDim, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${profile.name.firstOrNull() ?: 'D'}${profile.surname?.firstOrNull() ?: 'R'}",
                            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DriverColors.Accent
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = DriverColors.Accent,
                        modifier = Modifier.size(24.dp).padding(0.dp)
                    ) {
                        IconButton(
                            onClick = { picker.launch("image/*") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Change photo",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("${profile.name} ${profile.surname ?: ""}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900)
                    Text("RideWise Driver", fontSize = 13.sp, color = DriverColors.Slate500)
                    Spacer(Modifier.height(4.dp))
                    DriverBadge(if (profile.status == "active") "Verified" else "Pending")
                }
            }

            Spacer(Modifier.height(14.dp))
            DriverInfoRow("Driver ID", profile.driverUniqueId ?: "Pending")
            DriverInfoRow("Staff ID", profile.staffId ?: "Assigned once approved")
            DriverInfoRow("Company", profile.companyName ?: "Not linked")
            DriverInfoRow("Email", profile.email)
            DriverInfoRow("Phone", profile.phone ?: "Not provided")
            DriverInfoRow("License", profile.licenseNumber ?: "Not provided")
            DriverInfoRow("Since", profile.createdAt.take(10))

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Accent),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Edit Profile", color = Color.White) }
        }
    }
}