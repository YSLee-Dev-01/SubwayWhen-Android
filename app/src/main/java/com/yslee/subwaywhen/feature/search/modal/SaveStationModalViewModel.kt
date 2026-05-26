package com.yslee.subwaywhen.feature.search.modal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import com.yslee.subwaywhen.ui.common.subwayLineCode
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.common.subwayLineUpDownText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SaveStationModalViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaveStationModalUiState())
    val uiState: StateFlow<SaveStationModalUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SaveStationModalEffect>()
    val effect: SharedFlow<SaveStationModalEffect> = _effect.asSharedFlow()

    fun initStation(station: SearchStationInfo) {
        _uiState.update { it.copy(station = station) }
    }

    fun onIntent(intent: SaveStationModalIntent) {
        when (intent) {
            is SaveStationModalIntent.GroupToggled -> {
                _uiState.update {
                    it.copy(
                        group = if (it.group == SaveStationGroup.ONE) SaveStationGroup.TWO
                        else SaveStationGroup.ONE
                    )
                }
            }

            is SaveStationModalIntent.ExceptionChanged -> {
                _uiState.update { it.copy(exceptionLastStation = intent.text) }
            }

            is SaveStationModalIntent.UpButtonTapped -> save(isUp = true)

            is SaveStationModalIntent.DownButtonTapped -> save(isUp = false)

            is SaveStationModalIntent.NotServiceTapped -> {
                viewModelScope.launch { _effect.emit(SaveStationModalEffect.Close) }
            }

            is SaveStationModalIntent.DisposableUpTapped -> {
                val station = _uiState.value.station ?: return
                viewModelScope.launch {
                    // TODO: Detail 연동은 다음 spec에서 구현
                    _effect.emit(SaveStationModalEffect.DisposableDetailNavigate(station, isUp = true))
                }
            }

            is SaveStationModalIntent.DisposableDownTapped -> {
                val station = _uiState.value.station ?: return
                viewModelScope.launch {
                    // TODO: Detail 연동은 다음 spec에서 구현
                    _effect.emit(SaveStationModalEffect.DisposableDetailNavigate(station, isUp = false))
                }
            }

            is SaveStationModalIntent.Dismissed -> {
                viewModelScope.launch { _effect.emit(SaveStationModalEffect.Close) }
            }
        }
    }

    private fun save(isUp: Boolean) {
        val state = _uiState.value
        val station = state.station ?: return

        viewModelScope.launch {
            val displayLine = subwayLineDisplayName(station.line)
            val updnLine = subwayLineUpDownText(station.line, isUp)
            val korailCode = lineToKorailCode(displayLine)

            val lineCode = subwayLineCode(station.line)

            if (localDataRepository.saveSetting.value.searchOverlapAlert) {
                val isDuplicate = localDataRepository.saveStations.value.any {
                    it.stationName == station.stationName &&
                        it.updnLine == updnLine &&
                        it.lineCode == lineCode
                }
                if (isDuplicate) {
                    _effect.emit(SaveStationModalEffect.AlreadyExists)
                    return@launch
                }
            }

            val newStation = SaveStation(
                id = UUID.randomUUID().toString(),
                stationName = station.stationName,
                stationCode = station.stationCode,
                updnLine = updnLine,
                line = station.line,
                lineCode = lineCode,
                group = state.group,
                exceptionLastStation = state.exceptionLastStation,
                korailCode = korailCode,
            )
            localDataRepository.updateSaveStations(
                localDataRepository.saveStations.value + newStation
            )
            analytics.logEvent("SerachVC_Modal_Save") {
                param("Save_Station", station.stationName)
            }
            _effect.emit(SaveStationModalEffect.SaveCompleted)
        }
    }
}
