package com.fangxin.yiyan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.SampleCase
import com.fangxin.yiyan.domain.Samples
import com.fangxin.yiyan.ui.components.NoticeCard
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.RiskDot
import com.fangxin.yiyan.ui.components.TopBackBar
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.riskPalette

@Composable
fun SamplesScreen(
    onBack: () -> Unit,
    onPick: (SampleCase) -> Unit,
) {
    PageColumn {
        TopBackBar(onBack = onBack, title = "回首页")

        Text(
            "先看个例子",
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
        )
        Text(
            "点一条，看看会怎么说。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        Samples.ALL.forEach { sample ->
            SampleRow(sample = sample, onClick = { onPick(sample) })
        }

        NoticeCard("这三条都是编的，不会真的发出去。")
    }
}

@Composable
private fun SampleRow(sample: SampleCase, onClick: () -> Unit) {
    val palette = riskPalette(sample.risk)
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskDot(sample.risk, size = 11)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        sample.title,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink,
                    )
                }
                Text(
                    sample.text,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = InkMute,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    "看完给：${sample.label}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.deep,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Brand,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
