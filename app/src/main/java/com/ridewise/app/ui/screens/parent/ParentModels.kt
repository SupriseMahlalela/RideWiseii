package com.ridewise.app.ui.screens.parent

data class ParentProfile(
    val name: String,
    val surname: String,
    val email: String,
    val phone: String,
    val address: String,
    val createdAt: String
)

data class ParentChild(
    val childId: Int,
    val name: String,
    val surname: String,
    val grade: String,
    val schoolName: String,
    val pickupLocation: String,
    val dropoffLocation: String,
    val assignmentStatus: String,
    val driverName: String?,
    val driverSurname: String?,
    val companyName: String?,
    val profilePhoto: String?,
    val ownerId: Int?
)

data class ParentCompany(
    val ownerId: Int,
    val companyName: String
)

data class ParentPayment(
    val paymentId: Int,
    val invoiceNumber: String?,
    val childName: String,
    val childSurname: String,
    val driverName: String?,
    val driverSurname: String?,
    val amount: Double,
    val description: String?,
    val status: String,
    val paymentDate: String?,
    val month: String?
)

data class ParentTrip(
    val id: Int,
    val route: String?,
    val date: String,
    val completedAt: String?,
    val child: String?,
    val driver: String?,
    val distance: String?,
    val duration: String?,
    val pickupTime: String?,
    val dropoffTime: String?,
    val status: String,
    val notes: String?
)

data class ParentAlert(
    val id: Int,
    val title: String,
    val message: String,
    val time: String?
)

data class ParentAnnouncement(
    val id: Int,
    val title: String,
    val message: String,
    val createdAt: String
)

data class ChildLocation(
    val hasDriver: Boolean,
    val isBroadcasting: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val driverName: String?,
    val driverSurname: String?
)