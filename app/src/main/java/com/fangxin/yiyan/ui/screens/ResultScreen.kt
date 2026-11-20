package com.fangxin.yiyan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.R
import com.fangxin.yiyan.domain.CheckResult
import com.fangxin.yiyan.domain.CheckSource
import com.fangxin.yiyan.domain.Copy
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.domain.TextMasking
import com.fangxin.yiyan.speech.SpeechController
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.RiskBadge
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.SectionTitle
import com.fangxin.yiyan.ui.components.TopBackBar
import com.fangxin.yiyan.ui.components.TrustLine
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.ui.theme.riskPalette
import com.fangxin.yiyan.util.Intents

@Composable
fun ResultScreen(
    result: CheckResult?,
    speech: SpeechController,
    onHome: () -> Unit,
    onCheck: () -> Unit,
    onCamera: () -> Unit,
    onText: () -> Unit,
    onGuides: () -> Unit,
    onHelp: () -> Unit,
    onDialHotline: () -> Unit,
    onMessage: (String) -> Unit,
    screenSharing: Boolean = false,
    onShareScreen: () -> Unit = {},
    onStopShareScreen: () -> Unit = {},
) {
    if (result == null) {
        EmptyResult(onHome = onHome, onCheck = onCheck)
        return
    }

    DisposableEffect(result.id) {
        onDispose { speech.stop() }
    }

    val context = LocalContext.current
    val palette = riskPalette(result.risk)
    var feedback by remember(result.id) { mutableStateOf("") }
    var expanded by remember(result.id) { mutableStateOf(result.fallback) }
    var showText by remember(result.id) { mutableStateOf(false) }
    val maskedText = remember(result.id) {
        result.ocrText?.takeIf { it.isNotBlank() }?.let(TextMasking::maskSensitive)
    }

    PageColumn {
        TopBackBar(onBack = onHome, title = "回首页")

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = palette.color),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            when (result.risk) {
                                RiskDecision.RED -> "!"
                                RiskDecision.GREEN -> "✓"
                                else -> "?"
                            },
                            color = palette.color,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        result.label,
                        fontSize = 30.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    result.plain,
                    fontSize = 32.sp,
                    lineHeight = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
                if (result.source == CheckSource.SAMPLE) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "这是例子，不是您的消息。",
                        fontSize = 19.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (speech.speaking) {
                            speech.stop()
                        } else {
                            val started = speech.speak(Copy.composeResultSpeech(result))
                            if (!started) onMessage("这个手机不能朗读，让家人读给您听。")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = palette.deep,
                    ),
                ) {
                    Icon(
                        if (speech.speaking) Icons.Rounded.Stop else Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (speech.speaking) "停下" else "听一遍",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        SectionTitle("现在这样做")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            result.actions.forEachIndexed { index, action ->
                ActionRow(
                    index = index,
                    text = action,
                    first = index == 0,
                    accent = palette.color,
                    accentBg = palette.bg,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(
                text = "打 96110",
                onClick = onDialHotline,
                icon = Icons.Rounded.Phone,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = "找家人",
                onClick = onHelp,
                icon = Icons.Rounded.Person,
                modifier = Modifier.weight(1f),
            )
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    if (screenSharing) "家人正在看您的屏幕" else "让家人看看屏幕",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
                Text(
                    if (screenSharing) {
                        "最多看 5 分钟，随时可以停。"
                    } else {
                        "家人能看到您在点哪里，好教您怎么办。"
                    },
                    fontSize = 18.sp,
                    color = InkSoft,
                )
                if (screenSharing) {
                    SecondaryButton(
                        text = "停下，不给看了",
                        onClick = onStopShareScreen,
                        icon = Icons.Rounded.Stop,
                    )
                } else {
                    SecondaryButton(
                        text = "让家人看屏幕",
                        onClick = onShareScreen,
                        icon = Icons.Rounded.Visibility,
                    )
                }
                Text(
                    "每次都要您自己点同意，画面不留。",
                    fontSize = 16.sp,
                    color = InkMute,
                )
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .heightIn(min = 66.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (result.fallback) Icons.Rounded.Warning else Icons.Rounded.Info,
                        contentDescription = null,
                        tint = palette.color,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (result.fallback) "为什么没看清" else "为什么这么说",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (expanded) "收起" else "展开",
                        fontSize = 18.sp,
                        color = InkMute,
                    )
                }

                if (expanded) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (result.fallback) {
                            Text(
                                result.fallbackNote ?: Copy.FALLBACK_NOTE,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SecondaryButton(
                                    text = "重拍一张",
                                    onClick = onCamera,
                                    icon = Icons.Rounded.Refresh,
                                    modifier = Modifier.weight(1f),
                                )
                                SecondaryButton(
                                    text = "贴文字",
                                    onClick = onText,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        } else {
                            result.reasons.forEachIndexed { index, reason ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(palette.bg, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = palette.deep,
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        reason,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Ink,
                                        modifier = Modifier.padding(top = 3.dp),
                                    )
                                }
                            }
                        }

                        if (maskedText != null) {
                            TextButton(onClick = { showText = !showText }) {
                                Text(
                                    if (showText) "收起认出来的字" else "看看认出来的字（号码已打码）",
                                    fontSize = 18.sp,
                                    color = Brand,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if (showText) {
                                Text(
                                    maskedText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkMute,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Paper, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        SectionTitle("发给家人看")
        PrimaryButton(
            text = "发给家人",
            onClick = { Intents.share(context, Copy.composeShareText(result)) },
            icon = Icons.Rounded.Share,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (feedback.isEmpty()) {
                Text(
                    "这个结果准吗？",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkSoft,
                )
                Spacer(Modifier.weight(1f))
                FeedbackButton("准") {
                    feedback = "up"
                    onMessage("记下了，谢谢您。")
                }
                FeedbackButton("不准") {
                    feedback = "down"
                    onMessage("记下了，谢谢您。")
                }
            } else {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = palette.color,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    "记下了，谢谢您。",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.deep,
                )
            }
        }

        TrustLine()

        PrimaryButton(
            text = if (result.fallback) "再试一次" else "再看一条",
            onClick = onCheck,
            icon = Icons.Rounded.Refresh,
        )
        SecondaryButton(
            text = "看大字指引",
            onClick = onGuides,
        )
    }
}

@Composable
private fun ActionRow(
    index: Int,
    text: String,
    first: Boolean,
    accent: Color,
    accentBg: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (first) 46.dp else 38.dp)
                .background(if (first) accent else accentBg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${index + 1}",
                fontSize = if (first) 21.sp else 18.sp,
                fontWeight = FontWeight.Black,
                color = if (first) Color.White else accent,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            fontSize = 21.sp,
            lineHeight = 31.sp,
            fontWeight = if (first) FontWeight.ExtraBold else FontWeight.Medium,
            color = Ink,
        )
    }
}

@Composable
private fun FeedbackButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.widthIn(min = 72.dp, max = 100.dp).heightIn(min = 52.dp),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, Line),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Paper,
            contentColor = InkSoft,
        ),
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyResult(onHome: () -> Unit, onCheck: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        PageColumn {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ill_empty),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(132.dp)
                        .background(Paper, RoundedCornerShape(26.dp)),
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    "还没看过",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "拍张照片，我帮您看一眼。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkMute,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton(text = "去看一条", onClick = onCheck)
                TextButton(onClick = onHome) {
                    Text("回首页", fontSize = 18.sp, color = Brand)
                }
            }
        }
    }
}
