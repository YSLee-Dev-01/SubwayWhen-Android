package com.yslee.subwaywhen.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.repository.LocalDataRepository
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
class EditViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EditEffect>()
    val effect: SharedFlow<EditEffect> = _effect.asSharedFlow()

    /** dirty 비교용 기준 리스트 */
    private var lastSaved: List<SaveStation> = emptyList()

    init {
        val initial = localDataRepository.saveStations.value
        val (groupOne, groupTwo) = initial.splitByGroup()
        lastSaved = initial
        _uiState.update { it.copy(flatItems = buildFlatItems(groupOne, groupTwo)) }
    }

    fun onIntent(intent: EditIntent) {
        when (intent) {
            EditIntent.OnAppear -> Unit

            is EditIntent.DeleteStation -> {
                val newItems = _uiState.value.flatItems
                    .filterNot { it is EditFlatItem.Station && it.station == intent.station }
                    .let { insertDropTargetsIfNeeded(it) }
                _uiState.update {
                    it.copy(
                        flatItems = newItems,
                        isSaveEnabled = isSaveEnabled(newItems),
                    )
                }
                if (intent.station.line == "신분당선") {
                    viewModelScope.launch {
                        localDataRepository.deleteShinbundangSchedule(intent.station.stationName)
                    }
                }
            }

            is EditIntent.Reorder -> {
                // 드래그 종료 후 호출 — DropTarget 잔류 제거 + 빈 섹션에 재삽입
                val cleaned = insertDropTargetsIfNeeded(intent.newItems)
                _uiState.update {
                    it.copy(
                        flatItems = cleaned,
                        isSaveEnabled = isSaveEnabled(cleaned),
                    )
                }
            }

            EditIntent.SaveTap,
            EditIntent.DialogSave -> {
                viewModelScope.launch {
                    val merged = extractStations(_uiState.value.flatItems)
                    localDataRepository.updateSaveStations(merged)
                    lastSaved = merged
                    _uiState.update { it.copy(isSaveEnabled = false) }
                    _effect.emit(EditEffect.NavigateBack)
                }
            }

            EditIntent.BackTap -> {
                if (_uiState.value.isSaveEnabled) {
                    _uiState.update { it.copy(showNotSaveDialog = true) }
                } else {
                    viewModelScope.launch { _effect.emit(EditEffect.NavigateBack) }
                }
            }

            EditIntent.DialogDiscard -> {
                _uiState.update { it.copy(showNotSaveDialog = false) }
                viewModelScope.launch { _effect.emit(EditEffect.NavigateBack) }
            }

            EditIntent.DialogCancel -> {
                _uiState.update { it.copy(showNotSaveDialog = false) }
            }
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * 저장된 두 그룹으로부터 플랫 리스트 생성.
     * [Header(출근), ...Station, DropTarget?, Header(퇴근), ...Station, DropTarget?]
     */
    private fun buildFlatItems(
        groupOne: List<SaveStation>,
        groupTwo: List<SaveStation>,
    ): List<EditFlatItem> = buildList {
        add(EditFlatItem.Header("출근", SaveStationGroup.ONE))
        addAll(groupOne.map { EditFlatItem.Station(it) })
        if (groupOne.isEmpty()) add(EditFlatItem.DropTarget(SaveStationGroup.ONE))

        add(EditFlatItem.Header("퇴근", SaveStationGroup.TWO))
        addAll(groupTwo.map { EditFlatItem.Station(it) })
        if (groupTwo.isEmpty()) add(EditFlatItem.DropTarget(SaveStationGroup.TWO))
    }

    /**
     * 드래그/삭제 후 DropTarget 여부를 재조정.
     * 각 Header 뒤에 Station이 하나도 없으면 DropTarget 추가, 있으면 제거.
     */
    private fun insertDropTargetsIfNeeded(items: List<EditFlatItem>): List<EditFlatItem> {
        val result = mutableListOf<EditFlatItem>()
        var currentGroup: SaveStationGroup? = null
        var stationCountInSection = 0

        // DropTarget을 제거하고 필요한 위치에 다시 삽입
        val withoutDropTargets = items.filterNot { it is EditFlatItem.DropTarget }

        for (i in withoutDropTargets.indices) {
            val item = withoutDropTargets[i]
            if (item is EditFlatItem.Header) {
                // 이전 섹션이 비어있으면 DropTarget 삽입
                if (currentGroup != null && stationCountInSection == 0) {
                    result.add(EditFlatItem.DropTarget(currentGroup))
                }
                currentGroup = item.group
                stationCountInSection = 0
            } else if (item is EditFlatItem.Station) {
                stationCountInSection++
            }
            result.add(item)
        }
        // 마지막 섹션 처리
        if (currentGroup != null && stationCountInSection == 0) {
            result.add(EditFlatItem.DropTarget(currentGroup))
        }
        return result
    }

    /**
     * 플랫 리스트에서 각 역의 그룹을 Header 위치 기준으로 결정하여 SaveStation 리스트 반환.
     */
    private fun extractStations(items: List<EditFlatItem>): List<SaveStation> {
        var currentGroup = SaveStationGroup.ONE
        val result = mutableListOf<SaveStation>()
        for (item in items) {
            when (item) {
                is EditFlatItem.Header -> currentGroup = item.group
                is EditFlatItem.Station -> result.add(item.station.copy(group = currentGroup))
                is EditFlatItem.DropTarget -> Unit
            }
        }
        return result
    }

    private fun isSaveEnabled(items: List<EditFlatItem>): Boolean {
        return extractStations(items) != lastSaved
    }
}
