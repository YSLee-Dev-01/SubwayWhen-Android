# Tasks: Search

## 참조
- spec: `.claude/specs/features/Search/spec.md`
- plan: `.claude/specs/features/Search/plan.md`

## Task 목록

### Phase 1. 의존성 추가 및 Firebase 셋업

#### [x] Task 1 — `libs.versions.toml` (수정)
**파일**: `gradle/libs.versions.toml`
- `firebase-bom` 버전 항목 추가
- `firebase-database-ktx` 라이브러리 항목 추가
- `google-services` Gradle 플러그인 항목 추가

---

#### [x] Task 2 — 루트 `build.gradle.kts` (수정)
**파일**: `build.gradle.kts`
- `plugins` 블록에 `id("com.google.gms.google-services") version "x.y.z" apply false` 추가

---

#### [x] Task 3 — `app/build.gradle.kts` (수정)
**파일**: `app/build.gradle.kts`
- `plugins` 블록에 `alias(libs.plugins.google.services)` 추가
- `dependencies` 블록에 `implementation(platform(libs.firebase.bom))` + `implementation(libs.firebase.database.ktx)` 추가

---

#### [x] Task 4 — `google-services.json` (신규 배치)
**파일**: `app/google-services.json`
- Firebase 콘솔에서 발급받은 Android 앱 설정 파일 배치 (사용자 측 준비 항목)
- 빌드 검증: `./gradlew :app:assembleDebug` 통과 확인

---

### Phase 2. 데이터 레이어 — Firebase DataSource & Search Repository

#### [x] Task 5 — `SearchQueryRecommendData.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/dto/stationSearch/SearchQueryRecommendData.kt`
- `data class SearchQueryRecommendData(val queryName: String, val stationName: String, val line: String)` 정의
- iOS `SearchQueryRecommendData.swift`(`queryName` / `stationName` / `line`) 대응
- Firebase JSON 역직렬화를 위한 수동 `DataSnapshot` 파싱에서 사용하는 모델

---

#### [x] Task 6 — `FirebaseDataSource.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/firebase/FirebaseDataSource.kt`
- 인터페이스 정의
- `suspend fun getSearchDefaultList(): List<String>?` — `SubwayWhen/SearchDefaultList` 노드 조회
- `suspend fun getSearchQueryRecommendList(): List<SearchQueryRecommendData>?` — `SubwayWhen/SearchQueryRecommendList/value` 노드 조회
- 실패 시 `null` 반환 계약 명시

---

#### [x] Task 7 — `FirebaseDataSourceImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/firebase/FirebaseDataSourceImpl.kt`
- `FirebaseDatabase` Hilt 주입
- `getSearchDefaultList`: `database.reference.child("SubwayWhen/SearchDefaultList").get().await()` → `DataSnapshot.getValue<List<String>>()`
- `getSearchQueryRecommendList`: `database.reference.child("SubwayWhen/SearchQueryRecommendList/value").get().await()` → DataSnapshot 각 child를 `SearchQueryRecommendData`로 수동 매핑(`queryName`, `stationName`, `line`)
- 실패 / 타입 불일치 → `try/catch` 내부에서 로그 남기고 `null` 반환

---

#### [x] Task 8 — `SearchRepository.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/repository/SearchRepository.kt`
- 인터페이스 정의
- `suspend fun searchStations(query: String): List<SearchStationInfo>`
- `suspend fun recommendStations(): List<String>`
- `suspend fun searchQueryRecommendList(): List<SearchQueryRecommendData>`

---

#### [x] Task 9 — `SearchRepositoryImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/repository/SearchRepositoryImpl.kt`
- `LoadModel`, `FirebaseDataSource` Hilt 주입
- `searchStations(query)`: `loadModel.stationSearch(query)` → `NetworkResult.Success`면 `row` 반환, 실패면 `emptyList()` 반환
- `recommendStations()`: `firebaseDataSource.getSearchDefaultList()` → `null`이면 `DEFAULT_RECOMMEND` 폴백 반환
- `searchQueryRecommendList()`: `firebaseDataSource.getSearchQueryRecommendList()` → `null`이면 `emptyList()` 반환 (폴백 없음)
- `companion object { val DEFAULT_RECOMMEND = listOf("강남", "교대", "선릉", "삼성", "을지로3가", "종각", "홍대입구", "잠실", "명동", "여의도", "가산디지털단지", "판교") }` — 폴백 리스트를 이 1곳에만 정의

---

#### [x] Task 10 — `FirebaseModule.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/FirebaseModule.kt`
- `@Provides fun provideFirebaseDatabase(): FirebaseDatabase` 추가
- `@Binds fun bindFirebaseDataSource(impl: FirebaseDataSourceImpl): FirebaseDataSource` 추가

---

#### [x] Task 11 — `RepositoryModule.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/RepositoryModule.kt`
- `@Binds fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository` 바인딩 추가

---

#### [x] Task 12 — `SearchRepositoryImplTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/data/repository/SearchRepositoryImplTest.kt`
- 검색 성공 시 결과 리스트 반환 테스트
- 검색 실패 시 `emptyList()` 반환 테스트
- Firebase `getSearchDefaultList()` 성공 시 해당 리스트 반환 테스트
- Firebase `getSearchDefaultList()` `null` 반환 시 `DEFAULT_RECOMMEND` 폴백 반환 테스트
- Firebase `getSearchQueryRecommendList()` `null` 반환 시 `emptyList()` 반환 테스트

---

### Phase 3. ViewModel + Contract

#### [x] Task 13 — `SearchContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchContract.kt`
- `data class SearchUiState` 정의:
  - `isSearchMode: Boolean = false`
  - `searchQuery: String = ""`
  - `isSearchLoading: Boolean = false`
  - `searchResult: List<SearchStationInfo> = emptyList()`
  - `recommendStations: List<String> = emptyList()`
  - `nowQueryRecommendList: List<SearchQueryRecommendData> = emptyList()`
  - `filteredQueryRecommendList: List<SearchQueryRecommendData> = emptyList()`
- `sealed interface SearchIntent` 정의:
  - `OnAppear` — 탭 재진입 처리용 (현 spec에서 noop)
  - `EnterSearchMode` — 검색 모드 진입
  - `ExitSearchMode` — 검색 모드 종료
  - `QueryChanged(text: String)` — 검색어 변경
  - `RecommendStationTapped(name: String)` — 자주 검색 셀 탭
  - `QueryRecommendStationTapped(item: SearchQueryRecommendData)` — "혹시 이 역을 찾으셨나요?" 셀 탭
  - `ResultStationTapped(item: SearchStationInfo)` — 결과 항목 탭 (본 spec 범위 밖, noop 자리)
- `sealed interface SearchEffect` — 본 spec 범위 내 effect 없음, 추후 화면 전환용 빈 sealed interface

---

#### [x] Task 14 — `SearchViewModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchViewModel.kt`
- `SearchRepository` Hilt 주입 추가
- `private val _searchQuery = MutableStateFlow("")` 추가
- `private val _internal = MutableStateFlow(SearchUiState())` 추가
- `val uiState: StateFlow<SearchUiState>` 노출 (`_internal.asStateFlow()`)
- `val effect: SharedFlow<SearchEffect>` 자리 추가 (빈 SharedFlow)
- `init` 블록:
  - `recommendStations()` 조회 후 `_internal.update { it.copy(recommendStations = ...) }`
  - `searchQueryRecommendList()` 조회 후 `_internal.update { it.copy(nowQueryRecommendList = ...) }`
  - `_searchQuery.debounce(700).distinctUntilChanged().onEach { performSearch(it) }.launchIn(viewModelScope)`
- `fun onIntent(intent: SearchIntent)` 구현:
  - `OnAppear` → noop
  - `EnterSearchMode` → `_internal.update { copy(isSearchMode = true) }`
  - `ExitSearchMode` → `_searchQuery.value = ""`; `_internal.update { copy(isSearchMode = false, searchQuery = "", searchResult = emptyList(), isSearchLoading = false) }`
  - `QueryChanged(text)` → `_searchQuery.value = text`; `_internal.update { copy(searchQuery = text, isSearchLoading = text.isNotEmpty()) }`
  - `RecommendStationTapped(name)` → `isSearchMode = true`, `searchQuery = name`, `isSearchLoading = true` 업데이트 + `_searchQuery.value = name`
  - `QueryRecommendStationTapped(item)` → `item.stationName`으로 `RecommendStationTapped`와 동일 패턴 처리
  - `ResultStationTapped(item)` → TODO 주석 + noop
- `private suspend fun performSearch(query: String)` 구현:
  - `query.isEmpty()` → `searchResult = emptyList()`, `isSearchLoading = false`, `filteredQueryRecommendList = emptyList()` 후 return
  - `nowQueryRecommendList.filter { it.queryName.contains(query) }`로 `filteredQueryRecommendList` 도출
  - `repository.searchStations(query)` 호출
  - `_internal.update { copy(searchResult = result, isSearchLoading = false, filteredQueryRecommendList = filtered) }`

---

#### [x] Task 15 — `SearchViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/search/SearchViewModelTest.kt`
- fake `SearchRepository` 구현 (MockK 또는 수동 fake)
- `runTest` + `StandardTestDispatcher` + `Turbine` 사용
- 초기 상태에 폴백 추천 목록 12개 노출 테스트
- `EnterSearchMode` 후 `isSearchMode == true` 테스트
- `QueryChanged("강남")` 후 700ms advanceTime → `searchResult`에 fake 결과 + `filteredQueryRecommendList` 필터링 결과 테스트
- `QueryChanged("")` → 결과 즉시 비워짐, `filteredQueryRecommendList` 비워짐, `isSearchLoading = false` 테스트
- `RecommendStationTapped("교대")` → `isSearchMode = true`, `searchQuery = "교대"`, 검색 트리거 테스트
- `QueryRecommendStationTapped(item)` → `item.stationName`으로 검색 트리거 테스트

---

### Phase 4. UI — Section 컴포저블

#### [x] Task 16 — `SearchTextField.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchTextField.kt`
- 파라미터: `isSearchMode: Boolean`, `query: String`, `onQueryChange: (String) -> Unit`, `onEnterSearchMode: () -> Unit`, `onExitSearchMode: () -> Unit`
- 비검색 모드: `MainBgCard` + `AnimatedTapBox` 위에 `Text("🔍 지하철역을 검색하세요.")` 표시 → 탭 시 `onEnterSearchMode` 호출
- 검색 모드: `MainBgCard` + 실제 `BasicTextField`(또는 `OutlinedTextField`) + 우측 "닫기" `Text`(클릭 시 `onExitSearchMode` 호출)
- 검색 모드 진입 시 `LaunchedEffect(isSearchMode)`에서 `FocusRequester.requestFocus()` 호출하여 자동 포커스
- placeholder 텍스트는 `stringResource(R.string.search_textfield_placeholder)` / `search_textfield_active_placeholder` 사용

---

#### [x] Task 17 — `SearchResultSection.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchResultSection.kt`
- 파라미터: `query: String`, `isLoading: Boolean`, `result: List<SearchStationInfo>`, `onItemClick: (SearchStationInfo) -> Unit`
- `MainBgCard` 내부 `Column`으로 구성
- 상단 헤더 텍스트 분기:
  - `isLoading == true` → `stringResource(R.string.search_result_loading)`
  - `query.isEmpty()` → `stringResource(R.string.search_result_input_required)`
  - 그 외 → `stringResource(R.string.search_result_count, result.size)`
- 본문 분기:
  - `isLoading == true` → `CircularProgressIndicator` 중앙 표시
  - `result.isEmpty() && query.isEmpty()` → `Text(stringResource(R.string.search_result_empty_query))`("💬")
  - `result.isEmpty() && query.isNotEmpty()` → `Text(stringResource(R.string.search_result_no_match))`
  - 결과 있음 → `result.forEach { item -> AnimatedTapBox { … } }`로 역명 + 호선 문자열 그대로 행 렌더링 (호선 컬러 매핑은 다음 spec)
- iOS `SearchStationResultView.swift` 대응

---

#### [x] Task 18 — `SearchQueryRecommendSection.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchQueryRecommendSection.kt`
- 파라미터: `items: List<SearchQueryRecommendData>`, `onItemClick: (SearchQueryRecommendData) -> Unit`
- `MainBgCard` 내부 `Column`으로 구성 (부모 스크롤 안에 있으므로 `LazyColumn` 사용 금지)
- 헤더 `Text(stringResource(R.string.search_query_recommend_title))` ("혹시 이 역을 찾으셨나요?")
- `items.forEach { AnimatedTapBox { Row { Text(item.line) + Text(item.stationName) } } }`
- `items.isEmpty()` 시 SearchScreen에서 호출 자체를 생략 (컴포저블 내부 분기 불필요)
- iOS `SearchQueryRecommendView.swift` 대응

---

#### [x] Task 19 — `SearchWordRecommendSection.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchWordRecommendSection.kt`
- 파라미터: `stations: List<String>`, `onItemClick: (String) -> Unit`
- `MainBgCard` 내부 `Column(verticalArrangement = Arrangement.spacedBy(...))`으로 구성
- 헤더 `Text(stringResource(R.string.search_word_recommend_title))` ("자주 검색되는 지하철역")
- `stations.chunked(2).forEach { pair -> Row { pair.forEach { name -> AnimatedTapBox(Modifier.weight(1f)) { Text(name) } } } }` — Row당 2개 셀
- `LazyVerticalGrid` 사용 금지 (부모 `verticalScroll` 안에서 무한 높이 측정 에러 발생)
- iOS `SearchWordRecommendView.swift` 대응

---

### Phase 5. UI — SearchScreen 통합 및 strings.xml

#### [x] Task 20 — `strings.xml` (수정)
**파일**: `app/src/main/res/values/strings.xml`
- `search_textfield_placeholder` = "🔍 지하철역을 검색하세요."
- `search_textfield_active_placeholder` = "지하철역을 검색하세요."
- `search_close` = "닫기"
- `search_result_loading` = "지하철역을 찾는 중이에요 🔍"
- `search_result_input_required` = "지하철역을 입력해주세요."
- `search_result_count` = "총 %1$d개의 검색 결과"
- `search_result_empty_query` = "💬"
- `search_result_no_match` = "검색된 지하철역이 없어요."
- `search_word_recommend_title` = "자주 검색되는 지하철역"
- `search_query_recommend_title` = "혹시 이 역을 찾으셨나요?"

---

#### [x] Task 21 — `SearchScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt`
- placeholder 본문 제거
- `val uiState by viewModel.uiState.collectAsStateWithLifecycle()` 추가
- `CommonTopBarScreen(title = stringResource(R.string.tab_search))` 내부 구성:
  - `SearchTextField(isSearchMode, searchQuery, onQueryChange, onEnterSearchMode, onExitSearchMode)`
  - `if (uiState.isSearchMode) { SearchResultSection(query, isLoading, result, onItemClick) }`
  - `if (uiState.isSearchMode && uiState.filteredQueryRecommendList.isNotEmpty()) { SearchQueryRecommendSection(filteredItems, onItemClick) }`
  - `// TODO: SearchVicinitySection — 다음 spec` 주석
  - `SearchWordRecommendSection(recommendStations, onItemClick = { vm.onIntent(RecommendStationTapped(it)) })`
- 섹션 간 간격: 2회 이상 반복되면 `Dimens.kt`에 `searchSectionGap` 토큰 추가 후 참조
- `@Preview` 4종 추가:
  - 비검색 모드 (추천 목록 노출)
  - 검색 모드 + 결과 있음
  - 검색 모드 + 결과 없음
  - 검색 모드 + 로딩 중

---

### Phase 6. 검증

#### [x] Task 22 — 테스트 & 빌드 검증
- `./gradlew test` 실행 → 신규 `SearchViewModelTest` + `SearchRepositoryImplTest` 통과, 기존 테스트 회귀 없음 확인
- 실기기/에뮬레이터 시나리오 확인:
  - 검색 탭 진입 → 자주 검색 12개 표시
  - 네트워크 오프라인 → 폴백 12개 정상 표시 (AC 1)
  - TextField 탭 → 검색 모드 진입, 키보드 자동 포커스
  - "강남" 입력 → 700ms 후 결과 리스트 + "혹시 이 역을 찾으셨나요?" 섹션
  - 결과 없는 검색어 입력 → "검색된 지하철역이 없어요." 표시 (AC 2)
  - "닫기" 탭 → 비검색 모드 복귀, query/result 초기화
  - 자주 검색 "교대" 탭 → 검색 모드 진입 + "교대" 검색 결과 즉시 노출

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공 (`./gradlew :app:assembleDebug`)
- [ ] 테스트 통과 (`./gradlew test`)
- [ ] 기존 테스트 회귀 없음
- [ ] 매직 문자열 없음 (모든 사용자 노출 문자열 `strings.xml` 처리)
- [ ] `google-services.json` 배치 완료

### 기능 (AC)
- [ ] 네트워크 미연결 상태에서 기본 추천 역 12개 표시
- [ ] 검색 결과 0건이어도 화면(빈 메시지 포함) 표시
- [ ] 검색 모드 진입 / 종료 토글 정상 동작
- [ ] 700ms 디바운스 후 검색 실행
- [ ] 자주 검색 셀 탭 시 검색 모드 진입 + 즉시 검색
- [ ] "혹시 이 역을 찾으셨나요?" 섹션 — Firebase 실패 시 숨김, 성공 시 쿼리 필터링 표시
- [ ] Firebase 통신 실패 시 폴백 정상 동작 (단위 테스트로 보장)
