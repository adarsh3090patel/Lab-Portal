package com.example.labportal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

@Composable
fun BackgroundCurves(
    modifier: Modifier = Modifier,
    curveColor: Color = Color(0xFF5B61F4).copy(alpha = 0.08f),
    accentCurveColor: Color = Color(0xFF5B61F4).copy(alpha = 0.04f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top-left organic wave
        val topLeftPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width * 0.45f, 0f)
            cubicTo(
                width * 0.35f, height * 0.08f,
                width * 0.15f, height * 0.12f,
                0f, height * 0.18f
            )
            close()
        }
        drawPath(path = topLeftPath, color = curveColor)

        // Subtle top-left second wave layer
        val topLeftAccent = Path().apply {
            moveTo(0f, 0f)
            lineTo(width * 0.30f, 0f)
            cubicTo(
                width * 0.22f, height * 0.05f,
                width * 0.08f, height * 0.09f,
                0f, height * 0.13f
            )
            close()
        }
        drawPath(path = topLeftAccent, color = accentCurveColor)

        // Bottom-right organic wave
        val bottomRightPath = Path().apply {
            moveTo(width, height)
            lineTo(width * 0.55f, height)
            cubicTo(
                width * 0.65f, height * 0.92f,
                width * 0.85f, height * 0.88f,
                width, height * 0.82f
            )
            close()
        }
        drawPath(path = bottomRightPath, color = curveColor)

        // Subtle bottom-right second wave layer
        val bottomRightAccent = Path().apply {
            moveTo(width, height)
            lineTo(width * 0.70f, height)
            cubicTo(
                width * 0.78f, height * 0.95f,
                width * 0.92f, height * 0.91f,
                width, height * 0.87f
            )
            close()
        }
        drawPath(path = bottomRightAccent, color = accentCurveColor)
    }
}

