package com.family.menu.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

/**
 * 菜品图片本地压缩存储。
 * - 图片统一放 filesDir/dish-images/ 下，卸载 App 自动清除。
 * - 先按目标宽度采样解码，避免大图 OOM；再缩放到 1080px 内；
 * - 最后按 JPEG 质量逐级下调直到 ≤ 200 KB。
 */
class ImageStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, DIR_NAME)

    suspend fun saveFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        if (!dir.exists()) dir.mkdirs()

        val decoded = decodeSampledFromUri(uri, MAX_WIDTH)
            ?: throw IllegalArgumentException("无法读取所选图片")

        val scaled = if (decoded.width > MAX_WIDTH) {
            val targetHeight = (decoded.height * MAX_WIDTH.toFloat() / decoded.width).roundToInt()
            Bitmap.createScaledBitmap(decoded, MAX_WIDTH, targetHeight, true)
        } else {
            decoded
        }

        val bytes = compressToLimit(scaled)
        val file = File(dir, "dish_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")
        file.writeBytes(bytes)
        file.absolutePath
    }

    suspend fun delete(path: String) = withContext(Dispatchers.IO) {
        if (path.isBlank()) return@withContext
        runCatching {
            val file = File(path)
            val base = File(context.filesDir, DIR_NAME).canonicalPath
            if (file.exists() && file.canonicalPath.startsWith(base)) file.delete()
        }
    }

    private fun decodeSampledFromUri(uri: Uri, reqWidth: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsInput = context.contentResolver.openInputStream(uri) ?: return null
        boundsInput.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, reqWidth)
        }
        val bitmapInput = context.contentResolver.openInputStream(uri) ?: return null
        return bitmapInput.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun calculateInSampleSize(actualWidth: Int, reqWidth: Int): Int {
        var sample = 1
        while (actualWidth / (sample * 2) >= reqWidth) sample *= 2
        return sample.coerceAtLeast(1)
    }

    private fun compressToLimit(bitmap: Bitmap): ByteArray {
        val qualities = intArrayOf(85, 78, 70, 60, 50, 40, 30, 20)
        var result = ByteArray(0)
        for (quality in qualities) {
            val buffer = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, buffer)
            result = buffer.toByteArray()
            if (result.size <= MAX_SIZE_BYTES) break
        }
        return result
    }

    companion object {
        private const val DIR_NAME = "dish-images"
        private const val MAX_WIDTH = 1080
        private const val MAX_SIZE_BYTES = 200 * 1024
    }
}