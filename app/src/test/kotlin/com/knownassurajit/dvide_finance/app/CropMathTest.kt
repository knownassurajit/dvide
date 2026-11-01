package com.knownassurajit.dvide_finance.app

import com.knownassurajit.dvide_finance.app.util.CropMath
import com.knownassurajit.dvide_finance.app.util.CropRegion
import com.knownassurajit.dvide_finance.app.util.CropTransform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CropMathTest {

    private val viewport = 300f

    @Test
    fun `landscape image initially frames a centered square of its short side`() {
        val t = CropMath.initial(1600, 900, viewport)
        val r = CropMath.regionFor(1600, 900, viewport, t)
        assertEquals(900, r.size)
        assertEquals(0, r.top)
        assertEquals(350, r.left) // (1600 - 900) / 2
    }

    @Test
    fun `portrait image initially frames a centered square`() {
        val t = CropMath.initial(900, 1600, viewport)
        val r = CropMath.regionFor(900, 1600, viewport, t)
        assertEquals(CropRegion(left = 0, top = 350, size = 900), r)
    }

    @Test
    fun `zooming in shrinks the framed region about the focal point`() {
        val start = CropMath.initial(1000, 1000, viewport)
        val zoomed = CropMath.transform(start, 1000, 1000, viewport, zoom = 2f, panX = 0f, panY = 0f, focalX = 150f, focalY = 150f)
        val r = CropMath.regionFor(1000, 1000, viewport, zoomed)
        assertEquals(500, r.size)
        assertEquals(250, r.left) // centre stays at 500
        assertEquals(250, r.top)
    }

    @Test
    fun `zoom is limited to the minimum scale and the maximum zoom`() {
        val start = CropMath.initial(1000, 1000, viewport)
        val out = CropMath.transform(start, 1000, 1000, viewport, zoom = 0.1f, panX = 0f, panY = 0f, focalX = 0f, focalY = 0f)
        assertEquals(start.scale, out.scale, 1e-6f)
        val max = CropMath.transform(start, 1000, 1000, viewport, zoom = 100f, panX = 0f, panY = 0f, focalX = 0f, focalY = 0f)
        assertEquals(start.scale * CropMath.MAX_ZOOM, max.scale, 1e-5f)
    }

    @Test
    fun `panning cannot move the image away from the viewport edges`() {
        val zoomed = CropMath.transform(CropMath.initial(1000, 1000, viewport), 1000, 1000, viewport, 2f, 0f, 0f, 150f, 150f)
        val farRight = CropMath.transform(zoomed, 1000, 1000, viewport, 1f, panX = 9999f, panY = 9999f, focalX = 0f, focalY = 0f)
        assertEquals(0f, farRight.offsetX, 0f)
        assertEquals(0f, farRight.offsetY, 0f)
        val farLeft = CropMath.transform(zoomed, 1000, 1000, viewport, 1f, panX = -9999f, panY = -9999f, focalX = 0f, focalY = 0f)
        assertEquals(viewport - 1000 * farLeft.scale, farLeft.offsetX, 1e-3f)
        val r = CropMath.regionFor(1000, 1000, viewport, farLeft)
        assertTrue(r.left + r.size <= 1000 && r.top + r.size <= 1000)
    }

    @Test
    fun `region always lies inside the source bitmap`() {
        val t = CropTransform(scale = 0.3f, offsetX = -5f, offsetY = -5f)
        val r = CropMath.regionFor(1000, 800, viewport, t)
        assertTrue(r.left >= 0 && r.top >= 0)
        assertTrue(r.left + r.size <= 1000 && r.top + r.size <= 800)
    }

    @Test
    fun `sample size keeps at least the target resolution`() {
        assertEquals(1, CropMath.sampleSize(800, 600, 600))
        assertEquals(4, CropMath.sampleSize(4000, 3000, 700)) // 3000/4 = 750 >= 700, /8 = 375 < 700
        assertEquals(1, CropMath.sampleSize(100, 100, 512))
    }
}
