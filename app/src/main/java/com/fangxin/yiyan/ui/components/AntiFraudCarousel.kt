package com.fangxin.yiyan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.R
import com.fangxin.yiyan.ui.theme.AmberBg
import com.fangxin.yiyan.ui.theme.AmberColor
import com.fangxin.yiyan.ui.theme.AmberDeep
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.BrandDeep
import com.fangxin.yiyan.ui.theme.Card
import com.fangxin.yiyan.ui.theme.GreenBg
import com.fangxin.yiyan.ui.theme.GreenColor
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.LineStrong
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.ui.theme.RedBg
import com.fangxin.yiyan.ui.theme.RedColor
import com.fangxin.yiyan.ui.theme.BrandTint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class FraudSlide(
    val art: Int,
    val tag: String,
    val head: String,
    val sub: String,
    val bar: String,
    val desc: String,
    val main: Color,
    val deep: Color,
    val bg: Color,
    val line: Color,
)

private val FRAUD_SLIDES = listOf(
    FraudSlide(
        art = R.drawable.anti_fraud_m_1,
        tag = "冒充公安法院",
        head = "挂断！",
        sub = "电话里说你犯法、要你转钱",
        bar = "拿不准，就打 96110",
        desc = "挂断：电话里说你犯法、要你转钱，都是骗子",
        main = RedColor,
        deep = Color(0xFF6E0F12),
        bg = RedBg,
        line = Color(0xFFE7C3BB),
    ),
    FraudSlide(
        art = R.drawable.anti_fraud_m_2,
        tag = "短信验证码",
        head = "不给！",
        sub = "短信里那 6 位数字，谁要都不给",
        bar = "验证码就是你的钱",
        desc = "不给：短信里的验证码，谁要都不能给",
        main = RedColor,
        deep = Color(0xFF6E0F12),
        bg = RedBg,
        line = Color(0xFFE7C3BB),
    ),
    FraudSlide(
        art = R.drawable.anti_fraud_m_3,
        tag = "冒充儿女借钱",
        head = "先打电话",
        sub = "微信里说急用钱，先打给本人",
        bar = "只认声音，不信文字",
        desc = "先打电话：微信里说急用钱，先打给儿女本人",
        main = AmberColor,
        deep = AmberDeep,
        bg = AmberBg,
        line = Color(0xFFE2CBA0),
    ),
    FraudSlide(
        art = R.drawable.anti_fraud_m_4,
        tag = "免费送礼 高息理财",
        head = "不转账",
        sub = "免费送礼、保本高息，都是圈套",
        bar = "钱只存银行，不乱投",
        desc = "不转账：免费送礼、保本高息，都是圈套",
        main = Brand,
        deep = BrandDeep,
        bg = BrandTint,
        line = Color(0xFFC3D3E5),
    ),
    FraudSlide(
        art = R.drawable.anti_fraud_m_5,
        tag = "全国反诈专线",
        head = "记住 96110",
        sub = "不转账 · 不给码 · 先挂断",
        bar = "24 小时都有人接",
        desc = "记住 96110：不转账、不给码、先挂断，24 小时都有人接",
        main = GreenColor,
        deep = Color(0xFF073D28),
        bg = GreenBg,
        line = Color(0xFFBBD9C8),
    ),
)

@Composable
fun AntiFraudCarousel(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { FRAUD_SLIDES.size })
    val scope = rememberCoroutineScope()
    var autoPaused by remember { mutableStateOf(false) }

    LaunchedEffect(autoPaused) {
        while (!autoPaused) {
            delay(7000)
            if (!autoPaused) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % FRAUD_SLIDES.size)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, Line, RoundedCornerShape(22.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press) autoPaused = true
                        }
                    }
                },
        ) { page ->
            FraudSlideCard(FRAUD_SLIDES[page])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CarouselArrow(
                forward = false,
                onClick = {
                    autoPaused = true
                    scope.launch {
                        pagerState.animateScrollToPage(
                            (pagerState.currentPage - 1 + FRAUD_SLIDES.size) % FRAUD_SLIDES.size,
                        )
                    }
                },
            )
            FRAUD_SLIDES.indices.forEach { i ->
                CarouselDot(
                    active = pagerState.currentPage == i,
                    label = "第 ${i + 1} 张，共 ${FRAUD_SLIDES.size} 张：${FRAUD_SLIDES[i].head}",
                    onClick = {
                        autoPaused = true
                        scope.launch { pagerState.animateScrollToPage(i) }
                    },
                )
            }
            CarouselArrow(
                forward = true,
                onClick = {
                    autoPaused = true
                    scope.launch {
                        pagerState.animateScrollToPage((pagerState.currentPage + 1) % FRAUD_SLIDES.size)
                    }
                },
            )
        }
    }
}

@Composable
private fun FraudSlideCard(slide: FraudSlide) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card),
    ) {
        Image(
            painter = painterResource(slide.art),
            contentDescription = slide.desc,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1060f / 1080f)
                .background(Paper),
        )
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(slide.bg)
                    .border(1.5.dp, slide.line, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    slide.tag,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = slide.deep,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                slide.head,
                fontSize = 30.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Black,
                color = slide.main,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                slide.sub,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = InkSoft,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(slide.deep)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    slide.bar,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CarouselArrow(forward: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(46.dp)
            .background(Card, CircleShape)
            .border(1.5.dp, LineStrong, CircleShape),
    ) {
        Icon(
            imageVector = if (forward) Icons.Rounded.ArrowForward else Icons.Rounded.ArrowBack,
            contentDescription = if (forward) "下一张" else "上一张",
            tint = BrandDeep,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun CarouselDot(active: Boolean, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(if (active) 24.dp else 9.dp)
                .height(9.dp)
                .clip(CircleShape)
                .background(if (active) Brand else LineStrong),
        )
    }
}
