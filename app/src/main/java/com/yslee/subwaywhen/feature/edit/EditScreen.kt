package com.yslee.subwaywhen.feature.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
            val toKey = to.key as? String ?: return@rememberReorderableLazyListState

            // 헤더/from이 헤더인 경우 제외
            if (fromKey.startsWith("header_") || toKey.startsWith("header_")) return@rememberReorderableLazyListState

            // 빈 섹션 드롭 타겟 key → 해당 섹션 index 0으로 정규화
            val normalizedToKey = when (toKey) {
                "group1_drop_target" -> "group1_0"
                "group2_drop_target" -> "group2_0"
                else -> toKey
            }

            val (fromSection, fromIndex) = parseItemKey(fromKey) ?: return@rememberReorderableLazyListState
            val (toSection, toIndex) = parseItemKey(normalizedToKey) ?: return@rememberReorderableLazyListState

            onIntent(EditIntent.MoveStation(fromSection, fromIndex, toSection, toIndex))
        },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CommonTopBarLazyScreen(
            title = "편집",
            onBack = { onIntent(EditIntent.BackTap) },
            listState = lazyListState,
            bottomPadding = 120.dp,
        ) {
            item(key = "header_group1") {
                Text(
                    text = "출근",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Dimens.paddingTB),
                )
            }
            itemsIndexed(
                items = uiState.groupOne,
                key = { index, _ -> "group1_$index" },
            ) { index, station ->
                val itemKey = "group1_$index"
                ReorderableItem(reorderState, key = itemKey) {
                    EditStationRow(
                        station = station,
                        onDelete = { onIntent(EditIntent.DeleteStation(station)) },
                    )
                }
            }
            // 출근 섹션이 비어있을 때 드롭 타겟 — 퇴근→출근 이동 가능하게
            if (uiState.groupOne.isEmpty()) {
                item(key = "group1_drop_target") {
                    Spacer(modifier = Modifier.fillMaxWidth().height(91.dp))
                }
            }

            item(key = "header_group2") {
                Text(
                    text = "퇴근",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Dimens.paddingTB),
                )
            }
            itemsIndexed(
                items = uiState.groupTwo,
                key = { index, _ -> "group2_$index" },
            ) { index, station ->
                val itemKey = "group2_$index"
                ReorderableItem(reorderState, key = itemKey) {
                    EditStationRow(
                        station = station,
                        onDelete = { onIntent(EditIntent.DeleteStation(station)) },
                    )
                }
            }
            // 퇴근 섹션이 비어있을 때 드롭 타겟 — 출근→퇴근 이동 가능하게
            if (uiState.groupTwo.isEmpty()) {
                item(key = "group2_drop_target") {
                    Spacer(modifier = Modifier.fillMaxWidth().height(91.dp))
                }
            }
        }

        // iOS noListLabel 대응: 두 그룹 모두 비어있을 때 중앙 안내 문구
        if (uiState.groupOne.isEmpty() && uiState.groupTwo.isEmpty()) {
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

/** "group1_2" → Pair(section=0, index=2), "group2_0" → Pair(section=1, index=0) */
private fun parseItemKey(key: String): Pair<Int, Int>? {
    val parts = key.split("_")
    if (parts.size != 2) return null
    val section = when (parts[0]) {
        "group1" -> 0
        "group2" -> 1
        else -> return null
    }
    val index = parts[1].toIntOrNull() ?: return null
    return Pair(section, index)
}

@Preview(name = "EditScreen - 역 있음", showBackground = true)
@Composable
private fun EditScreenWithStationsPreview() {
    SubwayWhenTheme(darkTheme = false) {
        EditScreenContent(
            uiState = EditUiState(
                groupOne = listOf(
                    SaveStation(
                        id = "1",
                        stationName = "강남",
                        stationCode = "222",
                        updnLine = "상행",
                        line = "02호선",
                        lineCode = "1002",
                        group = SaveStationGroup.ONE,
                    ),
                ),
                groupTwo = listOf(
                    SaveStation(
                        id = "2",
                        stationName = "서울역",
                        stationCode = "150",
                        updnLine = "하행",
                        line = "01호선",
                        lineCode = "1001",
                        group = SaveStationGroup.TWO,
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
            uiState = EditUiState(),
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
                groupOne = listOf(
                    SaveStation(
                        id = "1",
                        stationName = "강남",
                        stationCode = "222",
                        updnLine = "상행",
                        line = "02호선",
                        lineCode = "1002",
                        group = SaveStationGroup.ONE,
                    ),
                ),
                isSaveEnabled = true,
                showNotSaveDialog = true,
            ),
            onIntent = {},
        )
    }
}
