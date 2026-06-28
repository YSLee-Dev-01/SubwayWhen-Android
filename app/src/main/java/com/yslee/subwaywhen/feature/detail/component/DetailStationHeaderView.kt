package com.yslee.subwaywhen.feature.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.StationLineCircle
import com.yslee.subwaywhen.ui.common.subwayLineColor
import com.yslee.subwaywhen.ui.theme.Dimens

@Composable
fun DetailStationHeaderView(
    prevStationName: String,
    stationName: String,
    nextStationName: String,
    lineNumber: String,
) {
    val lineColor = subwayLineColor(lineNumber) ?: Color.Gray
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(color = lineColor, shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (prevStationName.isNotEmpty()) {
                    Text(
                        text = prevStationName,
                        color = Color.White,
                        fontSize = Dimens.fontSizeSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (nextStationName.isNotEmpty()) {
                    Text(
                        text = nextStationName,
                        color = Color.White,
                        fontSize = Dimens.fontSizeSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        StationLineCircle(
            title = stationName,
            lineColor = lineColor,
            size = Dimens.stationLineCircleSize,
            isFilled = false,
        )
    }
}
