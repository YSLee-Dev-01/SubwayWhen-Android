package com.yslee.subwaywhen.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.local.SettingLocalDataSource
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
import com.yslee.subwaywhen.feature.detail.mapper.filterFromNow
import com.yslee.subwaywhen.feature.detail.mapper.isUnownedLine
import com.yslee.subwaywhen.feature.detail.mapper.toDetailArrivalItem
import com.yslee.subwaywhen.feature.detail.mapper.toDetailScheduleItem
import com.yslee.subwaywhen.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val totalLoadModel: TotalLoadModel,
    private val settingLocalDataSource: SettingLocalDataSource,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sendModel: DetailSendModel = run {
        val encoded = requireNotNull(savedStateHandle.get<String>(NavRoutes.ARG_DETAIL_MODEL))
        Json.decodeFromString(encoded)
    }

    private val _uiState = MutableStateFlow(DetailUiState(sendModel = sendModel))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<DetailEffect>()
    val effect: SharedFlow<DetailEffect> = _effect.asSharedFlow()

    private var timerJob: Job? = null
    private var cooldownJob: Job? = null

    init {
        viewModelScope.launch {
            settingLocalDataSource.getSaveSetting()
                .collect { setting ->
                    _uiState.update { it.copy(trainIcon = setting.detailVcTrainIcon) }
                }
        }

        // DetailResultSchedule에서 제외 행 설정 후 돌아올 때 반영
        viewModelScope.launch {
            savedStateHandle.getStateFlow("exceptionLastStation", "")
                .collect { exception ->
                    if (exception.isNotEmpty()) {
                        updateExceptionLastStation(exception)
                        savedStateHandle["exceptionLastStation"] = ""
                    }
                }
        }
    }

    fun onIntent(intent: DetailIntent) {
        when (intent) {
            DetailIntent.OnAppear -> {
                loadArrival()
                loadSchedule()
                startTimer()
            }
            DetailIntent.OnDisappear -> stopTimer()
            DetailIntent.Refresh -> handleRefresh()
            DetailIntent.ScheduleMoreTap -> {
                viewModelScope.launch {
                    _effect.emit(DetailEffect.NavigateToResultSchedule(_uiState.value.scheduleItems))
                }
            }
            DetailIntent.RealtimeTap -> { /* TODO: Realtime 화면 연결 */ }
            DetailIntent.ExceptionRowTap -> { /* 제외 행 설정은 DetailResultSchedule에서 처리 */ }
            DetailIntent.ReportTap -> { /* TODO: 민원 접수 연결 */ }
            DetailIntent.Back -> viewModelScope.launch { _effect.emit(DetailEffect.NavigateBack) }
        }
    }

    fun updateExceptionLastStation(exception: String) {
        val updated = sendModel.copy(exceptionLastStation = exception)
        _uiState.update { it.copy(sendModel = updated) }
        loadArrival(overrideModel = updated)
    }

    private fun loadArrival(overrideModel: DetailSendModel? = null) {
        val model = overrideModel ?: _uiState.value.sendModel
        viewModelScope.launch {
            _uiState.update { it.copy(isArrivalLoading = true, arrivalError = false) }
            val deferred = async { totalLoadModel.liveArrivalSplit(model.stationName, model.lineNumber) }
            delay(250L)
            val (upList, downList) = deferred.await()
            val isUp = model.upDown.contains("상행") || model.upDown.contains("내선")
            val arrivals = if (isUp) upList else downList

            val filtered = if (model.exceptionLastStation.isNotEmpty()) {
                arrivals.filter { it.lastStation != model.exceptionLastStation }
            } else arrivals

            val first = filtered.getOrNull(0)?.toDetailArrivalItem()
            val second = filtered.getOrNull(1)?.toDetailArrivalItem()
            val prevName = filtered.getOrNull(0)?.backStationName ?: ""
            val nextName = filtered.getOrNull(0)?.nextStationName ?: ""

            _uiState.update {
                it.copy(
                    firstArrival = first,
                    secondArrival = second,
                    prevStationName = prevName,
                    nextStationName = nextName,
                    isArrivalLoading = false,
                    arrivalError = first == null && second == null && arrivals.isEmpty(),
                )
            }
        }
    }

    private fun loadSchedule() {
        val model = _uiState.value.sendModel
        if (isUnownedLine(model.lineNumber)) {
            _uiState.update { it.copy(isScheduleLoading = false, isUnowned = true) }
            return
        }

        val weekDay = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "saturday"
            Calendar.SUNDAY -> "sunday"
            else -> "weekday"
        }
        val station = model.toSaveStation()

        viewModelScope.launch {
            _uiState.update { it.copy(isScheduleLoading = true, scheduleError = false) }
            val deferred = async {
                when {
                    model.lineNumber == "신분당선" -> {
                        when (val result = totalLoadModel.shinbundangScheduleLoad(station, weekDay, isDisposable = false)) {
                            is NetworkResult.Success -> result.data.map { it.toDetailScheduleItem() }
                            is NetworkResult.Failure -> null
                        }
                    }
                    model.korailCode.isNotEmpty() -> {
                        when (val result = totalLoadModel.korailScheduleLoad(station, weekDay)) {
                            is NetworkResult.Success -> result.data.map { it.toDetailScheduleItem() }
                            is NetworkResult.Failure -> null
                        }
                    }
                    else -> {
                        when (val result = totalLoadModel.seoulScheduleLoad(station, weekDay)) {
                            is NetworkResult.Success -> result.data.SearchSTNTimeTableByFRCodeService.row.map { it.toDetailScheduleItem() }
                            is NetworkResult.Failure -> null
                        }
                    }
                }
            }
            delay(250L)
            val items = deferred.await()

            if (items == null) {
                _uiState.update { it.copy(isScheduleLoading = false, scheduleError = true) }
            } else {
                _uiState.update { it.copy(isScheduleLoading = false, scheduleItems = items.filterFromNow()) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value.timerCount
                if (current <= 1) {
                    _uiState.update { it.copy(timerCount = 15) }
                    loadArrival()
                    sortSchedule()
                } else {
                    _uiState.update { it.copy(timerCount = current - 1) }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun handleRefresh() {
        if (_uiState.value.isRefreshCooldown) return
        _uiState.update { it.copy(timerCount = 15, isRefreshCooldown = true) }
        loadArrival()
        sortSchedule()
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            delay(1200)
            _uiState.update { it.copy(isRefreshCooldown = false) }
        }
    }

    private fun sortSchedule() {
        _uiState.update { it.copy(scheduleItems = it.scheduleItems.filterFromNow()) }
    }

    private fun DetailSendModel.toSaveStation() = SaveStation(
        stationName = stationName,
        stationCode = stationCode,
        updnLine = upDown,
        line = lineNumber,
        lineCode = lineCode,
        exceptionLastStation = exceptionLastStation,
        korailCode = korailCode,
    )
}
