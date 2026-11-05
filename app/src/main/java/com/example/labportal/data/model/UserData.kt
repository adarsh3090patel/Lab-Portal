package com.example.labportal.data.model

data class UserData(
    val fullName: String,
    val email: String,
    val rollNo: String,
    val role: String,
    val phone: String = "+91 98765 43210",
    val department: String = "Computer Science & Engineering",
    val programme: String = "B.Sc. (Computer Science)",
    val semester: String = "Semester V",
    val batch: String = "Batch A-1 (Lab Group 2)",
    val academicYear: String = "2024 - 2028",
    val faceEnrolled: Boolean = true,
    val facePhotoUrl: String = "",
    val profileCompletion: Int = 80
)
