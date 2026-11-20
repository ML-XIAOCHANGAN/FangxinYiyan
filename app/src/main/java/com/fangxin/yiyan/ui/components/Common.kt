package com.fangxin.yiyan.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.R
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.BrandDeep
import com.fangxin.yiyan.ui.theme.BrandTint
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.DisabledBg
import com.fangxin.yiyan.ui.theme.DisabledInk
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.LineStrong
import com.fangxin.yiyan.ui.theme.NoticeBg
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.ui.theme.WarmBg
import com.fangxin.yiyan.ui.theme.riskPalette

@Composable
fun PageColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
fun TopBackBar(
    onBack: () -> Unit,
    title: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = InkSoft, modifier = Modifier.size(28.dp))
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
        }
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun BrandHeader(
    largeText: Boolean,
    onToggleText: () -> Unit,
    onHelp: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brand),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "放心一眼",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                "帮您看一眼",
                style = MaterialTheme.typography.labelMedium,
                color = InkMute,
            )
        }
        HeaderChip(
            text = if (largeText) "字已大" else "字大点",
            selected = largeText,
            onClick = onToggleText,
        )
        Spacer(Modifier.width(8.dp))
        HeaderChip(text = "求助", selected = false, onClick = onHelp)
    }
}

@Composable
private fun HeaderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    SurfaceButton(
        onClick = onClick,
        selected = selected,
        modifier = Modifier.heightIn(min = 52.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) BrandDeep else InkSoft,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 13.dp),
        )
    }
}

@Composable
fun MascotBubble(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.owl_idle),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                .background(WarmBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "小望：$text",
                style = MaterialTheme.typography.labelMedium,
                color = Ink,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SurfaceButton(
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.5.dp, if (selected) Brand else LineStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) BrandTint else CardColor,
            contentColor = InkSoft,
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        content = { content() },
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp),
        shape = RoundedCornerShape(24.dp),
        border = if (enabled) null else BorderStroke(1.5.dp, LineStrong),
        colors = ButtonDefaults.buttonColors(
            containerColor = Brand,
            contentColor = Color.White,
            disabledContainerColor = DisabledBg,
            disabledContentColor = DisabledInk,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(text, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 74.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, if (enabled) LineStrong else Line),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CardColor,
            contentColor = BrandDeep,
            disabledContainerColor = Paper,
            disabledContentColor = InkMute,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, fontSize = 21.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QuietButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 56.dp)) {
        Text(text, fontSize = 19.sp, color = Brand, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BigActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(
            width = if (highlighted) 2.dp else 1.dp,
            color = if (highlighted) Brand else Line,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (highlighted) Brand else BrandTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (highlighted) Color.White else Brand,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = InkMute)
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
fun SectionTitle(
    text: String,
    trailing: String? = null,
    onTrailing: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Brand),
        )
        Spacer(Modifier.width(9.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(Modifier.weight(1f))
        if (trailing != null && onTrailing != null) {
            TextButton(onClick = onTrailing, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(trailing, fontSize = 18.sp, color = BrandDeep, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RiskBadge(risk: RiskDecision, label: String) {
    val palette = riskPalette(risk)
    val glyph = when (risk) {
        RiskDecision.RED -> "!"
        RiskDecision.GREEN -> "✓"
        else -> "?"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(palette.color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                glyph,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            color = palette.deep,
        )
    }
}

@Composable
fun RiskLegendRow(risk: RiskDecision, title: String, subtitle: String) {
    val palette = riskPalette(risk)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(palette.color),
        )
        Spacer(Modifier.width(10.dp))
        Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.width(8.dp))
        Text(subtitle, fontSize = 18.sp, color = InkMute)
    }
}

@DrawableRes
fun guideImageRes(category: String): Int = when (category) {
    "医院机器" -> R.drawable.ill_kiosk
    "手机" -> R.drawable.ill_phone
    "养老金" -> R.drawable.ill_pension
    else -> R.drawable.ill_empty
}

@Composable
fun GuideArt(category: String, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(guideImageRes(category)),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
    )
}

@Composable
fun NoticeCard(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NoticeBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Rounded.Lock,
            contentDescription = null,
            tint = InkSoft,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = InkSoft,
        )
    }
}

@Composable
fun TrustLine() {
    Text(
        text = "不注册 · 不收钱 · 照片不留",
        style = MaterialTheme.typography.labelSmall,
        color = InkMute,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun ProcessingOverlay(
    visible: Boolean,
    title: String = "正在看，请稍等",
) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Brand,
                strokeWidth = 6.dp,
                modifier = Modifier.size(68.dp),
            )
            Spacer(Modifier.height(22.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text(
                "一般两三秒",
                style = MaterialTheme.typography.labelMedium,
                color = InkMute,
            )
        }
    }
}

@Composable
fun RiskDot(risk: RiskDecision, size: Int = 14) {
    val palette = riskPalette(risk)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(palette.color),
    )
}

@Composable
fun ValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Paper)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = InkMute)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = Ink, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SimpleBullet(text: String, color: Color = InkSoft) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 9.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
    }
}
