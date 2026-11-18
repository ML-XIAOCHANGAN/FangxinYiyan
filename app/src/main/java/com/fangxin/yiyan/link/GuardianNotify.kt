package com.fangxin.yiyan.link

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fangxin.yiyan.MainActivity
import com.fangxin.yiyan.R
import com.fangxin.yiyan.domain.AlertEvent
import com.fangxin.yiyan.domain.RiskDecision

object GuardianNotify {
    const val CHANNEL_ALERTS = "guardian_alerts"
    private const val CHANNEL_LINK = "guardian_link"
    const val CHANNEL_SCREEN = "screen_share"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, "老人端警报", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "老人手机上判定出风险时提醒守护人"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_LINK, "守护连接", NotificationManager.IMPORTANCE_LOW).apply {
                description = "守护人端保持连接"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SCREEN, "屏幕共享", NotificationManager.IMPORTANCE_LOW).apply {
                description = "老人手机共享屏幕时的提示"
            },
        )
    }

    fun postAlert(context: Context, alert: AlertEvent) {
        ensureChannels(context)
        val title = when (alert.risk) {
            RiskDecision.RED -> "紧急：家里老人可能遇到骗子"
            RiskDecision.AMBER -> "注意：家里老人收到可疑消息"
            else -> "家里老人刚看过一条消息"
        }
        val text = "${alert.label} · ${alert.plain}"
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openApp(context))
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(alert.id.hashCode(), notification)
        }
    }

    fun guardService(context: Context): android.app.Notification =
        NotificationCompat.Builder(context, CHANNEL_LINK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("正在守护家人")
            .setContentText("有事会马上提醒您")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openApp(context))
            .build()

    fun screenShare(context: Context, stopIntent: Intent): android.app.Notification =
        NotificationCompat.Builder(context, CHANNEL_SCREEN)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("家人正在看您的屏幕")
            .setContentText("随时可以停下。画面看一眼就丢，不留存。")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(
                0,
                "停下",
                PendingIntent.getService(
                    context,
                    11,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()

    private fun openApp(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
