package com.example.labportal.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FaceScanDialog(
    user: UserData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scanSuccess by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_face_recognition),
                    contentDescription = null,
                    tint = MmyrvPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Facial Attendance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MmyrvTextDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Smart Biometric AI Verification",
                    fontSize = 12.sp,
                    color = MmyrvTextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Simulated Camera Viewfinder Frame
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            width = 2.dp,
                            color = if (scanSuccess) Color(0xFF10B981) else MmyrvPrimary,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Face silhouette placeholder
                    Icon(
                        painter = painterResource(id = R.drawable.ic_face_recognition),
                        contentDescription = "Face Frame",
                        tint = Color.White.copy(alpha = if (scanSuccess) 0.9f else 0.4f),
                        modifier = Modifier.size(90.dp)
                    )

                    // Animated Laser Scan Line
                    if (isScanning && !scanSuccess) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = scanLineOffset.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF38BDF8),
                                            Color.White,
                                            Color(0xFF38BDF8),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    if (scanSuccess) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_attendance),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        scanSuccess -> "Verified! Attendance marked successfully."
                        isScanning -> "Detecting facial landmarks..."
                        else -> "Align your face in the camera frame to record your practical lab presence."
                    },
                    fontSize = 12.sp,
                    color = if (scanSuccess) Color(0xFF10B981) else MmyrvTextDark,
                    fontWeight = if (scanSuccess) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Student: ${user.fullName} (${user.rollNo})",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        },
        confirmButton = {
            if (!scanSuccess) {
                Button(
                    onClick = {
                        isScanning = true
                        coroutineScope.launch {
                            delay(1800)
                            isScanning = false
                            scanSuccess = true
                            Toast.makeText(context, "Attendance Marked for AI & Robotics Lab Successfully!", Toast.LENGTH_LONG).show()
                            delay(1000)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verifying...", fontSize = 13.sp)
                    } else {
                        Text("Scan My Face", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MmyrvTextMuted)
            }
        }
    )
}

