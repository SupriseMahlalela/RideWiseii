package com.ridewise.app.ui.screens.admin

data class AdminStats(
    val students: Int = 0,
    val drivers: Int = 0,
    val pendingDrivers: Int = 0,
    val owners: Int = 0,
    val pendingOwners: Int = 0,
    val vehicles: Int = 0
)

data class AdminDriver(
    val driverId: Int,
    val driverUniqueId: String?,
    val name: String,
    val surname: String,
    val email: String,
    val phone: String?,
    val licenseNumber: String?,
    val status: String,
    val companyName: String?,
    val createdAt: String?
)

data class AdminOwner(
    val ownerId: Int,
    val companyName: String,
    val email: String,
    val phone: String?,
    val inviteCode: String?,
    val status: String,
    val createdAt: String?
)

data class AdminVehicle(
    val vehicleId: Int,
    val make: String?,
    val model: String?,
    val registration: String?,
    val capacity: Int,
    val status: String,
    val driverName: String?,
    val companyName: String?
)

data class AdminChild(
    val childId: Int,
    val name: String,
    val surname: String,
    val grade: String?,
    val schoolName: String?,
    val parentName: String?,
    val parentSurname: String?,
    val driverName: String?,
    val assignmentStatus: String?
)

data class AdminAlert(
    val id: Int,
    val type: String,       // emergency | overload | trip_started | trip_ended | system
    val title: String,
    val message: String,
    val driverName: String?,
    val createdAt: String
)

data class AdminAnnouncement(
    val id: Int,
    val title: String,
    val message: String,
    val createdAt: String
)

data class AdminUser(
    val userId: Int,
    val name: String?,
    val surname: String?,
    val email: String,
    val role: String,
    val phone: String?,
    val isActive: Boolean
)

data class AdminReportStats(
    val totalTrips: Int = 0,
    val tripsToday: Int = 0,
    val activeDrivers: Int = 0,
    val pendingApprovals: Int = 0
)