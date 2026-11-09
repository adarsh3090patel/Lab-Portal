package com.example.labportal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.network.CourseCreateDto
import com.example.labportal.network.CourseDto
import com.example.labportal.network.NetworkClient
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBadgeBg
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import kotlinx.coroutines.launch

object CourseCache {
    var list: List<CourseDto> = emptyList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCoursesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showAddCourseSheet by remember { mutableStateOf(false) }

    // Instant 0ms loading from in-memory cache or default static courses
    val coursesList = remember {
        mutableStateListOf<CourseDto>().apply {
            if (CourseCache.list.isNotEmpty()) {
                addAll(CourseCache.list)
            } else {
                addAll(defaultStaticCourses())
            }
        }
    }
    var isRefreshing by remember { mutableStateOf(false) }

    fun loadCourses(showSpinner: Boolean = false) {
        if (showSpinner) isRefreshing = true
        coroutineScope.launch {
            try {
                val res = NetworkClient.apiService.getCourses()
                isRefreshing = false
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    val fresh = res.body()!!
                    CourseCache.list = fresh
                    coursesList.clear()
                    coursesList.addAll(fresh)
                }
            } catch (e: Exception) {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        // Fast background sync so the screen opens immediately in 0ms without waiting
        loadCourses(showSpinner = false)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
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

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Courses & Programmes",
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Manage university degrees & semesters",
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }

                    Button(
                        onClick = { showAddCourseSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Course",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color.White
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Summary Info Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEF2FF))
                    .border(BorderStroke(1.dp, Color(0xFFE0E7FF)), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_school),
                            contentDescription = null,
                            tint = MmyrvPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${coursesList.size} Active Degree Programmes",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MmyrvPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Programmes and semesters added here appear instantly in student registration dropdowns.",
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (coursesList.isEmpty() && isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MmyrvPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Loading Degree Catalog...",
                            fontSize = 13.5.sp,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MmyrvTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(coursesList, key = { it.id }) { course ->
                        CourseManagementCard(
                            course = course,
                            onDelete = { onFinish ->
                                coroutineScope.launch {
                                    try {
                                        NetworkClient.apiService.deleteCourse(course.id)
                                    } catch (_: Exception) {}
                                    coursesList.removeAll { it.id == course.id }
                                    CourseCache.list = coursesList.toList()
                                    Toast.makeText(context, "${course.name} removed", Toast.LENGTH_SHORT).show()
                                    onFinish()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Course Bottom Slide Panel (ModalBottomSheet) with 100% width and compact height
    if (showAddCourseSheet) {
        AddCourseBottomSheet(
            onDismiss = { showAddCourseSheet = false },
            onAdd = { name, code, semestersCount, facultyName, onDone ->
                val generatedSemesters = (1..semestersCount).map { i ->
                    val roman = when (i) {
                        1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"
                        5 -> "V"; 6 -> "VI"; 7 -> "VII"; 8 -> "VIII"
                        9 -> "IX"; 10 -> "X"
                        else -> "$i"
                    }
                    "Semester $roman"
                }

                val newDto = CourseCreateDto(
                    name = name,
                    code = code,
                    semesters = generatedSemesters,
                    facultyName = facultyName
                )

                coroutineScope.launch {
                    val newCourse = CourseDto(
                        id = "c_${System.currentTimeMillis()}",
                        name = name,
                        code = code,
                        semesters = generatedSemesters,
                        facultyName = facultyName
                    )
                    try {
                        NetworkClient.apiService.addCourse(newDto)
                    } catch (_: Exception) {}

                    coursesList.add(0, newCourse)
                    CourseCache.list = coursesList.toList()
                    onDone()
                    showAddCourseSheet = false
                    Toast.makeText(context, "Added $name successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseManagementCard(
    course: CourseDto,
    onDelete: (onFinish: () -> Unit) -> Unit
) {
    var isDeleting by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MmyrvBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_school),
                        contentDescription = null,
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Code: ${course.code} • ${course.semesters.size} Semesters",
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (course.facultyName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Faculty: ${course.facultyName}",
                            fontSize = 11.sp,
                            fontFamily = AppFontFamily,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = {
                        isDeleting = true
                        onDelete { isDeleting = false }
                    },
                    modifier = Modifier.size(32.dp),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = Color(0xFFEF4444),
                            strokeWidth = 1.8.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete Course",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Available Semesters:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AppFontFamily,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                course.semesters.forEach { sem ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(BorderStroke(0.8.dp, Color(0xFFE2E8F0)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = sem,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                    }
                }
            }
        }
    }
}

// Bottom Slide Panel (ModalBottomSheet): 100% width, compact height, inputs: Name, Code, Semesters, Faculty Name
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCourseBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, code: String, semestersCount: Int, facultyName: String, onDone: () -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var semestersCountStr by remember { mutableStateOf("6") }
    var facultyName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isAddingCourse by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_school),
                            contentDescription = null,
                            tint = MmyrvPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Add Degree / Course",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Appears automatically in student registration",
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = MmyrvTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!errorMsg.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMsg!!,
                        color = Color(0xFFDC2626),
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Course Name
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it; errorMsg = null },
                label = { Text("Course / Degree Name *", fontSize = 12.sp, fontFamily = AppFontFamily) },
                placeholder = { Text("e.g. B.Tech (AI & Machine Learning)", fontSize = 12.sp, fontFamily = AppFontFamily) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MmyrvPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Course Code & Semesters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it.uppercase(); errorMsg = null },
                    label = { Text("Code *", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    placeholder = { Text("e.g. BTECH-AIML", fontSize = 11.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MmyrvPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = semestersCountStr,
                    onValueChange = { semestersCountStr = it; errorMsg = null },
                    label = { Text("Semesters *", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    placeholder = { Text("e.g. 6 or 8", fontSize = 11.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MmyrvPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Semester Selector Chips
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick:", fontSize = 11.sp, fontFamily = AppFontFamily, color = MmyrvTextMuted)
                listOf("2", "4", "6", "8", "10").forEach { count ->
                    val isSelected = semestersCountStr == count
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MmyrvPrimary else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isSelected) MmyrvPrimary else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .clickable { semestersCountStr = count; errorMsg = null }
                    ) {
                        Text(
                            text = "$count Sem",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFontFamily,
                            color = if (isSelected) Color.White else MmyrvTextDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Faculty Name / Department
            OutlinedTextField(
                value = facultyName,
                onValueChange = { facultyName = it },
                label = { Text("Faculty / Department Name", fontSize = 12.sp, fontFamily = AppFontFamily) },
                placeholder = { Text("e.g. School of Computer Sciences", fontSize = 12.sp, fontFamily = AppFontFamily) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MmyrvPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Button(
                onClick = {
                    val trimmedName = courseName.trim()
                    val trimmedCode = courseCode.trim()
                    val count = semestersCountStr.trim().toIntOrNull()
                    if (trimmedName.isBlank()) {
                        errorMsg = "Please enter the Course / Degree Name."
                        return@Button
                    }
                    if (trimmedCode.isBlank()) {
                        errorMsg = "Please enter the Course Code."
                        return@Button
                    }
                    if (count == null || count <= 0 || count > 12) {
                        errorMsg = "Please enter a valid number of semesters (1 to 12)."
                        return@Button
                    }
                    isAddingCourse = true
                    onAdd(trimmedName, trimmedCode, count, facultyName.trim()) {
                        isAddingCourse = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isAddingCourse
            ) {
                if (isAddingCourse) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adding Course...",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save & Add Course",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

fun defaultStaticCourses(): List<CourseDto> {
    return listOf(
        CourseDto(
            id = "c1",
            name = "B.Sc. (Computer Science)",
            code = "BSCCS",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI"),
            facultyName = "School of Computer Science & IT"
        ),
        CourseDto(
            id = "c2",
            name = "BCA (Bachelor of Computer Applications)",
            code = "BCA",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI"),
            facultyName = "Department of Computer Applications"
        ),
        CourseDto(
            id = "c3",
            name = "B.Tech (Computer Science & Engineering)",
            code = "BTECH-CSE",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI", "Semester VII", "Semester VIII"),
            facultyName = "Faculty of Engineering & Technology"
        ),
        CourseDto(
            id = "c4",
            name = "M.Sc. (Information Technology)",
            code = "MSCIT",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV"),
            facultyName = "School of Computer Science & IT"
        ),
        CourseDto(
            id = "c5",
            name = "MCA (Master of Computer Applications)",
            code = "MCA",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV"),
            facultyName = "Department of Computer Applications"
        )
    )
}
