package com.ridewise.app.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OwnerAnnouncementsScreen() {
    val scope = rememberCoroutineScope()
    var announcements by remember { mutableStateOf<List<OwnerAnnouncement>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    suspend fun reload() {
        loading = true
        announcements = fetchOwnerAnnouncements()
        loading = false
    }
    LaunchedEffect(Unit) { reload() }

    if (loading) { LoadingState("Loading announcements…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("Announcements") }

        item {
            OwnerCard {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message, onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (title.isBlank() || message.isBlank()) return@Button
                        scope.launch {
                            if (postOwnerAnnouncement(title.trim(), message.trim())) {
                                title = ""; message = ""; reload()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Orange950)
                ) { Text("Post Announcement", color = Color.White) }
            }
        }

        if (announcements.isEmpty()) {
            item { EmptyCard("📣", "No announcements posted yet.") }
        } else {
            items(announcements) { a ->
                OwnerCard {
                    Text(a.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(a.message, fontSize = 12.5.sp, color = OwnerColors.Slate700)
                    Spacer(Modifier.height(4.dp))
                    Text(a.createdAt, fontSize = 11.sp, color = OwnerColors.Slate500)
                }
            }
        }
    }
}