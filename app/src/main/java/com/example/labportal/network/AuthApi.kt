package com.example.labportal.network

import com.example.labportal.data.model.AllowedEditsDto
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

data class GrantPermissionsDto(
    val face: Boolean,
    val academic: Boolean,
    val personal: Boolean
)

data class FieldDiffDto(
    val field: String,
    val oldValue: String? = null,
    val newValue: String? = null
)

data class EditHistoryRecordDto(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val rollNo: String = "",
    val timestamp: String = "",
    val changes: List<FieldDiffDto> = emptyList(),
    val type: String = "Profile Edit"
)

data class UserStatusResponse(
    val isBlocked: Boolean = false,
    val isApproved: Boolean = true,
    val status: String = "approved",
    val allowedEdits: AllowedEditsDto = AllowedEditsDto(),
    val initialEditDone: Boolean = false
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
    val profilePhotoUrl: String = "",
    val faceEnrolled: Boolean = true,
    val facePhotoUrl: String = "",
    val faceEmbedding: List<Float>? = null
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
    val profilePhotoUrl: String = "",
    val faceEnrolled: Boolean = true,
    val facePhotoUrl: String = "",
    val faceEmbedding: List<Float>? = null,
    val profileCompletion: Int = 80,
    val collegeName: String = "Maharishi Mahesh Yogi Ramayan University Ayodhya",
    val department: String = "Computer Science & Engineering",
    val academicYear: String = "2024 - 2028",
    val dob: String = "",
    val bloodGroup: String = "",
    val permanentAddress: String = "",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val isBlocked: Boolean = false,
    val status: String = "approved",
    val allowedEdits: AllowedEditsDto = AllowedEditsDto(),
    val initialEditDone: Boolean = false
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
    val profilePhotoUrl: String? = null,
    val facePhotoUrl: String? = null,
    val faceEmbedding: List<Float>? = null
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
    val enrollmentNo: String = "",
    val collegeName: String = "Maharishi Mahesh Yogi Ramayan University Ayodhya",
    val role: String,
    val programme: String = "B.Sc. (Computer Science)",
    val semester: String = "Semester V",
    val department: String = "Computer Science & Engineering",
    val phone: String = "+91 98765 43210",
    val profilePhotoUrl: String = "",
    val faceEnrolled: Boolean = true,
    val facePhotoUrl: String = "",
    val faceEmbedding: List<Float>? = null,
    @SerializedName("isApproved") val isApproved: Boolean = false,
    val status: String = "pending",
    val isBlocked: Boolean = false,
    val allowedEdits: AllowedEditsDto = AllowedEditsDto(),
    val initialEditDone: Boolean = false,
    val createdAt: String? = null
)

data class BiometricEnrollDto(
    val userId: String,
    val rollNo: String,
    val faceEmbedding: List<Float>,
    val facePhotoUrl: String? = null
)

data class DeepFaceEnrollDto(
    val userId: String,
    val rollNo: String,
    val imageBase64: String,
    val consentGiven: Boolean = true
)

data class DeepFaceEnrollResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null,
    val rollNo: String? = null,
    val dimensions: Int = 512,
    val model: String = "Facenet512"
)

data class DeepFaceAttendanceDto(
    val studentId: String,
    val rollNo: String,
    val subject: String = "Central Laboratory Practical",
    val imageBase64: String,
    val livenessData: Map<String, Any>? = null
)

data class DeepFaceAttendanceResponse(
    val success: Boolean,
    val message: String,
    val attendance: AttendanceRecordDto? = null
)

data class AttendanceVerifyDto(
    val studentId: String,
    val rollNo: String,
    val subject: String = "Central Laboratory Practical",
    val similarityScore: Float,
    val livenessScore: Float = 1.0f,
    val liveEmbedding: List<Float>? = null
)

data class AttendanceRecordDto(
    val id: String = "",
    val studentId: String = "",
    val rollNo: String = "",
    val studentName: String = "",
    val subject: String = "",
    val date: String = "",
    val timestamp: String = "",
    val status: String = "Present",
    val similarityScore: Float = 0.0f
)

data class AttendanceVerifyResponse(
    val success: Boolean,
    val message: String,
    val attendance: AttendanceRecordDto? = null
)

data class AttendanceHistoryResponse(
    val success: Boolean,
    val count: Int,
    val records: List<AttendanceRecordDto> = emptyList()
)

data class BiometricTemplateResponse(
    val success: Boolean,
    val userId: String,
    val rollNo: String,
    val fullName: String,
    val faceEnrolled: Boolean,
    val faceEmbedding: List<Float> = emptyList()
)

data class SimpleActionResponse(
    val message: String,
    val success: Boolean = true
)

data class CourseDto(
    val id: String,
    val name: String,
    val code: String,
    val semesters: List<String>,
    val facultyName: String = "Faculty of Engineering & Technology"
)

data class CourseCreateDto(
    val name: String,
    val code: String,
    val semesters: List<String>,
    val facultyName: String = "Faculty of Engineering & Technology"
)

data class AddCourseResponse(
    val message: String,
    val success: Boolean = true,
    val course: CourseDto? = null
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

    @GET("api/admin/all-students")
    suspend fun getAllApprovedStudents(): Response<List<StudentApprovalDto>>

    @GET("api/admin/rejected-students")
    suspend fun getRejectedStudents(): Response<List<StudentApprovalDto>>

    @POST("api/admin/approve-student/{id}")
    suspend fun approveStudent(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

    @POST("api/admin/reject-student/{id}")
    suspend fun rejectStudent(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

    @retrofit2.http.DELETE("api/admin/delete-student/{id}")
    suspend fun deleteStudentPermanently(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

    @POST("api/admin/student/{id}/block")
    suspend fun blockStudent(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

    @POST("api/admin/student/{id}/unblock")
    suspend fun unblockStudent(@retrofit2.http.Path("id") id: String): Response<SimpleActionResponse>

    @POST("api/admin/student/{id}/grant-permissions")
    suspend fun grantStudentPermissions(
        @retrofit2.http.Path("id") id: String,
        @Body body: GrantPermissionsDto
    ): Response<SimpleActionResponse>

    @GET("api/admin/student/{id}/edit-history")
    suspend fun getStudentEditHistory(@retrofit2.http.Path("id") id: String): Response<List<EditHistoryRecordDto>>

    @GET("api/auth/status/{id}")
    suspend fun getUserStatus(@retrofit2.http.Path("id") id: String): Response<UserStatusResponse>


    @GET("api/courses")
    suspend fun getCourses(): Response<List<CourseDto>>

    @retrofit2.http.PUT("api/auth/profile/{id}")
    suspend fun updateProfile(
        @retrofit2.http.Path("id") id: String,
        @Body body: ProfileUpdateRequest
    ): Response<AuthResponse>

    @POST("api/admin/courses")
    suspend fun addCourse(@Body body: CourseCreateDto): Response<AddCourseResponse>

    @retrofit2.http.DELETE("api/admin/courses/{id}")
    suspend fun deleteCourse(
        @retrofit2.http.Path("id") id: String,
        @retrofit2.http.Query("name") name: String? = null,
        @retrofit2.http.Query("code") code: String? = null
    ): Response<SimpleActionResponse>

    @POST("api/biometric/enroll")
    suspend fun enrollBiometricTemplate(@Body body: BiometricEnrollDto): Response<SimpleActionResponse>

    @POST("api/biometric/deepface/enroll")
    suspend fun enrollStudentDeepFace(@Body body: DeepFaceEnrollDto): Response<DeepFaceEnrollResponse>

    @GET("api/biometric/template/{userId}")
    suspend fun getBiometricTemplate(@retrofit2.http.Path("userId") userId: String): Response<BiometricTemplateResponse>

    @POST("api/biometric/verify-attendance")
    suspend fun verifyAndRecordAttendance(@Body body: AttendanceVerifyDto): Response<AttendanceVerifyResponse>

    @POST("api/biometric/deepface/verify-attendance")
    suspend fun verifyAttendanceDeepFace(@Body body: DeepFaceAttendanceDto): Response<DeepFaceAttendanceResponse>

    @GET("api/biometric/attendance/student/{studentId}")
    suspend fun getStudentAttendanceHistory(@retrofit2.http.Path("studentId") studentId: String): Response<AttendanceHistoryResponse>

    @POST("api/biometric/revoke/{userId}")
    suspend fun revokeBiometricEnrollment(@retrofit2.http.Path("userId") userId: String): Response<SimpleActionResponse>

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

