package com.ridewise.app.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ridewise.app.utils.TempTokenHolder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var selectedSection by remember { mutableStateOf("dashboard") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var profile by remember { mutableStateOf<ParentProfile?>(null) }
    var children by remember { mutableStateOf<List<ParentChild>>(emptyList()) }
    var payments by remember { mutableStateOf<List<ParentPayment>>(emptyList()) }
    var announcements by remember { mutableStateOf<List<ParentAnnouncement>>(emptyList()) }

    suspend fun refreshHeader() {
        try {
            profile = fetchParentProfile()
            children = fetchParentChildren()
            payments = fetchParentPayments()
            announcements = fetchParentAnnouncements()
        } catch (e: Exception) {
            android.util.Log.e("ParentDash", "refreshHeader failed", e)
            throw e
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            refreshHeader()
            if (profile == null) error = "Could not load your profile."
        } catch (e: Exception) {
            error = e.message ?: "Unknown error loading parent dashboard"
        }
        isLoading = false
    }

    val pendingPayments = payments.count { it.status != "paid" }
    val hasNotifications = announcements.isNotEmpty()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentNavigationDrawer(
                profile = profile,
                selectedSection = selectedSection,
                pendingPayments = pendingPayments,
                hasNotifications = hasNotifications,
                onSectionSelected = { section ->
                    selectedSection = section
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    TempTokenHolder.finalToken = ""
                    TempTokenHolder.tempToken = ""
                    TempTokenHolder.userRole = ""
                    navController.navigate("login") {
                        popUpTo("parent_dashboard") { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "RideWise",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Welcome, ${profile?.name?.split(" ")?.firstOrNull() ?: "Parent"}",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { selectedSection = "notifications" }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                        IconButton(onClick = {
                            TempTokenHolder.finalToken = ""
                            TempTokenHolder.tempToken = ""
                            TempTokenHolder.userRole = ""
                            navController.navigate("login") {
                                popUpTo("parent_dashboard") { inclusive = true }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ParentColors.Green950)
                )
            },
            containerColor = ParentColors.Slate50
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(ParentColors.Slate50)
            ) {
                when {
                    isLoading -> ParentLoadingState("Loading your dashboard…")
                    error != null -> ParentErrorState(error!!, onRetry = {
                        scope.launch {
                            isLoading = true
                            try { refreshHeader(); error = null } catch (e: Exception) { error = e.message }
                            isLoading = false
                        }
                    })
                    else -> when (selectedSection) {
                        "dashboard" -> ParentHomeScreen(
                            profile = profile,
                            children = children,
                            payments = payments,
                            announcements = announcements,
                            onViewAllChildren = { selectedSection = "children" }
                        )
                        "tracking" -> ParentTrackingScreen(children = children)
                        "trips" -> ParentTripsScreen()
                        "payments" -> ParentPaymentsScreen()
                        "notifications" -> ParentNotificationsScreen()
                        "profile" -> ParentProfileScreen(profile = profile, onSaved = {
                            scope.launch { refreshHeader() }
                        })
                        "children" -> ParentChildrenScreen(children = children, onChanged = {
                            scope.launch { refreshHeader() }
                        })
                        else -> ParentHomeScreen(
                            profile = profile,
                            children = children,
                            payments = payments,
                            announcements = announcements,
                            onViewAllChildren = { selectedSection = "children" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentNavigationDrawer(
    profile: ParentProfile?,
    selectedSection: String,
    pendingPayments: Int,
    hasNotifications: Boolean,
    onSectionSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    data class Item(val key: String, val label: String, val badge: Int = 0, val dot: Boolean = false)

    val items = listOf(
        Item("dashboard", "Dashboard"),
        Item("tracking", "Live Tracking"),
        Item("trips", "Trip History"),
        Item("payments", "Payments", badge = pendingPayments),
        Item("notifications", "Notifications", dot = hasNotifications),
        Item("profile", "My Profile"),
        Item("children", "Child Details")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ParentColors.Green950)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp)
                    .background(ParentColors.Accent, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🚌", fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("RideWise", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Parent Portal", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            items.forEach { item ->
                val selected = selectedSection == item.key
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                        .clickable { onSectionSelected(item.key) },
                    color = if (selected) ParentColors.Accent.copy(alpha = 0.18f) else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Text(
                            item.label,
                            fontSize = 13.5.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.55f)
                        )
                        Spacer(Modifier.weight(1f))
                        when {
                            item.badge > 0 -> Box(
                                modifier = Modifier.size(18.dp)
                                    .background(ParentColors.Warn, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${item.badge}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            item.dot -> Box(
                                modifier = Modifier.size(9.dp)
                                    .background(ParentColors.Danger, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Box(
                modifier = Modifier.size(34.dp).background(ParentColors.Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile?.name?.firstOrNull()?.uppercase() ?: "P",
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile?.let { "${it.name} ${it.surname}" } ?: "Parent User",
                    color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold
                )
                Text("Parent Account", color = Color.White.copy(alpha = 0.4f), fontSize = 10.5.sp)
            }
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Logout", color = ParentColors.Danger)
        }
    }
}