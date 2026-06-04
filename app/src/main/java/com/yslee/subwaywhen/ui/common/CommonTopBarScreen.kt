package com.yslee.subwaywhen.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
 * Column + verticalScroll 기반. 역 목록처럼 LazyColumn이 필요한 화면은 [CommonTopBarLazyScreen] 사용.
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
    val threshold = remember(density) { with(density) { 25.dp.toPx() } }
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

/**
 * LazyColumn + PullToRefresh 기반 화면용 CommonTopBarScreen 오버로드.
 * iOS NavigationBarScrollViewInSUI 대응 — 역 목록처럼 스크롤 성능이 중요한 화면에 사용.
 *
 * @param title TopBar(스크롤 후 나타나는 작은 제목)에 표시할 텍스트.
 * @param largeTitle 스크롤 영역 상단의 큰 제목. null이면 title을 그대로 사용한다.
 * @param listState 스크롤 감지에 사용. 외부에서 주입하면 스크롤 위치를 공유할 수 있다.
 * @param isRefreshing PullToRefresh 로딩 상태. onRefresh가 null이면 무시된다.
 * @param onRefresh 당겨서 새로고침 콜백. null이면 PullToRefreshBox를 렌더링하지 않는다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBarLazyScreen(
    title: String,
    largeTitle: String? = null,
    listState: LazyListState = rememberLazyListState(),
    isLargeTitleHidden: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    bottomPadding: Dp = 0.dp,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val threshold = remember(density) { with(density) { 25.dp.toPx() } }
    val isSubTitleVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > threshold
        }
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

        val resolvedLargeTitle = largeTitle ?: title
        if (onRefresh != null) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                CommonTopBarLazyContent(
                    listState = listState,
                    title = resolvedLargeTitle,
                    isLargeTitleHidden = isLargeTitleHidden,
                    bottomPadding = bottomPadding,
                    content = content,
                )
            }
        } else {
            CommonTopBarLazyContent(
                listState = listState,
                title = resolvedLargeTitle,
                isLargeTitleHidden = isLargeTitleHidden,
                bottomPadding = bottomPadding,
                content = content,
            )
        }
    }
}

/** [CommonTopBarLazyScreen] 내부용 — LazyColumn + Large Title + content + 하단 여백. */
@Composable
private fun CommonTopBarLazyContent(
    listState: LazyListState,
    title: String,
    isLargeTitleHidden: Boolean,
    bottomPadding: Dp,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.paddingLR),
    ) {
        if (!isLargeTitleHidden) {
            item {
                Text(
                    text = title,
                    fontSize = Dimens.fontSizeMainTitle,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = Dimens.fontSizeMainTitle * 1.4f,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.offset(y = -Dimens.titleOffsetY),
                )
            }
        }
        content()
        if (bottomPadding > 0.dp) {
            item { Spacer(modifier = Modifier.height(bottomPadding)) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "CommonTopBarLazyScreen - Light", showBackground = true)
@Composable
private fun CommonTopBarLazyScreenLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        CommonTopBarLazyScreen(
            title = "월요일,\n한 주도 화이팅해봐요!",
            isRefreshing = false,
            onRefresh = {},
        ) {
            items(listOf("강남역", "교대역", "선릉역")) { name ->
                Text(text = name, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "CommonTopBarLazyScreen - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CommonTopBarLazyScreenDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        CommonTopBarLazyScreen(
            title = "월요일,\n한 주도 화이팅해봐요!",
            isRefreshing = false,
            onRefresh = {},
        ) {
            items(listOf("강남역", "교대역", "선릉역")) { name ->
                Text(text = name, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
