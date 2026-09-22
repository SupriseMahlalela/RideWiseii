package com.ridewise.app.ui.screens.driver


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ridewise.app.utils.TempTokenHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var selectedSection by remember { mutableStateOf("dashboard") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var profile by remember { mutableStateOf<DriverProfileData?>(null) }
    var vehicle by remember { mutableStateOf<DriverVehicle?>(null) }
    var children by remember { mutableStateOf<List<DriverChild>>(emptyList()) }
    var announcements by remember { mutableStateOf<List<DriverAnnouncement>>(emptyList()) }
    var alerts by remember { mutableStateOf<List<DriverAlert>>(emptyList()) }
    var documents by remember { mutableStateOf<DriverDocument?>(null) }
    var tripSessions by remember { mutableStateOf<List<DriverTripSession>>(emptyList()) }
    var broadcastStatus by remember { mutableStateOf<BroadcastStatus?>(null) }

    var showEditProfile by remember { mutableStateOf(false) }
    var showEditVehicle by remember { mutableStateOf(false) }

    // Emergency state
    var showEmergencyCountdown by remember { mutableStateOf(false) }
    var emergencyCountdown by remember { mutableIntStateOf(15) }
    var emergencySent by remember { mutableStateOf(false) }

    suspend fun refresh() {
        try {
            profile = fetchDriverProfile()
            vehicle = fetchDriverVehicle()
            children = fetchDriverChildren()
            announcements = fetchDriverAnnouncements()
            alerts = fetchDriverAlerts()
            documents = fetchDriverDocuments()
            tripSessions = fetchDriverTripSessions()
            broadcastStatus = fetchBroadcastStatus()
        } catch (e: Exception) {
            android.util.Log.e("DriverDash", "refresh failed", e)
            throw e
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            refresh()
            if (profile == null) error = "Could not load driver profile."
        } catch (e: Exception) {
            error = e.message ?: "Unknown error loading driver dashboard"
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DriverNavigationDrawer(
                    profile = profile,
                    selectedSection = selectedSection,
                    alertCount = alerts.size,
                    onSectionSelected = {
                        selectedSection = it
                        scope.launch { drawerState.close() }
                    },
                    onLogout = {
                        TempTokenHolder.finalToken = ""
                        TempTokenHolder.tempToken = ""
                        TempTokenHolder.userRole = ""
                        navController.navigate("login") {
                            popUpTo("driver_dashboard") { inclusive = true }
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
                                Text("RideWise", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    profile?.let { "Welcome, ${it.name.split(" ").firstOrNull() ?: "Driver"}" }
                                        ?: "Driver Dashboard",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        },
                        actions = {
                            BroadcastPill(
                                active = broadcastStatus?.isBroadcasting == true,
                                onToggle = {
                                    scope.launch {
                                        val current = broadcastStatus?.isBroadcasting == true
                                        if (current) {
                                            stopDriverBroadcast()
                                            broadcastStatus = broadcastStatus?.copy(isBroadcasting = false)
                                        }
                                        if (!current) selectedSection = "tracking"
                                    }
                                }
                            )
                            IconButton(onClick = {
                                TempTokenHolder.finalToken = ""
                                TempTokenHolder.tempToken = ""
                                TempTokenHolder.userRole = ""
                                navController.navigate("login") {
                                    popUpTo("driver_dashboard") { inclusive = true }
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DriverColors.Navy900)
                    )
                },
                containerColor = DriverColors.Slate50
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(DriverColors.Slate50)
                ) {
                    when {
                        isLoading -> DriverLoadingState("Loading driver profile…")
                        error != null -> DriverErrorState(error!!, onRetry = {
                            scope.launch {
                                isLoading = true
                                try { refresh(); error = null } catch (e: Exception) { error = e.message }
                                isLoading = false
                            }
                        })
                        else -> when (selectedSection) {
                            "dashboard" -> DriverHomeScreen(
                                profile = profile,
                                vehicle = vehicle,
                                children = children,
                                tripSessions = tripSessions,
                                documents = documents,
                                onEditProfile = { showEditProfile = true },
                                onEditVehicle = { showEditVehicle = true }
                            )
                            "profile" -> DriverProfileScreen(
                                profile = profile,
                                onEdit = { showEditProfile = true }
                            )
                            "vehicle" -> DriverVehicleScreen(
                                vehicle = vehicle,
                                onEdit = { showEditVehicle = true }
                            )
                            "tracking" -> DriverTrackingScreen(
                                broadcastStatus = broadcastStatus,
                                onBroadcastChanged = { broadcastStatus = it }
                            )
                            "register" -> DriverRegisterScreen()
                            "trips" -> DriverTripsScreen()
                            "notifications" -> DriverNotificationsScreen()
                            "documents" -> DriverDocumentsScreen(documents = documents)
                            "children" -> DriverChildrenScreen(children = children)
                            else -> DriverHomeScreen(
                                profile = profile,
                                vehicle = vehicle,
                                children = children,
                                tripSessions = tripSessions,
                                documents = documents,
                                onEditProfile = { showEditProfile = true },
                                onEditVehicle = { showEditVehicle = true }
                            )
                        }
                    }
                }
            }
        }

        // Emergency FAB — always on top
        EmergencyFab(
            onClick = {
                if (!showEmergencyCountdown && !emergencySent) {
                    showEmergencyCountdown = true
                    emergencyCountdown = 15
                    scope.launch {
                        while (emergencyCountdown > 0 && showEmergencyCountdown && !emergencySent) {
                            delay(1000)
                            emergencyCountdown--
                        }
                        if (emergencyCountdown == 0 && showEmergencyCountdown && !emergencySent) {
                            sendEmergencyAlert(null, null, "Emergency triggered from driver dashboard.")
                            showEmergencyCountdown = false
                            emergencySent = true
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        )

        if (showEmergencyCountdown && !emergencySent) {
            EmergencyCountdownDialog(
                countdown = emergencyCountdown,
                onCancel = { showEmergencyCountdown = false }
            )
        }
        if (emergencySent) {
            EmergencySentDialog(onDismiss = { emergencySent = false })
        }
    }

    if (showEditProfile && profile != null) {
        EditProfileDialogDriver(
            profile = profile!!,
            onDismiss = { showEditProfile = false },
            onSave = { name, surname, phone, license ->
                scope.launch {
                    if (updateDriverProfile(name, surname, phone, license)) {
                        showEditProfile = false
                        refresh()
                    }
                }
            }
        )
    }
    if (showEditVehicle && vehicle != null) {
        EditVehicleDialogDriver(
            vehicle = vehicle!!,
            onDismiss = { showEditVehicle = false },
            onSave = { registration, capacity ->
                scope.launch {
                    if (updateDriverVehicle(registration, capacity)) {
                        showEditVehicle = false
                        refresh()
                    }
                }
            }
        )
    }
}

// ================= DRAWER =================

@Composable
private fun DriverNavigationDrawer(
    profile: DriverProfileData?,
    selectedSection: String,
    alertCount: Int,
    onSectionSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        Triple("dashboard", "My Profile", 0),
        Triple("tracking", "Live Tracking", 0),
        Triple("register", "Online Register", 0),
        Triple("trips", "Trip History", 0),
        Triple("notifications", "Notifications", alertCount),
        Triple("documents", "My Documents", 0),
        Triple("children", "Assigned Children", 0)
    )

    Column(
        modifier = Modifier.fillMaxSize().background(DriverColors.Navy950)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(DriverColors.Accent, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🚌", fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("RideWise", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Driver Portal", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 8.dp)
        ) {
            items.forEach { (key, label, badge) ->
                val selected = selectedSection == key
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                        .clickable { onSectionSelected(key) },
                    color = if (selected) DriverColors.Accent else Color.Transparent,
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
                                modifier = Modifier.size(18.dp).background(DriverColors.Warn, CircleShape),
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
                modifier = Modifier.size(34.dp).background(DriverColors.Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${profile?.name?.firstOrNull()?.uppercase() ?: "D"}${profile?.surname?.firstOrNull()?.uppercase() ?: ""}",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    profile?.let { "${it.name} ${it.surname ?: ""}" } ?: "Loading…",
                    color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold
                )
                Text("Driver Account", color = Color.White.copy(alpha = 0.4f), fontSize = 10.5.sp)
            }
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) { Text("Logout", color = DriverColors.Danger) }
    }
}

// ================= BROADCAST PILL =================

@Composable
fun BroadcastPill(active: Boolean, onToggle: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = if (active) DriverColors.SuccessDim else DriverColors.Slate100,
        border = if (active) null else BorderStroke(1.dp, DriverColors.Slate200),
        modifier = Modifier.padding(end = 8.dp).clickable { onToggle() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier.size(6.dp).clip(CircleShape)
                    .background(if (active) DriverColors.Success else DriverColors.Slate500)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (active) "Broadcasting" else "Offline",
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = if (active) DriverColors.Success else DriverColors.Slate500
            )
        }
    }
}

// ================= EMERGENCY =================

@Composable
private fun EmergencyFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = DriverColors.Danger,
        shadowElevation = 8.dp,
        modifier = modifier
            .size(66.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("🚨", fontSize = 28.sp)
        }
    }
}

@Composable
private fun EmergencyCountdownDialog(countdown: Int, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Emergency Alert", color = DriverColors.Danger, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(DriverColors.DangerDim),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$countdown", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = DriverColors.Danger)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Emergency alert sending in $countdown seconds",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Admin and your transport company will be notified with your location.",
                    fontSize = 13.sp, color = DriverColors.Slate500
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Navy900)
            ) { Text("CANCEL", color = Color.White) }
        }
    )
}

@Composable
private fun EmergencySentDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alert Sent", color = DriverColors.Success, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🚨", fontSize = 42.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Admin has been notified",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DriverColors.Slate900
                )
                Text(
                    "Emergency services are being called.",
                    fontSize = 13.sp, color = DriverColors.Slate500
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DriverColors.Navy900)
            ) { Text("Close", color = Color.White) }
        }
    )
}