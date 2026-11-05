package com.example.labportal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.components.BackgroundCurves
import com.example.labportal.ui.theme.LabPortalTheme
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun DashboardScreen(
    user: UserData,
    onLogoutClick: () -> Unit = {}
) {
    val isAdmin = user.role.equals("Admin", ignoreCase = true)
    val roleBadgeBg = if (isAdmin) Color(0xFFFEF3C7) else Color(0xFFEEF2FF)
    val roleBadgeText = if (isAdmin) Color(0xFFB45309) else MmyrvPrimary
    val roleIconRes = if (isAdmin) R.drawable.ic_shield else R.drawable.ic_school

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BackgroundCurves()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Mini MMYRV Portal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.mmyrv_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MMYRV Lab Portal",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Maharishi Mahesh Yogi Ramayan Univ.",
                            fontSize = 11.sp,
                            color = MmyrvTextMuted
                        )
                    }
                }

                // Logout icon button
                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEF2F2)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout),
                        contentDescription = "Logout",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Logout",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Main Status & Role Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MmyrvBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Role Icon Circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(color = roleBadgeBg, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = roleIconRes),
                            contentDescription = user.role,
                            tint = roleBadgeText,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent Role Tag
                    Box(
                        modifier = Modifier
                            .background(color = roleBadgeBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ROLE: ${user.role.uppercase()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = roleBadgeText,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Full Name
                    Text(
                        text = user.fullName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MmyrvTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // User Email
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = MmyrvTextMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Roll No / ID
                    Text(
                        text = "ID: ${user.rollNo}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status Message Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isAdmin) "⚡ Administrator Access" else "🎓 Student Portal Access",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MmyrvTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAdmin)
                                    "You are logged in as Admin. You have full access to manage labs, approval requests, and inventory."
                                else
                                    "You are logged in as Student. You can view lab schedules, book lab slots, and check experiment materials.",
                                fontSize = 12.sp,
                                color = MmyrvTextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Big Logout Button
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardAdminPreview() {
    LabPortalTheme {
        DashboardScreen(
            user = UserData(
                fullName = "MMYRV Lab Admin",
                email = "admin@mmyrv.edu.in",
                rollNo = "STAFF-ADM-01",
                role = "Admin"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardStudentPreview() {
    LabPortalTheme {
        DashboardScreen(
            user = UserData(
                fullName = "Adarsh Sharma",
                email = "student@mmyrv.edu.in",
                rollNo = "MMYRV/2026/042",
                role = "Student"
            )
        )
    }
}

