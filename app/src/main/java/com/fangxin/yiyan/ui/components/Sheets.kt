package com.fangxin.yiyan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.HistoryItem
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.util.relativeTime

enum class AppSheet { HELP, HISTORY, PRIVACY }

@Composable
fun HelpSheetContent(
    familyPhone: String,
    onDialFamily: (String) -> Unit,
    onDialHotline: () -> Unit,
    onDialSocial: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
) {
    SheetColumn {
        Text("找人帮个忙", style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text(
            "别一个人拿主意。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        PrimaryButton(
            text = "打 96110 问警察",
            onClick = onDialHotline,
            icon = Icons.Rounded.Phone,
        )

        if (familyPhone.isBlank()) {
            NoticeCard("还没存家人号码。")
            SecondaryButton(
                text = "去存家人号码",
                onClick = onSettings,
                icon = Icons.Rounded.Person,
            )
        } else {
            SecondaryButton(
                text = "打给家人",
                onClick = { onDialFamily(familyPhone) },
                icon = Icons.Rounded.Person,
            )
            Text(
                "存的是尾号 ${familyPhone.takeLast(4)} 的号码。",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMute,
            )
        }

        SecondaryButton(
            text = "打 12333 问社保",
            onClick = onDialSocial,
            icon = Icons.Rounded.Phone,
        )

        Text(
            "这两个电话都是免费的，放心打。",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMute,
        )
        QuietButton(text = "我没事，先回去", onClick = onClose)
    }
}

@Composable
fun HistorySheetContent(
    items: List<HistoryItem>,
    onOpen: (HistoryItem) -> Unit,
    onGoCheck: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
) {
    var confirmingClear by remember { mutableStateOf(false) }

    SheetColumn {
        Text("最近看过", style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text(
            "只在这台手机上，最多三条。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        if (items.isEmpty()) {
            Text(
                "还没看过。",
                style = MaterialTheme.typography.titleMedium,
                color = InkSoft,
            )
            SecondaryButton(text = "去看一条", onClick = onGoCheck)
        } else {
            items.forEach { item ->
                HistoryRow(item = item, onClick = { onOpen(item) })
            }
            SecondaryButton(text = "关上", onClick = onClose)
            if (confirmingClear) {
                NoticeCard("清空后找不回来。")
                QuietButton(
                    text = "再点一下，清空记录",
                    onClick = {
                        onClear()
                        confirmingClear = false
                    },
                )
            } else {
                QuietButton(text = "清空本机记录", onClick = { confirmingClear = true })
            }
        }
    }
}

@Composable
fun PrivacySheetContent(onClose: () -> Unit) {
    SheetColumn {
        Text("隐私说明", style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text(
            "说人话，不绕弯。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        PrivacyPoint("照片不保存，看完就丢。")
        PrivacyPoint("不用注册，不用登录。")
        PrivacyPoint("卡号、身份证，只留后四位。")
        PrivacyPoint("本机只留最近三条结论。")
        PrivacyPoint("问小望时，文字交给云端 AI，只用来回答这一次，不保存。")
        PrivacyPoint("认字在这台手机里做，不上传。")

        NoticeCard("不想留记录，随手就能清空。")
        PrimaryButton(text = "我知道了", onClick = onClose)
    }
}

@Composable
private fun SheetColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
private fun HistoryRow(item: HistoryItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RiskDot(item.risk, size = 14)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        relativeTime(item.createdAt),
                        fontSize = 17.sp,
                        color = InkMute,
                    )
                }
                Text(
                    item.plain,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    color = InkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Brand,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun PrivacyPoint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Brand),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = InkSoft)
    }
}
