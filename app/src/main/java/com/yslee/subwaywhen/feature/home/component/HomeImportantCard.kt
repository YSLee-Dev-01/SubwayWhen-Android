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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.AnimatedTapBox
import com.yslee.subwaywhen.ui.common.AnimatedTapBoxAlignment
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS MainTableHeaderSubView(isImportantMode: true) 대응.
 * Firebase 공지 배너 카드. importantData가 null이 아닐 때만 표시된다.
 */
@Composable
fun HomeImportantCard(
    title: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight
    val pressedColor = if (isSystemInDarkTheme()) MainColorDark.copy(alpha = 0.7f) else MainColorLight.copy(alpha = 0.7f)

    AnimatedTapBox(
        bgColor = bgColor,
        pressedColor = pressedColor,
        modifier = modifier.fillMaxWidth().height(90.dp),
        alignment = AnimatedTapBoxAlignment.Fill,
        verticalPadding = 0.dp,
        horizontalPadding = Dimens.paddingInner,
        onClick = onTap,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "중요 알림",
                fontSize = Dimens.fontSizeLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = title,
                fontSize = Dimens.fontSizeMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
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

@Preview(name = "HomeImportantCard - Light", showBackground = true)
@Composable
private fun HomeImportantCardLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeImportantCard(
            title = "앱 업데이트 공지 사항",
            onTap = {},
        )
    }
}

@Preview(name = "HomeImportantCard - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeImportantCardDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeImportantCard(
            title = "앱 업데이트 공지 사항",
            onTap = {},
        )
    }
}
