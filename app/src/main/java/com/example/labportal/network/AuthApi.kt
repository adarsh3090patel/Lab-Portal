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
    val profileCompletion: Int = 80,
    val collegeName: String = "Maharishi Mahesh Yogi Ramayan University Ayodhya",
    val department: String = "Computer Science & Engineering",
    val academicYear: String = "2024 - 2028",
    val dob: String = "",
    val bloodGroup: String = "",
    val permanentAddress: String = "",
    val guardianName: String = "",
    val guardianPhone: String = ""
)

data class ProfileUpdateRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val department: String? = null,
    val programme: String? = null,
    val semester: String? = null,
    val academicYear: String? = null,
    val collegeName: String? = null,
    val dob: String? = null,
    val bloodGroup: String? = null,
    val permanentAddress: String? = null,
    val guardianName: String? = null,
    val guardianPhone: String? = null,
    val facePhotoUrl: String? = null
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

data class CourseDto(
    val id: String,
    val name: String,
    val code: String,
    val semesters: List<String>
)

data class CourseCreateDto(
    val name: String,
    val code: String,
    val semesters: List<String>
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

    @GET("api/courses")
    suspend fun getCourses(): Response<List<CourseDto>>

    @retrofit2.http.PUT("api/auth/profile/{id}")
    suspend fun updateProfile(
        @retrofit2.http.Path("id") id: String,
        @Body body: ProfileUpdateRequest
    ): Response<AuthResponse>

    @POST("api/admin/courses")
    suspend fun addCourse(@Body body: CourseCreateDto): Response<SimpleActionResponse>

    @retrofit2.http.DELETE("api/admin/courses/{id}")
    suspend fun deleteCourse(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

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

