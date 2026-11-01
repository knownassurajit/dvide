package com.knownassurajit.dvide_finance.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────
// Dynamic brand color palette generator
// Retains consistent, pitch-dark, high-contrast surfaces throughout
// while allowing the brand hue to modulate primary & accent behaviors.
// ─────────────────────────────────────────────────────────────

fun hslToColor(h: Float, s: Float, l: Float): Color {
    val normH = ((h % 360f) + 360f) % 360f
    val c = (1f - abs(2f * l - 1f)) * s
    val m = l - c / 2f
    val x = c * (1f - abs((normH / 60f) % 2f - 1f))
    val (r, g, b) = when {
        normH < 60f  -> Triple(c, x, 0f)
        normH < 120f -> Triple(x, c, 0f)
        normH < 180f -> Triple(0f, c, x)
        normH < 240f -> Triple(0f, x, c)
        normH < 300f -> Triple(x, 0f, c)
        else         -> Triple(c, 0f, x)
    }
    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f),
        alpha = 1f,
    )
}

fun Color.toHex(): String {
    val r = (red * 255f).toInt().coerceIn(0, 255)
    val g = (green * 255f).toInt().coerceIn(0, 255)
    val b = (blue * 255f).toInt().coerceIn(0, 255)
    return String.format(java.util.Locale.US, "#%02X%02X%02X", r, g, b)
}

fun hueToPrimaryColor(hue: Int): Color =
    hslToColor((((hue % 360) + 360) % 360).toFloat(), 0.85f, 0.76f)

fun hexToHue(hex: String): Int? {
    val clean = hex.trim().removePrefix("#")
    if (clean.length != 6) return null
    val rgb = clean.toLongOrNull(16) ?: return null
    val r = ((rgb shr 16) and 0xFF) / 255f
    val g = ((rgb shr 8) and 0xFF) / 255f
    val b = (rgb and 0xFF) / 255f

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    if (delta == 0f) return 0
    val hue = when (max) {
        r -> (((g - b) / delta) % 6f) * 60f
        g -> (((b - r) / delta) + 2f) * 60f
        else -> (((r - g) / delta) + 4f) * 60f
    }
    return (((hue.toInt() % 360) + 360) % 360)
}

fun colorSchemeForHue(hue: Int, dark: Boolean): ColorScheme {
    val h = (((hue % 360) + 360) % 360).toFloat()

    return if (dark) {
        val primary = hslToColor(h, 0.85f, 0.76f)
        val primaryContainer = hslToColor(h, 0.65f, 0.28f)
        val onPrimaryContainer = hslToColor(h, 0.85f, 0.92f)
        val secondary = hslToColor(h, 0.35f, 0.72f)
        val secondaryContainer = hslToColor(h, 0.30f, 0.28f)
        val onSecondaryContainer = hslToColor(h, 0.40f, 0.90f)
        val tertiary = hslToColor((h + 45f) % 360f, 0.60f, 0.76f)
        val tertiaryContainer = hslToColor((h + 45f) % 360f, 0.45f, 0.30f)
        val onTertiaryContainer = hslToColor((h + 45f) % 360f, 0.65f, 0.92f)

        darkColorScheme(
            primary              = primary,
            onPrimary            = Color(0xFF140824),
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = onPrimaryContainer,
            secondary            = secondary,
            onSecondary          = Color(0xFF181222),
            secondaryContainer   = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary             = tertiary,
            onTertiary           = Color(0xFF2E101D),
            tertiaryContainer    = tertiaryContainer,
            onTertiaryContainer  = onTertiaryContainer,
            error                = Color(0xFFE8A87A),   // terracotta — overspent state
            onError              = Color(0xFF4A1C00),
            errorContainer       = Color(0xFF7A3A12),
            onErrorContainer     = Color(0xFFFFDBCC),
            background           = Color(0xFF100C14),
            onBackground         = Color(0xFFEDE6F5),
            surface              = Color(0xFF100C14),
            onSurface            = Color(0xFFEDE6F5),
            surfaceVariant       = Color(0xFF262030),
            onSurfaceVariant     = Color(0xFFC7BED4),
            outline              = Color(0xFF7A7286),
            outlineVariant       = Color(0xFF383144),
            surfaceTint          = primary,
            inverseSurface       = Color(0xFFEDE6F5),
            inverseOnSurface     = Color(0xFF1E1828),
            inversePrimary       = hslToColor(h, 0.70f, 0.45f),
            scrim                = Color(0x8C000000),
            surfaceBright        = Color(0xFF342E40),
            surfaceContainer     = Color(0xFF1C1824),
            surfaceContainerHigh = Color(0xFF26212F),
            surfaceContainerHighest = Color(0xFF312B3B),
            surfaceContainerLow  = Color(0xFF16121C),
            surfaceContainerLowest = Color(0xFF0C0910),
            surfaceDim           = Color(0xFF100C14),
        )
    } else {
        val primary = hslToColor(h, 0.72f, 0.44f)
        val primaryContainer = hslToColor(h, 0.85f, 0.92f)
        val onPrimaryContainer = hslToColor(h, 0.90f, 0.16f)
        val secondary = hslToColor(h, 0.35f, 0.42f)
        val secondaryContainer = hslToColor(h, 0.40f, 0.90f)
        val onSecondaryContainer = hslToColor(h, 0.45f, 0.14f)
        val tertiary = hslToColor((h + 45f) % 360f, 0.50f, 0.42f)
        val tertiaryContainer = hslToColor((h + 45f) % 360f, 0.65f, 0.92f)
        val onTertiaryContainer = hslToColor((h + 45f) % 360f, 0.70f, 0.14f)

        lightColorScheme(
            primary              = primary,
            onPrimary            = Color(0xFFFFFFFF),
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = onPrimaryContainer,
            secondary            = secondary,
            onSecondary          = Color(0xFFFFFFFF),
            secondaryContainer   = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary             = tertiary,
            onTertiary           = Color(0xFFFFFFFF),
            tertiaryContainer    = tertiaryContainer,
            onTertiaryContainer  = onTertiaryContainer,
            error                = Color(0xFF9A5A20),   // terracotta light
            onError              = Color(0xFFFFFFFF),
            errorContainer       = Color(0xFFFFDBC8),
            onErrorContainer     = Color(0xFF3A1200),
            background           = Color(0xFFFCF8FF),
            onBackground         = Color(0xFF1C1A22),
            surface              = Color(0xFFFCF8FF),
            onSurface            = Color(0xFF1C1A22),
            surfaceVariant       = Color(0xFFE5DFEC),
            onSurfaceVariant     = Color(0xFF484452),
            outline              = Color(0xFF797482),
            outlineVariant       = Color(0xFFCBC4D4),
            surfaceTint          = primary,
            inverseSurface       = Color(0xFF322C3B),
            inverseOnSurface     = Color(0xFFF5EEFF),
            inversePrimary       = hslToColor(h, 0.85f, 0.76f),
            scrim                = Color(0x66000000),
            surfaceBright        = Color(0xFFFCF8FF),
            surfaceContainer     = Color(0xFFF3EDF8),
            surfaceContainerHigh = Color(0xFFEDE7F2),
            surfaceContainerHighest = Color(0xFFE7E1ED),
            surfaceContainerLow  = Color(0xFFF9F3FE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceDim           = Color(0xFFDDD7E3),
        )
    }
}
