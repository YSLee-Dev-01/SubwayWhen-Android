# Plan: Search-3 (가까운 지하철역 찾기)

## 참조 Spec
- @specs/features/Search-3/spec.md

## 참조 Skill
신규 화면 생성 시
- @skills/create-feature/SKILL.md

---

## 현재 상태 파악

### 신규
- `core/location/LocationManager.kt` (interface) + `core/location/LocationManagerImpl.kt`
    - iOS `Service/Location/LocationManager.swift` 대응
    - API: `suspend fun locationAuthCheck(): Boolean`, `suspend fun locationAuthRequest(): Boolean`, `suspend fun locationRequest(): LocationData?`
    - 권한 확인은 `ContextCompat.checkSelfPermission`, 권한 요청은 Compose 측 `rememberLauncherForActivityResult`에서 처리(Manager는 결과만 받음). 본 plan은 Manager는 "현재 권한 상태 확인 + 위치 조회"만 수행하고, 권한 요청 트리거는 UI 레이어에서 `ActivityResultContracts.RequestPermission` 사용.
    - 위치 조회는 `FusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()` 사용 (kotlinx-coroutines-play-services는 이미 의존성에 존재)
- `core/location/LocationData.kt` — `data class LocationData(val lat: Double, val lon: Double)` (iOS 1:1 대응)
- `data/remote/dto/vicinityStation/VicinityTransformData.kt`
    - iOS `VicinityTransformData.swift` 포팅
    - 필드: `id`, `name`, `line`, `distance`
    - 파생: `lineColorName(String)`, `lineName(String)` (iOS 동일 로직)
- `data/repository/VicinityRepository.kt` (interface) + `VicinityRepositoryImpl.kt`
    - `suspend fun loadVicinityStations(x: Double, y: Double): List<VicinityTransformData>`
    - iOS `TotalLoadModel.vicinityStationsDataLoad`의 변환(`SW8` 필터 + 거리 오름차순 정렬 + name/line/distance 분리) 그대로 포팅
    - `LoadModel.vicinityStationsLoad` (이미 존재) 호출
    - station name/line 분리·distance 표시 변환은 내부 helper로 처리
- `feature/search/vicinity/SearchVicinityContract.kt`
    - `VicinityUiState`(`authStatus: VicinityAuthStatus`, `isVicinityLoading`, `vicinityStations: List<VicinityTransformData>`, `tappedIndex: Int?`, `upLive`, `downLive`, `liveLoading: Pair<Boolean, Boolean>`, `lastSearchTime: Long?`, `showRefreshCooldownDialog: Boolean`, `errorDialog: String?`, `isLocationModalVisible`)
    - `VicinityAuthStatus { Unknown, Denied, Granted }`
    - `VicinityIntent { OnAppear, AuthRequestTapped, AuthResultReceived(Boolean), VicinityRefreshTapped, StationTapped(Int?), LiveRefreshTapped, ListModalOpenTapped, ListModalDismissed, ListStationTapped(Int), DialogDismissed }`
    - `VicinityEffect { SearchStation(name: String) }` — 역 모달 자동 띄우기용
- `feature/search/vicinity/SearchVicinityViewModel.kt`
    - LocationManager, VicinityRepository, (실시간) LiveStationRepository(또는 LoadModel)로부터 데이터 수집
    - 5분 쿨다운(`lastSearchTime + 5*60*1000`) 체크
    - 권한 상태에 따른 분기 처리
    - 9호선 상하행 반전 처리 (`subwayLineUpDownText` 활용)
- `feature/search/vicinity/SearchVicinitySection.kt`
    - iOS `SearchVicinityView.swift` 대응 메인 Composable
    - 권한 없음/거부/없음+빈/있음+빈/있음+목록/선택됨 5상태 분기 렌더링
    - 좌우 스크롤(`LazyRow`)로 가까운 역 목록 표출 (탭 전: `StationLineCircle` 사용, 탭 후 비선택 항목: 작은 컬러 바 + 역명)
- `feature/search/vicinity/component/VicinityStationDetailCard.kt`
    - 선택된 역의 실시간 정보 카드 (좌-가운데-우 3분할 레이아웃)
    - 가운데 역명(`StationLineCircle` size 65), 좌측 상행/내선·우측 하행/외선 라인 + 도착 메시지
    - 하단 5개 아이콘 액션(닫기, 새로고침, 임시보기, 추가하기, 신고하기) — 닫기/새로고침/추가하기만 실제 연동, 나머지는 TODO
- `feature/search/vicinity/component/VicinityEmptyState.kt` — "현재 가까운 지하철역이 없어요." 표시
- `feature/search/vicinity/component/VicinityLoading.kt` — `CircularProgressIndicator` 중앙 배치
- `feature/search/vicinity/modal/LocationListModal.kt`
    - iOS `LocationModalVC` 대응. `CommonModalBottomSheet` 위에 `LazyColumn`으로 가까운 역 목록 표출
    - 셀: 좌측 호선(원형 또는 라운드 라벨) + 가운데 역명 + 우측 거리(km)
    - 권한이 없는 경우: 안내 텍스트 + 닫기 버튼만 (`Report.json` 로띠는 옵션, 없으면 텍스트만)
- `feature/search/vicinity/modal/LocationListModalContract.kt`(필요 시 — 단순 stateless라면 생략) — Compose-state로 충분하면 별도 ViewModel 만들지 않음

### 재사용
- `ui/common/MainBgCard.kt` — Vicinity 섹션 컨테이너 배경 (iOS `MainStyleViewInSUI` 대응)
- `ui/common/StationLineCircle.kt` — 역 원형 표시(사이즈 45/65 두 케이스)
- `ui/common/SubwayLineMapper.kt`
    - `subwayLineColor` / `subwayLineDisplayName` / `subwayLineUpDownText` / `subwayLineCode` / `subwayLineIsService` 활용
    - 9호선 상하행 반전 로직은 ViewModel 측에서 처리
- `ui/common/modal/CommonModalBottomSheet.kt` — LocationListModal 컨테이너
- `ui/common/modal/ModalSubButton.kt` — "목록으로 확인하기" 버튼, 닫기 버튼
- `ui/common/AnimatedTapBox.kt` — 탭 인터랙션
- `data/remote/loadmodel/LoadModel.vicinityStationsLoad` — Kakao 근접역 API 이미 구현됨
- `data/remote/dto/vicinityStation/VicinityStationsData.kt` — DTO 이미 존재
- `data/remote/dto/liveArrival/LiveStationModel.kt` — 실시간 도착 DTO
- `data/remote/loadmodel/LoadModel.stationArrivalRequest` — 실시간 도착 API 이미 구현됨
- `data/remote/dto/stationSearch/SearchStationInfo.kt` — 역 모달 띄우기용 (`SaveStationModal`이 요구하는 타입)
- `data/repository/SearchRepository.searchStations` — 역명으로 역 정보 조회 (모달 연결용)
- `feature/search/modal/SaveStationModal.kt` — 셀(LocationListModal에서 역 탭) 시 결과로 띄울 모달

### 수정
- `app/src/main/AndroidManifest.xml`
    - `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />` 추가
    - `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />` 추가
- `gradle/libs.versions.toml`
    - `play-services-location` 추가 (FusedLocationProviderClient용). 버전: 21.3.0 정도. `kotlinx-coroutines-play-services`는 이미 존재.
- `app/build.gradle.kts`
    - `implementation(libs.play.services.location)` 추가
- `di/AppModule.kt` 또는 신규 `di/LocationModule.kt`
    - `LocationManager` 바인딩
    - `FusedLocationProviderClient` 프로바이더 (`LocationServices.getFusedLocationProviderClient(context)`)
- `di/RepositoryModule.kt`
    - `bindVicinityRepository(impl): VicinityRepository`
- `feature/search/SearchScreen.kt`
    - 기존 `// TODO: SearchVicinitySection — 다음 spec` 위치에 `SearchVicinitySection(...)` 삽입
    - `SaveStationModal`로 띄울 트리거를 Vicinity 측에서도 사용할 수 있도록 SearchViewModel에 새 Intent 라우팅(아래 참고)
- `feature/search/SearchContract.kt`
    - `SearchIntent`에 `VicinityRequestSaveModal(stationName: String)` 추가 (LocationListModal에서 셀 탭 시 호출)
    - 또는: `SaveStationModal` 트리거를 Vicinity 모듈이 자체적으로 보유 (분리 권장). 결정은 기술적 결정사항 참고.

### 삭제
- 없음

---

## 기술적 결정사항

- **Vicinity는 SearchScreen 안의 독립 섹션으로 분리**: 별도 `feature/search/vicinity/` 패키지 + 자체 ViewModel.
    - 사유: 위치 권한 / GPS / 5분 쿨다운 / 실시간 도착정보 등 도메인 로직이 무거움. SearchViewModel과 합치면 상태가 비대해짐. SearchScreen에서는 `SearchVicinitySection()`만 호출.
    - 대안: SearchViewModel에 모두 통합 — 제외 (단일 책임 위배, 테스트 부담).

- **위치 권한 요청 흐름**: Compose `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` 사용. Manager는 권한 "확인"과 "위치 조회"만 담당, 권한 요청 트리거는 Composable이 담당하고 결과를 `VicinityIntent.AuthResultReceived(Boolean)`로 ViewModel에 전달.
    - 사유: Android 권장 패턴. ViewModel에서 Activity context를 잡고 있지 않게 함.
    - 대안: Manager 내부에서 Activity context 잡고 권한 요청 — 제외 (Activity 수명주기 의존, 메모리/테스트 부담).

- **위치 조회 SDK 선택**: Play Services의 `FusedLocationProviderClient.getCurrentLocation()`.
    - 사유: 표준, 정확도 높음, suspend 변환(`.await()`) 지원. iOS의 `CLLocationManager.startUpdatingLocation()` 1회 대응.
    - 대안: `android.location.LocationManager` (system) — 제외 (Play Services가 사실상 표준).

- **5분 쿨다운**: ViewModel 상태 `lastSearchTime: Long?`을 `System.currentTimeMillis()`로 저장. 갱신 시 비교.
    - iOS는 `Date.addingTimeInterval(300)`. Android는 `lastSearchTime + 5 * 60 * 1000L < now`.
    - 쿨다운 미충족 시 `showRefreshCooldownDialog`를 true로 → Composable에서 `AlertDialog` 표시.
    - 사유: 시간 의존 로직은 ViewModel 내부의 `Clock`/`() -> Long` 람다로 주입하면 테스트 가능. (생성자에 `private val nowMillis: () -> Long = { System.currentTimeMillis() }` 형태로 두면 테스트 시 가짜 클럭 주입 가능)
    - 대안: 별도 클래스로 분리 — 과한 추상화 (Simplicity First).

- **실시간 도착 정보 로드**: 신규 Repository로 분리할지 vs SearchRepository에 메서드 추가할지.
    - 결정: Vicinity 전용 Repository(`VicinityRepository`)에 `loadLiveArrival(stationName: String, line: String)` 메서드 포함. iOS는 `totalLoad.singleLiveAsyncData`로 한 번에 가져옴. 본 plan에선 `LoadModel.stationArrivalRequest` 응답을 받아 상행/하행 분리 후 반환.
    - 9호선 분기: ViewModel에서 처리 (iOS와 동일하게 line.upDownText(isUp: true/false)로 결정).
    - 본 spec에서는 신규 데이터/스케줄/시간표 등 추가 노출이 없으므로 별도 `LiveArrivalRepository`를 만들지 않고 VicinityRepository에 합침.
    - **다음 spec(Detail 등) 도입 시 분리 검토**.

- **`SearchStationInfo` 변환 (실시간 → SaveStationModal 띄우기)**:
    - iOS는 `stationAddBtnTapped` 시 `searchQuery = station.name`으로 검색을 트리거하고 결과의 lineCode가 매칭되는 인덱스를 찾아 모달 표시.
    - Android는 동일 흐름: VicinityViewModel이 effect로 `SearchStation(name)` 발행 → SearchScreen 측에서 SearchViewModel에 위임 → 검색 결과 도착 후 lineCode 매칭 SearchStationInfo로 모달 띄움.
    - 단순화 옵션: `SearchRepository.searchStations(name)` 호출 결과를 VicinityViewModel 내부에서 직접 받고 lineCode 매칭 후 `SaveStationModal`을 위한 `SearchStationInfo`를 selectedStation 상태로 보관.
    - **결정**: VicinityViewModel이 `SearchRepository`까지 알게 되면 책임이 커짐 → effect(`VicinityEffect.SearchStation(name)`) 발행 후 SearchScreen에서 `SearchViewModel`에 위임. SearchViewModel은 기존 `RecommendStationTapped`와 유사하게 검색 모드 진입 + 검색 → 결과의 첫 매칭(또는 lineCode 매칭) 항목을 `selectedStation`으로 자동 세팅.
    - **단순화 결정**: 본 spec은 "검색하기를 누르면 해당 역명을 검색 한 후 결과가 나온 경우 Modal을 띄움"으로 명세. line 매칭 정확도까지 요구하지 않으므로 첫 결과 매칭(`searchResult.firstOrNull()`)을 `selectedStation`으로 세팅. iOS의 line 매칭은 추후 개선.

- **LocationListModal의 "권한 없는 경우" 안내 처리**:
    - iOS는 같은 모달 안에서 권한 여부에 따라 분기. Android도 동일하게 한 컴포저블 내부에서 `authStatus`로 분기 렌더.
    - 권한이 없으면 셀 목록 대신 안내 텍스트 + 닫기 버튼만 표시. (iOS Lottie Report 애니메이션 대응 자산이 없으면 텍스트만)
    - 모달 자체 트리거는 spec "위치 권한을 거부한 경우 Modal로 권한 설정이 되어 있지 않다는 것을 표출" + "목록으로 확인하기 버튼" 두 경로.

- **9호선 상하행 반전**: iOS `SubwayLineData.upDownText(isUp:)`에서 9호선만 반전. Android는 `subwayLineUpDownText` 함수가 이미 존재하나 9호선 분기 미포함. **본 spec에서 9호선 처리 필요**.
    - 확인 결과: 현 `subwayLineUpDownText`는 2호선만 "내선/외선"이고 9호선은 일반 "상행/하행"으로 반환. iOS의 `(line != .nine && firstData.upDown == "상행") || (line == .nine && firstData.upDown == "하행")` 로직은 라이브 데이터 매핑 단계에서 처리되는 것이므로 helper는 그대로 두고 VicinityViewModel에서 직접 분기.

- **임시 보기 / 신고하기**: spec 명시 "현재는 임시 보기, 신고 하기를 제외하고 연동". → UI는 그리되 클릭 시 빈 람다(`onTap = {}`) + TODO 주석.

- **2호선 내선/외선 처리**: 이미 `subwayLineUpDownText`가 처리. VicinityViewModel은 그대로 사용.

- **DisposableView**는 이미 Search-2에서 만들어진 placeholder가 있음. 본 spec에서는 Vicinity 카드 하단 5버튼의 "임시 보기"가 비활성화 상태이므로 추가 작업 없음.

---

## 구현 순서

### Phase 1. 인프라(권한·위치 SDK)
- `AndroidManifest.xml`에 `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` 추가
- `libs.versions.toml`에 `play-services-location` 21.3.0 추가
- `app/build.gradle.kts`에 `implementation(libs.play.services.location)` 추가
- `core/location/LocationData.kt` 생성
- `core/location/LocationManager.kt` interface + `LocationManagerImpl.kt` 구현
    - `locationAuthCheck()`: PackageManager로 `ACCESS_FINE_LOCATION` 확인
    - `locationRequest()`: `FusedLocationProviderClient.getCurrentLocation(...).await()` → `LocationData?` 반환(예외 시 null)
- DI: `LocationModule` 또는 `AppModule`에 바인딩
- 검증: `LocationManagerImpl` 단위 테스트는 어려움(Play Services 의존). Manager interface만 정의하고 ViewModel 테스트에서 `mockk<LocationManager>()`로 대체.

### Phase 2. DTO·Repository
- `data/remote/dto/vicinityStation/VicinityTransformData.kt` 생성 (iOS 1:1)
- `data/repository/VicinityRepository.kt` interface + `VicinityRepositoryImpl.kt`
    - `loadVicinityStations(x, y)`: `LoadModel.vicinityStationsLoad` → 필터(`category == "SW8"`) → 거리 정렬 → `VicinityTransformData` 매핑
    - `loadLiveArrival(stationName)`: `LoadModel.stationArrivalRequest` → `List<RealtimeStationArrival>`
    - station name/line 분리, distance(m → km) 변환 helper 추가
- `di/RepositoryModule.kt`에 `bindVicinityRepository` 추가
- 검증: `VicinityRepositoryImplTest`
    - 카카오 응답에서 `SW8` 카테고리만 통과, 거리 오름차순 정렬, name/line 분리 정확성
    - 빈 응답·통신 실패 시 emptyList 반환

### Phase 3. ViewModel·Contract
- `feature/search/vicinity/SearchVicinityContract.kt` 생성
- `feature/search/vicinity/SearchVicinityViewModel.kt` 생성
    - 의존성: `LocationManager`, `VicinityRepository`, (선택) `FirebaseAnalytics`, `() -> Long` clock
    - `onAppear`: 권한 체크 → 권한 있고 빈 목록이면 위치 요청 → 근접역 로드
    - `AuthResultReceived`: 결과에 따라 위치 요청 또는 모달(거부 시) 트리거
    - `VicinityRefreshTapped`: 5분 쿨다운 체크 → 미달 시 `showRefreshCooldownDialog=true` / 충족 시 재로드
    - `StationTapped(index)`: 미지원 노선(`subwayLineIsService==false`) 시 errorDialog 세팅, 아니면 `tappedIndex` 설정 후 실시간 도착 로드 (상/하행 두 번)
    - `LiveRefreshTapped`: 실시간 도착 재요청
    - `ListModalOpenTapped` / `ListModalDismissed`
    - `ListStationTapped(index)`: `VicinityEffect.SearchStation(name)` 발행 + 모달 닫기
- 검증: `SearchVicinityViewModelTest`
    - 권한 없음 → AuthRequest 흐름
    - 권한 거부 → ListModal 띄우기
    - 5분 쿨다운 미달 시 `showRefreshCooldownDialog=true`
    - 5분 쿨다운 충족 시 위치 재로드
    - 미지원 노선 탭 시 errorDialog 세팅
    - 9호선 상행/하행 매핑 정합성
    - 실시간 로딩 상태 토글

### Phase 4. UI Composable
- `feature/search/vicinity/component/VicinityEmptyState.kt`
- `feature/search/vicinity/component/VicinityLoading.kt`
- `feature/search/vicinity/component/VicinityStationRow.kt` (LazyRow 항목 — `StationLineCircle` 사이즈 45, isFill=true)
- `feature/search/vicinity/component/VicinityStationRowMini.kt` (탭 후 비선택 항목 — 컬러 라운드 바 + 텍스트)
- `feature/search/vicinity/component/VicinityStationDetailCard.kt` (선택된 역의 실시간 카드 + 하단 5버튼)
- `feature/search/vicinity/modal/LocationListModal.kt`
    - 권한 상태에 따라 본문 분기
- `feature/search/vicinity/SearchVicinitySection.kt`
    - `MainBgCard` 안에 헤더(타이틀+서브타이틀+새로고침 아이콘) + LazyRow + (선택 시) DetailCard or (미선택 시) "목록으로 확인하기" 버튼
    - `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` 등록
    - `LaunchedEffect`로 `viewModel.effect` 수집 (`SearchStation(name)` 도달 시 callback 호출)
    - `Dimens`에 반복 사용되는 값 2회 이상 시 추가 (예: `vicinityCircleSize=45.dp`, `vicinityCircleSizeLarge=65.dp`)
- 검증: Preview — 5상태 모두 (권한 없음/거부 모달/권한 있음 로딩/권한 있음 빈/권한 있음 목록/역 선택됨) 캡처

### Phase 5. SearchScreen 통합
- `SearchScreen` 내 `// TODO: SearchVicinitySection — 다음 spec` 자리에 `SearchVicinitySection(onStationSearch = { ... })` 삽입
- `SearchViewModel`에 `VicinityRequestSaveModal(name: String)` 또는 기존 `RecommendStationTapped`과 유사한 핸들러를 통해 검색 후 자동 모달 트리거
    - 신규 Intent: `SearchIntent.VicinityStationSelected(stationName: String)`
    - 처리: 검색 모드 진입 + `_searchQuery.value = name` + 검색 → 결과 도착 시 첫 결과를 `selectedStation`으로 자동 세팅
    - 결과 자동 모달 띄우기 위해 `performSearch` 종료 후 `vicinityAutoOpen` 같은 transient 플래그 필요 (Simple 결정: VicinityViewModel이 직접 SearchRepository를 1회 호출하지 않고 SearchViewModel에 위임)
    - 단순화: SearchScreen이 `onStationSearch` 콜백을 받고 `onIntent(SearchIntent.VicinityStationSelected(name))` 호출. ViewModel은 검색 결과 첫 항목을 `selectedStation`으로 세팅.
- 검증: 통합 동작
    - 권한 요청 다이얼로그 → 허용 → 가까운 역 목록 표시
    - 역 탭 → 실시간 도착 카드 표시
    - 새로고침 5분 미만 → 다이얼로그
    - "목록으로 확인하기" → 모달에서 셀 탭 → SaveStationModal 표출

### Phase 6. Dimens·문자열·정리
- 반복 사용 dimens 추가 (vicinity 관련 사이즈 2회 이상)
- 안내 문구 `strings.xml` 등록 ("가까운 지하철역 찾기", "역을 누르면 실시간 정보를 확인할 수 있어요.", "현재 가까운 지하철역이 없어요.", "가까운 지하철역 찾기 기능은 5분에 한번씩 조회할 수 있어요.\n%d초 후에 다시 시도해주세요.", "위치 권한이 설정되어 있지 않아요." 등)
- import 정리, lint, Preview 다크/라이트 모두 점검

---

## 완료 조건

- [ ] Spec Acceptance Criteria 충족
    - [ ] 위치 권한 미요청 상태: 안내 타이틀 + 권한 요청 버튼 노출, 탭 시 시스템 권한 다이얼로그
    - [ ] 위치 권한 거부 상태: LocationListModal에서 "위치 권한이 설정되어 있지 않아요." 안내 + 닫기 버튼
    - [ ] 위치 권한 허용 + 주변 역 있음:
        - [ ] 가까운 순서대로 좌우 스크롤 LazyRow에 `StationLineCircle`(노선색 배경, 사이즈 45)로 역명 표출
        - [ ] 새로고침 아이콘 탭 → 위치/근접역 재로드, 5분 내 재탭 시 안내 다이얼로그
        - [ ] "목록으로 확인하기" 버튼 → LocationListModal에 호선/역명/거리 km 표시
        - [ ] 모달 내 셀 탭 → 해당 역명으로 검색 후 SaveStationModal 자동 표출
        - [ ] LazyRow 역 탭 → 실시간 도착 카드 (좌:상행/내선, 우:하행/외선, 중앙 큰 역명원)
        - [ ] 카드 하단 5버튼 중 닫기/새로고침/추가하기 동작, 임시보기·신고하기는 비활성(TODO)
        - [ ] 닫기 → 선택 해제 (LazyRow 복귀)
        - [ ] 새로고침 → 실시간 도착 재요청
        - [ ] 추가하기 → 해당 역명으로 검색 후 SaveStationModal 자동 표출
    - [ ] 위치 권한 허용 + 주변에 역 없음: "현재 가까운 지하철역이 없어요." 텍스트만 표시
    - [ ] 위치 에러(GPS 실패 등) 발생해도 Vicinity 섹션의 영역(헤더 + 본문)은 유지 — Loading 종료 후 빈 상태 표시
- [ ] 단위 테스트
    - [ ] `VicinityRepositoryImplTest` (SW8 필터, 거리 정렬, 변환)
    - [ ] `SearchVicinityViewModelTest` (권한 분기, 5분 쿨다운, 미지원 노선, 9호선 상하행 매핑)
- [ ] 9호선 상하행 매핑 정합성 (iOS 동작 패리티)
- [ ] Compose Preview 다크/라이트 정상 (5상태)
- [ ] Dimens 2회 이상 반복 토큰 등록
- [ ] AndroidManifest 위치 권한 추가, play-services-location 의존성 추가
