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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
// THEME — same palette as RegisterScreen & DriverDashboardScreen
// ================================================================
private object LoginColors {
    val TopBar       = Color(0xFF0F1F38)
    val Accent       = Color(0xFF2F6FED)
    val AccentTint   = Color(0xFFE8EEFD)
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
fun LoginScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<Pair<String, String>?>(null) } // "success" | "error" to text

    fun cleanEmail(input: String): String {
        return input.trim().replace(Regex("@.*@"), "@")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginColors.Background)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "RideWise",
            color = LoginColors.TopBar,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Welcome Back",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = LoginColors.TextPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // ---- Message banner ----
        message?.let { (type, text) ->
            val isSuccess = type == "success"
            Text(
                text = text,
                color = if (isSuccess) LoginColors.Success else LoginColors.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSuccess) LoginColors.SuccessDim else LoginColors.DangerDim,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email Address") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                autoCorrectEnabled = false
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoginColors.Accent,
                unfocusedBorderColor = LoginColors.Border,
                focusedLabelColor = LoginColors.Accent,
                unfocusedLabelColor = LoginColors.TextMuted,
                focusedTextColor = LoginColors.TextPrimary,
                unfocusedTextColor = LoginColors.TextPrimary,
                cursorColor = LoginColors.Accent
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoginColors.Accent,
                unfocusedBorderColor = LoginColors.Border,
                focusedLabelColor = LoginColors.Accent,
                unfocusedLabelColor = LoginColors.TextMuted,
                focusedTextColor = LoginColors.TextPrimary,
                unfocusedTextColor = LoginColors.TextPrimary,
                cursorColor = LoginColors.Accent
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    message = "error" to "Please enter both email and password"
                    return@Button
                }
                message = null
                isLoading = true

                val cleanedEmail = cleanEmail(email)
                val payload = JSONObject().apply {
                    put("email", cleanedEmail)
                    put("password", password)
                }

                scope.launch {
                    val result = loginUser(payload.toString())
                    isLoading = false

                    if (!result.success) {
                        message = "error" to (result.error ?: "Login failed")
                        return@launch
                    }

                    if (result.requiresVerification) {
                        // First-ever login — go through the OTP screen
                        TempTokenHolder.tempToken = result.token ?: ""
                        navController.navigate("verify")
                    } else {
                        // Returning user — skip OTP, go straight to the correct dashboard
                        TempTokenHolder.finalToken = result.token ?: ""
                        val route = when (result.role) {
                            "owner"  -> "owner_dashboard"
                            "driver" -> "driver_dashboard"
                            "admin"  -> "admin_dashboard"
                            else     -> "parent_dashboard"
                        }
                        navController.navigate(route) {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LoginColors.TopBar),
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
                text = if (isLoading) "LOGGING IN..." else "LOGIN",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account? ", color = LoginColors.TextMuted)
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Register here", color = LoginColors.Accent, fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = { navController.navigate("welcome") }) {
            Text("← Back to Home", color = LoginColors.TextMuted)
        }
    }
}

// ================================================================
// API call — returns LoginResult with routing info
// ================================================================
data class LoginResult(
    val success: Boolean,
    val token: String?,                    // temp token if verification needed, final token otherwise
    val requiresVerification: Boolean,
    val role: String?,
    val error: String? = null
)

suspend fun loginUser(jsonPayload: String): LoginResult {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("${Constants.BASE_URL}/api/auth/login")
            println("🔍 URL: $url")
            println("🔍 Payload: $jsonPayload")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
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

            println("🔍 Response Code: $responseCode")
            println("🔍 Response Body: $response")

            if (responseCode in 200..299) {
                val json = JSONObject(response)

                // Read the role from the response
                var role = "parent"
                try {
                    val userObj = json.getJSONObject("user")
                    role = userObj.optString("role", "parent")
                } catch (_: Exception) {
                    role = json.optString("role", "parent")
                }
                TempTokenHolder.userRole = role
                println("🔍 User role: $role")

                val requiresVerification = json.optBoolean("requires_verification", false)

                if (requiresVerification) {
                    // OTP flow — use temp_token
                    val tempToken = json.getString("temp_token")
                    LoginResult(
                        success = true,
                        token = tempToken,
                        requiresVerification = true,
                        role = role
                    )
                } else {
                    // Returning user — final token, no verification needed
                    val finalToken = json.getString("token")
                    LoginResult(
                        success = true,
                        token = finalToken,
                        requiresVerification = false,
                        role = role
                    )
                }
            } else {
                val error = try {
                    JSONObject(response).optString("error",
                        JSONObject(response).optString("message", "Login failed"))
                } catch (_: Exception) {
                    "Server error: ${response.take(200)}"
                }
                LoginResult(success = false, token = null, requiresVerification = false, role = null, error = error)
            }
        } catch (e: Exception) {
            LoginResult(success = false, token = null, requiresVerification = false, role = null,
                error = "Cannot connect to server: ${e.message}")
        }
    }
}