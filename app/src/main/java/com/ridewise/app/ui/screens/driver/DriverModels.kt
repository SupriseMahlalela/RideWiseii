package com.ridewise.app.ui.screens.driver

data class DriverProfileData(
    val driverId: Int,
    val driverUniqueId: String?,
    val staffId: String?,
    val name: String,
    val surname: String?,
    val email: String,
    val phone: String?,
    val licenseNumber: String?,
    val status: String,
    val companyName: String?,
    val createdAt: String,
    val profilePhoto: String?
)

data class DriverVehicle(
    val vehicleId: Int,
    val make: String?,
    val model: String?,
    val registration: String?,
    val capacity: Int
)

data class DriverChild(
    val childId: Int,
    val name: String,
    val surname: String?,
    val grade: String?,
    val schoolName: String?,
    val parentName: String?,
    val parentSurname: String?,
    val assignmentStatus: String
)

data class DriverAnnouncement(
    val id: Int,
    val title: String,
    val message: String,
    val createdAt: String
)

data class DriverAlert(
    val id: Int,
    val title: String,
    val message: String,
    val createdAt: String
)

data class DriverDocument(
    val profilePhoto: String?,
    val licenseFile: String?,
    val pdpFile: String?,
    val roadworthyFile: String?
)

data class DriverTripSession(
    val sessionId: Int,
    val startTime: String,
    val endTime: String?,
    val distanceM: Double?,
    val status: String
)

data class BroadcastStatus(
    val isBroadcasting: Boolean,
    val latitude: Double?,
    val longitude: Double?
)

data class ActiveTripSession(
    val sessionId: Int,
    val startTime: String,
    val distanceM: Double,
    val status: String
)