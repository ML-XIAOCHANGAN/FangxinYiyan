package com.fangxin.yiyan.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.FangxinViewModel
import com.fangxin.yiyan.domain.CheckSource
import com.fangxin.yiyan.domain.Samples
import com.fangxin.yiyan.ui.components.BigActionCard
import com.fangxin.yiyan.ui.components.NoticeCard
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.ProcessingOverlay
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.TopBackBar
import com.fangxin.yiyan.ui.components.rememberGalleryLauncher
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.LineStrong
import com.fangxin.yiyan.util.Bitmaps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CheckScreen(
    viewModel: FangxinViewModel,
    onBack: () -> Unit,
    onCamera: () -> Unit,
    onText: () -> Unit,
    onMessage: (String) -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var decoding by remember { mutableStateOf(false) }
    val processing by viewModel.processing.collectAsState()

    val openGallery = rememberGalleryLauncher { uri ->
        scope.launch {
            decoding = true
            val bitmap = withContext(Dispatchers.IO) { Bitmaps.fromUri(context, uri) }
            decoding = false
            if (bitmap == null) {
                onMessage("这张图打不开，换一张。")
            } else {
                viewModel.analyzeBitmap(bitmap, CheckSource.ALBUM, onDone)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PageColumn {
            TopBackBar(onBack = onBack, title = "回首页")

            Text(
                "想怎么给我看？",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink,
            )
            Text(
                "选一个最省事的。",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft,
            )

            BigActionCard(
                icon = Icons.Rounded.CameraAlt,
                title = "拍照",
                subtitle = "拍屏幕上的字",
                onClick = onCamera,
                highlighted = true,
            )
            BigActionCard(
                icon = Icons.Rounded.PhotoLibrary,
                title = "从相册选",
                subtitle = "用刚才截的图",
                onClick = openGallery,
            )
            BigActionCard(
                icon = Icons.Rounded.Keyboard,
                title = "打字或说话",
                subtitle = "把短信贴进来",
                onClick = onText,
            )

            NoticeCard("只在这台手机上看，不上传。")

            Text(
                "字太小看不清？让家人帮您念一遍，也行。",
                fontSize = 17.sp,
                color = InkMute,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        ProcessingOverlay(visible = processing || decoding)
    }
}

@Composable
fun TextCheckScreen(
    viewModel: FangxinViewModel,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    onDone: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val processing by viewModel.processing.collectAsState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                text = if (text.isBlank()) spoken else "$text\n$spoken"
            }
        }
    }

    val startSpeech: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说，我在听")
        }
        runCatching { speechLauncher.launch(intent) }
            .onFailure { onMessage("这个手机没有语音输入，打字也行。") }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PageColumn(modifier = Modifier.imePadding()) {
            TopBackBar(onBack = onBack, title = "回上一步")

            Text(
                "把收到的字贴进来",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink,
            )
            Text(
                "长按输入框，选“粘贴”。",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft,
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 7,
                maxLines = 12,
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text(
                        "比如：您的社保卡今天停用…",
                        fontSize = 18.sp,
                        color = InkMute,
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Brand,
                    unfocusedBorderColor = LineStrong,
                    focusedContainerColor = Card,
                    unfocusedContainerColor = Card,
                    cursorColor = Brand,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { text = Samples.ALL[0].text }) {
                    Text("看个例子", fontSize = 18.sp, color = Brand, fontWeight = FontWeight.Bold)
                }
            }

            PrimaryButton(
                text = "帮我看看",
                onClick = {
                    if (text.isBlank()) {
                        onMessage("先贴一段字，或者拍张照片。")
                    } else {
                        viewModel.analyzeText(text, CheckSource.TEXT, onDone)
                    }
                },
                icon = Icons.Rounded.Search,
            )

            SecondaryButton(
                text = "说话输入",
                onClick = startSpeech,
                icon = Icons.Rounded.Mic,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Rounded.Keyboard,
                    contentDescription = null,
                    tint = InkMute,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    "  这段字不会保存。",
                    fontSize = 17.sp,
                    color = InkMute,
                )
            }
        }

        ProcessingOverlay(visible = processing)
    }
}
