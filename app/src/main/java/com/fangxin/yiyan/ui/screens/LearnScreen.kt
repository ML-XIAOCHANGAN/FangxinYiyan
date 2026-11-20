package com.fangxin.yiyan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.ui.components.AntiFraudCarousel
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.RiskLegendRow
import com.fangxin.yiyan.ui.components.TrustLine
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft

@Composable
fun LearnScreen() {
    PageColumn {
        Spacer(Modifier.height(14.dp))
        Text(
            "学防骗",
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
        )
        Text(
            "骗子常说的话，先听一遍。",
            style = MaterialTheme.typography.bodyLarge,
            color = InkSoft,
        )

        AntiFraudCarousel()

        Text(
            "看颜色，就知道",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RiskLegendRow(RiskDecision.RED, "很可能是骗子", "先停下")
            RiskLegendRow(RiskDecision.AMBER, "有点可疑", "先别急着办")
            RiskLegendRow(RiskDecision.GREEN, "没看出问题", "动钱先核实")
            Text(
                "拿不准，就按黄色处理。",
                fontSize = 17.sp,
                color = InkMute,
            )
        }

        TrustLine()
    }
}
