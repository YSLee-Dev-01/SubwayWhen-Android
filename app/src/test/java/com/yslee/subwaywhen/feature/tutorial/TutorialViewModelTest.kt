package com.yslee.subwaywhen.feature.tutorial

import app.cash.turbine.test
import com.yslee.subwaywhen.data.repository.TutorialRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TutorialViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()
    lateinit var repository: TutorialRepository
    lateinit var viewModel: TutorialViewModel

    beforeEach {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = TutorialViewModel(repository)
    }

    afterEach {
        Dispatchers.resetMain()
    }

    test("FinishClicked → markTutorialSeen 호출 + NavigateToHome Effect 방출") {
        coJustRun { repository.markTutorialSeen() }

        viewModel.effect.test {
            viewModel.onIntent(TutorialIntent.FinishClicked)

            awaitItem() shouldBe TutorialEffect.NavigateToHome
        }

        coVerify(exactly = 1) { repository.markTutorialSeen() }
    }

    test("NextClicked → currentIndex가 0에서 1로 증가") {
        val initialState = viewModel.uiState.value as TutorialUiState.Success
        initialState.currentIndex shouldBe 0

        viewModel.onIntent(TutorialIntent.NextClicked)

        val updatedState = viewModel.uiState.value as TutorialUiState.Success
        updatedState.currentIndex shouldBe 1
    }

    test("PageChanged(6) → isLastPage == true") {
        viewModel.onIntent(TutorialIntent.PageChanged(6))

        val state = viewModel.uiState.value as TutorialUiState.Success
        state.isLastPage shouldBe true
    }

    test("PageChanged(3) → isLastPage == false") {
        viewModel.onIntent(TutorialIntent.PageChanged(3))

        val state = viewModel.uiState.value as TutorialUiState.Success
        state.isLastPage shouldBe false
    }
})
