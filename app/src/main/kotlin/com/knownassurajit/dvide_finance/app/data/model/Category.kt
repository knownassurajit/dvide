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
    LIFESTYLE ("lifestyle", "Lifestyle",  Kind.EXPENSE, 75),
    INCOME    ("income",    "Income",     Kind.INCOME,  null);

    enum class Kind { ASIDE, EXPENSE, INCOME }

    companion object {
        fun fromKey(key: String): Category? = entries.firstOrNull { it.key == key }

        fun kindOf(key: String, fallbackKind: String? = null): Kind =
            fromKey(key)?.kind ?: when (fallbackKind) {
                "aside"  -> Kind.ASIDE
                "income" -> Kind.INCOME
                else     -> Kind.EXPENSE
            }

        fun labelOf(key: String): String =
            fromKey(key)?.label ?: key.replaceFirstChar { it.uppercase() }

        fun hueOf(key: String, seedHue: Int): Int =
            when (key.lowercase()) {
                "essentials", "income" -> seedHue
                "savings"    -> (seedHue + 35) % 360
                "investment" -> (seedHue + 70) % 360
                "security"   -> (seedHue + 315) % 360
                "lifestyle"  -> (seedHue + 180) % 360
                "bonus"      -> (seedHue + 40) % 360
                "freelance"  -> (seedHue + 80) % 360
                else         -> fromKey(key)?.hue ?: hashHue(key).let { if (it == -1) seedHue else it }
            }

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

        fun monochromeDark(key: String): Color {
            val lightness = when (key.lowercase()) {
                "income"     -> 0.90f
                "savings"    -> 0.85f
                "investment" -> 0.70f
                "security"   -> 0.55f
                "essentials" -> 0.40f
                "lifestyle"  -> 0.28f
                else -> {
                    val h = kotlin.math.abs(hashHue(key)) % 5
                    0.28f + (h * 0.14f)
                }
            }
            return Color(lightness, lightness, lightness, 1f)
        }

        fun monochromeLight(key: String): Color {
            val lightness = when (key.lowercase()) {
                "income"     -> 0.15f
                "savings"    -> 0.20f
                "investment" -> 0.35f
                "security"   -> 0.50f
                "essentials" -> 0.65f
                "lifestyle"  -> 0.78f
                else -> {
                    val h = kotlin.math.abs(hashHue(key)) % 5
                    0.20f + (h * 0.14f)
                }
            }
            return Color(lightness, lightness, lightness, 1f)
        }

        fun monochromeSoftDark(key: String): Color {
            val base = monochromeDark(key)
            return base.copy(alpha = 0.20f)
        }

        fun monochromeSoftLight(key: String): Color {
            val base = monochromeLight(key)
            return base.copy(alpha = 0.15f)
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
        val INCOME_KEYS  = listOf("income", "bonus", "freelance")
        val ALL_KEYS     = ASIDE_KEYS + EXPENSE_KEYS + INCOME_KEYS
    }
}
