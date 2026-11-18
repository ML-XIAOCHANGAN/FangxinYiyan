package com.fangxin.yiyan.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fangxin.yiyan.R
import com.fangxin.yiyan.assistant.OwlAnswer
import com.fangxin.yiyan.assistant.OwlMood
import com.fangxin.yiyan.assistant.OwlState
import com.fangxin.yiyan.domain.RiskDecision
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.SimpleBullet
import com.fangxin.yiyan.ui.components.TopBackBar
import com.fangxin.yiyan.ui.components.QuietButton
import com.fangxin.yiyan.ui.theme.AmberColor
import com.fangxin.yiyan.ui.theme.Brand
import com.fangxin.yiyan.ui.theme.Card as CardColor
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.ui.theme.LineStrong
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.ui.theme.RedColor
import com.fangxin.yiyan.ui.theme.riskPalette
import com.fangxin.yiyan.ui.theme.BrandTint
import com.fangxin.yiyan.ui.theme.WarmLine
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val MAX_INPUT = 600

private enum class OwlPose(@DrawableRes val res: Int) {
    IDLE(R.drawable.owl_idle),
    THINKING(R.drawable.owl_thinking),
    CHAT(R.drawable.owl_chat),
    ALERT(R.drawable.owl_alert),
    WORRY(R.drawable.owl_worry),
    SAD(R.drawable.owl_sad),
}

private fun poseFor(mood: OwlMood): OwlPose = when (mood) {
    OwlMood.THINKING -> OwlPose.THINKING
    OwlMood.CHAT -> OwlPose.CHAT
    OwlMood.RED -> OwlPose.ALERT
    OwlMood.AMBER -> OwlPose.WORRY
    OwlMood.ERROR -> OwlPose.SAD
    OwlMood.IDLE -> OwlPose.IDLE
}

@Composable
fun AssistantScreen(
    state: OwlState,
    onBack: () -> Unit,
    onAsk: (String) -> Unit,
    onReset: () -> Unit,
    onDialHotline: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }
    var greetToken by remember { mutableIntStateOf(0) }
    var greeting by remember { mutableStateOf(false) }
    var answerTop by remember { mutableIntStateOf(0) }
    val keyboard = LocalSoftwareKeyboardController.current
    val scroll = rememberScrollState()
    val judge = state.answer as? OwlAnswer.Judge

    BackHandler { onBack() }

    LaunchedEffect(state.answer, answerTop) {
        when {
            state.answer == null -> scroll.animateScrollTo(0)
            answerTop > 0 -> scroll.animateScrollTo(answerTop)
        }
    }

    LaunchedEffect(greetToken) {
        if (greetToken == 0) return@LaunchedEffect
        greeting = true
        delay(1700)
        greeting = false
    }

    val mood = if (greeting && !state.busy) OwlMood.CHAT else state.mood
    val bubble = when {
        greeting && !state.busy -> "我在呢，慢慢说。"
        focused && !state.busy -> "慢慢打，不着急。"
        else -> state.bubble
    }

    val submit = {
        val text = input.trim()
        if (text.length >= 4 && !state.busy) {
            keyboard?.hide()
            onAsk(text)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            TopBackBar(onBack = onBack, title = "问问小望")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (judge == null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "AI 助手 · 不睡觉，专盯坏消息",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand,
                        modifier = Modifier
                            .background(BrandTint, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("拿不准的，先问小望。", style = MaterialTheme.typography.headlineMedium, color = Ink)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "把可疑的短信、微信，或电话里说的话粘进来。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft,
                    )
                }
            }

            OwlPerch(
                bubble = bubble,
                mood = mood,
                focused = focused,
                burstToken = greetToken,
                compact = judge != null,
                onGreet = { greetToken++ },
            )

            val cardModifier = Modifier.onGloballyPositioned {
                answerTop = it.positionInParent().y.roundToInt()
            }
            when (val answer = state.answer) {
                is OwlAnswer.Chat -> OwlChatCard(reply = answer.reply, modifier = cardModifier)
                is OwlAnswer.Judge -> OwlVerdictCard(
                    answer = answer,
                    onDialHotline = onDialHotline,
                    onReset = {
                        input = ""
                        onReset()
                    },
                    modifier = cardModifier,
                )
                null -> Unit
            }

            if (judge == null) {
                AskBox(
                    input = input,
                    onInput = { input = it },
                    busy = state.busy,
                    onSubmit = submit,
                    onFocusChanged = { focused = it },
                    onExample = { input = it },
                )
            }

            Text(
                "内容不保存 · 判断只作参考，拿不准打 96110，或让家人马上回拨。",
                fontSize = 17.sp,
                lineHeight = 26.sp,
                color = InkMute,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OwlPerch(
    bubble: String,
    mood: OwlMood,
    focused: Boolean,
    burstToken: Int,
    compact: Boolean,
    onGreet: () -> Unit,
) {
    var blink by remember { mutableStateOf(false) }
    var curious by remember { mutableFloatStateOf(0f) }
    val pose = poseFor(mood)
    val mascot = if (compact) 148.dp else 208.dp
    val stageWidth = if (compact) 200.dp else 250.dp
    val stageHeight = mascot + 18.dp

    LaunchedEffect(mood, focused) {
        blink = false
        if (mood != OwlMood.IDLE || focused) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(3600, 6800))
            blink = true
            delay(150)
            blink = false
            delay(140)
        }
    }

    LaunchedEffect(mood, focused) {
        curious = 0f
        if (mood != OwlMood.IDLE || focused) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(5200, 10400))
            val amount = 1.4f + Random.nextFloat() * 1.2f
            val target = if (Random.nextBoolean()) amount else -amount
            animate(curious, target, animationSpec = tween(420)) { value, _ -> curious = value }
            delay(900)
            animate(curious, 0f, animationSpec = tween(520)) { value, _ -> curious = value }
            delay(400)
        }
    }

    val floatTransition = rememberInfiniteTransition(label = "owlFloat")
    val floatY by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "owlFloatY",
    )
    val bob by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(620, easing = LinearEasing), RepeatMode.Reverse),
        label = "owlBob",
    )
    val shake by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0f at 0
                -2.4f at 70
                2.4f at 140
                -1.8f at 210
                1.2f at 280
                0f at 360
                0f at 900
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "owlShake",
    )
    val sway by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2600
                0f at 0
                3f at 650
                0f at 1300
                -3f at 1950
                0f at 2600
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "owlSway",
    )
    val pulse by floatTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "owlHalo",
    )

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.03f else 1f,
        animationSpec = tween(260),
        label = "owlScale",
    )
    val lift by animateFloatAsState(
        targetValue = if (focused) -4f else 0f,
        animationSpec = tween(260),
        label = "owlLift",
    )

    val glow = when (mood) {
        OwlMood.RED -> RedColor
        OwlMood.AMBER -> AmberColor
        OwlMood.CHAT -> Brand
        OwlMood.ERROR -> InkMute
        else -> WarmLine
    }

    val sparks = remember { Animatable(0f) }
    LaunchedEffect(burstToken) {
        if (burstToken == 0) return@LaunchedEffect
        sparks.snapTo(0f)
        sparks.animateTo(1f, animationSpec = tween(650, easing = LinearOutSlowInEasing))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OwlBubble(text = bubble)

        Box(
            modifier = Modifier
                .width(stageWidth)
                .height(stageHeight)
                .semantics {
                    contentDescription = "和小望打个招呼"
                    role = Role.Button
                }
                .clickable(role = Role.Button) { onGreet() },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(mascot + 40.dp)
                    .alpha(0.55f * pulse)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(glow.copy(alpha = 0.42f), Color.Transparent),
                        ),
                        CircleShape,
                    ),
            )

            Box(
                modifier = Modifier
                    .size(mascot)
                    .graphicsLayer {
                        translationY = floatY + lift + if (mood == OwlMood.THINKING) bob else 0f
                        val moodRotation = when (mood) {
                            OwlMood.RED -> shake
                            OwlMood.AMBER -> sway
                            else -> curious
                        }
                        rotationZ = moodRotation
                        scaleX = scale
                        scaleY = scale
                    },
            ) {
                Crossfade(targetState = pose, animationSpec = tween(220), label = "owlPose") { current ->
                    Image(
                        painter = painterResource(current.res),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                val blinkAlpha by animateFloatAsState(
                    targetValue = if (blink && pose == OwlPose.IDLE) 1f else 0f,
                    animationSpec = tween(90),
                    label = "owlBlink",
                )
                if (blinkAlpha > 0.01f) {
                    Image(
                        painter = painterResource(R.drawable.owl_blink),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(blinkAlpha),
                    )
                }
            }

            ThinkingDots(
                visible = mood == OwlMood.THINKING,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 30.dp, end = if (compact) 18.dp else 30.dp),
            )

            val badge = when (mood) {
                OwlMood.RED -> "!"
                OwlMood.AMBER -> "?"
                else -> null
            }
            OwlBadge(
                badge = badge,
                compact = compact,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = if (compact) 8.dp else 16.dp),
            )

            if (sparks.value < 1f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val progress = sparks.value
                    if (progress <= 0f) return@Canvas
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val distance = (24.dp + 66.dp * progress).toPx()
                    val radius = 3.2.dp.toPx()
                    val colors = listOf(Color(0xFFD9920F), Brand, Color(0xFFC02B15))
                    repeat(9) { index ->
                        val angle = (index * 40f - 90f) * (Math.PI / 180f).toFloat()
                        drawCircle(
                            color = colors[index % colors.size].copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                            radius = radius,
                            center = center + Offset(cos(angle) * distance, sin(angle) * distance),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OwlBadge(badge: String?, compact: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = badge != null,
        enter = scaleIn(initialScale = 0.4f) + fadeIn(),
        exit = scaleOut(targetScale = 0.4f) + fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 34.dp else 40.dp)
                .background(if (badge == "!") RedColor else AmberColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                badge.orEmpty(),
                color = Color.White,
                fontSize = if (compact) 20.sp else 24.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun OwlBubble(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            border = BorderStroke(1.5.dp, Ink),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            AnimatedContent(
                targetState = text,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(initialScale = 0.94f)) togetherWith fadeOut(tween(90))
                },
                label = "owlBubble",
            ) { current ->
                Text(
                    current,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                )
            }
        }
        Canvas(
            modifier = Modifier
                .size(18.dp, 9.dp),
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, CardColor)
        }
    }
}

@Composable
private fun ThinkingDots(visible: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "owlDots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "owlDotsPhase",
    )
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(3) { index ->
                val active = (phase.toInt() % 3) == index
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .alpha(if (active) 1f else 0.45f)
                        .background(Brand, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun AskBox(
    input: String,
    onInput: (String) -> Unit,
    busy: Boolean,
    onSubmit: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onExample: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("把可疑的话粘在这儿", style = MaterialTheme.typography.titleMedium, color = Ink)
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.length <= MAX_INPUT) onInput(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                placeholder = {
                    Text(
                        "例：我是公安局的，你涉嫌洗钱，马上把钱转到安全账户。也可以问：你叫什么名字？",
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        color = InkMute,
                    )
                },
                minLines = 3,
                maxLines = 7,
                textStyle = TextStyle(fontSize = 19.sp, lineHeight = 30.sp, color = Ink),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Brand,
                    unfocusedBorderColor = LineStrong,
                    focusedContainerColor = Paper,
                    unfocusedContainerColor = Paper,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSubmit() }),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${input.length} / $MAX_INPUT", fontSize = 17.sp, color = InkMute)
                Spacer(Modifier.weight(1f))
                Text("写满四个字就能问", fontSize = 17.sp, color = InkSoft)
            }
            PrimaryButton(
                text = if (busy) "小望在琢磨…" else "问小望",
                onClick = onSubmit,
                icon = Icons.Rounded.Send,
                enabled = !busy && input.trim().length >= 4,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExampleChip("公安让我转账", onExample, Modifier.weight(1f))
                ExampleChip("你叫什么名字", onExample, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ExampleChip(text: String, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable { onPick(text) },
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        border = BorderStroke(1.5.dp, LineStrong),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkSoft,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OwlChatCard(reply: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "小望说",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Brand,
                modifier = Modifier
                    .background(BrandTint, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Text(
                reply,
                fontSize = 21.sp,
                lineHeight = 32.sp,
                color = Ink,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun OwlVerdictCard(
    answer: OwlAnswer.Judge,
    onDialHotline: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = riskPalette(answer.verdict)
    val glyph = if (answer.verdict == RiskDecision.RED) "!" else "?"

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.bg),
        border = BorderStroke(1.5.dp, palette.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(palette.color, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(glyph, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("小望的判断", fontSize = 17.sp, color = InkMute, fontWeight = FontWeight.Bold)
                    Text(
                        answer.headline,
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.deep,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("为啥这么说", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkMute)
                answer.reasons.forEach { reason ->
                    SimpleBullet(reason, color = palette.deep)
                }
            }

            if (answer.steps.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("接下来这么做", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkMute)
                    answer.steps.forEachIndexed { index, step ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(palette.color, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                step,
                                fontSize = 19.sp,
                                lineHeight = 28.sp,
                                color = Ink,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }

            SecondaryButton(
                text = "打 96110 问警察",
                onClick = onDialHotline,
                icon = Icons.Rounded.Phone,
            )
            QuietButton(text = "再问一条", onClick = onReset)
        }
    }
}
