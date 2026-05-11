package com.yslee.subwaywhen.feature.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.repository.TutorialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val repository: TutorialRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TutorialUiState>(
        TutorialUiState.Success(
            pages = tutorialPages,
            currentIndex = 0,
            isLastPage = false,
        )
    )
    val uiState: StateFlow<TutorialUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<TutorialEffect>(replay = 0)
    val effect: SharedFlow<TutorialEffect> = _effect.asSharedFlow()

    fun onIntent(intent: TutorialIntent) {
        when (intent) {
            is TutorialIntent.NextClicked -> {
                val current = _uiState.value as? TutorialUiState.Success ?: return
                onIntent(TutorialIntent.PageChanged(current.currentIndex + 1))
            }
            is TutorialIntent.PageChanged -> {
                val index = intent.index
                _uiState.value = TutorialUiState.Success(
                    pages = tutorialPages,
                    currentIndex = index,
                    isLastPage = index == tutorialPages.size - 1,
                )
            }
            is TutorialIntent.FinishClicked -> {
                viewModelScope.launch {
                    repository.markTutorialSeen()
                    _effect.emit(TutorialEffect.NavigateToHome)
                }
            }
        }
    }
}
