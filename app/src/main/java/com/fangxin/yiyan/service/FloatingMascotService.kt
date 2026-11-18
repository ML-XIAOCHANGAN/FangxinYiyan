package com.fangxin.yiyan.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.fangxin.yiyan.MainActivity
import com.fangxin.yiyan.R
import com.fangxin.yiyan.data.AppRepository
import kotlin.math.hypot
import kotlin.math.max

enum class MascotMood(val drawable: Int) {
    IDLE(R.drawable.owl_head_idle),
    ALERT(R.drawable.owl_head_alert),
    SAFE(R.drawable.owl_head_safe),
}

/** 退出软件后留在桌面的猫头鹰头像，点一下回到软件。 */
class FloatingMascotService : Service() {
    private val windowManager by lazy { getSystemService(WindowManager::class.java) }
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }
    private var view: ImageView? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        running = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()

        when (intent?.action) {
            ACTION_STOP -> {
                AppRepository.get(this).setFloating(false)
                removeOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_SHUTDOWN -> {
                removeOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_HIDE -> removeOverlay()
            ACTION_SHOW -> if (allowed()) showOverlay()
            ACTION_MOOD -> view?.setImageResource(mood.drawable)
            else -> if (allowed()) showOverlay()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        running = false
        removeOverlay()
        super.onDestroy()
    }

    private fun allowed(): Boolean {
        val settings = AppRepository.get(this).settings.value
        return settings.floatingEnabled &&
            settings.role != null &&
            Settings.canDrawOverlays(this)
    }

    private fun showOverlay() {
        if (view != null) {
            view?.setImageResource(mood.drawable)
            return
        }
        val manager = windowManager ?: return
        if (!Settings.canDrawOverlays(this)) return

        val size = (SIZE_DP * resources.displayMetrics.density).toInt()
        val screen = screenSize(manager)
        val image = ImageView(this).apply {
            setImageResource(mood.drawable)
            contentDescription = "放心一眼小助手，点一下回到软件"
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val layout = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt(KEY_X, screen.first - size - (EDGE_DP * resources.displayMetrics.density).toInt())
                .coerceIn(0, max(0, screen.first - size))
            y = prefs.getInt(KEY_Y, (screen.second * 0.62f).toInt())
                .coerceIn(0, max(0, screen.second - size))
        }
        attachDrag(image, manager, layout, screen, size)
        runCatching { manager.addView(image, layout) }
            .onSuccess {
                view = image
                params = layout
            }
    }

    private fun attachDrag(
        image: ImageView,
        manager: WindowManager,
        layout: WindowManager.LayoutParams,
        screen: Pair<Int, Int>,
        size: Int,
    ) {
        val slop = (8 * resources.displayMetrics.density).toInt()
        var downX = 0f
        var downY = 0f
        var originX = 0
        var originY = 0
        var dragging = false

        image.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    originX = layout.x
                    originY = layout.y
                    dragging = false
                    image.alpha = 0.82f
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!dragging && hypot(dx, dy) > slop) dragging = true
                    if (dragging) {
                        layout.x = (originX + dx.toInt()).coerceIn(0, max(0, screen.first - size))
                        layout.y = (originY + dy.toInt()).coerceIn(0, max(0, screen.second - size))
                        runCatching { manager.updateViewLayout(image, layout) }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    image.alpha = 1f
                    if (dragging) {
                        prefs.edit().putInt(KEY_X, layout.x).putInt(KEY_Y, layout.y).apply()
                    } else {
                        openApp()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    image.alpha = 1f
                    true
                }

                else -> false
            }
        }
    }

    private fun removeOverlay() {
        val manager = windowManager ?: return
        val current = view ?: return
        runCatching { manager.removeView(current) }
        view = null
        params = null
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        runCatching { startActivity(intent) }
    }

    private fun screenSize(manager: WindowManager): Pair<Int, Int> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = manager.currentWindowMetrics.bounds
            return bounds.width() to bounds.height()
        }
        val metrics = resources.displayMetrics
        return metrics.widthPixels to metrics.heightPixels
    }

    private fun startForegroundNotification() {
        ensureChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_FLOATING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.owl_head_idle))
            .setContentTitle("猫头鹰在桌面陪您")
            .setContentText("点它就能回到放心一眼")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .addAction(0, "收起", stopIntent())
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_FLOATING, "桌面小助手", NotificationManager.IMPORTANCE_LOW).apply {
                description = "退出软件后，猫头鹰留在桌面上"
            },
        )
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            20,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun stopIntent(): PendingIntent = PendingIntent.getService(
        this,
        21,
        Intent(this, FloatingMascotService::class.java).setAction(ACTION_STOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        private const val CHANNEL_FLOATING = "floating_mascot"
        private const val NOTIF_ID = 72
        private const val SIZE_DP = 88
        private const val EDGE_DP = 8
        private const val PREFS_NAME = "floating_mascot"
        private const val KEY_X = "x"
        private const val KEY_Y = "y"

        const val ACTION_SHOW = "com.fangxin.yiyan.action.SHOW_MASCOT"
        const val ACTION_HIDE = "com.fangxin.yiyan.action.HIDE_MASCOT"
        const val ACTION_MOOD = "com.fangxin.yiyan.action.MASCOT_MOOD"
        const val ACTION_STOP = "com.fangxin.yiyan.action.STOP_MASCOT"
        const val ACTION_SHUTDOWN = "com.fangxin.yiyan.action.SHUTDOWN_MASCOT"

        @Volatile
        private var running = false

        @Volatile
        private var mood = MascotMood.IDLE

        fun setMood(context: Context, next: MascotMood) {
            mood = next
            if (running) send(context, ACTION_MOOD)
        }

        fun onAppForeground(context: Context) {
            val settings = AppRepository.get(context).settings.value
            if (settings.floatingEnabled && settings.role != null && Settings.canDrawOverlays(context)) {
                send(context, ACTION_HIDE)
            } else if (running) {
                send(context, ACTION_SHUTDOWN)
            }
        }

        fun onAppBackground(context: Context) {
            val settings = AppRepository.get(context).settings.value
            if (!settings.floatingEnabled || settings.role == null) return
            if (!Settings.canDrawOverlays(context)) return
            send(context, ACTION_SHOW)
        }

        fun startIfAllowed(context: Context) {
            val settings = AppRepository.get(context).settings.value
            if (settings.floatingEnabled && settings.role != null && Settings.canDrawOverlays(context)) {
                send(context, ACTION_HIDE)
            }
        }

        fun stop(context: Context) {
            if (!running) return
            send(context, ACTION_SHUTDOWN)
        }

        private fun send(context: Context, action: String) {
            val intent = Intent(context, FloatingMascotService::class.java).setAction(action)
            runCatching { ContextCompat.startForegroundService(context, intent) }
        }
    }
}
