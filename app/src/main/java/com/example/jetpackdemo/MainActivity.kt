package com.example.jetpackdemo

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import com.example.jetpackdemo.player.Player
import com.example.jetpackdemo.ui.theme.JetpackDemoTheme

class MainActivity : ComponentActivity() {
    private val url = "https://chcblogs.com/api/web/file/videoResource/2ulOGuLjR58NBF57Fj8U"

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
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    Player(mediaUri = url, title = "Stay MV 影视")
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
                insetsControllerCompat.isAppearanceLightStatusBars = false
                insetsControllerCompat.isAppearanceLightNavigationBars = false
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                insetsControllerCompat.isAppearanceLightStatusBars = true
                insetsControllerCompat.isAppearanceLightNavigationBars = true
            }

            else -> {}
        }
    }
}
