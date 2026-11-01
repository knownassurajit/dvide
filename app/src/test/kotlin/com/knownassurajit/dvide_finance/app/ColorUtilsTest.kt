package com.knownassurajit.dvide_finance.app

import com.knownassurajit.dvide_finance.app.ui.theme.argbToHex
import com.knownassurajit.dvide_finance.app.ui.theme.argbToHsl
import com.knownassurajit.dvide_finance.app.ui.theme.contrastRatio
import com.knownassurajit.dvide_finance.app.ui.theme.parseHexArgb
import com.knownassurajit.dvide_finance.app.ui.theme.readableOnArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsTest {

    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()

    @Test
    fun `contrast of black on white is 21`() {
        assertEquals(21.0, contrastRatio(black, white), 0.01)
    }

    @Test
    fun `identical colors have contrast 1`() {
        assertEquals(1.0, contrastRatio(0xFF336699.toInt(), 0xFF336699.toInt()), 1e-9)
    }

    @Test
    fun `readable text is dark on bright colors and light on dark colors`() {
        assertEquals(black, readableOnArgb(0xFFFBBC05.toInt())) // amber
        assertEquals(white, readableOnArgb(0xFF1A237E.toInt())) // deep indigo
    }

    @Test
    fun `readable text meets WCAG AA 4_5 to 1 for every color on a coarse RGB grid`() {
        var worst = Double.MAX_VALUE
        for (r in 0..255 step 15) for (g in 0..255 step 15) for (b in 0..255 step 15) {
            val bg = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            val ratio = contrastRatio(bg, readableOnArgb(bg))
            worst = minOf(worst, ratio)
            assertTrue("bg ${argbToHex(bg)} has only $ratio", ratio >= 4.5)
        }
        assertTrue("worst case was $worst", worst >= 4.5)
    }

    @Test
    fun `hex parsing accepts 3 and 6 digit forms with or without hash`() {
        assertEquals(0xFFFF0000.toInt(), parseHexArgb("#F00"))
        assertEquals(0xFF336699.toInt(), parseHexArgb("336699"))
        assertEquals(0xFF336699.toInt(), parseHexArgb("  #3366aa".replace("aa", "99")))
    }

    @Test
    fun `invalid hex is rejected instead of throwing`() {
        listOf("", "#", "#12", "#12345", "#1234567", "#GGGGGG", "red", "#12 456").forEach {
            assertNull("`$it`", parseHexArgb(it))
        }
    }

    @Test
    fun `hex formatting round trips`() {
        assertEquals("#336699", argbToHex(0xFF336699.toInt()))
        assertEquals(0xFFEA4335.toInt(), parseHexArgb(argbToHex(0xFFEA4335.toInt())))
    }

    @Test
    fun `hsl conversion of primaries`() {
        val red = argbToHsl(0xFFFF0000.toInt())
        assertEquals(0f, red.hue, 0.5f); assertEquals(1f, red.saturation, 0.01f); assertEquals(0.5f, red.lightness, 0.01f)
        assertEquals(120f, argbToHsl(0xFF00FF00.toInt()).hue, 0.5f)
        assertEquals(240f, argbToHsl(0xFF0000FF.toInt()).hue, 0.5f)
        assertEquals(0f, argbToHsl(0xFF808080.toInt()).saturation, 0f)
    }
}
