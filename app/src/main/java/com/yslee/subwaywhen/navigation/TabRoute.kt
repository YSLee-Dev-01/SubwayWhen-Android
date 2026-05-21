package com.yslee.subwaywhen.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.yslee.subwaywhen.R

sealed class TabRoute(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Home : TabRoute("tab_home", R.string.tab_home, Icons.Filled.Home)
    object Search : TabRoute("tab_search", R.string.tab_search, Icons.Filled.Search)
    object Setting : TabRoute("tab_setting", R.string.tab_setting, Icons.Filled.Settings)

    companion object {
        val all = listOf(Home, Search, Setting)
    }
}
