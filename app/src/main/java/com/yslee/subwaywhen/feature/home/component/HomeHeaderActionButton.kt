package com.yslee.subwaywhen.feature.home.component

import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * iOS MainTableHeaderViewBtn 대응.
 * 민원/편집 공용 버튼 컴포저블.
 * 높이 90dp, MainColor 배경 카드 형태.
 * 상단 좌측: label (fontSizeLarge, ExtraBold), 하단 우측: Lottie 애니메이션 (50dp, 1회 재생).
 */
@Composable
fun HomeHeaderActionButton(
    label: String,
    @RawRes lottieRes: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight
    val pressedColor = if (isSystemInDarkTheme()) MainColorDark.copy(alpha = 0.7f) else MainColorLight.copy(alpha = 0.7f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) Dimens.animationScale else 1f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "actionBtnScale",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) pressedColor else bgColor,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "actionBtnColor",
    )

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(composition, iterations = 1)

    Box(
        modifier = modifier
            .height(90.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(color = backgroundColor, shape = RoundedCornerShape(Dimens.cornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { scope.launch { delay(100L); onTap() } },
            ),
    ) {
        Text(
            text = label,
            fontSize = Dimens.fontSizeLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 15.dp, top = 15.dp),
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 15.dp, bottom = 15.dp)
                .size(50.dp),
        )
    }
}

@Preview(name = "HomeHeaderActionButton - Report - Light", showBackground = true)
@Composable
private fun HomeHeaderActionButtonReportLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeHeaderActionButton(
            label = "지하철 민원",
            lottieRes = R.raw.report,
            onTap = {},
        )
    }
}

@Preview(name = "HomeHeaderActionButton - Report - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeHeaderActionButtonReportDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeHeaderActionButton(
            label = "지하철 민원",
            lottieRes = R.raw.report,
            onTap = {},
        )
    }
}

@Preview(name = "HomeHeaderActionButton - Edit - Light", showBackground = true)
@Composable
private fun HomeHeaderActionButtonEditLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeHeaderActionButton(
            label = "편집",
            lottieRes = R.raw.list,
            onTap = {},
        )
    }
}

@Preview(name = "HomeHeaderActionButton - Edit - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeHeaderActionButtonEditDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeHeaderActionButton(
            label = "편집",
            lottieRes = R.raw.list,
            onTap = {},
        )
    }
}
