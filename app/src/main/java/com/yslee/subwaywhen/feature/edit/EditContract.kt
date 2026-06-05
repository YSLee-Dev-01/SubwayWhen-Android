package com.yslee.subwaywhen.feature.edit

import com.yslee.subwaywhen.data.model.SaveStation

data class EditUiState(
    val groupOne: List<SaveStation> = emptyList(),
    val groupTwo: List<SaveStation> = emptyList(),
    val isSaveEnabled: Boolean = false,
    val showNotSaveDialog: Boolean = false,
)

sealed interface EditIntent {
    data object OnAppear : EditIntent
    data class DeleteStation(val station: SaveStation) : EditIntent
    data class MoveStation(
        val fromSection: Int,
        val fromIndex: Int,
        val toSection: Int,
        val toIndex: Int,
    ) : EditIntent
    data object SaveTap : EditIntent
    data object BackTap : EditIntent
    data object DialogSave : EditIntent
    data object DialogDiscard : EditIntent
    data object DialogCancel : EditIntent
}

sealed interface EditEffect {
    data object NavigateBack : EditEffect
}
