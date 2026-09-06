package com.family.menu.util

import java.util.Calendar
import java.util.Locale

/** 价格：整数不显示小数，小数保留两位 */
fun formatPrice(value: Double): String {
    if (value <= 0.0) return "0"
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", value)
    }
}

/** 时间：今天只显示时分，同年显示月日，跨年补年份 */
fun formatTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = Calendar.getInstance()
    val hm = String.format(
        Locale.getDefault(),
        "%02d:%02d",
        target.get(Calendar.HOUR_OF_DAY),
        target.get(Calendar.MINUTE)
    )
    val sameYear = target.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    val sameDay = sameYear && target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    return when {
        sameDay -> "今天 $hm"
        sameYear -> "${target.get(Calendar.MONTH) + 1}月${target.get(Calendar.DAY_OF_MONTH)}日 $hm"
        else -> "${target.get(Calendar.YEAR)}年${target.get(Calendar.MONTH) + 1}月${target.get(Calendar.DAY_OF_MONTH)}日 $hm"
    }
}

/** yyyy-MM-dd 日期字符串 */
fun todayDateString(): String = dateStringOf(System.currentTimeMillis())

fun dateStringOf(ts: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
}

private data class Ymd(val y: Int, val m: Int, val d: Int)

private fun parseYmd(date: String): Ymd? {
    val p = date.split("-")
    if (p.size != 3) return null
    val y = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val d = p[2].toIntOrNull() ?: return null
    return Ymd(y, m, d)
}

/** yyyy-MM-dd → 「9月6日」；跨年显示「2025年12月31日」 */
fun dateCN(date: String): String {
    val v = parseYmd(date) ?: return date
    val nowYear = Calendar.getInstance().get(Calendar.YEAR)
    return if (v.y == nowYear) "${v.m}月${v.d}日" else "${v.y}年${v.m}月${v.d}日"
}

/** yyyy-MM-dd → 「9月6日 · 周日」 等 */
fun dateCNWithWeek(date: String): String {
    val v = parseYmd(date) ?: return date
    return "${dateCN(date)} · ${weekCN(date)}"
}

/** yyyy-MM-dd → 「2026年9月6日 星期日」（海报等正式场景） */
fun dateCNFull(date: String): String {
    val v = parseYmd(date) ?: return date
    return "${v.y}年${v.m}月${v.d}日 ${weekCN(date)}"
}

fun weekCN(date: String): String {
    val v = parseYmd(date) ?: return ""
    val c = Calendar.getInstance().apply {
        set(v.y, v.m - 1, v.d)
    }
    val names = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    return names[c.get(Calendar.DAY_OF_WEEK) - 1]
}
