package com.yslee.subwaywhen.feature.home.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS MainTableViewDefaultCell 대응.
 * 저장 역 0개일 때 홈 화면에 표시하는 안내 카드.
 */
@Composable
fun HomeEmptyStationCard(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Dimens.animationScale else 1f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "emptyCardScale",
    )

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.plus))
    val progress by animateLottieCompositionAsState(composition, iterations = 1)

    MainBgCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(102.dp),
            )
            Text(
                text = stringResource(R.string.home_empty_station),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = Dimens.paddingInner),
            )
        }
    }
}

@Preview(name = "HomeEmptyStationCard - Light", showBackground = true)
@Composable
private fun HomeEmptyStationCardLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeEmptyStationCard(
            onTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
        )
    }
}

@Preview(name = "HomeEmptyStationCard - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeEmptyStationCardDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeEmptyStationCard(
            onTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
        )
    }
}
