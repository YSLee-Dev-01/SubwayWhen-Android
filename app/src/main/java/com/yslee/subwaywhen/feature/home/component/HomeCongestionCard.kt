package com.yslee.subwaywhen.feature.home.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

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

    AnimatedTapBox(
        bgColor = bgColor,
        pressedColor = pressedColor,
        modifier = modifier.fillMaxWidth().height(90.dp),
        alignment = AnimatedTapBoxAlignment.Leading,
        verticalPadding = 0.dp,
        horizontalPadding = Dimens.paddingInner,
        onClick = onTap,
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
