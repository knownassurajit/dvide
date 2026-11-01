package com.knownassurajit.dvide_finance.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Color helpers for user-chosen entry colors. Core functions work on opaque ARGB ints so they are testable
 * on the JVM without Compose types.
 */

private const val BLACK = 0xFF000000.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

/** Minimum contrast for meaningful graphics/large text (WCAG 1.4.11 / 1.4.3 large). */
const val MIN_VISIBLE_CONTRAST = 3.0

/** WCAG 2.x relative luminance of the RGB part of [argb] (alpha ignored). */
fun relativeLuminance(argb: Int): Double {
    fun channel(shift: Int): Double {
        val s = ((argb shr shift) and 0xFF) / 255.0
        return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
}

/** WCAG contrast ratio between two colors, 1.0 (identical) .. 21.0 (black on white). */
fun contrastRatio(a: Int, b: Int): Double {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
}

/**
 * White or black, whichever reads better on top of [background]. Choosing the better of exactly these two
 * guarantees a contrast of at least ~4.58:1 for *any* background (WCAG AA for normal text is 4.5:1); a softer
 * near-black would drop that floor to ~4.15:1 for mid-tones.
 */
fun readableOnArgb(background: Int): Int =
    if (contrastRatio(background, WHITE) >= contrastRatio(background, BLACK)) WHITE else BLACK

fun Color.readableContent(): Color = Color(readableOnArgb(toArgb()))

fun Color.contrastAgainst(other: Color): Double = contrastRatio(toArgb(), other.toArgb())

/**
 * Parses `#RGB`, `#RRGGBB`, `RGB` or `RRGGBB` (case-insensitive, surrounding spaces ignored) into an opaque ARGB int.
 * Returns null for anything else, so callers can show "invalid" instead of crashing on bad input.
 */
fun parseHexArgb(text: String): Int? {
    val raw = text.trim().removePrefix("#")
    val full = when (raw.length) {
        3 -> raw.map { "$it$it" }.joinToString("")
        6 -> raw
        else -> return null
    }
    if (!full.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
    return (0xFF000000L or full.toLong(16)).toInt()
}

fun parseHexColor(text: String): Color? = parseHexArgb(text)?.let { Color(it) }

fun argbToHex(argb: Int): String = String.format(java.util.Locale.US, "#%06X", argb and 0xFFFFFF)

/** Hue 0..360, saturation 0..1, lightness 0..1. */
data class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

fun argbToHsl(argb: Int): Hsl {
    val r = ((argb shr 16) and 0xFF) / 255f
    val g = ((argb shr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    val maxC = max(r, max(g, b))
    val minC = min(r, min(g, b))
    val l = (maxC + minC) / 2f
    val d = maxC - minC
    if (d == 0f) return Hsl(0f, 0f, l)
    val s = d / (1f - abs(2f * l - 1f))
    val h = when (maxC) {
        r -> ((g - b) / d).mod(6f)
        g -> (b - r) / d + 2f
        else -> (r - g) / d + 4f
    } * 60f
    return Hsl(h, s.coerceIn(0f, 1f), l)
}

fun Hsl.toColor(): Color = hslToColor(hue, saturation, lightness)
