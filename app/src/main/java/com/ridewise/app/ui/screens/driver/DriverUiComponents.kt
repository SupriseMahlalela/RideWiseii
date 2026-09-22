package com.ridewise.app.ui.screens.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriverCard(
    title: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = DriverColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DriverColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = DriverColors.Slate900)
                    action?.invoke()
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = DriverColors.Slate100)
                Spacer(Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
fun DriverInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = DriverColors.Slate500,
            modifier = Modifier.width(86.dp)
        )
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DriverColors.Slate900)
    }
}

@Composable
fun DriverIconRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = DriverColors.Slate500, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = DriverColors.Slate900)
    }
}

@Composable
fun DriverSectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DriverColors.Slate900)
}

@Composable
fun DriverEmptyCard(emoji: String, message: String) {
    Surface(
        color = DriverColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DriverColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = DriverColors.Slate500, fontSize = 13.sp)
        }
    }
}

@Composable
fun DriverLoadingState(message: String = "Loading…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = DriverColors.Accent)
            Spacer(Modifier.height(12.dp))
            Text(message, color = DriverColors.Slate500, fontSize = 13.sp)
        }
    }
}

@Composable
fun DriverErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error loading data", color = DriverColors.Danger, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(message, color = DriverColors.Slate700, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Accent)) {
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
fun DriverBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "active", "verified", "completed" -> DriverColors.SuccessDim to DriverColors.Success
        "pending" -> DriverColors.WarnDim to DriverColors.Warn
        "inactive", "rejected", "cancelled" -> DriverColors.DangerDim to DriverColors.Danger
        else -> DriverColors.Slate100 to DriverColors.Slate500
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            status.uppercase(),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DriverTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DriverColors.Accent,
            unfocusedBorderColor = DriverColors.Slate200,
            focusedLabelColor = DriverColors.Accent,
            unfocusedLabelColor = DriverColors.Slate500,
            cursorColor = DriverColors.Accent
        )
    )
}