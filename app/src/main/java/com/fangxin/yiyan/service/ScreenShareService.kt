package com.fangxin.yiyan.service

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.fangxin.yiyan.link.GuardianNotify
import com.fangxin.yiyan.link.LinkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * 老人端屏幕共享：每次开启都要本机同意，最多共享 5 分钟。
 * 画面只走配对通道给守护人，不落盘。
 */
class ScreenShareService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var loopJob: Job? = null

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        @Suppress("DEPRECATION")
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        if (resultCode != Activity.RESULT_OK || resultData == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = GuardianNotify.screenShare(this, stopIntent())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }

        val manager = getSystemService(MediaProjectionManager::class.java) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        val mediaProjection = manager.getMediaProjection(resultCode, resultData) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        projection = mediaProjection
        mediaProjection.registerCallback(projectionCallback, Handler(Looper.getMainLooper()))

        val metrics = resources.displayMetrics
        val scale = TARGET_WIDTH.toFloat() / metrics.widthPixels.coerceAtLeast(1)
        val width = (metrics.widthPixels * scale).toInt().coerceAtLeast(240)
        val height = (metrics.heightPixels * scale).toInt().coerceAtLeast(240)
        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        reader = imageReader
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "fangxin-screen",
            width,
            height,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null,
        )

        LinkClient.sendScreenSharing(true)
        loopJob = scope.launch {
            while (isActive) {
                delay(FRAME_INTERVAL_MS)
                pushFrame()
            }
        }
        scope.launch {
            delay(MAX_DURATION_MS)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun pushFrame() {
        val image = runCatching { reader?.acquireLatestImage() }.getOrNull() ?: return
        try {
            val plane = image.planes.firstOrNull() ?: return
            val paddedWidth = plane.rowStride / plane.pixelStride
            val bitmap = Bitmap.createBitmap(paddedWidth, image.height, Bitmap.Config.ARGB_8888)
            plane.buffer.rewind()
            bitmap.copyPixelsFromBuffer(plane.buffer)
            val cropped = if (paddedWidth > image.width) {
                Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
            } else {
                bitmap
            }
            val stream = ByteArrayOutputStream()
            cropped.compress(Bitmap.CompressFormat.JPEG, 45, stream)
            LinkClient.sendFrame(stream.toByteArray(), cropped.width, cropped.height)
            if (cropped !== bitmap) cropped.recycle()
            bitmap.recycle()
        } finally {
            image.close()
        }
    }

    private fun stopIntent(): Intent = Intent(this, ScreenShareService::class.java).apply {
        action = ACTION_STOP
    }

    override fun onDestroy() {
        loopJob?.cancel()
        scope.cancel()
        LinkClient.sendScreenSharing(false)
        reader?.close()
        reader = null
        virtualDisplay?.release()
        virtualDisplay = null
        projection?.unregisterCallback(projectionCallback)
        projection?.stop()
        projection = null
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 72
        private const val TARGET_WIDTH = 480
        private const val FRAME_INTERVAL_MS = 1_000L
        private const val MAX_DURATION_MS = 5 * 60_000L
        const val ACTION_STOP = "com.fangxin.yiyan.action.STOP_SCREEN"
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_RESULT_DATA = "result_data"

        fun start(context: Context, resultCode: Int, resultData: Intent) {
            val intent = Intent(context, ScreenShareService::class.java).apply {
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, resultData)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenShareService::class.java))
        }
    }
}
