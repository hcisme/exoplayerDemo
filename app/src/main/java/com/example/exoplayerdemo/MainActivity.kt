package com.example.exoplayerdemo

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import com.example.exoplayerdemo.player.PlayerNew
import com.example.exoplayerdemo.ui.theme.JetpackDemoTheme

class MainActivity : ComponentActivity() {
    private val url1 = "https://chcblogs.com/api/web/file/videoResource/ZbpMBzY1AECJnWvGkXZN"
    private val url2 = "https://chcblogs.com/api/web/file/videoResource/WFFLQ1r5QTmZzmhiAoNn"

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            JetpackDemoTheme(
                dynamicColor = false
            ) {
                var currentUrl by remember { mutableStateOf(url1) }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        PlayerNew(
                            mediaUri = currentUrl,
                            title = if (currentUrl == url1) "加勒比海盗 第一部 解说" else "肖申克的救赎 高清"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    currentUrl = if (currentUrl == url1) url2 else url1
                                }
                            ) {
                                Text("切换视频")
                            }
                        }
                    }

                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val insetsControllerCompat = WindowInsetsControllerCompat(window, window.decorView)
        val nightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (nightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                insetsControllerCompat.apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                insetsControllerCompat.apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }

            else -> {}
        }
    }
}
