package com.fangxin.yiyan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.HistoryItem
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.RiskDot
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.SectionTitle
import com.fangxin.yiyan.ui.components.TrustLine
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.util.relativeTime

@Composable
fun MineScreen(
    history: List<HistoryItem>,
    onOpenHistoryItem: (HistoryItem) -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit,
    onHelp: () -> Unit,
    onDialHotline: () -> Unit,
) {
    PageColumn {
        Spacer(Modifier.height(14.dp))
        Text(
            "我的",
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
        )
        Text(
            "记录、家人、隐私，都在这儿。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        SectionTitle("最近看过")
        if (history.isEmpty()) {
            Text(
                "还没看过。回「看消息」拍一张，试一次。",
                fontSize = 19.sp,
                lineHeight = 28.sp,
                color = InkMute,
            )
        } else {
            Column {
                history.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = Line, thickness = 1.dp)
                    MineHistoryRow(item = item, onClick = { onOpenHistoryItem(item) })
                }
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
                text = "家人设置",
                onClick = onSettings,
                icon = Icons.Rounded.Settings,
                modifier = Modifier.weight(1f),
            )
        }

        Column {
            MineLinkRow(
                icon = Icons.Rounded.Person,
                title = "找人帮忙",
                subtitle = "打电话问家人或社区",
                onClick = onHelp,
            )
            HorizontalDivider(color = Line, thickness = 1.dp)
            MineLinkRow(
                icon = Icons.Rounded.Lock,
                title = "隐私说明",
                subtitle = "照片不保存，不用注册",
                onClick = onPrivacy,
            )
        }

        TrustLine()
    }
}

@Composable
private fun MineHistoryRow(item: HistoryItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RiskDot(item.risk, size = 12)
        Spacer(Modifier.width(10.dp))
        Text(
            item.label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            relativeTime(item.createdAt),
            fontSize = 17.sp,
            color = InkMute,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = InkMute,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun MineLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Brand, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(subtitle, fontSize = 17.sp, color = InkMute)
        }
        Icon(
            Icons.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = InkMute,
            modifier = Modifier.size(24.dp),
        )
    }
}
