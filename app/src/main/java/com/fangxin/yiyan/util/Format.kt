package com.fangxin.yiyan.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

fun relativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - timestamp).coerceAtLeast(0)
    return when {
        diff < MINUTE -> "刚刚"
        diff < HOUR -> "${diff / MINUTE} 分钟前"
        diff < DAY -> "${diff / HOUR} 小时前"
        diff < 7 * DAY -> "${diff / DAY} 天前"
        else -> SimpleDateFormat("M月d日", Locale.CHINA).format(Date(timestamp))
    }
}

fun maskPhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return if (digits.length >= 11) "*******${digits.takeLast(4)}" else digits
}
