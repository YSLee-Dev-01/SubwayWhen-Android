package com.yslee.subwaywhen.feature.search.modal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.feature.search.modal.component.DisposableView
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.common.modal.ModalSubButton
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.common.subwayLineIsService
import com.yslee.subwaywhen.ui.common.subwayLineUpDownText
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SaveStationModal(
    station: SearchStationInfo,
    onDismiss: () -> Unit,
    onSaveCompleted: () -> Unit,
) {
    val viewModel: SaveStationModalViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAlreadyExistsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(station) {
        viewModel.initStation(station)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SaveStationModalEffect.SaveCompleted -> {
                    onSaveCompleted()
                    onDismiss()
                }
                is SaveStationModalEffect.AlreadyExists -> {
                    showAlreadyExistsDialog = true
                }
                is SaveStationModalEffect.Close -> {
                    onDismiss()
                }
                is SaveStationModalEffect.DisposableDetailNavigate -> {
                    // TODO: Detail 연동은 다음 spec에서 구현
                    onDismiss()
                }
            }
        }
    }

    SaveStationModalContent(
        uiState = uiState,
        station = station,
        showAlreadyExistsDialog = showAlreadyExistsDialog,
        onIntent = viewModel::onIntent,
        onAlreadyExistsDismiss = { showAlreadyExistsDialog = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveStationModalContent(
    uiState: SaveStationModalUiState,
    station: SearchStationInfo,
    showAlreadyExistsDialog: Boolean,
    onIntent: (SaveStationModalIntent) -> Unit,
    onAlreadyExistsDismiss: () -> Unit,
) {
    val isNotService = !subwayLineIsService(station.line)

    CommonModalBottomSheet(
        mainTitle = "지하철 역 추가",
        subTitle = "그룹, 제외 행을 선택 후 상/하행 버튼을 누르면 저장할 수 있어요.",
        onDismiss = { onIntent(SaveStationModalIntent.Dismissed) },
        topDecoration = if (!isNotService) {
            {
                DisposableView(
                    upText = subwayLineUpDownText(station.line, isUp = true),
                    downText = subwayLineUpDownText(station.line, isUp = false),
                    onUpTapped = { onIntent(SaveStationModalIntent.DisposableUpTapped) },
                    onDownTapped = { onIntent(SaveStationModalIntent.DisposableDownTapped) },
                )
            }
        } else null,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingInner)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StationLineCircle(
                    title = subwayLineDisplayName(station.line),
                    lineColor = subwayLineColor(station.line),
                    size = Dimens.stationLineCircleSize,
                    isFilled = true,
                    fontSize = Dimens.fontSizeSmall,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = station.stationName,
                    fontSize = Dimens.fontSizeLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            if (!isNotService) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModalSubButton(
                        text = if (uiState.group == SaveStationGroup.ONE) "출근" else "퇴근",
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        onClick = { onIntent(SaveStationModalIntent.GroupToggled) },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = uiState.exceptionLastStation,
                        onValueChange = { onIntent(SaveStationModalIntent.ExceptionChanged(it)) },
                        placeholder = {
                            Text(
                                text = "중간 종착역 제거 (1개이상 콤마 이용)",
                                fontSize = Dimens.fontSizeSuperSmall,
                            )
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = Dimens.fontSizeSuperSmall,
                            textAlign = TextAlign.Center,
                        ),
                        shape = RoundedCornerShape(Dimens.cornerRadius),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = Dimens.modalButtonHeight),
                    )
                }
            }

            if (!isNotService) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModalSubButton(
                        text = subwayLineUpDownText(station.line, isUp = true),
                        bgColor = Color(0xFFFF3B30),
                        textColor = Color.White,
                        onClick = { onIntent(SaveStationModalIntent.UpButtonTapped) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                    ModalSubButton(
                        text = subwayLineUpDownText(station.line, isUp = false),
                        bgColor = Color(0xFF007AFF),
                        textColor = Color.White,
                        onClick = { onIntent(SaveStationModalIntent.DownButtonTapped) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            }

            if (isNotService) {
                ModalSubButton(
                    text = "해당 노선은 서비스를 지원하지 않아요.",
                    bgColor = MaterialTheme.colorScheme.onSurface,
                    textColor = MaterialTheme.colorScheme.surface,
                    onClick = { onIntent(SaveStationModalIntent.NotServiceTapped) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showAlreadyExistsDialog) {
        AlertDialog(
            onDismissRequest = onAlreadyExistsDismiss,
            title = { Text("이미 저장된 지하철역이에요.") },
            confirmButton = {
                TextButton(onClick = onAlreadyExistsDismiss) {
                    Text("확인")
                }
            },
        )
    }
}

@Preview(name = "SaveStationModal - 1호선", showBackground = true)
@Composable
private fun SaveStationModal1LinePreview() {
    SubwayWhenTheme(darkTheme = false) {
        SaveStationModalContent(
            uiState = SaveStationModalUiState(),
            station = SearchStationInfo(stationName = "종각", line = "01호선", stationCode = "157"),
            showAlreadyExistsDialog = false,
            onIntent = {},
            onAlreadyExistsDismiss = {},
        )
    }
}

@Preview(name = "SaveStationModal - 2호선 (내선/외선)", showBackground = true)
@Composable
private fun SaveStationModal2LinePreview() {
    SubwayWhenTheme(darkTheme = false) {
        SaveStationModalContent(
            uiState = SaveStationModalUiState(),
            station = SearchStationInfo(stationName = "강남", line = "02호선", stationCode = "222"),
            showAlreadyExistsDialog = false,
            onIntent = {},
            onAlreadyExistsDismiss = {},
        )
    }
}

@Preview(name = "SaveStationModal - 비서비스 노선 (김포도시철도)", showBackground = true)
@Composable
private fun SaveStationModalNotServicePreview() {
    SubwayWhenTheme(darkTheme = false) {
        SaveStationModalContent(
            uiState = SaveStationModalUiState(),
            station = SearchStationInfo(stationName = "고촌", line = "김포도시철도", stationCode = ""),
            showAlreadyExistsDialog = false,
            onIntent = {},
            onAlreadyExistsDismiss = {},
        )
    }
}
