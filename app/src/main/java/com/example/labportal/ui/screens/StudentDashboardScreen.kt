package com.example.labportal.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.components.GreetingHeaderSection
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvBorder
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted

@Composable
fun StudentDashboardContent(
    user: UserData,
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val completionPercentage = user.profileCompletion

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dynamic Time-based Greeting & Current Date Pill
        GreetingHeaderSection(user = user)

        Spacer(modifier = Modifier.height(16.dp))

        // Complete Profile Shape / Progress Card
        // RULE: Visible ONLY until 100% complete. Once 100%, it completely disappears ("gayab ho jaye")!
        if (completionPercentage < 100) {
            ProfileCompletionSection(
                user = user,
                completionPercentage = completionPercentage,
                onCompleteClick = onOpenProfile
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

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
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_school),
                        contentDescription = "Student Badge",
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "MMYRV Student Identity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvPrimary
                    )
                    Text(
                        text = user.fullName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                    Text(
                        text = "${user.programme} • ${user.semester}",
                        fontSize = 12.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted
                    )
                    Text(
                        text = "Roll No: ${user.rollNo}",
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Student Stats
        Text(
            text = "My Lab Activities",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFontFamily,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Booked Slots",
                value = "02",
                subtitle = "Upcoming practicals",
                color = MmyrvPrimary,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Attended",
                value = "14",
                subtitle = "Sessions completed",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Quick Student Services
        Text(
            text = "Quick Services",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AppFontFamily,
            color = MmyrvTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MmyrvBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_science),
                        contentDescription = null,
                        tint = MmyrvPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Book a Computer or Science Lab Slot",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextDark
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your lab, choose an available date and time slot, and get instant system seat allotment.",
                    fontSize = 12.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextMuted,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, "Slot booking opened", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(text = "Book Slot Now", fontSize = 13.sp, fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AdminActionCard(
            title = "My Practical Schedule & Manuals",
            desc = "Download experiment PDFs, lab manuals, and view assigned faculty.",
            iconColor = Color(0xFF8B5CF6)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AdminActionCard(
            title = "Equipment & Kit Requisition",
            desc = "Apply to borrow multimeter, microcontroller kits, or lab components.",
            iconColor = Color(0xFFF59E0B)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Interactive Profile Completion Shape / Card
 * Shows ring progress (e.g. 80% -> 100%) and unlocks verified identity.
 */
@Composable
private fun ProfileCompletionSection(
    user: UserData,
    completionPercentage: Int,
    onCompleteClick: () -> Unit
) {
    val isComplete = completionPercentage >= 100

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) Color(0xFFF0FDF4) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            1.dp,
            if (isComplete) Color(0xFF86EFAC) else Color(0xFFE0E7FF)
        )
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
                // Circular Progress Indicator Shape
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { completionPercentage / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isComplete) Color(0xFF10B981) else MmyrvPrimary,
                        trackColor = if (isComplete) Color(0xFFDCFCE7) else Color(0xFFEEF2FF),
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "$completionPercentage%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = if (isComplete) Color(0xFF15803D) else MmyrvPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isComplete) "100% Profile Complete" else "Complete Your Profile",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(
                                id = if (isComplete) R.drawable.ic_check_circle else R.drawable.ic_badge
                            ),
                            contentDescription = null,
                            tint = if (isComplete) Color(0xFF10B981) else MmyrvPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isComplete)
                            "Verified Student Identity • Facial biometrics active for smart lab attendance."
                        else
                            "Complete emergency contact details to reach 100% and unlock verified identity.",
                        fontSize = 11.5.sp,
                        fontFamily = AppFontFamily,
                        color = MmyrvTextMuted,
                        lineHeight = 15.sp
                    )
                }
            }

            if (!isComplete) {
                Spacer(modifier = Modifier.height(12.dp))

                // Checklist Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileBadgePill(
                        label = "Academic Info",
                        isDone = true,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileBadgePill(
                        label = "Face Biometric",
                        isDone = user.faceEnrolled,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileBadgePill(
                        label = "Emergency Contact",
                        isDone = false,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCompleteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_badge),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Profile (+20%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_face_recognition),
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Automated Biometric Lab Attendance is Enabled",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = AppFontFamily,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileBadgePill(
    label: String,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDone) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(
                    id = if (isDone) R.drawable.ic_check_circle else R.drawable.ic_schedule
                ),
                contentDescription = null,
                tint = if (isDone) Color(0xFF16A34A) else Color(0xFF94A3B8),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                fontFamily = AppFontFamily,
                color = if (isDone) Color(0xFF15803D) else Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun CompleteProfileModal(
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var emergencyContactName by remember { mutableStateOf("Guardian / Parent") }
    var emergencyPhone by remember { mutableStateOf(currentPhone) }
    var bloodGroup by remember { mutableStateOf("B+ Positive") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_badge),
                    contentDescription = null,
                    tint = MmyrvPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete Student Profile",
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
                    text = "Provide essential lab safety & emergency contact details to reach 100% verified profile completion.",
                    fontSize = 12.sp,
                    fontFamily = AppFontFamily,
                    color = MmyrvTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    label = { Text("Guardian / Contact Name", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = { emergencyPhone = it },
                    label = { Text("Emergency Contact Phone", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("Blood Group (Lab Safety)", fontSize = 12.sp, fontFamily = AppFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Save & Complete 100%", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", fontFamily = AppFontFamily, color = MmyrvTextMuted)
            }
        }
    )
}
