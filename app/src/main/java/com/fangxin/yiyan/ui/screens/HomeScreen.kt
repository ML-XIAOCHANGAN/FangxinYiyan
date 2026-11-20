package com.fangxin.yiyan.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.R
import com.fangxin.yiyan.domain.HistoryItem
import com.fangxin.yiyan.ui.components.BrandHeader
import com.fangxin.yiyan.ui.components.MascotBubble
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.RiskDot
import com.fangxin.yiyan.ui.components.SectionTitle
import com.fangxin.yiyan.ui.components.TrustLine
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.util.relativeTime

@Composable
fun HomeScreen(
    history: List<HistoryItem>,
    largeText: Boolean,
    onToggleText: () -> Unit,
    onHelp: () -> Unit,
    onCamera: () -> Unit,
    onText: () -> Unit,
    onSample: () -> Unit,
    onAssistant: () -> Unit,
    onHistory: () -> Unit,
    onPrivacy: () -> Unit,
    onOpenHistoryItem: (HistoryItem) -> Unit,
) {
    PageColumn {
        BrandHeader(
            largeText = largeText,
            onToggleText = onToggleText,
            onHelp = onHelp,
        )

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                MascotBubble("看不懂的消息，拍给我，我帮您看。")
                Spacer(Modifier.height(12.dp))
                Text(
                    "这条消息\n是骗子吗？",
                    style = MaterialTheme.typography.displayLarge,
                    color = Ink,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = "拍照给我看",
                    onClick = onCamera,
                    icon = Icons.Rounded.CameraAlt,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    QuickTile(
                        icon = Icons.Rounded.Keyboard,
                        label = "打字或说话",
                        onClick = onText,
                        modifier = Modifier.weight(1f),
                    )
                    QuickTile(
                        icon = Icons.Rounded.PlayArrow,
                        label = "看个例子",
                        onClick = onSample,
                        modifier = Modifier.weight(1f),
                    )
                    QuickTile(
                        imageRes = R.drawable.owl_idle,
                        label = "问问小望",
                        onClick = onAssistant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (history.isNotEmpty()) {
            SectionTitle("最近看过", "全部", onHistory)
            Column {
                history.take(2).forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = Line, thickness = 1.dp)
                    HomeHistoryRow(item = item, onClick = { onOpenHistoryItem(item) })
                }
            }
        }

        TextButton(
            onClick = onPrivacy,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text("照片看完就丢 · 隐私说明", fontSize = 17.sp, color = Brand)
        }

        TrustLine()
    }
}

@Composable
private fun QuickTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes imageRes: Int? = null,
) {
    Card(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.height(34.dp), contentAlignment = Alignment.Center) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(34.dp),
                    )
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Brand, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
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
