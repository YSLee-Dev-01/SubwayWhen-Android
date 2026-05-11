package com.yslee.subwaywhen.feature.tutorial

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

@Composable
fun TutorialFirstPageContent(onNextClick: () -> Unit) {
    val bgColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.congratulations))

    var contentAlpha by remember { mutableFloatStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = contentAlpha,
        animationSpec = tween(durationMillis = 250),
        label = "fadeIn",
    )

    LaunchedEffect(Unit) {
        contentAlpha = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = stringResource(R.string.tutorial_page_0_title),
            fontSize = Dimens.fontSizeMainTitle,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(animatedAlpha),
        )

        PrimaryButton(
            text = stringResource(R.string.tutorial_page_0_button),
            containerColor = AppIconColor,
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingLR, vertical = 16.dp)
                .alpha(animatedAlpha),
        )
    }
}
