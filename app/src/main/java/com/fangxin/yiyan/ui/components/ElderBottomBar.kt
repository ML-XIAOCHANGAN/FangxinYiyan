package com.fangxin.yiyan.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.BrandDeep
import com.fangxin.yiyan.ui.theme.BrandTint
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.Line

data class ElderTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val ELDER_TABS = listOf(
    ElderTab("home", "看消息", Icons.Rounded.CameraAlt),
    ElderTab("guides", "办事", Icons.Rounded.MenuBook),
    ElderTab("learn", "防骗", Icons.Rounded.Shield),
    ElderTab("mine", "我的", Icons.Rounded.Person),
)

@Composable
fun ElderBottomBar(
    currentRoute: String?,
    onSelect: (String) -> Unit,
) {
    Surface(color = CardColor) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = Line, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ELDER_TABS.forEach { tab ->
                    TabItem(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = { onSelect(tab.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: ElderTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(if (selected) BrandTint else CardColor)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                tab.icon,
                contentDescription = null,
                tint = if (selected) Brand else InkMute,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            tab.label,
            fontSize = 17.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            color = if (selected) BrandDeep else InkMute,
        )
    }
}
