package com.yslee.subwaywhen.feature.home.modal

data class CongestionUiState(
    val selectedStation: String = "",
    val availableStations: List<String> = emptyList(),
    val congestionData: List<HourlyCongestion> = emptyList(),
    val nowHour: Int = 0,
)

sealed interface CongestionIntent {
    data object OnAppear : CongestionIntent
    data class StationTap(val station: String) : CongestionIntent
}
