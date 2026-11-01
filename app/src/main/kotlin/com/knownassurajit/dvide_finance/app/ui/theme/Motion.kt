package com.knownassurajit.dvide_finance.app.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Material 3 Expressive (M3X) Motion Physics tokens and spring curves.
 * Characterized by natural physical damping (dampingRatio ~ 0.75f) and high responsiveness.
 */
object M3XMotion {
    // Fast movements (chips, switches, small icon scale)
    const val StiffnessFast = 800f
    const val DampingFast = 0.75f

    // Standard spatial transforms (cards morphing, list animations, dialog enter)
    const val StiffnessSpatial = 380f
    const val DampingSpatial = 0.75f

    // Expressive page / sheet / container transforms
    const val StiffnessExpressive = 260f
    const val DampingExpressive = 0.75f

    // Subtle tactile bounce on press
    const val StiffnessTactile = 500f
    const val DampingTactile = 0.70f

    fun <T> fastSpatial(): FiniteAnimationSpec<T> =
        spring(stiffness = StiffnessFast, dampingRatio = DampingFast)

    fun <T> spatial(): FiniteAnimationSpec<T> =
        spring(stiffness = StiffnessSpatial, dampingRatio = DampingSpatial)

    fun <T> expressive(): FiniteAnimationSpec<T> =
        spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive)

    fun <T> tactile(): FiniteAnimationSpec<T> =
        spring(stiffness = StiffnessTactile, dampingRatio = DampingTactile)

    // Expressive screen navigation transitions
    fun enterPush(): EnterTransition =
        slideInHorizontally(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive)) { it } +
            scaleIn(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive), initialScale = 0.94f) +
            fadeIn(spring(stiffness = StiffnessFast, dampingRatio = Spring.DampingRatioNoBouncy))

    fun exitPush(): ExitTransition =
        slideOutHorizontally(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive)) { -it / 5 } +
            scaleOut(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive), targetScale = 0.95f) +
            fadeOut(spring(stiffness = StiffnessFast, dampingRatio = Spring.DampingRatioNoBouncy))

    fun enterPop(): EnterTransition =
        slideInHorizontally(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive)) { -it / 5 } +
            scaleIn(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive), initialScale = 0.95f) +
            fadeIn(spring(stiffness = StiffnessFast, dampingRatio = Spring.DampingRatioNoBouncy))

    fun exitPop(): ExitTransition =
        slideOutHorizontally(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive)) { it } +
            scaleOut(spring(stiffness = StiffnessExpressive, dampingRatio = DampingExpressive), targetScale = 0.94f) +
            fadeOut(spring(stiffness = StiffnessFast, dampingRatio = Spring.DampingRatioNoBouncy))
}

/**
 * Tactile M3X press physics modifier that naturally compresses the composable on touch
 * and releases with spring dynamics.
 */
fun Modifier.m3xPressEffect(targetScale: Float = 0.96f): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = M3XMotion.tactile(),
        label = "m3xPressScale",
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}
