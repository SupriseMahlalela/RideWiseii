package com.ridewise.app.ui.screens.admin

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
import androidx.compose.material.icons.filled.Refresh
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
fun AdminDashboardScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var selectedSection by remember { mutableStateOf("home") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var stats by remember { mutableStateOf(AdminStats()) }
    var reportStats by remember { mutableStateOf(AdminReportStats()) }
    var drivers by remember { mutableStateOf<List<AdminDriver>>(emptyList()) }
    var owners by remember { mutableStateOf<List<AdminOwner>>(emptyList()) }
    var vehicles by remember { mutableStateOf<List<AdminVehicle>>(emptyList()) }
    var children by remember { mutableStateOf<List<AdminChild>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<AdminAlert>>(emptyList()) }
    var announcements by remember { mutableStateOf<List<AdminAnnouncement>>(emptyList()) }
    var users by remember { mutableStateOf<List<AdminUser>>(emptyList()) }

    suspend fun refresh() {
        try {
            stats = fetchAdminStats()
            reportStats = fetchReportStats()
            drivers = fetchAdminDrivers()
            owners = fetchAdminOwners()
            vehicles = fetchAdminVehicles()
            children = fetchAdminChildren()
            alerts = fetchAdminAlerts()
            announcements = fetchAdminAnnouncements()
            users = fetchAdminUsers()
        } catch (e: Exception) {
            android.util.Log.e("AdminDash", "refresh failed", e)
            throw e
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try { refresh() } catch (e: Exception) { error = e.message }
        isLoading = false
    }

    val pendingDrivers = drivers.count { it.status == "pending" }
    val pendingOwners = owners.count { it.status == "pending" }
    val activeAlerts = alerts.size

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminNavigationDrawer(
                selectedSection = selectedSection,
                pendingDrivers = pendingDrivers,
                pendingOwners = pendingOwners,
                alertCount = activeAlerts,
                onSectionSelected = {
                    selectedSection = it
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    TempTokenHolder.finalToken = ""
                    TempTokenHolder.tempToken = ""
                    TempTokenHolder.userRole = ""
                    navController.navigate("login") {
                        popUpTo("admin_dashboard") { inclusive = true }
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
                            Text("RideWise Admin", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("System control panel", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                isLoading = true
                                try { refresh(); error = null } catch (e: Exception) { error = e.message }
                                isLoading = false
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                        IconButton(onClick = {
                            TempTokenHolder.finalToken = ""
                            TempTokenHolder.tempToken = ""
                            TempTokenHolder.userRole = ""
                            navController.navigate("login") {
                                popUpTo("admin_dashboard") { inclusive = true }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminColors.Navy900)
                )
            },
            containerColor = AdminColors.Slate50
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(AdminColors.Slate50)) {
                when {
                    isLoading -> AdminLoadingState("Loading admin dashboard…")
                    error != null -> AdminErrorState(error!!, onRetry = {
                        scope.launch {
                            isLoading = true
                            try { refresh(); error = null } catch (e: Exception) { error = e.message }
                            isLoading = false
                        }
                    })
                    else -> when (selectedSection) {
                        "home" -> AdminHomeScreen(
                            stats = stats,
                            reportStats = reportStats,
                            alerts = alerts,
                            onViewAlerts = { selectedSection = "alerts" }
                        )
                        "drivers" -> AdminDriversScreen(
                            drivers = drivers,
                            onChanged = { scope.launch { refresh() } }
                        )
                        "owners" -> AdminOwnersScreen(
                            owners = owners,
                            onChanged = { scope.launch { refresh() } }
                        )
                        "vehicles" -> AdminVehiclesScreen(
                            vehicles = vehicles,
                            onChanged = { scope.launch { refresh() } }
                        )
                        "children" -> AdminChildrenScreen(children = children)
                        "users" -> AdminUsersScreen(users = users)
                        "alerts" -> AdminAlertsScreen(alerts = alerts)
                        "announcements" -> AdminAnnouncementsScreen(
                            announcements = announcements,
                            onChanged = { scope.launch { refresh() } }
                        )
                        else -> AdminHomeScreen(
                            stats = stats,
                            reportStats = reportStats,
                            alerts = alerts,
                            onViewAlerts = { selectedSection = "alerts" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminNavigationDrawer(
    selectedSection: String,
    pendingDrivers: Int,
    pendingOwners: Int,
    alertCount: Int,
    onSectionSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        Triple("home", "Dashboard", 0),
        Triple("drivers", "Drivers", pendingDrivers),
        Triple("owners", "Owners", pendingOwners),
        Triple("vehicles", "Vehicles", 0),
        Triple("children", "Children", 0),
        Triple("users", "All Users", 0),
        Triple("alerts", "Alerts", alertCount),
        Triple("announcements", "Announcements", 0)
    )

    Column(Modifier.fillMaxSize().background(AdminColors.Navy900)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(AdminColors.Accent, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🛡️", fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("RideWise", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Admin Portal", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 8.dp)
        ) {
            items.forEach { (key, label, badge) ->
                val selected = selectedSection == key
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                        .clickable { onSectionSelected(key) },
                    color = if (selected) AdminColors.Accent else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 13.5.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.55f)
                        )
                        Spacer(Modifier.weight(1f))
                        if (badge > 0) {
                            Box(
                                modifier = Modifier.size(18.dp).background(AdminColors.Warn, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$badge", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
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
                modifier = Modifier.size(34.dp).background(AdminColors.Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Administrator", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text("System Account", color = Color.White.copy(alpha = 0.4f), fontSize = 10.5.sp)
            }
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) { Text("Logout", color = AdminColors.Danger) }
    }
}