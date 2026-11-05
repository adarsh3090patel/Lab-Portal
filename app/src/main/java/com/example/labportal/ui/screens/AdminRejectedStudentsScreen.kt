package com.example.labportal.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.labportal.R
import com.example.labportal.network.NetworkClient
import com.example.labportal.network.StudentApprovalDto
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
import kotlinx.coroutines.launch

object RejectedStudentsCache {
    var list: List<StudentApprovalDto> = emptyList()
}

@Composable
fun AdminRejectedStudentsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStudentForDetail by remember { mutableStateOf<StudentApprovalDto?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentApprovalDto?>(null) }

    val rejectedList = remember {
        mutableStateListOf<StudentApprovalDto>().apply {
            if (RejectedStudentsCache.list.isNotEmpty()) {
                addAll(RejectedStudentsCache.list)
            }
        }
    }
    var isLoading by remember { mutableStateOf(RejectedStudentsCache.list.isEmpty()) }
    var actionInProgressId by remember { mutableStateOf<String?>(null) }

    fun fetchRejectedStudents() {
        if (RejectedStudentsCache.list.isEmpty()) isLoading = true
        coroutineScope.launch {
            try {
                val res = NetworkClient.apiService.getRejectedStudents()
                isLoading = false
                if (res.isSuccessful && res.body() != null) {
                    val freshList = res.body()!!
                    RejectedStudentsCache.list = freshList
                    rejectedList.clear()
                    rejectedList.addAll(freshList)
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchRejectedStudents()
    }

    // Filter by universal search query
    val filteredStudents = remember(rejectedList.toList(), searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            rejectedList.toList()
        } else {
            rejectedList.filter { s ->
                s.fullName.lowercase().contains(q) ||
                s.rollNo.lowercase().contains(q) ||
                s.enrollmentNo.lowercase().contains(q) ||
                s.email.lowercase().contains(q) ||
                s.programme.lowercase().contains(q) ||
                s.semester.lowercase().contains(q) ||
                s.phone.lowercase().contains(q) ||
                s.department.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, MmyrvBorder),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onBackClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = MmyrvTextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rejected Students",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Review & restore declined registrations",
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }

                    IconButton(
                        onClick = { fetchRejectedStudents() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            tint = MmyrvPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MmyrvPrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Clear",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = "Search by Name, Roll, Email, Programme...",
                        fontSize = 12.5.sp,
                        fontFamily = AppFontFamily,
                        color = Color(0xFF94A3B8)
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Rejection Status Header Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${filteredStudents.size} Declined Student Records",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = AppFontFamily,
                        color = Color(0xFFB91C1C)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Can be restored anytime",
                        fontSize = 11.sp,
                        fontFamily = AppFontFamily,
                        color = Color(0xFF991B1B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MmyrvPrimary, modifier = Modifier.size(34.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Loading rejected records...",
                            fontSize = 13.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }
                }
            } else if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching rejected students" else "No Rejected Students",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching with a different term" else "All registered students are either approved or pending verification.",
                            fontSize = 12.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        RejectedStudentCard(
                            student = student,
                            isOperating = actionInProgressId == student.id,
                            onCardClick = { selectedStudentForDetail = student },
                            onReApprove = {
                                actionInProgressId = student.id
                                coroutineScope.launch {
                                    try {
                                        val res = NetworkClient.apiService.approveStudent(student.id)
                                        actionInProgressId = null
                                        if (res.isSuccessful) {
                                            rejectedList.removeAll { it.id == student.id }
                                            RejectedStudentsCache.list = rejectedList.toList()
                                            // Add to approved cache
                                            ApprovedStudentsCache.list = ApprovedStudentsCache.list + student.copy(isApproved = true, status = "approved")
                                            Toast.makeText(context, "${student.fullName} re-approved successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Approval failed: ${res.message()}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        actionInProgressId = null
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDelete = {
                                studentToDelete = student
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog for Rejected Student
    if (selectedStudentForDetail != null) {
        val student = selectedStudentForDetail!!
        val faceBitmap = remember(student.facePhotoUrl) {
            FaceDetectionHelper.base64ToImageBitmap(student.facePhotoUrl)
        }
        val avatarBitmap = remember(student.profilePhotoUrl) {
            FaceDetectionHelper.base64ToImageBitmap(student.profilePhotoUrl)
        }

        Dialog(onDismissRequest = { selectedStudentForDetail = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Declined Student Record",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                        }

                        IconButton(
                            onClick = { selectedStudentForDetail = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = MmyrvTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Photos Row: Biometric + Avatar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(BorderStroke(2.dp, Color(0xFFEF4444)), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (faceBitmap != null) {
                                    Image(
                                        bitmap = faceBitmap,
                                        contentDescription = "Facial Biometric Key",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (avatarBitmap != null) {
                                    Image(
                                        bitmap = avatarBitmap,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_face_recognition),
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Facial Biometric",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = AppFontFamily,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = student.fullName,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Status: Registration Rejected",
                        fontSize = 12.sp,
                        fontFamily = AppFontFamily,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItemRow("University Roll No.", student.rollNo)
                    DetailItemRow("Enrollment Number", student.enrollmentNo.ifBlank { "N/A" })
                    DetailItemRow("Degree & Programme", student.programme)
                    DetailItemRow("Current Semester", student.semester)
                    DetailItemRow("University Email", student.email)
                    DetailItemRow("Contact Phone", student.phone.ifBlank { "Not provided" })
                    DetailItemRow("College / University", student.collegeName.ifBlank { "MMYRV Ayodhya" })

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val s = student
                                selectedStudentForDetail = null
                                studentToDelete = s
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete Record", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                        }

                        Button(
                            onClick = {
                                val s = student
                                selectedStudentForDetail = null
                                coroutineScope.launch {
                                    try {
                                        val res = NetworkClient.apiService.approveStudent(s.id)
                                        if (res.isSuccessful) {
                                            rejectedList.removeAll { it.id == s.id }
                                            RejectedStudentsCache.list = rejectedList.toList()
                                            ApprovedStudentsCache.list = ApprovedStudentsCache.list + s.copy(isApproved = true, status = "approved")
                                            Toast.makeText(context, "${s.fullName} re-approved!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Re-Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                        }
                    }
                }
            }
        }
    }

    // Permanent Deletion Confirmation Dialog
    if (studentToDelete != null) {
        val s = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Student Record?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete ${s.fullName}'s registration record? This will remove all their details from MongoDB permanently and cannot be undone.",
                    fontSize = 13.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextMuted,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        studentToDelete = null
                        actionInProgressId = s.id
                        coroutineScope.launch {
                            try {
                                val res = NetworkClient.apiService.deleteStudentPermanently(s.id)
                                actionInProgressId = null
                                if (res.isSuccessful) {
                                    rejectedList.removeAll { it.id == s.id }
                                    RejectedStudentsCache.list = rejectedList.toList()
                                    Toast.makeText(context, "${s.fullName} deleted permanently.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Delete failed: ${res.message()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                actionInProgressId = null
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete Permanently", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel", fontFamily = AppFontFamily, color = MmyrvTextMuted)
                }
            }
        )
    }
}

@Composable
private fun RejectedStudentCard(
    student: StudentApprovalDto,
    isOperating: Boolean,
    onCardClick: () -> Unit,
    onReApprove: () -> Unit,
    onDelete: () -> Unit
) {
    val faceBitmap = remember(student.facePhotoUrl) {
        FaceDetectionHelper.base64ToImageBitmap(student.facePhotoUrl)
    }
    val avatarBitmap = remember(student.profilePhotoUrl) {
        FaceDetectionHelper.base64ToImageBitmap(student.profilePhotoUrl)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MmyrvBorder)
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
                // Photo
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(BorderStroke(1.5.dp, Color(0xFFFCA5A5)), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (faceBitmap != null) {
                        Image(
                            bitmap = faceBitmap,
                            contentDescription = "Facial Biometric",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = student.fullName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark,
                            maxLines = 1
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(0.8.dp, Color(0xFFFECACA))
                        ) {
                            Text(
                                text = "REJECTED",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Roll: ${student.rollNo} • ${student.programme}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = AppFontFamily,
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = student.email,
                        fontSize = 11.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    enabled = !isOperating
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delete",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily
                    )
                }

                Button(
                    onClick = onReApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    enabled = !isOperating
                ) {
                    if (isOperating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check_circle),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Re-Approve",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = AppFontFamily,
            color = MmyrvTextMuted
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = AppFontFamily,
            color = MmyrvTextDark,
            textAlign = TextAlign.End
        )
    }
}

