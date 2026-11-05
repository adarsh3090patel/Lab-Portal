package com.example.labportal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.ui.components.BackgroundCurves
import com.example.labportal.ui.components.PortalHeader
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun RegistrationPendingScreen(
    fullName: String,
    email: String,
    rollNo: String,
    role: String = "Student",
    programme: String = "B.Sc. (Computer Science)",
    semester: String = "Semester V",
    phone: String = "+91 98765 43210",
    onBackToLogin: () -> Unit
) {
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
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // University Branding Header
            PortalHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Main Submission Confirmation Card (matches uploaded mockup)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MmyrvBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Document Icon with Green Check Badge
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check_circle),
                            contentDescription = "Success",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Registration Submitted",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your registration has been received and is waiting for admin approval.",
                        fontSize = 13.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Registration Details Table
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            DetailRow(iconRes = R.drawable.ic_person, label = "Name", value = fullName)
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                            DetailRow(iconRes = R.drawable.ic_school, label = "Programme", value = if (role.equals("Student", ignoreCase = true)) programme else role)
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                            DetailRow(iconRes = R.drawable.ic_resources, label = "Semester", value = semester)
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                            DetailRow(iconRes = R.drawable.ic_badge, label = "Enrollment No.", value = rollNo)
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                            DetailRow(iconRes = R.drawable.ic_phone, label = "Phone", value = phone)
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                            DetailRow(iconRes = R.drawable.ic_face_recognition, label = "Face Biometric", value = "Enrolled")
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                            DetailRow(iconRes = R.drawable.ic_email, label = "Email", value = email)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pending Admin Approval Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(BorderStroke(1.dp, Color(0xFFFDE68A)), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFDE68A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_schedule),
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pending Admin Approval",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = Color(0xFF92400E)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "You will be notified on your email once your account is approved.",
                                    fontSize = 12.sp,
                                    fontFamily = AppFontFamily,
                                    color = Color(0xFFB45309),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Back to Login Button
                    Button(
                        onClick = onBackToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Back to Login",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MmyrvTextMuted,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontFamily = AppFontFamily,
                color = MmyrvTextMuted
            )
        }
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFontFamily,
            color = MmyrvTextDark,
            maxLines = 1
        )
    }
}

