package com.yslee.subwaywhen.feature.search.vicinity

import app.cash.turbine.test
import com.google.firebase.analytics.FirebaseAnalytics
import com.yslee.subwaywhen.core.location.LocationData
import com.yslee.subwaywhen.core.location.LocationManager
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.repository.VicinityRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SearchVicinityViewModelTest : FunSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeEach { Dispatchers.setMain(testDispatcher) }
    afterEach { Dispatchers.resetMain() }

    val fakeLocation = LocationData(lat = 37.498, lon = 127.028)

    // line 값은 VicinityTransformData.lineColorName 변환 후 subwayLineCode 매핑 기준으로 설정한다.
    // 숫자로 시작하면 "0"이 앞에 붙으므로 "2호선" → lineColorName "02호선" → 지원 노선
    // "9호선" → lineColorName "09호선" → 지원 노선
    // "김포골드라인" → lineColorName "김포도시철도" → 미지원 노선
    fun fakeStation(
        name: String = "강남",
        line: String = "2호선",
        id: String = "1",
    ) = VicinityTransformData(id = id, name = name, line = line, distance = "100m")

    fun fakeArrival(upDown: String) = RealtimeStationArrival(
        upDown = upDown,
        arrivalTime = "2",
        subPrevious = "강남 방면",
        code = "0",
        subWayId = "1002",
        stationName = "강남",
        lastStation = "성수",
        backStationId = "221",
        nextStationId = "223",
        trainCode = "1234"
    )

    fun createViewModel(
        locationGranted: Boolean = false,
        locationData: LocationData? = fakeLocation,
        vicinityStations: List<VicinityTransformData> = emptyList(),
        liveArrivals: List<RealtimeStationArrival> = emptyList(),
    ): SearchVicinityViewModel {
        val locationManager = mockk<LocationManager>()
        coEvery { locationManager.locationAuthCheck() } returns locationGranted
        coEvery { locationManager.locationRequest() } returns locationData

        val vicinityRepository = mockk<VicinityRepository>()
        coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns vicinityStations
        coEvery { vicinityRepository.loadLiveArrival(any()) } returns liveArrivals

        return SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
    }

    // ── 1. OnAppear → 권한 없음 ──────────────────────────────────────
    test("OnAppear → 권한 없음: authStatus == Unknown, 위치 요청 미호출") {
        runTest(testDispatcher) {
            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            val vicinityRepository = mockk<VicinityRepository>()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.OnAppear)
            advanceUntilIdle()

            vm.uiState.value.authStatus shouldBe VicinityAuthStatus.Unknown
            coVerify(exactly = 0) { locationManager.locationRequest() }
        }
    }

    // ── 2. OnAppear → 권한 있음 + 빈 목록 ────────────────────────────
    test("OnAppear → 권한 있음 + 빈 목록: 위치 요청 + 근접역 로드 호출") {
        runTest(testDispatcher) {
            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns true
            coEvery { locationManager.locationRequest() } returns fakeLocation

            val vicinityRepository = mockk<VicinityRepository>()
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns emptyList()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.OnAppear)
            advanceUntilIdle()

            vm.uiState.value.authStatus shouldBe VicinityAuthStatus.Granted
            coVerify(exactly = 1) { locationManager.locationRequest() }
            coVerify(exactly = 1) { vicinityRepository.loadVicinityStations(any(), any()) }
        }
    }

    // ── 3. AuthResultReceived(false) ─────────────────────────────────
    test("AuthResultReceived(false): authStatus == Denied, isLocationModalVisible == true") {
        runTest(testDispatcher) {
            val vm = createViewModel()
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = false))
            advanceUntilIdle()

            vm.uiState.value.authStatus shouldBe VicinityAuthStatus.Denied
            vm.uiState.value.isLocationModalVisible shouldBe true
        }
    }

    // ── 4. AuthResultReceived(true) ──────────────────────────────────
    test("AuthResultReceived(true): authStatus == Granted, 위치/역 로드 호출") {
        runTest(testDispatcher) {
            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            coEvery { locationManager.locationRequest() } returns fakeLocation

            val vicinityRepository = mockk<VicinityRepository>()
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns emptyList()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            vm.uiState.value.authStatus shouldBe VicinityAuthStatus.Granted
            coVerify(exactly = 1) { locationManager.locationRequest() }
            coVerify(exactly = 1) { vicinityRepository.loadVicinityStations(any(), any()) }
        }
    }

    // ── 5. VicinityRefreshTapped 쿨다운 미달 ─────────────────────────
    test("VicinityRefreshTapped 쿨다운 미달: showRefreshCooldownDialog == true, 재로드 미호출") {
        runTest(testDispatcher) {
            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            val vicinityRepository = mockk<VicinityRepository>()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))

            val now = System.currentTimeMillis()
            // lastSearchTime을 now - 1분 전으로 설정 (5분 쿨다운 미달)
            vm.nowMillis = { now }

            // lastSearchTime은 nowMillis로 간접 제어:
            // nowMillis를 고정 후 refresh를 1회 호출해 lastSearchTime을 기록한 뒤
            // nowMillis를 4분 뒤로 세팅하고 다시 refresh 호출
            coEvery { locationManager.locationRequest() } returns fakeLocation
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns emptyList()

            val fixedNow = 1_000_000L
            vm.nowMillis = { fixedNow }

            // AuthResultReceived로 첫 로드 → lastSearchTime = fixedNow
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            // 2분 뒤 (쿨다운 미달)
            vm.nowMillis = { fixedNow + 2 * 60 * 1000L }
            vm.onIntent(VicinityIntent.VicinityRefreshTapped)
            advanceUntilIdle()

            vm.uiState.value.showRefreshCooldownDialog shouldBe true
            // locationRequest는 첫 로드 1회만 호출
            coVerify(exactly = 1) { locationManager.locationRequest() }
        }
    }

    // ── 6. VicinityRefreshTapped 쿨다운 충족 ─────────────────────────
    test("VicinityRefreshTapped 쿨다운 충족: 위치/역 재로드 호출, lastSearchTime 갱신") {
        runTest(testDispatcher) {
            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            coEvery { locationManager.locationRequest() } returns fakeLocation

            val vicinityRepository = mockk<VicinityRepository>()
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns emptyList()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))

            val fixedNow = 1_000_000L
            vm.nowMillis = { fixedNow }

            // 첫 로드 → lastSearchTime = fixedNow
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            // 6분 뒤 (쿨다운 충족)
            val refreshTime = fixedNow + 6 * 60 * 1000L
            vm.nowMillis = { refreshTime }
            vm.onIntent(VicinityIntent.VicinityRefreshTapped)
            advanceUntilIdle()

            // locationRequest 2회 호출 (첫 로드 + refresh)
            coVerify(exactly = 2) { locationManager.locationRequest() }
            vm.uiState.value.lastSearchTime shouldBe refreshTime
        }
    }

    // ── 7. StationTapped 미지원 노선 ─────────────────────────────────
    test("StationTapped 미지원 노선: errorDialog != null, tappedIndex 변경 없음") {
        runTest(testDispatcher) {
            // 김포도시철도는 미지원 노선 (subwayLineCode 반환 "")
            val unsupportedStation = fakeStation(name = "고촌", line = "김포골드라인")

            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            coEvery { locationManager.locationRequest() } returns fakeLocation

            val vicinityRepository = mockk<VicinityRepository>()
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns listOf(unsupportedStation)
            coEvery { vicinityRepository.loadLiveArrival(any()) } returns emptyList()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            vm.onIntent(VicinityIntent.StationTapped(index = 0))
            advanceUntilIdle()

            vm.uiState.value.errorDialog shouldBe "서비스 중인 노선이 아닙니다."
            vm.uiState.value.tappedIndex shouldBe null
            coVerify(exactly = 0) { vicinityRepository.loadLiveArrival(any()) }
        }
    }

    // ── 8. StationTapped 지원 노선 ───────────────────────────────────
    test("StationTapped 지원 노선: tappedIndex 갱신, 실시간 로드 호출") {
        runTest(testDispatcher) {
            // "2호선" → lineColorName "02호선" → subwayLineCode "1002" → isService = true
            val station = fakeStation(name = "강남", line = "2호선")

            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            coEvery { locationManager.locationRequest() } returns fakeLocation

            val vicinityRepository = mockk<VicinityRepository>()
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns listOf(station)
            coEvery { vicinityRepository.loadLiveArrival(any()) } returns emptyList()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            vm.onIntent(VicinityIntent.StationTapped(index = 0))
            advanceUntilIdle()

            vm.uiState.value.tappedIndex shouldBe 0
            coVerify(exactly = 1) { vicinityRepository.loadLiveArrival("강남") }
        }
    }

    // ── 9. 9호선 상하행 매핑 ─────────────────────────────────────────
    test("9호선 상하행 매핑: 상행 데이터가 하행으로, 하행이 상행으로 반전되어 state에 반영") {
        runTest(testDispatcher) {
            val ninthLineStation = fakeStation(name = "가양", line = "9호선")
            val upArrival = fakeArrival("상행")
            val downArrival = fakeArrival("하행")

            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            coEvery { locationManager.locationRequest() } returns fakeLocation

            val vicinityRepository = mockk<VicinityRepository>()
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns listOf(ninthLineStation)
            coEvery { vicinityRepository.loadLiveArrival(any()) } returns listOf(upArrival, downArrival)

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            vm.onIntent(VicinityIntent.StationTapped(index = 0))
            advanceUntilIdle()

            // 9호선: upLiveArrival ← "하행" 필터, downLiveArrival ← "상행" 필터
            vm.uiState.value.upLiveArrival shouldBe listOf(downArrival)
            vm.uiState.value.downLiveArrival shouldBe listOf(upArrival)
        }
    }

    // ── 10. ListStationTapped ────────────────────────────────────────
    test("ListStationTapped: VicinityEffect.SearchStation emit + isLocationModalVisible == false") {
        runTest(testDispatcher) {
            val stations = listOf(fakeStation(name = "교대", line = "02호선"))

            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            coEvery { locationManager.locationRequest() } returns fakeLocation

            val vicinityRepository = mockk<VicinityRepository>()
            coEvery { vicinityRepository.loadVicinityStations(any(), any()) } returns stations

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            vm.effect.test {
                vm.onIntent(VicinityIntent.ListStationTapped(index = 0))
                advanceUntilIdle()
                awaitItem() shouldBe VicinityEffect.SearchStation("교대")
            }

            vm.uiState.value.isLocationModalVisible shouldBe false
        }
    }

    // ── 11. GPS 실패 (null 반환) ─────────────────────────────────────
    test("GPS 실패(null 반환): isVicinityLoading == false, vicinityStations.isEmpty()") {
        runTest(testDispatcher) {
            val locationManager = mockk<LocationManager>()
            coEvery { locationManager.locationAuthCheck() } returns false
            coEvery { locationManager.locationRequest() } returns null

            val vicinityRepository = mockk<VicinityRepository>()

            val vm = SearchVicinityViewModel(locationManager, vicinityRepository, mockk(relaxed = true))
            vm.onIntent(VicinityIntent.AuthResultReceived(granted = true))
            advanceUntilIdle()

            vm.uiState.value.isVicinityLoading shouldBe false
            vm.uiState.value.vicinityStations.isEmpty() shouldBe true
        }
    }
})
