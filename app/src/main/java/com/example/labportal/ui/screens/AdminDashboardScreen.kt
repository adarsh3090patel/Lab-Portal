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
import com.example.labportal.ui.components.GreetingHeaderSection
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun AdminDashboardContent(
    user: UserData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dynamic Time-based Greeting & Current Date Pill
        GreetingHeaderSection(user = user)

        Spacer(modifier = Modifier.height(18.dp))

        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MmyrvBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield),
                        contentDescription = "Admin Badge",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Portal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                    Text(
                        text = user.fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MmyrvTextDark
                    )
                    Text(
                        text = "ID: ${user.rollNo}",
                        fontSize = 12.sp,
                        color = MmyrvTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Stats Grid
        Text(
            text = "Campus Lab Overview",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Total Labs",
                value = "12",
                subtitle = "Active across campus",
                color = MmyrvPrimary,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Live Bookings",
                value = "48",
                subtitle = "Slots reserved today",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Equipment",
                value = "340",
                subtitle = "Logged in inventory",
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Requests",
                value = "07",
                subtitle = "Pending approvals",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Admin Management Actions
        Text(
            text = "Management & Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        AdminActionCard(
            title = "Manage Labs & Timetable",
            desc = "Configure lab capacities, allocate batch timings and slot rules.",
            iconColor = MmyrvPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AdminActionCard(
            title = "Equipment & Inventory",
            desc = "Track oscilloscopes, computers, microscopes and chemicals.",
            iconColor = Color(0xFF10B981)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AdminActionCard(
            title = "Student Requisitions & Approvals",
            desc = "Review and approve student slot bookings and material requests.",
            iconColor = Color(0xFFF59E0B)
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MmyrvBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, fontSize = 12.sp, color = MmyrvTextMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun AdminActionCard(
    title: String,
    desc: String,
    iconColor: Color
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(iconColor)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MmyrvTextDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontSize = 12.sp, color = MmyrvTextMuted, lineHeight = 16.sp)
            }
        }
    }
}

