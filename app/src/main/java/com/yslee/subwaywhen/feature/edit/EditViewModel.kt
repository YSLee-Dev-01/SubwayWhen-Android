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
        _uiState.update { it.copy(groupOne = groupOne, groupTwo = groupTwo) }
    }

    fun onIntent(intent: EditIntent) {
        when (intent) {
            EditIntent.OnAppear -> Unit

            is EditIntent.DeleteStation -> {
                val state = _uiState.value
                val newGroupOne = state.groupOne.filterNot { it == intent.station }
                val newGroupTwo = state.groupTwo.filterNot { it == intent.station }
                _uiState.update {
                    it.copy(
                        groupOne = newGroupOne,
                        groupTwo = newGroupTwo,
                        isSaveEnabled = isSaveEnabled(newGroupOne, newGroupTwo),
                    )
                }
            }

            is EditIntent.MoveStation -> handleMove(intent)

            EditIntent.SaveTap,
            EditIntent.DialogSave -> {
                viewModelScope.launch {
                    val state = _uiState.value
                    val merged = Pair(state.groupOne, state.groupTwo).mergeGroups()
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

    private fun handleMove(intent: EditIntent.MoveStation) {
        val state = _uiState.value
        val groupOne = state.groupOne.toMutableList()
        val groupTwo = state.groupTwo.toMutableList()

        if (intent.fromSection == intent.toSection) {
            val list = if (intent.fromSection == 0) groupOne else groupTwo
            val item = list.removeAt(intent.fromIndex)
            list.add(intent.toIndex, item)
        } else {
            val fromList = if (intent.fromSection == 0) groupOne else groupTwo
            val toList = if (intent.toSection == 0) groupOne else groupTwo
            val targetGroup = if (intent.toSection == 0) SaveStationGroup.ONE else SaveStationGroup.TWO
            val item = fromList.removeAt(intent.fromIndex).copy(group = targetGroup)
            val clampedIndex = intent.toIndex.coerceIn(0, toList.size)
            toList.add(clampedIndex, item)
        }

        _uiState.update {
            it.copy(
                groupOne = groupOne,
                groupTwo = groupTwo,
                isSaveEnabled = isSaveEnabled(groupOne, groupTwo),
            )
        }
    }

    private fun isSaveEnabled(groupOne: List<SaveStation>, groupTwo: List<SaveStation>): Boolean {
        return (groupOne + groupTwo) != lastSaved
    }
}
