package com.fangxin.yiyan.domain

import java.net.URI
import java.text.Normalizer
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

object RiskCheck {
    private val ZERO_WIDTH = Regex("[\\u200B-\\u200D\\uFEFF]")
    private val SPACES = Regex("\\s+")
    private val CJK = Regex("[\\u4e00-\\u9fff]")
    private val LEGIT_CODE = Regex("验证码(为|是|：|:)\\s*\\d{4,8}")
    private val IP_HOST = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")

    fun analyzeText(
        rawText: String,
        source: CheckSource = CheckSource.TEXT,
        ocrConfidence: Int? = null,
        ocrText: String? = null,
    ): CheckResult {
        val text = cleanText(rawText)
        val domains = inspectDomains(text)
        val ruleEvidence = analyzeRules(text)
        val semantics = analyzeSemantics(text, domains)
        val evidence = ruleEvidence + semantics.evidence

        if (evidence.any { it.level == RiskLevel.RED }) {
            return baseResult(
                RiskDecision.RED, 0.93, uniqueReasons(evidence, RiskDecision.RED),
                evidence, source, ocrText, ocrConfidence,
            )
        }

        val cjkCount = CJK.findAll(text).count()
        val tooLittle = text.length < 10 || (cjkCount < 3 && !domains.hasLink)
        val lowOcr = ocrConfidence != null && ocrConfidence < 45
        if (tooLittle || (lowOcr && evidence.isEmpty())) {
            return fallback(source, ocrText, ocrConfidence)
        }

        val amberGroups = evidence
            .filter { it.level == RiskLevel.AMBER && it.group != "family" }
            .map { it.group }
            .toSet()
        if (amberGroups.size >= 2) {
            return baseResult(
                RiskDecision.RED, 0.82, uniqueReasons(evidence, RiskDecision.RED),
                evidence, source, ocrText, ocrConfidence,
            )
        }

        if (evidence.isNotEmpty()) {
            return baseResult(
                RiskDecision.AMBER, 0.68, uniqueReasons(evidence, RiskDecision.AMBER),
                evidence, source, ocrText, ocrConfidence,
            )
        }

        val greenConfidence = if (semantics.safe.isNotEmpty()) 0.86 else 0.65
        val reasons = uniqueReasons(emptyList(), RiskDecision.GREEN).toMutableList()
        semantics.safe.firstOrNull()?.let { reasons[0] = clip(it) }
        return baseResult(
            RiskDecision.GREEN, greenConfidence, reasons,
            emptyList(), source, ocrText, ocrConfidence,
        )
    }

    fun fallback(
        source: CheckSource,
        ocrText: String? = null,
        ocrConfidence: Int? = null,
    ): CheckResult = baseResult(
        RiskDecision.AMBER,
        0.3,
        emptyList(),
        emptyList(),
        source,
        ocrText,
        ocrConfidence,
        fallback = true,
        fallbackNote = Copy.FALLBACK_NOTE,
    )

    private fun cleanText(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFKC)
        .replace(ZERO_WIDTH, "")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .trim()

    private fun clip(text: String, max: Int = 20): String {
        val normalized = SPACES.replace(text, " ").trim()
        return if (normalized.length > max) normalized.take(max - 1) + "…" else normalized
    }

    private fun isProtectiveMention(text: String, index: Int, length: Int): Boolean {
        val before = text.substring(max(0, index - 16), index)
        val after = text.substring(min(text.length, index + length), min(text.length, index + length + 12))
        return RiskRules.PROTECTIVE_BEFORE.containsMatchIn(before) ||
            RiskRules.PROTECTIVE_AFTER.containsMatchIn(after)
    }

    private fun hasRequestContext(text: String, index: Int, length: Int): Boolean {
        val window = text.substring(max(0, index - 18), min(text.length, index + length + 18))
        return RiskRules.REQUEST_WORDS.containsMatchIn(window)
    }

    private fun isLegitCodeNotice(text: String, index: Int, length: Int): Boolean {
        val near = text.substring(max(0, index - 6), min(text.length, index + length + 12))
        return LEGIT_CODE.containsMatchIn(near) && !hasRequestContext(text, index, length)
    }

    private fun termEvidence(text: String, terms: List<TermRule>): List<Evidence> {
        val found = mutableListOf<Evidence>()
        for (term in terms) {
            val index = text.indexOf(term.word, ignoreCase = true)
            if (index < 0) continue
            if (term.needsRequest && !hasRequestContext(text, index, term.word.length)) continue
            if (isProtectiveMention(text, index, term.word.length)) continue
            if (term.id == "cred-code" && isLegitCodeNotice(text, index, term.word.length)) continue
            if (found.any { it.id == term.id }) continue
            found += Evidence(term.id, term.group, term.level, term.reason, "rule")
        }
        return found
    }

    private fun analyzeRules(text: String): List<Evidence> = buildList {
        addAll(termEvidence(text, RiskRules.CREDENTIAL_TERMS))
        addAll(termEvidence(text, RiskRules.MONEY_TERMS))
        addAll(termEvidence(text, RiskRules.IMPERSONATION_TERMS))
        addAll(termEvidence(text, RiskRules.FUNNEL_TERMS))
    }

    private data class UrlInfo(val host: String)

    private data class DomainFinding(
        val hasLink: Boolean,
        val officialLink: Boolean,
        val evidence: List<Evidence>,
    )

    private data class Semantics(
        val evidence: List<Evidence>,
        val safe: List<String>,
    )

    private fun normalizeHost(raw: String): String {
        val value = raw.trim().trimEnd(
            '。', '，', '；', '！', '？', '、', '）', '】', '"', '\'', '”', '’',
        )
        return try {
            val target = if (value.startsWith("http", ignoreCase = true)) value else "http://$value"
            val host = URI(target).host ?: return ""
            host.lowercase().removePrefix("www.")
        } catch (error: Exception) {
            ""
        }
    }

    private fun extractHosts(text: String): List<UrlInfo> {
        val seen = mutableSetOf<String>()
        val hosts = mutableListOf<UrlInfo>()
        for (match in RiskRules.URL.findAll(text)) {
            val host = normalizeHost(match.value)
            if (host.isNotEmpty() && seen.add(host)) hosts += UrlInfo(host)
        }
        return hosts
    }

    private fun isOfficialHost(host: String): Boolean =
        RiskRules.OFFICIAL_DOMAIN_SUFFIXES.any { host == it || host.endsWith(".$it") }

    private fun isShortHost(host: String): Boolean =
        RiskRules.SHORT_LINK_HOSTS.any { host == it || host.endsWith(".$it") }

    private fun inspectDomains(text: String): DomainFinding {
        val hosts = extractHosts(text)
        val evidence = mutableListOf<Evidence>()
        var officialLink = false

        fun add(id: String, group: String, reason: String) {
            if (evidence.any { it.id == id }) return
            evidence += Evidence(id, group, RiskLevel.AMBER, reason, "semantic")
        }

        for (item in hosts) {
            val host = item.host
            if (isOfficialHost(host)) {
                officialLink = true
                continue
            }
            if (isShortHost(host)) add("link-short", "link", "链接是短链接，看不出真假")
            if (IP_HOST.matches(host)) add("link-ip", "link", "链接是一串数字，不像官网")
            if (host.contains("xn--")) add("link-puny", "link", "链接是编码过的怪地址")
            if (RiskRules.BRAND_TOKENS.any { host.contains(it) }) {
                add("link-lookalike", "link", "域名很像官网，但不是")
            }
            if (RiskRules.OFFICIAL_BRANDS.containsMatchIn(text) &&
                RiskRules.REQUEST_ACTION_WORDS.containsMatchIn(text)
            ) {
                add("link-unofficial", "link-official", "链接不是官方网站")
            }
            if (RiskRules.REQUEST_ACTION_WORDS.containsMatchIn(text)) {
                add("link-form", "link-form", "点开就要你填资料")
            }
        }

        return DomainFinding(hosts.isNotEmpty(), officialLink, evidence)
    }

    private fun analyzeSemantics(text: String, domains: DomainFinding): Semantics {
        val evidence = mutableListOf<Evidence>()
        val safe = mutableListOf<String>()

        for (item in termEvidence(text, RiskRules.URGENCY_TERMS)) {
            evidence += if (domains.hasLink) {
                item.copy(level = RiskLevel.RED, reason = "催你马上办，还带你点链接")
            } else {
                item
            }
        }

        evidence += termEvidence(text, RiskRules.REFUND_TERMS)
        evidence += termEvidence(text, RiskRules.LOAN_TERMS)
        evidence += termEvidence(text, RiskRules.INVEST_TERMS)
        evidence += termEvidence(text, RiskRules.PRIZE_TERMS)
        evidence += domains.evidence

        val familyBorrow = RiskRules.FAMILY_CHANGE_PATTERN.containsMatchIn(text) &&
            RiskRules.FAMILY_MONEY_PATTERN.containsMatchIn(text) &&
            (RiskRules.FAMILY_IDENTITY_PATTERN.containsMatchIn(text) ||
                RiskRules.FAMILY_BORROW_PATTERN.containsMatchIn(text))

        if (familyBorrow) {
            evidence += Evidence(
                "family-borrow", "family", RiskLevel.AMBER,
                "说是家人，但换了号码要钱", "semantic",
            )
        }

        if (RiskRules.TYPO_TERMS.any { text.contains(it) } && domains.hasLink) {
            evidence += Evidence("typo", "typo", RiskLevel.AMBER, "文字里有仿造的错别字", "semantic")
        }

        if (RiskRules.SAFE_SENDER_PATTERN.containsMatchIn(text)) safe += "像是官方号码发的"
        if (RiskRules.NOTIFICATION_WORDS.containsMatchIn(text)) safe += "内容像普通通知"
        if (domains.officialLink) safe += "链接是官方网站"

        val kept = if (familyBorrow) evidence.filter { it.group != "transfer-soft" } else evidence
        return Semantics(kept, safe)
    }

    private fun uniqueReasons(evidence: List<Evidence>, decision: RiskDecision): List<String> {
        val ranked = evidence.sortedWith(
            compareBy<Evidence>(
                { if (it.level == RiskLevel.RED) 0 else 1 },
                { if (it.layer == "rule") 0 else 1 },
            ),
        )
        val groups = mutableSetOf<String>()
        val reasons = mutableListOf<String>()
        for (item in ranked) {
            if (!groups.add(item.group)) continue
            reasons += clip(item.reason)
            if (reasons.size == 3) break
        }
        for (fallback in Copy.defaultReasons(decision)) {
            if (reasons.size == 3) break
            if (reasons.none { it == fallback }) reasons += clip(fallback)
        }
        return reasons.take(3)
    }

    private fun pickActions(decision: RiskDecision, evidence: List<Evidence>): List<String> {
        if (decision == RiskDecision.RED &&
            evidence.any { it.group == "transfer" || it.group == "credential" }
        ) {
            return Copy.ACTIONS_TRANSFER_RED
        }
        return Copy.actions(decision)
    }

    private fun requestId(): String = try {
        UUID.randomUUID().toString()
    } catch (error: Exception) {
        "req-${System.currentTimeMillis().toString(36)}-${(0..999999).random()}"
    }

    private fun baseResult(
        decision: RiskDecision,
        confidence: Double,
        reasons: List<String>,
        evidence: List<Evidence>,
        source: CheckSource,
        ocrText: String? = null,
        ocrConfidence: Int? = null,
        fallback: Boolean = false,
        fallbackNote: String? = null,
    ): CheckResult {
        val level = if (fallback) RiskDecision.UNKNOWN else decision
        return CheckResult(
            id = requestId(),
            risk = level,
            label = Copy.LABELS.getValue(level),
            confidence = confidence,
            plain = Copy.PLAIN.getValue(level),
            reasons = reasons,
            actions = pickActions(level, evidence),
            fallback = fallback,
            fallbackNote = fallbackNote,
            source = source,
            createdAt = System.currentTimeMillis(),
            hits = evidence.map { it.id },
            ocrText = ocrText,
            ocrConfidence = ocrConfidence,
        )
    }
}
