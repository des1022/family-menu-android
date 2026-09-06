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

/** 海报模板（P2 4-03） */
enum class PosterVariant(val label: String) {
    WARM("温馨"),
    MINIMAL("简约"),
    FESTIVE("节日"),
    CUTE("可爱")
}

/**
 * 家庭菜单分享海报（1080 宽、暖色家庭风、竖版高度随菜品数自适应）。
 *
 * 四套模板（温馨/简约/节日/可爱）通过不同调色板 + 顶部装饰 + 圆角区分：
 *  - 顶部主色条/装饰 + 大标题（可编辑）+ 完整日期 + 分隔线
 *  - 2 列菜品网格（方形缩略图 + 菜名 + 份数徽章），无图显示首字占位
 *  - 底部金线 + 可编辑文案 + 落款
 */
object PosterGenerator {

    private const val W = 1080

    private class Palette(
        val bg: Int,
        val primary: Int,
        val gold: Int,
        val ink: Int,
        val brown: Int,
        val cream: Int,
        val corner: Float,
        val bandH: Float
    )

    private val palettes = mapOf(
        PosterVariant.WARM to Palette(
            bg = Color.rgb(255, 247, 236), primary = Color.rgb(232, 89, 47),
            gold = Color.rgb(232, 163, 61), ink = Color.rgb(74, 43, 24),
            brown = Color.rgb(138, 90, 51), cream = Color.rgb(243, 227, 210),
            corner = 26f, bandH = 26f
        ),
        PosterVariant.MINIMAL to Palette(
            bg = Color.rgb(255, 255, 255), primary = Color.rgb(38, 38, 38),
            gold = Color.rgb(180, 180, 180), ink = Color.rgb(30, 30, 30),
            brown = Color.rgb(110, 110, 110), cream = Color.rgb(240, 240, 240),
            corner = 10f, bandH = 8f
        ),
        PosterVariant.FESTIVE to Palette(
            bg = Color.rgb(255, 246, 235), primary = Color.rgb(192, 40, 27),
            gold = Color.rgb(224, 166, 60), ink = Color.rgb(96, 28, 18),
            brown = Color.rgb(168, 88, 42), cream = Color.rgb(241, 223, 200),
            corner = 26f, bandH = 30f
        ),
        PosterVariant.CUTE to Palette(
            bg = Color.rgb(255, 246, 248), primary = Color.rgb(242, 106, 141),
            gold = Color.rgb(247, 178, 103), ink = Color.rgb(122, 51, 80),
            brown = Color.rgb(176, 112, 140), cream = Color.rgb(255, 225, 233),
            corner = 42f, bandH = 24f
        )
    )

    fun generate(
        lines: List<OrderLine>,
        title: String,
        footer: String,
        dateFull: String,
        variant: PosterVariant = PosterVariant.WARM
    ): Bitmap {
        val p = palettes[variant] ?: palettes.getValue(PosterVariant.WARM)

        val shown = lines.take(12)
        val rows = ceil(shown.size / 2.0).toInt().coerceAtLeast(0)

        val cellH = 444f
        val gridStartY = 412f
        val contentBottom = gridStartY + rows * cellH
        // 基础 1440（3:4），菜多则加高
        val H = (maxOf(1440, contentBottom.roundToInt() + 320)).coerceAtMost(5200)

        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        canvas.drawColor(p.bg)

        // 顶部装饰（依模板不同）
        when (variant) {
            PosterVariant.FESTIVE -> {
                // 红 + 金 双条
                canvas.drawRect(0f, 0f, W.toFloat(), p.bandH * 0.6f, solid(p.primary))
                canvas.drawRect(0f, p.bandH * 0.6f, W.toFloat(), p.bandH, solid(p.gold))
            }
            PosterVariant.CUTE -> {
                canvas.drawRect(0f, 0f, W.toFloat(), p.bandH, solid(p.primary))
                // 粉底上的小圆点装饰
                val dot = solid(p.gold)
                repeat(4) { i ->
                    val cx = 130f + i * 220f
                    canvas.drawCircle(cx, p.bandH + 46f, 12f, dot)
                }
            }
            else -> {
                canvas.drawRect(0f, 0f, W.toFloat(), p.bandH, solid(p.primary))
            }
        }

        // 大标题
        val titlePaint = textPaint(82f, p.ink, Typeface.create("sans-serif-medium", Typeface.BOLD))
        canvas.drawText(title, W / 2f, p.bandH + 118f, center(titlePaint))

        // 日期
        val datePaint = textPaint(44f, p.brown, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        canvas.drawText(dateFull, W / 2f, p.bandH + 118f + 84f, center(datePaint))

        // 分隔线
        val gold = solid(p.gold)
        canvas.drawRoundRect(
            RectF(W / 2f - 70f, p.bandH + 280f, W / 2f + 70f, p.bandH + 290f), 5f, 5f, gold
        )

        // 菜品网格
        if (shown.isEmpty()) {
            val emptyPaint = textPaint(46f, p.brown, Typeface.DEFAULT)
            canvas.drawText("今天还没有点单", W / 2f, gridStartY + 160f, center(emptyPaint))
        } else {
            shown.forEachIndexed { i, line ->
                val col = i % 2
                val row = i / 2
                val x = (56 + col * (464 + 40)).toFloat()
                val y = (gridStartY + row * cellH).toFloat()
                drawDishCell(canvas, line, x, y, p, variant)
            }
            if (lines.size > 12) {
                val more = lines.size - 12
                val morePaint = textPaint(36f, p.brown, Typeface.DEFAULT)
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
        val footerPaint = textPaint(50f, p.ink, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        canvas.drawText(footer, W / 2f, footerY + 96f, center(footerPaint))

        val signPaint = textPaint(30f, p.brown, Typeface.DEFAULT)
        canvas.drawText(
            if (variant == PosterVariant.FESTIVE) "— 家宴开席 · 欢聚时刻 —" else "— 家庭菜单 · 一家人就要整整齐齐吃饭 —",
            W / 2f, H - 80f, center(signPaint)
        )

        return bmp
    }

    private fun drawDishCell(canvas: Canvas, line: OrderLine, x: Float, y: Float, p: Palette, variant: PosterVariant) {
        val imgSize = 320f
        val imgX = x + (464 - imgSize) / 2f

        val img = decodeSquare(line.dish.imagePath, 320)
        if (img != null) {
            canvas.save()
            val clip = Path().apply {
                addRoundRect(RectF(imgX, y, imgX + imgSize, y + imgSize), p.corner, p.corner, Path.Direction.CW)
            }
            canvas.clipPath(clip)
            canvas.drawBitmap(img, null, RectF(imgX, y, imgX + imgSize, y + imgSize), null)
            canvas.restore()
            img.recycle()
        } else {
            canvas.drawRoundRect(
                RectF(imgX, y, imgX + imgSize, y + imgSize), p.corner, p.corner, solid(p.cream)
            )
            if (line.dish.name.isNotBlank()) {
                val first = line.dish.name.substring(0, 1)
                val fp = textPaint(120f, p.primary, Typeface.DEFAULT)
                val fm = fp.fontMetrics
                val cy = y + imgSize / 2f - (fm.ascent + fm.descent) / 2f
                canvas.drawText(first, imgX + imgSize / 2f, cy, center(fp))
            }
        }

        // 菜名（单行省略）
        val namePaint = textPaint(44f, p.ink, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        drawEllipsizedCenter(canvas, line.dish.name, x, y + imgSize + 26f + 56f, 464f, namePaint)

        // 份数 > 1 时右上角徽章
        if (line.num > 1) {
            val cx = imgX + imgSize - 8f - 52f
            val cy = y + 8f + 52f
            canvas.drawCircle(cx, cy, 52f, solid(p.primary))
            if (variant == PosterVariant.FESTIVE) {
                canvas.drawCircle(cx, cy, 52f - 6f, Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    color = p.gold
                    isAntiAlias = true
                })
            }
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
