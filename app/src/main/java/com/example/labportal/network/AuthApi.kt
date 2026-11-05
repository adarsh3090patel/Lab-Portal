package com.example.labportal.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Request & Response Data Classes
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val rollNo: String,
    val role: String,
    val password: String,
    val programme: String = "B.Sc. (Computer Science)",
    val semester: String = "Semester V",
    val phone: String = "+91 98765 43210",
    val faceEnrolled: Boolean = true,
    val facePhotoUrl: String = ""
)

data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val rollNo: String,
    val role: String,
    val programme: String = "B.Sc. (Computer Science)",
    val semester: String = "Semester V",
    val phone: String = "+91 98765 43210",
    val faceEnrolled: Boolean = true,
    val facePhotoUrl: String = "",
    val profileCompletion: Int = 80
)

data class AuthResponse(
    val message: String,
    @SerializedName("isApproved") val isApproved: Boolean = true,
    val user: UserDto
)

data class StudentApprovalDto(
    val id: String,
    val fullName: String,
    val email: String,
    val rollNo: String,
    val role: String,
    val programme: String = "B.Sc. (Computer Science)",
    val semester: String = "Semester V",
    val phone: String = "+91 98765 43210",
    val faceEnrolled: Boolean = true,
    val facePhotoUrl: String = "",
    @SerializedName("isApproved") val isApproved: Boolean = false,
    val createdAt: String? = null
)

data class SimpleActionResponse(
    val message: String,
    val success: Boolean = true
)

data class HealthResponse(
    val status: String,
    val database: String
)

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @GET("api/admin/students")
    suspend fun getStudentsForApproval(): Response<List<StudentApprovalDto>>

    @POST("api/admin/approve-student/{id}")
    suspend fun approveStudent(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

    @POST("api/admin/reject-student/{id}")
    suspend fun rejectStudent(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

object NetworkClient {
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
        try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        } catch (_: Throwable) {}
        builder.build()
    }

    private var _apiService: AuthApiService? = null

    val apiService: AuthApiService
        get() {
            if (_apiService == null) {
                rebuildClient()
            }
            return _apiService!!
        }

    fun rebuildClient() {
        try {
            _apiService = Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApiService::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

