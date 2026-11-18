package com.fangxin.yiyan.domain

object Copy {
    val LABELS: Map<RiskDecision, String> = mapOf(
        RiskDecision.RED to "很可能是骗子",
        RiskDecision.AMBER to "有点可疑",
        RiskDecision.GREEN to "没看出问题",
        RiskDecision.UNKNOWN to "没看清",
    )

    val PLAIN: Map<RiskDecision, String> = mapOf(
        RiskDecision.RED to "先停下，这很可能是骗子。",
        RiskDecision.AMBER to "先别急着办。",
        RiskDecision.GREEN to "没看出问题。动钱先核实。",
        RiskDecision.UNKNOWN to "没看清，先别急着办。",
    )

    val ACTIONS_RED: List<String> = listOf(
        "别点，别转账，别填信息",
        "打 96110 或官方电话问",
        "把截图发给家人",
    )

    val ACTIONS_AMBER: List<String> = listOf(
        "先放一放，别急着办",
        "让家人看一眼",
        "问清楚再操作",
    )

    val ACTIONS_GREEN: List<String> = listOf(
        "动钱前，先跟家人说一声",
        "从官方软件里操作",
        "拿不准就打 96110",
    )

    val ACTIONS_UNKNOWN: List<String> = listOf(
        "先放一放，别急着办",
        "打 96110 问一句",
        "让家人帮你看一眼",
    )

    val ACTIONS_TRANSFER_RED: List<String> = listOf(
        "马上停下，别再转钱",
        "打银行客服或 96110",
        "告诉家人，截图留好",
    )

    const val FALLBACK_NOTE = "没看清。先别急着办，重拍一张，或者把字贴进来。"

    val DEFAULT_REASONS_RED: List<String> = listOf(
        "有几处很不对劲",
        "正规单位不会这么催",
        "先停下来最稳",
    )

    val DEFAULT_REASONS_AMBER: List<String> = listOf(
        "有一处看着不对劲",
        "信息不全，没法放心",
        "先问清楚最稳",
    )

    val DEFAULT_REASONS_GREEN: List<String> = listOf(
        "没看到常见的骗法",
        "没要验证码和卡号",
        "没催你马上转钱",
    )

    fun actions(decision: RiskDecision): List<String> = when (decision) {
        RiskDecision.RED -> ACTIONS_RED
        RiskDecision.AMBER -> ACTIONS_AMBER
        RiskDecision.GREEN -> ACTIONS_GREEN
        RiskDecision.UNKNOWN -> ACTIONS_UNKNOWN
    }

    fun defaultReasons(decision: RiskDecision): List<String> = when (decision) {
        RiskDecision.RED -> DEFAULT_REASONS_RED
        RiskDecision.AMBER -> DEFAULT_REASONS_AMBER
        RiskDecision.GREEN -> DEFAULT_REASONS_GREEN
        RiskDecision.UNKNOWN -> emptyList()
    }

    fun composeResultSpeech(result: CheckResult): String {
        val ordinals = listOf("一", "二", "三")
        val parts = mutableListOf(result.plain)
        val actions = result.actions.mapIndexed { index, action ->
            "${ordinals.getOrElse(index) { "" }}，$action"
        }.joinToString("；")
        parts += "现在这样做：$actions。"
        if (result.reasons.isNotEmpty()) {
            val reasons = result.reasons.mapIndexed { index, reason ->
                "${ordinals.getOrElse(index) { "" }}，$reason"
            }.joinToString("；")
            parts += "为什么：$reasons。"
        }
        if (result.fallback) result.fallbackNote?.let { parts += it }
        parts += "拿不准，就打 96110。"
        return parts.joinToString("")
    }

    fun composeStepSpeech(title: String, stepText: String, index: Int, total: Int): String =
        "第${index + 1}步，共${total}步。$title。$stepText"

    fun composeShareText(result: CheckResult): String = buildString {
        append("放心一眼：")
        append(result.label)
        append('\n')
        append(result.plain)
        append("\n\n现在这样做：\n")
        result.actions.forEachIndexed { index, action ->
            append(index + 1)
            append(". ")
            append(action)
            append('\n')
        }
        append("\n拿不准，就打 96110。")
    }
}
