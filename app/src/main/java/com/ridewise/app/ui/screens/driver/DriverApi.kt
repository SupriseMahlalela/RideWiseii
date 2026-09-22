package com.ridewise.app.ui.screens.driver

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ridewise.app.utils.Constants
import com.ridewise.app.utils.TempTokenHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

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

private suspend fun sendJson(path: String, method: String, body: JSONObject): Pair<Int, String> =
    withContext(Dispatchers.IO) {
        val conn = (URL("$BASE$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            doOutput = true
            setRequestProperty("Authorization", authHeader())
            setRequestProperty("Content-Type", "application/json; utf-8")
            connectTimeout = 15000
            readTimeout = 15000
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
        conn.disconnect()
        code to text
    }

private fun JSONObject.str(key: String): String? =
    if (isNull(key)) null else optString(key, "").ifBlank { null }

private fun JSONObject.dbl(key: String): Double? = if (isNull(key)) null else optDouble(key)

// ---------------- PROFILE ----------------
suspend fun fetchDriverProfile(): DriverProfileData? {
    val (code, text) = get("/driver/profile")
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return DriverProfileData(
        driverId = o.optInt("driver_id"),
        driverUniqueId = o.str("driver_unique_id"),
        staffId = o.str("staff_id"),
        name = o.optString("name", ""),
        surname = o.str("surname"),
        email = o.optString("email", ""),
        phone = o.str("phone"),
        licenseNumber = o.str("license_number"),
        status = o.optString("status", "pending"),
        companyName = o.str("company_name"),
        createdAt = o.optString("created_at", ""),
        profilePhoto = o.str("profile_photo")
    )
}

suspend fun updateDriverProfile(name: String, surname: String, phone: String, licenseNumber: String): Boolean {
    val body = JSONObject()
        .put("name", name).put("surname", surname)
        .put("phone", phone).put("license_number", licenseNumber)
    val (code, _) = sendJson("/driver/profile", "PUT", body)
    return code in 200..299
}

suspend fun uploadDriverProfilePhoto(uri: Uri, context: Context): String? = withContext(Dispatchers.IO) {
    try {
        val file = uriToFile(uri, context) ?: return@withContext null
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build()
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("profile_photo", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()
        val req = Request.Builder()
            .url("$BASE/driver/profile/photo")
            .header("Authorization", authHeader())
            .post(body).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext null
            val text = resp.body?.string() ?: return@withContext null
            JSONObject(text).optString("profile_photo").ifBlank { null }
        }
    } catch (e: Exception) {
        Log.e("DriverApi", "uploadDriverProfilePhoto failed", e)
        null
    }
}

private fun uriToFile(uri: Uri, context: Context): File? = try {
    val input = context.contentResolver.openInputStream(uri) ?: return null
    val f = File(context.cacheDir, "driver_upload_${System.currentTimeMillis()}")
    FileOutputStream(f).use { input.copyTo(it) }
    f
} catch (e: Exception) { null }

// ---------------- VEHICLE ----------------
suspend fun fetchDriverVehicle(): DriverVehicle? {
    val (code, text) = get("/driver/vehicle")
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return DriverVehicle(
        vehicleId = o.optInt("vehicle_id"),
        make = o.str("make"),
        model = o.str("model"),
        registration = o.str("registration"),
        capacity = o.optInt("capacity", 0)
    )
}

suspend fun updateDriverVehicle(registration: String, capacity: Int): Boolean {
    val body = JSONObject().put("registration", registration).put("capacity", capacity)
    val (code, _) = sendJson("/driver/vehicle", "PUT", body)
    return code in 200..299
}

// ---------------- CHILDREN ----------------
suspend fun fetchDriverChildren(): List<DriverChild> {
    val (code, text) = get("/driver/children")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        DriverChild(
            childId = o.optInt("child_id"),
            name = o.optString("name", ""),
            surname = o.str("surname"),
            grade = o.str("grade"),
            schoolName = o.str("school_name"),
            parentName = o.str("parent_name"),
            parentSurname = o.str("parent_surname"),
            assignmentStatus = o.optString("assignment_status", "pending")
        )
    }
}

// ---------------- ANNOUNCEMENTS / ALERTS ----------------
suspend fun fetchDriverAnnouncements(): List<DriverAnnouncement> {
    val (code, text) = get("/driver/announcements")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        DriverAnnouncement(
            id = o.optInt("id"),
            title = o.optString("title"),
            message = o.optString("message"),
            createdAt = o.optString("created_at")
        )
    }
}

suspend fun postDriverAnnouncement(title: String, message: String): Boolean {
    val body = JSONObject().put("title", title).put("message", message)
    val (code, _) = sendJson("/driver/announcements", "POST", body)
    return code in 200..299
}

suspend fun fetchDriverAlerts(): List<DriverAlert> {
    val (code, text) = get("/driver/alerts")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        DriverAlert(
            id = o.optInt("id"),
            title = o.optString("title"),
            message = o.optString("message"),
            createdAt = o.optString("created_at")
        )
    }
}

// ---------------- DOCUMENTS ----------------
suspend fun fetchDriverDocuments(): DriverDocument? {
    val (code, text) = get("/driver/documents")
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return DriverDocument(
        profilePhoto = o.str("profile_photo"),
        licenseFile = o.str("license_file"),
        pdpFile = o.str("pdp_file"),
        roadworthyFile = o.str("roadworthy_file")
    )
}

// ---------------- TRIPS ----------------
suspend fun fetchDriverTripSessions(limit: Int = 20): List<DriverTripSession> {
    val (code, text) = get("/driver/trip-sessions?limit=$limit")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        DriverTripSession(
            sessionId = o.optInt("session_id"),
            startTime = o.optString("start_time"),
            endTime = o.str("end_time"),
            distanceM = o.dbl("distance_m"),
            status = o.optString("status", "ended")
        )
    }
}

// ---------------- LOCATION ----------------
suspend fun fetchBroadcastStatus(): BroadcastStatus? {
    val (code, text) = get("/driver/location")
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return BroadcastStatus(
        isBroadcasting = o.optBoolean("is_broadcasting"),
        latitude = o.dbl("latitude"),
        longitude = o.dbl("longitude")
    )
}

suspend fun pushDriverLocation(lat: Double, lng: Double, broadcasting: Boolean): Boolean {
    val body = JSONObject()
        .put("latitude", lat).put("longitude", lng)
        .put("is_broadcasting", broadcasting)
    val (code, _) = sendJson("/driver/location", "POST", body)
    return code in 200..299
}

// ---------------- EMERGENCY ----------------
suspend fun sendEmergencyAlert(
    lat: Double?, lng: Double?, description: String
): Boolean {
    val body = JSONObject()
        .put("latitude", lat ?: JSONObject.NULL)
        .put("longitude", lng ?: JSONObject.NULL)
        .put("location", if (lat != null && lng != null) "${"%.6f".format(lat)}, ${"%.6f".format(lng)}" else "Location unavailable")
        .put("accident_type", "accident")
        .put("description", description)
        .put("injuries", JSONObject.NULL)
    val (code, _) = sendJson("/driver/report-accident", "POST", body)
    return code in 200..299
}

suspend fun stopDriverBroadcast(): Boolean {
    val body = JSONObject().put("is_broadcasting", false)
    val (code, _) = sendJson("/driver/location", "POST", body)
    return code in 200..299
}

// ---------------- TRIP SESSIONS ----------------

suspend fun fetchActiveTripSession(): ActiveTripSession? {
    val (code, text) = get("/driver/trip-sessions/active")
    if (code !in 200..299) return null
    if (text.isBlank() || text == "null") return null
    val o = JSONObject(text)
    if (o.isNull("session_id")) return null
    return ActiveTripSession(
        sessionId = o.optInt("session_id"),
        startTime = o.optString("start_time"),
        distanceM = o.optDouble("distance_m", 0.0),
        status = o.optString("status", "active")
    )
}

suspend fun startDriverTripSession(lat: Double?, lng: Double?): ActiveTripSession? {
    val body = JSONObject()
        .put("latitude", lat ?: JSONObject.NULL)
        .put("longitude", lng ?: JSONObject.NULL)
    val (code, text) = sendJson("/driver/trip-sessions/start", "POST", body)
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return ActiveTripSession(
        sessionId = o.optInt("session_id"),
        startTime = o.optString("start_time"),
        distanceM = 0.0,
        status = "active"
    )
}

suspend fun endDriverTripSession(
    sessionId: Int,
    lat: Double?,
    lng: Double?,
    distanceM: Double
): Boolean {
    val body = JSONObject()
        .put("session_id", sessionId)
        .put("latitude", lat ?: JSONObject.NULL)
        .put("longitude", lng ?: JSONObject.NULL)
        .put("distance_m", distanceM)
    val (code, _) = sendJson("/driver/trip-sessions/end", "POST", body)
    return code in 200..299
}