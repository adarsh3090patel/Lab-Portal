package com.example.labportal.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labportal.R
import com.example.labportal.data.model.UserData
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
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
    var realCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var verificationText by remember { mutableStateOf<String?>(null) }
    var verificationError by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            isScanning = true
            realCapturedBitmap = bitmap
            coroutineScope.launch {
                val matchResult = FaceDetectionHelper.verifyFaceMatch(bitmap, user.facePhotoUrl)
                isScanning = false
                if (matchResult.isMatch) {
                    scanSuccess = true
                    verificationError = null
                    verificationText = matchResult.message
                    Toast.makeText(context, "Attendance Marked for AI & Robotics Lab Successfully!", Toast.LENGTH_LONG).show()
                } else {
                    scanSuccess = false
                    verificationError = matchResult.message
                    Toast.makeText(context, matchResult.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
                    fontFamily = AppFontFamily,
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
                    fontFamily = AppFontFamily,
                    color = MmyrvTextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Camera Viewfinder Frame
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
                    if (realCapturedBitmap != null) {
                        Image(
                            bitmap = realCapturedBitmap!!.asImageBitmap(),
                            contentDescription = "Real Captured Face",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Face silhouette placeholder
                        Icon(
                            painter = painterResource(id = R.drawable.ic_face_recognition),
                            contentDescription = "Face Frame",
                            tint = Color.White.copy(alpha = if (scanSuccess) 0.9f else 0.4f),
                            modifier = Modifier.size(90.dp)
                        )
                    }

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
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        scanSuccess -> verificationText ?: "Verified! Attendance marked successfully."
                        verificationError != null -> verificationError!!
                        isScanning -> "Analyzing AI biometric landmarks & matching face..."
                        else -> "Point camera at your face with eyes open to mark practical lab presence."
                    },
                    fontSize = 12.sp,
                    fontFamily = AppFontFamily,
                    color = when {
                        scanSuccess -> Color(0xFF10B981)
                        verificationError != null -> Color(0xFFEF4444)
                        else -> MmyrvTextDark
                    },
                    fontWeight = if (scanSuccess || verificationError != null) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Student: ${user.fullName} (${user.rollNo})",
                    fontSize = 11.sp,
                    fontFamily = AppFontFamily,
                    color = Color(0xFF94A3B8)
                )
            }
        },
        confirmButton = {
            if (!scanSuccess) {
                Button(
                    onClick = {
                        try {
                            takePictureLauncher.launch(null)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verifying...", fontSize = 12.sp, fontFamily = AppFontFamily)
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_face_recognition),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (verificationError != null) "Retry Face Verification" else "Open Camera & Verify Face",
                            fontSize = 12.5.sp,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = AppFontFamily, color = MmyrvTextMuted)
            }
        }
    )
}
