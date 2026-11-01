package com.knownassurajit.dvide_finance.app.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.LruCache
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.knownassurajit.dvide_finance.app.util.CropMath
import com.knownassurajit.dvide_finance.app.util.CropRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Profile-photo storage.
 *
 * A picked image is never referenced by its (transient) content URI. It is decoded with sub-sampling,
 * cropped to a square, scaled to [AVATAR_PX] and written to app-private storage as JPEG. Settings store only
 * the file *name* (the "ref"), resolved against `filesDir/profile`, so the reference survives restarts,
 * revoked URI grants and path changes. All decoding runs on [Dispatchers.IO].
 */
object ProfileImages {
    const val AVATAR_PX = 512
    private const val DIR = "profile"
    private const val PREVIEW_SHORT_SIDE = 1024
    private const val JPEG_QUALITY = 90

    private val cache = object : LruCache<String, Bitmap>(4 * 1024 * 1024) { // ~4 MB, measured in bytes
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    private fun dir(context: Context) = File(context.filesDir, DIR)

    /** A ref is a bare file name. Anything with a scheme or a path separator is legacy/untrusted. */
    fun isLocalRef(ref: String) = ref.isNotBlank() && '/' !in ref && '\\' !in ref && ".." !in ref

    // ───────────────────────── picking → cropping ─────────────────────────

    /** Decodes [uri] for the cropper: sub-sampled to ~[PREVIEW_SHORT_SIDE] px and rotated upright per EXIF. */
    suspend fun loadForCrop(context: Context, uri: Uri): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                ?: throw IOException("Couldn't open the selected image.")
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("The selected file isn't a readable image.")

            val opts = BitmapFactory.Options().apply {
                inSampleSize = CropMath.sampleSize(bounds.outWidth, bounds.outHeight, PREVIEW_SHORT_SIDE)
            }
            val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: throw IOException("The selected image could not be decoded.")

            val orientation = try {
                resolver.openInputStream(uri)?.use {
                    ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: IOException) {
                ExifInterface.ORIENTATION_NORMAL // unreadable EXIF is not fatal
            }
            upright(decoded, orientation)
        }
    }

    private fun upright(src: Bitmap, orientation: Int): Bitmap {
        val m = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.postScale(-1f, 1f) }
            else -> return src
        }
        val out = Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
        if (out !== src) src.recycle()
        return out
    }

    // ───────────────────────── saving ─────────────────────────

    /** Crops [source] to [region], scales to [AVATAR_PX], writes atomically and returns the new ref. */
    suspend fun saveAvatar(context: Context, source: Bitmap, region: CropRegion): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val size = region.size.coerceIn(1, minOf(source.width, source.height))
                val left = region.left.coerceIn(0, source.width - size)
                val top = region.top.coerceIn(0, source.height - size)
                val square = Bitmap.createBitmap(source, left, top, size, size)
                val scaled = if (size > AVATAR_PX) square.scale(AVATAR_PX, AVATAR_PX) else square

                val folder = dir(context).apply { mkdirs() }
                val ref = "avatar_${System.currentTimeMillis()}.jpg"
                val tmp = File(folder, "$ref.tmp")
                try {
                    tmp.outputStream().use { out ->
                        if (!scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                            throw IOException("The photo could not be saved.")
                        }
                    }
                    if (!tmp.renameTo(File(folder, ref))) throw IOException("The photo could not be saved.")
                } finally {
                    tmp.delete() // no-op after a successful rename
                    if (scaled !== square) scaled.recycle()
                    if (square !== source) square.recycle()
                }
                ref
            }
        }

    /** Deletes every stored avatar except [keepRef] (and any leftover temp files). */
    suspend fun prune(context: Context, keepRef: String?) = withContext(Dispatchers.IO) {
        dir(context).listFiles()?.forEach { f -> if (f.name != keepRef) f.delete() }
        cache.evictAll()
    }

    // ───────────────────────── displaying ─────────────────────────

    /** Synchronous cache lookup so an already-decoded avatar is drawn on the first frame (no initials flash). */
    fun peek(ref: String, targetPx: Int): Bitmap? = cache.get("$ref@$targetPx")

    /**
     * Decodes a stored ref for display at about [targetPx] px. Returns null when the file is gone or unreadable
     * (callers fall back to initials). Legacy refs that are content URIs are tried once and may simply fail.
     */
    suspend fun decodeAvatar(context: Context, ref: String, targetPx: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = "$ref@$targetPx"
        cache.get(key)?.let { return@withContext it }
        val bmp = try {
            if (isLocalRef(ref)) decodeFile(File(dir(context), ref), targetPx)
            else decodeLegacyUri(context, ref.toUri(), targetPx)
        } catch (_: Exception) {
            null // SecurityException, IOException, OOM-guarded decode failures: show initials instead
        }
        if (bmp != null) cache.put(key, bmp)
        bmp
    }

    private fun decodeFile(file: File, targetPx: Int): Bitmap? {
        if (!file.isFile) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0) return null
        val opts = BitmapFactory.Options().apply { inSampleSize = CropMath.sampleSize(bounds.outWidth, bounds.outHeight, targetPx) }
        return BitmapFactory.decodeFile(file.path, opts)
    }

    private fun decodeLegacyUri(context: Context, uri: Uri, targetPx: Int): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0) return null
        val opts = BitmapFactory.Options().apply { inSampleSize = CropMath.sampleSize(bounds.outWidth, bounds.outHeight, targetPx) }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }
}
