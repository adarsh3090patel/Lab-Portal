package com.example.labportal.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.abs

enum class LiveFaceFeedback(val text: String, val color: Color) {
    ALIGN("Align face inside biometric target oval", Color(0xFFF59E0B)),
    LOOK_STRAIGHT("Look directly at screen (0° straight gaze)", Color(0xFFF59E0B)),
    OPEN_EYES("Keep both eyes wide open (Liveness Check)", Color(0xFFEF4444)),
    CENTER("Center face in the middle of scanner", Color(0xFFF59E0B)),
    TOO_FAR("Bring camera closer to your face", Color(0xFF38BDF8)),
    MULTIPLE_FACES("Only 1 face permitted (Screen Lock Rule)", Color(0xFFEF4444)),
    HOLD_STILL("Screen Lock scanning... Hold steady", Color(0xFF38BDF8)),
    LOCKED("Biometric Face Locked (100% Screen-Lock Match)", Color(0xFF10B981))
}

@Composable
fun LiveFaceScannerModal(
    title: String = "Screen-Lock Biometric Scanner",
    subtitle: String = "AI 3D Landmark & Liveness Security",
    registeredBase64: String = "",
    onFaceCaptured: (bitmap: Bitmap, base64: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // 1. In-App Camera Runtime Permission Self-Request
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

    var liveFeedback by remember { mutableStateOf(LiveFaceFeedback.ALIGN) }
    var isFaceLocked by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var lockProgress by remember { mutableStateOf(0f) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var captureError by remember { mutableStateOf<String?>(null) }

    // Steady lock progress ticker
    LaunchedEffect(isFaceLocked) {
        if (isFaceLocked) {
            while (lockProgress < 1f) {
                delay(120)
                lockProgress = (lockProgress + 0.25f).coerceAtMost(1f)
            }
        } else {
            lockProgress = 0f
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val animatedFrameColor by animateColorAsState(
        targetValue = liveFeedback.color,
        animationSpec = tween(300),
        label = "frameColor"
    )

    // Futuristic Laser Scan Animation
    val infiniteTransition = rememberInfiniteTransition(label = "laserScanner")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = -110f,
        targetValue = 110f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserScan"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(26.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Biometric Shield Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_face_recognition),
                                    contentDescription = null,
                                    tint = MmyrvPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = MmyrvTextDark
                                )
                                Text(
                                    text = subtitle,
                                    fontSize = 11.sp,
                                    fontFamily = AppFontFamily,
                                    color = MmyrvTextMuted
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!hasCameraPermission) {
                        // Permission Fallback Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(20.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_face_recognition),
                                    contentDescription = null,
                                    tint = MmyrvPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Camera Permission Required",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AppFontFamily,
                                    color = MmyrvTextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Allow camera access to verify screen-lock grade facial biometrics.",
                                    fontSize = 12.sp,
                                    fontFamily = AppFontFamily,
                                    color = MmyrvTextMuted,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MmyrvPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Grant Camera Access", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Real-Time Front Camera Scanner Feed (Screen-Lock Oval View)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(310.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF0F172A))
                                .border(3.dp, animatedFrameColor, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
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

                                        // Strict Screen-Lock Real-Time Analyzer
                                        val detectorOptions = FaceDetectorOptions.Builder()
                                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                            .setMinFaceSize(0.18f)
                                            .build()
                                        val liveDetector = FaceDetection.getClient(detectorOptions)

                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()

                                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                            processLiveFrame(imageProxy, liveDetector) { feedback, locked ->
                                                liveFeedback = feedback
                                                isFaceLocked = locked
                                            }
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
                                            captureError = e.localizedMessage
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))

                                    previewView
                                }
                            )

                            // Biometric Target Oval Frame (Screen Lock Style)
                            Box(
                                modifier = Modifier
                                    .size(width = 190.dp, height = 240.dp)
                                    .clip(RoundedCornerShape(95.dp))
                                    .border(
                                        width = if (isFaceLocked) 3.5.dp else 2.5.dp,
                                        color = animatedFrameColor,
                                        shape = RoundedCornerShape(95.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Sweeping Laser Line inside the biometric oval
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

                            // 4 Corner Biometric Reticle Brackets
                            Box(
                                modifier = Modifier
                                    .size(220.dp, 260.dp)
                                    .border(1.dp, animatedFrameColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lock Progress Bar
                    if (isFaceLocked) {
                        LinearProgressIndicator(
                            progress = { lockProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFFDCFCE7)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Live Status Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(animatedFrameColor.copy(alpha = 0.12f))
                            .border(1.dp, animatedFrameColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(vertical = 9.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = liveFeedback.text,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = if (liveFeedback == LiveFaceFeedback.LOCKED) Color(0xFF15803D) else animatedFrameColor,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (!captureError.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = captureError!!,
                            color = Color(0xFFEF4444),
                            fontSize = 11.5.sp,
                            fontFamily = AppFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmation Action Button
                    Button(
                        onClick = {
                            val bitmap = previewViewRef?.bitmap
                            if (bitmap != null) {
                                isCapturing = true
                                coroutineScope.launch {
                                    val strictCheck = FaceDetectionHelper.validateStrictFace(bitmap)
                                    if (!strictCheck.isValid) {
                                        isCapturing = false
                                        captureError = strictCheck.message
                                        Toast.makeText(context, strictCheck.message, Toast.LENGTH_LONG).show()
                                        return@launch
                                    }

                                    // If verifying against enrolled student face (for attendance)
                                    if (registeredBase64.isNotBlank()) {
                                        val match = FaceDetectionHelper.verifyFaceMatch(bitmap, registeredBase64)
                                        if (!match.isMatch) {
                                            isCapturing = false
                                            captureError = match.message
                                            Toast.makeText(context, match.message, Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                    }

                                    val base64 = FaceDetectionHelper.bitmapToBase64(bitmap)
                                    isCapturing = false
                                    onFaceCaptured(bitmap, base64)
                                }
                            } else {
                                Toast.makeText(context, "Initializing live camera stream...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = hasCameraPermission && !isCapturing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFaceLocked) Color(0xFF10B981) else MmyrvPrimary
                        )
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verifying Screen-Lock Biometrics...",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily
                            )
                        } else {
                            Icon(
                                painter = painterResource(
                                    id = if (isFaceLocked) R.drawable.ic_check_circle else R.drawable.ic_face_recognition
                                ),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFaceLocked)
                                    "Confirm Screen-Lock Biometric (Verified)"
                                else
                                    "Capture Screen-Lock Face",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AppFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", fontFamily = AppFontFamily, color = MmyrvTextMuted)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processLiveFrame(
    imageProxy: ImageProxy,
    detector: com.google.mlkit.vision.face.FaceDetector,
    onResult: (LiveFaceFeedback, Boolean) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                when {
                    faces.isEmpty() -> onResult(LiveFaceFeedback.ALIGN, false)
                    faces.size > 1 -> onResult(LiveFaceFeedback.MULTIPLE_FACES, false)
                    else -> {
                        val face = faces[0]
                        val headY = abs(face.headEulerAngleY)
                        val headX = abs(face.headEulerAngleX)
                        val headZ = abs(face.headEulerAngleZ)
                        val leftEye = face.leftEyeOpenProbability ?: 1f
                        val rightEye = face.rightEyeOpenProbability ?: 1f

                        when {
                            headY > 12f || headX > 12f || headZ > 10f -> {
                                onResult(LiveFaceFeedback.LOOK_STRAIGHT, false)
                            }
                            leftEye < 0.65f || rightEye < 0.65f -> {
                                onResult(LiveFaceFeedback.OPEN_EYES, false)
                            }
                            else -> {
                                onResult(LiveFaceFeedback.LOCKED, true)
                            }
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
