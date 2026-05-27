# Tasks: Search-2 (검색 결과 저장 Modal)

## 참조
- spec: `.claude/specs/features/Search-2/spec.md`
- plan: `.claude/specs/features/Search-2/plan.md`

## Task 목록

### Phase 1. 모델/매퍼 레이어

#### [x] Task 1 — `LineToKorailCodeMapper.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/modal/LineToKorailCodeMapper.kt`
- iOS `ModalModel.useLineTokorailCode()` 포팅 — `lineToKorailCode(line: String): String` 단일 최상위 함수
- 매핑: `"경의중앙"→"K4"`, `"수인분당"→"K1"`, `"경춘"→"K2"`, `"우이"→"UI"`, `"신분당"→"D1"`, `"공항"→"A1"`, 그 외 `""`
- 입력값은 `subwayLineDisplayName()`이 반환하는 약어 표기 기준 (예: `"경의선"` 아닌 `"경의중앙"`)

---

#### [x] Task 2 — `SubwayLineMapper.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/SubwayLineMapper.kt`
- 기존 `subwayLineDisplayName`, `subwayLineColor` 함수 아래에 `subwayLineUpDownText(line: String, isUp: Boolean): String` 추가
- 2호선(`"02호선"`)만 `"내선"/"외선"`, 그 외 모든 호선은 `"상행"/"하행"` 반환
- iOS `upDownText(isUp:)` 확장 함수 동작과 동일

---

#### [x] Task 3 — 단위 테스트: `LineToKorailCodeMapperTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/search/modal/LineToKorailCodeMapperTest.kt`
- Kotest `FunSpec` 스타일 사용
- 경의중앙→K4, 수인분당→K1, 경춘→K2, 우이→UI, 신분당→D1, 공항→A1 정상 매핑 검증
- 1호선, 2호선 등 일반 호선 → `""` 반환 검증

---

#### [x] Task 4 — 단위 테스트: `SubwayLineMapperTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/ui/common/SubwayLineMapperTest.kt`
- Kotest `FunSpec` 스타일 사용
- `subwayLineUpDownText("02호선", isUp=true)` → `"내선"` 검증
- `subwayLineUpDownText("02호선", isUp=false)` → `"외선"` 검증
- `subwayLineUpDownText("01호선", isUp=true)` → `"상행"` 검증
- `subwayLineUpDownText("01호선", isUp=false)` → `"하행"` 검증

---

### Phase 2. Modal ViewModel / Contract

#### [x] Task 5 — `SaveStationModalContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/modal/SaveStationModalContract.kt`
- `SaveStationModalUiState` data class 정의
  - `group: SaveStationGroup = SaveStationGroup.ONE`
  - `exceptionLastStation: String = ""`
  - `station: SearchStationInfo` (nullable 아님, 모달 진입 시 초기값으로 설정)
- `SaveStationModalIntent` sealed interface 정의
  - `GroupToggled` — 출근/퇴근 토글
  - `ExceptionChanged(text: String)` — 중간 종착역 TF 입력값 변경
  - `UpButtonTapped` — 상행(또는 내선) 저장 버튼 탭
  - `DownButtonTapped` — 하행(또는 외선) 저장 버튼 탭
  - `NotServiceTapped` — 비서비스 노선 버튼 탭
  - `DisposableUpTapped` — DisposableView 상행 버튼 탭 (일회성)
  - `DisposableDownTapped` — DisposableView 하행 버튼 탭 (일회성)
  - `Dismissed` — 모달 드래그/백드롭으로 닫힘
- `SaveStationModalEffect` sealed interface 정의
  - `SaveCompleted` — 저장 성공 → SearchScreen에서 저장 완료 모달 표출 트리거
  - `AlreadyExists` — 중복 저장 시도 → AlertDialog 표출 트리거
  - `Close` — 모달 닫기
  - `DisposableDetailNavigate(station: SearchStationInfo, isUp: Boolean)` — 일회성 상세 진입 (TODO 처리)

---

#### [x] Task 6 — `SaveStationModalViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/modal/SaveStationModalViewModel.kt`
- `@HiltViewModel` + `@Inject constructor(private val localDataRepository: LocalDataRepository)` 선언
- `_uiState: MutableStateFlow<SaveStationModalUiState>`, `_effect: MutableSharedFlow<SaveStationModalEffect>` 관리
- `fun initStation(station: SearchStationInfo)` — SearchScreen에서 모달 표출 시 초기 station 설정 (Composable 파라미터로 전달하는 방식 대신 초기화 함수 사용)
- `onIntent(intent: SaveStationModalIntent)` 처리:
  - `GroupToggled`: `ONE ↔ TWO` 토글
  - `ExceptionChanged`: `exceptionLastStation` 업데이트
  - `UpButtonTapped` / `DownButtonTapped`: 아래 저장 로직 수행
  - `NotServiceTapped`: `Close` Effect 발행
  - `DisposableUpTapped` / `DisposableDownTapped`: `DisposableDetailNavigate` Effect 발행 (TODO 주석 포함)
  - `Dismissed`: `Close` Effect 발행
- 저장 로직 (`UpButtonTapped` / `DownButtonTapped` 공통):
  1. `subwayLineUpDownText(line, isUp)` 로 `updnLine` 결정
  2. `lineToKorailCode(subwayLineDisplayName(line))` 로 `korailCode` 결정
  3. `localDataRepository.saveSetting.value.searchOverlapAlert == true` 이면 중복 체크:
     - `saveStations` 리스트에서 `stationName`, `updnLine`, `lineCode` 모두 일치하는 항목이 있으면 `AlreadyExists` Effect 발행 후 return
  4. 중복 없으면 새 `SaveStation(id=UUID, stationName, stationCode, updnLine, line, lineCode, group, exceptionLastStation, korailCode)` 생성
  5. `localDataRepository.updateSaveStations(기존 list + 새 항목)` 호출
  6. `SaveCompleted` Effect 발행

---

#### [x] Task 7 — 단위 테스트: `SaveStationModalViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/search/modal/SaveStationModalViewModelTest.kt`
- Kotest `FunSpec` + Turbine + MockK + `UnconfinedTestDispatcher` 사용
- `FakeLocalDataRepository` 구현 (인라인 object 또는 별도 클래스)
- 테스트 케이스:
  1. **신규 저장 성공**: `UpButtonTapped` 시 `updateSaveStations`가 기존 list + 새 `SaveStation`으로 호출됨
  2. **중복 저장 차단**: `searchOverlapAlert=true` 설정에서 동일 stationName/updnLine/lineCode 중복 시 `AlreadyExists` Effect 발행, `updateSaveStations` 미호출
  3. **중복 허용**: `searchOverlapAlert=false` 설정에서 동일 역도 `SaveCompleted` Effect 발행
  4. **그룹 토글**: `GroupToggled` → ONE→TWO, 재호출 시 TWO→ONE
  5. **중간 종착역 입력**: `ExceptionChanged("잠실,선릉")` 후 저장 시 `exceptionLastStation="잠실,선릉"` 으로 저장됨

---

### Phase 3. Modal UI Composable

#### [x] Task 8 — `DisposableView.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/modal/component/DisposableView.kt`
- iOS `DisposableView.swift` 대응 임시 UI Composable
- 레이아웃: 좌측 타이틀 Text("저장하지 않고 일회성으로 볼 수 있어요.") + 우측 상행 버튼/하행 버튼 2개 (`Row` + `Spacer`)
- 배경: `AppIconColor`에 해당하는 앱 주조색(`MaterialTheme.colorScheme.primary`) + alpha 0.7, `cornerRadius` 적용
- 상행 버튼: `ModalSubButton` 또는 작은 `Box`, 배경 `Color.Red.copy(alpha=0.5)`, 텍스트 `upText` 파라미터 수신
- 하행 버튼: 배경 `Color.Blue.copy(alpha=0.5)`, 텍스트 `downText` 파라미터 수신
- 버튼 탭 콜백: `onUpTapped: () -> Unit`, `onDownTapped: () -> Unit` 파라미터
- 탭 동작 구현체는 호출부에서 TODO 람다로 전달 (View 자체에는 로직 없음)
- Compose Preview 1개 (Light)

---

#### [x] Task 9 — `SaveStationModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/modal/SaveStationModal.kt`
- `@Composable fun SaveStationModal(station: SearchStationInfo, viewModel: SaveStationModalViewModel, onDismiss: () -> Unit)` 선언
- `viewModel.uiState` collect
- `viewModel.effect` collect: `SaveCompleted`/`AlreadyExists`/`Close`/`DisposableDetailNavigate` 처리 → 상위 `onDismiss()` 콜백 또는 내부 Dialog 상태 관리
- `Box`로 전체 래핑: `CommonModalBottomSheet`와 `DisposableView`를 겹쳐 배치
  - `DisposableView`는 BottomSheet 상단 외부(`Alignment.TopCenter` + 음수 offset 또는 BottomSheet 내부 최상단 Row)에 배치
  - iOS `disposableView.snp.makeConstraints{ $0.bottom.equalTo(mainBG.snp.top).offset(-paddingTB) }` 대응
- `CommonModalBottomSheet` 파라미터:
  - `mainTitle = "지하철 역 추가"`
  - `subTitle = "그룹, 제외 행을 선택 후 상/하행 버튼을 누르면 저장할 수 있어요."`
  - `onDismiss = { viewModel.onIntent(SaveStationModalIntent.Dismissed) }`
- BottomSheet 본문 레이아웃 (iOS ModalVC 배치 기준):
  - 상단 Row: `StationLineCircle(line=station.line)` + `Text(station.stationName, style=bold/large)`
  - 중간 Row (2열): 출퇴근 토글 `ModalSubButton` (좌측) + 중간 종착역 `OutlinedTextField` (우측, placeholder="중간 종착역 제거 (1개이상 콤마 이용)")
  - 하단 Row (2열): 상행/내선 `ModalSubButton`(빨강, 좌측) + 하행/외선 `ModalSubButton`(파랑, 우측)
  - `lineCode == ""` (비서비스 노선) 분기: 2열 버튼/출퇴근/TF 대신 `ModalSubButton(text="해당 노선은 서비스를 지원하지 않아요.", bgColor=Color.Black)` 풀와이드 1개만 표시
- 상행/하행 버튼 텍스트: `subwayLineUpDownText(station.line, isUp)` 사용
- 중복 저장(`AlreadyExists` Effect) 시: `AlertDialog`("이미 저장된 지하철역이에요.") → 확인 버튼 → 모달 유지
- Compose Preview 3개: 일반(1호선), 2호선(내선/외선), 비서비스 노선

---

### Phase 4. 저장 완료 Modal + 축하 애니메이션

#### [x] Task 10 — `SaveCompletedModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/modal/component/SaveCompletedModal.kt`
- `@Composable fun SaveCompletedModal(onConfirm: () -> Unit)` 선언
- `CommonModalBottomSheet` 사용:
  - `mainTitle = "저장 완료"`
  - `subTitle = "지하철 역이 저장되었어요."`
  - `onDismiss = onConfirm`
- 본문: `LottieAnimation(composition, iterations=1)` — `R.raw.tutorial_success` 사용, 크기 100dp, `Alignment.Center`
- `confirmButton` 슬롯: `ModalSubButton(text="확인", ...)` — 탭 시 `onConfirm()` 호출
- `LottieCompositionSpec.RawRes(R.raw.tutorial_success)` + `rememberLottieComposition()` 사용
- Compose Preview 1개 (Light)

---

#### [x] Task 11 — SearchScreen에 축하 애니메이션 오버레이 추가
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt`
- `SearchScreenContent` 의 최상위 컨테이너를 `Box`로 교체
- `uiState.isSaveCompletedModalVisible == true` 일 때 `LottieAnimation(R.raw.congratulations, iterations=1)` 를 `Modifier.fillMaxSize().zIndex(Float.MAX_VALUE)`로 풀스크린 오버레이
- `SaveCompletedModal`도 같은 조건에서 표출
- 애니메이션은 1회 재생(`iterations=1`), 재생 완료 후 자동 제거 불필요 (확인 버튼 탭으로만 닫힘)

---

### Phase 5. SearchContract / SearchViewModel / SearchScreen 통합

#### [x] Task 12 — `SearchContract.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchContract.kt`
- `SearchUiState`에 필드 추가:
  - `selectedStation: SearchStationInfo? = null` — 저장 모달 표출 대상
  - `isSaveCompletedModalVisible: Boolean = false` — 저장 완료 모달/축하 애니메이션 표출 여부
- `SearchIntent`에 추가:
  - `ModalDismissed` — 저장 모달이 Close Effect로 닫힐 때
  - `SaveCompletedDismissed` — 저장 완료 모달 확인 버튼 탭 시

---

#### [x] Task 13 — `SearchViewModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchViewModel.kt`
- `ResultStationTapped` 처리: `_internal.update { it.copy(selectedStation = intent.item) }`
- `QueryRecommendStationTapped` 처리: 기존 검색어 갱신 로직 유지 + `selectedStation = intent.item` 설정
- `ModalDismissed` 처리: `_internal.update { it.copy(selectedStation = null) }`
- `SaveCompletedDismissed` 처리: `_internal.update { it.copy(isSaveCompletedModalVisible = false) }`
- `SaveStationModalEffect.SaveCompleted` 수신 경로 연결: `SaveStationModal`에서 Effect를 받아 SearchScreen이 `SearchIntent`로 변환하는 방식 (ViewModel 간 직접 참조 없이 콜백으로 처리)

---

#### [x] Task 14 — `SearchScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt`
- `SearchScreenContent` 최상위를 `Box`로 교체 (Task 11 축하 애니메이션 오버레이 추가 포함)
- `uiState.selectedStation != null` 조건: `SaveStationModal` 호출
  - `onDismiss = { onIntent(SearchIntent.ModalDismissed) }`
  - `SaveStationModalEffect.SaveCompleted` 수신 시 → `onIntent(SearchIntent.ModalDismissed)` + `_internal`의 `isSaveCompletedModalVisible = true` 갱신은 콜백 `onSaveCompleted: () -> Unit` 파라미터로 상위 전달
- `uiState.isSaveCompletedModalVisible == true` 조건: `SaveCompletedModal` 호출
  - `onConfirm = { onIntent(SearchIntent.SaveCompletedDismissed) }`
- `SaveStationModal`의 `viewModel` 인스턴스: `hiltViewModel<SaveStationModalViewModel>()` 으로 생성, `LaunchedEffect(station)` 로 `viewModel.initStation(station)` 호출
- Preview 기존 4개 유지 (변경 없음)

---

### Phase 6. 의존성/리소스 점검

#### [x] Task 15 — `app/build.gradle.kts` Lottie 의존성 확인
**파일**: `app/build.gradle.kts`
- `dependencies` 블록에 `implementation(libs.lottie.compose)` 선언 여부 확인
- 미선언 시 추가 (libs.versions.toml에 이미 `lottie-compose 6.6.6` 정의 존재)

---

#### [x] Task 16 — DI 모듈: `SaveStationModalViewModel` Hilt 등록 확인
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/RepositoryModule.kt` (또는 `AppModule.kt`)
- `SaveStationModalViewModel`이 `@HiltViewModel`이므로 별도 모듈 수정 불필요 여부 확인
- `LocalDataRepository` 바인딩이 이미 `RepositoryModule`에 존재하는지 확인, 없으면 추가

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공
- [x] 단위 테스트 통과 (`LineToKorailCodeMapperTest`, `SubwayLineMapperTest`, `SaveStationModalViewModelTest`)
- [ ] Compose Preview 다크/라이트 정상 렌더링 (`SaveStationModal`, `SaveCompletedModal`, `DisposableView`)

### 기능 (AC)
- [ ] 검색 결과 또는 추천 역 탭 시 `SaveStationModal`이 표출되고, 해당 역의 호선 색/호선명/역명이 올바르게 표시됨
- [ ] 출근/퇴근 버튼 토글 정상 동작 (`ONE ↔ TWO`)
- [ ] 중간 종착역 TF에 콤마 구분 입력값이 저장 시 `exceptionLastStation`에 그대로 반영됨
- [ ] 상행/하행 버튼 탭 시 `SaveStation`이 `LocalDataRepository`를 통해 DataStore에 저장됨
- [ ] 2호선만 "내선/외선", 그 외 노선은 "상행/하행"으로 표기됨
- [ ] 비서비스 노선(`lineCode == ""`)은 "해당 노선은 서비스를 지원하지 않아요." 버튼만 표시됨
- [ ] 저장 완료 후 `SaveStationModal` 닫힘 → `SaveCompletedModal` 표출 + `congratulations.json` 축하 애니메이션 풀스크린 오버레이
- [ ] `SaveCompletedModal` 확인 버튼 탭 시 모달 + 축하 애니메이션 모두 닫힘
- [ ] `DisposableView` UI 표출, 탭 동작은 TODO 처리
- [ ] 중복 저장 시도 시 AlertDialog("이미 저장된 지하철역이에요.") 표출, 저장 미수행
