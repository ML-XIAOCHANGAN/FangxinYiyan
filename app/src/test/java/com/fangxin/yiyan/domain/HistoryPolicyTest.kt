package com.fangxin.yiyan.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPolicyTest {
    private fun item(id: String, createdAt: Long): HistoryItem = HistoryItem(
        id = id,
        risk = RiskDecision.AMBER,
        label = "有点可疑",
        plain = "先别急着办。",
        reasons = emptyList(),
        actions = emptyList(),
        source = CheckSource.TEXT,
        createdAt = createdAt,
    )

    @Test
    fun `keeps only the newest three items`() {
        val items = listOf(
            item("a", 1),
            item("b", 5),
            item("c", 3),
            item("d", 4),
            item("e", 2),
        )

        val trimmed = HistoryPolicy.trim(items)

        assertEquals(listOf("b", "d", "c"), trimmed.map { it.id })
    }

    @Test
    fun `deduplicates by id`() {
        val items = listOf(item("a", 1), item("a", 2), item("b", 3))

        val trimmed = HistoryPolicy.trim(items)

        assertEquals(2, trimmed.size)
        assertEquals(listOf("b", "a"), trimmed.map { it.id })
        assertEquals(2L, trimmed.last().createdAt)
    }

    @Test
    fun `history round trip keeps the conclusion`() {
        val original = RiskCheck.analyzeText("客服说请提供验证码，帮我核对一下身份")
        val restored = original.toHistoryItem().toResult()

        assertEquals(original.risk, restored.risk)
        assertEquals(original.label, restored.label)
        assertEquals(original.actions, restored.actions)
        assertEquals(original.fallback, restored.fallback)
    }
}
