package com.yslee.subwaywhen.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yslee.subwaywhen.feature.splash.SplashEffect
import com.yslee.subwaywhen.feature.splash.SplashViewModel
import com.yslee.subwaywhen.feature.tutorial.TutorialScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier, splashViewModel: SplashViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavRoutes.Splash, modifier = modifier) {
        composable(NavRoutes.Splash) {
            val viewModel: SplashViewModel = splashViewModel
            LaunchedEffect(Unit) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        SplashEffect.NavigateToHome -> navController.navigate(NavRoutes.Root) {
                            popUpTo(NavRoutes.Splash) { inclusive = true }
                        }
                        SplashEffect.NavigateToTutorial -> navController.navigate(NavRoutes.Tutorial) {
                            popUpTo(NavRoutes.Splash) { inclusive = true }
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize())
        }
        composable(NavRoutes.Tutorial) {
            TutorialScreen(
                onNavigateToHome = {
                    navController.navigate(NavRoutes.Root) {
                        popUpTo(NavRoutes.Tutorial) { inclusive = true }
                    }
                },
            )
        }
        composable(NavRoutes.Root) {
            RootScaffold()
        }
    }
}
