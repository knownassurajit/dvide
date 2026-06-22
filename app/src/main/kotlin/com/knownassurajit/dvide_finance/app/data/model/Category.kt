package com.knownassurajit.dvide_finance.app.data.model

import androidx.compose.ui.graphics.Color

enum class Category(
    val key: String,
    val label: String,
    val kind: Kind,
    val hue: Int?,       // null → use seed hue
) {
    SAVINGS   ("savings",    "Savings",    Kind.ASIDE,   168),
    INVESTMENT("investment", "Investment", Kind.ASIDE,   262),
    SECURITY  ("security",  "Security",   Kind.ASIDE,   28),
    ESSENTIALS("essentials","Essentials", Kind.EXPENSE, null),
    LIFESTYLE ("lifestyle", "Lifestyle",  Kind.EXPENSE, 75);

    enum class Kind { ASIDE, EXPENSE }

    companion object {
        fun fromKey(key: String): Category? = entries.firstOrNull { it.key == key }

        fun kindOf(key: String, fallbackKind: String? = null): Kind =
            fromKey(key)?.kind ?: when (fallbackKind) {
                "aside" -> Kind.ASIDE
                else    -> Kind.EXPENSE
            }

        fun labelOf(key: String): String =
            fromKey(key)?.label ?: key.replaceFirstChar { it.uppercase() }

        fun hueOf(key: String, seedHue: Int): Int =
            fromKey(key)?.hue ?: hashHue(key).let { if (it == -1) seedHue else it }

        fun colorDark(key: String, seedHue: Int): Color {
            val h = hueOf(key, seedHue)
            return oklchToColor(0.815f, 0.105f, h)
        }

        fun colorLight(key: String, seedHue: Int): Color {
            val h = hueOf(key, seedHue)
            return oklchToColor(0.520f, 0.130f, h)
        }

        fun softDark(key: String, seedHue: Int): Color {
            val h = hueOf(key, seedHue)
            return oklchToColor(0.380f, 0.075f, h)
        }

        fun softLight(key: String, seedHue: Int): Color {
            val h = hueOf(key, seedHue)
            return oklchToColor(0.905f, 0.055f, h)
        }

        private fun hashHue(str: String): Int {
            var h = 0
            for (c in str) h = c.code + ((h shl 5) - h)
            return ((h % 360) + 360) % 360
        }

        // Approximate OKLCH → sRGB via hue-aware HSL mapping.
        // Full OKLCH→sRGB requires Lab→XYZ→sRGB matrix ops; this approximation
        // is accurate enough for colour coding and theme swatches.
        fun oklchToColor(l: Float, c: Float, h: Int): Color {
            val hf = h.toFloat()
            val sat = (c / 0.4f).coerceIn(0f, 1f)
            val r = l
            val s = sat
            val lightness = l

            // Convert HSL to RGB
            val c2 = (1f - Math.abs(2f * lightness - 1f)) * s
            val x = c2 * (1f - Math.abs((hf / 60f) % 2f - 1f))
            val m = lightness - c2 / 2f

            val (r1, g1, b1) = when {
                hf < 60f  -> Triple(c2, x, 0f)
                hf < 120f -> Triple(x, c2, 0f)
                hf < 180f -> Triple(0f, c2, x)
                hf < 240f -> Triple(0f, x, c2)
                hf < 300f -> Triple(x, 0f, c2)
                else       -> Triple(c2, 0f, x)
            }
            return Color(
                red   = (r1 + m).coerceIn(0f, 1f),
                green = (g1 + m).coerceIn(0f, 1f),
                blue  = (b1 + m).coerceIn(0f, 1f),
                alpha = 1f
            )
        }

        val ASIDE_KEYS   = listOf("savings", "investment", "security")
        val EXPENSE_KEYS = listOf("essentials", "lifestyle")
        val ALL_KEYS     = ASIDE_KEYS + EXPENSE_KEYS
    }
}
