package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.BorderStroke
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

// ---------------- Section title ----------------
@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = OwnerColors.Slate900
    )
}

// ---------------- Generic card ----------------
@Composable
fun OwnerCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = OwnerColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, OwnerColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = OwnerColors.Slate900
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = OwnerColors.Slate100)
                Spacer(Modifier.height(10.dp))
            }
            content()
        }
    }
}

// ---------------- Info line ----------------
@Composable
fun InfoLine(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            "$label:",
            fontSize = 12.sp,
            color = OwnerColors.Slate500,
            modifier = Modifier.width(110.dp)
        )
        Text(value, fontSize = 13.sp, color = OwnerColors.Slate900)
    }
}

// ---------------- Profile line ----------------
@Composable
fun ProfileLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "$label:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = OwnerColors.Slate500,
            modifier = Modifier.width(140.dp)
        )
        Text(value, fontSize = 13.sp, color = OwnerColors.Slate900)
    }
    HorizontalDivider(color = OwnerColors.Slate100)
}

// ---------------- Empty state card ----------------
@Composable
fun EmptyCard(emoji: String, message: String) {
    Surface(
        color = OwnerColors.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, OwnerColors.Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(28.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = OwnerColors.Slate500, fontSize = 13.sp)
        }
    }
}

// ---------------- Loading state ----------------
@Composable
fun LoadingState(message: String = "Loading…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = OwnerColors.Accent)
            Spacer(Modifier.height(12.dp))
            Text(message, color = OwnerColors.Slate500, fontSize = 13.sp)
        }
    }
}

// ---------------- Error state ----------------
@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Error loading dashboard",
            color = OwnerColors.Danger,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(message, color = OwnerColors.Slate700, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Accent)
        ) { Text("Retry", color = Color.White) }
    }
}

// ---------------- Locked state ----------------
@Composable
fun LockedState(title: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔒", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = OwnerColors.Slate900,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "This section unlocks once your company is approved by RideWise admin.",
                color = OwnerColors.Slate500,
                fontSize = 13.sp
            )
        }
    }
}

// ---------------- Status badge ----------------
@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "active", "paid", "assigned"     -> OwnerColors.SuccessDim to OwnerColors.Success
        "pending", "maintenance"         -> OwnerColors.WarnDim    to OwnerColors.Warn
        "inactive", "rejected", "overdue"-> OwnerColors.DangerDim  to OwnerColors.Danger
        "unassigned", "waiting"          -> OwnerColors.Slate100   to OwnerColors.Slate500
        else                             -> OwnerColors.Slate100   to OwnerColors.Slate700
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

// ---------------- Small action button ----------------
@Composable
fun Btn(label: String, fg: Color, bg: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg)
    ) { Text(label, fontSize = 12.sp) }
}

// ---------------- Text field ----------------
@Composable
fun OwnerField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

