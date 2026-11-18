package com.fangxin.yiyan.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.AlertEvent
import com.fangxin.yiyan.domain.AppSettings
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.domain.TextMasking
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
import com.fangxin.yiyan.ui.theme.LineStrong
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.ui.theme.riskPalette
import com.fangxin.yiyan.util.relativeTime

@Composable
fun GuardianHomeScreen(
    paired: Boolean,
    room: String,
    connected: Boolean,
    elderOnline: Boolean,
    alerts: List<AlertEvent>,
    onConnect: (String) -> Unit,
    onOpenAlert: (AlertEvent) -> Unit,
    onWatch: () -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit,
) {
    var codeInput by remember { mutableStateOf("") }

    PageColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Brand),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("放心一眼 · 守护", fontSize = 21.sp, fontWeight = FontWeight.Black, color = Ink)
                Text("有事马上知道", fontSize = 17.sp, color = InkMute)
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(52.dp)) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "设置",
                    tint = InkSoft,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        if (!paired) {
            PairingCard(
                codeInput = codeInput,
                onCodeChange = { input -> codeInput = input.filter { it.isDigit() }.take(9) },
                onConnect = { onConnect(codeInput) },
            )
        } else {
            PairedCard(
                room = room,
                connected = connected,
                elderOnline = elderOnline,
                onWatch = onWatch,
                onUnpairHint = onSettings,
            )
        }

        SectionTitle("最新警报")
        if (alerts.isEmpty()) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                border = BorderStroke(1.dp, Line),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("还没有警报", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(
                        "老人手机上判定出风险，这里会马上出现。",
                        fontSize = 18.sp,
                        color = InkMute,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                alerts.forEach { alert ->
                    AlertRow(alert = alert, onClick = { onOpenAlert(alert) })
                }
            }
        }

        NoticeCard("警报只走配对码通道，看完就删；屏幕要老人同意才看得到。")

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(
                text = "看老人屏幕",
                onClick = onWatch,
                icon = Icons.Rounded.Visibility,
                enabled = paired,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = "设置",
                onClick = onSettings,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = onPrivacy, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("隐私说明", fontSize = 18.sp, color = Brand)
            }
        }
    }
}

@Composable
private fun PairingCard(
    codeInput: String,
    onCodeChange: (String) -> Unit,
    onConnect: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.5.dp, Brand),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("先和老人手机配对", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink)
            Text(
                "在老人手机的「家人设置」里，找到配对码，填在下面。",
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = InkSoft,
            )
            OutlinedTextField(
                value = codeInput,
                onValueChange = onCodeChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 28.sp,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Bold,
                ),
                placeholder = {
                    Text("配对码", fontSize = 20.sp, color = InkMute)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            PrimaryButton(
                text = "连上老人手机",
                onClick = onConnect,
                enabled = codeInput.length == 6 || codeInput.length == 9,
            )
        }
    }
}

@Composable
private fun PairedCard(
    room: String,
    connected: Boolean,
    elderOnline: Boolean,
    onWatch: () -> Unit,
    onUnpairHint: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !connected -> LineStrong
                                elderOnline -> Color(0xFF2E7D4F)
                                else -> Color(0xFFC98A00)
                            },
                        ),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        !connected -> "正在连中继…"
                        elderOnline -> "老人手机在线"
                        else -> "老人手机还没连上"
                    },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "配对码 ${formatCode(room)}",
                    fontSize = 17.sp,
                    color = InkMute,
                )
            }
            Text(
                when {
                    !connected -> "网络断了会自动重连，警报会在恢复后补上。"
                    elderOnline -> "老人一看消息，这里就会同步出现警报。"
                    else -> "让老人打开「放心一眼」，两边就会连上。"
                },
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = InkSoft,
            )
            QuietButton(text = "换配对码 / 解除", onClick = onUnpairHint)
        }
    }
}

@Composable
private fun AlertRow(alert: AlertEvent, onClick: () -> Unit) {
    val palette = riskPalette(alert.risk)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, if (alert.risk == RiskDecision.RED) palette.color else Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(palette.color),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        alert.label,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(relativeTime(alert.createdAt), fontSize = 16.sp, color = InkMute)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    alert.plain,
                    fontSize = 17.sp,
                    color = InkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = InkMute,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun GuardianAlertDetailScreen(
    alert: AlertEvent,
    screenOn: Boolean,
    onBack: () -> Unit,
    onWatch: () -> Unit,
    onDialHotline: () -> Unit,
) {
    val palette = riskPalette(alert.risk)
    PageColumn {
        TopBackBar(onBack = onBack, title = "回警报")

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = palette.bg),
            border = BorderStroke(1.5.dp, palette.color),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(palette.color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (alert.risk == RiskDecision.RED) "!" else "?",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(alert.label, fontSize = 26.sp, fontWeight = FontWeight.Black, color = palette.deep)
                }
                Text(alert.plain, fontSize = 19.sp, lineHeight = 28.sp, color = Ink)
                Text(relativeTime(alert.createdAt), fontSize = 16.sp, color = InkMute)
            }
        }

        if (alert.maskedText.isNotBlank()) {
            SectionTitle("老人收到的内容（号码已打码）")
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                border = BorderStroke(1.dp, Line),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Text(
                    TextMasking.maskSensitive(alert.maskedText),
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    color = InkSoft,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }

        if (alert.actions.isNotEmpty()) {
            SectionTitle("该怎么做")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alert.actions.forEachIndexed { index, action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardColor)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(palette.bg),
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
                        Text(action, fontSize = 18.sp, color = Ink)
                    }
                }
            }
        }

        SectionTitle("现在能做什么")
        PrimaryButton(
            text = if (screenOn) "看他正在用的屏幕" else "看看老人屏幕",
            onClick = onWatch,
            icon = Icons.Rounded.Visibility,
        )
        SecondaryButton(
            text = "打 96110 报警",
            onClick = onDialHotline,
            icon = Icons.Rounded.Phone,
        )
        NoticeCard("不是所有警报都是诈骗。先打个电话问问老人，再决定要不要报警。")
    }
}

@Composable
fun GuardianWatchScreen(
    screenOn: Boolean,
    frame: ImageBitmap?,
    onBack: () -> Unit,
    onRequestScreen: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (screenOn || frame != null) "正在看老人屏幕 · 实时" else "老人还没打开屏幕",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.weight(1f))
                if (screenOn) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3DDC84)),
                    )
                } else {
                    Spacer(Modifier.width(10.dp))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val bitmap = frame
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "老人手机屏幕",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF9A9A9A),
                            modifier = Modifier.size(56.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (screenOn) {
                                "正在等画面，马上就来。"
                            } else {
                                "为了隐私，屏幕要老人自己同意才能共享。"
                            },
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFFCFCFCF),
                            textAlign = TextAlign.Center,
                        )
                        if (!screenOn) {
                            Spacer(Modifier.height(16.dp))
                            TextButton(onClick = onRequestScreen) {
                                Text("提醒老人打开屏幕", fontSize = 18.sp, color = Color(0xFF9CC3E8))
                            }
                        }
                    }
                }
            }

            Text(
                "共享最多 5 分钟，随时能停；画面不保存。",
                fontSize = 16.sp,
                color = Color(0xFF9A9A9A),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
            )
        }
    }
}

@Composable
fun GuardianSettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onUnpair: () -> Unit,
    onClearAlerts: () -> Unit,
    onSwitchRole: () -> Unit,
    onToggleFloating: (Boolean) -> Unit,
    onMessage: (String) -> Unit,
    onPrivacy: () -> Unit,
) {
    var confirmingClear by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (Settings.canDrawOverlays(context)) {
            onToggleFloating(true)
        } else {
            onMessage("还没开成。想开的时候，再来这里。")
        }
    }

    PageColumn {
        TopBackBar(onBack = onBack, title = "回守护页")

        Text("守护设置", style = MaterialTheme.typography.headlineLarge, color = Ink)
        Text("配对和连接都在这台手机上。", style = MaterialTheme.typography.bodyLarge, color = InkSoft)

        GuardianCard {
            Text("配对", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                if (settings.guardianPaired) {
                    "已连上配对码 ${formatCode(settings.guardianRoom)}。"
                } else {
                    "还没配对。回守护页填老人手机的配对码。"
                },
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = InkSoft,
            )
            if (settings.guardianPaired) {
                SecondaryButton(text = "解除配对", onClick = onUnpair)
            }
        }

        GuardianCard {
            Text("警报记录", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text("只记结论和打码后的原文，最多 20 条。", fontSize = 18.sp, color = InkSoft)
            SecondaryButton(
                text = if (confirmingClear) "再点一下，清空警报" else "清空警报记录",
                onClick = {
                    if (confirmingClear) {
                        onClearAlerts()
                        confirmingClear = false
                    } else {
                        confirmingClear = true
                    }
                },
            )
        }

        GuardianCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("桌面小助手", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(
                        if (settings.floatingEnabled) {
                            "已开启。退出软件后，猫头鹰会在桌面上等您。"
                        } else {
                            "开了以后，退出软件时桌面上会留下猫头鹰头像，点它就能回来看警报。"
                        },
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        color = InkSoft,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = settings.floatingEnabled,
                    onCheckedChange = { next ->
                        if (next && !Settings.canDrawOverlays(context)) {
                            overlayLauncher.launch(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}"),
                                ),
                            )
                        } else {
                            onToggleFloating(next)
                        }
                    },
                    modifier = Modifier.heightIn(min = 52.dp),
                )
            }
        }

        GuardianCard {
            Text("身份", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "这台手机现在按守护人使用。老人那台要选「我是中老年人」。",
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = InkSoft,
            )
            SecondaryButton(text = "换成老人界面", onClick = onSwitchRole)
        }

        NoticeCard("不注册、不收钱；警报和屏幕都不保存。")
        QuietButton(text = "看隐私说明", onClick = onPrivacy)
        QuietButton(text = "回守护页", onClick = onBack)
    }
}

@Composable
private fun GuardianCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

fun formatCode(raw: String): String = when {
    raw.length == 9 -> "${raw.take(3)} ${raw.substring(3, 6)} ${raw.takeLast(3)}"
    raw.length == 6 -> "${raw.take(3)} ${raw.takeLast(3)}"
    else -> raw
}
