package com.example.labportal.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBadgeBg
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun SettingsContent(
    user: UserData,
    onLogoutClick: () -> Unit
) {
    val isAdmin = user.role.equals("Admin", ignoreCase = true)
    var notificationEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(true) }
    var showFaceTestScan by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Digital MMYRV Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MmyrvBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header with Avatar & Role
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(if (isAdmin) Color(0xFFFEF3C7) else MmyrvBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (isAdmin) R.drawable.ic_shield else R.drawable.ic_school),
                            contentDescription = null,
                            tint = if (isAdmin) Color(0xFFD97706) else MmyrvPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isAdmin) Color(0xFFFEF3C7) else Color(0xFFEEF2FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = user.role.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = if (isAdmin) Color(0xFFD97706) else MmyrvPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ID: ${user.rollNo}",
                                fontSize = 11.5.sp,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextMuted
                            )
                        }
                    }

                    // Profile Completion Ring Shape
                    Box(
                        modifier = Modifier.size(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1.0f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFFDCFCE7),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "100%",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Academic & Student Details Grid
                Text(
                    text = "Academic & Lab Credentials",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        StudentDetailItem(label = "Programme", value = user.programme, iconRes = R.drawable.ic_school)
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                        StudentDetailItem(label = "Semester", value = user.semester, iconRes = R.drawable.ic_resources)
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                        StudentDetailItem(label = "Department", value = user.department, iconRes = R.drawable.ic_science)
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                        StudentDetailItem(label = "Lab Batch", value = user.batch, iconRes = R.drawable.ic_group)
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                        StudentDetailItem(label = "Academic Year", value = user.academicYear, iconRes = R.drawable.ic_calendar_today)
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                        StudentDetailItem(label = "Contact Phone", value = user.phone, iconRes = R.drawable.ic_phone)
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                        StudentDetailItem(label = "University Email", value = user.email, iconRes = R.drawable.ic_email)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Facial Recognition Biometric Status Card
        Text(
            text = "Biometric Face Recognition",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFontFamily,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_face_recognition),
                            contentDescription = "Face ID",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Facial Attendance System",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Active & Verified",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Face registered on enrollment. Used for practical lab room biometric check-in.",
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { showFaceTestScan = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MmyrvPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MmyrvPrimary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_face_recognition),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Facial Attendance Scanner",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = AppFontFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // App Preferences
        Text(
            text = "Preferences",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFontFamily,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                    Column {
                        Text(
                            text = "Lab Notifications",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Get practical session slot alerts",
                            fontSize = 11.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MmyrvPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Facial Attendance Quick-Mark",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = "Allow biometric camera scan on arrival",
                            fontSize = 11.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MmyrvPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_logout),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign Out",
                fontWeight = FontWeight.Bold,
                fontFamily = AppFontFamily,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Interactive Face Scan Modal
    if (showFaceTestScan) {
        FaceScanDialog(
            user = user,
            onDismiss = { showFaceTestScan = false }
        )
    }
}

@Composable
private fun StudentDetailItem(
    label: String,
    value: String,
    iconRes: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MmyrvPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = AppFontFamily,
                color = MmyrvTextMuted
            )
        }

        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = AppFontFamily,
            color = MmyrvTextDark
        )
    }
}
