package com.yslee.subwaywhen.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS NavigationBarScrollViewInSUI 대응 — CommonTopBar + 스크롤 대형 타이틀 결합 스캐폴드.
 * 내부 rememberScrollState()로 스크롤 offset을 감지해 isSubTitleVisible 상태를 결정한다.
 * isLargeTitleHidden == false일 때 본문 상단에 대형 타이틀을 노출한다.
 */
@Composable
fun CommonTopBarScreen(
    title: String,
    isLargeTitleHidden: Boolean = false,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    bottomPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val threshold = with(density) { 25.dp.toPx() }
    val isSubTitleVisible by remember {
        derivedStateOf { scrollState.value >= threshold }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        CommonTopBar(
            title = title,
            isSubTitleVisible = isSubTitleVisible,
            onBack = onBack,
            backIcon = backIcon,
            trailingIcon = trailingIcon,
            onTrailingClick = onTrailingClick,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.paddingLR),
        ) {
            if (!isLargeTitleHidden) {
                Text(
                    text = title,
                    fontSize = Dimens.fontSizeMainTitle,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.offset(y = -Dimens.titleOffsetY),
                )
            }
            content()
            if (bottomPadding > 0.dp) {
                Spacer(modifier = Modifier.height(bottomPadding))
            }
        }
    }
}

@Preview(name = "CommonTopBarScreen - Light", showBackground = true)
@Composable
private fun CommonTopBarScreenLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        CommonTopBarScreen(
            title = "상세화면",
            onBack = {},
        ) {
            Text(text = "콘텐츠 영역")
        }
    }
}

@Preview(name = "CommonTopBarScreen - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CommonTopBarScreenDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        CommonTopBarScreen(
            title = "상세화면",
            isLargeTitleHidden = true,
            onBack = {},
        ) {
            Text(text = "콘텐츠 영역")
        }
    }
}
