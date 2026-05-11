package com.yslee.subwaywhen.feature.tutorial

sealed interface TutorialUiState {
    data class Success(
        val pages: List<TutorialPage>,
        val currentIndex: Int,
        val isLastPage: Boolean,
    ) : TutorialUiState
}

sealed interface TutorialIntent {
    data object NextClicked : TutorialIntent
    data class PageChanged(val index: Int) : TutorialIntent
    data object FinishClicked : TutorialIntent
}

sealed interface TutorialEffect {
    data object NavigateToHome : TutorialEffect
}
