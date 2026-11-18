package com.fangxin.yiyan.domain

object HistoryPolicy {
    const val MAX_ITEMS = 3

    fun trim(items: List<HistoryItem>, max: Int = MAX_ITEMS): List<HistoryItem> =
        items.sortedByDescending { it.createdAt }.distinctBy { it.id }.take(max)
}
