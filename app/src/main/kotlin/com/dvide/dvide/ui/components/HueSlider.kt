package com.dvide.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dvide.app.data.model.Category

@Composable
fun HueSlider(
    hue: Int,
    onHueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var trackWidth by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Spectrum track
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(999.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        trackWidth = size.width.toFloat()
                        val h = ((offset.x / trackWidth) * 360).toInt().coerceIn(0, 360)
                        onHueChange(h)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        trackWidth = size.width.toFloat()
                        val h = ((change.position.x / trackWidth) * 360).toInt().coerceIn(0, 360)
                        onHueChange(h)
                    }
                },
        ) {
            trackWidth = size.width
            // Full-spectrum gradient
            val stops = (0..12).map { i ->
                (i / 12f) to Category.oklchToColor(0.65f, 0.15f, (i * 30))
            }.toTypedArray()
            drawRect(
                brush = Brush.horizontalGradient(colorStops = stops),
                size  = Size(size.width, size.height),
            )
        }

        // Thumb
        val pct = (hue.toFloat() / 360f).coerceIn(0f, 1f)
        Canvas(modifier = Modifier.fillMaxWidth().height(38.dp)) {
            trackWidth = size.width
            val cx = pct * size.width
            val cy = size.height / 2f
            // White border
            drawCircle(color = Color.White, radius = 16.dp.toPx(), center = Offset(cx, cy))
            // Hue fill
            drawCircle(
                color  = Category.oklchToColor(0.65f, 0.15f, hue),
                radius = 13.dp.toPx(),
                center = Offset(cx, cy),
            )
        }
    }
}
