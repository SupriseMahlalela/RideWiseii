package com.ridewise.app.ui.screens.owner

import com.ridewise.app.utils.Constants
import com.ridewise.app.utils.TempTokenHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private const val BASE =  "${Constants.BASE_URL}/api"   // emulator → host machine
// private const val BASE = "http://localhost:5000/api" // physical device w/ port forward

private fun authHeader(): String = "Bearer ${TempTokenHolder.finalToken}"

private suspend fun request(
    path: String,
    method: String = "GET",
    body: JSONObject? = null,
    contentType: String = "application/json"
): Pair<Int, String> = withContext(Dispatchers.IO) {
    val conn = (URL("$BASE$path").openConnection() as HttpURLConnection).apply {
        requestMethod = method
        setRequestProperty("Authorization", authHeader())
        if (body != null || method != "GET") {
            doOutput = true
            setRequestProperty("Content-Type", contentType)
        }
        connectTimeout = 15000
        readTimeout = 15000
    }
    if (body != null) {
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
    }
    val code = conn.responseCode
    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
    val text = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
    conn.disconnect()
    code to text
}

private fun JSONObject.strOrNull(key: String): String? =
    if (isNull(key)) null else optString(key, "").ifBlank { null }

private fun JSONObject.intOrNull(key: String): Int? =
    if (isNull(key)) null else optInt(key)

private fun JSONObject.dblOrNull(key: String): Double? =
    if (isNull(key)) null else optDouble(key)

// ---------------- PROFILE ----------------
suspend fun fetchOwnerProfile(): OwnerProfileData? {
    val (code, text) = request("/owner/profile")
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return OwnerProfileData(
        ownerId = o.optInt("owner_id"),
        companyName = o.optString("company_name"),
        email = o.optString("email"),
        contactPhone = o.strOrNull("contact_phone"),
        contactAddress = o.strOrNull("contact_address"),
        businessRegistrationNumber = o.strOrNull("business_registration_number"),
        operatingLicenseNumber = o.strOrNull("operating_license_number"),
        inviteCode = o.strOrNull("invite_code"),
        status = o.optString("status", "pending"),
        createdAt = o.strOrNull("created_at")
    )
}

suspend fun fetchOwnerStats(): OwnerStats? = coroutineScope {
    try {
        // Fire everything in parallel — total latency is one call, not seven
        val directDeferred   = async {
            val (code, text) = request("/owner/stats")
            if (code in 200..299) JSONObject(text) else null
        }
        val driversDeferred  = async { fetchAllDrivers() }
        val pendingDeferred  = async { fetchPendingDrivers() }
        val childrenDeferred = async { fetchAllOwnerChildren() }
        val waitingDeferred  = async { fetchWaitingChildren() }
        val vehiclesDeferred = async { fetchVehicles() }
        val paymentsDeferred = async { fetchOwnerPayments() }

        val direct   = directDeferred.await()
        val drivers  = driversDeferred.await()
        val pending  = pendingDeferred.await()
        val children = childrenDeferred.await()
        val waiting  = waitingDeferred.await()
        val vehicles = vehiclesDeferred.await()
        val payments = paymentsDeferred.await()

        OwnerStats(
            // prefer /owner/stats when it exists; otherwise compute from the fetch
            pendingDrivers    = direct?.optInt("pending_drivers")    ?: pending.size,
            activeDrivers     = direct?.optInt("active_drivers")     ?: drivers.count { it.status == "active" },
            totalDrivers      = drivers.size,
            totalChildren     = direct?.optInt("total_children")     ?: children.size,
            assignedChildren  = children.count { it.driverId != null },
            waitingChildren   = waiting.size,
            totalVehicles     = vehicles.size,
            assignedVehicles  = vehicles.count { it.driverId != null },
            pendingPayments   = payments.count { it.status == "pending" },
            paidPayments      = payments.count { it.status == "paid" }
        )
    } catch (e: Exception) {
        android.util.Log.e("OwnerApi", "fetchOwnerStats failed", e)
        null
    }
}

// ---------------- DRIVERS ----------------
private fun parseDriver(o: JSONObject) = OwnerDriver(
    driverId = o.optInt("driver_id"),
    driverUniqueId = o.strOrNull("driver_unique_id"),
    staffId = o.strOrNull("staff_id"),
    name = o.optString("name"),
    surname = o.optString("surname"),
    email = o.optString("email"),
    phone = o.strOrNull("phone"),
    licenseNumber = o.strOrNull("license_number"),
    status = o.optString("status", "pending"),
    createdAt = o.strOrNull("created_at"),
    vehicleRegistration = o.strOrNull("vehicle_registration"),
    vehicleMake = o.strOrNull("vehicle_make"),
    vehicleModel = o.strOrNull("vehicle_model"),
    licenseFile = o.strOrNull("license_file"),
    pdpFile = o.strOrNull("pdp_file"),
    roadworthyFile = o.strOrNull("roadworthy_file")
)

suspend fun fetchPendingDrivers(): List<OwnerDriver> {
    val (code, text) = request("/owner/drivers/pending")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map { parseDriver(arr.getJSONObject(it)) }
}

suspend fun fetchAllDrivers(): List<OwnerDriver> {
    val (code, text) = request("/owner/drivers")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map { parseDriver(arr.getJSONObject(it)) }
}

suspend fun approveDriver(driverId: Int): Boolean {
    val (code, _) = request("/owner/drivers/$driverId/approve", "PUT")
    return code in 200..299
}

suspend fun rejectDriver(driverId: Int): Boolean {
    val (code, _) = request("/owner/drivers/$driverId/reject", "PUT")
    return code in 200..299
}

// ---------------- VEHICLES ----------------
private fun parseVehicle(o: JSONObject) = OwnerVehicle(
    vehicleId = o.optInt("vehicle_id"),
    registration = o.optString("registration"),
    make = o.strOrNull("make"),
    model = o.strOrNull("model"),
    year = o.intOrNull("year"),
    capacity = o.intOrNull("capacity"),
    color = o.strOrNull("color"),
    status = o.optString("status", "unassigned"),
    driverId = o.intOrNull("driver_id"),
    driverName = o.strOrNull("driver_name"),
    driverSurname = o.strOrNull("driver_surname"),
    driverUniqueId = o.strOrNull("driver_unique_id"),
    registrationDoc = o.strOrNull("registration_doc"),
    roadworthyDoc = o.strOrNull("roadworthy_doc"),
    insuranceDoc = o.strOrNull("insurance_doc")
)

suspend fun fetchVehicles(): List<OwnerVehicle> {
    val (code, text) = request("/owner/vehicles")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map { parseVehicle(arr.getJSONObject(it)) }
}

suspend fun fetchAvailableDriversForVehicle(): List<AvailableDriver> {
    val (code, text) = request("/owner/vehicles/available-drivers")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AvailableDriver(
            driverId = o.optInt("driver_id"),
            name = o.optString("name"),
            surname = o.optString("surname"),
            driverUniqueId = o.strOrNull("driver_unique_id")
        )
    }
}

suspend fun createVehicle(payload: JSONObject): Boolean {
    val (code, _) = request("/owner/vehicles", "POST", payload)
    return code in 200..299
}

suspend fun updateVehicle(vehicleId: Int, payload: JSONObject): Boolean {
    val (code, _) = request("/owner/vehicles/$vehicleId", "PUT", payload)
    return code in 200..299
}

suspend fun assignVehicle(vehicleId: Int, driverId: Int): Boolean {
    val body = JSONObject().put("driver_id", driverId)
    val (code, _) = request("/owner/vehicles/$vehicleId/assign", "PUT", body)
    return code in 200..299
}

suspend fun unassignVehicle(vehicleId: Int): Boolean {
    val (code, _) = request("/owner/vehicles/$vehicleId/unassign", "PUT")
    return code in 200..299
}

suspend fun deleteVehicle(vehicleId: Int): Boolean {
    val (code, _) = request("/owner/vehicles/$vehicleId", "DELETE")
    return code in 200..299
}

// ---------------- CHILDREN ----------------
private fun parseChild(o: JSONObject) = OwnerChild(
    childId = o.optInt("child_id"),
    name = o.optString("name"),
    surname = o.optString("surname"),
    grade = o.strOrNull("grade"),
    schoolName = o.strOrNull("school_name"),
    parentName = o.optString("parent_name"),
    parentSurname = o.optString("parent_surname"),
    parentPhone = o.strOrNull("parent_phone"),
    driverId = o.intOrNull("driver_id"),
    driverName = o.strOrNull("driver_name"),
    driverSurname = o.strOrNull("driver_surname"),
    assignmentStatus = o.strOrNull("assignment_status")
)

suspend fun fetchWaitingChildren(): List<OwnerChild> {
    val (code, text) = request("/owner/children/waiting")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map { parseChild(arr.getJSONObject(it)) }
}

suspend fun fetchAllOwnerChildren(): List<OwnerChild> {
    val (code, text) = request("/owner/children")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map { parseChild(arr.getJSONObject(it)) }
}

suspend fun fetchAvailableDriversForChild(): List<AvailableDriver> {
    val (code, text) = request("/owner/children/available-drivers")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AvailableDriver(
            driverId = o.optInt("driver_id"),
            name = o.optString("name"),
            surname = o.optString("surname"),
            driverUniqueId = o.strOrNull("driver_unique_id")
        )
    }
}

suspend fun assignChildToDriver(childId: Int, driverId: Int): Boolean {
    val body = JSONObject().put("driver_id", driverId)
    val (code, _) = request("/owner/children/$childId/assign", "PUT", body)
    return code in 200..299
}

// ---------------- PAYMENTS ----------------
private fun parsePayment(o: JSONObject) = OwnerPayment(
    paymentId = o.optInt("payment_id"),
    invoiceNumber = o.optString("invoice_number"),
    childName = o.optString("child_name"),
    childSurname = o.optString("child_surname"),
    parentName = o.optString("parent_name"),
    parentSurname = o.optString("parent_surname"),
    driverName = o.optString("driver_name"),
    driverSurname = o.optString("driver_surname"),
    amount = o.optDouble("amount"),
    description = o.strOrNull("description"),
    status = o.optString("status", "pending"),
    paymentDate = o.strOrNull("payment_date")
)

suspend fun fetchOwnerPayments(): List<OwnerPayment> {
    val (code, text) = request("/owner/payments")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map { parsePayment(arr.getJSONObject(it)) }
}

suspend fun requestPayment(childId: Int, amount: Double, description: String?): Boolean {
    val body = JSONObject()
        .put("child_id", childId)
        .put("amount", amount)
        .put("description", description ?: "")
    val (code, _) = request("/owner/payments/request", "POST", body)
    return code in 200..299
}

suspend fun editPaymentRequest(paymentId: Int, amount: Double, description: String?): Boolean {
    val body = JSONObject()
        .put("amount", amount)
        .put("description", description ?: "")
    val (code, _) = request("/owner/payments/$paymentId", "PUT", body)
    return code in 200..299
}

// ---------------- TRACKING / TRIPS / ALERTS ----------------
suspend fun fetchDriverLocations(): List<DriverLocation> {
    val (code, text) = request("/owner/drivers/locations")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        DriverLocation(
            driverId = o.optInt("driver_id"),
            name = o.optString("name"),
            surname = o.optString("surname"),
            driverUniqueId = o.strOrNull("driver_unique_id"),
            latitude = o.dblOrNull("latitude"),
            longitude = o.dblOrNull("longitude"),
            isBroadcasting = o.optBoolean("is_broadcasting")
        )
    }
}

suspend fun fetchOwnerTripSessions(): List<OwnerTripSession> {
    val (code, text) = request("/owner/trip-sessions")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        OwnerTripSession(
            sessionId = o.optInt("session_id"),
            driverName = o.optString("driver_name"),
            driverSurname = o.optString("driver_surname"),
            driverUniqueId = o.strOrNull("driver_unique_id"),
            startTime = o.optString("start_time"),
            endTime = o.strOrNull("end_time"),
            distanceM = o.dblOrNull("distance_m"),
            status = o.optString("status", "ended")
        )
    }
}

suspend fun fetchOwnerAlerts(): List<OwnerAlert> {
    val (code, text) = request("/owner/alerts")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        OwnerAlert(
            id = o.optInt("id"),
            type = o.optString("type"),
            title = o.optString("title"),
            message = o.optString("message"),
            createdAt = o.optString("created_at")
        )
    }
}

// ---------------- ANNOUNCEMENTS ----------------
suspend fun fetchOwnerAnnouncements(): List<OwnerAnnouncement> {
    val (code, text) = request("/owner/announcements")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        OwnerAnnouncement(
            id = o.optInt("id"),
            title = o.optString("title"),
            message = o.optString("message"),
            createdAt = o.optString("created_at")
        )
    }
}

suspend fun postOwnerAnnouncement(title: String, message: String): Boolean {
    val body = JSONObject().put("title", title).put("message", message)
    val (code, _) = request("/owner/announcements", "POST", body)
    return code in 200..299
}