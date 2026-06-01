package com.yslee.subwaywhen.feature.home

import com.yslee.subwaywhen.data.model.SaveStationGroup

data class HomeUiState(
    val mainTitle: String = "",
    val congestionEmoji: String = "🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥",
    val currentGroup: SaveStationGroup = SaveStationGroup.ONE,
    val cells: List<HomeCellData> = emptyList(),
    val isRefreshing: Boolean = false,
)

sealed interface HomeIntent {
    data object OnAppear : HomeIntent
    data object Refresh : HomeIntent
    data class GroupTap(val group: SaveStationGroup) : HomeIntent
    data class StationTap(val cell: HomeCellData) : HomeIntent
    data class ScheduleTap(val cell: HomeCellData) : HomeIntent
    data object CongestionTap : HomeIntent
    data object ReportTap : HomeIntent
    data object EditTap : HomeIntent
    data object EmptyAddTap : HomeIntent
}

sealed interface HomeEffect {
    data object NavigateToSearch : HomeEffect
    data class NavigateToDetail(val cell: HomeCellData) : HomeEffect
    data object NavigateToCongestion : HomeEffect
    data object NavigateToReport : HomeEffect
    data object NavigateToEdit : HomeEffect
}
