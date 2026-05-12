package com.yslee.subwaywhen.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.yslee.subwaywhen.ui.theme.Dimens

/**
 * iOS ModalCustomButton 대응.
 * 탭 시 Dimens.animationScale(0.94f) scale 애니메이션 적용.
 * containerColor는 호출 측에서 지정 (iOS bgColor 대응).
 */
@Composable
fun PrimaryButton(
    text: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Dimens.animationScale else 1f,
        animationSpec = tween(durationMillis = (Dimens.animationSpeed * 1000).toInt()),
        label = "buttonScale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(Dimens.cornerRadius),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
    ) {
        Text(
            text = text,
            fontSize = Dimens.fontSizeMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}
