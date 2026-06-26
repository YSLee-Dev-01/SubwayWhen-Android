package com.yslee.subwaywhen.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS UpDownExceptionViewInSUI 대응.
 * 상/하행 텍스트 + 제외역 텍스트(아래 화살표 포함) 두 칸짜리 행.
 *
 * @param upDownText 상행/하행 등 방향 텍스트
 * @param exceptionText 제외역 텍스트. 빈 문자열이면 "제외 없음" 표시
 * @param onExceptionClick 제외역 영역 탭 콜백
 */
@Composable
fun UpDownExceptionRow(
    upDownText: String,
    exceptionText: String,
    isExceptionEnabled: Boolean = true,
    onExceptionClick: () -> Unit,
) {
    val displayException = if (exceptionText.isEmpty()) "제외 행 없음" else "${exceptionText}행 제외"

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        MainBgCard(modifier = Modifier.weight(1f)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(40.dp),
            ) {
                Text(
                    text = upDownText,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = Dimens.fontSizeSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        MainBgCard(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (isExceptionEnabled) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onExceptionClick,
                        )
                    } else Modifier
                ),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(40.dp),
            ) {
                val contentColor = if (isExceptionEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = displayException,
                        color = contentColor,
                        fontSize = Dimens.fontSizeSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    if (isExceptionEnabled && exceptionText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .graphicsLayer(rotationZ = 180f)
                                .background(color = contentColor, shape = TriangleShape),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "UpDownExceptionRow - Light", showBackground = true)
@Composable
private fun UpDownExceptionRowLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        UpDownExceptionRow(
            upDownText = "상행",
            exceptionText = "구파발",
            onExceptionClick = {},
        )
    }
}

@Preview(name = "UpDownExceptionRow - Dark", showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun UpDownExceptionRowDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        UpDownExceptionRow(
            upDownText = "하행",
            exceptionText = "",
            onExceptionClick = {},
        )
    }
}
