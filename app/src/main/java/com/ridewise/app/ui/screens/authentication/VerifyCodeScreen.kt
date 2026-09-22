package com.ridewise.app.ui.screens.authentication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ridewise.app.utils.Constants
import com.ridewise.app.utils.TempTokenHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ================================================================
// THEME — same palette as LoginScreen / RegisterScreen
// ================================================================
private object VerifyColors {
    val TopBar       = Color(0xFF0F1F38)
    val Accent       = Color(0xFF2F6FED)
    val Background   = Color(0xFFF6F8FB)
    val TextPrimary  = Color(0xFF16202E)
    val TextMuted    = Color(0xFF7C889C)
    val Border       = Color(0xFFCDD6E3)

    val Success      = Color(0xFF1C9D63)
    val SuccessDim   = Color(0xFFE7F7EF)
    val Danger       = Color(0xFFD13A4C)
    val DangerDim    = Color(0xFFFBE9EB)
}

@Composable
fun VerifyCodeScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyColors.Background)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verification Code",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = VerifyColors.TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Enter the 6-digit code sent to your email",
            fontSize = 14.sp,
            color = VerifyColors.TextMuted,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // ---- Message banner ----
        message?.let { (type, text) ->
            val isSuccess = type == "success"
            Text(
                text = text,
                color = if (isSuccess) VerifyColors.Success else VerifyColors.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSuccess) VerifyColors.SuccessDim else VerifyColors.DangerDim,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter { ch -> ch.isDigit() }.take(6) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            label = { Text("Enter Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerifyColors.Accent,
                unfocusedBorderColor = VerifyColors.Border,
                focusedLabelColor = VerifyColors.Accent,
                unfocusedLabelColor = VerifyColors.TextMuted,
                focusedTextColor = VerifyColors.TextPrimary,
                unfocusedTextColor = VerifyColors.TextPrimary,
                cursorColor = VerifyColors.Accent
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (code.length != 6) {
                    message = "error" to "Please enter a valid 6-digit code"
                    return@Button
                }
                message = null
                isLoading = true

                val payload = JSONObject().apply { put("code", code) }

                scope.launch {
                    val result = verifyCode(payload.toString())
                    isLoading = false
                    if (result.first) {
                        val finalToken = result.second

                        // Store final token — used by ALL roles including owner
                        TempTokenHolder.finalToken = finalToken

                        // Route by role. TempTokenHolder.userRole was set in LoginScreen.
                        val route = when (TempTokenHolder.userRole) {
                            "owner"  -> "owner_dashboard"
                            "driver" -> "driver_dashboard"
                            "admin"  -> "admin_dashboard"
                            "parent" -> "parent_dashboard"
                            else     -> "parent_dashboard"
                        }

                        navController.navigate(route) {
                            // Wipe the whole auth flow so back button doesn't return here
                            popUpTo("login") { inclusive = true }
                            popUpTo("verify") { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        message = "error" to result.second
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerifyColors.TopBar),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = if (isLoading) "VERIFYING..." else "VERIFY",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("login") }) {
            Text("← Back to Login", color = VerifyColors.TextMuted)
        }
    }
}

// ================================================================
// API call — unchanged
// ================================================================
suspend fun verifyCode(jsonPayload: String): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        try {
            val tempToken = TempTokenHolder.tempToken
            if (tempToken.isBlank()) {
                return@withContext Pair(false, "No temporary token found. Please login again.")
            }

            val url = URL("${Constants.BASE_URL}/api/auth/verify-2fa")
            println("🔍 Verify URL: $url")
            println("🔍 Temp Token: $tempToken")
            println("🔍 Payload: $jsonPayload")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $tempToken")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            connection.disconnect()

            println("🔍 Verify Response Code: $responseCode")
            println("🔍 Verify Response Body: $response")

            if (responseCode in 200..299) {
                val json = JSONObject(response)
                val token = json.getString("token")

                // Optional: re-read role from verify response if the backend sends it.
                // If it does, this keeps TempTokenHolder.userRole in sync.
                try {
                    val userObj = json.optJSONObject("user")
                    val role = userObj?.optString("role")
                    if (!role.isNullOrBlank()) {
                        TempTokenHolder.userRole = role
                        println("🔍 Role refreshed from verify response: $role")
                    }
                } catch (_: Exception) { /* keep the role from login */ }

                Pair(true, token)
            } else {
                try {
                    val json = JSONObject(response)
                    val error = json.optString("error", json.optString("message", "Verification failed"))
                    Pair(false, error)
                } catch (e: Exception) {
                    val truncated = if (response.length > 200) response.take(200) + "..." else response
                    Pair(false, "Server error: $truncated")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Cannot connect to server: ${e.message}")
        }
    }
}