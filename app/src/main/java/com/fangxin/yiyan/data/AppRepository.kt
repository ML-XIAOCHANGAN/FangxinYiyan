package com.fangxin.yiyan.data

import android.content.Context
import com.fangxin.yiyan.analytics.Analytics
import com.fangxin.yiyan.domain.AlertEvent
import com.fangxin.yiyan.domain.AppSettings
import com.fangxin.yiyan.domain.CheckResult
import com.fangxin.yiyan.domain.CheckSource
import com.fangxin.yiyan.domain.DEFAULT_RELAY_URL
import com.fangxin.yiyan.domain.HistoryItem
import com.fangxin.yiyan.domain.HistoryPolicy
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.domain.UserRole
import com.fangxin.yiyan.domain.toHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.SecureRandom

class AppRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _guardianAlerts = MutableStateFlow(loadAlerts())
    val guardianAlerts: StateFlow<List<AlertEvent>> = _guardianAlerts.asStateFlow()

    private val _settings = MutableStateFlow(
        AppSettings(
            largeText = prefs.getBoolean(KEY_LARGE_TEXT, false),
            familyPhone = prefs.getString(KEY_FAMILY_PHONE, "").orEmpty(),
            role = loadRole(),
            pairingCode = prefs.getString(KEY_PAIRING_CODE, null) ?: newPairingCode().also {
                prefs.edit().putString(KEY_PAIRING_CODE, it).apply()
            },
            relayUrl = prefs.getString(KEY_RELAY_URL, DEFAULT_RELAY_URL) ?: DEFAULT_RELAY_URL,
            guardianRoom = prefs.getString(KEY_GUARDIAN_ROOM, "").orEmpty(),
            guardianPaired = prefs.getBoolean(KEY_GUARDIAN_PAIRED, false),
            floatingEnabled = prefs.getBoolean(KEY_FLOATING, false),
        ),
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadRole(): UserRole? {
        val stored = prefs.getString(KEY_ROLE, null)
        if (stored != null) {
            return UserRole.values().firstOrNull { it.name == stored }
        }
        // 老版本升级上来：已经用过的手机默认还是老人界面，不打断。
        val usedBefore = prefs.contains(KEY_HISTORY) || prefs.contains(KEY_FAMILY_PHONE) || prefs.contains(KEY_LARGE_TEXT)
        return if (usedBefore) {
            prefs.edit().putString(KEY_ROLE, UserRole.ELDER.name).apply()
            UserRole.ELDER
        } else {
            null
        }
    }

    fun addHistory(result: CheckResult) {
        val updated = HistoryPolicy.trim(listOf(result.toHistoryItem()) + _history.value)
        _history.value = updated
        prefs.edit().putString(KEY_HISTORY, encodeHistory(updated)).apply()
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun setLargeText(enabled: Boolean) {
        _settings.value = _settings.value.copy(largeText = enabled)
        prefs.edit().putBoolean(KEY_LARGE_TEXT, enabled).apply()
    }

    fun setFloating(enabled: Boolean) {
        _settings.value = _settings.value.copy(floatingEnabled = enabled)
        prefs.edit().putBoolean(KEY_FLOATING, enabled).apply()
    }

    fun setFamilyPhone(raw: String) {
        val phone = raw.filter { it.isDigit() }.take(15)
        _settings.value = _settings.value.copy(familyPhone = phone)
        prefs.edit().putString(KEY_FAMILY_PHONE, phone).apply()
    }

    fun setRole(role: UserRole?) {
        _settings.value = _settings.value.copy(role = role)
        prefs.edit().putString(KEY_ROLE, role?.name.orEmpty()).apply()
    }

    fun regeneratePairingCode(): String {
        val code = newPairingCode()
        _settings.value = _settings.value.copy(pairingCode = code)
        prefs.edit().putString(KEY_PAIRING_CODE, code).apply()
        return code
    }

    fun setGuardianRoom(code: String, paired: Boolean) {
        _settings.value = _settings.value.copy(guardianRoom = code, guardianPaired = paired)
        prefs.edit()
            .putString(KEY_GUARDIAN_ROOM, code)
            .putBoolean(KEY_GUARDIAN_PAIRED, paired)
            .apply()
    }

    fun addGuardianAlert(alert: AlertEvent) {
        val isNew = _guardianAlerts.value.none { it.id == alert.id }
        val updated = (listOf(alert) + _guardianAlerts.value.filterNot { it.id == alert.id }).take(MAX_ALERTS)
        _guardianAlerts.value = updated
        prefs.edit().putString(KEY_GUARDIAN_ALERTS, encodeAlerts(updated)).apply()
        if (isNew) Analytics.guardAlert(appContext, alert.id, alert.risk)
    }

    fun clearGuardianAlerts() {
        _guardianAlerts.value = emptyList()
        prefs.edit().remove(KEY_GUARDIAN_ALERTS).apply()
    }

    private fun loadAlerts(): List<AlertEvent> {
        val raw = prefs.getString(KEY_GUARDIAN_ALERTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        AlertEvent(
                            id = item.optString("id"),
                            risk = riskOf(item.optString("risk")),
                            label = item.optString("label"),
                            plain = item.optString("plain"),
                            maskedText = item.optString("maskedText"),
                            actions = item.optJSONArray("actions").toStringList(),
                            source = sourceOf(item.optString("source")),
                            createdAt = item.optLong("createdAt"),
                        ),
                    )
                }
            }
        } catch (error: JSONException) {
            emptyList()
        }
    }

    private fun encodeAlerts(items: List<AlertEvent>): String {
        val array = JSONArray()
        for (item in items) {
            val json = JSONObject()
            json.put("id", item.id)
            json.put("risk", item.risk.name)
            json.put("label", item.label)
            json.put("plain", item.plain)
            json.put("maskedText", item.maskedText)
            json.put("actions", JSONArray(item.actions))
            json.put("source", item.source.name)
            json.put("createdAt", item.createdAt)
            array.put(json)
        }
        return array.toString()
    }

    private fun newPairingCode(): String {
        val value = 100_000_000L + SecureRandom().nextInt(900_000_000)
        return value.toString()
    }

    private fun loadHistory(): List<HistoryItem> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        HistoryItem(
                            id = item.optString("id"),
                            risk = riskOf(item.optString("risk")),
                            label = item.optString("label"),
                            plain = item.optString("plain"),
                            reasons = item.optJSONArray("reasons").toStringList(),
                            actions = item.optJSONArray("actions").toStringList(),
                            source = sourceOf(item.optString("source")),
                            createdAt = item.optLong("createdAt"),
                            fallback = item.optBoolean("fallback"),
                            fallbackNote = if (item.isNull("fallbackNote")) {
                                null
                            } else {
                                item.optString("fallbackNote").ifBlank { null }
                            },
                        ),
                    )
                }
            }
        } catch (error: JSONException) {
            emptyList()
        }
    }

    private fun encodeHistory(items: List<HistoryItem>): String {
        val array = JSONArray()
        for (item in items) {
            val json = JSONObject()
            json.put("id", item.id)
            json.put("risk", item.risk.name)
            json.put("label", item.label)
            json.put("plain", item.plain)
            json.put("reasons", JSONArray(item.reasons))
            json.put("actions", JSONArray(item.actions))
            json.put("source", item.source.name)
            json.put("createdAt", item.createdAt)
            json.put("fallback", item.fallback)
            json.put("fallbackNote", item.fallbackNote ?: JSONObject.NULL)
            array.put(json)
        }
        return array.toString()
    }

    private fun riskOf(value: String): RiskDecision =
        RiskDecision.values().firstOrNull { it.name == value } ?: RiskDecision.UNKNOWN

    private fun sourceOf(value: String): CheckSource =
        CheckSource.values().firstOrNull { it.name == value } ?: CheckSource.TEXT

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) add(optString(index))
        }
    }

    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun get(context: Context): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }

        private const val PREFS_NAME = "fangxin_yiyan"
        const val KEY_HISTORY = "history"
        const val KEY_LARGE_TEXT = "large_text"
        const val KEY_FAMILY_PHONE = "family_phone"
        const val KEY_ROLE = "role"
        const val KEY_PAIRING_CODE = "pairing_code"
        const val KEY_RELAY_URL = "relay_url"
        const val KEY_GUARDIAN_ROOM = "guardian_room"
        const val KEY_GUARDIAN_PAIRED = "guardian_paired"
        const val KEY_GUARDIAN_ALERTS = "guardian_alerts"
        const val KEY_FLOATING = "floating_enabled"
        const val MAX_ALERTS = 20
    }
}
