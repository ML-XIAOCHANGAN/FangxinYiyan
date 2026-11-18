package com.fangxin.yiyan.link

import android.util.Base64
import com.fangxin.yiyan.domain.AlertEvent
import com.fangxin.yiyan.domain.CheckSource
import com.fangxin.yiyan.domain.DEFAULT_RELAY_URL
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.domain.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

data class LinkStatus(
    val connected: Boolean = false,
    val peerOnline: Boolean = false,
)

sealed interface LinkEvent {
    data class Alert(val alert: AlertEvent) : LinkEvent
    data class ScreenSharing(val on: Boolean) : LinkEvent
    data class Frame(val jpeg: ByteArray, val width: Int, val height: Int) : LinkEvent
    data object ScreenRequested : LinkEvent
}

/**
 * 老人端与守护人端共用的配对连接。
 * 走 Qoder Sites 上的中转：只转发，不留账号，消息被对面读走就删。
 */
object LinkClient {
    private const val POLL_MS = 1_500L
    private const val START_BACKOFF_MS = 2_000L
    private const val MAX_BACKOFF_MS = 10_000L
    private const val JSON_TYPE = "application/json; charset=utf-8"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val jsonType = JSON_TYPE.toMediaType()

    private val _status = MutableStateFlow(LinkStatus())
    val status: StateFlow<LinkStatus> = _status.asStateFlow()

    private val _screenSharing = MutableStateFlow(false)
    val screenSharing: StateFlow<Boolean> = _screenSharing.asStateFlow()

    private val _events = MutableSharedFlow<LinkEvent>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<LinkEvent> = _events.asSharedFlow()

    private data class Config(
        val origin: String,
        val base: String,
        val room: String,
        val role: UserRole,
    ) {
        val roleKey: String get() = if (role == UserRole.GUARDIAN) "guardian" else "elder"
        val peerKey: String get() = if (role == UserRole.GUARDIAN) "elder" else "guardian"
    }

    private data class FramePayload(val jpeg: ByteArray, val width: Int, val height: Int)

    private var config: Config? = null
    private var jobs: List<Job> = emptyList()
    private var outbox: Channel<JSONObject>? = null
    private var frameSlot: Channel<FramePayload>? = null
    private var frameSeq = 0L

    fun connect(url: String, room: String, role: UserRole) {
        if (!isValidRoom(room)) return
        val target = parseConfig(url, room, role)
        if (config == target && jobs.any { it.isActive }) return
        disconnect()
        config = target
        frameSeq = 0L
        val messages = Channel<JSONObject>(Channel.BUFFERED)
        val frames = Channel<FramePayload>(Channel.CONFLATED)
        outbox = messages
        frameSlot = frames
        jobs = listOf(
            scope.launch { pollLoop(target) },
            scope.launch { sendLoop(target, messages) },
            scope.launch { frameLoop(target, frames) },
        )
    }

    fun disconnect() {
        jobs.forEach { it.cancel() }
        jobs = emptyList()
        outbox?.close()
        outbox = null
        frameSlot?.close()
        frameSlot = null
        config = null
        frameSeq = 0L
        _screenSharing.value = false
        _status.value = LinkStatus()
    }

    fun sendAlert(alert: AlertEvent): Boolean {
        if (!_status.value.connected) return false
        val message = JSONObject().put("type", "alert").put("alert", alert.toJson())
        return outbox?.trySend(message)?.isSuccess == true
    }

    fun sendScreenSharing(on: Boolean) {
        _screenSharing.value = on
        val target = config ?: return
        outbox?.trySend(JSONObject().put("type", "screen").put("on", on))
        if (!on) {
            // 停止共享时顺手把云端那帧删掉，家人那边马上看不到。
            scope.launch {
                runCatching {
                    post(
                        target,
                        "/stop",
                        JSONObject().put("room", target.room).put("role", target.roleKey),
                    )
                }
            }
        }
    }

    fun sendFrame(jpeg: ByteArray, width: Int, height: Int) {
        if (!_screenSharing.value) return
        frameSlot?.trySend(FramePayload(jpeg, width, height))
    }

    fun requestScreen(): Boolean {
        if (!_status.value.connected) return false
        return outbox?.trySend(JSONObject().put("type", "screen_request"))?.isSuccess == true
    }

    private suspend fun pollLoop(target: Config) {
        var joined = false
        var backoff = START_BACKOFF_MS
        while (config == target) {
            try {
                if (!joined) {
                    val joinedBody = post(
                        target,
                        "/join",
                        JSONObject().put("room", target.room).put("role", target.roleKey),
                    )
                    frameSeq = joinedBody.optLong("frameSeq", 0L)
                    applyPresence(target, joinedBody.optJSONObject("presence"))
                    joined = true
                } else {
                    val body = post(
                        target,
                        "/poll",
                        JSONObject()
                            .put("room", target.room)
                            .put("role", target.roleKey)
                            .put("frameSeq", frameSeq),
                    )
                    applyPresence(target, body.optJSONObject("presence"))
                    body.optJSONArray("messages")?.let { array ->
                        for (index in 0 until array.length()) {
                            array.optJSONObject(index)?.let(::handleMessage)
                        }
                    }
                    body.optJSONObject("frame")?.let { frame ->
                        frameSeq = frame.optLong("seq", frameSeq)
                        emitFrame(frame)
                    }
                }
                backoff = START_BACKOFF_MS
                delay(POLL_MS)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                joined = false
                _status.value = LinkStatus(connected = false, peerOnline = false)
                delay(backoff)
                backoff = (backoff * 2).coerceAtMost(MAX_BACKOFF_MS)
            }
        }
    }

    private suspend fun sendLoop(target: Config, messages: Channel<JSONObject>) {
        for (message in messages) {
            var attempt = 0
            while (attempt < 3) {
                attempt++
                try {
                    post(
                        target,
                        "/send",
                        JSONObject()
                            .put("room", target.room)
                            .put("role", target.roleKey)
                            .put("message", message),
                    )
                    break
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (error: Exception) {
                    if (attempt >= 3) break
                    delay(1_000L * attempt)
                }
            }
        }
    }

    private suspend fun frameLoop(target: Config, frames: Channel<FramePayload>) {
        for (frame in frames) {
            runCatching {
                post(
                    target,
                    "/frame",
                    JSONObject()
                        .put("room", target.room)
                        .put("role", target.roleKey)
                        .put("w", frame.width)
                        .put("h", frame.height)
                        .put("data", Base64.encodeToString(frame.jpeg, Base64.NO_WRAP)),
                )
            }
        }
    }

    private fun post(target: Config, path: String, body: JSONObject): JSONObject {
        val request = Request.Builder()
            .url(target.base + path)
            .header("Origin", target.origin)
            .post(body.toString().toRequestBody(jsonType))
            .build()
        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("relay ${response.code}")
            return JSONObject(text)
        }
    }

    private fun applyPresence(target: Config, presence: JSONObject?) {
        _status.value = LinkStatus(
            connected = true,
            peerOnline = presence?.optBoolean(target.peerKey, false) == true,
        )
    }

    private fun emitFrame(frame: JSONObject) {
        val data = frame.optString("data")
        if (data.isEmpty()) return
        val bytes = runCatching { Base64.decode(data, Base64.NO_WRAP) }.getOrNull() ?: return
        _events.tryEmit(LinkEvent.Frame(bytes, frame.optInt("w", 0), frame.optInt("h", 0)))
    }

    private fun handleMessage(json: JSONObject) {
        when (json.optString("type")) {
            "alert" -> json.optJSONObject("alert")?.toAlertEvent()?.let {
                _events.tryEmit(LinkEvent.Alert(it))
            }
            "screen" -> _events.tryEmit(LinkEvent.ScreenSharing(json.optBoolean("on")))
            "screen_request" -> _events.tryEmit(LinkEvent.ScreenRequested)
        }
    }

    private fun isValidRoom(room: String): Boolean =
        (room.length == 6 || room.length == 9) && room.all { it.isDigit() }

    private fun parseConfig(url: String, room: String, role: UserRole): Config {
        val origin = trustedOrigin(url)
        val base = "$origin/functions/v1/app/relay"
        return Config(origin = origin, base = base, room = room, role = role)
    }

    // 只认官方中转主机；预置里被改成别的地址也回落到默认，避免警报和屏幕被导走。
    private fun trustedOrigin(url: String): String {
        val allowedHost = runCatching { URI(DEFAULT_RELAY_URL).host?.lowercase() }.getOrNull()
            ?: return DEFAULT_RELAY_URL
        val candidate = when {
            url.isBlank() -> DEFAULT_RELAY_URL
            url.startsWith("wss://") -> "https://" + url.removePrefix("wss://")
            url.startsWith("ws://") -> "http://" + url.removePrefix("ws://")
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https://$url"
        }.trim().trimEnd('/').removeSuffix("/relay")
        val parsed = runCatching { URI(candidate.substringBefore("/functions")) }.getOrNull()
        val secure = parsed?.scheme?.equals("https", ignoreCase = true) == true
        val hostMatches = parsed?.host?.equals(allowedHost, ignoreCase = true) == true
        return if (secure && hostMatches) DEFAULT_RELAY_URL.trimEnd('/') else DEFAULT_RELAY_URL
    }
}

fun AlertEvent.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("risk", risk.name)
    .put("label", label)
    .put("plain", plain)
    .put("maskedText", maskedText)
    .put("actions", JSONArray(actions))
    .put("source", source.name)
    .put("createdAt", createdAt)

fun JSONObject.toAlertEvent(): AlertEvent = AlertEvent(
    id = optString("id"),
    risk = RiskDecision.values().firstOrNull { it.name == optString("risk") } ?: RiskDecision.UNKNOWN,
    label = optString("label"),
    plain = optString("plain"),
    maskedText = optString("maskedText"),
    actions = optJSONArray("actions").toStringList(),
    source = CheckSource.values().firstOrNull { it.name == optString("source") } ?: CheckSource.TEXT,
    createdAt = optLong("createdAt", System.currentTimeMillis()),
)

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) add(optString(index))
    }
}
