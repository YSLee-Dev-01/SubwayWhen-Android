package com.yslee.subwaywhen.feature.search.vicinity

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.yslee.subwaywhen.core.location.LocationManager
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.repository.VicinityRepository
import com.yslee.subwaywhen.ui.common.subwayLineIsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchVicinityViewModel @Inject constructor(
    private val locationManager: LocationManager,
    private val vicinityRepository: VicinityRepository,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    internal var nowMillis: () -> Long = { System.currentTimeMillis() }

    // 검색 탭 최초 진입 시에만 초기 로드 수행 — LaunchedEffect 재실행 방지
    private var hasInitialized = false

    private val _uiState = MutableStateFlow(VicinityUiState())
    val uiState: StateFlow<VicinityUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<VicinityEffect>(replay = 0)
    val effect: SharedFlow<VicinityEffect> = _effect.asSharedFlow()

    fun onIntent(intent: VicinityIntent) {
        when (intent) {
            is VicinityIntent.OnAppear -> {
                // 이미 초기화된 경우 skip (검색 모드 진입/복귀 시 재로드 방지)
                if (hasInitialized) return
                hasInitialized = true
                viewModelScope.launch {
                    // 에뮬레이터는 권한 체크 없이 임시 역 바로 표시
                    if (isEmulator()) {
                        _uiState.update { it.copy(authStatus = VicinityAuthStatus.Granted) }
                        loadVicinityStations()
                        return@launch
                    }
                    val granted = locationManager.locationAuthCheck()
                    if (granted) {
                        _uiState.update { it.copy(authStatus = VicinityAuthStatus.Granted) }
                        loadVicinityStations()
                    }
                    // 권한 없으면 authStatus = Unknown 유지
                }
            }

            is VicinityIntent.AuthRequestTapped -> {
                _uiState.update { it.copy(authStatus = VicinityAuthStatus.Unknown) }
            }

            is VicinityIntent.AuthResultReceived -> {
                if (intent.granted) {
                    _uiState.update { it.copy(authStatus = VicinityAuthStatus.Granted) }
                    loadVicinityStations()
                } else {
                    _uiState.update {
                        it.copy(
                            authStatus = VicinityAuthStatus.Denied,
                            isLocationModalVisible = true
                        )
                    }
                }
            }

            is VicinityIntent.VicinityRefreshTapped -> {
                val lastSearchTime = _uiState.value.lastSearchTime
                val now = nowMillis()
                if (lastSearchTime == null || now - lastSearchTime >= 5 * 60 * 1000L) {
                    loadVicinityStations()
                } else {
                    val remainingSec = (5 * 60 - (now - lastSearchTime) / 1000).toInt()
                    _uiState.update { it.copy(showRefreshCooldownDialog = true, refreshCooldownSec = remainingSec) }
                }
            }

            is VicinityIntent.StationTapped -> {
                if (intent.index == null) {
                    _uiState.update {
                        it.copy(
                            tappedIndex = null,
                            upLiveArrival = emptyList(),
                            downLiveArrival = emptyList(),
                            liveLoading = Pair(false, false)
                        )
                    }
                } else {
                    val station = _uiState.value.vicinityStations.getOrNull(intent.index) ?: return
                    if (!subwayLineIsService(station.lineColorName)) {
                        _uiState.update { it.copy(errorDialog = "서비스 중인 노선이 아닙니다.") }
                    } else {
                        _uiState.update { it.copy(tappedIndex = intent.index) }
                        loadLiveArrival(station)
                    }
                }
            }

            is VicinityIntent.LiveRefreshTapped -> {
                val index = _uiState.value.tappedIndex ?: return
                val station = _uiState.value.vicinityStations.getOrNull(index) ?: return
                loadLiveArrival(station)
            }

            is VicinityIntent.ListModalOpenTapped -> {
                _uiState.update { it.copy(isLocationModalVisible = true) }
            }

            is VicinityIntent.ListModalDismissed -> {
                _uiState.update { it.copy(isLocationModalVisible = false) }
            }

            is VicinityIntent.ListStationTapped -> {
                val name = _uiState.value.vicinityStations.getOrNull(intent.index)?.name ?: return
                viewModelScope.launch {
                    _effect.emit(VicinityEffect.SearchStation(name))
                }
                _uiState.update { it.copy(isLocationModalVisible = false) }
            }

            is VicinityIntent.DialogDismissed -> {
                _uiState.update { it.copy(showRefreshCooldownDialog = false, errorDialog = null) }
            }
        }
    }

    private fun loadVicinityStations() = viewModelScope.launch {
        _uiState.update { it.copy(isVicinityLoading = true) }

        // 에뮬레이터: GPS/API 불필요, 임시 역 데이터 즉시 표시
        if (isEmulator()) {
            _uiState.update {
                it.copy(
                    isVicinityLoading = false,
                    vicinityStations = EMULATOR_PREVIEW_STATIONS,
                    lastSearchTime = nowMillis()
                )
            }
            return@launch
        }

        val location = locationManager.locationRequest()
        if (location == null) {
            _uiState.update { it.copy(isVicinityLoading = false, vicinityStations = emptyList()) }
            return@launch
        }
        val stations = vicinityRepository.loadVicinityStations(location.lon, location.lat)
        _uiState.update {
            it.copy(
                isVicinityLoading = false,
                vicinityStations = stations,
                lastSearchTime = nowMillis()
            )
        }
    }

    private fun isEmulator(): Boolean =
        Build.HARDWARE == "ranchu" ||                               // 모든 현대 AVD (x86/ARM64, 구글플레이 포함)
            Build.HARDWARE == "goldfish" ||                         // 구형 AVD
            Build.FINGERPRINT.orEmpty().startsWith("generic") ||
            Build.FINGERPRINT.orEmpty().startsWith("unknown") ||
            Build.MODEL.orEmpty().contains("sdk_gphone") ||         // 구글플레이 이미지 (e.g. sdk_gphone64_arm64)
            Build.MODEL.orEmpty().contains("google_sdk") ||
            Build.MODEL.orEmpty().contains("Emulator") ||
            Build.MODEL.orEmpty().contains("Android SDK") ||
            Build.MANUFACTURER.orEmpty().contains("Genymotion") ||
            (Build.BRAND.orEmpty().startsWith("generic") && Build.DEVICE.orEmpty().startsWith("generic"))

    companion object {
        private val EMULATOR_PREVIEW_STATIONS = listOf(
            VicinityTransformData(id = "preview_1", name = "강남",      line = "2호선", distance = "0.3km"),
            VicinityTransformData(id = "preview_2", name = "역삼",      line = "2호선", distance = "0.8km"),
            VicinityTransformData(id = "preview_3", name = "교대",      line = "2호선", distance = "1.2km"),
            VicinityTransformData(id = "preview_4", name = "한성대입구", line = "4호선", distance = "1.5km"),
            VicinityTransformData(id = "preview_5", name = "신림",      line = "2호선", distance = "1.8km"),
            VicinityTransformData(id = "preview_6", name = "주안",      line = "1호선", distance = "2.1km"),
            VicinityTransformData(id = "preview_7", name = "혜화",      line = "4호선", distance = "2.4km"),
            VicinityTransformData(id = "preview_8", name = "낙성대",    line = "2호선", distance = "2.7km"),
            VicinityTransformData(id = "preview_9", name = "서초",      line = "2호선", distance = "3.0km"),
        )
    }

    private fun loadLiveArrival(station: VicinityTransformData) = viewModelScope.launch {
        _uiState.update { it.copy(liveLoading = Pair(true, true)) }
        val arrivals = vicinityRepository.loadLiveArrival(station.name)
        val isNinthLine = station.line.contains("9호선")

        val upArrivals = arrivals.filter { arrival ->
            if (isNinthLine) arrival.upDown == "하행" else arrival.upDown == "상행"
        }
        val downArrivals = arrivals.filter { arrival ->
            if (isNinthLine) arrival.upDown == "상행" else arrival.upDown == "하행"
        }

        _uiState.update {
            it.copy(
                upLiveArrival = upArrivals,
                downLiveArrival = downArrivals,
                liveLoading = Pair(false, false)
            )
        }
    }
}
