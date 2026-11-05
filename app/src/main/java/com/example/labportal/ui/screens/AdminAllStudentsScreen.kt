package com.example.labportal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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

object ApprovedStudentsCache {
    var list: List<StudentApprovalDto> = emptyList()
}

@Composable
fun AdminAllStudentsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProgrammeFilter by remember { mutableStateOf("All") }
    var showFilterRow by remember { mutableStateOf(false) }
    var selectedStudentForDetail by remember { mutableStateOf<StudentApprovalDto?>(null) }

    val studentsList = remember {
        mutableStateListOf<StudentApprovalDto>().apply {
            if (ApprovedStudentsCache.list.isNotEmpty()) {
                addAll(ApprovedStudentsCache.list)
            }
        }
    }
    var isLoading by remember { mutableStateOf(ApprovedStudentsCache.list.isEmpty()) }

    fun fetchStudents() {
        if (ApprovedStudentsCache.list.isEmpty()) isLoading = true
        coroutineScope.launch {
            try {
                val res = NetworkClient.apiService.getAllApprovedStudents()
                isLoading = false
                if (res.isSuccessful && res.body() != null) {
                    val approvedOnly = res.body()!!.filter { it.isApproved }
                    studentsList.clear()
                    studentsList.addAll(approvedOnly)
                    ApprovedStudentsCache.list = approvedOnly
                } else if (studentsList.isEmpty()) {
                    val fallback = defaultApprovedStudentsList()
                    studentsList.clear()
                    studentsList.addAll(fallback)
                }
            } catch (e: Exception) {
                isLoading = false
                if (studentsList.isEmpty()) {
                    studentsList.clear()
                    studentsList.addAll(defaultApprovedStudentsList())
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchStudents()
    }

    // Filter and search logic across ALL student details
    val filteredStudents = remember(studentsList.toList(), searchQuery, selectedProgrammeFilter) {
        val query = searchQuery.trim().lowercase()
        studentsList.filter { student ->
            val matchesFilter = selectedProgrammeFilter == "All" ||
                    student.programme.contains(selectedProgrammeFilter, ignoreCase = true)

            val matchesSearch = query.isEmpty() ||
                    student.fullName.lowercase().contains(query) ||
                    student.rollNo.lowercase().contains(query) ||
                    student.email.lowercase().contains(query) ||
                    student.phone.lowercase().contains(query) ||
                    student.programme.lowercase().contains(query) ||
                    student.semester.lowercase().contains(query) ||
                    student.department.lowercase().contains(query)

            matchesFilter && matchesSearch
        }
    }

    // Available programmes for filter chips
    val availableProgrammes = remember(studentsList.toList()) {
        listOf("All") + studentsList.map { it.programme }.distinct().filter { it.isNotBlank() }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
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
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Students",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Text(
                                text = "Approved Student Directory (${filteredStudents.size} enrolled)",
                                fontSize = 11.5.sp,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextMuted
                            )
                        }

                        // Filter Toggle Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (showFilterRow || selectedProgrammeFilter != "All") Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (selectedProgrammeFilter != "All") MmyrvPrimary else MmyrvBorder),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { showFilterRow = !showFilterRow }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_filter),
                                    contentDescription = "Filter",
                                    tint = if (selectedProgrammeFilter != "All") MmyrvPrimary else MmyrvTextDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Universal Multi-Field Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search by Name, Roll No, Email, Programme...",
                                fontSize = 12.5.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = AppFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = null,
                                tint = MmyrvPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "Clear",
                                        tint = MmyrvTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MmyrvPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    // Expandable Filter Chips Row
                    if (showFilterRow) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableProgrammes.forEach { prog ->
                                val isSelected = selectedProgrammeFilter == prog
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) MmyrvPrimary else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) MmyrvPrimary else Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable {
                                        selectedProgrammeFilter = prog
                                    }
                                ) {
                                    Text(
                                        text = prog,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = AppFontFamily,
                                        color = if (isSelected) Color.White else MmyrvTextDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MmyrvPrimary, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading approved students...",
                            fontSize = 13.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }
                }
            } else if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = null,
                                tint = MmyrvPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching students found" else "No approved students yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Try searching with a different name, roll number, or clear filters."
                            else
                                "When student registration requests are approved, they will appear here.",
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        ApprovedStudentCard(
                            student = student,
                            onClick = { selectedStudentForDetail = student }
                        )
                    }
                }
            }
        }
    }

    // Full Details Modal Dialog
    if (selectedStudentForDetail != null) {
        val s = selectedStudentForDetail!!
        val studentPhotoBitmap = remember(s.profilePhotoUrl, s.facePhotoUrl) {
            val photoStr = s.profilePhotoUrl.ifBlank { s.facePhotoUrl }
            FaceDetectionHelper.base64ToImageBitmap(photoStr)
        }

        Dialog(onDismissRequest = { selectedStudentForDetail = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp)
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
                        Text(
                            text = "Student Official Record",
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            fontSize = 16.sp,
                            color = MmyrvTextDark
                        )
                        IconButton(
                            onClick = { selectedStudentForDetail = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = MmyrvTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF))
                                .border(BorderStroke(2.dp, Color(0xFFC7D2FE)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (studentPhotoBitmap != null) {
                                Image(
                                    bitmap = studentPhotoBitmap,
                                    contentDescription = "Student Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_person),
                                    contentDescription = null,
                                    tint = MmyrvPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = s.fullName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Roll No: ${s.rollNo}",
                                fontSize = 12.sp,
                                fontFamily = AppFontFamily,
                                color = MmyrvPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Text(
                                    text = "Active & Enrolled",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    StudentRecordRow(iconRes = R.drawable.ic_school, label = "Programme / Degree", value = s.programme)
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    StudentRecordRow(iconRes = R.drawable.ic_schedule, label = "Semester", value = s.semester)
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    StudentRecordRow(iconRes = R.drawable.ic_resources, label = "Department", value = s.department)
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    StudentRecordRow(iconRes = R.drawable.ic_email, label = "University Email", value = s.email)
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    StudentRecordRow(iconRes = R.drawable.ic_phone, label = "Phone Number", value = s.phone)
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    StudentRecordRow(iconRes = R.drawable.ic_face_recognition, label = "Facial Biometric Template", value = if (s.faceEnrolled) "Enrolled & Active (Screen Lock)" else "Not Enrolled")

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedStudentForDetail = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                    ) {
                        Text("Close Record", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovedStudentCard(
    student: StudentApprovalDto,
    onClick: () -> Unit
) {
    val photoBitmap = remember(student.profilePhotoUrl, student.facePhotoUrl) {
        val str = student.profilePhotoUrl.ifBlank { student.facePhotoUrl }
        FaceDetectionHelper.base64ToImageBitmap(str)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF))
                        .border(BorderStroke(1.5.dp, Color(0xFFC7D2FE)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            tint = MmyrvPrimary,
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
                        text = "Roll: ${student.rollNo} • ${student.semester}",
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Green Approved Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Text(
                        text = "Approved",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = student.programme,
                    fontSize = 11.5.sp,
                    fontFamily = AppFontFamily,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Tap to view record",
                    fontSize = 10.5.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StudentRecordRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MmyrvPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, fontFamily = AppFontFamily, color = MmyrvTextMuted)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = AppFontFamily, color = MmyrvTextDark)
        }
    }
}

fun defaultApprovedStudentsList(): List<StudentApprovalDto> {
    return listOf(
        StudentApprovalDto(
            id = "demo1",
            fullName = "Adarsh Kumar",
            email = "adarsh25@mmyrv.ac.in",
            rollNo = "25CS0101",
            role = "Student",
            programme = "B.Sc. (Computer Science)",
            semester = "Semester V",
            department = "Computer Science & Engineering",
            phone = "+91 98765 43210",
            isApproved = true,
            status = "approved"
        ),
        StudentApprovalDto(
            id = "demo2",
            fullName = "Rohan Sharma",
            email = "rohan.sharma@mmyrv.ac.in",
            rollNo = "25CS0102",
            role = "Student",
            programme = "B.Tech (Computer Science & Engineering)",
            semester = "Semester I",
            department = "Computer Science & Engineering",
            phone = "+91 98765 43211",
            isApproved = true,
            status = "approved"
        ),
        StudentApprovalDto(
            id = "demo3",
            fullName = "Priya Verma",
            email = "priya.verma@mmyrv.ac.in",
            rollNo = "25IT0201",
            role = "Student",
            programme = "BCA (Bachelor of Computer Applications)",
            semester = "Semester III",
            department = "Information Technology",
            phone = "+91 98765 43212",
            isApproved = true,
            status = "approved"
        )
    )
}

