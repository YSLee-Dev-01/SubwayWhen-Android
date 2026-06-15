package com.yslee.subwaywhen.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.congestion.CongestionManager
import com.yslee.subwaywhen.data.remote.firebase.FirebaseDataSource
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import com.yslee.subwaywhen.feature.home.mapper.timeGroup
import com.yslee.subwaywhen.feature.home.mapper.toLoadingCell
import com.yslee.subwaywhen.feature.home.mapper.toRealCells
import com.yslee.subwaywhen.feature.home.mapper.toScheduleCell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val totalLoadModel: TotalLoadModel,
    private val localDataRepository: LocalDataRepository,
    private val congestionManager: CongestionManager,
    private val firebaseDataSource: FirebaseDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    /** in-flight 실시간 로드 취소용 */
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            val data = firebaseDataSource.getImportantData()
            if (data != null) {
                _uiState.update { it.copy(importantData = data) }
            }
        }
        viewModelScope.launch {
            var prevStations: List<SaveStation>? = null
            var prevOneTime: Int? = null
            var prevTwoTime: Int? = null
            combine(
                localDataRepository.saveStations,
                localDataRepository.saveSetting,
            ) { stations, setting -> Pair(stations, setting) }
                .collect { (stations, setting) ->
                    val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val autoGroup = timeGroup(
                        oneTime = setting.mainGroupOneTime,
                        twoTime = setting.mainGroupTwoTime,
                        nowHour = nowHour,
                    )
                    if (autoGroup != null) {
                        _uiState.update { it.copy(currentGroup = autoGroup) }
                    }
                    val stationsChanged = stations != prevStations
                    val groupTimeChanged = setting.mainGroupOneTime != prevOneTime ||
                        setting.mainGroupTwoTime != prevTwoTime
                    if (stationsChanged || groupTimeChanged) {
                        loadGroupData(stations)
                    }
                    prevStations = stations
                    prevOneTime = setting.mainGroupOneTime
                    prevTwoTime = setting.mainGroupTwoTime
                }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.OnAppear -> loadGroupData()
            HomeIntent.Refresh -> {
                _uiState.update { it.copy(isRefreshing = true, mainTitleVersion = it.mainTitleVersion + 1) }
                loadGroupData()
            }
            is HomeIntent.GroupTap -> {
                _uiState.update { it.copy(currentGroup = intent.group) }
                loadGroupData()
            }
            is HomeIntent.StationTap -> emitEffect(HomeEffect.NavigateToDetail(intent.cell))
            is HomeIntent.ScheduleTap -> handleScheduleTap(intent.cell)
            HomeIntent.CongestionTap -> emitEffect(HomeEffect.NavigateToCongestion)
            HomeIntent.ReportTap -> emitEffect(HomeEffect.NavigateToReport)
            HomeIntent.EditTap -> emitEffect(HomeEffect.NavigateToEdit)
            HomeIntent.EmptyAddTap -> emitEffect(HomeEffect.NavigateToSearch)
            HomeIntent.ImportantTap -> {
                val data = _uiState.value.importantData ?: return
                emitEffect(HomeEffect.ShowImportantDetail(data.first, data.second))
            }
        }
    }

    private fun loadGroupData(allStations: List<SaveStation>? = null) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentGroup = _uiState.value.currentGroup
            val stations = (allStations ?: localDataRepository.saveStations.value)
                .filter { it.group == currentGroup }

            // 즉시 로딩 카드 표시
            val loadingCells = stations.mapIndexed { index, station -> station.toLoadingCell(index) }
            _uiState.update { it.copy(cells = loadingCells, isRefreshing = false) }

            if (stations.isEmpty()) return@launch

            // 혼잡도 로드 (실시간 요청과 병렬)
            launch {
                val saveSetting = localDataRepository.saveSetting.first()
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val level = congestionManager.getLevel(
                    station = saveSetting.mainCongestionBaseStation,
                    hour = hour,
                ) ?: 0
                _uiState.update { it.copy(congestionEmoji = buildCongestionEmoji(level, saveSetting.mainCongestionLabel)) }
            }

            // 역별 병렬 실시간 요청 → 응답 순서대로 카드 교체
            totalLoadModel.arrivalDataLoad(stations).collect { indexedResult ->
                val index = indexedResult.index
                val station = stations.getOrNull(index) ?: return@collect

                val newCells = when (val result = indexedResult.value) {
                    is NetworkResult.Success -> result.data.toRealCells(index, station)
                    is NetworkResult.Failure -> listOf(
                        station.toLoadingCell(index).copy(
                            type = HomeCellType.Real,
                            stateMSG = "현재 실시간 열차 데이터가 없어요.",
                        )
                    )
                }

                _uiState.update { state ->
                    val updated = state.cells.toMutableList()
                    // 해당 stationIndex를 가진 카드들을 newCells로 교체
                    val firstIdx = updated.indexOfFirst { it.stationIndex == index }
                    if (firstIdx >= 0) {
                        updated.removeAll { it.stationIndex == index }
                        updated.addAll(firstIdx, newCells)
                    } else {
                        updated.addAll(newCells)
                    }
                    state.copy(cells = updated)
                }
            }
        }
    }

    private fun handleScheduleTap(cell: HomeCellData) {
        viewModelScope.launch {
            if (cell.type != HomeCellType.Real) return@launch

            // 해당 카드를 로딩 상태로 전환
            _uiState.update { state ->
                state.copy(cells = state.cells.map {
                    if (it.stationIndex == cell.stationIndex && it.updnLine == cell.updnLine && it.subIndex == cell.subIndex) {
                        it.copy(type = HomeCellType.Loading, stateMSG = "시간표 로드 중")
                    } else it
                })
            }

            val currentGroup = _uiState.value.currentGroup
            val station = localDataRepository.saveStations.value
                .filter { it.group == currentGroup }
                .getOrNull(cell.stationIndex) ?: return@launch

            val weekDay = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> "saturday"
                Calendar.SUNDAY -> "sunday"
                else -> "weekday"
            }

            val scheduleCell = if (station.line == "신분당선") {
                when (val result = totalLoadModel.shinbundangScheduleLoad(station, weekDay, isDisposable = false)) {
                    is NetworkResult.Success -> result.data.toScheduleCell(cell)
                    is NetworkResult.Failure -> cell.copy(
                        type = HomeCellType.Schedule,
                        stateMSG = "정보없음",
                        subPrevious = "정보없음",
                    )
                }
            } else if (station.korailCode.isNotEmpty()) {
                when (val result = totalLoadModel.korailScheduleLoad(station, weekDay)) {
                    is NetworkResult.Success -> result.data.toScheduleCell(cell)
                    is NetworkResult.Failure -> cell.copy(
                        type = HomeCellType.Schedule,
                        stateMSG = "정보없음",
                        subPrevious = "정보없음",
                    )
                }
            } else {
                when (val result = totalLoadModel.seoulScheduleLoad(station, weekDay)) {
                    is NetworkResult.Success -> result.data.toScheduleCell(cell)
                    is NetworkResult.Failure -> cell.copy(
                        type = HomeCellType.Schedule,
                        stateMSG = "정보없음",
                        subPrevious = "정보없음",
                    )
                }
            }

            _uiState.update { state ->
                state.copy(cells = state.cells.map {
                    if (it.stationIndex == cell.stationIndex && it.updnLine == cell.updnLine && it.subIndex == cell.subIndex) scheduleCell else it
                })
            }
        }
    }

    private fun buildCongestionEmoji(level: Int, label: String): String {
        if (level == 0) return "🫥".repeat(10)
        val emoji = label.ifEmpty { "☹️" }
        return emoji.repeat(level) + "🫥".repeat(maxOf(0, 10 - level))
    }

    private fun emitEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}
