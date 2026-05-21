package com.yslee.subwaywhen.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.yslee.subwaywhen.ui.theme.TabIconUnselectedDark
import com.yslee.subwaywhen.ui.theme.TabIconUnselectedLight
import com.yslee.subwaywhen.ui.theme.TabIndicatorDark
import com.yslee.subwaywhen.ui.theme.TabIndicatorLight

@Composable
fun RootScaffold() {
    val childNavController = rememberNavController()
    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDark = isSystemInDarkTheme()
    val indicatorColor = if (isDark) TabIndicatorDark else TabIndicatorLight
    val unselectedIconColor = if (isDark) TabIconUnselectedDark else TabIconUnselectedLight

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                TabRoute.all.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            childNavController.navigate(tab.route) {
                                popUpTo(TabRoute.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.labelRes),
                            )
                        },
                        label = null,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppIconColor,
                            unselectedIconColor = unselectedIconColor,
                            indicatorColor = indicatorColor,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = childNavController,
            startDestination = TabRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TabRoute.Home.route) { HomeScreen() }
            composable(TabRoute.Search.route) { SearchScreen() }
            composable(TabRoute.Setting.route) { SettingScreen() }
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
