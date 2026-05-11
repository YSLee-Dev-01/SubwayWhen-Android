package com.yslee.subwaywhen.feature.tutorial

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun TutorialScreen(
    onNavigateToHome: () -> Unit,
    viewModel: TutorialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is TutorialEffect.NavigateToHome) {
                onNavigateToHome()
            }
        }
    }

    val state = uiState as? TutorialUiState.Success ?: return
    val pages = state.pages
    val currentIndex = state.currentIndex
    val isLastPage = state.isLastPage

    val pagerState = rememberPagerState(pageCount = { pages.size }, initialPage = currentIndex)

    val backgroundColor by animateColorAsState(
        targetValue = if (isLastPage) AppIconColor else MaterialTheme.colorScheme.background,
        label = "backgroundColor",
    )
    val headerTextColor by animateColorAsState(
        targetValue = if (isLastPage) Color.White else MaterialTheme.colorScheme.onBackground,
        label = "headerTextColor",
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onIntent(TutorialIntent.PageChanged(pagerState.currentPage))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        Text(
            text = stringResource(R.string.tutorial_header_title),
            fontSize = Dimens.fontSizeMedium,
            fontWeight = FontWeight.Bold,
            color = headerTextColor,
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = 16.dp),
        )

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val page = pages[pageIndex]
            when (val type = page.type) {
                is TutorialPageType.First -> TutorialFirstPageContent(
                    onNextClick = {
                        scope.launch { pagerState.animateScrollToPage(currentIndex + 1) }
                    },
                )
                is TutorialPageType.Middle -> TutorialMiddlePageContent(
                    page = type,
                    buttonLabelRes = page.buttonLabelRes,
                    onNextClick = {
                        scope.launch { pagerState.animateScrollToPage(currentIndex + 1) }
                    },
                )
                is TutorialPageType.Last -> TutorialLastPageContent(
                    buttonLabelRes = page.buttonLabelRes,
                    onFinishClick = { viewModel.onIntent(TutorialIntent.FinishClicked) },
                )
            }
        }
    }
}
