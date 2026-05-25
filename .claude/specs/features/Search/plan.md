# Plan: Search

## 참조 Spec
- @specs/features/Search/spec.md

## 참조 iOS 원본
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Search/SearchView.swift` — 전체 화면 구성 (TextField + Vicinity + WordRecommend + Result)
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Search/SearchFeature.swift` — TCA Reducer (state/action/effect 매핑 원본)
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Search/Sub/SearchStationResultView.swift` — 결과 영역(로딩/빈/리스트) 분기
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Search/Sub/SearchWordRecommendView.swift` — 자주 검색 영역 (2열 그리드)
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Search/Sub/SearchQueryRecommendView.swift` — "혹시 이 역을 찾으셨나요?" 영역 (filteredQueryRecommendList 기반)
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhenNetworking/DataLoad/Entity/StationSearch/SearchQueryRecommendData.swift` — queryName/stationName/line 모델
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhenNetworking/DataLoad/LoadModel/LoadModel.swift` — `defaultViewListRequest()` Firebase Realtime DB `SubwayWhen/SearchDefaultList` 노드 + `searchQueryRecommendListRequest()` `SubwayWhen/SearchQueryRecommendList/value` 노드 참조
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Resource/Json/DetailStationIdList.plist` — 역명/호선 매핑 데이터 (검색 결과 표시에는 미사용)

## 참조 Skill
신규 화면 생성 시
- @skills/create-feature/SKILL.md

## 현재 상태 파악

### 재사용
- `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchViewModel.kt` — Tabbar spec에서 만든 빈 `@HiltViewModel`. 본 spec에서 상태/Intent/Effect를 채워 넣음.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt` — Tabbar spec에서 만든 placeholder. 본 spec에서 실제 화면으로 교체.
- `app/src/main/java/com/yslee/subwaywhen/ui/common/CommonTopBarScreen.kt` — iOS `NavigationBarScrollViewInSUI` 대응 스캐폴드. 검색 화면의 상단 타이틀 + 스크롤 컨테이너로 그대로 사용.
- `app/src/main/java/com/yslee/subwaywhen/ui/common/MainBgCard.kt` — iOS `MainStyleViewInSUI` 대응 카드 컨테이너. TextField/Result/WordRecommend 영역의 배경으로 사용.
- `app/src/main/java/com/yslee/subwaywhen/ui/common/AnimatedTapBox.kt` — iOS `AnimationButtonInSUI` 대응. 자주 검색 그리드 셀, 검색 결과 행, "닫기" 버튼에 사용.
- `app/src/main/java/com/yslee/subwaywhen/ui/common/StationLineCircle.kt` — iOS `StationTitleViewInSUI` 대응. 검색 결과의 호선 표시 원형(현재 spec 범위 내에서 사용 여부는 Phase 1에서 결정 — 호선 컬러 매핑 미구축이면 단순 텍스트로 폴백).
- `app/src/main/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModel.kt` — `stationSearch(stationName)`. 검색 API 호출에 그대로 재사용.
- `app/src/main/java/com/yslee/subwaywhen/data/remote/dto/stationSearch/SearchStationModel.kt` — `SearchStationInfo` DTO. 결과 항목 데이터로 그대로 사용.
- `app/src/main/java/com/yslee/subwaywhen/data/network/NetworkResult.kt` — 네트워크 결과 Sealed. Repository 결과 표현에 사용.
- `app/src/main/java/com/yslee/subwaywhen/ui/theme/Dimens.kt`, `Color.kt` — 폰트/패딩/색 토큰.
- `app/src/main/res/values/strings.xml` — 검색 화면 텍스트 추가 위치.

### 신규
- `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchContract.kt` — `SearchUiState` / `SearchIntent` / `SearchEffect` 정의.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchTextField.kt` — 상단 검색 입력/표시 컴포저블. 비검색모드(읽기전용 카드 + tap) ↔ 검색모드(실제 `TextField` + "닫기" 버튼) 토글.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchResultSection.kt` — 검색 결과 카드(로딩 / 빈 / 결과 리스트 분기). iOS `SearchStationResultView` 대응.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchVicinitySection.kt` — 가까운 지하철역 카드의 자리표시(EmptyView). 본 spec에서는 빈 컴포저블로 두고 위치만 잡음.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchWordRecommendSection.kt` — 자주 검색 카드(2열 Grid). iOS `SearchWordRecommendView` 대응.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchQueryRecommendSection.kt` — "혹시 이 역을 찾으셨나요?" 카드. iOS `SearchQueryRecommendView` 대응. 검색 모드에서 `filteredQueryRecommendList`가 비어있지 않을 때 표시.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/model/SearchResultItem.kt` — UI 표시용 모델(`stationName`, `lineLabel`). DTO를 그대로 노출하지 않기 위한 얇은 변환 모델. (단순 typealias로 충분하면 제거.)
- `app/src/main/java/com/yslee/subwaywhen/data/remote/dto/stationSearch/SearchQueryRecommendData.kt` — iOS `SearchQueryRecommendData` 대응. `queryName`, `stationName`, `line` 필드. Firebase JSON 역직렬화용 `@Serializable` 또는 수동 `DataSnapshot` 파싱.
- `app/src/main/java/com/yslee/subwaywhen/data/repository/SearchRepository.kt` — 인터페이스. `searchStations(query)`, `recommendStations()`, `searchQueryRecommendList()` 정의.
- `app/src/main/java/com/yslee/subwaywhen/data/repository/SearchRepositoryImpl.kt` — `LoadModel.stationSearch` 호출(검색) + Firebase Realtime DB `SubwayWhen/SearchDefaultList` 노드 1회 조회(추천 목록) + `SubwayWhen/SearchQueryRecommendList/value` 노드 1회 조회(쿼리 추천). 네트워크/Firebase 실패 시 폴백 반환.
- `app/src/main/java/com/yslee/subwaywhen/data/remote/firebase/FirebaseDataSource.kt` — Firebase Realtime DB 접근 추상화. `getSearchDefaultList(): List<String>?`, `getSearchQueryRecommendList(): List<SearchQueryRecommendData>?` 두 함수.
- `app/src/main/java/com/yslee/subwaywhen/data/remote/firebase/FirebaseDataSourceImpl.kt` — Firebase Realtime DB 구현체. `SubwayWhen/SearchDefaultList`와 `SubwayWhen/SearchQueryRecommendList/value` 경로를 각각 `get()`(단발 fetch)으로 조회.
- `app/src/test/java/com/yslee/subwaywhen/feature/search/SearchViewModelTest.kt` — onAppear/검색 디바운스/검색 결과/추천 탭 시 검색 모드 진입 시나리오 테스트.
- `app/src/test/java/com/yslee/subwaywhen/data/repository/SearchRepositoryImplTest.kt` — 추천 목록 폴백, 검색 실패 시 빈 리스트 반환 테스트.

### 수정
- `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt` — placeholder 본문 제거 후 `CommonTopBarScreen("검색") { … }` + 3개 섹션을 호출하는 본 화면으로 교체.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchViewModel.kt` — 빈 ViewModel에 `StateFlow<SearchUiState>` + `SharedFlow<SearchEffect>` + `onIntent(SearchIntent)` 구조 추가. `SearchRepository` 주입.
- `app/src/main/res/values/strings.xml` — 검색 화면 문자열 추가 (`search_textfield_placeholder`, `search_close`, `search_result_loading`, `search_result_input_required`, `search_result_empty_query`, `search_result_no_match`, `search_result_count`, `search_word_recommend_title`).
- `gradle/libs.versions.toml` — Firebase BOM + Realtime Database 의존성 추가(`firebase-bom`, `firebase-database-ktx`).
- `app/build.gradle.kts` — Firebase BOM/Realtime Database 의존성 + Google Services Gradle 플러그인(`com.google.gms.google-services`) 적용.
- `build.gradle.kts`(루트) — Google Services 플러그인 classpath / `plugins` 블록에 추가.
- `app/src/main/java/com/yslee/subwaywhen/di/RepositoryModule.kt` — `SearchRepositoryImpl` 바인딩 추가.
- `app/src/main/java/com/yslee/subwaywhen/di/NetworkModule.kt` 또는 신규 `FirebaseModule.kt` — `FirebaseDataSourceImpl` 바인딩 + `FirebaseDatabase` provider 추가. (관심사 분리상 신규 모듈 권장.)
- `app/google-services.json` — Firebase 콘솔에서 발급받은 설정 파일 배치(커밋 여부는 사용자 정책에 따름. spec에 명시되지 않았으므로 본 plan에서는 "추가 필요" 표시만 함).

### 삭제
- 없음. (Tabbar에서 만든 placeholder 본문은 "수정"으로 분류.)

## iOS → Android 매핑

| iOS | Android | 비고 |
|-----|---------|------|
| `NavigationBarScrollViewInSUI(title: "검색") { … }` | `CommonTopBarScreen(title = "검색") { … }` | 기존 공통 컴포저블 재사용 |
| `MainStyleViewInSUI { … }` | `MainBgCard { … }` | 기존 공통 카드 |
| `AnimationButtonInSUI { … }` | `AnimatedTapBox(...) { … }` | 기존 공통 탭 박스 |
| TCA `BindingReducer` + `@ObservableState searchQuery` | `SearchUiState.searchQuery` + `SearchIntent.QueryChanged(text)` | Compose에서 `TextField`의 `onValueChange`로 Intent 발행 |
| `case .isSearchMode(Bool)` | `SearchIntent.EnterSearchMode` / `SearchIntent.ExitSearchMode` | iOS 단일 액션을 의도가 명확한 2개로 분리 |
| `case .stationSearchRequest` + `.debounce(0.7s)` | ViewModel 내부 `viewModelScope.launch` + `delay(700)` 또는 `Flow.debounce(700)` 패턴 | `searchQuery`를 MutableStateFlow로 만들고 `debounce(700).collect`로 검색 트리거 |
| `case .recommendStationRequest` → `totalLoad.defaultViewListLoad()` | `SearchIntent.OnAppear` → `SearchRepository.recommendStations()` | Firebase Realtime DB 조회 |
| `case .searchQueryRecommendListRequest` → `totalLoad.searchQueryRecommendListLoad()` | `SearchIntent.OnAppear` → `SearchRepository.searchQueryRecommendList()` | Firebase `SubwayWhen/SearchQueryRecommendList/value` 단발 fetch. 앱 진입 시 1회 로드 후 보관. |
| `state.filteredSearchQueryRecommendList = nowList.filter { $0.queryName.contains(query) }` | ViewModel `performSearch(query)` 내부 인메모리 필터링 | 별도 API 호출 없음. `nowQueryRecommendList.filter { it.queryName.contains(query) }` |
| `SearchQueryRecommendView` ("혹시 이 역을 찾으셨나요?") | `SearchQueryRecommendSection` | 검색 모드 + filteredQueryRecommendList 비어있지 않을 때 표시 |
| `case .stationTapped(.searchQueryRecommend)` | `SearchIntent.QueryRecommendStationTapped(item)` | `item.stationName`으로 검색 실행 |
| `SearchVicinityView` (위치 기반) | `SearchVicinitySection` (빈 컴포저블) | spec 명시 "View는 Empty로 처리" |
| `case .stationTapped(.recommend)` | `SearchIntent.RecommendStationTapped(name)` | `searchQuery` 설정 + 검색 모드 진입 + 즉시 검색 |
| `case .searchResultTapped(Int)` | **현 spec 범위 밖** — 결과 탭 시 액션은 표시만 하고 실제 상세 push는 다음 spec | spec "검색화면의 기본만 구성" |
| Firebase Realtime DB `SubwayWhen/SearchDefaultList` | `FirebaseDatabase.reference.child("SubwayWhen/SearchDefaultList").get()` | 단발 fetch(`get()`), `observe(.value)`는 사용 안함 — 본 spec에서 실시간 구독 불필요 |
| `Analytics.logEvent("SerachVC_Search", …)` | **본 spec 범위 밖** — Firebase Analytics는 별도 spec | spec 명시 없음 |

## 기술적 결정사항

- **검색 디바운스는 `MutableStateFlow<String>` + `debounce(700)` + `distinctUntilChanged()` + `flatMapLatest`로 구현한다.**
  - 이유: iOS `case .stationSearchRequest.debounce(id: Key.searchDelay, for: 0.7, scheduler: DispatchQueue.main)`와 동일한 효과를 Coroutines Flow 관용 패턴으로 표현. `flatMapLatest`가 in-flight 검색을 자동 취소.
  - 대안: `viewModelScope.launch { delay(700); search() }` + `Job.cancel()` 수동 관리 → 코드량 증가, race 조건 위험. 채택 안 함.

- **`SearchUiState`는 sealed interface가 아닌 단일 `data class`로 정의한다.**
  - 이유: 화면 전체가 항상 동일 레이아웃(타이틀 + 카드 3개)을 그리고, 영역별로 부분 상태(로딩/빈/리스트)가 다르므로 화면 단위의 `Loading/Success/Error` 분기로 묶기엔 적합하지 않다.
  - 컨벤션 `Loading/Success/Error` 규칙은 "화면 자체의 상태 흐름"을 의미하며, 본 화면처럼 항상 본문이 그려지는 경우엔 단일 `data class` + 내부 플래그(`isSearchMode: Boolean`, `isSearchLoading: Boolean`, `searchResult: List<SearchResultItem>`, `recommendStations: List<String>`)가 적합.
  - 대안: `sealed interface SearchUiState { Loading, Success(...) }` → 추천 목록만 늦게 도착해도 화면 전체가 Loading으로 빠지는 부작용. 채택 안 함.

- **Firebase 추천 목록은 초기 상태에 하드코딩 폴백으로 채워두고, 통신 성공 시 갱신한다.**
  - 이유: spec Acceptance Criteria "네트워크에 연결되지 않아도 기본 추천 역은 보여야 함" 충족. Repository는 실패 시 같은 폴백 리스트를 반환하고, ViewModel은 단순히 받아 그린다.
  - 폴백 값: `["강남", "교대", "선릉", "삼성", "을지로3가", "종각", "홍대입구", "잠실", "명동", "여의도", "가산디지털단지", "판교"]` (spec.md, iOS `SearchFeature.State.nowRecommendStationList`와 동일).
  - 폴백 리스트는 `SearchRepositoryImpl` 내부 `companion object`에 1군데만 둔다(ViewModel 중복 금지).

- **Firebase 의존성은 Firebase BOM + Realtime Database KTX를 추가한다.**
  - 이유: iOS는 Realtime DB(`FirebaseDatabase.database().reference`)에 `SubwayWhen/SearchDefaultList` 경로로 저장돼 있어 Android에서도 동일 경로를 단발 `get()`으로 조회. Firestore가 아님에 유의.
  - Google Services Gradle 플러그인 + `google-services.json` 필요. 본 spec 구현 전 사용자가 Firebase 콘솔에서 Android 앱 등록 후 파일 제공 필요(완료 조건에 표시).
  - 대안: Firebase Realtime DB REST(`https://<project>.firebaseio.com/SubwayWhen/SearchDefaultList.json`) 호출을 Ktor로 직접 → SDK가 제공하는 인증/규칙/오프라인 캐시 이점 포기. 채택 안 함. (단, 사용자가 SDK 도입을 원치 않을 경우 폴백 안으로 보유.)

- **`FirebaseDataSource`를 별도 추상화로 분리한다.**
  - 이유: Repository가 Firebase SDK에 직접 의존하면 단위 테스트가 어려움(SDK는 Robolectric 또는 instrumented test 필요). 인터페이스로 추상화 시 테스트에서 fake로 대체.
  - 인터페이스 함수: `suspend fun getSearchDefaultList(): List<String>?` + `suspend fun getSearchQueryRecommendList(): List<SearchQueryRecommendData>?`. 실패 시 `null` 반환.

- **`nowQueryRecommendList`는 앱 진입 시 1회 로드하고, 필터링은 인메모리에서 수행한다.**
  - 이유: iOS와 동일 — `searchQueryRecommendListRequest`는 앱 시작 시 1회 fetch 후 `filteredSearchQueryRecommendList = now.filter(query)`로만 사용. 검색어 변경마다 Firebase 호출하지 않음.
  - `SearchUiState`에 `nowQueryRecommendList: List<SearchQueryRecommendData>` 추가. `filteredQueryRecommendList`는 `performSearch` 내 인메모리 필터로 도출.
  - Firebase 실패 시 `nowQueryRecommendList = emptyList()` → "혹시 이 역을 찾으셨나요?" 섹션은 항상 비어 숨김 (폴백 없음 — spec 불변 조건 충족: 화면은 무조건 표출).

- **검색 결과의 호선 표시(line)는 본 spec에서는 텍스트 그대로 노출한다.**
  - 이유: iOS `SearchStationInfo.line` (`"01호선"` 형태) → `useLine`("1호선")으로 변환하려면 `SubwayLineData` 매핑 enum 이식이 필요한데, 본 spec 범위가 검색 화면의 기본 구성이므로 매핑 enum은 다음 spec(Detail 또는 SubwayLineData 공통 spec)에서 다룬다.
  - 화면 표시: `StationLineCircle` 대신 `SearchStationInfo.line` 문자열을 간단히 노출하거나, 색 매핑 없이 `StationLineCircle(title=line, lineColor=null)`을 사용해 회색 폴백으로 그린다. Phase 2에서 결정.
  - 대안: 본 spec에 `SubwayLineData` enum까지 포함 → 범위 초과. 가이드라인 2 위반. 채택 안 함.

- **자주 검색 그리드는 Compose `LazyVerticalGrid` 대신 `Column` + 2등분 `Row`로 구성한다.**
  - 이유: `LazyVerticalGrid`는 자체 스크롤을 요구하지만, 본 화면은 부모 `CommonTopBarScreen`의 `verticalScroll(scrollState)` 안에 있다. `LazyVerticalGrid`를 verticalScroll 안에 두면 무한 높이 측정 에러가 발생.
  - 12개 정도의 정적 아이템이므로 `chunked(2)` + `Row` 반복으로 충분. 성능 이슈 없음.
  - 대안: `LazyVerticalGrid(modifier.heightIn(max = …))` → 높이 하드코딩이 필요해 토큰 규칙(2회 이상 반복)과 맞지 않음. 채택 안 함.

- **TextField는 두 가지 모드를 한 컴포저블 내에서 토글한다(iOS `matchedGeometryEffect` 흉내는 생략).**
  - 비검색 모드: `AnimatedTapBox` 위에 `Text("🔍 지하철역을 검색하세요.")`만 표시, 탭하면 `SearchIntent.EnterSearchMode` 발행.
  - 검색 모드: 실제 `OutlinedTextField`/`BasicTextField` + 우측 "닫기" 버튼. 진입 시 `LaunchedEffect(isSearchMode)`에서 `FocusRequester.requestFocus()`.
  - `matchedGeometryEffect`의 시각적 전환은 Compose에서 `SharedTransitionLayout` 등으로 구현 가능하지만 spec 요구 사항이 아니므로 생략(가이드라인 2).

- **결과 영역의 분기 텍스트는 `strings.xml`에 추가한다.**
  - 텍스트 목록(iOS와 동일 의미):
    - `search_textfield_placeholder` = "🔍 지하철역을 검색하세요." (검색 모드 전 placeholder)
    - `search_textfield_active_placeholder` = "지하철역을 검색하세요." (검색 모드 TextField placeholder)
    - `search_close` = "닫기"
    - `search_result_loading` = "지하철역을 찾는 중이에요 🔍"
    - `search_result_input_required` = "지하철역을 입력해주세요."
    - `search_result_count` = "총 %1$d개의 검색 결과"
    - `search_result_empty_query` = "💬"
    - `search_result_no_match` = "검색된 지하철역이 없어요."
    - `search_word_recommend_title` = "자주 검색되는 지하철역"
    - `search_query_recommend_title` = "혹시 이 역을 찾으셨나요?"
  - 이유: 매직 문자열 금지 컨벤션. 다국어 대응은 본 spec 범위 밖이지만 토큰화 자체는 비용 거의 없음.

- **에러 경로는 silent fail.**
  - spec 명시 "네트워크 에러 → 기본 처리", "로컬 데이터 저장 에러 → 기본 처리", 불변 조건 "통신/저장 실패해도 화면은 무조건 표출".
  - 결과: 네트워크 실패 시 Toast/Dialog 노출 없음. 검색 실패 → 빈 결과로 처리. Firebase 실패 → 폴백 리스트 사용. ViewModel은 `NetworkResult.Failure`를 빈 리스트로 매핑.
  - 로그는 기존 `AppLogger.e`로 남긴다(추후 디버깅용). 사용자에게는 보이지 않음.

- **`searchQuery`를 `MutableStateFlow`로 두고, `SearchUiState`는 `combine`으로 합성한다.**
  - 이유: TextField의 onValueChange는 매 키 입력마다 호출되므로 매번 `_uiState.update { … }`보다 별도 Flow가 효율적이고 디바운스 적용이 자연스럽다.
  - 패턴:
    - `private val _searchQuery = MutableStateFlow("")`
    - `init { _searchQuery.debounce(700).distinctUntilChanged().flatMapLatest { performSearch(it) }.launchIn(viewModelScope) }`
    - `uiState`는 `_internalState` + `_searchQuery`를 `combine`해 노출.
  - 대안: 단일 `_uiState`만 두고 `update`로 매번 갱신 → 디바운스 적용을 위해 별도 Job/cancel 수동 관리 필요. 채택 안 함.

- **테스트는 ViewModel 단위 테스트 + Repository 단위 테스트만 진행한다.**
  - Compose UI 테스트는 본 spec 범위 밖(설정 비용 대비 가치 낮음, Tabbar spec과 동일 결정).
  - ViewModel 테스트: `kotlinx-coroutines-test`의 `runTest` + `StandardTestDispatcher` + `Turbine`으로 `uiState` flow 검증. fake `SearchRepository` 주입.
  - Repository 테스트: fake `FirebaseDataSource` + fake `LoadModel`로 폴백/실패 경로 검증.

## 구현 순서

### Phase 1. 의존성 추가 및 Firebase 셋업
- `gradle/libs.versions.toml`: `firebase-bom`, `firebase-database-ktx`, `google-services`(plugin) 추가.
- 루트 `build.gradle.kts`: `id("com.google.gms.google-services") version "x.y.z" apply false` 추가.
- `app/build.gradle.kts`:
  - `plugins { alias(libs.plugins.google.services) }` 추가
  - `dependencies { implementation(platform(libs.firebase.bom)); implementation(libs.firebase.database.ktx) }` 추가
- `app/google-services.json` 배치 (사용자가 Firebase 콘솔에서 생성한 파일 필요).
- 빌드 검증: `./gradlew :app:assembleDebug` 통과.

### Phase 2. 데이터 레이어 — Firebase DataSource & Search Repository
- `data/remote/dto/stationSearch/SearchQueryRecommendData.kt`: `data class SearchQueryRecommendData(val queryName: String, val stationName: String, val line: String)`.
- `data/remote/firebase/FirebaseDataSource.kt`: 인터페이스.
  - `suspend fun getSearchDefaultList(): List<String>?`
  - `suspend fun getSearchQueryRecommendList(): List<SearchQueryRecommendData>?`
- `data/remote/firebase/FirebaseDataSourceImpl.kt`:
  - `FirebaseDatabase` 주입.
  - `getSearchDefaultList`: `database.reference.child("SubwayWhen/SearchDefaultList").get().await()` → `DataSnapshot.getValue<List<String>>()`.
  - `getSearchQueryRecommendList`: `database.reference.child("SubwayWhen/SearchQueryRecommendList/value").get().await()` → DataSnapshot을 `List<SearchQueryRecommendData>`로 수동 매핑(각 child → `queryName`, `stationName`, `line`).
  - 실패/타입 불일치 → `null` 반환 (try/catch 내부, 로그만 남김).
- `data/repository/SearchRepository.kt`:
  - `suspend fun searchStations(query: String): List<SearchStationInfo>`
  - `suspend fun recommendStations(): List<String>`
  - `suspend fun searchQueryRecommendList(): List<SearchQueryRecommendData>`
- `data/repository/SearchRepositoryImpl.kt`:
  - `LoadModel`, `FirebaseDataSource` 주입.
  - `searchStations`: `loadModel.stationSearch(query)` 호출 → `Success`면 `row` 반환, 실패면 빈 리스트.
  - `recommendStations`: `firebaseDataSource.getSearchDefaultList()` → null이면 `DEFAULT_RECOMMEND` 폴백.
  - `searchQueryRecommendList`: `firebaseDataSource.getSearchQueryRecommendList()` → null이면 `emptyList()` (폴백 없음).
  - `companion object { val DEFAULT_RECOMMEND = listOf("강남", ...) }`.
- DI:
  - `FirebaseModule.kt` 신규: `provideFirebaseDatabase()` + `bindFirebaseDataSource` 바인딩.
  - `RepositoryModule.kt`: `bindSearchRepository` 추가.
- 단위 테스트: `SearchRepositoryImplTest` — 통신 성공/실패, Firebase null/성공, `searchQueryRecommendList` null 시 emptyList 반환 케이스.

### Phase 3. ViewModel + Contract
- `feature/search/SearchContract.kt`:
  - `data class SearchUiState(isSearchMode, searchQuery, isSearchLoading, searchResult, recommendStations, nowQueryRecommendList, filteredQueryRecommendList)`.
    - `filteredQueryRecommendList`는 `nowQueryRecommendList.filter { it.queryName.contains(searchQuery) }` 로 도출 — 별도 필드로 UiState에 포함시키거나 ViewModel에서 파생 프로퍼티로 노출(어느 방식이든 Screen에서 직접 필터 호출 금지).
    - 초기값: `isSearchMode=false, searchQuery="", isSearchLoading=false, searchResult=emptyList(), recommendStations=emptyList(), nowQueryRecommendList=emptyList(), filteredQueryRecommendList=emptyList()`.
  - `sealed interface SearchIntent { OnAppear; EnterSearchMode; ExitSearchMode; QueryChanged(text); RecommendStationTapped(name); QueryRecommendStationTapped(item: SearchQueryRecommendData); ResultStationTapped(item) }`
  - `sealed interface SearchEffect { }` — 본 spec 범위 내 effect 없음(빈 sealed interface는 유지). 추후 화면 전환용 자리만 마련.
- `feature/search/SearchViewModel.kt`:
  - `@HiltViewModel class SearchViewModel @Inject constructor(repository: SearchRepository): ViewModel()`.
  - `private val _searchQuery = MutableStateFlow("")`
  - `private val _internal = MutableStateFlow(SearchUiState(...))`
  - `val uiState: StateFlow<SearchUiState> = _internal.stateIn(...)` (단일 source가 충분하면 `_internal`만 노출).
  - `init {`
    - `viewModelScope.launch { _internal.update { it.copy(recommendStations = repository.recommendStations()) } }`
    - `viewModelScope.launch { _internal.update { it.copy(nowQueryRecommendList = repository.searchQueryRecommendList()) } }`
    - `_searchQuery.debounce(700).distinctUntilChanged().onEach { performSearch(it) }.launchIn(viewModelScope)`
    - `}`
  - `fun onIntent(intent: SearchIntent)`:
    - `OnAppear` — Tabbar 재진입 처리(현 spec에서는 noop. recommend 재로딩이 필요하면 추가).
    - `EnterSearchMode` — `_internal.update { copy(isSearchMode = true) }`.
    - `ExitSearchMode` — `_searchQuery.value = ""`; `_internal.update { copy(isSearchMode = false, searchQuery = "", searchResult = emptyList(), isSearchLoading = false) }`.
    - `QueryChanged(text)` — `_searchQuery.value = text`; `_internal.update { copy(searchQuery = text, isSearchLoading = text.isNotEmpty()) }`.
    - `RecommendStationTapped(name)` — `_internal.update { copy(isSearchMode = true, searchQuery = name, isSearchLoading = true) }`; `_searchQuery.value = name` (debounce를 건너뛰고 즉시 검색하려면 별도 함수 호출 — iOS는 `.send(.stationSearchRequest)` 직접 호출).
    - `QueryRecommendStationTapped(item)` — `item.stationName`을 query로 설정 후 즉시 검색. `RecommendStationTapped`와 동일 패턴.
    - `ResultStationTapped(item)` — 본 spec 범위 밖. TODO 주석 + noop.
  - `private suspend fun performSearch(query: String)`:
    - `query.isEmpty()` → `_internal.update { copy(searchResult = emptyList(), isSearchLoading = false, filteredQueryRecommendList = emptyList()) }`; return.
    - `val filtered = _internal.value.nowQueryRecommendList.filter { it.queryName.contains(query) }`
    - `val result = repository.searchStations(query)`
    - `_internal.update { copy(searchResult = result.map { it.toUi() }, isSearchLoading = false, filteredQueryRecommendList = filtered) }`
- 단위 테스트: `SearchViewModelTest`:
  - 초기 상태에 폴백 추천 목록 노출.
  - `EnterSearchMode` 후 `isSearchMode == true`.
  - `QueryChanged("강남")` 후 700ms 진행 → `searchResult`에 fake 결과 + `filteredQueryRecommendList` 필터링 결과.
  - `QueryChanged("")` → 결과 즉시 비워짐, `filteredQueryRecommendList` 비워짐, loading false.
  - `RecommendStationTapped("교대")` → `searchMode = true`, `searchQuery = "교대"`, 검색 트리거.
  - `QueryRecommendStationTapped(item)` → `item.stationName`으로 검색 트리거.

### Phase 4. UI — Section 컴포저블
- `feature/search/component/SearchTextField.kt`:
  - 파라미터: `isSearchMode: Boolean`, `query: String`, `onQueryChange: (String) -> Unit`, `onEnterSearchMode: () -> Unit`, `onExitSearchMode: () -> Unit`.
  - `Row` 내부에 `MainBgCard` + (TextField 또는 placeholder Text).
  - 검색 모드 시 우측에 "닫기" `Text` 클릭 가능(`Modifier.clickable`).
  - 검색 모드 진입 시 `FocusRequester`로 자동 포커스.
- `feature/search/component/SearchResultSection.kt`:
  - 파라미터: `query: String`, `isLoading: Boolean`, `result: List<SearchResultItem>`, `onItemClick: (SearchResultItem) -> Unit`.
  - `MainBgCard` 내부 `Column`:
    - 상단 헤더 텍스트: 로딩 시 `search_result_loading`, query 빈 경우 `search_result_input_required`, 그 외 `search_result_count`(count formatting).
    - 본문 분기:
      - `isLoading == true` → `CircularProgressIndicator` 중앙 표시.
      - `result.isEmpty()` → `query.isEmpty() ? "💬" : "검색된 지하철역이 없어요."`.
      - 그 외 → `result.forEach { item -> AnimatedTapBox { ... } }`로 행 렌더링.
- `feature/search/component/SearchQueryRecommendSection.kt`:
  - 파라미터: `items: List<SearchQueryRecommendData>`, `onItemClick: (SearchQueryRecommendData) -> Unit`.
  - `MainBgCard` 내부 `LazyColumn` (또는 `Column` — 부모 스크롤 안에 있을 경우 Column 사용).
    - 헤더 `Text("혹시 이 역을 찾으셨나요?")`.
    - `items.forEach { AnimatedTapBox { Row { StationLineCircle(line) + Text(stationName) } } }`.
  - `filteredQueryRecommendList.isEmpty()` 이면 호출 자체 안 함 (SearchScreen에서 조건부 노출).
- `feature/search/component/SearchVicinitySection.kt`:
  - 본 spec에서는 빈 `Spacer(Modifier.height(0.dp))` 또는 빈 `MainBgCard`로 자리만 잡고 추후 spec에서 채움. (spec 명시 "View는 Empty로 처리".)
  - 또는 아예 호출 자체를 생략. 결정: 호출 생략(가이드라인 3, 불필요한 빈 컴포저블 만들지 않음).
  - 결정 갱신: SearchScreen에서 if (!isSearchMode) 분기 하에 향후 추가될 자리만 주석으로 남김.
- `feature/search/component/SearchWordRecommendSection.kt`:
  - 파라미터: `stations: List<String>`, `onItemClick: (String) -> Unit`.
  - `MainBgCard` 내부 `Column(verticalArrangement = spacedBy)`:
    - 헤더 `Text(stringResource(R.string.search_word_recommend_title))`.
    - `stations.chunked(2).forEach { pair -> Row { pair.forEach { AnimatedTapBox(Modifier.weight(1f)) { Text(it) } } } }`.

### Phase 5. UI — SearchScreen 통합
- `feature/search/SearchScreen.kt`:
  - `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`.
  - `CommonTopBarScreen(title = stringResource(R.string.tab_search)) {`
    - `Spacer(Modifier.height(Dimens.paddingTB))`
    - `SearchTextField(isSearchMode, query, onQueryChange = { vm.onIntent(QueryChanged(it)) }, onEnter = { vm.onIntent(EnterSearchMode) }, onExit = { vm.onIntent(ExitSearchMode) })`
    - `if (uiState.isSearchMode) { SearchResultSection(...) }`
    - `if (uiState.isSearchMode && uiState.filteredQueryRecommendList.isNotEmpty()) { SearchQueryRecommendSection(uiState.filteredQueryRecommendList, onItemClick = { vm.onIntent(QueryRecommendStationTapped(it)) }) }`
    - `// TODO: SearchVicinitySection — 다음 spec`
    - `SearchWordRecommendSection(uiState.recommendStations, onItemClick = { vm.onIntent(RecommendStationTapped(it)) })`
  - `}`
  - 섹션 간 간격은 `Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingTB * 2))` 또는 각 섹션 외부 `Modifier.padding(top = ...)`. 2회 이상 반복되면 `Dimens`에 토큰 추가(`searchSectionGap` 등).
- `@Preview(Light/Dark)`:
  - 비검색 모드(추천 목록 노출).
  - 검색 모드 + 결과 있음.
  - 검색 모드 + 결과 없음.
  - 검색 모드 + 로딩.

### Phase 6. 검증
- `./gradlew test` 통과 (신규 ViewModel/Repository 테스트 + 기존 테스트 회귀 없음).
- 실기기/에뮬레이터에서 시나리오 확인:
  - 검색 탭 진입 → "🔍 지하철역을 검색하세요." 표시 + 자주 검색 12개 표시.
  - 네트워크 오프라인 상태에서도 동일하게 폴백 12개 표시(AC 1 충족).
  - TextField 탭 → 검색 모드 진입, 자주 검색 영역은 그대로 노출(spec 명세 재확인 필요 — iOS는 자주 검색을 검색 모드에서도 노출하므로 동일하게 유지).
  - "강남" 입력 → 700ms 후 결과 리스트 표시 + queryName에 "강남" 포함된 추천 역 "혹시 이 역을 찾으셨나요?" 섹션 표시.
  - "ㅁㅁㅁㅁㅁ" 입력 → "검색된 지하철역이 없어요." 표시 + "혹시 이 역을 찾으셨나요?" 섹션 숨김(AC 2 충족).
  - "닫기" 탭 → 비검색 모드 복귀, query/result 초기화.
  - 자주 검색 "교대" 탭 → 검색 모드 진입 + "교대" 검색 결과 즉시 노출.

## 완료 조건
- [ ] Spec Acceptance Criteria 충족
  - [ ] 네트워크 미연결 상태에서 기본 추천 12개 표시
  - [ ] 검색 결과 0건이어도 화면(빈 메시지 포함) 표시
- [ ] 검색 모드 진입/종료 토글 동작
- [ ] 700ms 디바운스 후 검색 실행
- [ ] 자주 검색 셀 탭 시 검색 모드 진입 + 즉시 검색
- [ ] 검색어 입력 시 "혹시 이 역을 찾으셨나요?" 섹션 필터링 표시 (Firebase 실패 시 섹션 숨김)
- [ ] 매직 문자열 없음 (모든 사용자 노출 문자열 `strings.xml`)
- [ ] Firebase 통신 실패 시 폴백 정상 동작 (단위 테스트로 보장)
- [ ] 빌드 성공, 기존 테스트 회귀 없음
- [ ] `google-services.json` 배치 완료 (사용자 측 준비 항목)
