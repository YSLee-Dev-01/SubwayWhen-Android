package com.yslee.subwaywhen.feature.tutorial

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

@Composable
fun TutorialLastPageContent(
    buttonLabelRes: Int,
    onFinishClick: () -> Unit,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.yslee.subwaywhen.R.raw.tutorial_success))
    var animationFinished by remember { mutableStateOf(false) }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
    )

    val lottieOffsetY by animateDpAsState(
        targetValue = if (animationFinished) 0.dp else (-110).dp,
        animationSpec = tween(durationMillis = 250),
        label = "lottieSlide",
    )

    var buttonAlpha by remember { mutableFloatStateOf(0f) }
    val animatedButtonAlpha by animateFloatAsState(
        targetValue = buttonAlpha,
        animationSpec = tween(durationMillis = 250),
        label = "buttonFadeIn",
    )

    LaunchedEffect(progress) {
        if (progress == 1f && !animationFinished) {
            animationFinished = true
            buttonAlpha = 1f
        }
    }

    // iOS: animationIcon → top.leading.trailing.equalToSuperview() + bottom.inset(110)
    // (contentView already has 20pt leading/trailing insets from the collection view layout)
    // Button is overlaid inside the animationIcon at bottom-16pt
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.paddingLR)
                .padding(bottom = 110.dp),
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = lottieOffsetY),
            )
        }

        PrimaryButton(
            text = stringResource(buttonLabelRes),
            containerColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight,
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = onFinishClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = Dimens.paddingLR, end = Dimens.paddingLR, top = 20.dp, bottom = 16.dp)
                .alpha(animatedButtonAlpha),
        )
    }
}
