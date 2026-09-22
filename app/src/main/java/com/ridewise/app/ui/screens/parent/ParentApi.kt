package com.ridewise.app.ui.screens.parent

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

private suspend fun sendJson(
    path: String,
    method: String,
    body: JSONObject
): Pair<Int, String> = withContext(Dispatchers.IO) {
    val conn = (URL("$BASE$path").openConnection() as HttpURLConnection).apply {
        requestMethod = method
        doOutput = true
        setRequestProperty("Authorization", authHeader())
        setRequestProperty("Content-Type", "application/json; utf-8")
        connectTimeout = 15000
        readTimeout = 15000
    }
    conn.outputStream.use { it.write(body.toString().toByteArray()) }
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
suspend fun fetchParentProfile(): ParentProfile? {
    val (code, text) = get("/parent/profile")
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return ParentProfile(
        name = o.optString("name", ""),
        surname = o.optString("surname", ""),
        email = o.optString("email", ""),
        phone = o.optString("phone", "Not provided"),
        address = o.optString("address", "Not provided"),
        createdAt = o.optString("created_at", "")
    )
}

suspend fun updateParentProfile(
    name: String, surname: String, phone: String, address: String
): Boolean {
    val body = JSONObject()
        .put("name", name).put("surname", surname)
        .put("phone", phone).put("address", address)
    val (code, _) = sendJson("/parent/profile", "PUT", body)
    return code in 200..299
}

// ---------------- CHILDREN ----------------
private fun parseChild(o: JSONObject) = ParentChild(
    childId = o.optInt("child_id"),
    name = o.optString("name", ""),
    surname = o.optString("surname", ""),
    grade = o.optString("grade", ""),
    schoolName = o.optString("school_name", ""),
    pickupLocation = o.optString("pickup_location", ""),
    dropoffLocation = o.optString("dropoff_location", ""),
    assignmentStatus = o.optString("assignment_status", ""),
    driverName = o.strOrNull("driver_name"),
    driverSurname = o.strOrNull("driver_surname"),
    companyName = o.strOrNull("company_name"),
    profilePhoto = o.strOrNull("profile_photo"),
    ownerId = o.intOrNull("owner_id")
)

suspend fun fetchParentChildren(): List<ParentChild> {
    val (code, text) = get("/parent/children")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map { parseChild(arr.getJSONObject(it)) }
}

suspend fun fetchParentCompanies(): List<ParentCompany> {
    val (code, text) = get("/parent/companies")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        ParentCompany(o.optInt("owner_id"), o.optString("company_name"))
    }
}

suspend fun addParentChild(payload: JSONObject): Int? {
    val (code, text) = sendJson("/parent/children", "POST", payload)
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return o.optInt("child_id").takeIf { it > 0 }
        ?: o.optInt("id").takeIf { it > 0 }
}

suspend fun updateParentChild(childId: Int, payload: JSONObject): Boolean {
    val (code, _) = sendJson("/parent/children/$childId", "PUT", payload)
    return code in 200..299
}

// ---------------- PAYMENTS ----------------
suspend fun fetchParentPayments(): List<ParentPayment> {
    val (code, text) = get("/parent/payments")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        ParentPayment(
            paymentId = o.optInt("payment_id"),
            invoiceNumber = o.strOrNull("invoice_number"),
            childName = o.optString("child_name"),
            childSurname = o.optString("child_surname"),
            driverName = o.strOrNull("driver_name"),
            driverSurname = o.strOrNull("driver_surname"),
            amount = o.optDouble("amount"),
            description = o.strOrNull("description"),
            status = o.optString("status", "pending"),
            paymentDate = o.strOrNull("payment_date"),
            month = o.strOrNull("month")
        )
    }
}

suspend fun initiateParentPayment(paymentId: Int): Pair<String, JSONObject>? {
    val body = JSONObject().put("payment_id", paymentId)
    val (code, text) = sendJson("/parent/payments/initiate", "POST", body)
    if (code !in 200..299) return null
    val o = JSONObject(text)
    val actionUrl = o.optString("action_url")
    val paymentData = o.optJSONObject("payment_data") ?: return null
    return actionUrl to paymentData
}

// ---------------- TRIPS / ALERTS / ANNOUNCEMENTS ----------------
suspend fun fetchParentTrips(): List<ParentTrip> {
    val (code, text) = get("/parent/trips")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        ParentTrip(
            id = o.optInt("id"),
            route = o.strOrNull("route"),
            date = o.optString("date"),
            completedAt = o.strOrNull("completedAt"),
            child = o.strOrNull("child"),
            driver = o.strOrNull("driver"),
            distance = o.strOrNull("distance"),
            duration = o.strOrNull("duration"),
            pickupTime = o.strOrNull("pickup_time"),
            dropoffTime = o.strOrNull("dropoff_time"),
            status = o.optString("status", "Completed"),
            notes = o.strOrNull("notes")
        )
    }
}

suspend fun fetchParentTripAlerts(): List<ParentAlert> {
    val (code, text) = get("/parent/trip-alerts")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        ParentAlert(
            id = o.optInt("notification_id"),
            title = o.optString("title"),
            message = o.optString("message"),
            time = o.strOrNull("created_at")
        )
    }
}

suspend fun fetchParentAnnouncements(): List<ParentAnnouncement> {
    val (code, text) = get("/parent/announcements")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        ParentAnnouncement(
            id = o.optInt("id"),
            title = o.optString("title"),
            message = o.optString("message"),
            createdAt = o.optString("created_at")
        )
    }
}

data class SystemNotification(
    val id: Int, val title: String, val message: String, val createdAt: String
)

suspend fun fetchParentSystemNotifications(): List<SystemNotification> {
    val (code, text) = get("/parent/system-notifications")
    if (code !in 200..299) return emptyList()
    val arr = JSONArray(text)
    return (0 until arr.length()).map {
        val o = arr.getJSONObject(it)
        SystemNotification(
            id = o.optInt("id"),
            title = o.optString("title"),
            message = o.optString("message"),
            createdAt = o.optString("created_at")
        )
    }
}

// ---------------- TRACKING ----------------
suspend fun fetchChildLocation(childId: Int): ChildLocation? {
    val (code, text) = get("/parent/children/$childId/location")
    if (code !in 200..299) return null
    val o = JSONObject(text)
    return ChildLocation(
        hasDriver = o.optBoolean("has_driver"),
        isBroadcasting = o.optBoolean("is_broadcasting"),
        latitude = o.dblOrNull("latitude"),
        longitude = o.dblOrNull("longitude"),
        driverName = o.strOrNull("driver_name"),
        driverSurname = o.strOrNull("driver_surname")
    )
}

// ---------------- PHOTO UPLOAD ----------------
suspend fun uploadChildPhoto(childId: Int, uri: Uri, context: Context): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(uri, context) ?: return@withContext false
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                    "profile_photo", file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()
            val req = Request.Builder()
                .url("$BASE/parent/children/$childId/photo")
                .header("Authorization", authHeader())
                .post(body)
                .build()
            client.newCall(req).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            Log.e("ParentApi", "uploadChildPhoto failed", e)
            false
        }
    }

private fun uriToFile(uri: Uri, context: Context): File? = try {
    val input = context.contentResolver.openInputStream(uri) ?: return null
    val f = File(context.cacheDir, "parent_upload_${System.currentTimeMillis()}")
    FileOutputStream(f).use { input.copyTo(it) }
    f
} catch (e: Exception) { null }