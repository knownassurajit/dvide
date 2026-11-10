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

        val background           = hslToColor(h, 0.14f, 0.06f)
        val surface              = hslToColor(h, 0.14f, 0.06f)
        val surfaceContainerLowest = hslToColor(h, 0.16f, 0.04f)
        val surfaceContainerLow  = hslToColor(h, 0.14f, 0.09f)
        val surfaceContainer     = hslToColor(h, 0.14f, 0.12f)
        val surfaceContainerHigh = hslToColor(h, 0.14f, 0.16f)
        val surfaceContainerHighest = hslToColor(h, 0.14f, 0.20f)
        val surfaceBright        = hslToColor(h, 0.14f, 0.22f)
        val surfaceVariant       = hslToColor(h, 0.15f, 0.16f)
        val surfaceDim           = hslToColor(h, 0.14f, 0.06f)
        val onSurface            = hslToColor(h, 0.15f, 0.93f)
        val onSurfaceVariant     = hslToColor(h, 0.12f, 0.78f)
        val outline              = hslToColor(h, 0.10f, 0.50f)
        val outlineVariant       = hslToColor(h, 0.12f, 0.24f)

        darkColorScheme(
            primary              = primary,
            onPrimary            = hslToColor(h, 0.60f, 0.10f),
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = onPrimaryContainer,
            secondary            = secondary,
            onSecondary          = hslToColor(h, 0.35f, 0.10f),
            secondaryContainer   = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary             = tertiary,
            onTertiary           = hslToColor((h + 45f) % 360f, 0.45f, 0.10f),
            tertiaryContainer    = tertiaryContainer,
            onTertiaryContainer  = onTertiaryContainer,
            error                = Color(0xFFE8A87A),   // terracotta — overspent state
            onError              = Color(0xFF4A1C00),
            errorContainer       = Color(0xFF7A3A12),
            onErrorContainer     = Color(0xFFFFDBCC),
            background           = background,
            onBackground         = onSurface,
            surface              = surface,
            onSurface            = onSurface,
            surfaceVariant       = surfaceVariant,
            onSurfaceVariant     = onSurfaceVariant,
            outline              = outline,
            outlineVariant       = outlineVariant,
            surfaceTint          = primary,
            inverseSurface       = hslToColor(h, 0.15f, 0.92f),
            inverseOnSurface     = hslToColor(h, 0.20f, 0.12f),
            inversePrimary       = hslToColor(h, 0.70f, 0.45f),
            scrim                = Color(0x8C000000),
            surfaceBright        = surfaceBright,
            surfaceContainer     = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow  = surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceDim           = surfaceDim,
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

        val background           = hslToColor(h, 0.25f, 0.98f)
        val surface              = hslToColor(h, 0.25f, 0.98f)
        val surfaceContainerLowest = Color(0xFFFFFFFF)
        val surfaceContainerLow  = hslToColor(h, 0.22f, 0.96f)
        val surfaceContainer     = hslToColor(h, 0.20f, 0.93f)
        val surfaceContainerHigh = hslToColor(h, 0.20f, 0.90f)
        val surfaceContainerHighest = hslToColor(h, 0.20f, 0.87f)
        val surfaceDim           = hslToColor(h, 0.18f, 0.85f)
        val surfaceBright        = hslToColor(h, 0.25f, 0.98f)
        val surfaceVariant       = hslToColor(h, 0.16f, 0.90f)
        val onSurface            = hslToColor(h, 0.25f, 0.12f)
        val onSurfaceVariant     = hslToColor(h, 0.15f, 0.32f)
        val outline              = hslToColor(h, 0.12f, 0.50f)
        val outlineVariant       = hslToColor(h, 0.15f, 0.80f)

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
            background           = background,
            onBackground         = onSurface,
            surface              = surface,
            onSurface            = onSurface,
            surfaceVariant       = surfaceVariant,
            onSurfaceVariant     = onSurfaceVariant,
            outline              = outline,
            outlineVariant       = outlineVariant,
            surfaceTint          = primary,
            inverseSurface       = hslToColor(h, 0.15f, 0.20f),
            inverseOnSurface     = hslToColor(h, 0.20f, 0.96f),
            inversePrimary       = hslToColor(h, 0.85f, 0.76f),
            scrim                = Color(0x66000000),
            surfaceBright        = surfaceBright,
            surfaceContainer     = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow  = surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceDim           = surfaceDim,
        )
    }
}

fun monochromeColorScheme(dark: Boolean): ColorScheme {
    return if (dark) {
        val primary = Color(0xFFFFFFFF)
        val onPrimary = Color(0xFF000000)
        val primaryContainer = Color(0xFF2C2C2C)
        val onPrimaryContainer = Color(0xFFFFFFFF)
        val secondary = Color(0xFFD4D4D4)
        val onSecondary = Color(0xFF000000)
        val secondaryContainer = Color(0xFF262626)
        val onSecondaryContainer = Color(0xFFE5E5E5)
        val tertiary = Color(0xFFA3A3A3)
        val onTertiary = Color(0xFF000000)
        val tertiaryContainer = Color(0xFF1F1F1F)
        val onTertiaryContainer = Color(0xFFD4D4D4)

        darkColorScheme(
            primary              = primary,
            onPrimary            = onPrimary,
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = onPrimaryContainer,
            secondary            = secondary,
            onSecondary          = onSecondary,
            secondaryContainer   = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary             = tertiary,
            onTertiary           = onTertiary,
            tertiaryContainer    = tertiaryContainer,
            onTertiaryContainer  = onTertiaryContainer,
            error                = Color(0xFFE5E5E5),
            onError              = Color(0xFF000000),
            errorContainer       = Color(0xFF333333),
            onErrorContainer     = Color(0xFFFFFFFF),
            background           = Color(0xFF121212),
            onBackground         = Color(0xFFEDEDED),
            surface              = Color(0xFF121212),
            onSurface            = Color(0xFFEDEDED),
            surfaceVariant       = Color(0xFF242424),
            onSurfaceVariant     = Color(0xFFA8A8A8),
            outline              = Color(0xFF707070),
            outlineVariant       = Color(0xFF383838),
            surfaceTint          = primary,
            inverseSurface       = Color(0xFFEDEDED),
            inverseOnSurface     = Color(0xFF1A1A1A),
            inversePrimary       = Color(0xFF383838),
            scrim                = Color(0x8C000000),
            surfaceBright        = Color(0xFF2E2E2E),
            surfaceContainer     = Color(0xFF1A1A1A),
            surfaceContainerHigh = Color(0xFF222222),
            surfaceContainerHighest = Color(0xFF2A2A2A),
            surfaceContainerLow  = Color(0xFF161616),
            surfaceContainerLowest = Color(0xFF0E0E0E),
            surfaceDim           = Color(0xFF121212),
        )
    } else {
        val primary = Color(0xFF000000)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFE0E0E0)
        val onPrimaryContainer = Color(0xFF000000)
        val secondary = Color(0xFF404040)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFEBEBEB)
        val onSecondaryContainer = Color(0xFF171717)
        val tertiary = Color(0xFF5E5E5E)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFF0F0F0)
        val onTertiaryContainer = Color(0xFF262626)

        lightColorScheme(
            primary              = primary,
            onPrimary            = onPrimary,
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = onPrimaryContainer,
            secondary            = secondary,
            onSecondary          = onSecondary,
            secondaryContainer   = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary             = tertiary,
            onTertiary           = onTertiary,
            tertiaryContainer    = tertiaryContainer,
            onTertiaryContainer  = onTertiaryContainer,
            error                = Color(0xFF262626),
            onError              = Color(0xFFFFFFFF),
            errorContainer       = Color(0xFFD6D6D6),
            onErrorContainer     = Color(0xFF000000),
            background           = Color(0xFFF8F8F8),
            onBackground         = Color(0xFF171717),
            surface              = Color(0xFFF8F8F8),
            onSurface            = Color(0xFF171717),
            surfaceVariant       = Color(0xFFE2E2E2),
            onSurfaceVariant     = Color(0xFF4A4A4A),
            outline              = Color(0xFF787878),
            outlineVariant       = Color(0xFFC7C7C7),
            surfaceTint          = primary,
            inverseSurface       = Color(0xFF262626),
            inverseOnSurface     = Color(0xFFF5F5F5),
            inversePrimary       = Color(0xFFE0E0E0),
            scrim                = Color(0x66000000),
            surfaceBright        = Color(0xFFFFFFFF),
            surfaceContainer     = Color(0xFFEDEDED),
            surfaceContainerHigh = Color(0xFFE5E5E5),
            surfaceContainerHighest = Color(0xFFDEDEDE),
            surfaceContainerLow  = Color(0xFFF3F3F3),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceDim           = Color(0xFFD8D8D8),
        )
    }
}
