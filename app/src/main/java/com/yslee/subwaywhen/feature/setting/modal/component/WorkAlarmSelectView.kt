package com.yslee.subwaywhen.feature.setting.modal.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.StationRow
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

@Composable
fun WorkAlarmSelectView(
    stations: List<SaveStation>,
    selectedStation: SaveStation?,
    onBack: () -> Unit,
    onStationSelected: (SaveStation) -> Unit,
    onAlarmOff: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (stations.isNotEmpty()) {
                IconButton(onClick = onAlarmOff) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = "알림 끄기",
                    )
                }
            }
        }

        if (stations.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = "현재 저장되어 있는 지하철역이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val isDark = isSystemInDarkTheme()
            val bgColor = if (isDark) MainColorDark else MainColorLight
            val pressedColor = if (isDark) MainColorDark.copy(alpha = 0.7f) else MainColorLight.copy(alpha = 0.7f)
            LazyColumn {
                items(stations, key = { it.id }) { station ->
                    AnimatedTapBox(
                        bgColor = bgColor,
                        pressedColor = pressedColor,
                        onClick = { onStationSelected(station) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.paddingTB),
                    ) {
                        StationRow(
                            station = station,
                            modifier = Modifier.fillMaxWidth(),
                            trailingContent = if (station.id == selectedStation?.id) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "선택됨",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            } else null,
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(Dimens.tabBarBottomPadding)) }
            }
        }
    }
}
