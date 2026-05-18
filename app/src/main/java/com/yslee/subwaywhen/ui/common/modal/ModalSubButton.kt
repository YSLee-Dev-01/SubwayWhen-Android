package com.yslee.subwaywhen.ui.common.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS ModalSubCustomButton 대응.
 * 모달 내부 보조 버튼. cornerRadius/fontSize는 Dimens 토큰 사용.
 * 배경색·텍스트색은 호출부에서 지정.
 */
@Composable
fun ModalSubButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(Dimens.cornerRadius),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
    ) {
        Text(
            text = text,
            fontSize = Dimens.fontSizeMedium,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "ModalSubButton - Light", showBackground = true)
@Composable
private fun ModalSubButtonLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        ModalSubButton(
            text = "취소",
            bgColor = Color(0xFFE0E0E0),
            textColor = Color.Black,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "ModalSubButton - Dark", showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun ModalSubButtonDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        ModalSubButton(
            text = "취소",
            bgColor = Color(0xFF3A3A3C),
            textColor = Color.White,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
