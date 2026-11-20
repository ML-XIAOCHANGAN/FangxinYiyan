package com.fangxin.yiyan.assistant

import com.fangxin.yiyan.domain.RiskDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

const val ASSISTANT_ORIGIN = "https://fangxin-yiyan-oqspdkiq3lk.qoder.zone"
private const val ASSISTANT_URL = "$ASSISTANT_ORIGIN/functions/v1/app/ask"

enum class OwlMood { IDLE, THINKING, CHAT, RED, AMBER, ERROR }

sealed interface OwlAnswer {
    data class Chat(val reply: String) : OwlAnswer
    data class Judge(
        val verdict: RiskDecision,
        val headline: String,
        val reasons: List<String>,
        val steps: List<String>,
    ) : OwlAnswer
}

data class OwlState(
    val mood: OwlMood = OwlMood.IDLE,
    val busy: Boolean = false,
    val answer: OwlAnswer? = null,
) {
    val bubble: String
        get() = when (mood) {
            OwlMood.THINKING -> "让我琢磨一下…"
            OwlMood.CHAT -> "我在呢，慢慢说。"
            OwlMood.RED -> "这条不对头，先停下。"
            OwlMood.AMBER -> "看不出准头，先别急着办。"
            OwlMood.ERROR -> "没连上，等会儿再试。"
            OwlMood.IDLE -> "把那段话给我看看。"
        }
}

class OwlException(val userMessage: String, cause: Throwable? = null) : Exception(userMessage, cause)

object AssistantClient {
    private const val JSON_TYPE = "application/json; charset=utf-8"

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(55, TimeUnit.SECONDS)
        .build()
    private val jsonType = JSON_TYPE.toMediaType()

    suspend fun ask(raw: String): OwlAnswer = withContext(Dispatchers.IO) {
        val text = raw.trim()
        if (text.length < 4) throw OwlException("至少写四个字，小望才看得懂。")
        val body = JSONObject().put("text", text.take(600)).toString().toRequestBody(jsonType)
        val request = Request.Builder()
            .url(ASSISTANT_URL)
            .header("Origin", ASSISTANT_ORIGIN)
            .header("Accept", "application/json")
            .post(body)
            .build()

        val response = try {
            http.newCall(request).execute()
        } catch (error: IOException) {
            throw OwlException("小望没连上，等会儿再试。", error)
        }
        response.use { result ->
            val payload = result.body?.string().orEmpty()
            if (!result.isSuccessful) {
                val code = runCatching { JSONObject(payload).optString("error") }.getOrNull()
                throw OwlException(
                    when {
                        result.code == 429 -> "小望正忙着，等半分钟再问。"
                        code == "invalid_request" -> "至少写四个字，小望才看得懂。"
                        else -> "小望没连上，等会儿再试。"
                    },
                )
            }
            val json = runCatching { JSONObject(payload) }.getOrNull()
                ?: throw OwlException("小望没答上来，等会儿再试。")
            when (json.optString("kind")) {
                "chat" -> {
                    val reply = json.optString("reply").trim()
                    if (reply.isEmpty()) throw OwlException("小望没答上来，等会儿再试。")
                    OwlAnswer.Chat(reply)
                }
                "judge" -> {
                    val verdict = if (json.optString("verdict") == "red") RiskDecision.RED else RiskDecision.AMBER
                    val reasons = json.optJSONArray("reasons").toStringList().take(3)
                    if (reasons.isEmpty()) throw OwlException("小望没答上来，等会儿再试。")
                    OwlAnswer.Judge(
                        verdict = verdict,
                        headline = json.optString("headline").ifBlank {
                            if (verdict == RiskDecision.RED) "很可能是骗子" else "先别急着办"
                        },
                        reasons = reasons,
                        steps = json.optJSONArray("steps").toStringList().take(3),
                    )
                }
                else -> throw OwlException("小望没答上来，等会儿再试。")
            }
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optString(index).trim().takeIf { it.isNotEmpty() }?.let(::add)
        }
    }
}
