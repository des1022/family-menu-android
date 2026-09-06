package com.family.menu.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.family.menu.data.model.OrderLine
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * 家庭菜单分享海报（竖版 ~3:4、1080 宽、暖色家庭风）。
 *
 * 版式：
 *  - 顶部主色横条 + 大标题（可编辑）+ 完整日期
 *  - 金色分隔线下方为 2 列菜品网格（方形缩略图 + 菜名 + 份数徽章）
 *  - 底部金线 + 可编辑文案
 *
 * 高度随菜品数量自适应（基础高度 = 1440 ≈ 1080×4/3）。
 * 菜品图片统一按 320px 方形中心裁切后绘制。
 */
object PosterGenerator {

    private const val W = 1080
    private val BG = Color.rgb(255, 247, 236)      // 米白
    private val PRIMARY = Color.rgb(232, 89, 47)   // 暖橙红
    private val GOLD = Color.rgb(232, 163, 61)
    private val INK = Color.rgb(74, 43, 24)        // 深棕主文字
    private val BROWN = Color.rgb(138, 90, 51)     // 次级棕
    private val CREAM = Color.rgb(243, 227, 210)   // 占位底

    fun generate(
        lines: List<OrderLine>,
        title: String,
        footer: String,
        dateFull: String
    ): Bitmap {
        val shown = lines.take(12)
        val rows = ceil(shown.size / 2.0).toInt().coerceAtLeast(0)

        val cellH = 444f
        val gridStartY = 412f
        val contentBottom = gridStartY + rows * cellH
        // 基础 1440（3:4），菜多则加高
        val H = (maxOf(1440, contentBottom.roundToInt() + 320)).coerceAtMost(5200)

        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // 底
        canvas.drawColor(BG)

        // 顶部主色条
        canvas.drawRect(0f, 0f, W.toFloat(), 26f, solid(PRIMARY))

        // 大标题
        val titlePaint = textPaint(82f, INK, Typeface.create("sans-serif-medium", Typeface.BOLD))
        canvas.drawText(title, W / 2f, 26f + 118f, center(titlePaint))

        // 日期
        val datePaint = textPaint(44f, BROWN, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        canvas.drawText(dateFull, W / 2f, 26f + 118f + 84f, center(datePaint))

        // 金色分隔线
        val gold = solid(GOLD)
        canvas.drawRoundRect(
            RectF(W / 2f - 70f, 26f + 280f, W / 2f + 70f, 26f + 280f + 10f), 5f, 5f, gold
        )

        // 菜品网格
        if (shown.isEmpty()) {
            val emptyPaint = textPaint(46f, BROWN, Typeface.DEFAULT)
            canvas.drawText("今天还没有点单", W / 2f, gridStartY + 160f, center(emptyPaint))
        } else {
            shown.forEachIndexed { i, line ->
                val col = i % 2
                val row = i / 2
                val x = (56 + col * (464 + 40)).toFloat()
                val y = (gridStartY + row * cellH).toFloat()
                drawDishCell(canvas, line, x, y)
            }
            if (lines.size > 12) {
                val more = lines.size - 12
                val morePaint = textPaint(36f, BROWN, Typeface.DEFAULT)
                canvas.drawText(
                    "…… 还有 $more 道菜，晚餐超丰盛", W / 2f,
                    gridStartY + rows * cellH - 40f, center(morePaint)
                )
            }
        }

        // 底部：金线 + 文案 + 落款
        val footerY = (H - 260).toFloat()
        canvas.drawRoundRect(
            RectF(W / 2f - 90f, footerY, W / 2f + 90f, footerY + 8f), 4f, 4f, gold
        )
        val footerPaint = textPaint(50f, INK, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        canvas.drawText(footer, W / 2f, footerY + 96f, center(footerPaint))

        val signPaint = textPaint(30f, Color.rgb(190, 152, 110), Typeface.DEFAULT)
        canvas.drawText("— 家庭菜单 · 一家人就要整整齐齐吃饭 —", W / 2f, H - 80f, center(signPaint))

        return bmp
    }

    private fun drawDishCell(canvas: Canvas, line: OrderLine, x: Float, y: Float) {
        val imgSize = 320f
        val imgX = x + (464 - imgSize) / 2f

        val img = decodeSquare(line.dish.imagePath, 320)
        if (img != null) {
            canvas.save()
            val clip = Path().apply { addRoundRect(RectF(imgX, y, imgX + imgSize, y + imgSize), 26f, 26f, Path.Direction.CW) }
            canvas.clipPath(clip)
            canvas.drawBitmap(img, null, RectF(imgX, y, imgX + imgSize, y + imgSize), null)
            canvas.restore()
            img.recycle()
        } else {
            // 无图/解码失败：暖色占位块 + 菜名首字
            canvas.drawRoundRect(
                RectF(imgX, y, imgX + imgSize, y + imgSize), 26f, 26f, solid(CREAM)
            )
            if (line.dish.name.isNotBlank()) {
                val first = line.dish.name.substring(0, 1)
                val p = textPaint(120f, Color.rgb(232, 89, 47), Typeface.DEFAULT)
                val fm = p.fontMetrics
                val cy = y + imgSize / 2f - (fm.ascent + fm.descent) / 2f
                canvas.drawText(first, imgX + imgSize / 2f, cy, center(p))
            }
        }

        // 菜名（单行省略）
        val namePaint = textPaint(44f, INK, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        drawEllipsizedCenter(canvas, line.dish.name, x, y + imgSize + 26f + 56f, 464f, namePaint)

        // 份数 > 1 时右上角红徽章
        if (line.num > 1) {
            val cx = imgX + imgSize - 8f - 52f
            val cy = y + 8f + 52f
            canvas.drawCircle(cx, cy, 52f, solid(PRIMARY))
            val badgePaint = textPaint(48f, Color.WHITE, Typeface.create("sans-serif-medium", Typeface.BOLD))
            val fm = badgePaint.fontMetrics
            val textY = cy - (fm.ascent + fm.descent) / 2f
            canvas.drawText("×${line.num}", cx, textY, center(badgePaint))
        }
    }

    // ---------- 工具 ----------

    private fun solid(color: Int) = Paint().apply { isAntiAlias = true; this.color = color }

    private fun textPaint(size: Float, color: Int, typeface: Typeface) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        this.typeface = typeface
    }

    private fun center(paint: Paint) = paint.apply { textAlign = Paint.Align.CENTER }

    private fun drawEllipsizedCenter(canvas: Canvas, raw: String, left: Float, baseline: Float, maxWidth: Float, paint: Paint) {
        if (paint.measureText(raw) <= maxWidth) {
            canvas.drawText(raw, left + maxWidth / 2f, baseline, paint)
            return
        }
        var cut = raw.length
        while (cut > 1 && paint.measureText(raw.substring(0, cut) + "…") > maxWidth) cut--
        canvas.drawText(raw.substring(0, cut) + "…", left + maxWidth / 2f, baseline, paint)
    }

    /** 按 320px 方形中心裁切读取本地图片；失败返回 null */
    private fun decodeSquare(path: String, side: Int): Bitmap? {
        if (path.isBlank()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val opts = BitmapFactory.Options().apply {
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= side && bounds.outHeight / (sample * 2) >= side) sample *= 2
                inSampleSize = sample
            }
            val decoded = BitmapFactory.decodeFile(path, opts) ?: return null

            val s = minOf(decoded.width, decoded.height)
            val x = (decoded.width - s) / 2
            val y = (decoded.height - s) / 2
            val square = Bitmap.createBitmap(decoded, x, y, s, s)
            if (square != decoded) decoded.recycle()

            val scaled = Bitmap.createScaledBitmap(square, side, side, true)
            if (scaled != square) square.recycle()
            scaled
        } catch (e: Exception) {
            null
        }
    }
}
