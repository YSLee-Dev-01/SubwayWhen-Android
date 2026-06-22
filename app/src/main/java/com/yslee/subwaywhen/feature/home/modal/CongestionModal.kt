package com.yslee.subwaywhen.feature.home.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.feature.home.modal.component.CongestionChart
import com.yslee.subwaywhen.feature.home.modal.component.CongestionStationChips
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

@Composable
fun CongestionModal(
    onDismiss: () -> Unit,
    viewModel: CongestionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedHour by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onIntent(CongestionIntent.OnAppear)
    }

    CommonModalBottomSheet(
        mainTitle = "현재 지하철 예상 혼잡도",
        subTitle = "선택된 지하철역의 예상 혼잡도를 확인할 수 있어요.",
        onDismiss = onDismiss,
        confirmButton = { animatedDismiss ->
            val isDark = isSystemInDarkTheme()
            PrimaryButton(
                text = "닫기",
                containerColor = if (isDark) MainColorDark else MainColorLight,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = animatedDismiss,
                modifier = Modifier.fillMaxWidth().height(Dimens.modalButtonHeight),
            )
        },
    ) {
        CongestionStationChips(
            stations = uiState.availableStations,
            selectedStation = uiState.selectedStation,
            onStationTap = { station ->
                selectedHour = null
                viewModel.onIntent(CongestionIntent.StationTap(station))
            },
            modifier = Modifier,
        )

        Spacer(modifier = Modifier.height(20.dp))

        CongestionChart(
            congestionData = uiState.congestionData,
            nowHour = uiState.nowHour,
            selectedHour = selectedHour,
            onHourSelect = { selectedHour = it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
