package com.example.exoplayerdemo.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BulletChat(
    modifier: Modifier = Modifier,
    bulletChatList: List<Danmu>,
    isPlaying: Boolean,
    currentPosition: Long
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    // 状态管理当前显示的弹幕
    val activeBulletChats = remember { mutableStateListOf<ActiveDanmu>() }

    // 处理弹幕添加
    LaunchedEffect(currentPosition) {
        val currentSeconds = (currentPosition / 1000).toInt()
        bulletChatList
            .filter { it.time == currentSeconds }
            .take(3)
            .forEach { danmu ->
                val color = try {
                    Color(android.graphics.Color.parseColor(danmu.color))
                } catch (e: Exception) {
                    Color.White
                }
                val textHeight = textMeasurer.measure(danmu.text).size.height
                val textWidth = textMeasurer.measure(danmu.text).size.width

                with(density) {
                    activeBulletChats.add(
                        ActiveDanmu(
                            text = danmu.text,
                            color = color,
                            x = view.width.toFloat(),
                            y = (activeBulletChats.size % 3 + 1) * textHeight.dp.toPx(),
                            speed = 10F,
                            textWidth = textWidth.toFloat()
                        )
                    )
                }
            }
    }

    // 平滑动画
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastFrameTime = withFrameNanos { it }
            while (true) {
                withFrameNanos { frameTime ->
                    val deltaSeconds = (frameTime - lastFrameTime) / 1_000_000_000f
                    lastFrameTime = frameTime

                    // 移除超出屏幕的弹幕
                    activeBulletChats.removeAll { danmu ->
                        danmu.x + danmu.textWidth < 0
                    }

                    // 更新位置
                    activeBulletChats.forEach { danmu ->
                        danmu.x -= danmu.speed * deltaSeconds
                    }
                }
            }
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            activeBulletChats.forEach { danmu ->
                drawText(
                    textMeasurer = textMeasurer,
                    text = danmu.text,
                    topLeft = Offset(danmu.x, danmu.y),
                    maxLines = 1,
                    style = TextStyle.Default.copy(fontSize = 16.sp, color = danmu.color)
                )
            }
        }
    }
}

// 弹幕数据类
private class ActiveDanmu(
    val text: String,
    val color: Color,
    var x: Float,
    var y: Float,
    // speed 表示每秒移动的像素数
    val speed: Float,
    var textWidth: Float = 0f // 需要计算文本宽度
)
