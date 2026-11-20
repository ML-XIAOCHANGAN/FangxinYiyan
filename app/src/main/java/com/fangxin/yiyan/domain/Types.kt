package com.fangxin.yiyan.domain

enum class RiskLevel { RED, AMBER, GREEN }

enum class RiskDecision { RED, AMBER, GREEN, UNKNOWN }

enum class CheckSource { PHOTO, ALBUM, TEXT, SAMPLE }

enum class UserRole { ELDER, GUARDIAN }

const val DEFAULT_RELAY_URL = "https://fangxin-yiyan-oqspdkiq3lk.qoder.zone"

data class TermRule(
    val word: String,
    val id: String,
    val group: String,
    val level: RiskLevel,
    val reason: String,
    val needsRequest: Boolean = false,
)

data class Evidence(
    val id: String,
    val group: String,
    val level: RiskLevel,
    val reason: String,
    val layer: String,
)

data class CheckResult(
    val id: String,
    val risk: RiskDecision,
    val label: String,
    val confidence: Double,
    val plain: String,
    val reasons: List<String>,
    val actions: List<String>,
    val fallback: Boolean = false,
    val fallbackNote: String? = null,
    val source: CheckSource,
    val createdAt: Long,
    val hits: List<String> = emptyList(),
    val ocrText: String? = null,
    val ocrConfidence: Int? = null,
)

data class GuideStep(
    val text: String,
    val tip: String,
)

data class Guide(
    val id: String,
    val category: String,
    val title: String,
    val minutes: String,
    val steps: List<GuideStep>,
)

data class GuideCategory(
    val name: String,
)

data class AppSettings(
    val largeText: Boolean = false,
    val familyPhone: String = "",
    val role: UserRole? = null,
    val pairingCode: String = "",
    val relayUrl: String = DEFAULT_RELAY_URL,
    val guardianRoom: String = "",
    val guardianPaired: Boolean = false,
    val floatingEnabled: Boolean = false,
)

data class AlertEvent(
    val id: String,
    val risk: RiskDecision,
    val label: String,
    val plain: String,
    val maskedText: String,
    val actions: List<String>,
    val source: CheckSource,
    val createdAt: Long,
)

data class HistoryItem(
    val id: String,
    val risk: RiskDecision,
    val label: String,
    val plain: String,
    val reasons: List<String>,
    val actions: List<String>,
    val source: CheckSource,
    val createdAt: Long,
    val fallback: Boolean = false,
    val fallbackNote: String? = null,
) {
    fun toResult(): CheckResult = CheckResult(
        id = id,
        risk = risk,
        label = label,
        confidence = 0.0,
        plain = plain,
        reasons = reasons,
        actions = actions,
        fallback = fallback,
        fallbackNote = fallbackNote,
        source = source,
        createdAt = createdAt,
    )
}

fun CheckResult.toHistoryItem(): HistoryItem = HistoryItem(
    id = id,
    risk = risk,
    label = label,
    plain = plain,
    reasons = reasons,
    actions = actions,
    source = source,
    createdAt = createdAt,
    fallback = fallback,
    fallbackNote = fallbackNote,
)

data class SampleCase(
    val id: String,
    val title: String,
    val text: String,
    val risk: RiskDecision,
    val label: String,
    val plain: String,
    val reasons: List<String>,
    val actions: List<String>,
)
