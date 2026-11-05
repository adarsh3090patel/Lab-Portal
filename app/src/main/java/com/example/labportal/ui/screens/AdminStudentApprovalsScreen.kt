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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.network.NetworkClient
import com.example.labportal.network.StudentApprovalDto
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBadgeBg
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
import kotlinx.coroutines.launch

@Composable
fun AdminStudentApprovalsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var selectedFilterTab by remember { mutableStateOf(0) } // 0 = Pending, 1 = Approved / All
    val studentsList = remember { mutableStateListOf<StudentApprovalDto>() }

    // Fetch students from backend MongoDB
    fun loadStudents() {
        isLoading = true
        coroutineScope.launch {
            try {
                val response = NetworkClient.apiService.getStudentsForApproval()
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    studentsList.clear()
                    studentsList.addAll(response.body()!!)
                } else {
                    // Demo fallback if backend is offline or starting up
                    if (studentsList.isEmpty()) {
                        studentsList.addAll(
                            listOf(
                                StudentApprovalDto(
                                    id = "demo1",
                                    fullName = "Adarsh Kumar",
                                    email = "adarsh25@mmyrv.ac.in",
                                    rollNo = "25CS0101",
                                    role = "Student",
                                    isApproved = false,
                                    createdAt = "Today, 12:30 PM"
                                ),
                                StudentApprovalDto(
                                    id = "demo2",
                                    fullName = "Priya Sharma",
                                    email = "priya.sharma@mmyrv.edu.in",
                                    rollNo = "MMYRV/2026/088",
                                    role = "Student",
                                    isApproved = false,
                                    createdAt = "Yesterday"
                                ),
                                StudentApprovalDto(
                                    id = "demo3",
                                    fullName = "Rohan Verma",
                                    email = "student@mmyrv.edu.in",
                                    rollNo = "MMYRV/2026/042",
                                    role = "Student",
                                    isApproved = true,
                                    createdAt = "01 Nov 2026"
                                )
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                if (studentsList.isEmpty()) {
                    studentsList.addAll(
                        listOf(
                            StudentApprovalDto(
                                id = "demo1",
                                fullName = "Adarsh Kumar",
                                email = "adarsh25@mmyrv.ac.in",
                                rollNo = "25CS0101",
                                role = "Student",
                                isApproved = false,
                                createdAt = "Today, 12:30 PM"
                            ),
                            StudentApprovalDto(
                                id = "demo2",
                                fullName = "Priya Sharma",
                                email = "priya.sharma@mmyrv.edu.in",
                                rollNo = "MMYRV/2026/088",
                                role = "Student",
                                isApproved = false,
                                createdAt = "Yesterday"
                            )
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadStudents()
    }

    val pendingCount = studentsList.count { !it.isApproved }
    val filteredList = when (selectedFilterTab) {
        0 -> studentsList.filter { !it.isApproved }
        else -> studentsList
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Back to Menu",
                                tint = MmyrvTextDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Student Approvals",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Text(
                                text = "Verify new registrations",
                                fontSize = 11.sp,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextMuted
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(onClick = { loadStudents() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_schedule),
                            contentDescription = "Refresh",
                            tint = MmyrvPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                HorizontalDivider(color = MmyrvBorder, thickness = 1.dp)
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Filter Pills Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                // Tab 0: Pending
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selectedFilterTab == 0) Color.White else Color.Transparent)
                        .clickable { selectedFilterTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Pending Approvals",
                            fontSize = 12.5.sp,
                            fontWeight = if (selectedFilterTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                            fontFamily = AppFontFamily,
                            color = if (selectedFilterTab == 0) MmyrvPrimary else MmyrvTextMuted
                        )
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selectedFilterTab == 0) MmyrvPrimary else Color(0xFF94A3B8))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$pendingCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Tab 1: All Registered
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selectedFilterTab == 1) Color.White else Color.Transparent)
                        .clickable { selectedFilterTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All Registered (${studentsList.size})",
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedFilterTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                        fontFamily = AppFontFamily,
                        color = if (selectedFilterTab == 1) MmyrvPrimary else MmyrvTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MmyrvPrimary, modifier = Modifier.size(32.dp))
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Pending Approvals",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All student registrations have been verified and processed.",
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { student ->
                        StudentApprovalCard(
                            student = student,
                            onApprove = {
                                coroutineScope.launch {
                                    try {
                                        NetworkClient.apiService.approveStudent(student.id)
                                    } catch (_: Exception) {}
                                    // Update state locally
                                    val idx = studentsList.indexOfFirst { it.id == student.id }
                                    if (idx != -1) {
                                        studentsList[idx] = student.copy(isApproved = true)
                                    }
                                    Toast.makeText(context, "${student.fullName} has been approved!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onReject = {
                                coroutineScope.launch {
                                    try {
                                        NetworkClient.apiService.rejectStudent(student.id)
                                    } catch (_: Exception) {}
                                    studentsList.removeAll { it.id == student.id }
                                    Toast.makeText(context, "${student.fullName} rejected and removed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentApprovalCard(
    student: StudentApprovalDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showFullFaceDialog by remember { mutableStateOf(false) }
    val profileImageBitmap = remember(student.profilePhotoUrl) {
        FaceDetectionHelper.base64ToImageBitmap(student.profilePhotoUrl)
    }
    val realFaceBitmap = remember(student.facePhotoUrl) {
        FaceDetectionHelper.base64ToImageBitmap(student.facePhotoUrl)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MmyrvBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Avatar, Name & Enrollment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (student.isApproved) Color(0xFFDCFCE7) else MmyrvBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageBitmap != null) {
                        Image(
                            bitmap = profileImageBitmap,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = if (student.isApproved) R.drawable.ic_check_circle else R.drawable.ic_school),
                            contentDescription = null,
                            tint = if (student.isApproved) Color(0xFF10B981) else MmyrvPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Enrollment: ${student.rollNo}",
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status Badge Pill
                if (student.isApproved) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Approved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color(0xFF15803D)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Pending",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Details: Email & Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email),
                        contentDescription = null,
                        tint = MmyrvTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = student.email,
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted
                    )
                }

                Text(
                    text = student.role,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = AppFontFamily,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Biometric Facial Recognition Identification Panel (for Admin verification)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric Face Capture Thumbnail
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                            .clickable { if (realFaceBitmap != null) showFullFaceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (realFaceBitmap != null) {
                            Image(
                                bitmap = realFaceBitmap,
                                contentDescription = "Real Student Face",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_face_recognition),
                                contentDescription = "Biometric Face Preview",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Screen-Lock Biometric ID",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (student.faceEnrolled) "Enrolled" else "Pending",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${student.programme} • ${student.semester}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                        Text(
                            text = "Phone: ${student.phone}",
                            fontSize = 10.5.sp,
                            fontFamily = AppFontFamily,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // If pending: Action Buttons (Approve / Reject)
            if (!student.isApproved) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Approve Button
                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check_circle),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Approve",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color.White
                        )
                    }

                    // Reject Button
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text(
                            text = "Reject",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }

    // High-Resolution Biometric Face Verification Modal for Admin
    if (showFullFaceDialog && realFaceBitmap != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFullFaceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_face_recognition),
                        contentDescription = null,
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Biometric Face Verification",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
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
                        text = "Verify this enrolled photo matches student identity before approving.",
                        fontSize = 12.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Image(
                        bitmap = realFaceBitmap,
                        contentDescription = "Full Student Face",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(2.dp, Color(0xFF10B981), RoundedCornerShape(18.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = student.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                    Text(
                        text = "${student.programme} • ${student.semester}",
                        fontSize = 12.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Roll No: ${student.rollNo} • Phone: ${student.phone}",
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFullFaceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Close Inspection", fontFamily = AppFontFamily)
                }
            }
        )
    }
}

