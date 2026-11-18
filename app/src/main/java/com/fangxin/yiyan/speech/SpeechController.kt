package com.fangxin.yiyan.speech

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

class SpeechController(context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var ready = false

    var speaking by mutableStateOf(false)
        private set

    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS && ::tts.isInitialized) {
                val result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
                ready = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                postSpeaking(true)
            }

            override fun onDone(utteranceId: String?) {
                postSpeaking(false)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                postSpeaking(false)
            }
        })
    }

    fun speak(text: String): Boolean {
        if (!ready || text.isBlank()) return false
        tts.stop()
        speaking = true
        val utteranceId = "fangxin-${System.nanoTime()}"
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            speaking = false
            return false
        }
        return true
    }

    fun stop() {
        tts.stop()
        speaking = false
    }

    fun shutdown() {
        ready = false
        tts.stop()
        tts.shutdown()
    }

    private fun postSpeaking(value: Boolean) {
        mainHandler.post { speaking = value }
    }
}
