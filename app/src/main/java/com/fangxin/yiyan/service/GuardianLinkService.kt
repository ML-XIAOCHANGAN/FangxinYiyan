package com.fangxin.yiyan.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.fangxin.yiyan.data.AppRepository
import com.fangxin.yiyan.domain.UserRole
import com.fangxin.yiyan.link.GuardianNotify
import com.fangxin.yiyan.link.LinkClient
import com.fangxin.yiyan.link.LinkEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** 守护人端常驻连接：保持配对在线，收到警报就存下来并弹通知。 */
class GuardianLinkService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var collector: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val repository = AppRepository.get(this)
        val settings = repository.settings.value
        if (settings.role != UserRole.GUARDIAN || !settings.guardianPaired) {
            stopSelf()
            return START_NOT_STICKY
        }

        GuardianNotify.ensureChannels(this)
        val notification = GuardianNotify.guardService(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIF_ID, notification)
        }

        LinkClient.connect(settings.relayUrl, settings.guardianRoom, UserRole.GUARDIAN)
        collector?.cancel()
        collector = scope.launch {
            LinkClient.events.collectLatest { event ->
                if (event is LinkEvent.Alert) {
                    repository.addGuardianAlert(event.alert)
                    FloatingMascotService.setMood(applicationContext, MascotMood.ALERT)
                    GuardianNotify.postAlert(applicationContext, event.alert)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        collector?.cancel()
        scope.cancel()
        // 切回老人界面时，新连接可能已经建立，别把它断开。
        if (AppRepository.get(this).settings.value.role != UserRole.ELDER) {
            LinkClient.disconnect()
        }
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 71
        const val ACTION_STOP = "com.fangxin.yiyan.action.STOP_GUARD"

        fun start(context: Context) {
            val intent = Intent(context, GuardianLinkService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GuardianLinkService::class.java))
        }
    }
}
