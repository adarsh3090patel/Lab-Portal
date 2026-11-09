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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.labportal.network.EditHistoryRecordDto
import com.example.labportal.network.GrantPermissionsDto
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAllStudentsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProgrammeFilter by remember { mutableStateOf("All") }
    var showFilterRow by remember { mutableStateOf(false) }

    // Bottom Sheet & Dialog States
    var studentForActionSheet by remember { mutableStateOf<StudentApprovalDto?>(null) }
    var studentForPermissions by remember { mutableStateOf<StudentApprovalDto?>(null) }
    var studentForEditHistory by remember { mutableStateOf<StudentApprovalDto?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentApprovalDto?>(null) }

    // Permissions Dialog State
    var allowFacePermission by remember { mutableStateOf(false) }
    var allowAcademicPermission by remember { mutableStateOf(false) }
    var allowPersonalPermission by remember { mutableStateOf(false) }
    var isSavingPermissions by remember { mutableStateOf(false) }

    // Edit History Dialog State
    var historyList by remember { mutableStateOf<List<EditHistoryRecordDto>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(false) }

    // Operational loading state
    var isOperating by remember { mutableStateOf(false) }

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
                            onClick = { studentForActionSheet = student }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet Slide Panel: Action controls & details for selected student
    if (studentForActionSheet != null) {
        val s = studentForActionSheet!!
        val studentPhotoBitmap = remember(s.profilePhotoUrl, s.facePhotoUrl) {
            val photoStr = s.profilePhotoUrl.ifBlank { s.facePhotoUrl }
            FaceDetectionHelper.base64ToImageBitmap(photoStr)
        }

        ModalBottomSheet(
            onDismissRequest = { studentForActionSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                // Header with Avatar and Basic Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF))
                            .border(BorderStroke(2.dp, if (s.isBlocked) Color(0xFFFCA5A5) else Color(0xFFC7D2FE)), CircleShape),
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
                                tint = if (s.isBlocked) Color(0xFFEF4444) else MmyrvPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.fullName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Roll No: ${s.rollNo} • ${s.semester}",
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (s.isBlocked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, if (s.isBlocked) Color(0xFFFECACA) else Color(0xFFA7F3D0))
                        ) {
                            Text(
                                text = if (s.isBlocked) "ACCOUNT BLOCKED" else "ACTIVE STUDENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (s.isBlocked) Color(0xFFB91C1C) else Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Administrative Actions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Grid (Row 1: Block/Unblock & Update Permissions)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Block / Unblock Button
                    Button(
                        onClick = {
                            val willBlock = !s.isBlocked
                            coroutineScope.launch {
                                isOperating = true
                                try {
                                    val res = if (willBlock) {
                                        NetworkClient.apiService.blockStudent(s.id)
                                    } else {
                                        NetworkClient.apiService.unblockStudent(s.id)
                                    }
                                    isOperating = false
                                    if (res.isSuccessful) {
                                        val updated = s.copy(
                                            isBlocked = willBlock,
                                            status = if (willBlock) "blocked" else "approved"
                                        )
                                        val idx = studentsList.indexOfFirst { it.id == s.id }
                                        if (idx != -1) studentsList[idx] = updated
                                        studentForActionSheet = updated
                                        ApprovedStudentsCache.list = studentsList.toList()
                                        Toast.makeText(
                                            context,
                                            if (willBlock) "Student blocked! Portal access suspended." else "Student unblocked successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(context, "Failed to update block status.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isOperating = false
                                    Toast.makeText(context, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (s.isBlocked) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        enabled = !isOperating
                    ) {
                        if (isOperating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (s.isBlocked) "Unblocking..." else "Blocking...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = AppFontFamily
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = if (s.isBlocked) R.drawable.ic_check_circle else R.drawable.ic_close),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (s.isBlocked) "Unblock Student" else "Block Access",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = AppFontFamily
                            )
                        }
                    }

                    // Update Permissions Button
                    Button(
                        onClick = {
                            allowFacePermission = s.allowedEdits.face
                            allowAcademicPermission = s.allowedEdits.academic
                            allowPersonalPermission = s.allowedEdits.personal
                            studentForPermissions = s
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF)),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = null,
                            tint = MmyrvPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Permissions",
                            color = MmyrvPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Grid (Row 2: Edit History & Delete Student)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Edit History Button
                    Button(
                        onClick = {
                            studentForEditHistory = s
                            isLoadingHistory = true
                            historyList = emptyList()
                            coroutineScope.launch {
                                try {
                                    val res = NetworkClient.apiService.getStudentEditHistory(s.id)
                                    isLoadingHistory = false
                                    if (res.isSuccessful && res.body() != null) {
                                        historyList = res.body()!!
                                    }
                                } catch (_: Exception) {
                                    isLoadingHistory = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAF5FF)),
                        border = BorderStroke(1.dp, Color(0xFFE9D5FF))
                    ) {
                        if (isLoadingHistory && studentForEditHistory?.id == s.id) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF7E22CE)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Loading History...",
                                color = Color(0xFF7E22CE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = AppFontFamily
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_schedule),
                                contentDescription = null,
                                tint = Color(0xFF7E22CE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit History",
                                color = Color(0xFF7E22CE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = AppFontFamily
                            )
                        }
                    }

                    // Delete Student Button
                    Button(
                        onClick = { studentToDelete = s },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F2)),
                        border = BorderStroke(1.dp, Color(0xFFFECDD3))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delete Student",
                            color = Color(0xFFE11D48),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Academic & Personal Details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                StudentRecordRow(iconRes = R.drawable.ic_school, label = "Programme / Degree", value = s.programme)
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)

                StudentRecordRow(iconRes = R.drawable.ic_schedule, label = "Semester", value = s.semester)
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)

                StudentRecordRow(iconRes = R.drawable.ic_resources, label = "Department", value = s.department)
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)

                StudentRecordRow(iconRes = R.drawable.ic_email, label = "University Email", value = s.email)
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)

                StudentRecordRow(iconRes = R.drawable.ic_phone, label = "Phone Number", value = s.phone)
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)

                StudentRecordRow(
                    iconRes = R.drawable.ic_face_recognition,
                    label = "Facial Biometric Template",
                    value = if (s.faceEnrolled) "Enrolled & Active (192D Features)" else "Not Enrolled"
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog 1: Grant Permissions Popup (Face, Academic, Personal)
    if (studentForPermissions != null) {
        val s = studentForPermissions!!
        AlertDialog(
            onDismissRequest = { studentForPermissions = null },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = null,
                    tint = MmyrvPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Grant 1-Time Update Permissions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Student: ${s.fullName} (${s.rollNo})\nSelect which sections the student is authorized to modify. The student can edit each granted section ONCE, after which it automatically re-locks.",
                        fontSize = 12.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Checkbox 1: Facial Recognition
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (allowFacePermission) Color(0xFFEEF2FF) else Color(0xFFF8FAFC))
                            .clickable { allowFacePermission = !allowFacePermission }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowFacePermission,
                            onCheckedChange = { allowFacePermission = it },
                            colors = CheckboxDefaults.colors(checkedColor = MmyrvPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Face Recognition Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = AppFontFamily, color = MmyrvTextDark)
                            Text("Permit 1-time re-enrollment of 3D facial template", fontSize = 11.sp, fontFamily = AppFontFamily, color = MmyrvTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checkbox 2: Academic Information
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (allowAcademicPermission) Color(0xFFEEF2FF) else Color(0xFFF8FAFC))
                            .clickable { allowAcademicPermission = !allowAcademicPermission }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowAcademicPermission,
                            onCheckedChange = { allowAcademicPermission = it },
                            colors = CheckboxDefaults.colors(checkedColor = MmyrvPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Academic Information", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = AppFontFamily, color = MmyrvTextDark)
                            Text("Permit 1-time edit of Course, Semester, Department", fontSize = 11.sp, fontFamily = AppFontFamily, color = MmyrvTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checkbox 3: Contact & Personal Details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (allowPersonalPermission) Color(0xFFEEF2FF) else Color(0xFFF8FAFC))
                            .clickable { allowPersonalPermission = !allowPersonalPermission }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowPersonalPermission,
                            onCheckedChange = { allowPersonalPermission = it },
                            colors = CheckboxDefaults.colors(checkedColor = MmyrvPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Contact & Personal Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = AppFontFamily, color = MmyrvTextDark)
                            Text("Permit 1-time edit of Phone, DOB, Blood Group, Address", fontSize = 11.sp, fontFamily = AppFontFamily, color = MmyrvTextMuted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSavingPermissions = true
                        coroutineScope.launch {
                            try {
                                val res = NetworkClient.apiService.grantStudentPermissions(
                                    s.id,
                                    GrantPermissionsDto(
                                        face = allowFacePermission,
                                        academic = allowAcademicPermission,
                                        personal = allowPersonalPermission
                                    )
                                )
                                isSavingPermissions = false
                                if (res.isSuccessful) {
                                    val updatedAllowed = s.allowedEdits.copy(
                                        face = allowFacePermission,
                                        academic = allowAcademicPermission,
                                        personal = allowPersonalPermission
                                    )
                                    val updated = s.copy(allowedEdits = updatedAllowed)
                                    val idx = studentsList.indexOfFirst { it.id == s.id }
                                    if (idx != -1) studentsList[idx] = updated
                                    studentForActionSheet = updated
                                    studentForPermissions = null
                                    Toast.makeText(context, res.body()?.message ?: "Permissions granted successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to grant permissions.", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isSavingPermissions = false
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSavingPermissions
                ) {
                    if (isSavingPermissions) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saving...", color = Color.White, fontFamily = AppFontFamily)
                    } else {
                        Text("Save Permissions", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { studentForPermissions = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = MmyrvTextDark, fontFamily = AppFontFamily)
                }
            }
        )
    }

    // Dialog 2: Edit History Audit Log Dialog
    if (studentForEditHistory != null) {
        val s = studentForEditHistory!!
        Dialog(onDismissRequest = { studentForEditHistory = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.75f)
                    .clip(RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Profile Edit History",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Text(
                                text = "${s.fullName} (${s.rollNo})",
                                fontSize = 11.5.sp,
                                fontFamily = AppFontFamily,
                                color = MmyrvPrimary
                            )
                        }
                        IconButton(onClick = { studentForEditHistory = null }, modifier = Modifier.size(28.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Close", tint = MmyrvTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoadingHistory) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = MmyrvPrimary,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Fetching edit audit trail from MongoDB Atlas...",
                                    fontSize = 12.sp,
                                    fontFamily = AppFontFamily,
                                    color = MmyrvTextMuted
                                )
                            }
                        }
                    } else if (historyList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(painter = painterResource(id = R.drawable.ic_schedule), contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Edit History Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = AppFontFamily, color = MmyrvTextDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Student profile has not been modified since registration.", fontSize = 12.sp, fontFamily = AppFontFamily, color = MmyrvTextMuted, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(historyList, key = { it.id.ifBlank { it.timestamp } }) { record ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFEEF2FF),
                                                border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                                            ) {
                                                Text(
                                                    text = record.type,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MmyrvPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = record.timestamp.take(19).replace("T", " "),
                                                fontSize = 10.5.sp,
                                                fontFamily = AppFontFamily,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        record.changes.forEach { change ->
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Text(
                                                    text = change.field,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    fontFamily = AppFontFamily,
                                                    color = MmyrvTextDark
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Before: ${change.oldValue ?: "-"}",
                                                        fontSize = 11.sp,
                                                        fontFamily = AppFontFamily,
                                                        color = Color(0xFFEF4444)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "->",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF94A3B8)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "After: ${change.newValue ?: "-"}",
                                                        fontSize = 11.sp,
                                                        fontFamily = AppFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF10B981)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog 3: Delete Student Confirmation Dialog
    if (studentToDelete != null) {
        val s = studentToDelete!!
        var isDeletingStudent by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isDeletingStudent) studentToDelete = null },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Student Record?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete ${s.fullName} (Roll: ${s.rollNo})?\n\nThis will completely erase all academic records, facial biometrics, and attendance logs from MongoDB Atlas. This action cannot be undone.",
                    fontSize = 12.5.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextMuted,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeletingStudent = true
                        coroutineScope.launch {
                            try {
                                val res = NetworkClient.apiService.deleteStudentPermanently(s.id)
                                isDeletingStudent = false
                                if (res.isSuccessful) {
                                    studentsList.removeAll { it.id == s.id }
                                    ApprovedStudentsCache.list = studentsList.toList()
                                    studentForActionSheet = null
                                    studentToDelete = null
                                    Toast.makeText(context, "Student deleted permanently.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete student.", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isDeletingStudent = false
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isDeletingStudent
                ) {
                    if (isDeletingStudent) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Deleting...", color = Color.White, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { if (!isDeletingStudent) studentToDelete = null },
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isDeletingStudent
                ) {
                    Text("Cancel", color = MmyrvTextDark, fontFamily = AppFontFamily)
                }
            }
        )
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

                // Status Badge (Active vs Blocked)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (student.isBlocked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, if (student.isBlocked) Color(0xFFFECACA) else Color(0xFFA7F3D0))
                ) {
                    Text(
                        text = if (student.isBlocked) "Blocked" else "Approved",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = if (student.isBlocked) Color(0xFFB91C1C) else Color(0xFF15803D),
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

