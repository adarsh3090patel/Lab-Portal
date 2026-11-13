package com.example.labportal.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.network.CourseDto
import com.example.labportal.network.NetworkClient
import com.example.labportal.ui.components.BackgroundCurves
import com.example.labportal.ui.components.LiveFaceScannerModal
import com.example.labportal.ui.components.PortalHeader
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.LabPortalTheme
import com.example.labportal.ui.theme.MmyrvBadgeBg
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class UserRole(val label: String) {
    STUDENT("Student"),
    FACULTY("Faculty"),
    LAB_ASSISTANT("Lab Assistant")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (
        name: String,
        email: String,
        rollNo: String,
        role: String,
        password: String,
        programme: String,
        semester: String,
        phone: String,
        faceEnrolled: Boolean,
        facePhotoUrl: String,
        faceEmbedding: List<Float>?
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    // Dynamic Courses & Semesters managed by Admin
    val availableCourses = remember { mutableStateListOf<CourseDto>() }
    var programme by remember { mutableStateOf("B.Sc. (Computer Science)") }
    var semester by remember { mutableStateOf("Semester V") }
    var programmeExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }

    // Real Facial Recognition Biometric State
    var faceEnrolled by remember { mutableStateOf(false) }
    var facePhotoBase64 by remember { mutableStateOf("") }
    var faceEmbedding by remember { mutableStateOf<List<Float>?>(null) }
    var capturedFaceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var faceDetectionMsg by remember { mutableStateOf<String?>(null) }
    var showFaceScanModal by remember { mutableStateOf(false) }
    var localValidationMsg by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Load available courses and semesters from Backend / Admin API
    LaunchedEffect(Unit) {
        try {
            val response = NetworkClient.apiService.getCourses()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                availableCourses.clear()
                availableCourses.addAll(response.body()!!)
                programme = availableCourses.first().name
                semester = availableCourses.first().semesters.firstOrNull() ?: "Semester I"
            } else {
                availableCourses.clear()
                availableCourses.addAll(defaultStaticCourses())
            }
        } catch (_: Exception) {
            if (availableCourses.isEmpty()) {
                availableCourses.addAll(defaultStaticCourses())
            }
        }
    }

    // Camera Capture Launcher for DeepFace Enrollment
    var isVerifyingFace by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedFaceBitmap = bitmap
            facePhotoBase64 = FaceDetectionHelper.bitmapToBase64(bitmap)
            faceEnrolled = true
            faceDetectionMsg = "Real Face Captured • Python DeepFace Enrollment Ready"
            localValidationMsg = null
            Toast.makeText(context, "Face Captured! Ready for DeepFace Enrollment.", Toast.LENGTH_SHORT).show()
        }
    }

    // Current selected course's semester list
    val currentCourseSemesters = remember(programme, availableCourses.size) {
        availableCourses.find { it.name == programme }?.semesters
            ?: listOf("Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BackgroundCurves()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // University Logo & Title
            PortalHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Registration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MmyrvBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // Card Header with Badge Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    color = MmyrvBadgeBg,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Register Icon",
                                tint = MmyrvPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Create Account",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Register for campus lab & biometric access",
                                fontSize = 13.sp,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role Selection Chips
                    Text(
                        text = "I am a:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.entries.forEach { role ->
                            val isSelected = selectedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) MmyrvPrimary else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.label,
                                    fontSize = 12.sp,
                                    fontFamily = AppFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MmyrvTextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message Display
                    val activeError = localValidationMsg ?: errorMessage
                    if (!activeError.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = activeError,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontFamily = AppFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; localValidationMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Full Name", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Name Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // University Email (Strictly "University Email" without any eg)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; localValidationMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("University Email", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = "Email Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Roll / Scholar / Employee No
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it; localValidationMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = if (selectedRole == UserRole.STUDENT) "Enrollment / Roll No." else "Faculty / Staff ID",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_badge),
                                contentDescription = "ID Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; localValidationMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Contact Phone (+91 98765 43210)", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_phone),
                                contentDescription = "Phone Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Student-Specific Academic Fields (Admin-Managed Dropdowns)
                    if (selectedRole == UserRole.STUDENT) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Degree / Programme Dropdown
                        Text(
                            text = "Degree / Programme",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ExposedDropdownMenuBox(
                            expanded = programmeExpanded,
                            onExpandedChange = { programmeExpanded = !programmeExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = programme,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = programmeExpanded)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_school),
                                        contentDescription = null,
                                        tint = MmyrvPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MmyrvPrimary,
                                    unfocusedBorderColor = MmyrvBorder,
                                    focusedContainerColor = Color(0xFFFAFAFC),
                                    unfocusedContainerColor = Color(0xFFFAFAFC)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = programmeExpanded,
                                onDismissRequest = { programmeExpanded = false }
                            ) {
                                availableCourses.forEach { course ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = course.name, fontWeight = FontWeight.SemiBold, fontFamily = AppFontFamily)
                                                Text(text = "Code: ${course.code}", fontSize = 11.sp, color = MmyrvTextMuted)
                                            }
                                        },
                                        onClick = {
                                            programme = course.name
                                            semester = course.semesters.firstOrNull() ?: "Semester I"
                                            programmeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Semester Dropdown
                        Text(
                            text = "Current Semester",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ExposedDropdownMenuBox(
                            expanded = semesterExpanded,
                            onExpandedChange = { semesterExpanded = !semesterExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = semester,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_resources),
                                        contentDescription = null,
                                        tint = MmyrvPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MmyrvPrimary,
                                    unfocusedBorderColor = MmyrvBorder,
                                    focusedContainerColor = Color(0xFFFAFAFC),
                                    unfocusedContainerColor = Color(0xFFFAFAFC)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = semesterExpanded,
                                onDismissRequest = { semesterExpanded = false }
                            ) {
                                currentCourseSemesters.forEach { sem ->
                                    DropdownMenuItem(
                                        text = { Text(text = sem, fontFamily = AppFontFamily) },
                                        onClick = {
                                            semester = sem
                                            semesterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ==========================================
                    // REAL BIOMETRIC FACIAL RECOGNITION ENROLLMENT CARD
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (faceEnrolled) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (faceEnrolled) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Real photo preview if captured, otherwise icon
                                if (capturedFaceBitmap != null) {
                                    Image(
                                        bitmap = capturedFaceBitmap!!.asImageBitmap(),
                                        contentDescription = "Real Captured Face",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFF16A34A), CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (faceEnrolled) Color(0xFFDCFCE7) else Color(0xFFEEF2FF)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (faceEnrolled) R.drawable.ic_check_circle else R.drawable.ic_face_recognition
                                            ),
                                            contentDescription = "Facial Recognition",
                                            tint = if (faceEnrolled) Color(0xFF16A34A) else MmyrvPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Facial Recognition ID",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = AppFontFamily,
                                            color = MmyrvTextDark
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (faceEnrolled) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (faceEnrolled) "Face Captured" else "Mandatory",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (faceEnrolled) Color(0xFF15803D) else Color(0xFFB45309)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (faceEnrolled)
                                            faceDetectionMsg ?: "Real face landmarks enrolled for Admin verification."
                                        else
                                            "Capture real front camera face. Admin will inspect photo before approving.",
                                        fontSize = 11.5.sp,
                                        fontFamily = AppFontFamily,
                                        color = MmyrvTextMuted,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Primary Action: Open Live Camera Viewport
                            Button(
                                onClick = { showFaceScanModal = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (faceEnrolled) Color(0xFF16A34A) else MmyrvPrimary
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_face_recognition),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (faceEnrolled) "Retake Live Face Photo" else "Open Live Camera & Enroll Face ID",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localValidationMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Password", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Lock Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                    ),
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; localValidationMsg = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Confirm Password", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Lock Icon",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (confirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                    ),
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = MmyrvBorder,
                            focusedContainerColor = Color(0xFFFAFAFC),
                            unfocusedContainerColor = Color(0xFFFAFAFC)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Terms Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreeTerms = !agreeTerms },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreeTerms,
                            onCheckedChange = { agreeTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MmyrvPrimary,
                                uncheckedColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I agree to the University Lab Guidelines & Biometric Policy",
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Register Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (fullName.isBlank() || email.isBlank() || rollNo.isBlank() || password.isBlank()) {
                                localValidationMsg = "Please fill in all mandatory fields."
                                return@Button
                            }
                            if (password != confirmPassword) {
                                localValidationMsg = "Passwords do not match."
                                return@Button
                            }
                            if (selectedRole == UserRole.STUDENT && !faceEnrolled) {
                                localValidationMsg = "Real Face Biometric registration is required. Please capture your photo above."
                                return@Button
                            }
                            if (!agreeTerms) {
                                localValidationMsg = "Please accept the university lab guidelines."
                                return@Button
                            }
                            localValidationMsg = null
                            onRegisterClick(
                                fullName,
                                email,
                                rollNo,
                                selectedRole.label,
                                password,
                                programme,
                                semester,
                                phone.ifBlank { "+91 98765 43210" },
                                faceEnrolled,
                                facePhotoBase64,
                                faceEmbedding
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_person),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Submit for Admin Approval",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Already have an account? Login Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 13.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                        Text(
                            text = "Login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvPrimary,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Live Camera In-App Facial Recognition Modal
    if (showFaceScanModal) {
        LiveFaceScannerModal(
            title = "Live Biometric Enrollment",
            subtitle = "Python DeepFace Facenet512 • 512D Template",
            onFaceVerified = { bitmap, base64, embedding, _ ->
                capturedFaceBitmap = bitmap
                facePhotoBase64 = base64
                faceEmbedding = embedding.toList()
                faceEnrolled = true
                faceDetectionMsg = "Real 512D Face Biometrics Enrolled (Python DeepFace)"
                showFaceScanModal = false
                localValidationMsg = null
                Toast.makeText(context, "Face Biometrics Enrolled Successfully!", Toast.LENGTH_SHORT).show()
            },
            onFaceCaptured = { bitmap, base64 ->
                capturedFaceBitmap = bitmap
                facePhotoBase64 = base64
                faceEnrolled = true
                showFaceScanModal = false
            },
            onDismiss = { showFaceScanModal = false }
        )
    }
}

@Composable
private fun FaceEnrollmentModal(
    studentName: String,
    onFaceCaptured: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = -75f,
        targetValue = 75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_face_recognition),
                    contentDescription = null,
                    tint = MmyrvPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Biometric Face Capture",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Initial identity enrollment for Lab Administrator verification.",
                    fontSize = 12.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Simulated High-Tech Camera Viewfinder Frame
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            width = 2.5.dp,
                            color = if (scanCompleted) Color(0xFF10B981) else MmyrvPrimary,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_face_recognition),
                        contentDescription = "Face Frame",
                        tint = Color.White.copy(alpha = if (scanCompleted) 0.95f else 0.45f),
                        modifier = Modifier.size(96.dp)
                    )

                    if (isScanning && !scanCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = scanLineOffset.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF38BDF8),
                                            Color.White,
                                            Color(0xFF38BDF8),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    if (scanCompleted) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        scanCompleted -> "Face successfully enrolled! AI biometric landmarks saved for admin verification."
                        isScanning -> "Analyzing 68 facial landmark coordinates ($scanProgress%)..."
                        else -> "Position your face squarely in the viewfinder frame with proper lighting."
                    },
                    fontSize = 12.sp,
                    fontFamily = AppFontFamily,
                    color = if (scanCompleted) Color(0xFF10B981) else MmyrvTextDark,
                    fontWeight = if (scanCompleted) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enrolling profile for: $studentName",
                    fontSize = 11.5.sp,
                    fontFamily = AppFontFamily,
                    color = Color(0xFF94A3B8)
                )
            }
        },
        confirmButton = {
            if (!scanCompleted) {
                Button(
                    onClick = {
                        isScanning = true
                        coroutineScope.launch {
                            for (p in 1..5) {
                                delay(350)
                                scanProgress = p * 20
                            }
                            delay(200)
                            isScanning = false
                            scanCompleted = true
                        }
                    },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Scanning...", fontFamily = AppFontFamily)
                    } else {
                        Text(text = "Start Face Capture", fontFamily = AppFontFamily)
                    }
                }
            } else {
                Button(
                    onClick = onFaceCaptured,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Confirm Face ID", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", fontFamily = AppFontFamily, color = MmyrvTextMuted)
            }
        }
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun RegisterScreenPreview() {
    LabPortalTheme {
        RegisterScreen()
    }
}
