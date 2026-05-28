# Tasks: Search-3 (가까운 지하철역 찾기)

## 참조
- spec: `.claude/specs/features/Search-3/spec.md`
- plan: `.claude/specs/features/Search-3/plan.md`

## Task 목록

### Phase 1. 인프라 (권한·위치 SDK)

#### [x] Task 1 — `AndroidManifest.xml` (수정)
**파일**: `app/src/main/AndroidManifest.xml`
- `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />` 추가
- `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />` 추가

---

#### [x] Task 2 — `libs.versions.toml` + `build.gradle.kts` (수정)
**파일**: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- `libs.versions.toml`에 `play-services-location` 버전 `21.3.0` 항목 추가 (version + library 선언)
- `app/build.gradle.kts`에 `implementation(libs.play.services.location)` 추가

---

#### [x] Task 3 — `LocationData.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/core/location/LocationData.kt`
- `data class LocationData(val lat: Double, val lon: Double)` 선언
- iOS `CLLocationCoordinate2D` 대응 (1:1 포팅)

---

#### [x] Task 4 — `LocationManager.kt` + `LocationManagerImpl.kt` (신규)
**파일**:
- `app/src/main/java/com/yslee/subwaywhen/core/location/LocationManager.kt`
- `app/src/main/java/com/yslee/subwaywhen/core/location/LocationManagerImpl.kt`

`LocationManager` interface:
- `suspend fun locationAuthCheck(): Boolean` — `ContextCompat.checkSelfPermission(ACCESS_FINE_LOCATION)` 결과 반환
- `suspend fun locationRequest(): LocationData?` — `FusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, token).await()` 로 위치 조회 후 `LocationData` 반환, 예외 시 `null` 반환

`LocationManagerImpl`:
- 생성자에 `@ApplicationContext context: Context`, `FusedLocationProviderClient` 주입
- `locationRequest()` 내부에서 `CancellationTokenSource` 생성, `getCurrentLocation(...).await()` 호출
- 예외(`SecurityException`, `CancellationException` 포함) 발생 시 `null` 반환

---

#### [x] Task 5 — `LocationModule.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/LocationModule.kt`
- `@Provides fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient` — `LocationServices.getFusedLocationProviderClient(context)` 반환
- `@Binds abstract fun bindLocationManager(impl: LocationManagerImpl): LocationManager`

---

### Phase 2. DTO · Repository

#### [x] Task 6 — `VicinityTransformData.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/dto/vicinityStation/VicinityTransformData.kt`
- iOS `VicinityTransformData.swift` 1:1 포팅
- 필드: `id: String`, `name: String`, `line: String`, `distance: String`
- 파생 프로퍼티: `lineColorName: String` (호선명 → 색상 이름 문자열, iOS 동일 로직), `lineName: String` (표시용 약어명)
- `VicinityDocumentData`의 `place_name` 파싱 시 역명/호선 분리는 Repository 내 helper에서 처리

---

#### [x] Task 7 — `VicinityRepository.kt` + `VicinityRepositoryImpl.kt` (신규)
**파일**:
- `app/src/main/java/com/yslee/subwaywhen/data/repository/VicinityRepository.kt`
- `app/src/main/java/com/yslee/subwaywhen/data/repository/VicinityRepositoryImpl.kt`

`VicinityRepository` interface:
- `suspend fun loadVicinityStations(x: Double, y: Double): List<VicinityTransformData>`
- `suspend fun loadLiveArrival(stationName: String): List<RealtimeStationArrival>`

`VicinityRepositoryImpl`:
- `loadVicinityStations`: `LoadModel.vicinityStationsLoad(x.toString(), y.toString())` 호출 → `category == "SW8"` 필터 → 거리 오름차순 정렬 → `VicinityTransformData` 매핑
  - `place_name`에서 `"역"` 기준으로 역명과 호선 분리하는 private helper 추가 (iOS `SubwayTCA` 변환 로직 대응)
  - `distance` (미터 단위 String) → km 변환 표시 문자열로 변환하는 private helper 추가
  - 에러 / 빈 응답 시 `emptyList()` 반환
- `loadLiveArrival`: `LoadModel.stationArrivalRequest(stationName)` 호출 → `List<RealtimeStationArrival>` 반환
  - 에러 시 `emptyList()` 반환

---

#### [x] Task 8 — `RepositoryModule.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/RepositoryModule.kt`
- `@Binds abstract fun bindVicinityRepository(impl: VicinityRepositoryImpl): VicinityRepository` 추가

---

#### [x] Task 9 — `VicinityRepositoryImplTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/data/repository/VicinityRepositoryImplTest.kt`
- `LoadModel` MockK mock 사용
- 검증 시나리오:
  - `category != "SW8"` 항목은 필터링되어 결과에 포함되지 않음
  - 거리 기준 오름차순 정렬 정확성
  - `place_name` 파싱: 역명/호선 분리 정확성
  - `distance` 미터→km 변환 정확성
  - 빈 응답 시 `emptyList()` 반환
  - 네트워크 에러(`NetworkResult.Error`) 시 `emptyList()` 반환

---

### Phase 3. ViewModel · Contract

#### [x] Task 10 — `SearchVicinityContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/SearchVicinityContract.kt`

`VicinityAuthStatus`:
- `sealed interface` 또는 `enum class`: `Unknown`, `Denied`, `Granted`

`VicinityUiState` (data class):
- `authStatus: VicinityAuthStatus` (기본값 `Unknown`)
- `isVicinityLoading: Boolean`
- `vicinityStations: List<VicinityTransformData>`
- `tappedIndex: Int?`
- `upLiveArrival: List<RealtimeStationArrival>`
- `downLiveArrival: List<RealtimeStationArrival>`
- `liveLoading: Pair<Boolean, Boolean>` (상행/하행 각 로딩 상태)
- `lastSearchTime: Long?`
- `showRefreshCooldownDialog: Boolean`
- `errorDialog: String?`
- `isLocationModalVisible: Boolean`

`VicinityIntent` (sealed interface):
- `OnAppear`
- `AuthRequestTapped`
- `AuthResultReceived(granted: Boolean)`
- `VicinityRefreshTapped`
- `StationTapped(index: Int?)`
- `LiveRefreshTapped`
- `ListModalOpenTapped`
- `ListModalDismissed`
- `ListStationTapped(index: Int)`
- `DialogDismissed`

`VicinityEffect` (sealed interface):
- `SearchStation(name: String)` — SearchScreen에서 SaveStationModal 자동 띄우기용

---

#### [x] Task 11 — `SearchVicinityViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/SearchVicinityViewModel.kt`
- `@HiltViewModel`, 생성자 주입: `LocationManager`, `VicinityRepository`, `FirebaseAnalytics`, `nowMillis: () -> Long = { System.currentTimeMillis() }`
- `_uiState: MutableStateFlow<VicinityUiState>`, `_effect: MutableSharedFlow<VicinityEffect>`
- `fun onIntent(intent: VicinityIntent)` 단일 진입점

`OnAppear` 처리:
- `locationAuthCheck()` 호출
- 권한 있으면 `vicinityStations`가 비어 있을 때만 위치 요청 + 근접역 로드 트리거
- 권한 없으면 `authStatus = Unknown` 유지 (UI에서 권한 요청 버튼 표시)

`AuthRequestTapped` 처리:
- UI 레이어에서 권한 요청 launcher를 실행하는 트리거 역할 (ViewModel 상태 변경 없음, UI가 launcher 실행)

`AuthResultReceived(granted)` 처리:
- `granted == true`: `authStatus = Granted`, 위치 요청 + 근접역 로드
- `granted == false`: `authStatus = Denied`, `isLocationModalVisible = true`

`VicinityRefreshTapped` 처리:
- `lastSearchTime`이 null이거나 `nowMillis() - lastSearchTime >= 5 * 60 * 1000L`이면 위치 요청 + 근접역 재로드 + `lastSearchTime` 갱신
- 쿨다운 미달 시 `showRefreshCooldownDialog = true` (남은 초 계산하여 `errorDialog`에 저장)

`StationTapped(index)` 처리:
- `index == null`이면 선택 해제 (`tappedIndex = null`, live 목록 초기화)
- 선택된 역의 `subwayLineIsService == false`이면 `errorDialog` 세팅, `tappedIndex` 변경 없음
- 지원 노선이면 `tappedIndex = index` 후 실시간 도착 로드
  - 상행/하행 각각 `VicinityRepository.loadLiveArrival(stationName)` 호출
  - 9호선 분기: `line == "09호선"` 일 때 상행/하행 데이터 반전 적용 (iOS `(line != .nine && upDown == "상행") || (line == .nine && upDown == "하행")` 로직 대응)
  - `liveLoading` 쌍을 로딩 시작/종료 시 업데이트

`LiveRefreshTapped` 처리:
- 현재 `tappedIndex`의 역 실시간 도착 재요청 (StationTapped 로직 재사용)

`ListModalOpenTapped` / `ListModalDismissed` 처리:
- `isLocationModalVisible` 토글

`ListStationTapped(index)` 처리:
- `VicinityEffect.SearchStation(vicinityStations[index].name)` emit
- `isLocationModalVisible = false`

`DialogDismissed` 처리:
- `showRefreshCooldownDialog = false`, `errorDialog = null`

private helper:
- `suspend fun loadVicinityStations()`: `isVicinityLoading = true` → 위치 조회 → 근접역 로드 → `isVicinityLoading = false`; 위치 null 시 `isVicinityLoading = false` 후 빈 목록 유지

---

#### [x] Task 12 — `SearchVicinityViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/search/vicinity/SearchVicinityViewModelTest.kt`
- `LocationManager`, `VicinityRepository`, `FirebaseAnalytics` MockK mock 사용
- `nowMillis` 람다를 테스트에서 고정값으로 주입하여 쿨다운 테스트 결정론적 처리
- 검증 시나리오:
  - `OnAppear` → 권한 없음: `authStatus == Unknown`, 위치 요청 미호출
  - `OnAppear` → 권한 있음 + 빈 목록: 위치 요청 + 근접역 로드 호출
  - `AuthResultReceived(false)`: `authStatus == Denied`, `isLocationModalVisible == true`
  - `AuthResultReceived(true)`: `authStatus == Granted`, 위치/역 로드 호출
  - `VicinityRefreshTapped` 쿨다운 미달: `showRefreshCooldownDialog == true`, 재로드 미호출
  - `VicinityRefreshTapped` 쿨다운 충족: 위치/역 재로드 호출, `lastSearchTime` 갱신
  - `StationTapped` 미지원 노선: `errorDialog != null`, `tappedIndex` 변경 없음
  - `StationTapped` 지원 노선: `tappedIndex` 갱신, 실시간 로드 호출
  - 9호선 상하행 매핑: 상행 데이터가 하행으로, 하행이 상행으로 반전되어 state에 반영
  - `ListStationTapped`: `VicinityEffect.SearchStation` emit + `isLocationModalVisible == false`
  - GPS 실패(null 반환): `isVicinityLoading == false`, `vicinityStations.isEmpty()`

---

### Phase 4. UI Composable

#### [x] Task 13 — `VicinityEmptyState.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/component/VicinityEmptyState.kt`
- "현재 가까운 지하철역이 없어요." 텍스트 중앙 배치
- 다크/라이트 Preview 포함

---

#### [x] Task 14 — `VicinityLoading.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/component/VicinityLoading.kt`
- `CircularProgressIndicator` 중앙 배치
- Preview 포함

---

#### [x] Task 15 — `VicinityStationRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/component/VicinityStationRow.kt`
- `LazyRow` 내 각 항목 — 미선택 상태용
- `StationLineCircle` 사이즈 45dp, `isFill = true` 사용 (iOS `StationLineCircle` size 45 대응)
- `AnimatedTapBox` 로 탭 인터랙션 처리
- 파라미터: `station: VicinityTransformData`, `onClick: () -> Unit`
- Preview (다크/라이트) 포함

---

#### [x] Task 16 — `VicinityStationRowMini.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/component/VicinityStationRowMini.kt`
- 역 탭 후 비선택 항목용 축소 뷰
- 컬러 라운드 바(해당 노선색) + 역명 텍스트
- 파라미터: `station: VicinityTransformData`, `onClick: () -> Unit`
- Preview (다크/라이트) 포함

---

#### [x] Task 17 — `VicinityStationDetailCard.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/component/VicinityStationDetailCard.kt`
- 좌·중앙·우 3분할 레이아웃
  - 좌측: 상행/내선 도착 메시지 + 방면 (역명 기준 살짝 위 배치)
  - 중앙: `StationLineCircle` 사이즈 65dp (역명 크게 표시)
  - 우측: 하행/외선 도착 메시지 + 방면 (역명 기준 살짝 아래 배치)
- 상행/하행 로딩 중일 때 각 측 `CircularProgressIndicator` 표시
- 하단 5개 아이콘 버튼: 닫기, 새로고침, 임시 보기(TODO), 추가하기, 신고하기(TODO)
  - 닫기: `onClose` 콜백 (StationTapped(null) 위임)
  - 새로고침: `onRefresh` 콜백 (LiveRefreshTapped 위임)
  - 추가하기: `onAddStation` 콜백 (SearchStation effect 위임)
  - 임시 보기 / 신고하기: 클릭 시 빈 람다 + `// TODO` 주석
- 파라미터: `station: VicinityTransformData`, `upArrival: List<RealtimeStationArrival>`, `downArrival: List<RealtimeStationArrival>`, `liveLoading: Pair<Boolean,Boolean>`, `onClose: () -> Unit`, `onRefresh: () -> Unit`, `onAddStation: () -> Unit`
- Preview (역 선택 + 실시간 데이터 있는 상태, 다크/라이트) 포함

---

#### [x] Task 18 — `LocationListModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/modal/LocationListModal.kt`
- `CommonModalBottomSheet` 컨테이너 사용
- `authStatus`에 따라 본문 분기:
  - `Denied` 또는 `Unknown`: "위치 권한이 설정되어 있지 않아요." 안내 텍스트 + `ModalSubButton` 닫기 버튼
  - `Granted`: `LazyColumn`으로 가까운 역 목록
    - 셀: 좌측 호선 원형 또는 라운드 라벨(`StationLineCircle` 또는 노선색 컨테이너) + 가운데 역명 + 우측 거리(km)
    - 셀 탭: `onStationTapped(index)` 콜백
- 파라미터: `authStatus: VicinityAuthStatus`, `stations: List<VicinityTransformData>`, `onStationTapped: (Int) -> Unit`, `onDismiss: () -> Unit`
- Preview (권한 있음 + 역 목록, 권한 없음 안내, 다크/라이트) 포함

---

#### [x] Task 19 — `SearchVicinitySection.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/vicinity/SearchVicinitySection.kt`
- `hiltViewModel<SearchVicinityViewModel>()` 로 자체 ViewModel 사용
- `MainBgCard` 컨테이너 안에 전체 섹션 배치
- 헤더 영역:
  - "가까운 지하철역 찾기" 타이틀 + 서브 텍스트
  - 새로고침 아이콘 버튼 (`onIntent(VicinityIntent.VicinityRefreshTapped)`)
  - `authStatus == Unknown`일 때: 권한 요청 버튼 표시 (`AnimatedTapBox`, 탭 시 권한 launcher 실행)
- `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` 등록
  - 결과 콜백에서 `onIntent(VicinityIntent.AuthResultReceived(granted))` 호출
- 본문 분기:
  - `isVicinityLoading == true`: `VicinityLoading`
  - `authStatus == Unknown`: 안내 타이틀 + 권한 요청 버튼
  - `authStatus == Denied`: "위치 권한이 설정되어 있지 않아요." + 설정 이동 안내
  - `authStatus == Granted && vicinityStations.isEmpty()`: `VicinityEmptyState`
  - `authStatus == Granted && tappedIndex == null`: `LazyRow`로 `VicinityStationRow` 목록 + 하단 "목록으로 확인하기" `ModalSubButton`
  - `authStatus == Granted && tappedIndex != null`: `LazyRow`(선택 항목 `VicinityStationRow`, 비선택 `VicinityStationRowMini`) + `VicinityStationDetailCard`
- `LaunchedEffect`로 `viewModel.effect` 수집:
  - `VicinityEffect.SearchStation(name)` → `onStationSearch(name)` 콜백 호출
- `LaunchedEffect(Unit)` → `onIntent(VicinityIntent.OnAppear)`
- `AlertDialog`로 쿨다운 다이얼로그 / 에러 다이얼로그 표시 (`showRefreshCooldownDialog`, `errorDialog` 기반)
- `LocationListModal` 표시 (`isLocationModalVisible` 기반)
- 파라미터: `onStationSearch: (String) -> Unit`
- Preview 5상태 (권한 없음 / 로딩 중 / 빈 상태 / 역 목록 / 역 선택됨, 다크/라이트) 포함

---

### Phase 5. SearchScreen · SearchViewModel 통합

#### [x] Task 20 — `SearchContract.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchContract.kt`
- `SearchIntent`에 `VicinityStationSelected(stationName: String)` 추가
  - Vicinity 섹션에서 역 추가 버튼 탭 시 SearchScreen → SearchViewModel에 위임하기 위한 Intent

---

#### [x] Task 21 — `SearchViewModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchViewModel.kt`
- `onIntent`의 `when` 분기에 `SearchIntent.VicinityStationSelected` 핸들러 추가
  - `_internal.update { it.copy(isSearchMode = true, searchQuery = intent.stationName, isSearchLoading = true) }`
  - `_searchQuery.value = intent.stationName`
  - 기존 debounce + `performSearch` 파이프라인이 자동으로 검색 수행
  - `performSearch` 완료 후 `searchResult.firstOrNull()`을 `selectedStation`으로 자동 세팅
    - `vicinityAutoOpen` 플래그: `SearchUiState`에 `vicinityAutoOpen: Boolean = false` 추가, `VicinityStationSelected` 처리 시 `true`로 세팅, `performSearch`에서 `vicinityAutoOpen == true`이면 첫 결과를 `selectedStation`으로 세팅 후 플래그 `false`로 리셋

---

#### [x] Task 22 — `SearchUiState` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchContract.kt`
- `SearchUiState`에 `vicinityAutoOpen: Boolean = false` 필드 추가 (Task 21과 함께 처리)

---

#### [x] Task 23 — `SearchScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt`
- `// TODO: SearchVicinitySection — 다음 spec` 위치에 `SearchVicinitySection(...)` 삽입
  - `onStationSearch = { name -> onIntent(SearchIntent.VicinityStationSelected(name)) }`
- 관련 import 추가 (`SearchVicinitySection`)

---

### Phase 6. Dimens · 문자열 · 정리

#### [x] Task 24 — `Dimens.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/theme/Dimens.kt`
- 2회 이상 반복 사용되는 Vicinity 관련 값 추가:
  - `vicinityStationCircleSize = 45.dp` (LazyRow 역 원형)
  - `vicinityStationCircleSizeLarge = 65.dp` (Detail Card 가운데 원형)
  - 추가로 반복 사용 확인된 값 있으면 함께 등록

---

#### [x] Task 25 — `strings.xml` (수정)
**파일**: `app/src/main/res/values/strings.xml`
- 신규 문자열 리소스 추가:
  - `vicinity_section_title`: "가까운 지하철역 찾기"
  - `vicinity_section_subtitle`: "역을 누르면 실시간 정보를 확인할 수 있어요."
  - `vicinity_empty`: "현재 가까운 지하철역이 없어요."
  - `vicinity_refresh_cooldown`: "가까운 지하철역 찾기 기능은 5분에 한번씩 조회할 수 있어요.\n%d초 후에 다시 시도해주세요."
  - `vicinity_location_denied`: "위치 권한이 설정되어 있지 않아요."
  - `vicinity_list_button`: "목록으로 확인하기"
  - `vicinity_auth_request_button`: "가까운 역 확인하기"

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공
- [x] 테스트 통과 (`VicinityRepositoryImplTest` 8개, `SearchVicinityViewModelTest` 11개)
- [x] Compose Preview 다크/라이트 5상태 정상 렌더
- [x] import 정리 (Task별 변경으로 생긴 미사용 import 제거)

### 기능 (AC)
- [x] 위치 에러가 발생하더라도 Vicinity 섹션 영역(헤더 + 본문)은 유지됨
- [x] 위치 권한이 거부된 경우 `LocationListModal`에서 안내 텍스트 표출
- [x] 사용자가 원하는 역을 선택했을 때 해당 역의 실시간 정보가 보임
- [x] 하단 버튼을 눌러 원하는 기능으로 바로 이동 가능 (실시간 정보 확인 중)
- [x] 위치 권한 허용 + 역 있음: 가까운 순서 `LazyRow`, 새로고침 쿨다운, 목록 모달, 역 탭 → 실시간 카드
- [x] 위치 권한 허용 + 역 없음: "현재 가까운 지하철역이 없어요." 텍스트만 표시
- [x] 카드 하단 5버튼 중 닫기/새로고침/추가하기 동작, 임시보기·신고하기 비활성(TODO)
- [x] 모달 내 셀 탭 → 해당 역명 검색 후 `SaveStationModal` 자동 표출
- [x] 9호선 상하행 매핑 iOS 동작 패리티 충족
- [x] `AndroidManifest.xml` 위치 권한 추가 확인
- [x] `play-services-location` 의존성 추가 확인
