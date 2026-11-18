package com.fangxin.yiyan.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.AppSettings
import com.fangxin.yiyan.ui.components.NoticeCard
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.QuietButton
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.TopBackBar
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.Paper

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onSavePhone: (String) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onRegenerateCode: () -> Unit,
    onSwitchRole: () -> Unit,
    onToggleFloating: (Boolean) -> Unit,
    onMessage: (String) -> Unit,
    onPrivacy: () -> Unit,
    onHelp: () -> Unit,
) {
    var phone by remember(settings.familyPhone) { mutableStateOf(settings.familyPhone) }
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
        TopBackBar(onBack = onBack, title = "回首页")

        Text(
            "家人帮忙设置",
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
        )
        Text(
            "只在这台手机上，不用登录。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        SettingsCard {
            Text("跟家人手机连上", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "家人在自己手机上选「守护人」，把这串数字填进去。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Paper)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    formatCode(settings.pairingCode),
                    fontSize = if (settings.pairingCode.length > 6) 30.sp else 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (settings.pairingCode.length > 6) 2.sp else 4.sp,
                    color = Brand,
                )
            }
            Text(
                "念给家人听，或者让他们照着填。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMute,
            )
            QuietButton(text = "换一个配对码", onClick = onRegenerateCode)
        }

        SettingsCard {
            Text("家人的电话", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "存好以后，求助时一点就能打给您。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { input -> phone = input.filter { it.isDigit() }.take(15) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    letterSpacing = 1.sp,
                ),
                placeholder = {
                    Text(
                        "儿女的手机号",
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkMute,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            PrimaryButton(
                text = "存好号码",
                onClick = { onSavePhone(phone) },
                enabled = phone.length >= 3,
            )
            if (settings.familyPhone.isNotBlank()) {
                QuietButton(
                    text = "不用存家人号码",
                    onClick = {
                        phone = ""
                        onSavePhone("")
                    },
                )
            }
        }

        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("字再大一点", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(
                        if (settings.largeText) "现在已经是再大一档。" else "默认已经是大字，还能更大。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = settings.largeText,
                    onCheckedChange = onToggleLargeText,
                    modifier = Modifier.heightIn(min = 52.dp),
                )
            }
        }

        SettingsCard {
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
                            "开了以后，退出软件时桌面上会留下一个猫头鹰头像。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
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

        SettingsCard {
            Text("最近看过", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "只存最近三条结论，不存照片。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            SecondaryButton(
                text = if (confirmingClear) "再点一下，清空记录" else "清空本机记录",
                onClick = {
                    if (confirmingClear) {
                        onClearHistory()
                        confirmingClear = false
                    } else {
                        confirmingClear = true
                    }
                },
            )
            if (confirmingClear) {
                Text(
                    "清空后找不回来。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMute,
                )
            }
        }

        SettingsCard {
            Text("隐私", style = MaterialTheme.typography.titleMedium, color = Ink)
            PrivacyBullet("照片不保存，看完就丢。")
            PrivacyBullet("不用注册，不用登录。")
            PrivacyBullet("卡号、身份证只留后四位。")
            QuietButton(text = "看隐私说明", onClick = onPrivacy)
        }

        SettingsCard {
            Text("找人帮忙", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "拿不准的时候，先打电话问人。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            SecondaryButton(text = "打电话求助", onClick = onHelp)
        }

        SettingsCard {
            Text("这台手机谁在用", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "现在选的是「中老年人」。家人要用，可以换成守护人界面。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
            )
            SecondaryButton(text = "换成守护人界面", onClick = onSwitchRole)
        }

        NoticeCard("配对码只是把两台手机连起来，不用注册，也不需要密码。")
        Spacer(Modifier.height(4.dp))
        Text(
            "放心一眼 1.0 · 不注册 · 不收钱",
            style = MaterialTheme.typography.labelSmall,
            color = InkMute,
            modifier = Modifier.fillMaxWidth(),
        )
        QuietButton(text = "回首页", onClick = onBack)
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
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

@Composable
private fun PrivacyBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .clip(CircleShape)
                .background(Brand),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
    }
}
