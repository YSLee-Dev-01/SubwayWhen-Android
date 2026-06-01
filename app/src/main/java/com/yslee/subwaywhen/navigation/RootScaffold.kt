package com.yslee.subwaywhen.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yslee.subwaywhen.feature.home.HomeScreen
import com.yslee.subwaywhen.feature.search.SearchScreen
import com.yslee.subwaywhen.feature.setting.SettingScreen
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.TabIconUnselectedDark
import com.yslee.subwaywhen.ui.theme.TabIconUnselectedLight
import com.yslee.subwaywhen.ui.theme.TabIndicatorDark
import com.yslee.subwaywhen.ui.theme.TabIndicatorLight

private val TabBarShape = RoundedCornerShape(28.dp)
private val TabItemShape = RoundedCornerShape(20.dp)

@Composable
fun RootScaffold() {
    val childNavController = rememberNavController()
    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDark = isSystemInDarkTheme()
    val indicatorColor = if (isDark) TabIndicatorDark else TabIndicatorLight
    val unselectedIconColor = if (isDark) TabIconUnselectedDark else TabIconUnselectedLight

    var isTabBarVisible by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = childNavController,
            startDestination = TabRoute.Home.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(TabRoute.Home.route) {
                HomeScreen(
                    onNavigateToSearch = {
                        childNavController.navigate(TabRoute.Search.route) {
                            popUpTo(TabRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToDetail = { _ -> },
                    onCongestionTap = {},
                    onReportTap = {},
                    onEditTap = {},
                )
            }
            composable(TabRoute.Search.route) {
                SearchScreen(onTabBarVisibilityChange = { isTabBarVisible = it })
            }
            composable(TabRoute.Setting.route) { SettingScreen() }
        }

        AnimatedVisibility(
            visible = isTabBarVisible,
            enter = slideInVertically(tween(Dimens.animationDurationMs)) { it } + fadeIn(tween(Dimens.animationDurationMs)),
            exit = slideOutVertically(tween(Dimens.animationDurationMs)) { it } + fadeOut(tween(Dimens.animationDurationMs)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp)
                    .shadow(elevation = 8.dp, shape = TabBarShape)
                    .background(MaterialTheme.colorScheme.surface, TabBarShape)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabRoute.all.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    Box(
                        modifier = Modifier
                            .clip(TabItemShape)
                            .background(if (selected) indicatorColor else Color.Transparent)
                            .clickable {
                                childNavController.navigate(tab.route) {
                                    popUpTo(TabRoute.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(horizontal = 19.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = stringResource(tab.labelRes),
                            tint = if (selected) AppIconColor else unselectedIconColor,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "RootScaffold - Light", showBackground = true)
@Composable
private fun RootScaffoldLightPreview() {
    RootScaffold()
}

@Preview(name = "RootScaffold - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun RootScaffoldDarkPreview() {
    RootScaffold()
}
