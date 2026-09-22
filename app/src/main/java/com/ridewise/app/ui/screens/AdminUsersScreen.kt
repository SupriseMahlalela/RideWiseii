package com.ridewise.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminUsersScreen(users: List<AdminUser>) {
    var search by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("all") }

    val filtered = users.filter { u ->
        val matchesRole = roleFilter == "all" || u.role == roleFilter
        val matchesSearch = search.isBlank() ||
                u.name?.contains(search, ignoreCase = true) == true ||
                u.surname?.contains(search, ignoreCase = true) == true ||
                u.email.contains(search, ignoreCase = true)
        matchesRole && matchesSearch
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        AdminSectionTitle("All Users (${users.size})")
        Spacer(Modifier.height(10.dp))

        // Search
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search by name or email") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AdminColors.Slate500) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminColors.Accent,
                unfocusedBorderColor = AdminColors.Slate200,
                focusedLabelColor = AdminColors.Accent,
                unfocusedLabelColor = AdminColors.Slate500
            )
        )
        Spacer(Modifier.height(10.dp))

        // Role filter chips
        Row(
            Modifier
                .background(AdminColors.Slate100, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "all" to "All",
                "parent" to "Parents",
                "driver" to "Drivers",
                "owner" to "Owners",
                "admin" to "Admins"
            ).forEach { (key, label) ->
                val selected = roleFilter == key
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selected) Color.White else Color.Transparent,
                    modifier = Modifier.clickable { roleFilter = key }
                ) {
                    Text(
                        label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) AdminColors.Slate900 else AdminColors.Slate500,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            AdminEmptyCard("👥", "No users match your search.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { u ->
                    AdminUserRow(u)
                }
            }
        }
    }
}

@Composable
private fun AdminUserRow(user: AdminUser) {
    AdminCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    listOfNotNull(user.name, user.surname)
                        .joinToString(" ")
                        .ifBlank { "No name" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AdminColors.Slate900
                )
                Spacer(Modifier.height(2.dp))
                Text(user.email, fontSize = 12.sp, color = AdminColors.Slate500)
                user.phone?.let {
                    Text(it, fontSize = 12.sp, color = AdminColors.Slate500)
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RolePill(user.role)
                    AdminBadge(if (user.isActive) "active" else "inactive")
                }
            }
        }
    }
}

@Composable
private fun RolePill(role: String) {
    val (bg, fg) = when (role.lowercase()) {
        "admin" -> AdminColors.Navy900 to Color.White
        "owner" -> AdminColors.WarnDim to AdminColors.Warn
        "driver" -> AdminColors.AccentDim to AdminColors.Accent
        "parent" -> AdminColors.SuccessDim to AdminColors.Success
        else -> AdminColors.Slate100 to AdminColors.Slate500
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            role.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}