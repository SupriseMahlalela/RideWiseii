package com.ridewise.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AdminAnnouncementsScreen(
    announcements: List<AdminAnnouncement>,
    onChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var posting by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AdminSectionTitle("Announcements")
        Spacer(Modifier.height(10.dp))

        AdminCard(title = "Post Announcement") {
            AdminField("Title", title) { title = it }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AdminColors.Accent,
                    unfocusedBorderColor = AdminColors.Slate200
                )
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) return@Button
                    posting = true
                    scope.launch {
                        if (postAdminAnnouncement(title.trim(), message.trim())) {
                            title = ""
                            message = ""
                            onChanged()
                        }
                        posting = false
                    }
                },
                enabled = !posting,
                colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Navy900),
                modifier = Modifier.align(Alignment.End)
            ) {
                if (posting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Post", color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (announcements.isEmpty()) {
            AdminEmptyCard("📣", "No announcements posted yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(announcements) { a ->
                    AdminCard {
                        Text(a.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AdminColors.Slate900)
                        Spacer(Modifier.height(4.dp))
                        Text(a.message, fontSize = 12.5.sp, color = AdminColors.Slate700)
                        Spacer(Modifier.height(4.dp))
                        Text(a.createdAt, fontSize = 11.sp, color = AdminColors.Slate500)
                    }
                }
            }
        }
    }
}