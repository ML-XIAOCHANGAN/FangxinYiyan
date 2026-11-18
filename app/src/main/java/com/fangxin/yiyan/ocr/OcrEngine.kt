package com.fangxin.yiyan.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

class OcrEngine {
    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    fun recognize(
        bitmap: Bitmap,
        onSuccess: (text: String, confidence: Int) -> Unit,
        onFailure: () -> Unit,
    ) {
        val input = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(input)
            .addOnSuccessListener { text ->
                onSuccess(text.text.trim(), estimateConfidence(text.text))
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    fun close() {
        recognizer.close()
    }

    private fun estimateConfidence(text: String): Int {
        val chars = text.count { !it.isWhitespace() }
        return when {
            chars < 8 -> 30
            chars < 20 -> 58
            chars < 50 -> 76
            else -> 88
        }
    }
}
