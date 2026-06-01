package com.yslee.subwaywhen.feature.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * iOS MainTableHeaderSubView 대응. 혼잡도 카드 버튼.
 */
@Composable
fun HomeCongestionCard(
    congestionEmoji: String,
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
        label = "congestionCardScale",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) pressedColor else bgColor,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "congestionCardColor",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(Dimens.cornerRadius),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { scope.launch { delay(100L); onTap() } },
            )
            .padding(horizontal = Dimens.paddingInner),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.home_header_current_traffic),
                fontSize = Dimens.fontSizeMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = congestionEmoji,
                fontSize = Dimens.fontSizeMainTitleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

@Preview(name = "HomeCongestionCard - Light", showBackground = true)
@Composable
private fun HomeCongestionCardLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeCongestionCard(
            congestionEmoji = "🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥",
            onTap = {},
        )
    }
}

@Preview(name = "HomeCongestionCard - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeCongestionCardDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeCongestionCard(
            congestionEmoji = "🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥",
            onTap = {},
        )
    }
}
