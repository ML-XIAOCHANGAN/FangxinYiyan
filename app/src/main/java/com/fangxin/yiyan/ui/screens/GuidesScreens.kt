package com.fangxin.yiyan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.Copy
import com.fangxin.yiyan.domain.Guide
import com.fangxin.yiyan.domain.Guides
import com.fangxin.yiyan.speech.SpeechController
import com.fangxin.yiyan.ui.components.GuideArt
import com.fangxin.yiyan.ui.components.NoticeCard
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.QuietButton
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.SectionTitle
import com.fangxin.yiyan.ui.components.TopBackBar
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.Paper

@Composable
fun GuideListScreen(
    onBack: () -> Unit,
    onGuide: (String) -> Unit,
) {
    PageColumn {
        TopBackBar(onBack = onBack, title = "回首页")

        Text(
            "大字办事指引",
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
        )
        Text(
            "一步一步，照着做。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        for (category in Guides.CATEGORIES) {
            SectionTitle(category.name)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (guide in Guides.byCategory(category.name)) {
                    GuideRow(guide = guide, onClick = { onGuide(guide.id) })
                }
            }
        }

        NoticeCard("找不到，就问穿红马甲的导医。")
        QuietButton(text = "回首页", onClick = onBack)
    }
}

@Composable
private fun GuideRow(guide: Guide, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GuideArt(
                category = guide.category,
                modifier = Modifier.size(82.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    guide.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${guide.steps.size} 步 · ${guide.minutes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMute,
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = InkMute,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
fun GuideDetailScreen(
    guideId: String,
    speech: SpeechController,
    onBack: () -> Unit,
    onGuideList: () -> Unit,
    onHome: () -> Unit,
    onHelp: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val guide = remember(guideId) { Guides.find(guideId) }

    if (guide == null) {
        PageColumn {
            TopBackBar(onBack = onBack, title = "回指引")
            Text("找不到这一条", style = MaterialTheme.typography.headlineLarge, color = Ink)
            Text("回指引里重新选一条。", style = MaterialTheme.typography.bodyLarge, color = InkSoft)
            PrimaryButton(text = "回指引", onClick = onGuideList)
        }
        return
    }

    DisposableEffect(guide.id) {
        onDispose { speech.stop() }
    }

    var stepIndex by rememberSaveable(guide.id) { mutableStateOf(0) }
    var done by rememberSaveable(guide.id) { mutableStateOf(false) }
    val step = guide.steps[stepIndex.coerceIn(0, guide.steps.lastIndex)]

    PageColumn {
        TopBackBar(onBack = onBack, title = "回指引")

        GuideArt(
            category = guide.category,
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp),
        )

        Text(
            guide.title,
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
        )
        Text(
            "${guide.steps.size} 步 · ${guide.minutes}",
            style = MaterialTheme.typography.labelMedium,
            color = InkMute,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            guide.steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(9.dp)
                        .background(
                            if (index <= stepIndex || done) Brand else Line,
                            RoundedCornerShape(999.dp),
                        ),
                )
            }
        }

        if (!done) {
            Text(
                "第 ${stepIndex + 1} 步，共 ${guide.steps.size} 步",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Brand,
            )

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                border = BorderStroke(1.5.dp, Line),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .background(Brand, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${stepIndex + 1}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            guide.category,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brand,
                            modifier = Modifier
                                .background(Paper, RoundedCornerShape(999.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        step.text,
                        fontSize = 26.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = InkMute,
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            step.tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    SecondaryButton(
                        text = "听这一步",
                        onClick = {
                            val started = speech.speak(
                                Copy.composeStepSpeech(
                                    title = guide.title,
                                    stepText = step.text,
                                    index = stepIndex,
                                    total = guide.steps.size,
                                ),
                            )
                            if (!started) onMessage("这个手机不能朗读，让家人读给您听。")
                        },
                        icon = Icons.Rounded.VolumeUp,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = "上一步",
                    onClick = {
                        speech.stop()
                        stepIndex = (stepIndex - 1).coerceAtLeast(0)
                    },
                    enabled = stepIndex > 0,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = if (stepIndex == guide.steps.lastIndex) "办好了" else "下一步",
                    onClick = {
                        speech.stop()
                        if (stepIndex == guide.steps.lastIndex) {
                            done = true
                        } else {
                            stepIndex += 1
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                "慢一点，没关系。",
                style = MaterialTheme.typography.labelMedium,
                color = InkMute,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                border = BorderStroke(1.5.dp, Brand),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Brand,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "办好了，真棒。",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "遇到不懂的，就问工作人员。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkSoft,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(text = "再看一个", onClick = onGuideList)
                    SecondaryButton(
                        text = "找人帮忙",
                        onClick = onHelp,
                        icon = Icons.Rounded.Person,
                    )
                    TextButton(onClick = onHome) {
                        Text("回首页", fontSize = 18.sp, color = InkMute)
                    }
                }
            }
        }
    }
}
