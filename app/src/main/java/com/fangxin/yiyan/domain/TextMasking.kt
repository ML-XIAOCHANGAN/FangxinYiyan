package com.fangxin.yiyan.domain

object TextMasking {
    private val CODE = Regex("(验证码|校验码|动态码|动态密码)(\\s*[为是:：]?\\s*)(\\d{4,8})")
    private val ID_CARD = Regex("\\b\\d{17}[\\dXx]\\b")
    private val BANK_CARD = Regex("\\b(?:\\d[ -]?){15,19}\\d\\b")
    private val MOBILE = Regex("\\b1[3-9]\\d{9}\\b")

    fun maskSensitive(input: String): String {
        var text = input
        text = CODE.replace(text) { match ->
            "${match.groupValues[1]}${match.groupValues[2]}******"
        }
        text = ID_CARD.replace(text) { match ->
            "**************${match.value.takeLast(4)}"
        }
        text = BANK_CARD.replace(text) { match ->
            val digits = match.value.filter { it.isDigit() }
            "**** **** **** ${digits.takeLast(4)}"
        }
        text = MOBILE.replace(text) { match ->
            "*******${match.value.takeLast(4)}"
        }
        return text
    }
}
