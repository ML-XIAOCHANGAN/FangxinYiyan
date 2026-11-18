package com.fangxin.yiyan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.domain.RiskDecision

val Paper = Color(0xFFF8F4EC)
val Card = Color(0xFFFFFFFF)
val Ink = Color(0xFF1C1B18)
val InkSoft = Color(0xFF43423C)
val InkMute = Color(0xFF5A574E)
val Line = Color(0xFFE3DCCD)
val LineStrong = Color(0xFFC9BFA9)
val Brand = Color(0xFF123E6B)
val BrandDeep = Color(0xFF0B2747)
val BrandTint = Color(0xFFE4ECF5)
val Accent = Color(0xFFA63C1A)
val DisabledBg = Color(0xFFE8E2D5)
val DisabledInk = Color(0xFF55524A)
val NoticeBg = Color(0xFFF1ECE0)
val WarmBg = Color(0xFFF7EFDC)
val WarmLine = Color(0xFFB98528)
val WarmInk = Color(0xFF8A5A00)
val RedColor = Color(0xFF8E1B12)
val RedBg = Color(0xFFFBEAE6)
val AmberColor = Color(0xFF8A5A00)
val AmberDeep = Color(0xFF6B4400)
val AmberBg = Color(0xFFFBF1DC)
val GreenColor = Color(0xFF0B4F35)
val GreenBg = Color(0xFFE7F3EC)

data class RiskPalette(
    val color: Color,
    val deep: Color,
    val bg: Color,
)

fun riskPalette(risk: RiskDecision): RiskPalette = when (risk) {
    RiskDecision.RED -> RiskPalette(RedColor, RedColor, RedBg)
    RiskDecision.GREEN -> RiskPalette(GreenColor, GreenColor, GreenBg)
    else -> RiskPalette(AmberColor, AmberDeep, AmberBg)
}

private val FangxinTypography = Typography(
    displayLarge = TextStyle(fontSize = 40.sp, lineHeight = 52.sp, fontWeight = FontWeight.Black),
    headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 46.sp, fontWeight = FontWeight.Black),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold),
    titleMedium = TextStyle(fontSize = 21.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 29.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
)

private val FangxinShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun FangxinTheme(largeText: Boolean, content: @Composable () -> Unit) {
    val base = LocalDensity.current
    val scaled = Density(
        density = base.density,
        fontScale = base.fontScale * if (largeText) 1.15f else 1f,
    )

    CompositionLocalProvider(LocalDensity provides scaled) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Brand,
                onPrimary = Color.White,
                primaryContainer = BrandTint,
                onPrimaryContainer = BrandDeep,
                secondary = Accent,
                onSecondary = Color.White,
                background = Paper,
                onBackground = Ink,
                surface = Card,
                onSurface = Ink,
                surfaceVariant = Paper,
                onSurfaceVariant = InkSoft,
                outline = LineStrong,
                error = RedColor,
                onError = Color.White,
            ),
            typography = FangxinTypography,
            shapes = FangxinShapes,
            content = content,
        )
    }
}
