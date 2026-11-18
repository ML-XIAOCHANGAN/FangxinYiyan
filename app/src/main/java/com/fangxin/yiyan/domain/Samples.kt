package com.fangxin.yiyan.domain

object Samples {
    val ALL: List<SampleCase> = listOf(
        SampleCase(
            id = "sample-red",
            title = "社保卡要停用",
            text = "【社保局】您的社保卡将于今日停用，请立即点击 http://sbk-renzheng.top 完成认证，逾期作废。认证需填写身份证号和银行卡号。",
            risk = RiskDecision.RED,
            label = "很可能是骗子",
            plain = "先停下，这很可能是骗子。",
            reasons = listOf("要你填身份证和银行卡", "说不认证今天就停用", "链接不是官方网站"),
            actions = listOf("别点，别转账，别填信息", "打 12333 或 96110", "把截图发给家人"),
        ),
        SampleCase(
            id = "sample-amber",
            title = "“家人”换号借钱",
            text = "妈，我手机掉水里了，这是借别人的号。我急用两千块钱，你先转到这个卡上，回头跟你细说。",
            risk = RiskDecision.AMBER,
            label = "有点可疑",
            plain = "先别急着办。",
            reasons = listOf("说是家人，但换了号码", "急着让你马上转钱", "没有当面或电话说清"),
            actions = listOf("先打电话问清楚", "别急着转钱", "见到人再说"),
        ),
        SampleCase(
            id = "sample-green",
            title = "话费扣费通知",
            text = "【中国移动】您本月话费58元，已从余额扣除，当前余额132.5元。查询详情请使用中国移动官方App。",
            risk = RiskDecision.GREEN,
            label = "没看出问题",
            plain = "没看出问题。动钱先核实。",
            reasons = listOf("是常见的话费提醒", "没要验证码和卡号", "没催你马上转钱"),
            actions = listOf("动钱前，先跟家人说一声", "从官方软件里查", "拿不准打 96110"),
        ),
    )

    fun toResult(sample: SampleCase): CheckResult = CheckResult(
        id = "sample-${System.currentTimeMillis().toString(36)}",
        risk = sample.risk,
        label = sample.label,
        confidence = if (sample.risk == RiskDecision.RED) 1.0 else 0.9,
        plain = sample.plain,
        reasons = sample.reasons,
        actions = sample.actions,
        source = CheckSource.SAMPLE,
        createdAt = System.currentTimeMillis(),
        hits = listOf("sample"),
        ocrText = sample.text,
    )
}
