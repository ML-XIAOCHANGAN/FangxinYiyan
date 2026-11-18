package com.fangxin.yiyan.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object Intents {
    const val HOTLINE = "96110"
    const val SOCIAL_SECURITY = "12333"

    fun dial(context: Context, number: String) {
        val target = number.filter { it.isDigit() || it == '+' }.ifBlank { HOTLINE }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$target"))
        runCatching { context.startActivity(intent) }
    }

    fun share(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "发给家人"))
        }
    }
}
