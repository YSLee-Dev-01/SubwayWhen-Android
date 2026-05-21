package com.yslee.subwaywhen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yslee.subwaywhen.feature.splash.SplashViewModel
import com.yslee.subwaywhen.navigation.AppNavHost
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { !splashViewModel.isReady.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubwayWhenTheme {
                AppNavHost(splashViewModel = splashViewModel)
            }
        }
    }
}
