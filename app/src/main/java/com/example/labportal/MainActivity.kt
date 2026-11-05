package com.example.labportal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.labportal.data.model.UserData
import com.example.labportal.network.ApiConstants
import com.example.labportal.network.LoginRequest
import com.example.labportal.network.NetworkClient
import com.example.labportal.network.RegisterRequest
import com.example.labportal.ui.screens.DashboardScreen
import com.example.labportal.ui.screens.LoginScreen
import com.example.labportal.ui.screens.RegisterScreen
import com.example.labportal.ui.screens.RegistrationPendingScreen
import com.example.labportal.ui.theme.LabPortalTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class AppScreen {
    LOGIN,
    REGISTER,
    REGISTRATION_PENDING,
    DASHBOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiConstants.init(this)
        enableEdgeToEdge()
        setContent {
            LabPortalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    MainAppNavigation()
                }
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var loggedInUser by remember { mutableStateOf<UserData?>(null) }
    var pendingStudentData by remember { mutableStateOf<UserData?>(null) }

    // Loading & Error States
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var registerError by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        AppScreen.LOGIN -> {
            LoginScreen(
                isLoading = isLoading,
                errorMessage = loginError,
                onLoginClick = { email, password, _ ->
                    val cleanEmail = email.trim().lowercase()
                    val cleanPass = password.trim()

                    if (cleanEmail.isBlank() || cleanPass.isBlank()) {
                        loginError = "Please enter both email and password"
                        return@LoginScreen
                    }

                    loginError = null
                    isLoading = true

                    // 100% Real API Call to Backend & MongoDB Database
                    coroutineScope.launch {
                        try {
                            val response = NetworkClient.apiService.login(
                                LoginRequest(email = cleanEmail, password = cleanPass)
                            )

                            isLoading = false

                            if (response.isSuccessful && response.body() != null) {
                                val userDto = response.body()!!.user
                                loggedInUser = UserData(
                                    fullName = userDto.fullName,
                                    email = userDto.email,
                                    rollNo = userDto.rollNo,
                                    role = userDto.role,
                                    phone = userDto.phone,
                                    programme = userDto.programme,
                                    semester = userDto.semester,
                                    faceEnrolled = userDto.faceEnrolled,
                                    facePhotoUrl = userDto.facePhotoUrl,
                                    profileCompletion = userDto.profileCompletion
                                )
                                Toast.makeText(context, "Welcome ${userDto.fullName} (${userDto.role})", Toast.LENGTH_SHORT).show()
                                currentScreen = AppScreen.DASHBOARD
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val message = try {
                                    val json = JSONObject(errorBody ?: "")
                                    json.optString("detail", "Invalid email or password.")
                                } catch (e: Exception) {
                                    "Invalid credentials or user not found."
                                }
                                loginError = message
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            loginError = "Server connection failed!\nMake sure backend is running at:\n${ApiConstants.BASE_URL}\nError: ${e.localizedMessage ?: "Network error"}"
                        }
                    }
                },
                onGoogleSignInClick = {
                    Toast.makeText(context, "Google Sign-in selected", Toast.LENGTH_SHORT).show()
                },
                onForgotPasswordClick = {
                    Toast.makeText(context, "Contact lab administrator to reset password", Toast.LENGTH_LONG).show()
                },
                onNavigateToRegister = {
                    loginError = null
                    registerError = null
                    currentScreen = AppScreen.REGISTER
                }
            )
        }

        AppScreen.REGISTER -> {
            RegisterScreen(
                isLoading = isLoading,
                errorMessage = registerError,
                onRegisterClick = { name, email, rollNo, role, password, programme, semester, phone, faceEnrolled, facePhotoUrl ->
                    val cleanName = name.trim()
                    val cleanEmail = email.trim().lowercase()
                    val cleanRoll = rollNo.trim()
                    val cleanPass = password.trim()

                    if (cleanName.isBlank() || cleanEmail.isBlank() || cleanPass.isBlank()) {
                        registerError = "Please fill in all required fields"
                        return@RegisterScreen
                    }

                    registerError = null
                    isLoading = true

                    // 100% Real API Call to Register User in MongoDB
                    coroutineScope.launch {
                        try {
                            val response = NetworkClient.apiService.register(
                                RegisterRequest(
                                    fullName = cleanName,
                                    email = cleanEmail,
                                    rollNo = cleanRoll.ifBlank { "N/A" },
                                    role = role,
                                    password = cleanPass,
                                    programme = programme,
                                    semester = semester,
                                    phone = phone,
                                    faceEnrolled = faceEnrolled,
                                    facePhotoUrl = facePhotoUrl
                                )
                            )

                            isLoading = false

                            if (response.isSuccessful && response.body() != null) {
                                val userDto = response.body()!!.user
                                val isApproved = response.body()!!.isApproved

                                if (!isApproved && !userDto.role.equals("Admin", ignoreCase = true)) {
                                    pendingStudentData = UserData(
                                        fullName = userDto.fullName,
                                        email = userDto.email,
                                        rollNo = userDto.rollNo,
                                        role = userDto.role,
                                        programme = userDto.programme,
                                        semester = userDto.semester,
                                        phone = userDto.phone,
                                        faceEnrolled = userDto.faceEnrolled,
                                        facePhotoUrl = userDto.facePhotoUrl,
                                        profileCompletion = userDto.profileCompletion
                                    )
                                    currentScreen = AppScreen.REGISTRATION_PENDING
                                } else {
                                    loggedInUser = UserData(
                                        fullName = userDto.fullName,
                                        email = userDto.email,
                                        rollNo = userDto.rollNo,
                                        role = userDto.role,
                                        programme = userDto.programme,
                                        semester = userDto.semester,
                                        phone = userDto.phone,
                                        faceEnrolled = userDto.faceEnrolled,
                                        facePhotoUrl = userDto.facePhotoUrl,
                                        profileCompletion = userDto.profileCompletion
                                    )
                                    Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                    currentScreen = AppScreen.DASHBOARD
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val message = try {
                                    val json = JSONObject(errorBody ?: "")
                                    json.optString("detail", "Registration failed.")
                                } catch (e: Exception) {
                                    "Registration failed."
                                }
                                registerError = message
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            registerError = "Server connection failed!\nURL: ${ApiConstants.BASE_URL}\nError: ${e.localizedMessage}"
                        }
                    }
                },
                onNavigateToLogin = {
                    loginError = null
                    registerError = null
                    currentScreen = AppScreen.LOGIN
                }
            )
        }

        AppScreen.REGISTRATION_PENDING -> {
            RegistrationPendingScreen(
                fullName = pendingStudentData?.fullName ?: "Student",
                email = pendingStudentData?.email ?: "",
                rollNo = pendingStudentData?.rollNo ?: "N/A",
                role = pendingStudentData?.role ?: "Student",
                programme = pendingStudentData?.programme ?: "B.Sc. (Computer Science)",
                semester = pendingStudentData?.semester ?: "Semester V",
                phone = pendingStudentData?.phone ?: "+91 98765 43210",
                onBackToLogin = {
                    loginError = null
                    registerError = null
                    currentScreen = AppScreen.LOGIN
                }
            )
        }

        AppScreen.DASHBOARD -> {
            loggedInUser?.let { user ->
                DashboardScreen(
                    user = user,
                    onLogoutClick = {
                        loggedInUser = null
                        currentScreen = AppScreen.LOGIN
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            } ?: run {
                currentScreen = AppScreen.LOGIN
            }
        }
    }
}
