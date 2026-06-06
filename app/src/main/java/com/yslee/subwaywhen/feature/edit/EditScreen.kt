package com.yslee.subwaywhen.feature.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.feature.edit.component.EditStationRow
import com.yslee.subwaywhen.feature.edit.component.NotSaveAlertDialog
import com.yslee.subwaywhen.ui.common.CommonTopBarLazyScreen
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTabBarVisibilityChange: (Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onTabBarVisibilityChange(false)
        viewModel.effect.collect { effect ->
            if (effect is EditEffect.NavigateBack) onNavigateBack()
        }
    }
    DisposableEffect(Unit) { onDispose { onTabBarVisibilityChange(true) } }

    BackHandler { viewModel.onIntent(EditIntent.BackTap) }

    EditScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun EditScreenContent(
    uiState: EditUiState,
    onIntent: (EditIntent) -> Unit,
) {
    // remember 키 없음 → MutableState 객체가 컴포저블 생명주기 내내 동일
    // remember(key) 방식은 key 변경 시 새 객체를 생성해 LaunchedEffect가 stale closure를 캡처
    var localItems by remember { mutableStateOf(uiState.flatItems) }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromIdx = localItems.indexOfFirst { it.listKey == from.key }
            val toIdx = localItems.indexOfFirst { it.listKey == to.key }

            // Header는 이동 불가 — from이나 to가 Header면 무시
            if (fromIdx == -1 || toIdx == -1) return@rememberReorderableLazyListState
            if (localItems[fromIdx] is EditFlatItem.Header) return@rememberReorderableLazyListState
            if (localItems[toIdx] is EditFlatItem.Header) return@rememberReorderableLazyListState

            // 드래그 중: localItems만 즉각 갱신 (ViewModel 동기화는 드래그 종료 시)
            // DropTarget 정리를 mid-drag에 하면 LazyColumn 구조 변경 → scroll jump 재발
            localItems = localItems.toMutableList().also {
                it.add(toIdx, it.removeAt(fromIdx))
            }
        },
    )

    // ViewModel 변경(삭제, 저장 후 복귀 등)을 localItems에 반영 — 드래그 중엔 무시
    LaunchedEffect(uiState.flatItems) {
        if (!reorderState.isAnyItemDragging) {
            localItems = uiState.flatItems
        }
    }

    // 드래그 종료 감지: isAnyItemDragging true→false 전환 시 ViewModel 동기화
    // 동일한 MutableState를 항상 읽으므로 stale closure 없음
    LaunchedEffect(reorderState) {
        var prevDragging = false
        snapshotFlow { reorderState.isAnyItemDragging }
            .collect { isDragging ->
                if (prevDragging && !isDragging) {
                    onIntent(EditIntent.Reorder(localItems))
                }
                prevDragging = isDragging
            }
    }

    val stations = localItems.filterIsInstance<EditFlatItem.Station>()
    val isEmpty = stations.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        CommonTopBarLazyScreen(
            title = "편집",
            onBack = { onIntent(EditIntent.BackTap) },
            listState = lazyListState,
            bottomPadding = 120.dp,
        ) {
            items(
                items = localItems,
                key = { it.listKey },
            ) { item ->
                when (item) {
                    is EditFlatItem.Header -> {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Dimens.paddingTB),
                        )
                    }

                    is EditFlatItem.Station -> {
                        ReorderableItem(reorderState, key = item.listKey) {
                            EditStationRow(
                                station = item.station,
                                onDelete = { onIntent(EditIntent.DeleteStation(item.station)) },
                            )
                        }
                    }

                    is EditFlatItem.DropTarget -> {
                        ReorderableItem(reorderState, key = item.listKey) {
                            Spacer(modifier = Modifier.fillMaxWidth().height(91.dp))
                        }
                    }
                }
            }
        }

        // 두 그룹 모두 비어있을 때 중앙 안내 문구
        if (isEmpty) {
            Text(
                text = "현재 저장되어 있는 지하철역이 없어요.",
                fontSize = Dimens.fontSizeMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        PrimaryButton(
            text = "저장",
            containerColor = if (uiState.isSaveEnabled) {
                AppIconColor
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (uiState.isSaveEnabled) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            onClick = { if (uiState.isSaveEnabled) onIntent(EditIntent.SaveTap) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingInner)
                .height(Dimens.modalButtonHeight),
        )
    }

    if (uiState.showNotSaveDialog) {
        NotSaveAlertDialog(
            onSave = { onIntent(EditIntent.DialogSave) },
            onDiscard = { onIntent(EditIntent.DialogDiscard) },
            onCancel = { onIntent(EditIntent.DialogCancel) },
        )
    }
}


@Preview(name = "EditScreen - 역 있음", showBackground = true)
@Composable
private fun EditScreenWithStationsPreview() {
    SubwayWhenTheme(darkTheme = false) {
        EditScreenContent(
            uiState = EditUiState(
                flatItems = listOf(
                    EditFlatItem.Header("출근", SaveStationGroup.ONE),
                    EditFlatItem.Station(
                        SaveStation(
                            id = "1",
                            stationName = "강남",
                            stationCode = "222",
                            updnLine = "상행",
                            line = "02호선",
                            lineCode = "1002",
                            group = SaveStationGroup.ONE,
                        )
                    ),
                    EditFlatItem.Header("퇴근", SaveStationGroup.TWO),
                    EditFlatItem.Station(
                        SaveStation(
                            id = "2",
                            stationName = "서울역",
                            stationCode = "150",
                            updnLine = "하행",
                            line = "01호선",
                            lineCode = "1001",
                            group = SaveStationGroup.TWO,
                        )
                    ),
                ),
                isSaveEnabled = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "EditScreen - 빈 상태", showBackground = true)
@Composable
private fun EditScreenEmptyPreview() {
    SubwayWhenTheme(darkTheme = false) {
        EditScreenContent(
            uiState = EditUiState(
                flatItems = listOf(
                    EditFlatItem.Header("출근", SaveStationGroup.ONE),
                    EditFlatItem.DropTarget(SaveStationGroup.ONE),
                    EditFlatItem.Header("퇴근", SaveStationGroup.TWO),
                    EditFlatItem.DropTarget(SaveStationGroup.TWO),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "EditScreen - 다이얼로그", showBackground = true)
@Composable
private fun EditScreenDialogPreview() {
    SubwayWhenTheme(darkTheme = false) {
        EditScreenContent(
            uiState = EditUiState(
                flatItems = listOf(
                    EditFlatItem.Header("출근", SaveStationGroup.ONE),
                    EditFlatItem.Station(
                        SaveStation(
                            id = "1",
                            stationName = "강남",
                            stationCode = "222",
                            updnLine = "상행",
                            line = "02호선",
                            lineCode = "1002",
                            group = SaveStationGroup.ONE,
                        )
                    ),
                    EditFlatItem.Header("퇴근", SaveStationGroup.TWO),
                    EditFlatItem.DropTarget(SaveStationGroup.TWO),
                ),
                isSaveEnabled = true,
                showNotSaveDialog = true,
            ),
            onIntent = {},
        )
    }
}
