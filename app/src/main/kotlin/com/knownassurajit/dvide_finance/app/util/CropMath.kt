package com.knownassurajit.dvide_finance.app.util

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Square region of a source bitmap, in source pixels. */
data class CropRegion(val left: Int, val top: Int, val size: Int)

/** Image placement inside the square crop viewport: [offsetX]/[offsetY] is the image's top-left in viewport px. */
data class CropTransform(val scale: Float, val offsetX: Float, val offsetY: Float)

/** Pure geometry for the profile-photo cropper (no Android types, so it is unit-testable on the JVM). */
object CropMath {
    const val MAX_ZOOM = 5f

    /** Smallest scale at which the image still covers the whole viewport (no empty edges). */
    fun minScale(imageWidth: Int, imageHeight: Int, viewport: Float): Float {
        require(imageWidth > 0 && imageHeight > 0 && viewport > 0f) { "Image and viewport must be non-empty" }
        return max(viewport / imageWidth, viewport / imageHeight)
    }

    /** Centered, fully covering placement — the starting point of the cropper. */
    fun initial(imageWidth: Int, imageHeight: Int, viewport: Float): CropTransform {
        val s = minScale(imageWidth, imageHeight, viewport)
        return CropTransform(s, (viewport - imageWidth * s) / 2f, (viewport - imageHeight * s) / 2f)
    }

    private fun clampOffset(offset: Float, imageExtent: Int, scale: Float, viewport: Float): Float {
        val lowest = min(0f, viewport - imageExtent * scale)
        return offset.coerceIn(lowest, 0f)
    }

    /**
     * Applies one pinch/pan step. Zoom happens about ([focalX], [focalY]) in viewport coordinates so the point
     * under the fingers stays put; scale is limited to [minScale]..[minScale]×[MAX_ZOOM]; the image never leaves the viewport.
     */
    fun transform(
        current: CropTransform,
        imageWidth: Int,
        imageHeight: Int,
        viewport: Float,
        zoom: Float,
        panX: Float,
        panY: Float,
        focalX: Float,
        focalY: Float,
    ): CropTransform {
        val minS = minScale(imageWidth, imageHeight, viewport)
        val newScale = (current.scale * zoom).coerceIn(minS, minS * MAX_ZOOM)
        val ratio = newScale / current.scale
        val x = focalX - (focalX - current.offsetX) * ratio + panX
        val y = focalY - (focalY - current.offsetY) * ratio + panY
        return CropTransform(
            scale = newScale,
            offsetX = clampOffset(x, imageWidth, newScale, viewport),
            offsetY = clampOffset(y, imageHeight, newScale, viewport),
        )
    }

    /** Maps the on-screen viewport back to the square of source pixels the user framed. */
    fun regionFor(imageWidth: Int, imageHeight: Int, viewport: Float, t: CropTransform): CropRegion {
        val size = (viewport / t.scale).roundToInt().coerceIn(1, min(imageWidth, imageHeight))
        val left = (-t.offsetX / t.scale).roundToInt().coerceIn(0, imageWidth - size)
        val top = (-t.offsetY / t.scale).roundToInt().coerceIn(0, imageHeight - size)
        return CropRegion(left, top, size)
    }

    /** Largest power-of-two sample size that keeps the decoded image at least [targetPx] on its short side. */
    fun sampleSize(width: Int, height: Int, targetPx: Int): Int {
        var sample = 1
        val shortSide = min(width, height)
        while (shortSide / (sample * 2) >= targetPx) sample *= 2
        return sample
    }
}
