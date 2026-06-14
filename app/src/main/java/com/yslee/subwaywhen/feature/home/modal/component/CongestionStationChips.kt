package com.yslee.subwaywhen.feature.home.modal.component

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

@Composable
fun CongestionStationChips(
    stations: List<String>,
    selectedStation: String,
    onStationTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (stations.isEmpty()) return

    LazyRow(
        modifier = modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(start = 2.dp),
    ) {
        items(stations, key = { it }) { station ->
            val isSelected = station == selectedStation
            val borderColor = AppIconColor

            val isDark = isSystemInDarkTheme()
            AnimatedTapBox(
                bgColor = if (isDark) MainColorDark else MainColorLight,
                pressedColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .then(
                        if (isSelected) Modifier.border(
                            width = 2.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(Dimens.cornerRadius),
                        ) else Modifier
                    ),
                onClick = { onStationTap(station) },
            ) {
                Text(
                    text = station,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CongestionStationChipsPreview() {
    CongestionStationChips(
        stations = listOf("강남", "홍대입구", "신촌", "서울역"),
        selectedStation = "강남",
        onStationTap = {},
    )
}
