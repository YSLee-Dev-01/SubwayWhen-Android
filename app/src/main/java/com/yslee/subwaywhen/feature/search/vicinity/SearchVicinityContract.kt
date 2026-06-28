package com.yslee.subwaywhen.feature.search.vicinity

import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.feature.detail.DetailSendModel

enum class VicinityAuthStatus {
    Unknown, Denied, Granted
}

data class LiveLoading(val up: Boolean, val down: Boolean)

data class VicinityUiState(
    val authStatus: VicinityAuthStatus = VicinityAuthStatus.Unknown,
    val isVicinityLoading: Boolean = false,
    val vicinityStations: List<VicinityTransformData> = emptyList(),
    val tappedIndex: Int? = null,
    val upLiveArrival: List<RealtimeStationArrival> = emptyList(),
    val downLiveArrival: List<RealtimeStationArrival> = emptyList(),
    val liveLoading: LiveLoading = LiveLoading(false, false),
    val lastSearchTime: Long? = null,
    val showRefreshCooldownDialog: Boolean = false,
    val refreshCooldownSec: Int = 0,
    val errorDialog: String? = null,
    val isLocationModalVisible: Boolean = false,
    val trainIcon: String = "🚃",
    val showDisposableDirectionDialog: Boolean = false,
    val disposableStationCode: String? = null,
)

sealed interface VicinityIntent {
    data object OnAppear : VicinityIntent
    data object AuthRequestTapped : VicinityIntent
    data class AuthResultReceived(val granted: Boolean) : VicinityIntent
    data object VicinityRefreshTapped : VicinityIntent
    data class StationTapped(val index: Int?) : VicinityIntent
    data object LiveRefreshTapped : VicinityIntent
    data object ListModalOpenTapped : VicinityIntent
    data object ListModalDismissed : VicinityIntent
    data class ListStationTapped(val index: Int) : VicinityIntent
    data object DialogDismissed : VicinityIntent
    data class AddStationTapped(val index: Int) : VicinityIntent
    data object DisposableDetailTapped : VicinityIntent
    data class DisposableDirectionSelected(val isUp: Boolean) : VicinityIntent
}

sealed interface VicinityEffect {
    data class SearchStation(val name: String) : VicinityEffect
    data object NoLiveDataError : VicinityEffect
    data class SearchStationOnly(val name: String) : VicinityEffect
    data class NavigateToDisposableDetail(val model: DetailSendModel) : VicinityEffect
}
