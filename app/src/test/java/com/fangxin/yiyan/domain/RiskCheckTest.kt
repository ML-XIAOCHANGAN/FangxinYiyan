package com.fangxin.yiyan.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskCheckTest {
    @Test
    fun `asks for verification code is red`() {
        val result = RiskCheck.analyzeText("客服说请提供验证码，帮我核对一下身份")

        assertEquals(RiskDecision.RED, result.risk)
        assertEquals("很可能是骗子", result.label)
        assertTrue(result.reasons.isNotEmpty())
    }

    @Test
    fun `warning others about codes is not red`() {
        val result = RiskCheck.analyzeText("请勿把验证码告诉别人")

        assertTrue(result.risk != RiskDecision.RED)
    }

    @Test
    fun `legit code notice stays green`() {
        val result = RiskCheck.analyzeText("验证码为1234，请勿告诉他人。")

        assertEquals(RiskDecision.GREEN, result.risk)
        assertFalse(result.fallback)
    }

    @Test
    fun `two amber groups escalate to red`() {
        val result = RiskCheck.analyzeText("您的账户即将停用，可以投资稳赚")

        assertEquals(RiskDecision.RED, result.risk)
        assertEquals(0.82, result.confidence, 0.001)
    }

    @Test
    fun `single refund mention is amber`() {
        val result = RiskCheck.analyzeText("您有一笔退款，请留意到账")

        assertEquals(RiskDecision.AMBER, result.risk)
    }

    @Test
    fun `too little text falls back`() {
        val result = RiskCheck.analyzeText("你好")

        assertTrue(result.fallback)
        assertEquals(RiskDecision.UNKNOWN, result.risk)
        assertEquals("没看清", result.label)
        assertEquals(Copy.FALLBACK_NOTE, result.fallbackNote)
    }

    @Test
    fun `low ocr confidence without evidence falls back`() {
        val result = RiskCheck.analyzeText(
            "今天天气很好我们一起去公园散步吧",
            source = CheckSource.PHOTO,
            ocrConfidence = 30,
        )

        assertTrue(result.fallback)
    }

    @Test
    fun `family asks for money after changing number is amber`() {
        val result = RiskCheck.analyzeText("妈，我换号了，急用钱，转两千给我")

        assertEquals(RiskDecision.AMBER, result.risk)
        assertTrue(result.reasons.any { it.contains("换了号码") })
    }

    @Test
    fun `official link notice stays green`() {
        val result = RiskCheck.analyzeText("【中国移动】您的话费余额为30元，详情见 https://www.10086.cn")

        assertEquals(RiskDecision.GREEN, result.risk)
        assertTrue(result.reasons.any { it.contains("官方") || it.contains("通知") })
    }

    @Test
    fun `typo with link is red`() {
        val result = RiskCheck.analyzeText("请点击 http://abc.top 登陆帐户")

        assertEquals(RiskDecision.RED, result.risk)
    }

    @Test
    fun `red transfer uses banking actions`() {
        val result = RiskCheck.analyzeText("请马上转账到安全账户")

        assertEquals(RiskDecision.RED, result.risk)
        assertEquals("马上停下，别再转钱", result.actions.first())
    }

    @Test
    fun `reason list is capped at three and unique`() {
        val result = RiskCheck.analyzeText(
            "法院传票已到，你涉案了，请马上转账到安全账户，否则逮捕，公安局会冻结你全部资金",
        )

        assertEquals(3, result.reasons.size)
        assertEquals(3, result.reasons.distinct().size)
    }

    @Test
    fun `samples keep their intended verdicts`() {
        for (sample in Samples.ALL) {
            val result = RiskCheck.analyzeText(sample.text)
            assertEquals(sample.id, sample.risk, result.risk)
        }
    }
}
