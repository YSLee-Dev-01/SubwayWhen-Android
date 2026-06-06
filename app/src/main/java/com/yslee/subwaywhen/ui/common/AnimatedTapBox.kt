package com.yslee.subwaywhen.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * iOS AnimationButtonInSUI 대응.
 * 눌림 시 scale + 배경색 전환 컨테이너.
 * alignment에 따라 내부 content의 좌/중/우 정렬을 Spacer로 제어한다.
 * tap → 100ms 지연 후 onClick() 호출 (iOS 0.1s 동작 동일).
 *
 * Box 절대 배치(TopStart/BottomEnd 등)가 필요한 경우:
 * alignment = Leading, verticalPadding = 0.dp, horizontalPadding = 0.dp 로 설정 후
 * content 내부에 Box(Modifier.fillMaxSize()) 를 두면 fillMaxWidth/fillMaxHeight로 전체 영역 활용 가능.
 */
enum class AnimatedTapBoxAlignment {
    Leading, Center, Trailing,
    /** content가 Row 전체를 채울 때 사용. Spacer를 추가하지 않는다. */
    Fill
}

@Composable
fun AnimatedTapBox(
    bgColor: Color,
    pressedColor: Color,
    modifier: Modifier = Modifier,
    alignment: AnimatedTapBoxAlignment = AnimatedTapBoxAlignment.Center,
    verticalPadding: Dp = 10.dp,
    horizontalPadding: Dp = 0.dp,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) Dimens.animationScale else 1f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "tapBoxScale",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) pressedColor else bgColor,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "tapBoxColor",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(Dimens.cornerRadius),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { scope.launch { delay(50L); onClick() } },
            )
            .padding(vertical = verticalPadding, horizontal = horizontalPadding),
    ) {
        if (alignment == AnimatedTapBoxAlignment.Center || alignment == AnimatedTapBoxAlignment.Trailing) {
            Spacer(modifier = Modifier.weight(1f))
        }
        content()
        if (alignment == AnimatedTapBoxAlignment.Center || alignment == AnimatedTapBoxAlignment.Leading) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(name = "AnimatedTapBox - Light", showBackground = true)
@Composable
private fun AnimatedTapBoxLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        AnimatedTapBox(
            bgColor = Color(0xFFE0E0E0),
            pressedColor = Color(0xFFBDBDBD),
            alignment = AnimatedTapBoxAlignment.Center,
            onClick = {},
        ) {
            Text(text = "버튼")
        }
    }
}

@Preview(name = "AnimatedTapBox - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AnimatedTapBoxDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        AnimatedTapBox(
            bgColor = Color(0xFF424242),
            pressedColor = Color(0xFF616161),
            alignment = AnimatedTapBoxAlignment.Leading,
            onClick = {},
        ) {
            Text(text = "버튼", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
