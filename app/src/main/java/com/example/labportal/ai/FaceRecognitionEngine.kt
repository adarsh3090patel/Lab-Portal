package com.example.labportal.ai

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

data class FaceDetectionResult(
    val face: Face?,
    val faceCount: Int,
    val croppedFaceBitmap: Bitmap?,
    val error: String? = null
)

data class RecognitionMatch(
    val isMatch: Boolean,
    val similarity: Float,
    val percentage: Int,
    val matchedRollNo: String?,
    val matchedName: String?,
    val message: String
)

class FaceRecognitionEngine private constructor(private val context: Context) {

    companion object {
        const val MODEL_FILENAME = "mobilefacenet.tflite"
        const val INPUT_SIZE = 112
        const val EMBEDDING_DIM = 192

        // Validated matching thresholds for MobileFaceNet
        const val STRICT_MATCH_THRESHOLD = 0.65f // Production screen-lock grade
        const val HIGH_CONFIDENCE_THRESHOLD = 0.75f

        @Volatile
        private var instance: FaceRecognitionEngine? = null

        fun getInstance(context: Context): FaceRecognitionEngine {
            return instance ?: synchronized(this) {
                instance ?: FaceRecognitionEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    private var tfliteInterpreter: Interpreter? = null

    private val mlKitDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build()
        FaceDetection.getClient(options)
    }

    init {
        loadModel()
    }

    @Synchronized
    private fun loadModel() {
        if (tfliteInterpreter != null) return
        try {
            val byteBuffer = loadModelFile(MODEL_FILENAME)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(false)
            }
            tfliteInterpreter = Interpreter(byteBuffer, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(assetName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(assetName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Detects human face using Google ML Kit.
     * Enforces exactly 1 human face in camera frame.
     */
    suspend fun detectFace(bitmap: Bitmap): FaceDetectionResult = withContext(Dispatchers.Default) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val faces = suspendCancellableCoroutine<List<Face>> { cont ->
            mlKitDetector.process(inputImage)
                .addOnSuccessListener { result ->
                    if (cont.isActive) cont.resume(result)
                }
                .addOnFailureListener { exception ->
                    if (cont.isActive) cont.resumeWithException(exception)
                }
        }

        if (faces.isEmpty()) {
            return@withContext FaceDetectionResult(
                face = null,
                faceCount = 0,
                croppedFaceBitmap = null,
                error = "No human face detected inside scanner oval."
            )
        }

        if (faces.size > 1) {
            return@withContext FaceDetectionResult(
                face = null,
                faceCount = faces.size,
                croppedFaceBitmap = null,
                error = "Multiple faces detected (${faces.size} people). Screen-lock requires only 1 person."
            )
        }

        val primaryFace = faces[0]
        val cropped = cropAndAlignFace(bitmap, primaryFace)

        FaceDetectionResult(
            face = primaryFace,
            faceCount = 1,
            croppedFaceBitmap = cropped,
            error = null
        )
    }

    /**
     * Crops the detected face bounding box with 15% safety margin and scales to 112x112 for MobileFaceNet.
     */
    fun cropAndAlignFace(bitmap: Bitmap, face: Face): Bitmap {
        val box = face.boundingBox
        val marginX = (box.width() * 0.15f).toInt()
        val marginY = (box.height() * 0.15f).toInt()

        val left = (box.left - marginX).coerceAtLeast(0)
        val top = (box.top - marginY).coerceAtLeast(0)
        val right = (box.right + marginX).coerceAtMost(bitmap.width)
        val bottom = (box.bottom + marginY).coerceAtMost(bitmap.height)

        val cropWidth = (right - left).coerceAtLeast(1)
        val cropHeight = (bottom - top).coerceAtLeast(1)

        val cropped = Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight)
        return Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true)
    }

    /**
     * Generates a 192-dimensional face embedding using MobileFaceNet TFLite model.
     * Normalized: input [-1, 1], output L2-unit vector.
     */
    suspend fun generateEmbedding(faceBitmap: Bitmap): FloatArray = withContext(Dispatchers.Default) {
        val scaled = if (faceBitmap.width != INPUT_SIZE || faceBitmap.height != INPUT_SIZE) {
            Bitmap.createScaledBitmap(faceBitmap, INPUT_SIZE, INPUT_SIZE, true)
        } else {
            faceBitmap
        }

        val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaled.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        // MobileFaceNet RGB normalization: (val - 127.5) / 128.0
        for (pixel in intValues) {
            val r = (((pixel shr 16) and 0xFF) - 127.5f) / 128.0f
            val g = (((pixel shr 8) and 0xFF) - 127.5f) / 128.0f
            val b = ((pixel and 0xFF) - 127.5f) / 128.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        val outputArray = Array(1) { FloatArray(EMBEDDING_DIM) }

        synchronized(this@FaceRecognitionEngine) {
            if (tfliteInterpreter == null) loadModel()
            tfliteInterpreter?.run(inputBuffer, outputArray)
        }

        val rawEmbedding = outputArray[0]
        l2Normalize(rawEmbedding)
    }

    /**
     * Normalizes a float vector to unit length (L2 norm = 1.0).
     */
    fun l2Normalize(vector: FloatArray): FloatArray {
        var sumSquares = 0.0
        for (v in vector) {
            sumSquares += v * v
        }
        val norm = sqrt(sumSquares).toFloat().coerceAtLeast(1e-6f)
        val normalized = FloatArray(vector.size)
        for (i in vector.indices) {
            normalized[i] = vector[i] / norm
        }
        return normalized
    }

    /**
     * Calculates Cosine Similarity between two L2-normalized 192D embeddings.
     * Returns a value between -1.0 and 1.0 (typically 0.0 to 1.0 for faces).
     */
    fun cosineSimilarity(emb1: FloatArray, emb2: FloatArray): Float {
        if (emb1.size != emb2.size || emb1.isEmpty()) return 0f
        var dotProduct = 0f
        for (i in emb1.indices) {
            dotProduct += emb1[i] * emb2[i]
        }
        return dotProduct.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Compares a live face embedding against an enrolled biometric template vector.
     */
    fun matchEmbedding(
        liveEmbedding: FloatArray,
        enrolledEmbedding: FloatArray,
        expectedRollNo: String? = null,
        expectedName: String? = null
    ): RecognitionMatch {
        val similarity = cosineSimilarity(liveEmbedding, enrolledEmbedding)
        val percentage = (similarity * 100).toInt().coerceIn(0, 100)

        val isMatch = similarity >= STRICT_MATCH_THRESHOLD

        val msg = if (isMatch) {
            "Identity Verified: ${expectedName ?: "Student"} (${percentage}% biometric similarity score)"
        } else {
            "Access Denied: Unrecognized Person (${percentage}% similarity score). Does not match registered student template."
        }

        return RecognitionMatch(
            isMatch = isMatch,
            similarity = similarity,
            percentage = percentage,
            matchedRollNo = if (isMatch) expectedRollNo else null,
            matchedName = if (isMatch) expectedName else null,
            message = msg
        )
    }

    /**
     * Multi-sample Consented Enrollment: Combines multiple consented captures into a single template.
     */
    fun createRepresentativeTemplate(sampleEmbeddings: List<FloatArray>): FloatArray {
        if (sampleEmbeddings.isEmpty()) return FloatArray(EMBEDDING_DIM)
        val sum = FloatArray(EMBEDDING_DIM)
        for (emb in sampleEmbeddings) {
            for (i in 0 until EMBEDDING_DIM) {
                sum[i] += emb[i]
            }
        }
        val count = sampleEmbeddings.size.toFloat()
        for (i in 0 until EMBEDDING_DIM) {
            sum[i] /= count
        }
        return l2Normalize(sum)
    }

    fun close() {
        try {
            tfliteInterpreter?.close()
            tfliteInterpreter = null
            mlKitDetector.close()
        } catch (_: Exception) {}
    }
}

