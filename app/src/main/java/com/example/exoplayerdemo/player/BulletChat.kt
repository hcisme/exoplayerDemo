package com.example.exoplayerdemo.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

val bulletChatFs = 16.sp
var i = -1

@Composable
fun BulletChat(
    modifier: Modifier = Modifier,
    bulletChatList: List<Danmu>,
    isPlaying: Boolean,
    currentPosition: Long,
) {
    val view = LocalView.current
    val animateScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val activeBulletChats = remember { mutableStateListOf<ActiveDanmu>() }

    fun startDanmuAnimation(danmu: ActiveDanmu, dt: Int) {
        activeBulletChats.add(danmu)

        animateScope.launch {
            danmu.animatable.animateTo(
                targetValue = -danmu.textWidth,
                animationSpec = tween(
                    durationMillis = dt,
                    easing = LinearEasing
                )
            )
            activeBulletChats.remove(danmu)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            i = -1
        }
    }

    // 处理弹幕添加
    LaunchedEffect(currentPosition, isPlaying) {
        if (!isPlaying) {
            activeBulletChats.clear()
            return@LaunchedEffect
        }
        val currentSeconds = (currentPosition / 1000).toInt()

        bulletChatList
            .filter { it.time == currentSeconds }
            .take(3)
            .forEach { item ->
                val color = parseDanmuColor(item.color)
                val textLayoutResult = measureDanmuText(item.text, textMeasurer)
                i += 1

                val initialX = view.width.toFloat()
                val duration = (initialX / 200f * 1000).toInt()

                ActiveDanmu(
                    text = item.text,
                    color = color,
                    animatable = Animatable(initialX),
                    startTime = System.currentTimeMillis(),
                    y = (i % 4) * textLayoutResult.size.height.toFloat(),
                    textWidth = textLayoutResult.size.width.toFloat()
                ).also {
                    startDanmuAnimation(danmu = it, dt = duration)
                }
            }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier = modifier.fillMaxSize()) {
            activeBulletChats.forEach { item ->
                Text(
                    item.text,
                    fontSize = bulletChatFs,
                    color = item.color,
                    maxLines = 1,
                    modifier = Modifier.offset {
                        IntOffset(item.animatable.value.toInt(), item.y.toInt())
                    }
                )
            }

//            Canvas(Modifier.fillMaxSize()) {
//                activeBulletChats.forEach { danmu ->
//                    drawText(
//                        textMeasurer = textMeasurer,
//                        text = danmu.text,
//                        topLeft = Offset(danmu.animatable.value, danmu.y),
//                        maxLines = 1,
//                        style = TextStyle.Default.copy(
//                            fontSize = bulletChatFs,
//                            color = danmu.color
//                        )
//                    )
//                }
//            }
        }
    }
}

// 测量弹幕文本尺寸
private fun measureDanmuText(text: String, textMeasurer: TextMeasurer): TextLayoutResult {
    return textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle.Default.copy(fontSize = bulletChatFs)
    )
}

// 解析弹幕颜色
private fun parseDanmuColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.White // 默认颜色
    }
}

data class ActiveDanmu(
    val text: String,
    val color: Color,
    val animatable: Animatable<Float, AnimationVector1D>,
    val startTime: Long,
    val y: Float,
    val textWidth: Float
)
