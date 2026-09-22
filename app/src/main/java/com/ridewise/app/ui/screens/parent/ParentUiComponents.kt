package com.ridewise.app.ui.screens.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentSectionTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = ParentColors.Slate900
    )
}

@Composable
fun ParentCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = ParentColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ParentColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ParentColors.Slate900
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = ParentColors.Slate100)
                Spacer(Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
fun ParentInfoLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(
            "$label:",
            fontSize = 12.sp,
            color = ParentColors.Slate500,
            modifier = Modifier.width(110.dp)
        )
        Text(value, fontSize = 13.sp, color = ParentColors.Slate900)
    }
}

@Composable
fun ParentInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = ParentColors.Slate500, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = ParentColors.Slate900)
    }
}

@Composable
fun ParentLoadingState(message: String = "Loading…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = ParentColors.Accent)
            Spacer(Modifier.height(12.dp))
            Text(message, color = ParentColors.Slate500, fontSize = 13.sp)
        }
    }
}

@Composable
fun ParentErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Error loading data",
            color = ParentColors.Danger,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(message, color = ParentColors.Slate700, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = ParentColors.Accent)
        ) { Text("Retry", color = Color.White) }
    }
}

@Composable
fun ParentEmptyCard(emoji: String, message: String) {
    Surface(
        color = ParentColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ParentColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = ParentColors.Slate500, fontSize = 13.sp)
        }
    }
}

@Composable
fun ParentStatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "paid", "active", "completed" -> ParentColors.AccentDim to ParentColors.Accent
        "pending", "in progress" -> ParentColors.WarnDim to ParentColors.Warn
        "overdue", "cancelled", "rejected" -> ParentColors.DangerDim to ParentColors.Danger
        else -> ParentColors.Slate100 to ParentColors.Slate500
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