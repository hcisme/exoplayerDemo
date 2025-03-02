package com.chc.videoplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.chc.player.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("SourceLockedOrientationActivity")
@OptIn(UnstableApi::class)
@Composable
fun Player(
    mediaUri: String,
    title: String,
    autoPlay: Boolean = false,
    isShowBulletChat: Boolean = false,
    bulletChatList: List<Danmu> = listOf(),
    onClickBackButton: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val insetsController = remember {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_icon))
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var firstLoad = remember { true }
    var isLandScreen by remember { mutableStateOf(activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) }
    var bulletChatVisible by remember { mutableStateOf(isShowBulletChat) }
    var isSeeking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var currentTimePosition by remember { mutableFloatStateOf(0F) }
    var duration by remember { mutableFloatStateOf(0F) }
    var danmuMenuTrigger by remember { mutableLongStateOf(0) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }.join()
    }

    fun play() {
        isPlaying = true
        exoPlayer?.play()
    }

    fun pause() {
        isPlaying = false
        exoPlayer?.pause()
    }

    fun enterFullscreen() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        isLandScreen = true
    }

    fun exitFullscreen() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        isLandScreen = false
    }

    LaunchedEffect(exoPlayer, mediaUri) {
        if (exoPlayer == null) {
            return@LaunchedEffect
        }
        currentTimePosition = 0F
        duration = 0F

        val mediaItem = MediaItem.Builder()
            .setUri(mediaUri)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        val httpDataSource = DefaultHttpDataSource.Factory()
        val dataSourceFactory = ResolvingDataSource.Factory(httpDataSource) { dataSpec ->
            val link = dataSpec.uri.toString()
            if (link.endsWith(".ts")) {
                val fileName = link.substringAfterLast("/")
                dataSpec.withUri("$mediaUri/$fileName".toUri())
            } else {
                dataSpec
            }
        }

        val mediaSource = DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)

        exoPlayer!!.setMediaSource(mediaSource)
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = autoPlay
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                pause()
            }

            override fun onResume(owner: LifecycleOwner) {
                if (!firstLoad) {
                    play()
                } else {
                    firstLoad = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(exoPlayer, isSeeking) {
        if (exoPlayer == null) {
            return@LaunchedEffect
        }

        while (isActive) {
            if (!isSeeking && exoPlayer!!.isPlaying) {
                currentTimePosition = exoPlayer!!.currentPosition.toFloat()
            }
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .then(if (isLandScreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16 / 9f))
            .clipToBounds()
    ) {
        if (exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    val playerView = PlayerView(ctx).apply {
                        useController = false
                        player = exoPlayer
                    }

                    // ExoPlayer监听器
                    exoPlayer!!.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_BUFFERING -> {
                                    isLoading = true
                                }

                                Player.STATE_READY -> {
                                    // 更新总时长和SeekBar最大值
                                    if (exoPlayer!!.duration != C.TIME_UNSET) {
                                        duration = exoPlayer!!.duration.toFloat()
                                    }
                                    isLoading = false
                                }

                                Player.STATE_ENDED -> {
                                    isLoading = false
                                }

                                Player.STATE_IDLE -> {
                                    isLoading = false
                                }
                            }
                        }

                        override fun onIsLoadingChanged(isLoading: Boolean) {
                            // 更新缓冲进度
                            exoPlayer!!.bufferedPosition.toInt()
                        }
                    })

                    playerView
                },
                modifier = Modifier.fillMaxSize(),
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                }
            )
        }

        if (isLoading) {
            LottieAnimation(
                composition = composition,
                iterations = Int.MAX_VALUE,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
            )
        }

        // 播放控件
        PlayerController(
            isPlaying = isPlaying,
            isShowBulletChat = bulletChatVisible,
            title = title,
            isLandscape = isLandScreen,
            currentTimePosition = currentTimePosition,
            duration = duration,
            onClickBackButton = {
                if (isLandScreen) {
                    exitFullscreen()
                } else {
                    onClickBackButton()
                }
            },
            onClickPlayButton = {
                if (isPlaying) pause() else play()
                danmuMenuTrigger += 1
            },
            onSliderValueChange = {
                currentTimePosition = it
                if (!isSeeking) {
                    isSeeking = true
                }
            },
            onSliderValueChangeFinished = {
                exoPlayer?.seekTo(currentTimePosition.toLong())
                isSeeking = false
            },
            onClickFullScreen = {
                if (isLandScreen) {
                    exitFullscreen()
                } else {
                    enterFullscreen()
                }
            },
            onClickDanmuIcon = {
                bulletChatVisible = !bulletChatVisible
            },
            onClick = {
                danmuMenuTrigger += 1
            },
            onDoubleClick = {
                if (isPlaying) pause() else play()
                danmuMenuTrigger += 1
            },
            onLongClick = {
                play()
                exoPlayer?.setPlaybackSpeed(1.5F)
            },
            onLongClickEnd = {
                exoPlayer?.setPlaybackSpeed(1F)
            }
        )

        // 弹幕
        if (bulletChatVisible) {
            BulletChat(
                bulletChatList = bulletChatList,
                isPlaying = isPlaying,
                currentPosition = currentTimePosition.toLong(),
                trigger = danmuMenuTrigger,
            ) { offset, _ ->
                Box(
                    modifier = Modifier
                        .offset(offset = { offset })
                        .background(Color.Black.copy(alpha = 0.4F), CircleShape)
                        .clip(CircleShape)
                        .padding(horizontal = 6.dp)
                        .zIndex(1F)
                ) {
                    Text("上下文菜单", fontSize = 10.sp)
                }
            }
        }
    }

    BackHandler(isLandScreen) {
        exitFullscreen()
    }
}