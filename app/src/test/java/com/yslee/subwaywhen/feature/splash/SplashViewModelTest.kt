package com.yslee.subwaywhen.feature.splash

import app.cash.turbine.test
import com.yslee.subwaywhen.data.repository.TutorialRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest : FunSpec({

    lateinit var repository: TutorialRepository

    beforeEach {
        repository = mockk()
    }

    afterEach {
        Dispatchers.resetMain()
    }

    test("isTutorialSeen()이 true → NavigateToHome Effect 방출") {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        every { repository.isTutorialSeen() } returns flowOf(true)

        runTest(testDispatcher) {
            val viewModel = SplashViewModel(repository)

            viewModel.effect.test {
                advanceUntilIdle()
                awaitItem() shouldBe SplashEffect.NavigateToHome
            }
        }
    }

    test("isTutorialSeen()이 false → NavigateToTutorial Effect 방출") {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        every { repository.isTutorialSeen() } returns flowOf(false)

        runTest(testDispatcher) {
            val viewModel = SplashViewModel(repository)

            viewModel.effect.test {
                advanceUntilIdle()
                awaitItem() shouldBe SplashEffect.NavigateToTutorial
            }
        }
    }

    test("isTutorialSeen()이 예외 throw → NavigateToHome Effect 방출 (true fallback)") {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        every { repository.isTutorialSeen() } returns flow { throw RuntimeException("error") }

        runTest(testDispatcher) {
            val viewModel = SplashViewModel(repository)

            viewModel.effect.test {
                advanceUntilIdle()
                awaitItem() shouldBe SplashEffect.NavigateToHome
            }
        }
    }

    test("isReady가 조회 완료 후 true가 된다") {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        every { repository.isTutorialSeen() } returns flowOf(true)

        runTest(testDispatcher) {
            val viewModel = SplashViewModel(repository)
            advanceUntilIdle()

            viewModel.isReady.value shouldBe true
        }
    }
})
