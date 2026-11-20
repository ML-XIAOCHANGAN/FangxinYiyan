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

val Paper = Color(0xFFFFF6EE)
val Card = Color(0xFFFFFFFF)
val Ink = Color(0xFF2B2118)
val InkSoft = Color(0xFF4A3F35)
val InkMute = Color(0xFF6B5D50)
val Line = Color(0xFFEFE0CE)
val LineStrong = Color(0xFFD9C4AC)
val Brand = Color(0xFFA63C1A)
val BrandDeep = Color(0xFF7A2A12)
val BrandTint = Color(0xFFFBE7DC)
val Accent = Color(0xFF1E6B45)
val DisabledBg = Color(0xFFEDE3D6)
val DisabledInk = Color(0xFF5F5449)
val NoticeBg = Color(0xFFF6EDE3)
val WarmBg = Color(0xFFFFF1D8)
val WarmLine = Color(0xFFD9A25A)
val WarmInk = Color(0xFF8A5A00)
val RedColor = Color(0xFFA61B1B)
val RedBg = Color(0xFFFBE9E7)
val AmberColor = Color(0xFF8A5A00)
val AmberDeep = Color(0xFF6B4400)
val AmberBg = Color(0xFFFFF1D6)
val GreenColor = Color(0xFF1E6B45)
val GreenBg = Color(0xFFE8F4EC)

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
    displayLarge = TextStyle(fontSize = 42.sp, lineHeight = 54.sp, fontWeight = FontWeight.Black),
    headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 46.sp, fontWeight = FontWeight.Black),
    headlineMedium = TextStyle(fontSize = 29.sp, lineHeight = 41.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 25.sp, lineHeight = 35.sp, fontWeight = FontWeight.ExtraBold),
    titleMedium = TextStyle(fontSize = 22.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = 21.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 19.sp, lineHeight = 29.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 19.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 17.sp, lineHeight = 24.sp),
)

private val FangxinShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(30.dp),
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
