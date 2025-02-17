package com.example.exoplayerdemo.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

val bulletChatFs = 16.sp
var i = -1

@Composable
fun BulletChat(
    bulletChatList: List<Danmu>,
    isPlaying: Boolean,
    currentPosition: Long,
    speed: Float = 80F,
    trigger: Long,
    danmuContextMenu: @Composable ((IntOffset, ActiveDanmu) -> Unit)? = null
) {
    val view = LocalView.current
    val animateScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val activeBulletChats = remember { mutableStateListOf<ActiveDanmu>() }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }

    fun startDanmuAnimation(danmu: ActiveDanmu, dt: Int) {
        // 1. 添加或替换列表中的弹幕
        val index = activeBulletChats.indexOfFirst { it.id == danmu.id }
        if (index == -1) {
            activeBulletChats.add(danmu)
        } else {
            activeBulletChats[index] = danmu
        }

        // 2. 创建实际 Job 并更新弹幕对象
        val newJob = animateScope.launch {
            danmu.animatable.animateTo(
                targetValue = -danmu.textWidth,
                animationSpec = tween(durationMillis = dt, easing = LinearEasing)
            )
            activeBulletChats.remove(danmu)
        }

        // 3. 更新弹幕对象的 Job 引用
        val updatedDanmu = danmu.copy(job = newJob)
        val latestIndex = activeBulletChats.indexOfFirst { it.id == updatedDanmu.id }
        if (latestIndex != -1) {
            activeBulletChats[latestIndex] = updatedDanmu
        }
    }

    fun resumeDanmuAnimation() {
        activeBulletChats.toList().forEach { danmu ->
            val currentX = danmu.animatable.value
            val targetX = -danmu.textWidth

            if (currentX <= targetX) {
                activeBulletChats.remove(danmu)
                return@forEach
            }

            val remainingDistance = currentX - targetX
            val remainingTime = (remainingDistance / speed * 1000).toInt()

            if (remainingTime > 0) {
                val latestDanmu = activeBulletChats.find { it.id == danmu.id } ?: return@forEach
                startDanmuAnimation(latestDanmu, remainingTime)
            } else {
                activeBulletChats.remove(danmu)
            }
        }
    }

    fun onClickDanmu(textPosition: Offset, item: ActiveDanmu, offset: Offset) {
        // 通过 id 查找当前最新的弹幕对象
        val currentItem = activeBulletChats.find { it.id == item.id } ?: return
        val globalOffset = Offset(
            x = textPosition.x + offset.x,
            y = item.y + item.textHeight + 4
        )
        menuPosition = globalOffset
        currentItem.job?.cancel()

        val currentIndex =
            activeBulletChats.indexOfFirst { it.id == currentItem.id }
        activeBulletChats.forEach { it.isShowPopover = false }
        if (currentIndex >= 0) {
            activeBulletChats[currentIndex] =
                currentItem.copy(isShowPopover = true)
        }
    }

    LaunchedEffect(trigger) {
        if (activeBulletChats.all { !it.isShowPopover }) {
            return@LaunchedEffect
        }
        activeBulletChats.forEach { it.isShowPopover = false }
        if (isPlaying) {
            resumeDanmuAnimation()
        }
    }

    // 处理弹幕暂停/恢复
    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            // 暂停时取消所有动画
            activeBulletChats.forEach { it.job?.cancel() }
        } else {
            resumeDanmuAnimation()
        }
    }

    // 处理新弹幕添加
    LaunchedEffect(currentPosition, isPlaying) {
        if (!isPlaying) return@LaunchedEffect

        val currentSeconds = (currentPosition / 1000).toInt()

        bulletChatList
            .filter { it.time == currentSeconds }
            .take(3)
            .forEach { item ->
                if (activeBulletChats.any { it.id == item.id }) {
                    return@forEach
                }

                val color = parseDanmuColor(item.color)
                val textLayoutResult = measureDanmuText(item.text, textMeasurer)
                i += 1

                val initialX = view.width.toFloat()
                val duration = (initialX / speed * 1000).toInt()
                val textWidth = textLayoutResult.size.width
                val textHeight = textLayoutResult.size.height

                ActiveDanmu(
                    id = item.id,
                    text = item.text,
                    color = color,
                    animatable = Animatable(initialX),
                    startTime = System.currentTimeMillis(),
                    y = (i % 3) * textHeight.toFloat(),
                    textWidth = textWidth.toFloat(),
                    textHeight = textHeight.toFloat(),
                    durationMillis = duration
                ).also { startDanmuAnimation(it, duration) }
            }
    }

    DisposableEffect(Unit) {
        onDispose { i = -1 }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        activeBulletChats.forEach { item ->
            var textPosition by remember { mutableStateOf(Offset.Zero) }

            Text(
                text = item.text,
                fontSize = bulletChatFs,
                color = item.color,
                modifier = Modifier
                    .offset {
                        IntOffset(item.animatable.value.toInt(), item.y.toInt())
                    }
                    .onGloballyPositioned { coordinates ->
                        textPosition = coordinates.localToWindow(Offset.Zero)
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            onClickDanmu(textPosition = textPosition, item = item, offset = offset)
                        }
                    }
            )

            if (item.isShowPopover) {
                danmuContextMenu?.invoke(
                    IntOffset(
                        menuPosition.x.toInt(),
                        menuPosition.y.toInt()
                    ),
                    item
                )
            }
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
    val id: Long,
    val text: String,
    val color: Color,
    var animatable: Animatable<Float, AnimationVector1D>,
    var job: Job? = null,
    val startTime: Long,
    val y: Float,
    val textWidth: Float,
    val textHeight: Float,
    val durationMillis: Int,
    var isShowPopover: Boolean = false
)
