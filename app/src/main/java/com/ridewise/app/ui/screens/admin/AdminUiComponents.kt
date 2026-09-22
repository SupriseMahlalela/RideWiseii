package com.ridewise.app.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
fun AdminCard(
    title: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = AdminColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AdminColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = AdminColors.Slate900)
                    action?.invoke()
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AdminColors.Slate100)
                Spacer(Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
fun AdminStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color = AdminColors.Accent,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AdminColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AdminColors.Slate200)
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.size(30.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
            Text(label, fontSize = 10.5.sp, color = AdminColors.Slate500)
        }
    }
}

@Composable
fun AdminBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "active", "approved", "assigned" -> AdminColors.SuccessDim to AdminColors.Success
        "pending", "maintenance" -> AdminColors.WarnDim to AdminColors.Warn
        "inactive", "rejected", "suspended" -> AdminColors.DangerDim to AdminColors.Danger
        else -> AdminColors.Slate100 to AdminColors.Slate500
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
fun AdminInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$label:", fontSize = 12.sp, color = AdminColors.Slate500, modifier = Modifier.width(110.dp))
        Text(value, fontSize = 13.sp, color = AdminColors.Slate900)
    }
}

@Composable
fun AdminSectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AdminColors.Slate900)
}

@Composable
fun AdminLoadingState(message: String = "Loading…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AdminColors.Accent)
            Spacer(Modifier.height(12.dp))
            Text(message, color = AdminColors.Slate500, fontSize = 13.sp)
        }
    }
}

@Composable
fun AdminErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error loading data", color = AdminColors.Danger, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(message, color = AdminColors.Slate700, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Accent)) {
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
fun AdminEmptyCard(emoji: String, message: String) {
    Surface(
        color = AdminColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AdminColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = AdminColors.Slate500, fontSize = 13.sp)
        }
    }
}


@Composable
fun AdminField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AdminColors.Accent,
            unfocusedBorderColor = AdminColors.Slate200,
            focusedLabelColor = AdminColors.Accent,
            unfocusedLabelColor = AdminColors.Slate500
        )
    )
}