package com.fangxin.yiyan.analytics

import android.content.Context
import com.fangxin.yiyan.domain.CheckResult
import com.fangxin.yiyan.domain.CheckSource
import com.fangxin.yiyan.domain.DEFAULT_RELAY_URL
import com.fangxin.yiyan.domain.RiskDecision
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.concurrent.Executors

/**
 * 使用上报：只发类型、结果、规则编号和耗时，不带照片和原文。
 * 失败静默，不影响老人正常用。
 */
object Analytics {
    private const val PREFS = "fy.analytics"
    private const val KEY_DEVICE = "device"
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var deviceId = ""

    fun appOpen(context: Context, role: String?) {
        send(context, "app_open", ref = role)
    }

    fun guideView(context: Context, guideId: String) {
        if (guideId.isBlank()) return
        send(context, "guide_view", ref = guideId)
    }

    fun check(context: Context, result: CheckResult, durationMs: Long?) {
        send(
            context,
            "check",
            risk = riskName(result.risk),
            source = result.source.name.lowercase(),
            category = categoryOf(result),
            durationMs = durationMs,
        )
    }

    fun guardAlert(context: Context, id: String, risk: RiskDecision) {
        send(context, "guard_alert", risk = riskName(risk), ref = id)
    }

    private fun riskName(risk: RiskDecision): String = when (risk) {
        RiskDecision.RED -> "red"
        RiskDecision.AMBER -> "amber"
        RiskDecision.GREEN -> "green"
        RiskDecision.UNKNOWN -> "unknown"
    }

    private fun categoryOf(result: CheckResult): String {
        result.hits.firstOrNull { !it.startsWith("model-") }?.let { return it }
        if (result.source == CheckSource.SAMPLE) return "sample"
        return if (result.fallback) "unclear" else "model"
    }

    private fun device(context: Context): String {
        if (deviceId.isNotEmpty()) return deviceId
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_DEVICE, null)
        if (!saved.isNullOrBlank()) {
            deviceId = saved
            return saved
        }
        val random = ByteArray(12)
        SecureRandom().nextBytes(random)
        val created = "d" + random.joinToString("") { "%02x".format(it) }
        prefs.edit().putString(KEY_DEVICE, created).apply()
        deviceId = created
        return created
    }

    private fun send(
        context: Context,
        kind: String,
        risk: String? = null,
        source: String? = null,
        category: String? = null,
        ref: String? = null,
        durationMs: Long? = null,
    ) {
        val app = context.applicationContext
        val payload = JSONObject().apply {
            put("kind", kind)
            put("deviceId", device(app))
            put("platform", "android")
            if (risk != null) put("risk", risk)
            if (source != null) put("source", source)
            if (category != null) put("category", category)
            if (ref != null) put("ref", ref)
            if (durationMs != null) put("durationMs", durationMs)
            put("occurredAt", System.currentTimeMillis())
        }.toString()

        executor.execute {
            runCatching {
                val connection = URL("$DEFAULT_RELAY_URL/functions/v1/app/api/events").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                connection.doOutput = true
                connection.setRequestProperty("content-type", "application/json")
                connection.outputStream.use { it.write(payload.toByteArray()) }
                connection.inputStream.use { it.readBytes() }
                connection.disconnect()
            }
        }
    }
}
