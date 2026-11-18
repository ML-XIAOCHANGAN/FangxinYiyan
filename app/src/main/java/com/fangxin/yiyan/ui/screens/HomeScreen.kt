package com.fangxin.yiyan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.R
import com.fangxin.yiyan.domain.HistoryItem
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.ui.components.AntiFraudCarousel
import com.fangxin.yiyan.ui.components.BrandHeader
import com.fangxin.yiyan.ui.components.GuideArt
import com.fangxin.yiyan.ui.components.NoticeCard
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.RiskDot
import com.fangxin.yiyan.ui.components.RiskLegendRow
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.SectionTitle
import com.fangxin.yiyan.ui.components.TrustLine
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.LineStrong
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.ui.theme.BrandTint
import com.fangxin.yiyan.ui.theme.WarmBg
import com.fangxin.yiyan.ui.theme.WarmInk
import com.fangxin.yiyan.ui.theme.WarmLine
import com.fangxin.yiyan.util.relativeTime

private data class GuideTile(
    val id: String,
    val category: String,
    val label: String,
)

private val HOME_TILES = listOf(
    GuideTile("kiosk-insurance", "医院机器", "医院机器"),
    GuideTile("phone-font", "手机", "把字调大"),
    GuideTile("social-pension", "养老金", "查养老金"),
)

@Composable
fun HomeScreen(
    history: List<HistoryItem>,
    largeText: Boolean,
    onToggleText: () -> Unit,
    onHelp: () -> Unit,
    onCamera: () -> Unit,
    onText: () -> Unit,
    onSample: () -> Unit,
    onGuides: () -> Unit,
    onGuide: (String) -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit,
    onDialHotline: () -> Unit,
    onAssistant: () -> Unit,
    onOpenHistoryItem: (HistoryItem) -> Unit,
) {
    PageColumn {
        BrandHeader(
            largeText = largeText,
            onToggleText = onToggleText,
            onHelp = onHelp,
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            border = BorderStroke(1.dp, Line),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "给爸妈用的",
                    style = MaterialTheme.typography.labelSmall,
                    color = Brand,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(BrandTint, RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "这条消息\n是骗子吗？",
                    style = MaterialTheme.typography.displayLarge,
                    color = Ink,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "拍张照片，我帮您看一眼。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkSoft,
                )
                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    text = "拍照给我看",
                    onClick = onCamera,
                    icon = Icons.Rounded.CameraAlt,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTile(
                        icon = Icons.Rounded.Keyboard,
                        label = "打字或说话",
                        onClick = onText,
                        modifier = Modifier.weight(1f),
                    )
                    QuickTile(
                        icon = Icons.Rounded.PlayArrow,
                        label = "先看个例子",
                        onClick = onSample,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        OwlEntryCard(onClick = onAssistant)

        SectionTitle("防骗提醒")
        AntiFraudCarousel()

        SectionTitle("看颜色，就知道")
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            border = BorderStroke(1.dp, Line),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RiskLegendRow(RiskDecision.RED, "很可能是骗子", "先停下")
                RiskLegendRow(RiskDecision.AMBER, "有点可疑", "先别急着办")
                RiskLegendRow(RiskDecision.GREEN, "没看出问题", "动钱先核实")
                Text(
                    "拿不准，就按黄色处理。",
                    fontSize = 17.sp,
                    color = InkMute,
                )
            }
        }

        SectionTitle("大字办事指引", "全部", onGuides)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (tile in HOME_TILES) {
                GuideTileCard(
                    tile = tile,
                    onClick = { onGuide(tile.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (history.isNotEmpty()) {
            SectionTitle("最近看过")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                border = BorderStroke(1.dp, Line),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    history.forEach { item ->
                        HomeHistoryRow(item = item, onClick = { onOpenHistoryItem(item) })
                    }
                }
            }
        }

        NoticeCard("照片看完就丢，不保存；不用注册。")

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onHistory, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("最近看过", fontSize = 17.sp, color = Brand)
            }
            Text("·", color = LineStrong)
            TextButton(onClick = onPrivacy, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("隐私说明", fontSize = 17.sp, color = Brand)
            }
        }

        TrustLine()
    }
}

@Composable
private fun OwlEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = WarmBg),
        border = BorderStroke(1.5.dp, WarmLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.owl_idle),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "拿不准，问问小望",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink,
                )
                Text(
                    "AI 助手帮您看可疑短信和电话",
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    color = InkSoft,
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = WarmInk,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .heightIn(min = 94.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        border = BorderStroke(1.5.dp, LineStrong),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = Brand, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GuideTileCard(
    tile: GuideTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GuideArt(
                category = tile.category,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tile.label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun HomeHistoryRow(item: HistoryItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
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
