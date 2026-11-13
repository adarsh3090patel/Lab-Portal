package com.example.labportal.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.labportal.R
import com.example.labportal.network.DeepFaceAttendanceDto
import com.example.labportal.network.DeepFaceEnrollDto
import com.example.labportal.network.NetworkClient
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.Executors

enum class RecognitionState(val label: String, val color: Color) {
    INITIALIZING_CAMERA("Initializing Camera & DeepFace Pipeline...", Color(0xFF64748B)),
    POSITION_YOUR_FACE("Position Your Face Inside Oval Frame", Color(0xFFF59E0B)),
    CAPTURING_FRAME("Capturing High-Resolution Face Frame...", Color(0xFF38BDF8)),
    PROCESSING_FACE("Transmitting Frame to Python DeepFace Server...", Color(0xFF818CF8)),
    MATCHING_IDENTITY("DeepFace Facenet512 Verification in Progress...", Color(0xFF38BDF8)),
    IDENTITY_VERIFIED("Identity Verified (DeepFace Confirmed)", Color(0xFF10B981)),
    UNKNOWN_PERSON("Identity Mismatch: Face does not match registered student", Color(0xFFEF4444)),
    MULTIPLE_FACES_DETECTED("Multiple Faces Detected! Only 1 person permitted", Color(0xFFEF4444)),
    RECOGNITION_FAILED("Recognition Failed. Please try again in good lighting", Color(0xFFEF4444))
}

@Composable
fun LiveFaceScannerModal(
    title: String = "Python DeepFace Biometric Scanner",
    subtitle: String = "Facenet512 • Server-Authoritative Identity Matching",
    registeredBase64: String = "",
    enrolledEmbedding: List<Float>? = null,
    expectedStudentName: String? = null,
    expectedRollNo: String? = null,
    studentId: String? = null,
    subject: String = "Central Laboratory Practical",
    onFaceCaptured: ((bitmap: Bitmap, base64: String) -> Unit)? = null,
    onFaceVerified: ((bitmap: Bitmap, base64: String, embedding: FloatArray, similarityScore: Float) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Camera permission check
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required for face recognition", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var recognitionState by remember { mutableStateOf(RecognitionState.INITIALIZING_CAMERA) }
    var statusMessage by remember { mutableStateOf(recognitionState.label) }
    var isProcessingFrame by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var progressFraction by remember { mutableStateOf(0f) }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val animatedFrameColor by animateColorAsState(
        targetValue = recognitionState.color,
        animationSpec = tween(350),
        label = "frameColor"
    )

    // Animated Scan Line Laser
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    // Function to trigger DeepFace server verification
    fun sendFrameToDeepFace(bitmap: Bitmap) {
        if (isProcessingFrame || isCompleted) return
        isProcessingFrame = true
        recognitionState = RecognitionState.PROCESSING_FACE
        statusMessage = "Processing with Python DeepFace Facenet512..."
        progressFraction = 0.5f

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val base64 = FaceDetectionHelper.bitmapToBase64(bitmap, quality = 85, maxDimension = 640)
                if (base64.isBlank()) {
                    withContext(Dispatchers.Main) {
                        isProcessingFrame = false
                        recognitionState = RecognitionState.RECOGNITION_FAILED
                        statusMessage = "Failed to capture camera frame. Please try again."
                    }
                    return@launch
                }

                val effectiveRollNo = expectedRollNo ?: ""
                val effectiveStudentId = studentId ?: effectiveRollNo

                // Check mode: Attendance Verification Mode vs Face Enrollment Mode
                val isAttendanceMode = effectiveRollNo.isNotBlank() || effectiveStudentId.isNotBlank() || registeredBase64.isNotBlank()

                if (isAttendanceMode && effectiveStudentId.isNotBlank()) {
                    // 1. RECOGNITION & ATTENDANCE MODE (Verified by Python DeepFace)
                    withContext(Dispatchers.Main) {
                        recognitionState = RecognitionState.MATCHING_IDENTITY
                        statusMessage = "Matching live face against enrolled DeepFace template..."
                        progressFraction = 0.75f
                    }

                    val req = DeepFaceAttendanceDto(
                        studentId = effectiveStudentId,
                        rollNo = effectiveRollNo,
                        subject = subject,
                        imageBase64 = base64
                    )

                    val response = NetworkClient.apiService.verifyAttendanceDeepFace(req)

                    withContext(Dispatchers.Main) {
                        isProcessingFrame = false
                        if (response.isSuccessful && response.body() != null) {
                            val resBody = response.body()!!
                            isCompleted = true
                            progressFraction = 1.0f
                            recognitionState = RecognitionState.IDENTITY_VERIFIED
                            statusMessage = resBody.message

                            onFaceVerified?.invoke(bitmap, base64, FloatArray(512), resBody.attendance?.similarityScore ?: 0.95f)
                            onFaceCaptured?.invoke(bitmap, base64)

                            // Auto-dismiss after brief success feedback
                            coroutineScope.launch {
                                delay(850)
                                onDismiss()
                            }
                        } else {
                            val errStr = response.errorBody()?.string() ?: "Verification rejected"
                            val cleanMsg = try {
                                JSONObject(errStr).optString("detail", errStr)
                            } catch (_: Exception) { errStr }

                            if (cleanMsg.contains("already recorded", ignoreCase = true)) {
                                isCompleted = true
                                progressFraction = 1.0f
                                recognitionState = RecognitionState.IDENTITY_VERIFIED
                                statusMessage = cleanMsg
                                onFaceVerified?.invoke(bitmap, base64, FloatArray(512), 1.0f)
                                onFaceCaptured?.invoke(bitmap, base64)
                                coroutineScope.launch {
                                    delay(950)
                                    onDismiss()
                                }
                            } else if (cleanMsg.contains("Multiple faces", ignoreCase = true)) {
                                recognitionState = RecognitionState.MULTIPLE_FACES_DETECTED
                                statusMessage = cleanMsg
                            } else if (cleanMsg.contains("No human face", ignoreCase = true)) {
                                recognitionState = RecognitionState.POSITION_YOUR_FACE
                                statusMessage = cleanMsg
                            } else {
                                recognitionState = RecognitionState.UNKNOWN_PERSON
                                statusMessage = cleanMsg
                            }
                        }
                    }
                } else {
                    // 2. ENROLLMENT / REGISTRATION MODE
                    withContext(Dispatchers.Main) {
                        recognitionState = RecognitionState.PROCESSING_FACE
                        statusMessage = "DeepFace analyzing 512D facial landmarks..."
                        progressFraction = 0.8f
                    }

                    if (effectiveStudentId.isNotBlank()) {
                        val enrollReq = DeepFaceEnrollDto(
                            userId = effectiveStudentId,
                            rollNo = effectiveRollNo,
                            imageBase64 = base64
                        )
                        val res = NetworkClient.apiService.enrollStudentDeepFace(enrollReq)
                        withContext(Dispatchers.Main) {
                            isProcessingFrame = false
                            if (res.isSuccessful && res.body() != null) {
                                isCompleted = true
                                progressFraction = 1.0f
                                recognitionState = RecognitionState.IDENTITY_VERIFIED
                                statusMessage = res.body()!!.message

                                onFaceCaptured?.invoke(bitmap, base64)
                                onFaceVerified?.invoke(bitmap, base64, FloatArray(512), 1.0f)
                                coroutineScope.launch {
                                    delay(850)
                                    onDismiss()
                                }
                            } else {
                                val err = res.errorBody()?.string() ?: "Enrollment failed"
                                val clean = try { JSONObject(err).optString("detail", err) } catch (_: Exception) { err }
                                recognitionState = RecognitionState.RECOGNITION_FAILED
                                statusMessage = clean
                            }
                        }
                    } else {
                        // Registration mode prior to account creation (captures frame for register request)
                        withContext(Dispatchers.Main) {
                            isProcessingFrame = false
                            isCompleted = true
                            progressFraction = 1.0f
                            recognitionState = RecognitionState.IDENTITY_VERIFIED
                            statusMessage = "Face Captured • Ready for Python DeepFace Enrollment"

                            onFaceCaptured?.invoke(bitmap, base64)
                            onFaceVerified?.invoke(bitmap, base64, FloatArray(512), 1.0f)
                            coroutineScope.launch {
                                delay(850)
                                onDismiss()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isProcessingFrame = false
                    recognitionState = RecognitionState.RECOGNITION_FAILED
                    statusMessage = "Network error: ${e.localizedMessage ?: "Connection failure"}"
                }
            }
        }
    }

    // Auto-capture trigger once camera preview is active
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            delay(1200) // Allow camera exposure to settle
            recognitionState = RecognitionState.POSITION_YOUR_FACE
            statusMessage = "Position your face inside the oval frame"

            // Automatic scan trigger
            delay(1000)
            if (!isCompleted && !isProcessingFrame) {
                previewViewRef?.bitmap?.let { bmp ->
                    sendFrameToDeepFace(bmp)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextDark
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            color = MmyrvTextMuted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = MmyrvTextDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Camera Viewfinder Box with Biometric Target Oval
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!hasCameraPermission) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Camera Permission Required",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary)
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    } else {
                        // CameraX PreviewView
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                previewViewRef = previewView

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        imageProxy.close()
                                    }

                                    val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        statusMessage = e.localizedMessage ?: "Camera binding failed"
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            }
                        )

                        // Biometric Target Oval Frame
                        Box(
                            modifier = Modifier
                                .size(width = 190.dp, height = 240.dp)
                                .clip(RoundedCornerShape(95.dp))
                                .border(
                                    width = if (isCompleted) 3.5.dp else 2.5.dp,
                                    color = animatedFrameColor,
                                    shape = RoundedCornerShape(95.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset(y = laserYOffset.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                animatedFrameColor,
                                                animatedFrameColor,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        // Reticle Brackets
                        Box(
                            modifier = Modifier
                                .size(220.dp, 260.dp)
                                .border(1.dp, animatedFrameColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                if (progressFraction > 0f) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFDCFCE7)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Status Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(animatedFrameColor.copy(alpha = 0.12f))
                        .border(1.dp, animatedFrameColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isProcessingFrame) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = animatedFrameColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = statusMessage,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = if (recognitionState == RecognitionState.IDENTITY_VERIFIED) Color(0xFF15803D) else animatedFrameColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Capture Button / Complete State
                if (isCompleted) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_circle),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verified Automatically • Success!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextMuted
                            )
                        }

                        Button(
                            onClick = {
                                previewViewRef?.bitmap?.let { bmp ->
                                    sendFrameToDeepFace(bmp)
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                            enabled = !isProcessingFrame && hasCameraPermission
                        ) {
                            if (isProcessingFrame) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verifying...", fontSize = 12.sp, color = Color.White, fontFamily = AppFontFamily)
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Face Now", fontSize = 12.5.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = AppFontFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}
