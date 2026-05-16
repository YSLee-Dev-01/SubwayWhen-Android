package com.yslee.subwaywhen.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yslee.subwaywhen.data.repository.TutorialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashEffect {
    data object NavigateToHome : SplashEffect
    data object NavigateToTutorial : SplashEffect
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: TutorialRepository
) : ViewModel() {

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        viewModelScope.launch {
            // iOS: !tutorialSuccess && saveStation.isEmpty (AND 조건)
            // Android: SaveStation 미구현으로 tutorialSuccess만 확인. 역 저장 기능 구현 시 조건 추가 필요.
            val seen = repository.isTutorialSeen()
                .catch { emit(true) }
                .first()
            if (seen) _effect.emit(SplashEffect.NavigateToHome)
            else _effect.emit(SplashEffect.NavigateToTutorial)
            _isReady.value = true
        }
    }
}
