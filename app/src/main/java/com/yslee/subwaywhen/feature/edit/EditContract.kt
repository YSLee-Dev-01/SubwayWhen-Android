package com.yslee.subwaywhen.feature.edit

import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup

/**
 * LazyColumn에 표시되는 플랫 리스트 아이템.
 * - Header: 출근/퇴근 구분선 (non-reorderable)
 * - Station: 역 셀 (ReorderableItem)
 * - DropTarget: 빈 섹션으로의 드롭 공간 (ReorderableItem, 비어있을 때만)
 */
sealed interface EditFlatItem {
    val listKey: String

    data class Header(val title: String, val group: SaveStationGroup) : EditFlatItem {
        override val listKey = "header_${group.name}"
    }

    data class Station(val station: SaveStation) : EditFlatItem {
        override val listKey = station.id
    }

    data class DropTarget(val group: SaveStationGroup) : EditFlatItem {
        override val listKey = "drop_target_${group.name}"
    }
}

data class EditUiState(
    val flatItems: List<EditFlatItem> = emptyList(),
    val isSaveEnabled: Boolean = false,
    val showNotSaveDialog: Boolean = false,
)

sealed interface EditIntent {
    data object OnAppear : EditIntent
    data class DeleteStation(val station: SaveStation) : EditIntent
    /** onMove에서 즉각 처리한 새 플랫 리스트를 ViewModel에 동기화 */
    data class Reorder(val newItems: List<EditFlatItem>) : EditIntent
    data object SaveTap : EditIntent
    data object BackTap : EditIntent
    data object DialogSave : EditIntent
    data object DialogDiscard : EditIntent
    data object DialogCancel : EditIntent
}

sealed interface EditEffect {
    data object NavigateBack : EditEffect
}
