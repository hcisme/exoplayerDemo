package com.chc.videoplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chc.player.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerController(
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
    isPlaying: Boolean,
    isShowBulletChat: Boolean,
    title: String,
    currentTimePosition: Float,
    duration: Float,
    onClickBackButton: () -> Unit = {},
    onClickPlayButton: () -> Unit = {},
    onSliderValueChange: (Float) -> Unit = {},
    onSliderValueChangeFinished: (() -> Unit)? = null,
    onClickFullScreen: () -> Unit = {},
    onClickDanmuIcon: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickEnd: (() -> Unit)? = null,
    onListenerControllerVisible: ((Boolean) -> Unit)? = null,
    centerAreaBox: @Composable (RowScope.() -> Unit) = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isShowController by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    var longPressJob by remember { mutableStateOf<Job?>(null) }

    // 监听交互状态
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    longPressJob?.cancel()
                }

                else -> {}
            }
        }
    }

    // 自动隐藏控制器
    LaunchedEffect(isShowController) {
        if (isShowController) {
            delay(5000)
            isShowController = false
            onListenerControllerVisible?.invoke(false)
        }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .combinedClickable(
                    indication = null,
                    interactionSource = interactionSource,
                    onDoubleClick = onDoubleClick,
                    onLongClick = {
                        longPressJob = coroutineScope.launch {
                            try {
                                onLongClick?.invoke()
                                delay(Long.MAX_VALUE)
                            } finally {
                                onLongClickEnd?.invoke()
                            }
                        }
                    },
                ) {
                    isShowController = !isShowController
                    onListenerControllerVisible?.invoke(isShowController)
                    onClick?.invoke()
                }
        ) {
            // 顶部控制栏
            AnimatedVisibility(
                visible = isShowController,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isLandscape) 56.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClickBackButton) {
                        Icon(
                            painterResource(R.drawable.round_arrow_back_ios_new_24),
                            contentDescription = "返回"
                        )
                    }
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                }
            }

            // 中间区域
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (isLandscape) 56.dp else 4.dp),
            ) {
                if (isShowController) centerAreaBox()
            }

            // 底部控制栏
            AnimatedVisibility(
                visible = isShowController,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                if (isLandscape) {
                    // 横屏布局
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .padding(horizontal = 56.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatTime(currentTimePosition.toLong()),
                                fontSize = 12.sp
                            )
                            Text(" / ", fontSize = 12.sp)
                            Text(
                                text = formatTime(duration.toLong()),
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                        ) {
                            Slider(
                                value = currentTimePosition,
                                onValueChange = onSliderValueChange,
                                onValueChangeFinished = onSliderValueChangeFinished,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .weight(1f),
                                valueRange = 0f..duration,
                                thumb = {
                                    SliderDefaults.Thumb(
                                        interactionSource = remember { MutableInteractionSource() },
                                        thumbSize = DpSize(13.dp, 13.dp),
                                        modifier = Modifier.offset(y = 3.dp)
                                    )
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onClickPlayButton) {
                                Icon(
                                    painterResource(
                                        if (isPlaying) R.drawable.round_pause_24
                                        else R.drawable.round_play_arrow_24
                                    ),
                                    contentDescription = "播放暂停"
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onClickDanmuIcon) {
                                    Icon(
                                        painterResource(if (isShowBulletChat) R.drawable.danmu_open_v24 else R.drawable.danmu_close_v24),
                                        contentDescription = "弹幕开关",
                                        tint = Color.Unspecified
                                    )
                                }

                                IconButton(onClick = onClickFullScreen) {
                                    Icon(
                                        painterResource(R.drawable.baseline_fullscreen_exit_24),
                                        contentDescription = "全屏"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 竖屏布局
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onClickPlayButton) {
                            Icon(
                                painterResource(
                                    if (isPlaying) R.drawable.round_pause_24
                                    else R.drawable.round_play_arrow_24
                                ),
                                contentDescription = "播放暂停"
                            )
                        }

                        Text(
                            text = formatTime(currentTimePosition.toLong()),
                            fontSize = 12.sp
                        )

                        Slider(
                            value = currentTimePosition,
                            onValueChange = onSliderValueChange,
                            onValueChangeFinished = onSliderValueChangeFinished,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .weight(1f),
                            valueRange = 0f..duration,
                            thumb = {
                                SliderDefaults.Thumb(
                                    interactionSource = remember { MutableInteractionSource() },
                                    thumbSize = DpSize(13.dp, 13.dp),
                                    modifier = Modifier.offset(y = 3.dp)
                                )
                            }
                        )

                        Text(
                            text = formatTime(duration.toLong()),
                            fontSize = 12.sp
                        )

                        IconButton(onClick = onClickFullScreen) {
                            Icon(
                                painterResource(R.drawable.baseline_fullscreen_24),
                                contentDescription = "全屏"
                            )
                        }
                    }
                }
            }
        }
    }
}

// 时间格式化工具
private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
