package com.ridewise.app.ui.screens.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverRegisterScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DriverCard(title = "Online Register") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📋", fontSize = 42.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Open the boarding register",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Use the web register on your phone's browser to mark children as boarded. The mobile app opens it for you.",
                    fontSize = 13.sp, color = DriverColors.Slate500, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val url = "${com.ridewise.app.utils.Constants.BASE_URL}/online-register"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Open Register", color = Color.White) }
            }
        }
    }
}