package com.ridewise.app.ui.screens.owner

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
fun OwnerDashboardScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var selectedSection by remember { mutableStateOf("profile") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var profile by remember { mutableStateOf<OwnerProfileData?>(null) }
    var isActive by remember { mutableStateOf(false) }
    var pendingBadge by remember { mutableStateOf(0) }
    var waitingBadge by remember { mutableStateOf(0) }
    var alertsBadge by remember { mutableStateOf(0) }
    var stats by remember { mutableStateOf<OwnerStats?>(null) }

    suspend fun refreshHeader() {
        try {
            val p = fetchOwnerProfile()
            profile = p
            isActive = p?.status == "active"
            if (isActive) {
                val s = fetchOwnerStats()
                stats = s
                pendingBadge = s?.pendingDrivers ?: 0
                waitingBadge = s?.waitingChildren ?: 0
                alertsBadge = fetchOwnerAlerts().size
            }
        } catch (e: Exception) {
            android.util.Log.e("OwnerDash", "refreshHeader failed", e)
            throw e
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            refreshHeader()
            if (profile == null) {
                error = "Could not load owner profile. Check the server, token, or role."
            }
        } catch (e: Exception) {
            error = e.message ?: "Unknown error loading owner dashboard"
        }
        isLoading = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            OwnerNavigationDrawer(
                profile = profile,
                isActive = isActive,
                selectedSection = selectedSection,
                pendingBadge = pendingBadge,
                waitingBadge = waitingBadge,
                alertsBadge = alertsBadge,
                onSectionSelected = { section ->
                    selectedSection = section
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    TempTokenHolder.finalToken = ""
                    TempTokenHolder.tempToken = ""
                    TempTokenHolder.userRole = ""
                    navController.navigate("login") {
                        popUpTo("owner_dashboard") { inclusive = true }
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
                                profile?.companyName ?: "Owner Dashboard",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                profile?.email ?: "Loading company profile…",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        profile?.let { p ->
                            val bg = when (p.status) {
                                "active" -> OwnerColors.SuccessDim
                                "pending" -> OwnerColors.WarnDim
                                else -> OwnerColors.DangerDim
                            }
                            val fg = when (p.status) {
                                "active" -> OwnerColors.Success
                                "pending" -> OwnerColors.Warn
                                else -> OwnerColors.Danger
                            }
                            val label = when (p.status) {
                                "active" -> "APPROVED"
                                "pending" -> "PENDING"
                                else -> "REJECTED"
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = bg,
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    label,
                                    color = fg,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                        IconButton(onClick = {
                            TempTokenHolder.finalToken = ""
                            TempTokenHolder.tempToken = ""
                            TempTokenHolder.userRole = ""
                            navController.navigate("login") {
                                popUpTo("owner_dashboard") { inclusive = true }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = OwnerColors.Orange950)
                )
            },
            containerColor = OwnerColors.Slate50
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(OwnerColors.Slate50)
            ) {
                when {
                    isLoading -> LoadingState("Loading owner dashboard…")
                    error != null -> ErrorState(error!!, onRetry = {
                        scope.launch {
                            isLoading = true
                            try { refreshHeader(); error = null } catch (e: Exception) { error = e.message }
                            isLoading = false
                        }
                    })
                    else -> when (selectedSection) {
                        "profile" -> CompanyProfileScreen(profile, isActive, stats)
                        "pending" -> if (isActive) PendingDriversScreen()
                        else LockedState("Pending Drivers")
                        "all" -> if (isActive) AllDriversScreen()
                        else LockedState("All Drivers")
                        "vehicles" -> if (isActive) VehiclesScreen()
                        else LockedState("Vehicles")
                        "waitingChildren" -> if (isActive) WaitingChildrenScreen()
                        else LockedState("Waiting Children")
                        "allChildren" -> if (isActive) AllChildrenScreen()
                        else LockedState("All Children")
                        "liveTracking" -> if (isActive) LiveTrackingScreen()
                        else LockedState("Live Tracking")
                        "tripHistory" -> if (isActive) TripHistoryScreen()
                        else LockedState("Trip History")
                        "alerts" -> if (isActive) OwnerAlertsScreen()
                        else LockedState("Alerts")
                        "payments" -> if (isActive) PaymentRequestsScreen()
                        else LockedState("Payment Requests")
                        "driverDocs" -> if (isActive) DriverDocumentsScreen()
                        else LockedState("Driver Documents")
                        "announcements" -> if (isActive) OwnerAnnouncementsScreen()
                        else LockedState("Announcements")
                        else -> CompanyProfileScreen(profile, isActive, stats)
                    }
                }
            }
        }
    }
}

// ================= DRAWER =================

@Composable
private fun OwnerNavigationDrawer(
    profile: OwnerProfileData?,
    isActive: Boolean,
    selectedSection: String,
    pendingBadge: Int,
    waitingBadge: Int,
    alertsBadge: Int,
    onSectionSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        Triple("profile", "Company Profile", 0),
        Triple("pending", "Pending Drivers", pendingBadge),
        Triple("all", "All Drivers", 0),
        Triple("vehicles", "Vehicles", 0),
        Triple("waitingChildren", "Waiting Children", waitingBadge),
        Triple("allChildren", "All Children", 0),
        Triple("liveTracking", "Live Tracking", 0),
        Triple("tripHistory", "Trip History", 0),
        Triple("alerts", "Alerts", alertsBadge),
        Triple("payments", "Payment Requests", 0),
        Triple("driverDocs", "Driver Documents", 0),
        Triple("announcements", "Announcements", 0)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OwnerColors.Orange950)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(OwnerColors.Accent, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏢", fontSize = 20.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("RideWise", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Owner Portal", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            items.forEach { (key, label, badge) ->
                val enabled = isActive || key == "profile"
                val selected = selectedSection == key
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                        .clickable(enabled) { onSectionSelected(key) },
                    color = if (selected) OwnerColors.Accent else Color.Transparent,
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
                            color = when {
                                selected -> Color.White
                                enabled -> Color.White.copy(alpha = 0.55f)
                                else -> Color.White.copy(alpha = 0.25f)
                            }
                        )
                        Spacer(Modifier.weight(1f))
                        if (badge > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(OwnerColors.Warn, CircleShape),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(OwnerColors.Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile?.companyName?.firstOrNull()?.uppercase() ?: "O",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile?.companyName ?: "Loading…",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Owner Account", color = Color.White.copy(alpha = 0.4f), fontSize = 10.5.sp)
            }
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Logout", color = OwnerColors.Danger)
        }
    }
}