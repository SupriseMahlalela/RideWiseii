package com.ridewise.app.ui.screens.admin

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

private val BASE = "${Constants.BASE_URL}/api"
private fun authHeader() = "Bearer ${TempTokenHolder.finalToken}"

private suspend fun get(path: String): Pair<Int, String> = withContext(Dispatchers.IO) {
    val conn = (URL("$BASE$path").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        setRequestProperty("Authorization", authHeader())
        connectTimeout = 15000
        readTimeout = 15000
    }
    val code = conn.responseCode
    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
    val text = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
    conn.disconnect()
    code to text
}

private suspend fun sendJson(path: String, method: String, body: JSONObject?): Pair<Int, String> =
    withContext(Dispatchers.IO) {
        val conn = (URL("$BASE$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", authHeader())
            connectTimeout = 15000
            readTimeout = 15000
            if (body != null || method != "GET") {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; utf-8")
            }
        }
        body?.let {
            OutputStreamWriter(conn.outputStream).use { w -> w.write(it.toString()) }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
        conn.disconnect()
        code to text
    }

private fun JSONObject.s(key: String): String? = if (isNull(key)) null else optString(key, "").ifBlank { null }

// ---------------- STATS ----------------
suspend fun fetchAdminStats(): AdminStats {
    val (code, text) = get("/admin/dashboard-stats")
    if (code !in 200..299) return AdminStats()
    val o = JSONObject(text)
    return AdminStats(
        students = o.optInt("students", 0),
        drivers = o.optInt("drivers", 0),
        pendingDrivers = o.optInt("pendingDrivers", 0),
        owners = o.optInt("owners", 0),
        pendingOwners = o.optInt("pendingOwners", 0),
        vehicles = o.optInt("vehicles", 0)
    )
}

suspend fun fetchReportStats(): AdminReportStats {
    val (code, text) = get("/admin/report-stats")
    if (code !in 200..299) return AdminReportStats()
    val o = JSONObject(text)
    return AdminReportStats(
        totalTrips = o.optInt("totalTrips", 0),
        tripsToday = o.optInt("tripsToday", 0),
        activeDrivers = o.optInt("activeDrivers", 0),
        pendingApprovals = o.optInt("pendingApprovals", 0)
    )
}

// ---------------- DRIVERS ----------------
suspend fun fetchAdminDrivers(): List<AdminDriver> {
    val (code, text) = get("/admin/drivers")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AdminDriver(
            driverId = o.optInt("driver_id"),
            driverUniqueId = o.s("driver_unique_id"),
            name = o.optString("name", ""),
            surname = o.optString("surname", ""),
            email = o.optString("email", ""),
            phone = o.s("phone"),
            licenseNumber = o.s("license_number"),
            status = o.optString("status", "pending"),
            companyName = o.s("company_name"),
            createdAt = o.s("created_at")
        )
    }
}

suspend fun approveAdminDriver(driverId: Int): Boolean {
    val (code, _) = sendJson("/admin/drivers/$driverId/approve", "PUT", null)
    return code in 200..299
}

suspend fun rejectAdminDriver(driverId: Int): Boolean {
    val (code, _) = sendJson("/admin/drivers/$driverId/reject", "PUT", null)
    return code in 200..299
}

// ---------------- OWNERS ----------------
suspend fun fetchAdminOwners(): List<AdminOwner> {
    val (code, text) = get("/admin/owners")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AdminOwner(
            ownerId = o.optInt("owner_id"),
            companyName = o.optString("company_name", ""),
            email = o.optString("email", ""),
            phone = o.s("contact_phone"),
            inviteCode = o.s("invite_code"),
            status = o.optString("status", "pending"),
            createdAt = o.s("created_at")
        )
    }
}

suspend fun approveAdminOwner(ownerId: Int): Boolean {
    val (code, _) = sendJson("/admin/owners/$ownerId/approve", "PUT", null)
    return code in 200..299
}

suspend fun rejectAdminOwner(ownerId: Int): Boolean {
    val (code, _) = sendJson("/admin/owners/$ownerId/reject", "PUT", null)
    return code in 200..299
}

// ---------------- VEHICLES ----------------
suspend fun fetchAdminVehicles(): List<AdminVehicle> {
    val (code, text) = get("/admin/vehicles")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AdminVehicle(
            vehicleId = o.optInt("vehicle_id"),
            make = o.s("make"),
            model = o.s("model"),
            registration = o.s("registration"),
            capacity = o.optInt("capacity", 0),
            status = o.optString("status", "unassigned"),
            driverName = o.s("driver_name"),
            companyName = o.s("company_name")
        )
    }
}

suspend fun addAdminVehicle(
    make: String, model: String, registration: String, capacity: Int
): Boolean {
    val body = JSONObject()
        .put("make", make).put("model", model)
        .put("registration", registration).put("capacity", capacity)
    val (code, _) = sendJson("/admin/vehicles", "POST", body)
    return code in 200..299
}

suspend fun updateAdminVehicle(
    vehicleId: Int, make: String, model: String, registration: String, capacity: Int
): Boolean {
    val body = JSONObject()
        .put("make", make).put("model", model)
        .put("registration", registration).put("capacity", capacity)
    val (code, _) = sendJson("/admin/vehicles/$vehicleId", "PUT", body)
    return code in 200..299
}

suspend fun deleteAdminVehicle(vehicleId: Int): Boolean {
    val (code, _) = sendJson("/admin/vehicles/$vehicleId", "DELETE", null)
    return code in 200..299
}

// ---------------- CHILDREN ----------------
suspend fun fetchAdminChildren(): List<AdminChild> {
    val (code, text) = get("/admin/children")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AdminChild(
            childId = o.optInt("child_id"),
            name = o.optString("name", ""),
            surname = o.optString("surname", ""),
            grade = o.s("grade"),
            schoolName = o.s("school_name"),
            parentName = o.s("parent_name"),
            parentSurname = o.s("parent_surname"),
            driverName = o.s("driver_name"),
            assignmentStatus = o.s("assignment_status")
        )
    }
}

// ---------------- ALERTS ----------------
suspend fun fetchAdminAlerts(): List<AdminAlert> {
    val (code, text) = get("/admin/alerts")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AdminAlert(
            id = o.optInt("id"),
            type = o.optString("type", "system"),
            title = o.optString("title", ""),
            message = o.optString("message", ""),
            driverName = o.s("driver_name"),
            createdAt = o.optString("created_at", "")
        )
    }
}

// ---------------- ANNOUNCEMENTS ----------------
suspend fun fetchAdminAnnouncements(): List<AdminAnnouncement> {
    val (code, text) = get("/admin/announcements")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AdminAnnouncement(
            id = o.optInt("id"),
            title = o.optString("title", ""),
            message = o.optString("message", ""),
            createdAt = o.optString("created_at", "")
        )
    }
}

suspend fun postAdminAnnouncement(title: String, message: String): Boolean {
    val body = JSONObject().put("title", title).put("message", message)
    val (code, _) = sendJson("/admin/announcements", "POST", body)
    return code in 200..299
}

// ---------------- USERS ----------------
suspend fun fetchAdminUsers(): List<AdminUser> {
    val (code, text) = get("/admin/users")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        AdminUser(
            userId = o.optInt("user_id"),
            name = o.s("name"),
            surname = o.s("surname"),
            email = o.optString("email", ""),
            role = o.optString("role", "parent"),
            phone = o.s("phone"),
            isActive = o.optBoolean("is_active", true)
        )
    }
}