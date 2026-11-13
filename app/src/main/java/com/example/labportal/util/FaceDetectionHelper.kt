package com.example.labportal.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

/**
 * Image processing & Base64 encoding utilities for Python DeepFace client communication.
 * All face detection, feature extraction, and identity matching are executed on the Python DeepFace server.
 */
object FaceDetectionHelper {

    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85, maxDimension: Int = 640): String {
        return try {
            val scale = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val factor = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * factor).toInt(),
                    (bitmap.height * factor).toInt(),
                    true
                )
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scale.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    fun base64ToImageBitmap(base64Str: String): ImageBitmap? {
        val bmp = base64ToBitmap(base64Str) ?: return null
        return bmp.asImageBitmap()
    }

    fun base64ToBitmap(base64Str: String): Bitmap? {
        if (base64Str.isBlank()) return null
        return try {
            val cleanB64 = if (base64Str.contains(",")) {
                base64Str.substringAfter(",")
            } else {
                base64Str
            }
            val bytes = Base64.decode(cleanB64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
