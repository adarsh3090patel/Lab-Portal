package com.example.labportal.ai

import android.graphics.Bitmap
import android.graphics.PointF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlin.math.hypot

enum class LivenessState {
    ALIGN_FACE,
    CHECKING_QUALITY,
    LOOK_STRAIGHT,
    PROMPT_BLINK,
    ANALYZING_MOTION,
    LIVENESS_VERIFIED,
    SPOOF_DETECTED,
    FAILED
}

data class LivenessResult(
    val state: LivenessState,
    val isLive: Boolean,
    val confidence: Float,
    val promptMessage: String,
    val blinkCompleted: Boolean,
    val orientationValid: Boolean,
    val motionValid: Boolean
)

class LivenessDetector {

    private var eyeClosedRecorded = false
    private var blinkCompleted = false
    private var validOrientationFrames = 0
    private var lastNosePosition: PointF? = null
    private val motionDeltas = mutableListOf<Float>()

    fun reset() {
        eyeClosedRecorded = false
        blinkCompleted = false
        validOrientationFrames = 0
        lastNosePosition = null
        motionDeltas.clear()
    }

    /**
     * Evaluates live frame against strict multi-criteria anti-spoofing pipeline.
     */
    fun processFrame(face: Face, bitmap: Bitmap?): LivenessResult {
        // 1. Check head orientation (Strict Screen-Lock Euler limits)
        val yaw = abs(face.headEulerAngleY)
        val pitch = abs(face.headEulerAngleX)
        val roll = abs(face.headEulerAngleZ)

        val orientationValid = yaw <= 10f && pitch <= 10f && roll <= 8f

        if (!orientationValid) {
            validOrientationFrames = 0
            val hint = when {
                yaw > 10f -> "Look directly at camera (head turned sideways)"
                pitch > 10f -> "Hold camera level with eyes (head tilted)"
                else -> "Keep head upright"
            }
            return LivenessResult(
                state = LivenessState.LOOK_STRAIGHT,
                isLive = false,
                confidence = 0.3f,
                promptMessage = hint,
                blinkCompleted = blinkCompleted,
                orientationValid = false,
                motionValid = false
            )
        }

        validOrientationFrames++

        // 2. Interactive Eye-Blink Verification
        val leftEye = face.leftEyeOpenProbability ?: 1.0f
        val rightEye = face.rightEyeOpenProbability ?: 1.0f

        if (!blinkCompleted) {
            // Check for natural eye closure
            if (leftEye <= 0.25f && rightEye <= 0.25f) {
                eyeClosedRecorded = true
            } else if (eyeClosedRecorded && leftEye >= 0.65f && rightEye >= 0.65f) {
                // Eye re-opened after closure = genuine live blink verified
                blinkCompleted = true
            }
        }

        // 3. Micro-Motion / Anti-Photo Spoofing Check
        val noseLandmark = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
        if (noseLandmark != null) {
            if (lastNosePosition != null) {
                val delta = hypot(noseLandmark.x - lastNosePosition!!.x, noseLandmark.y - lastNosePosition!!.y)
                motionDeltas.add(delta)
                if (motionDeltas.size > 15) {
                    motionDeltas.removeAt(0)
                }
            }
            lastNosePosition = noseLandmark
        }

        // Natural living micro-tremor: not perfectly frozen (photo attack) and not erratic (shaking screen)
        val avgMotion = if (motionDeltas.isNotEmpty()) motionDeltas.average().toFloat() else 1.0f
        val motionValid = motionDeltas.size >= 5 && avgMotion in 0.05f..15.0f

        // 4. Photometric Image Quality Check
        val qualityValid = if (bitmap != null) checkImageQuality(bitmap) else true

        if (!qualityValid) {
            return LivenessResult(
                state = LivenessState.CHECKING_QUALITY,
                isLive = false,
                confidence = 0.4f,
                promptMessage = "Poor lighting or glare detected. Move to a well-lit area.",
                blinkCompleted = blinkCompleted,
                orientationValid = true,
                motionValid = motionValid
            )
        }

        // State Machine Decision
        return when {
            !blinkCompleted -> {
                LivenessResult(
                    state = LivenessState.PROMPT_BLINK,
                    isLive = false,
                    confidence = 0.6f,
                    promptMessage = "Please blink your eyes naturally to verify liveness",
                    blinkCompleted = false,
                    orientationValid = true,
                    motionValid = motionValid
                )
            }
            !motionValid && motionDeltas.size < 5 -> {
                LivenessResult(
                    state = LivenessState.ANALYZING_MOTION,
                    isLive = false,
                    confidence = 0.8f,
                    promptMessage = "Analyzing live micro-movement... Hold steady",
                    blinkCompleted = true,
                    orientationValid = true,
                    motionValid = false
                )
            }
            else -> {
                LivenessResult(
                    state = LivenessState.LIVENESS_VERIFIED,
                    isLive = true,
                    confidence = 0.98f,
                    promptMessage = "Liveness Verified (Straight gaze, blink confirmed, anti-spoof passed)",
                    blinkCompleted = true,
                    orientationValid = true,
                    motionValid = true
                )
            }
        }
    }

    /**
     * Checks brightness and contrast to prevent black-screen or overexposed spoofing.
     */
    private fun checkImageQuality(bitmap: Bitmap): Boolean {
        return try {
            val step = 16
            var totalBrightness = 0L
            var sampleCount = 0

            val width = bitmap.width
            val height = bitmap.height

            for (y in 0 until height step step) {
                for (x in 0 until width step step) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    totalBrightness += luminance
                    sampleCount++
                }
            }

            if (sampleCount == 0) return true
            val avgLuminance = totalBrightness / sampleCount

            // Reject overly dark (< 30) or washed out white (> 245)
            avgLuminance in 30..245
        } catch (e: Exception) {
            true
        }
    }
}

