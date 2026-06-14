package com.yslee.subwaywhen.feature.home.modal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.remote.congestion.CongestionManager
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CongestionViewModel @Inject constructor(
    private val congestionManager: CongestionManager,
    private val localDataRepository: LocalDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CongestionUiState())
    val uiState: StateFlow<CongestionUiState> = _uiState.asStateFlow()

    fun onIntent(intent: CongestionIntent) {
        when (intent) {
            is CongestionIntent.OnAppear -> onAppear()
            is CongestionIntent.StationTap -> onStationTap(intent.station)
        }
    }

    private fun onAppear() {
        viewModelScope.launch {
            val availableStations = congestionManager.getAvailableStations()
            val selectedStation = localDataRepository.saveSetting.first().mainCongestionBaseStation
            val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val congestionData = congestionManager.getCongestions(selectedStation)

            _uiState.value = CongestionUiState(
                selectedStation = selectedStation,
                availableStations = availableStations,
                congestionData = congestionData,
                nowHour = nowHour,
            )
        }
    }

    private fun onStationTap(station: String) {
        if (_uiState.value.selectedStation == station) return
        viewModelScope.launch {
            val congestionData = congestionManager.getCongestions(station)
            _uiState.value = _uiState.value.copy(
                selectedStation = station,
                congestionData = congestionData,
            )
            val current = localDataRepository.saveSetting.first()
            localDataRepository.updateSaveSetting(current.copy(mainCongestionBaseStation = station))
        }
    }
}
