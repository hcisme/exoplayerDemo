package com.example.jetpackdemo.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.airbnb.lottie.LottieAnimationView
import com.example.jetpackdemo.R
import kotlinx.coroutines.delay

@SuppressLint("SourceLockedOrientationActivity")
@OptIn(UnstableApi::class)
@Composable
fun Player(mediaUri: String, autoPlay: Boolean = false) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val insetsController =
        remember { WindowInsetsControllerCompat(activity.window, activity.window.decorView) }
    val exoPlayer = remember(mediaUri) {
        ExoPlayer.Builder(context).build().apply {
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
            val mediaSource =
                DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = autoPlay
        }
    }
    var isLandScreen by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isSeeking && exoPlayer.isPlaying) {
                // 更新 UI
                val seekBar: SeekBar = activity.findViewById(R.id.seekBar)
                val currentPosition: TextView = activity.findViewById(R.id.currentPosition)
                seekBar.progress = (exoPlayer.currentPosition * 100 / exoPlayer.duration).toInt()
                currentPosition.text = formatTime(exoPlayer.currentPosition)
            }
            delay(1000) // 每秒更新一次
        }
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                exoPlayer.pause()
            }

            override fun onResume(owner: LifecycleOwner) {
//                exoPlayer.playWhenReady = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { ctx ->
            val rootView = LayoutInflater.from(ctx).inflate(R.layout.exo_player, null, false)
            val playerView = rootView.findViewById<PlayerView>(R.id.playerView)
            playerView.player = exoPlayer

            // 绑定控制器功能
            val playButton = playerView.findViewById<ImageView>(R.id.playButton)
            playButton.setOnClickListener {
                Log.i("@@", "点击了")
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    playButton.setImageResource(R.drawable.round_play_arrow_24)
                } else {
                    exoPlayer.play()
                    playButton.setImageResource(R.drawable.round_pause_24)
                }
            }

            val fullscreenButton = playerView.findViewById<ImageView>(R.id.fullscreenButton)
            fullscreenButton.setOnClickListener {
                if (isLandScreen) {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    insetsController.show(WindowInsetsCompat.Type.statusBars())
                    fullscreenButton.setImageResource(R.drawable.baseline_fullscreen_24)
                } else {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    fullscreenButton.setImageResource(R.drawable.baseline_fullscreen_exit_24)
                }
                isLandScreen = !isLandScreen
            }

            val backButton = playerView.findViewById<ImageView>(R.id.backButton)
            backButton.setOnClickListener {}

            val seekBar = playerView.findViewById<SeekBar>(R.id.seekBar)
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) exoPlayer.seekTo((progress * exoPlayer.duration / 100))
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    isSeeking = false
                }
            })

            // 监听播放状态更新总时长
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    val loadingIcon = rootView.findViewById<LottieAnimationView>(R.id.loadingIcon)
                    val duration = playerView.findViewById<TextView>(R.id.duration)
                    when (state) {
                        Player.STATE_BUFFERING -> loadingIcon.visibility = View.VISIBLE
                        Player.STATE_ENDED, Player.STATE_IDLE -> loadingIcon.visibility = View.GONE
                        Player.STATE_READY -> {
                            duration.text = formatTime(exoPlayer.duration)
                            seekBar.max = 100
                            loadingIcon.visibility = View.GONE
                        }
                    }
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    seekBar.secondaryProgress =
                        (exoPlayer.bufferedPosition * 100 / exoPlayer.duration).toInt()
                }
            })
            rootView
        },
        modifier = Modifier
            .background(Color.Black)
            .then(if (isLandScreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16 / 9f)),
        update = { rootView ->
            val playerView: PlayerView = rootView.findViewById(R.id.playerView)
            playerView.player = exoPlayer
        }
    )
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
