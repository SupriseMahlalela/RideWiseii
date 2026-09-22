package com.ridewise.app.ui.screens.owner

data class OwnerProfileData(
    val ownerId: Int,
    val companyName: String,
    val email: String,
    val contactPhone: String?,
    val contactAddress: String?,
    val businessRegistrationNumber: String?,
    val operatingLicenseNumber: String?,
    val inviteCode: String?,
    val status: String,
    val createdAt: String?
)

data class OwnerDriver(
    val driverId: Int,
    val driverUniqueId: String?,
    val staffId: String?,
    val name: String,
    val surname: String,
    val email: String,
    val phone: String?,
    val licenseNumber: String?,
    val status: String,
    val createdAt: String?,
    val vehicleRegistration: String?,
    val vehicleMake: String?,
    val vehicleModel: String?,
    val licenseFile: String?,
    val pdpFile: String?,
    val roadworthyFile: String?
)

data class OwnerVehicle(
    val vehicleId: Int,
    val registration: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val capacity: Int?,
    val color: String?,
    val status: String,
    val driverId: Int?,
    val driverName: String?,
    val driverSurname: String?,
    val driverUniqueId: String?,
    val registrationDoc: String?,
    val roadworthyDoc: String?,
    val insuranceDoc: String?
)

data class OwnerChild(
    val childId: Int,
    val name: String,
    val surname: String,
    val grade: String?,
    val schoolName: String?,
    val parentName: String,
    val parentSurname: String,
    val parentPhone: String?,
    val driverId: Int?,
    val driverName: String?,
    val driverSurname: String?,
    val assignmentStatus: String?
)

data class OwnerPayment(
    val paymentId: Int,
    val invoiceNumber: String,
    val childName: String,
    val childSurname: String,
    val parentName: String,
    val parentSurname: String,
    val driverName: String,
    val driverSurname: String,
    val amount: Double,
    val description: String?,
    val status: String,
    val paymentDate: String?
)

data class OwnerAlert(
    val id: Int,
    val type: String,          // emergency | overload | trip_started | trip_ended | ...
    val title: String,
    val message: String,
    val createdAt: String
)

data class OwnerTripSession(
    val sessionId: Int,
    val driverName: String,
    val driverSurname: String,
    val driverUniqueId: String?,
    val startTime: String,
    val endTime: String?,
    val distanceM: Double?,
    val status: String          // active | ended
)

data class OwnerAnnouncement(
    val id: Int,
    val title: String,
    val message: String,
    val createdAt: String
)

data class DriverLocation(
    val driverId: Int,
    val name: String,
    val surname: String,
    val driverUniqueId: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isBroadcasting: Boolean
)

data class OwnerStats(
    val pendingDrivers: Int = 0,
    val activeDrivers: Int = 0,
    val totalDrivers: Int = 0,
    val totalChildren: Int = 0,
    val assignedChildren: Int = 0,
    val waitingChildren: Int = 0,
    val totalVehicles: Int = 0,
    val assignedVehicles: Int = 0,
    val pendingPayments: Int = 0,
    val paidPayments: Int = 0
)

data class AvailableDriver(
    val driverId: Int,
    val name: String,
    val surname: String,
    val driverUniqueId: String?
)