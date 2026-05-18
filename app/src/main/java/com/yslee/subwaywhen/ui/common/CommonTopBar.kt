package com.yslee.subwaywhen.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS NavigationBarInSUI 대응 — 커스텀 상단바.
 * isSubTitleVisible에 따라 title 텍스트를 fade + slide 애니메이션으로 표시/숨김.
 * onBack이 null이면 뒤로가기 아이콘 미렌더링.
 * trailingIcon과 onTrailingClick이 모두 non-null일 때만 우측 아이콘 렌더링.
 */
@Composable
fun CommonTopBar(
    title: String,
    isSubTitleVisible: Boolean,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    val titleAlpha by animateFloatAsState(
        targetValue = if (isSubTitleVisible) 1f else 0f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "topBarTitleAlpha",
    )
    val titleOffsetY by animateDpAsState(
        targetValue = if (isSubTitleVisible) 0.dp else Dimens.titleOffsetY,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "topBarTitleOffsetY",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(45.dp)
            .padding(horizontal = Dimens.paddingLR),
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = backIcon ?: Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Text(
            text = title,
            fontSize = Dimens.fontSizeLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(start = 1.dp)
                .alpha(titleAlpha)
                .offset(y = titleOffsetY),
        )

        Spacer(modifier = Modifier.weight(1f))

        if (trailingIcon != null && onTrailingClick != null) {
            IconButton(
                onClick = onTrailingClick,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Preview(name = "CommonTopBar - Light", showBackground = true)
@Composable
private fun CommonTopBarLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        CommonTopBar(
            title = "상세화면",
            isSubTitleVisible = true,
            onBack = {},
        )
    }
}

@Preview(name = "CommonTopBar - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CommonTopBarDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        CommonTopBar(
            title = "상세화면",
            isSubTitleVisible = false,
            onBack = {},
        )
    }
}
