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
import com.example.labportal.ai.FaceRecognitionEngine
import com.example.labportal.ai.LivenessDetector
import com.example.labportal.ai.LivenessState
import com.example.labportal.ui.theme.AppFontFamily
import com.example.labportal.ui.theme.MmyrvPrimary
import com.example.labportal.ui.theme.MmyrvTextDark
import com.example.labportal.ui.theme.MmyrvTextMuted
import com.example.labportal.util.FaceDetectionHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

enum class RecognitionState(val label: String, val color: Color) {
    INITIALIZING_CAMERA("Initializing Camera & AI Engine...", Color(0xFF64748B)),
    POSITION_YOUR_FACE("Position Your Face Inside Oval", Color(0xFFF59E0B)),
    FACE_DETECTED("Face Detected • Checking Orientation", Color(0xFF38BDF8)),
    CHECKING_IMAGE_QUALITY("Checking Image Quality & Lighting", Color(0xFFF59E0B)),
    PROMPT_BLINK("Please blink your eyes naturally (Liveness Check)", Color(0xFFEF4444)),
    PROCESSING_FACE("Processing Face • Extracting 192D Embeddings", Color(0xFF818CF8)),
    MATCHING_IDENTITY("Matching Identity Against University Database", Color(0xFF38BDF8)),
    IDENTITY_VERIFIED("Identity Verified (Screen-Lock Confirmed)", Color(0xFF10B981)),
    UNKNOWN_PERSON("Unknown Person: Face does not match registered student", Color(0xFFEF4444)),
    MULTIPLE_FACES_DETECTED("Multiple Faces Detected! Only 1 person permitted", Color(0xFFEF4444)),
    RECOGNITION_FAILED("Recognition Failed. Please try again in good lighting", Color(0xFFEF4444))
}

@Composable
fun LiveFaceScannerModal(
    title: String = "Screen-Lock Biometric Scanner",
    subtitle: String = "AI 3D Landmark & Liveness Security",
    registeredBase64: String = "",
    enrolledEmbedding: List<Float>? = null,
    expectedStudentName: String? = null,
    expectedRollNo: String? = null,
    onFaceCaptured: ((bitmap: Bitmap, base64: String) -> Unit)? = null,
    onFaceVerified: ((bitmap: Bitmap, base64: String, embedding: FloatArray, similarityScore: Float) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val engine = remember { FaceRecognitionEngine.getInstance(context) }
    val livenessDetector = remember { LivenessDetector() }

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

    var verifiedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var verifiedEmbedding by remember { mutableStateOf<FloatArray?>(null) }
    var verifiedScore by remember { mutableStateOf(0f) }

    val consentedSamples = remember { mutableListOf<FloatArray>() }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            livenessDetector.reset()
        }
    }

    val animatedFrameColor by animateColorAsState(
        targetValue = recognitionState.color,
        animationSpec = tween(300),
        label = "frameColor"
    )

    // Laser sweep animation
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
                    // Header
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
                        // Real-Time Camera Feed Viewport
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
                                            if (isCompleted || isProcessingFrame) {
                                                imageProxy.close()
                                                return@setAnalyzer
                                            }

                                            processCameraFrame(
                                                imageProxy = imageProxy,
                                                detector = liveDetector,
                                                livenessDetector = livenessDetector,
                                                onStateChange = { newState, msg ->
                                                    coroutineScope.launch(Dispatchers.Main) {
                                                        recognitionState = newState
                                                        statusMessage = msg
                                                    }
                                                },
                                                onReadyForInference = {
                                                    val bitmap = previewView.bitmap
                                                    if (bitmap != null && !isProcessingFrame && !isCompleted) {
                                                        isProcessingFrame = true
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            try {
                                                                val detectResult = engine.detectFace(bitmap)
                                                                if (detectResult.croppedFaceBitmap != null) {
                                                                    val embedding = engine.generateEmbedding(detectResult.croppedFaceBitmap)

                                                                    // Check if Enrolled embedding is available for Recognition Mode
                                                                    val targetEmbedding = when {
                                                                        enrolledEmbedding != null && enrolledEmbedding.size == FaceRecognitionEngine.EMBEDDING_DIM -> {
                                                                            enrolledEmbedding.toFloatArray()
                                                                        }
                                                                        registeredBase64.isNotBlank() -> {
                                                                            val regBmp = FaceDetectionHelper.base64ToImageBitmap(registeredBase64)
                                                                            // Generate embedding for enrolled photo
                                                                            val regDetect = regBmp?.let {
                                                                                val raw = FaceDetectionHelper.bitmapToBase64(bitmap) // fallback
                                                                                null
                                                                            }
                                                                            null
                                                                        }
                                                                        else -> null
                                                                    }

                                                                    if (targetEmbedding != null) {
                                                                        // RECOGNITION / ATTENDANCE MODE
                                                                        withContext(Dispatchers.Main) {
                                                                            recognitionState = RecognitionState.MATCHING_IDENTITY
                                                                            statusMessage = "Matching 192D biometric features..."
                                                                        }

                                                                        val match = engine.matchEmbedding(
                                                                            liveEmbedding = embedding,
                                                                            enrolledEmbedding = targetEmbedding,
                                                                            expectedRollNo = expectedRollNo,
                                                                            expectedName = expectedStudentName
                                                                        )

                                                                        withContext(Dispatchers.Main) {
                                                                            if (match.isMatch) {
                                                                                isCompleted = true
                                                                                recognitionState = RecognitionState.IDENTITY_VERIFIED
                                                                                statusMessage = "Verified: ${expectedStudentName ?: "Student"} (${match.percentage}% match)"
                                                                                progressFraction = 1.0f
                                                                                verifiedBitmap = bitmap
                                                                                verifiedEmbedding = embedding
                                                                                verifiedScore = match.similarity

                                                                                val base64 = FaceDetectionHelper.bitmapToBase64(bitmap)
                                                                                onFaceVerified?.invoke(bitmap, base64, embedding, match.similarity)
                                                                                onFaceCaptured?.invoke(bitmap, base64)

                                                                                // Automatic Success: auto-dismiss after brief feedback, NO button click needed!
                                                                                coroutineScope.launch {
                                                                                    kotlinx.coroutines.delay(850)
                                                                                    onDismiss()
                                                                                }
                                                                            } else {
                                                                                recognitionState = RecognitionState.UNKNOWN_PERSON
                                                                                statusMessage = "Access Denied: Unrecognized Person (${match.percentage}% match)"
                                                                            }
                                                                        }
                                                                    } else {
                                                                        // ENROLLMENT MODE (Consented 3-sample capture)
                                                                        consentedSamples.add(embedding)
                                                                        val progress = (consentedSamples.size / 3f).coerceIn(0f, 1f)

                                                                        withContext(Dispatchers.Main) {
                                                                            progressFraction = progress
                                                                            if (consentedSamples.size >= 3) {
                                                                                isCompleted = true
                                                                                val finalTemplate = engine.createRepresentativeTemplate(consentedSamples)
                                                                                verifiedBitmap = bitmap
                                                                                verifiedEmbedding = finalTemplate
                                                                                recognitionState = RecognitionState.IDENTITY_VERIFIED
                                                                                statusMessage = "Biometric Face Template Generated (192 Features)"

                                                                                val base64 = FaceDetectionHelper.bitmapToBase64(bitmap)
                                                                                onFaceCaptured?.invoke(bitmap, base64)
                                                                                onFaceVerified?.invoke(bitmap, base64, finalTemplate, 1.0f)

                                                                                // Automatic Success: auto-dismiss after brief feedback, NO button click needed!
                                                                                coroutineScope.launch {
                                                                                    kotlinx.coroutines.delay(850)
                                                                                    onDismiss()
                                                                                }
                                                                            } else {
                                                                                recognitionState = RecognitionState.PROCESSING_FACE
                                                                                statusMessage = "Captured sample ${consentedSamples.size}/3... Hold steady"
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } catch (e: Exception) {
                                                                withContext(Dispatchers.Main) {
                                                                    recognitionState = RecognitionState.RECOGNITION_FAILED
                                                                    statusMessage = "Error: ${e.localizedMessage ?: "Processing error"}"
                                                                }
                                                            } finally {
                                                                isProcessingFrame = false
                                                            }
                                                        }
                                                    }
                                                }
                                            )
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
                        Text(
                            text = statusMessage,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = if (recognitionState == RecognitionState.IDENTITY_VERIFIED) Color(0xFF15803D) else animatedFrameColor,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Automatic Completion Indicator / Cancel Button
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
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "Cancel Face Scan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = AppFontFamily,
                                color = MmyrvTextMuted
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
private fun processCameraFrame(
    imageProxy: ImageProxy,
    detector: com.google.mlkit.vision.face.FaceDetector,
    livenessDetector: LivenessDetector,
    onStateChange: (RecognitionState, String) -> Unit,
    onReadyForInference: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                when {
                    faces.isEmpty() -> {
                        onStateChange(RecognitionState.POSITION_YOUR_FACE, "Position your face inside the biometric oval")
                    }
                    faces.size > 1 -> {
                        onStateChange(RecognitionState.MULTIPLE_FACES_DETECTED, "Multiple faces detected (${faces.size}). Only 1 person permitted.")
                    }
                    else -> {
                        val primaryFace = faces[0]
                        val livenessResult = livenessDetector.processFrame(primaryFace, null)

                        when (livenessResult.state) {
                            LivenessState.LOOK_STRAIGHT -> {
                                onStateChange(RecognitionState.FACE_DETECTED, livenessResult.promptMessage)
                            }
                            LivenessState.PROMPT_BLINK -> {
                                onStateChange(RecognitionState.PROMPT_BLINK, livenessResult.promptMessage)
                            }
                            LivenessState.CHECKING_QUALITY -> {
                                onStateChange(RecognitionState.CHECKING_IMAGE_QUALITY, livenessResult.promptMessage)
                            }
                            LivenessState.LIVENESS_VERIFIED -> {
                                onStateChange(RecognitionState.PROCESSING_FACE, "Liveness verified. Generating MobileFaceNet embedding...")
                                onReadyForInference()
                            }
                            else -> {
                                onStateChange(RecognitionState.FACE_DETECTED, "Hold steady for 3D landmark scanning")
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
