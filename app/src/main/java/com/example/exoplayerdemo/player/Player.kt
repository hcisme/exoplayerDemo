package com.example.exoplayerdemo.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.airbnb.lottie.LottieAnimationView
import com.example.exoplayerdemo.R
import com.example.exoplayerdemo.view.DanmuView
import kotlinx.coroutines.delay


@SuppressLint("SourceLockedOrientationActivity")
@OptIn(UnstableApi::class)
@Composable
fun Player(mediaUri: String, title: String, autoPlay: Boolean = false) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val insetsController =
        remember { WindowInsetsControllerCompat(activity.window, activity.window.decorView) }
    var firstLoad = remember { true }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isLandScreen by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    val danmuMutableList = remember { mutableStateListOf<Danmu>() }
    var currentPlayPosition by remember { mutableLongStateOf(0L) }

    fun play() {
        val playButton = activity.findViewById<ImageView>(R.id.playButton)
        exoPlayer.play()
        activity.findViewById<DanmuView>(R.id.danmuView)?.resumeDanmu()
        playButton.setImageResource(R.drawable.round_pause_24)
    }

    fun pause() {
        val playButton = activity.findViewById<ImageView>(R.id.playButton)
        exoPlayer.pause()
        activity.findViewById<DanmuView>(R.id.danmuView)?.pauseDanmu()
        playButton.setImageResource(R.drawable.round_play_arrow_24)
    }

    fun enterFullscreen() {
        val fullscreenButton = activity.findViewById<ImageView>(R.id.fullscreenButton)
        val exoTitle = activity.findViewById<TextView>(R.id.exo_title)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        fullscreenButton.setImageResource(R.drawable.baseline_fullscreen_exit_24)
        exoTitle.visibility = View.VISIBLE
        isLandScreen = true
    }

    fun exitFullscreen() {
        val fullscreenButton = activity.findViewById<ImageView>(R.id.fullscreenButton)
        val exoTitle = activity.findViewById<TextView>(R.id.exo_title)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        fullscreenButton.setImageResource(R.drawable.baseline_fullscreen_24)
        exoTitle.visibility = View.GONE
        isLandScreen = false
    }

    LaunchedEffect(mediaUri) {
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

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = autoPlay
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isSeeking && exoPlayer.isPlaying) {
                // 更新 UI
                val seekBar = activity.findViewById<SeekBar>(R.id.seekBar)
                val currentPosition = activity.findViewById<TextView>(R.id.currentPosition)
                seekBar.progress = exoPlayer.currentPosition.toInt()
                currentPosition.text = formatTime(exoPlayer.currentPosition)
            }
            delay(1000) // 每秒更新一次
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

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        danmuMutableList.addAll(danmuList)

        while (true) {
            if (exoPlayer.isPlaying) {
                val currentPos = exoPlayer.currentPosition
                currentPlayPosition = currentPos
            }
            delay(1000)
        }
    }

    // 处理弹幕显示
    LaunchedEffect(currentPlayPosition) {
        val currentTimeSeconds = currentPlayPosition / 1000
        val showDanmus = danmuMutableList
            .filter { it.time == currentTimeSeconds.toInt() }
            .take(3)
        showDanmus.forEach { danmu ->
            activity.findViewById<DanmuView>(R.id.danmuView)
                ?.addDanmu(danmu, currentTimeSeconds.toInt())
//            danmuMutableList.remove(danmu)
        }
    }

    AndroidView(
        factory = { ctx ->
            val inflater = LayoutInflater.from(ctx)
            val rootView = inflater.inflate(R.layout.exo_player, null, false)
            val controlView = rootView.findViewById<PlayerControlView>(R.id.playerControlView)
            val playerView = rootView.findViewById<PlayerView>(R.id.playerView).apply {
                controlView.player = exoPlayer
                player = exoPlayer
            }

            with(rootView) {
                val exoTitle = findViewById<TextView>(R.id.exo_title)
                val playButton = findViewById<ImageView>(R.id.playButton)
                val fullscreenButton = findViewById<ImageView>(R.id.fullscreenButton)
                val backButton = findViewById<ImageView>(R.id.backButton)
                val seekBar = findViewById<SeekBar>(R.id.seekBar)
                val currentPosition = findViewById<TextView>(R.id.currentPosition)
                val duration = findViewById<TextView>(R.id.duration)
                val loadingIcon = findViewById<LottieAnimationView>(R.id.loadingIcon)

                exoTitle.text = title

                playerView.setOnClickListener {
                    controlView.apply {
                        if (isFullyVisible) {
                            hide()
                        } else {
                            show()
                        }
                    }
                }

                // 播放按钮点击事件
                playButton.setOnClickListener {
                    if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.currentPosition < exoPlayer.duration) {
                        if (exoPlayer.isPlaying) {
                            pause()
                        } else {
                            play()
                        }
                    }
                }

                // 全屏按钮点击事件
                fullscreenButton.setOnClickListener {
                    if (isLandScreen) {
                        exitFullscreen()
                    } else {
                        enterFullscreen()
                    }
                }

                // 返回按钮点击事件
                backButton.setOnClickListener {
                    if (isLandScreen) {
                        exitFullscreen()
                    }
                }

                // SeekBar事件监听
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            currentPosition.text = formatTime(progress.toLong())
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                        controlView.showTimeoutMs = 0
                        isSeeking = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        exoPlayer.seekTo(seekBar.progress.toLong())
                        isSeeking = false
                        controlView.showTimeoutMs = PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
                    }
                })

                // ExoPlayer监听器
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> loadingIcon.visibility = View.VISIBLE

                            Player.STATE_READY -> {
                                // 更新总时长和SeekBar最大值
                                if (exoPlayer.duration != C.TIME_UNSET) {
                                    duration.text = formatTime(exoPlayer.duration)
                                    seekBar.max = exoPlayer.duration.toInt()
                                }
                                loadingIcon.visibility = View.GONE
                            }

                            Player.STATE_ENDED -> {
                                loadingIcon.visibility = View.GONE
                                pause()
                            }

                            Player.STATE_IDLE -> loadingIcon.visibility = View.GONE
                        }
                    }

                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        // 更新缓冲进度
                        seekBar.secondaryProgress = exoPlayer.bufferedPosition.toInt()
                    }
                })
            }

            rootView
        },
        modifier = Modifier
            .background(Color.Black)
            .then(if (isLandScreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16 / 9f)),
        update = { rootView ->
            val playerView = rootView.findViewById<PlayerView>(R.id.playerView)
            if (playerView.player != exoPlayer) {
                playerView.player = exoPlayer
            }
        },
    )

    BackHandler(isLandScreen) {
        exitFullscreen()
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
