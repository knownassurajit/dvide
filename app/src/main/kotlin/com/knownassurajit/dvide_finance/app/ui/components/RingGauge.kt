package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// 270° sweep ring gauge — gap centred at the bottom.
// startAngle: 135°  sweepAngle: 270°
@Composable
fun RingGauge(
    value: Float,             // 0..1
    size: Dp = 220.dp,
    strokeWidth: Dp = 20.dp,
    fillColor: Color,
    trackColor: Color,
    sharp: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue   = value.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 180f, dampingRatio = 0.75f),
        label         = "ringGauge",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val sw         = strokeWidth.toPx()
            val diameter   = this.size.minDimension - sw
            val topLeft    = Offset(sw / 2f, sw / 2f)
            val arcSize    = Size(diameter, diameter)
            val startAngle = 135f
            val sweepTotal = 270f
            val cap        = if (sharp) StrokeCap.Square else StrokeCap.Round

            // Background track
            drawArc(
                color      = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = sw, cap = cap),
            )
            // Fill
            if (animated > 0f) {
                drawArc(
                    color      = fillColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal * animated,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = sw, cap = cap),
                )
            }
        }
        content()
    }
}
