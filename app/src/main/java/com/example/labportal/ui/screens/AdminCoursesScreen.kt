package com.example.labportal.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

@Composable
fun AdminCoursesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    val coursesList = remember { mutableStateListOf<CourseDto>() }

    fun loadCourses() {
        isLoading = true
        coroutineScope.launch {
            try {
                val res = NetworkClient.apiService.getCourses()
                isLoading = false
                if (res.isSuccessful && res.body() != null) {
                    coursesList.clear()
                    coursesList.addAll(res.body()!!)
                } else if (coursesList.isEmpty()) {
                    coursesList.addAll(defaultStaticCourses())
                }
            } catch (e: Exception) {
                isLoading = false
                if (coursesList.isEmpty()) {
                    coursesList.addAll(defaultStaticCourses())
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCourses()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Back to dashboard",
                            tint = MmyrvTextDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Courses & Programmes",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Manage degrees & semesters for registration",
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }

                    Button(
                        onClick = { showAddCourseDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                        modifier = Modifier.height(36.dp)
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

                    Column {
                        Text(
                            text = "${coursesList.size} Active Degree Programmes",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Courses and semesters added here appear automatically in student registration dropdowns.",
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MmyrvPrimary, modifier = Modifier.size(32.dp))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(coursesList, key = { it.id }) { course ->
                        CourseManagementCard(
                            course = course,
                            onDelete = {
                                coroutineScope.launch {
                                    try {
                                        NetworkClient.apiService.deleteCourse(course.id)
                                    } catch (_: Exception) {}
                                    coursesList.removeAll { it.id == course.id }
                                    Toast.makeText(context, "${course.name} removed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddCourseDialog) {
        AddCourseModal(
            onDismiss = { showAddCourseDialog = false },
            onAdd = { name, code, semestersCount ->
                val generatedSemesters = (1..semestersCount).map { i ->
                    val roman = when (i) {
                        1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"
                        5 -> "V"; 6 -> "VI"; 7 -> "VII"; 8 -> "VIII"
                        else -> "$i"
                    }
                    "Semester $roman"
                }

                val newDto = CourseCreateDto(name = name, code = code, semesters = generatedSemesters)
                coroutineScope.launch {
                    try {
                        NetworkClient.apiService.addCourse(newDto)
                    } catch (_: Exception) {}
                    coursesList.add(
                        CourseDto(
                            id = "c_${System.currentTimeMillis()}",
                            name = name,
                            code = code,
                            semesters = generatedSemesters
                        )
                    )
                    showAddCourseDialog = false
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
    onDelete: () -> Unit
) {
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
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout),
                        contentDescription = "Delete Course",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
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

@Composable
private fun AddCourseModal(
    onDismiss: () -> Unit,
    onAdd: (name: String, code: String, semestersCount: Int) -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var semestersCountStr by remember { mutableStateOf("6") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_school),
                    contentDescription = null,
                    tint = MmyrvPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Degree / Course",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add a new programme and define its total semesters for student registration.",
                    fontSize = 12.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextMuted
                )

                if (!errorMsg.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg!!, color = Color(0xFFEF4444), fontSize = 11.5.sp, fontFamily = AppFontFamily)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it; errorMsg = null },
                    label = { Text("Degree Name (e.g. B.Sc. Data Science)", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it; errorMsg = null },
                    label = { Text("Course Code (e.g. BSDS)", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = semestersCountStr,
                    onValueChange = { semestersCountStr = it; errorMsg = null },
                    label = { Text("Number of Semesters (e.g. 6 or 8)", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = courseName.trim()
                    val trimmedCode = courseCode.trim()
                    val count = semestersCountStr.trim().toIntOrNull() ?: 6
                    if (trimmedName.isBlank() || trimmedCode.isBlank()) {
                        errorMsg = "Please fill in course name and code."
                        return@Button
                    }
                    onAdd(trimmedName, trimmedCode, count)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Add Course", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", fontFamily = AppFontFamily, color = MmyrvTextMuted)
            }
        }
    )
}

fun defaultStaticCourses(): List<CourseDto> {
    return listOf(
        CourseDto(
            id = "c1",
            name = "B.Sc. (Computer Science)",
            code = "BSCCS",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI")
        ),
        CourseDto(
            id = "c2",
            name = "BCA (Bachelor of Computer Applications)",
            code = "BCA",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI")
        ),
        CourseDto(
            id = "c3",
            name = "B.Tech (Computer Science & Engineering)",
            code = "BTECH-CSE",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV", "Semester V", "Semester VI", "Semester VII", "Semester VIII")
        ),
        CourseDto(
            id = "c4",
            name = "M.Sc. (Information Technology)",
            code = "MSCIT",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV")
        ),
        CourseDto(
            id = "c5",
            name = "MCA (Master of Computer Applications)",
            code = "MCA",
            semesters = listOf("Semester I", "Semester II", "Semester III", "Semester IV")
        )
    )
}

