package com.yslee.subwaywhen.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.network.NetworkResult
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val totalLoadModel: TotalLoadModel,
    private val localDataRepository: LocalDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    /** in-flight 실시간 로드 취소용 */
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
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
                    loadGroupData()
                }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.OnAppear -> loadGroupData()
            HomeIntent.Refresh -> {
                _uiState.update { it.copy(isRefreshing = true) }
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
        }
    }

    private fun loadGroupData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentGroup = _uiState.value.currentGroup
            val stations = localDataRepository.saveStations.value
                .filter { it.group == currentGroup }

            // 즉시 로딩 카드 표시
            val loadingCells = stations.mapIndexed { index, station -> station.toLoadingCell(index) }
            _uiState.update { it.copy(cells = loadingCells, isRefreshing = false) }

            if (stations.isEmpty()) return@launch

            // 역별 병렬 실시간 요청 → 응답 순서대로 카드 교체
            totalLoadModel.arrivalDataLoad(stations).collect { indexedResult ->
                val index = indexedResult.index
                val station = stations.getOrNull(index) ?: return@collect

                val newCells = when (val result = indexedResult.value) {
                    is NetworkResult.Success -> result.data.toRealCells(index, station)
                    is NetworkResult.Failure -> listOf(
                        station.toLoadingCell(index).copy(
                            type = HomeCellType.Real,
                            stateMSG = "정보 없음",
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
                    if (it.stationIndex == cell.stationIndex && it.updnLine == cell.updnLine) {
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

            val scheduleCell = if (station.korailCode.isNotEmpty()) {
                when (val result = totalLoadModel.korailScheduleLoad(station, weekDay)) {
                    is NetworkResult.Success -> result.data.toScheduleCell(cell)
                    is NetworkResult.Failure -> cell // 실패 → 이전 Real 상태 복원
                }
            } else {
                when (val result = totalLoadModel.seoulScheduleLoad(station, weekDay)) {
                    is NetworkResult.Success -> result.data.toScheduleCell(cell)
                    is NetworkResult.Failure -> cell // 실패 → 이전 Real 상태 복원
                }
            }

            _uiState.update { state ->
                state.copy(cells = state.cells.map {
                    if (it.stationIndex == cell.stationIndex && it.updnLine == cell.updnLine) scheduleCell else it
                })
            }
        }
    }

    private fun emitEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}
