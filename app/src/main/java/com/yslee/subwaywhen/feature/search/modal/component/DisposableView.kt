package com.yslee.subwaywhen.feature.search.modal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS DisposableView.swift 대응. 임시 UI — Detail 연동은 다음 spec.
 */
@Composable
fun DisposableView(
    upText: String,
    downText: String,
    onUpTapped: () -> Unit,
    onDownTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cornerRadius))
            .background(color = AppIconColor.copy(alpha = 0.7f))
            .padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
    ) {
        Text(
            text = "저장하지 않고 일회성으로 볼 수 있어요.",
            fontSize = 10.sp,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(Dimens.disposableButtonWidth)
                .height(Dimens.disposableButtonHeight)
                .background(
                    color = Color.Red.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(Dimens.cornerRadius),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onUpTapped,
                ),
        ) {
            Text(
                text = upText,
                fontSize = Dimens.fontSizeSuperSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(Dimens.disposableButtonWidth)
                .height(Dimens.disposableButtonHeight)
                .background(
                    color = Color.Blue.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(Dimens.cornerRadius),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDownTapped,
                ),
        ) {
            Text(
                text = downText,
                fontSize = Dimens.fontSizeSuperSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "DisposableView - Light", showBackground = true)
@Composable
private fun DisposableViewLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        DisposableView(
            upText = "상행",
            downText = "하행",
            onUpTapped = {},
            onDownTapped = {},
            modifier = Modifier.padding(Dimens.paddingLR),
        )
    }
}
