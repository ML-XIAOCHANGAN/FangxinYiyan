package com.fangxin.yiyan.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextMaskingTest {
    @Test
    fun `masks verification code`() {
        val masked = TextMasking.maskSensitive("验证码为 123456，五分钟内有效")

        assertTrue(masked.contains("******"))
        assertFalse(masked.contains("123456"))
    }

    @Test
    fun `keeps only last four of id card`() {
        val masked = TextMasking.maskSensitive("身份证 110101199003071234")

        assertTrue(masked.endsWith("1234"))
        assertTrue(masked.contains("**************"))
        assertFalse(masked.contains("110101199003071234"))
    }

    @Test
    fun `keeps only last four of bank card`() {
        val masked = TextMasking.maskSensitive("卡号 6222 0212 3456 7890")

        assertTrue(masked.contains("**** **** **** 7890"))
        assertFalse(masked.contains("6222"))
    }

    @Test
    fun `keeps only last four of mobile number`() {
        val masked = TextMasking.maskSensitive("电话 13812345678")

        assertTrue(masked.contains("*******5678"))
        assertFalse(masked.contains("13812345678"))
    }
}
