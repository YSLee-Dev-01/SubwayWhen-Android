package com.yslee.subwaywhen.feature.search.modal

import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo

data class SaveStationModalUiState(
    val station: SearchStationInfo? = null,
    val group: SaveStationGroup = SaveStationGroup.ONE,
    val exceptionLastStation: String = "",
)

sealed interface SaveStationModalIntent {
    data class InitStation(val station: SearchStationInfo) : SaveStationModalIntent
    data object GroupToggled : SaveStationModalIntent
    data class ExceptionChanged(val text: String) : SaveStationModalIntent
    data object UpButtonTapped : SaveStationModalIntent
    data object DownButtonTapped : SaveStationModalIntent
    data object NotServiceTapped : SaveStationModalIntent
    data object DisposableUpTapped : SaveStationModalIntent
    data object DisposableDownTapped : SaveStationModalIntent
    data object Dismissed : SaveStationModalIntent
}

sealed interface SaveStationModalEffect {
    data object SaveCompleted : SaveStationModalEffect
    data object AlreadyExists : SaveStationModalEffect
    data object Close : SaveStationModalEffect
    data class DisposableDetailNavigate(
        val station: SearchStationInfo,
        val isUp: Boolean,
    ) : SaveStationModalEffect
}
