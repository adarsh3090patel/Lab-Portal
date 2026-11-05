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
fun TodaysLabContent(
    user: UserData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Today's Date Banner
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
                        Text(text = "Today's Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MmyrvTextDark)
                        Text(text = "Live Active Lab Sessions", fontSize = 12.sp, color = MmyrvTextMuted)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF15803D))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "4 LIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Ongoing & Upcoming Practical Slots",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabSessionCard(
            labName = "AI & Machine Learning Lab 2",
            time = "10:00 AM - 12:00 PM",
            batch = "B.Tech CSE - Batch A",
            status = "Ongoing",
            statusColor = Color(0xFF10B981),
            students = "36/40 Present"
        )

        Spacer(modifier = Modifier.height(10.dp))

        LabSessionCard(
            labName = "VLSI & Digital Electronics Lab",
            time = "01:30 PM - 03:30 PM",
            batch = "B.Tech ECE - Batch B",
            status = "Upcoming",
            statusColor = MmyrvPrimary,
            students = "32 Registered"
        )

        Spacer(modifier = Modifier.height(10.dp))

        LabSessionCard(
            labName = "Organic Chemistry Synthesis Lab",
            time = "03:30 PM - 05:30 PM",
            batch = "B.Sc Chemistry - Year 2",
            status = "Scheduled",
            statusColor = Color(0xFFF59E0B),
            students = "24 Registered"
        )
    }
}

@Composable
fun LabSessionCard(
    labName: String,
    time: String,
    batch: String,
    status: String,
    statusColor: Color,
    students: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MmyrvBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = labName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MmyrvTextDark)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_schedule),
                    contentDescription = null,
                    tint = MmyrvTextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = time, fontSize = 12.sp, color = MmyrvTextMuted)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_group),
                    contentDescription = null,
                    tint = MmyrvTextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = batch, fontSize = 12.sp, color = MmyrvTextMuted)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = students, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
        }
    }
}

