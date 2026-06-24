package com.yslee.subwaywhen.feature.detail.resultschedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.feature.detail.DetailScheduleItem
import com.yslee.subwaywhen.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
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
class DetailResultScheduleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sendModel: DetailResultScheduleSendModel = run {
        val encoded = requireNotNull(savedStateHandle.get<String>(NavRoutes.ARG_RESULT_SCHEDULE_MODEL))
        Json.decodeFromString(encoded)
    }

    private val _uiState = MutableStateFlow(DetailResultScheduleUiState())
    val uiState: StateFlow<DetailResultScheduleUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<DetailResultScheduleEffect>()
    val effect: SharedFlow<DetailResultScheduleEffect> = _effect.asSharedFlow()

    fun onIntent(intent: DetailResultScheduleIntent) {
        when (intent) {
            DetailResultScheduleIntent.OnAppear -> buildSections()
            is DetailResultScheduleIntent.ExceptionButtonTap -> {
                _uiState.update { it.copy(selectedDestination = intent.destination, isExceptionModalVisible = true) }
            }
            DetailResultScheduleIntent.ExceptionConfirm -> {
                val destination = _uiState.value.selectedDestination ?: return
                viewModelScope.launch {
                    _effect.emit(DetailResultScheduleEffect.NavigateBackWithException(destination))
                }
            }
            DetailResultScheduleIntent.ExceptionDismiss -> {
                _uiState.update { it.copy(isExceptionModalVisible = false, selectedDestination = null) }
            }
            DetailResultScheduleIntent.Back -> viewModelScope.launch {
                _effect.emit(DetailResultScheduleEffect.NavigateBack)
            }
        }
    }

    private fun buildSections() {
        val grouped = sendModel.scheduleItems.groupBy { item ->
            item.timeLabel.split(":").firstOrNull()?.toIntOrNull() ?: 0
        }
        val sections = grouped.entries
            .sortedBy { it.key }
            .map { (hour, items) -> HourSection(hour = hour, label = "${hour}시", items = items) }

        val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentIndex = sections.indexOfFirst { it.hour >= nowHour }.coerceAtLeast(0)

        _uiState.update {
            it.copy(
                stationName = sendModel.stationName,
                upDown = sendModel.upDown,
                exceptionLastStation = sendModel.exceptionLastStation,
                hourSections = sections,
                currentHourIndex = currentIndex,
            )
        }
    }
}
