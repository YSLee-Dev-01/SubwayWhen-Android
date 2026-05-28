package com.yslee.subwaywhen.feature.search.vicinity.modal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.feature.search.vicinity.VicinityAuthStatus
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.common.modal.ModalSubButton
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.common.subwayLineDisplayName
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun LocationListModal(
    authStatus: VicinityAuthStatus,
    stations: List<VicinityTransformData>,
    onStationTapped: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    CommonModalBottomSheet(
        mainTitle = stringResource(R.string.vicinity_list_modal_title),
        onDismiss = onDismiss,
    ) {
        when (authStatus) {
            VicinityAuthStatus.Denied, VicinityAuthStatus.Unknown -> {
                Text(
                    text = stringResource(R.string.vicinity_location_denied),
                    fontSize = Dimens.fontSizeMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.paddingLR),
                )
                ModalSubButton(
                    text = stringResource(R.string.common_close),
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            VicinityAuthStatus.Granted -> {
                LazyColumn {
                    itemsIndexed(stations) { index, station ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStationTapped(index) }
                                .padding(vertical = 12.dp),
                        ) {
                            StationLineCircle(
                                title = subwayLineDisplayName(station.lineColorName),
                                lineColor = subwayLineColor(station.lineColorName),
                                size = 32.dp,
                                isFilled = true,
                                fontSize = Dimens.fontSizeSuperSmall,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = station.name,
                                fontSize = Dimens.fontSizeMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = station.distance,
                                fontSize = Dimens.fontSizeSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "LocationListModal - Granted (Light)", showBackground = true)
@Composable
private fun LocationListModalGrantedLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        LocationListModal(
            authStatus = VicinityAuthStatus.Granted,
            stations = listOf(
                VicinityTransformData(id = "1", name = "강남", line = "02호선", distance = "0.3km"),
                VicinityTransformData(id = "2", name = "역삼", line = "02호선", distance = "0.8km"),
                VicinityTransformData(id = "3", name = "선릉", line = "수인분당선", distance = "1.2km"),
            ),
            onStationTapped = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "LocationListModal - Granted (Dark)", showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun LocationListModalGrantedDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        LocationListModal(
            authStatus = VicinityAuthStatus.Granted,
            stations = listOf(
                VicinityTransformData(id = "1", name = "강남", line = "02호선", distance = "0.3km"),
                VicinityTransformData(id = "2", name = "역삼", line = "02호선", distance = "0.8km"),
            ),
            onStationTapped = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "LocationListModal - Denied (Light)", showBackground = true)
@Composable
private fun LocationListModalDeniedLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        LocationListModal(
            authStatus = VicinityAuthStatus.Denied,
            stations = emptyList(),
            onStationTapped = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "LocationListModal - Denied (Dark)", showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun LocationListModalDeniedDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        LocationListModal(
            authStatus = VicinityAuthStatus.Denied,
            stations = emptyList(),
            onStationTapped = {},
            onDismiss = {},
        )
    }
}
