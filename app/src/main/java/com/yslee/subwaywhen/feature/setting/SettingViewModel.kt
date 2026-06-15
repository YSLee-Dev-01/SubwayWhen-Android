package com.yslee.subwaywhen.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.core.notification.NotificationScheduler
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.repository.LocalDataRepository
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository,
    private val totalLoadModel: TotalLoadModel,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            localDataRepository.saveSetting.collect { setting ->
                _uiState.update { it.copy(saveSetting = setting) }
            }
        }
        viewModelScope.launch {
            localDataRepository.saveStations.collect { stations ->
                _uiState.update { it.copy(allStations = stations) }
            }
        }
    }

    fun onIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.OnAppear -> Unit

            is SettingIntent.TimeGroupTapped -> {
                val current = _uiState.value.expandedTimeGroup
                _uiState.update {
                    it.copy(expandedTimeGroup = if (current == intent.group) null else intent.group)
                }
            }

            is SettingIntent.TimeSaved -> {
                viewModelScope.launch {
                    val updated = when (intent.group) {
                        TimeGroup.Work -> _uiState.value.saveSetting.copy(mainGroupOneTime = intent.time)
                        TimeGroup.Leave -> _uiState.value.saveSetting.copy(mainGroupTwoTime = intent.time)
                    }
                    localDataRepository.updateSaveSetting(updated)
                    _uiState.update { it.copy(expandedTimeGroup = null) }
                }
            }

            is SettingIntent.ToggleChanged -> {
                viewModelScope.launch {
                    val s = _uiState.value.saveSetting
                    val updated = when (intent.field) {
                        SettingToggleField.AutoReload -> s.copy(detailAutoReload = !s.detailAutoReload)
                        SettingToggleField.ScheduleAutoTime -> s.copy(detailScheduleAutoTime = !s.detailScheduleAutoTime)
                        SettingToggleField.SearchOverlap -> s.copy(searchOverlapAlert = !s.searchOverlapAlert)
                    }
                    localDataRepository.updateSaveSetting(updated)
                }
            }

            is SettingIntent.CongestionLabelChanged -> {
                val clamped = intent.text.takeLast(1)
                _uiState.update {
                    it.copy(saveSetting = it.saveSetting.copy(mainCongestionLabel = clamped))
                }
            }

            is SettingIntent.CongestionLabelFocusLost -> {
                viewModelScope.launch {
                    val label = _uiState.value.saveSetting.mainCongestionLabel
                    val finalLabel = if (label.isEmpty()) "☹️" else label
                    localDataRepository.updateSaveSetting(
                        _uiState.value.saveSetting.copy(mainCongestionLabel = finalLabel)
                    )
                }
            }

            is SettingIntent.TrainIconTapped -> {
                _uiState.update { it.copy(activeModal = SettingModalType.TrainIcon) }
            }

            is SettingIntent.TrainIconSelected -> {
                viewModelScope.launch {
                    localDataRepository.updateSaveSetting(
                        _uiState.value.saveSetting.copy(detailVcTrainIcon = intent.icon)
                    )
                    _uiState.update { it.copy(activeModal = null) }
                }
            }

            is SettingIntent.LicenseTapped -> {
                _uiState.update { it.copy(activeModal = SettingModalType.License) }
            }

            is SettingIntent.ContentsTapped -> {
                _uiState.update { it.copy(activeModal = SettingModalType.Contents, isModalLoading = true) }
                viewModelScope.launch {
                    val contents = totalLoadModel.getContents()
                    _uiState.update { it.copy(modalContents = contents, isModalLoading = false) }
                }
            }

            is SettingIntent.ModalDismissed -> {
                _uiState.update {
                    it.copy(
                        activeModal = null,
                        modalLicenses = emptyList(),
                        modalContents = "",
                        isModalLoading = false,
                    )
                }
            }

            is SettingIntent.WorkAlarmOpened -> {
                val state = _uiState.value
                val stations = state.allStations
                val setting = state.saveSetting
                val groupOneStations = stations.filter { it.group == SaveStationGroup.ONE }
                val groupTwoStations = stations.filter { it.group == SaveStationGroup.TWO }
                val selectedOne = groupOneStations.firstOrNull { it.id == setting.alertGroupOneId }
                val selectedTwo = groupTwoStations.firstOrNull { it.id == setting.alertGroupTwoId }
                _uiState.update {
                    it.copy(
                        activeModal = SettingModalType.WorkAlarm,
                        hasNotificationPermission = intent.hasPermission,
                        isWeekendIncluded = setting.isWeekendNotificationEnabled,
                        workAlarmGroupOneStation = selectedOne,
                        workAlarmGroupTwoStation = selectedTwo,
                        workAlarmSelectGroup = null,
                        groupOneStations = groupOneStations,
                        groupTwoStations = groupTwoStations,
                    )
                }
            }

            is SettingIntent.WeekendToggled -> {
                _uiState.update { it.copy(isWeekendIncluded = !it.isWeekendIncluded) }
            }

            is SettingIntent.WorkAlarmStationTapped -> {
                _uiState.update { it.copy(workAlarmSelectGroup = intent.group) }
            }

            is SettingIntent.WorkAlarmStationSelected -> {
                _uiState.update { state ->
                    when (state.workAlarmSelectGroup) {
                        TimeGroup.Work -> state.copy(workAlarmGroupOneStation = intent.station, workAlarmSelectGroup = null)
                        TimeGroup.Leave -> state.copy(workAlarmGroupTwoStation = intent.station, workAlarmSelectGroup = null)
                        null -> state
                    }
                }
            }

            is SettingIntent.WorkAlarmStationReset -> {
                _uiState.update { state ->
                    when (intent.group) {
                        TimeGroup.Work -> state.copy(workAlarmGroupOneStation = null, workAlarmSelectGroup = null)
                        TimeGroup.Leave -> state.copy(workAlarmGroupTwoStation = null, workAlarmSelectGroup = null)
                    }
                }
            }

            is SettingIntent.WorkAlarmSelectPopped -> {
                _uiState.update { it.copy(workAlarmSelectGroup = null) }
            }

            is SettingIntent.WorkAlarmSaved -> {
                viewModelScope.launch {
                    val state = _uiState.value
                    val updated = state.saveSetting.copy(
                        alertGroupOneId = state.workAlarmGroupOneStation?.id ?: "",
                        alertGroupTwoId = state.workAlarmGroupTwoStation?.id ?: "",
                        isWeekendNotificationEnabled = state.isWeekendIncluded,
                    )
                    localDataRepository.updateSaveSetting(updated)
                    notificationScheduler.reschedule(updated, _uiState.value.allStations)
                    // activeModal은 animatedDismiss → onDismiss → ModalDismissed 흐름에서 정리
                }
            }
        }
    }
}
