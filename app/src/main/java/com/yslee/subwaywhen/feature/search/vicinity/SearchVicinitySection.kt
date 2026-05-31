package com.yslee.subwaywhen.feature.search.vicinity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.feature.search.vicinity.component.VicinityEmptyState
import com.yslee.subwaywhen.feature.search.vicinity.component.VicinityLoading
import com.yslee.subwaywhen.feature.search.vicinity.component.VicinityStationDetailCard
import com.yslee.subwaywhen.feature.search.vicinity.component.VicinityStationRow
import com.yslee.subwaywhen.feature.search.vicinity.component.VicinityStationRowMini
import com.yslee.subwaywhen.feature.search.vicinity.modal.LocationListModal
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.modal.ModalSubButton
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS SearchVicinityView 대응.
 * 위치 기반 가까운 지하철역 섹션.
 * 권한 상태·로딩·빈 상태·역 목록·역 상세를 단계별로 분기한다.
 */
@Composable
fun SearchVicinitySection(
    onStationSearch: (String) -> Unit,
    onTabBarVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchVicinityViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // LocationListModal이 열리면 탭바를 숨기고, 닫히면 복원한다
    LaunchedEffect(state.isLocationModalVisible) {
        onTabBarVisibilityChange(!state.isLocationModalVisible)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onIntent(VicinityIntent.AuthResultReceived(granted))
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VicinityEffect.SearchStation -> onStationSearch(effect.name)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(VicinityIntent.OnAppear)
    }

    if (state.showRefreshCooldownDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(VicinityIntent.DialogDismissed) },
            title = { Text(stringResource(R.string.vicinity_refresh_cooldown_title)) },
            text = { Text(stringResource(R.string.vicinity_refresh_cooldown, state.refreshCooldownSec)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(VicinityIntent.DialogDismissed) }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
        )
    }

    if (state.errorDialog != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(VicinityIntent.DialogDismissed) },
            text = { Text(state.errorDialog!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(VicinityIntent.DialogDismissed) }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
        )
    }

    if (state.isLocationModalVisible) {
        LocationListModal(
            authStatus = state.authStatus,
            stations = state.vicinityStations,
            onStationTapped = { viewModel.onIntent(VicinityIntent.ListStationTapped(it)) },
            onDismiss = { viewModel.onIntent(VicinityIntent.ListModalDismissed) },
        )
    }

    SearchVicinitySectionContent(
        state = state,
        onPermissionRequest = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        onIntent = viewModel::onIntent,
        onStationSearch = onStationSearch,
        modifier = modifier,
    )
}

@Composable
private fun SearchVicinitySectionContent(
    state: VicinityUiState,
    onPermissionRequest: () -> Unit,
    onIntent: (VicinityIntent) -> Unit,
    onStationSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    MainBgCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingInner)) {
            // 헤더: 권한이 있을 때만 타이틀 + 서브텍스트 + 새로고침 표시
            if (state.authStatus == VicinityAuthStatus.Granted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.vicinity_title),
                            fontSize = Dimens.fontSizeLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.vicinity_subtitle),
                            fontSize = Dimens.fontSizeSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.tappedIndex == null) {
                        IconButton(onClick = { onIntent(VicinityIntent.VicinityRefreshTapped) }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 본문 분기
            when {
                state.isVicinityLoading -> VicinityLoading()

                state.authStatus == VicinityAuthStatus.Unknown -> {
                    // 권한 미결정: 안내 텍스트 + 확인하기 버튼
                    Text(
                        text = stringResource(R.string.vicinity_auth_unknown_title),
                        fontSize = Dimens.fontSizeLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = Dimens.paddingTB),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedTapBox(
                        bgColor = AppIconColor,
                        pressedColor = AppIconColor.copy(alpha = 0.8f),
                        alignment = AnimatedTapBoxAlignment.Center,
                        verticalPadding = Dimens.paddingTB,
                        onClick = onPermissionRequest,
                    ) {
                        Text(
                            text = stringResource(R.string.vicinity_auth_request_button),
                            fontSize = Dimens.fontSizeSmall,
                            color = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.paddingTB))
                }

                state.authStatus == VicinityAuthStatus.Denied -> {
                    // 권한 거부: 안내 텍스트 + 설정으로 이동 버튼
                    Text(
                        text = stringResource(R.string.vicinity_location_denied),
                        fontSize = Dimens.fontSizeMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Dimens.paddingTB),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ModalSubButton(
                        text = stringResource(R.string.vicinity_go_to_settings),
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(Dimens.paddingTB))
                }

                state.vicinityStations.isEmpty() -> VicinityEmptyState()

                else -> {
                    val tappedIdx = state.tappedIndex

                    // exit 애니메이션 중에도 올바른 역 데이터를 유지하기 위해 마지막 non-null 인덱스 기억
                    var lastSelectedIdx by remember { mutableIntStateOf(0) }
                    if (tappedIdx != null) lastSelectedIdx = tappedIdx

                    // 역 목록: 미선택 → full 크기 / 선택 → 선택된 역 제외 mini 크기
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        itemsIndexed(state.vicinityStations) { index, station ->
                            if (tappedIdx == null) {
                                VicinityStationRow(
                                    station = station,
                                    onClick = { onIntent(VicinityIntent.StationTapped(index)) },
                                )
                            } else if (index != tappedIdx) {
                                VicinityStationRowMini(
                                    station = station,
                                    onClick = { onIntent(VicinityIntent.StationTapped(index)) },
                                )
                            }
                        }
                    }

                    // "목록으로 확인하기" 버튼 — 미선택 시만 표시 (fade)
                    AnimatedVisibility(
                        visible = tappedIdx == null,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(200)),
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            ModalSubButton(
                                text = stringResource(R.string.vicinity_list_button),
                                bgColor = AppIconColor,
                                textColor = Color.White,
                                onClick = { onIntent(VicinityIntent.ListModalOpenTapped) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.vicinityListButtonHeight),
                            )
                        }
                    }

                    // 상세 카드:
                    // - 최초 선택(null → non-null): visible false→true → enter 애니메이션
                    // - 역 전환(non-null → non-null): visible 유지 → 애니메이션 없이 내용만 갱신
                    // - 닫기(non-null → null): visible true→false → exit 애니메이션
                    AnimatedVisibility(
                        visible = tappedIdx != null,
                        enter = fadeIn(animationSpec = tween(300)) +
                            expandVertically(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(200)) +
                            shrinkVertically(animationSpec = tween(200)),
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            VicinityStationDetailCard(
                                station = state.vicinityStations[lastSelectedIdx],
                                upArrival = state.upLiveArrival,
                                downArrival = state.downLiveArrival,
                                liveLoading = state.liveLoading,
                                onClose = { onIntent(VicinityIntent.StationTapped(null)) },
                                onRefresh = { onIntent(VicinityIntent.LiveRefreshTapped) },
                                onAddStation = { onStationSearch(state.vicinityStations[lastSelectedIdx].name) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.paddingTB))
                }
            }
        }
    }
}

// region Preview

private val previewStations = listOf(
    VicinityTransformData(id = "1", name = "강남", line = "2호선", distance = "0.3km"),
    VicinityTransformData(id = "2", name = "역삼", line = "2호선", distance = "0.8km"),
    VicinityTransformData(id = "3", name = "선릉", line = "수인분당선", distance = "1.2km"),
)

private val previewArrivals = listOf(
    RealtimeStationArrival(
        upDown = "상행",
        arrivalTime = "120",
        subPrevious = "2분 후",
        code = "4",
        subWayId = "1002",
        stationName = "강남",
        lastStation = "성수",
        isFast = null,
        backStationId = "1002000222",
        nextStationId = "1002000224",
        trainCode = "2345",
    ),
)

@Preview(name = "SearchVicinitySection - 권한 없음 (Light)", showBackground = true)
@Composable
private fun PreviewAuthUnknownLight() {
    SubwayWhenTheme(darkTheme = false) {
        SearchVicinitySectionContent(
            state = VicinityUiState(authStatus = VicinityAuthStatus.Unknown),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 권한 없음 (Dark)", showBackground = true, backgroundColor = 0xFF161616)
@Composable
private fun PreviewAuthUnknownDark() {
    SubwayWhenTheme(darkTheme = true) {
        SearchVicinitySectionContent(
            state = VicinityUiState(authStatus = VicinityAuthStatus.Unknown),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 로딩 (Light)", showBackground = true)
@Composable
private fun PreviewLoadingLight() {
    SubwayWhenTheme(darkTheme = false) {
        SearchVicinitySectionContent(
            state = VicinityUiState(authStatus = VicinityAuthStatus.Granted, isVicinityLoading = true),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 로딩 (Dark)", showBackground = true, backgroundColor = 0xFF161616)
@Composable
private fun PreviewLoadingDark() {
    SubwayWhenTheme(darkTheme = true) {
        SearchVicinitySectionContent(
            state = VicinityUiState(authStatus = VicinityAuthStatus.Granted, isVicinityLoading = true),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 빈 상태 (Light)", showBackground = true)
@Composable
private fun PreviewEmptyLight() {
    SubwayWhenTheme(darkTheme = false) {
        SearchVicinitySectionContent(
            state = VicinityUiState(authStatus = VicinityAuthStatus.Granted, vicinityStations = emptyList()),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 빈 상태 (Dark)", showBackground = true, backgroundColor = 0xFF161616)
@Composable
private fun PreviewEmptyDark() {
    SubwayWhenTheme(darkTheme = true) {
        SearchVicinitySectionContent(
            state = VicinityUiState(authStatus = VicinityAuthStatus.Granted, vicinityStations = emptyList()),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 역 목록 (Light)", showBackground = true)
@Composable
private fun PreviewStationListLight() {
    SubwayWhenTheme(darkTheme = false) {
        SearchVicinitySectionContent(
            state = VicinityUiState(
                authStatus = VicinityAuthStatus.Granted,
                vicinityStations = previewStations,
                tappedIndex = null,
            ),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 역 목록 (Dark)", showBackground = true, backgroundColor = 0xFF161616)
@Composable
private fun PreviewStationListDark() {
    SubwayWhenTheme(darkTheme = true) {
        SearchVicinitySectionContent(
            state = VicinityUiState(
                authStatus = VicinityAuthStatus.Granted,
                vicinityStations = previewStations,
                tappedIndex = null,
            ),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 역 선택됨 (Light)", showBackground = true)
@Composable
private fun PreviewStationSelectedLight() {
    SubwayWhenTheme(darkTheme = false) {
        SearchVicinitySectionContent(
            state = VicinityUiState(
                authStatus = VicinityAuthStatus.Granted,
                vicinityStations = previewStations,
                tappedIndex = 0,
                upLiveArrival = previewArrivals,
                downLiveArrival = previewArrivals,
                liveLoading = Pair(false, false),
            ),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

@Preview(name = "SearchVicinitySection - 역 선택됨 (Dark)", showBackground = true, backgroundColor = 0xFF161616)
@Composable
private fun PreviewStationSelectedDark() {
    SubwayWhenTheme(darkTheme = true) {
        SearchVicinitySectionContent(
            state = VicinityUiState(
                authStatus = VicinityAuthStatus.Granted,
                vicinityStations = previewStations,
                tappedIndex = 0,
                upLiveArrival = previewArrivals,
                downLiveArrival = previewArrivals,
                liveLoading = Pair(false, false),
            ),
            onPermissionRequest = {},
            onIntent = {},
            onStationSearch = {},
        )
    }
}

// endregion
