package com.ridewise.app.ui.screens.authentication

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ridewise.app.R
import com.ridewise.app.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

// ================================================================
// THEME — matches DriverDashboardScreen / ParentDashboardScreen
// ================================================================
private object RideWiseColors {
    val TopBar       = Color(0xFF0F1F38)   // dark navy (same as driver topbar)
    val Accent       = Color(0xFF2F6FED)   // primary blue accent
    val AccentTint   = Color(0xFFE8EEFD)   // light blue tint
    val Background   = Color(0xFFF6F8FB)   // page background
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextPrimary  = Color(0xFF16202E)
    val TextMuted    = Color(0xFF7C889C)
    val Border       = Color(0xFFCDD6E3)
    val ChipBg       = Color(0xFFEEF1F6)

    val Success      = Color(0xFF1C9D63)
    val SuccessDim   = Color(0xFFE7F7EF)
    val Warn         = Color(0xFFC17A1F)
    val WarnDim      = Color(0xFFFBF1E2)
    val Danger       = Color(0xFFD13A4C)
    val DangerDim    = Color(0xFFFBE9EB)

    // Warm hint used ONLY for the owner invite-code card
    val OwnerAccent    = Color(0xFFB45309)  // deep amber — harmonises with navy/blue
    val OwnerAccentDim = Color(0xFFFDF3E2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedRole by remember { mutableStateOf("parent") }
    var expanded by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var consentChecked by remember { mutableStateOf(false) }

    var inviteCode by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }

    var companyName by remember { mutableStateOf("") }
    var businessRegNumber by remember { mutableStateOf("") }
    var operatingLicenseNumber by remember { mutableStateOf("") }
    var contactAddress by remember { mutableStateOf("") }

    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var licenseFileUri by remember { mutableStateOf<Uri?>(null) }
    var pdpFileUri by remember { mutableStateOf<Uri?>(null) }
    var businessLicenseUri by remember { mutableStateOf<Uri?>(null) }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> profilePhotoUri = uri }

    val licenseFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> licenseFileUri = uri }

    val pdpFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> pdpFileUri = uri }

    val businessLicensePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> businessLicenseUri = uri }

    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var generatedInviteCode by remember { mutableStateOf("") }

    fun passwordStrength(pwd: String): Pair<String, Color> {
        return when {
            pwd.length < 6 -> "Weak" to RideWiseColors.Danger
            pwd.length < 10 ||
                    !pwd.any { it.isUpperCase() } ||
                    !pwd.any { it.isDigit() } -> "Medium" to RideWiseColors.Warn
            else -> "Strong" to RideWiseColors.Success
        }
    }

    fun submitRegistration() {
        scope.launch {
            isLoading = true
            message = ""
            isError = false

            if (name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank()) {
                message = "Please fill in all required fields."
                isError = true; isLoading = false; return@launch
            }
            if (!consentChecked) {
                message = "You must accept the Privacy Policy and Terms of Service."
                isError = true; isLoading = false; return@launch
            }
            if (password != confirmPassword) {
                message = "Passwords do not match."
                isError = true; isLoading = false; return@launch
            }
            if (password.length < 6) {
                message = "Password must be at least 6 characters."
                isError = true; isLoading = false; return@launch
            }

            when (selectedRole) {
                "driver" -> {
                    if (inviteCode.isBlank()) {
                        message = "Please enter the company invite code."
                        isError = true; isLoading = false; return@launch
                    }
                    if (licenseNumber.isBlank()) {
                        message = "Please enter your driver's license number."
                        isError = true; isLoading = false; return@launch
                    }
                    if (licenseFileUri == null) {
                        message = "Please upload your driver's license document."
                        isError = true; isLoading = false; return@launch
                    }
                    if (address.isBlank()) {
                        message = "Address is required for drivers."
                        isError = true; isLoading = false; return@launch
                    }
                }
                "owner" -> {
                    if (companyName.isBlank()) {
                        message = "Please provide your company name."
                        isError = true; isLoading = false; return@launch
                    }
                    if (businessLicenseUri == null) {
                        message = "Please upload your business license."
                        isError = true; isLoading = false; return@launch
                    }
                }
            }

            val result = if (selectedRole == "owner") {
                registerOwner(
                    email = email,
                    password = password,
                    phone = phone,
                    companyName = companyName,
                    businessRegNumber = businessRegNumber,
                    operatingLicenseNumber = operatingLicenseNumber,
                    contactAddress = contactAddress,
                    name = name,
                    surname = surname,
                    businessLicenseUri = businessLicenseUri,
                    context = context
                )
            } else {
                registerParentOrDriver(
                    name = name,
                    surname = surname,
                    email = email,
                    password = password,
                    role = selectedRole,
                    phone = phone,
                    address = address,
                    inviteCode = if (selectedRole == "driver") inviteCode else null,
                    licenseNumber = if (selectedRole == "driver") licenseNumber else null,
                    profilePhotoUri = if (selectedRole == "driver") profilePhotoUri else null,
                    licenseFileUri = if (selectedRole == "driver") licenseFileUri else null,
                    pdpFileUri = if (selectedRole == "driver") pdpFileUri else null,
                    context = context
                )
            }

            isLoading = false

            if (result.success) {
                if (selectedRole == "owner") {
                    generatedInviteCode = result.inviteCode ?: "RW-XXXXX"
                    showSuccess = true
                } else {
                    Toast.makeText(
                        context,
                        "Registration successful!",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            } else {
                message = result.error ?: "Registration failed. Please try again."
                isError = true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = RideWiseColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_ridewise),
                contentDescription = "RideWise Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = RideWiseColors.TextPrimary
            )
            Text(
                text = "Join RideWise — select your role below",
                fontSize = 13.sp,
                color = RideWiseColors.TextMuted,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Message banner
            if (message.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isError) RideWiseColors.DangerDim else RideWiseColors.SuccessDim,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = if (isError) RideWiseColors.Danger else RideWiseColors.Success,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Role dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = when (selectedRole) {
                        "parent" -> "Parent / Guardian"
                        "driver" -> "Driver / Operator"
                        "owner" -> "Transport Owner"
                        else -> ""
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("I am registering as") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RideWiseColors.Accent,
                        unfocusedBorderColor = RideWiseColors.Border,
                        focusedTextColor = RideWiseColors.TextPrimary,
                        unfocusedTextColor = RideWiseColors.TextPrimary,
                        focusedLabelColor = RideWiseColors.Accent,
                        unfocusedLabelColor = RideWiseColors.TextMuted
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Parent / Guardian") },
                        onClick = { selectedRole = "parent"; expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Driver / Operator") },
                        onClick = { selectedRole = "driver"; expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Transport Owner") },
                        onClick = { selectedRole = "owner"; expanded = false }
                    )
                }
            }

            // ---- Common fields ----
            RwField(name, { name = it }, "First Name *")
            RwField(surname, { surname = it }, "Surname *")
            RwField(
                email, { email = it }, "Email *",
                keyboardType = KeyboardType.Email
            )
            RwField(
                phone, { phone = it }, "Phone",
                keyboardType = KeyboardType.Phone
            )
            RwField(
                address, { address = it },
                if (selectedRole == "driver") "Address *" else "Address"
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    if (password.isNotEmpty()) {
                        val (strength, color) = passwordStrength(password)
                        Text(
                            text = strength,
                            fontSize = 10.sp,
                            color = color,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                },
                colors = rwFieldColors()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = rwFieldColors()
            )

            // ---- Driver fields ----
            if (selectedRole == "driver") {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Driver Information")
                RwField(inviteCode, { inviteCode = it.uppercase() }, "Company Invite Code *")
                RwField(licenseNumber, { licenseNumber = it }, "License Number *")
                Spacer(modifier = Modifier.height(8.dp))
                FilePickerRow(
                    label = "Profile Photo",
                    uri = profilePhotoUri,
                    onPick = { profilePhotoPicker.launch("image/*") },
                    onClear = { profilePhotoUri = null }
                )
                FilePickerRow(
                    label = "Driver's License *",
                    uri = licenseFileUri,
                    onPick = { licenseFilePicker.launch("application/pdf,image/*") },
                    onClear = { licenseFileUri = null }
                )
                FilePickerRow(
                    label = "PDP Document",
                    uri = pdpFileUri,
                    onPick = { pdpFilePicker.launch("application/pdf,image/*") },
                    onClear = { pdpFileUri = null }
                )
                Text(
                    text = "Your vehicle will be assigned by your transport company.",
                    fontSize = 11.sp,
                    color = RideWiseColors.TextMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // ---- Owner fields ----
            if (selectedRole == "owner") {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Company Details")
                RwField(companyName, { companyName = it }, "Company Name *")
                RwField(businessRegNumber, { businessRegNumber = it }, "Business Registration Number")
                RwField(operatingLicenseNumber, { operatingLicenseNumber = it }, "Operating License Number")
                OutlinedTextField(
                    value = contactAddress,
                    onValueChange = { contactAddress = it },
                    label = { Text("Business Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = rwFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilePickerRow(
                    label = "Business License *",
                    uri = businessLicenseUri,
                    onPick = { businessLicensePicker.launch("application/pdf,image/*") },
                    onClear = { businessLicenseUri = null }
                )
                Text(
                    text = "RideWise admin reviews this before approving your account.",
                    fontSize = 11.sp,
                    color = RideWiseColors.TextMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Consent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = consentChecked,
                    onCheckedChange = { consentChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = RideWiseColors.Accent,
                        uncheckedColor = RideWiseColors.Border,
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = "I agree to the Privacy Policy and Terms of Service",
                    fontSize = 12.sp,
                    color = RideWiseColors.TextPrimary,
                    modifier = Modifier.clickable { consentChecked = !consentChecked }
                )
            }

            Button(
                onClick = { submitRegistration() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RideWiseColors.TopBar),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("CREATE ACCOUNT", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { navController.navigate("login") }) {
                Text(
                    "Already have an account? Sign in",
                    color = RideWiseColors.Accent,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showSuccess) {
                OwnerSuccessView(
                    inviteCode = generatedInviteCode,
                    onLogin = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// ================================================================
// Shared helpers — themed
// ================================================================

@Composable
private fun rwFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RideWiseColors.Accent,
    unfocusedBorderColor = RideWiseColors.Border,
    focusedTextColor = RideWiseColors.TextPrimary,
    unfocusedTextColor = RideWiseColors.TextPrimary,
    focusedLabelColor = RideWiseColors.Accent,
    unfocusedLabelColor = RideWiseColors.TextMuted,
    cursorColor = RideWiseColors.Accent
)

@Composable
private fun RwField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = rwFieldColors()
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = RideWiseColors.TextPrimary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FilePickerRow(
    label: String,
    uri: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = RideWiseColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (uri != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RideWiseColors.Success,
                    modifier = Modifier.size(16.dp).padding(end = 2.dp)
                )
                TextButton(onClick = onClear) {
                    Text("Clear", color = RideWiseColors.Danger)
                }
            }
        } else {
            TextButton(onClick = onPick) {
                Text("Choose File", color = RideWiseColors.Accent)
            }
        }
    }
}

@Composable
fun OwnerSuccessView(inviteCode: String, onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = RideWiseColors.Success,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Account created!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RideWiseColors.TextPrimary
        )
        Text(
            text = "Your application is pending review by RideWise admin.",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = RideWiseColors.TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Invite code card — subtle warm accent to distinguish owner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RideWiseColors.OwnerAccentDim,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RideWiseColors.OwnerAccent.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = inviteCode,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RideWiseColors.OwnerAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your driver invite code — share this with drivers",
                    fontSize = 11.sp,
                    color = RideWiseColors.OwnerAccent.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RideWiseColors.TopBar),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue to Login", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ================================================================
// API Helpers — unchanged
// ================================================================

private data class RegistrationResult(
    val success: Boolean,
    val error: String? = null,
    val inviteCode: String? = null
)

private suspend fun registerParentOrDriver(
    name: String,
    surname: String,
    email: String,
    password: String,
    role: String,
    phone: String,
    address: String,
    inviteCode: String?,
    licenseNumber: String?,
    profilePhotoUri: Uri?,
    licenseFileUri: Uri?,
    pdpFileUri: Uri?,
    context: Context
): RegistrationResult = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        multipartBuilder.addFormDataPart("name", name)
        multipartBuilder.addFormDataPart("surname", surname)
        multipartBuilder.addFormDataPart("email", email)
        multipartBuilder.addFormDataPart("password", password)
        multipartBuilder.addFormDataPart("role", role)
        multipartBuilder.addFormDataPart("phone", phone)
        multipartBuilder.addFormDataPart("address", address)

        if (role == "driver") {
            inviteCode?.let { multipartBuilder.addFormDataPart("invite_code", it) }
            licenseNumber?.let { multipartBuilder.addFormDataPart("license_number", it) }

            profilePhotoUri?.let { uri ->
                uriToFile(uri, context)?.let {
                    multipartBuilder.addFormDataPart(
                        "profile_photo", it.name,
                        it.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }
            }
            licenseFileUri?.let { uri ->
                uriToFile(uri, context)?.let {
                    multipartBuilder.addFormDataPart(
                        "license_file", it.name,
                        it.asRequestBody("application/pdf".toMediaTypeOrNull())
                    )
                }
            }
            pdpFileUri?.let { uri ->
                uriToFile(uri, context)?.let {
                    multipartBuilder.addFormDataPart(
                        "pdp_file", it.name,
                        it.asRequestBody("application/pdf".toMediaTypeOrNull())
                    )
                }
            }
        }

        val request = Request.Builder()
            .url("${Constants.BASE_URL}/api/auth/register")
            .post(multipartBuilder.build())
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        val json = JSONObject(responseBody)

        if (response.isSuccessful) RegistrationResult(success = true)
        else RegistrationResult(
            success = false,
            error = json.optString("error", "Registration failed")
        )
    } catch (e: Exception) {
        Log.e("Register", "Error in parent/driver registration", e)
        RegistrationResult(success = false, error = e.message ?: "Network error")
    }
}

private suspend fun registerOwner(
    email: String,
    password: String,
    phone: String,
    companyName: String,
    businessRegNumber: String,
    operatingLicenseNumber: String,
    contactAddress: String,
    name: String,
    surname: String,
    businessLicenseUri: Uri?,
    context: Context
): RegistrationResult = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        multipartBuilder.addFormDataPart("email", email)
        multipartBuilder.addFormDataPart("password", password)
        multipartBuilder.addFormDataPart("phone", phone)
        multipartBuilder.addFormDataPart("company_name", companyName)
        multipartBuilder.addFormDataPart("business_registration_number", businessRegNumber)
        multipartBuilder.addFormDataPart("operating_license_number", operatingLicenseNumber)
        multipartBuilder.addFormDataPart("contact_address", contactAddress)
        multipartBuilder.addFormDataPart("name", name)
        multipartBuilder.addFormDataPart("surname", surname)

        businessLicenseUri?.let { uri ->
            uriToFile(uri, context)?.let {
                multipartBuilder.addFormDataPart(
                    "business_license", it.name,
                    it.asRequestBody("application/pdf".toMediaTypeOrNull())
                )
            }
        }

        val request = Request.Builder()
            .url("${Constants.BASE_URL}/api/owner/register")
            .post(multipartBuilder.build())
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        val json = JSONObject(responseBody)

        if (response.isSuccessful) {
            RegistrationResult(
                success = true,
                inviteCode = if (json.isNull("invite_code")) null
                else json.optString("invite_code")
            )
        } else {
            RegistrationResult(
                success = false,
                error = json.optString("error", "Owner registration failed")
            )
        }
    } catch (e: Exception) {
        Log.e("Register", "Error in owner registration", e)
        RegistrationResult(success = false, error = e.message ?: "Network error")
    }
}

private fun uriToFile(uri: Uri, context: Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
        FileOutputStream(file).use { output -> inputStream.copyTo(output) }
        file
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(navController = rememberNavController())
    }
}