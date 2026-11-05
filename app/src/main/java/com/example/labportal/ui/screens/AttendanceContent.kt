package com.example.labportal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun AttendanceContent(
    user: UserData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Attendance Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MmyrvBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Practical Attendance",
                            fontSize = 13.sp,
                            color = MmyrvTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "88.5%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1FAE5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_attendance),
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = 0.885f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Eligibility: Eligible for End-Semester Practical Exams (>75%)",
                    fontSize = 11.sp,
                    color = Color(0xFF059669),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lab Subjects
        Text(
            text = "Subject-Wise Lab Log",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        SubjectAttendanceCard(subject = "Computer Networks & IoT Lab", attended = "12/14", percentage = "86%", faculty = "Dr. R. Sharma")
        Spacer(modifier = Modifier.height(10.dp))
        SubjectAttendanceCard(subject = "Microcontroller & Embedded Systems", attended = "10/10", percentage = "100%", faculty = "Prof. V. Gupta")
        Spacer(modifier = Modifier.height(10.dp))
        SubjectAttendanceCard(subject = "Database Management System Lab", attended = "13/15", percentage = "87%", faculty = "Dr. A. Verma")
        Spacer(modifier = Modifier.height(10.dp))
        SubjectAttendanceCard(subject = "Advanced Physics Optics Lab", attended = "08/10", percentage = "80%", faculty = "Prof. S. Pandey")
    }
}

@Composable
fun SubjectAttendanceCard(
    subject: String,
    attended: String,
    percentage: String,
    faculty: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MmyrvBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = subject, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MmyrvTextDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Instructor: $faculty", fontSize = 12.sp, color = MmyrvTextMuted)
                Text(text = "Sessions: $attended", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEEF2FF))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = percentage, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MmyrvPrimary)
            }
        }
    }
}

