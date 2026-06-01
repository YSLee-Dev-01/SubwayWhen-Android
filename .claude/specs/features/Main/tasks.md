# Tasks: Main (메인 화면)

## 참조
- spec: `.claude/specs/features/Main/spec.md`
- plan: `.claude/specs/features/Main/plan.md`

## Task 목록

### Phase 1. TotalLoadModel 구현 + DI 등록

#### [x] Task 1 — `TotalLoadModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModel.kt`
- 현재 빈 인터페이스에 3개 메서드를 추가한다
- `arrivalDataLoad(stations: List<SaveStation>): Flow<IndexedValue<NetworkResult<LiveStationModel>>>` — 여러 역 실시간 병렬 로드, 응답 순서대로 Flow 방출
- `seoulScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<ScheduleStationModel>` — 서울 지하철 시간표
- `korailScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<KorailHeader>` — 코레일 시간표

---

#### [x] Task 2 — `TotalLoadModelImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModelImpl.kt`
- `LoadModel`을 생성자 주입(`@Inject`)으로 받는다
- `arrivalDataLoad`: `channelFlow` + 역별 `launch`로 병렬 처리. 각 역의 인덱스를 보존하여 `IndexedValue<NetworkResult<LiveStationModel>>`를 방출한다
  - 내부에서 `LoadModel.stationArrivalRequest(station.stationName)` 호출
- `seoulScheduleLoad`: `LoadModel.seoulStationScheduleLoad`에 station의 `stationCode`, `updnLine`, `line` 등 필요한 파라미터를 매핑하여 위임
- `korailScheduleLoad`: `LoadModel.korailScheduleLoad`에 station의 `korailCode`, 코레일 라인 코드 등을 매핑하여 위임

---

#### [x] Task 3 — `NetworkModule.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/NetworkModule.kt`
- 기존 `bindLoadModel` 하단에 `TotalLoadModel` → `TotalLoadModelImpl` Singleton Binds 바인딩 추가
- 기존 바인딩 구조와 동일한 패턴으로 작성한다

---

### Phase 2. 표시 모델 + 매핑 + 문자열 리소스

#### [x] Task 4 — `HomeCellType.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeCellType.kt`
- `enum class HomeCellType { Loading, Real, Schedule }` 정의
- iOS `MainTableViewCellType` 대응

---

#### [x] Task 5 — `HomeCellData.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeCellData.kt`
- iOS `MainTableViewCellData` 대응 표시 모델 data class 정의
- 필드: `stationIndex: Int`, `type: HomeCellType`, `stationName: String`, `updnLine: String`, `lastStation: String`, `exceptionLastStation: String`, `stateMSG: String`, `arrivalTime: String`, `subPrevious: String`, `code: String`, `isFast: String`, `line: String`, `lineCode: String`, `korailCode: String`, `stationCode: String`
- `useTime: String` 계산 프로퍼티 (val) — spec의 도착 시간 계산 규칙 구현:
  - `real` & arrivalTime == "0" & code == "0" → `home_arrival_soon` ("곧 도착")
  - `real` & arrivalTime == "0" & code == "1" → `home_arrival_now` ("도착")
  - `real` & arrivalTime == "0" & code == "2" → `home_arrival_depart` ("출발")
  - `real` & arrivalTime == "0" & 그 외 → subPrevious에서 `(` 이전 문자열 추출
  - `real` & arrivalTime > 0 & < 60 → "%d초" 형식
  - `real` & arrivalTime >= 60 → "%d분" 형식 (초를 60으로 나눔)
  - `schedule` → subPrevious 그대로
- `useFast: String` 계산 프로퍼티 (val):
  - isFast == "급행" → "(급)"
  - isFast == "ITX" → "(ITX)"
  - 그 외 → 빈 문자열

---

#### [x] Task 6 — `HomeCellMapper.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/mapper/HomeCellMapper.kt`
- `SaveStation.toLoadingCell(index: Int): HomeCellData` — `type=Loading`으로 초기 카드 생성
- `LiveStationModel.toRealCells(index: Int, base: SaveStation): List<HomeCellData>` — `realtimeArrivalList`에서 `base.updnLine`과 `upDown`이 일치하는 항목만 `type=Real`로 변환. 없으면 "정보 없음" 상태의 Real 카드 반환
- `ScheduleStationModel.toScheduleCell(prev: HomeCellData): HomeCellData` — 서울 시간표 응답에서 현재 시간 이후 가장 빠른 `ScheduleStationArrival`을 찾아 `type=Schedule`로 변환
- `KorailHeader.toScheduleCell(prev: HomeCellData): HomeCellData` — 코레일 시간표 응답에서 현재 시간 이후 가장 빠른 `KorailSchedule`을 찾아 `type=Schedule`로 변환
- `timeGroup(oneTime: Int, twoTime: Int, nowHour: Int): SaveStationGroup?` — iOS `MainModel.timeGroup` 포팅. 현재 시각 기준 그룹 자동 결정. 양쪽 모두 0이면 null 반환

---

#### [x] Task 7 — `HomeMainTitle.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeMainTitle.kt`
- `todayDayOfWeek(): Int` — `Calendar.getInstance().get(Calendar.DAY_OF_WEEK)` 반환 (1=일요일 ~ 7=토요일)
- Screen에서 `stringArrayResource`로 요일별 string-array를 참조하고 `remember { messages.random() }`으로 메시지를 선택하기 위한 요일 인덱스 계산만 담당

---

#### [x] Task 8 — `strings.xml` (수정)
**파일**: `app/src/main/res/values/strings.xml`
- 다음 문자열 리소스 추가:
  - `home_header_current_traffic` ("현재 지하철 예상 혼잡도")
  - `home_header_report` ("지하철 민원")
  - `home_header_edit` ("편집")
  - `home_live_status_title` ("실시간 현황")
  - `home_group_one` ("출근"), `home_group_two` ("퇴근")
  - `home_empty_station` ("버튼을 눌러서 지하철 역을 추가할 수 있어요!")
  - `home_schedule_loading` ("시간표 로드 중")
  - `home_arrival_no_data` ("정보 없음")
  - `home_arrival_soon` ("곧 도착"), `home_arrival_now` ("도착"), `home_arrival_depart` ("출발")
  - `home_arrival_seconds` ("%d초"), `home_arrival_minutes` ("%d분")
  - `home_state_fast` ("(급)"), `home_state_itx` ("(ITX)")
  - `home_schedule_prefix` ("⏱️")
  - `home_bound_suffix` ("행")
- 요일별 string-array 7개 추가:
  - `home_main_title_sunday`, `home_main_title_monday`, `home_main_title_tuesday`, `home_main_title_wednesday`, `home_main_title_thursday`, `home_main_title_friday`, `home_main_title_saturday`
  - 각 배열에 해당 요일에 어울리는 타이틀 메시지 3개 이상 포함 (iOS 원본 `SubwayWhen/Presentation/Main/MainModel.swift` 참조)

---

#### [x] Task 9 — `HomeCellDataTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/home/HomeCellDataTest.kt`
- `HomeCellData.useTime` 계산 규칙 단위 테스트 (JUnit + Kotest 스타일)
  - real & arrivalTime == "0" & 각 code 값별 케이스
  - real & arrivalTime == "30" → "30초"
  - real & arrivalTime == "120" → "2분"
  - schedule → subPrevious 그대로
- `HomeCellData.useFast` 계산 규칙 단위 테스트
  - isFast == "급행" → "(급)"
  - isFast == "ITX" → "(ITX)"
  - isFast == "" → ""

---

### Phase 3. Contract + ViewModel

#### [x] Task 10 — `HomeContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeContract.kt`
- `HomeUiState` data class:
  - `mainTitle: String` (현재 표시할 타이틀 메시지)
  - `congestionEmoji: String` (고정값 "🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥")
  - `currentGroup: SaveStationGroup`
  - `cells: List<HomeCellData>`
  - `isRefreshing: Boolean`
- `HomeIntent` sealed interface:
  - `OnAppear` — 화면 진입
  - `Refresh` — Pull-to-Refresh
  - `GroupTap(group: SaveStationGroup)` — 탭 전환
  - `StationTap(cell: HomeCellData)` — 역 카드 탭
  - `ScheduleTap(cell: HomeCellData)` — 시간표 버튼 탭
  - `CongestionTap` — 혼잡도 버튼 탭
  - `ReportTap` — 민원 버튼 탭
  - `EditTap` — 편집 버튼 탭
  - `EmptyAddTap` — 빈 목록 카드 탭
- `HomeEffect` sealed interface:
  - `NavigateToSearch`
  - `NavigateToDetail(cell: HomeCellData)`
  - `NavigateToCongestion`
  - `NavigateToReport`
  - `NavigateToEdit`

---

#### [x] Task 11 — `HomeViewModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeViewModel.kt`
- `TotalLoadModel`, `LocalDataRepository` Hilt 생성자 주입 추가
- `StateFlow<HomeUiState>` — `MutableStateFlow`로 초기 상태 관리
- `SharedFlow<HomeEffect>` — `MutableSharedFlow`로 일회성 이벤트 방출
- `loadJob: Job?` — in-flight 요청 취소용 Job 보관
- `init` 블록:
  - `saveStations` + `saveSetting` 결합(combine) Flow 구독
  - `saveSetting`의 `mainGroupOneTime`/`mainGroupTwoTime` 기반으로 `timeGroup()` 호출 → 자동 그룹 결정
  - `loadGroupData()` 호출
- `onIntent(intent: HomeIntent)` 단일 진입점:
  - `OnAppear` / `Refresh` → `loadGroupData()` 호출 (Refresh는 `isRefreshing=true` 선행)
  - `GroupTap` → `currentGroup` 업데이트 후 `loadGroupData()` 호출
  - `StationTap` → `HomeEffect.NavigateToDetail` 방출
  - `ScheduleTap` → `handleScheduleTap()` 호출
  - `CongestionTap` → `HomeEffect.NavigateToCongestion` 방출
  - `ReportTap` → `HomeEffect.NavigateToReport` 방출
  - `EditTap` → `HomeEffect.NavigateToEdit` 방출
  - `EmptyAddTap` → `HomeEffect.NavigateToSearch` 방출
- `loadGroupData()` private 함수:
  - 기존 `loadJob?.cancel()`
  - 현재 그룹 역 목록 필터 → `toLoadingCell()` 목록으로 즉시 상태 업데이트
  - `arrivalDataLoad(stations)` Flow collect → 응답마다 해당 인덱스 카드를 `toRealCells()` 결과로 교체
  - API 실패 → 해당 카드를 "정보 없음" 상태로 교체 (앱 크래시 없음)
  - 완료 후 `isRefreshing=false`
- `handleScheduleTap(cell: HomeCellData)` private 함수:
  - 해당 카드를 `type=Loading` + `stateMSG=home_schedule_loading`으로 즉시 교체
  - `cell.korailCode.isNotEmpty()` 여부로 `korailScheduleLoad` vs `seoulScheduleLoad` 분기
  - 성공: 해당 카드를 `toScheduleCell()` 결과로 교체
  - 실패: 카드를 이전 Real 상태로 복원

---

#### [x] Task 12 — `HomeViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/home/HomeViewModelTest.kt`
- MockK로 `TotalLoadModel`, `LocalDataRepository` Fake 주입
- Turbine으로 StateFlow 검증
- 테스트 케이스:
  - `GroupTap` 시 `currentGroup`이 변경되고 `cells`가 새 그룹 기준으로 로딩 상태로 전환됨
  - `arrivalDataLoad` 응답 수신 시 해당 인덱스 카드가 Real 타입으로 교체됨
  - `ScheduleTap` 시 해당 카드가 Loading → Schedule 순으로 전환됨
  - API 실패 시 카드가 "정보 없음" 상태로 흡수됨 (앱 크래시 없음)
  - `EmptyAddTap` 시 `NavigateToSearch` Effect 방출

---

### Phase 4. 정적 컴포넌트

#### [x] Task 13 — `HomeMainTitleHeader.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeMainTitleHeader.kt`
- 요일별 타이틀 메시지 텍스트를 표시하는 stateless Composable
- 파라미터: `title: String`, `modifier: Modifier`
- Light/Dark Preview 포함

---

#### [x] Task 14 — `HomeCongestionCard.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeCongestionCard.kt`
- iOS `MainTableHeaderSubView` 대응
- 파라미터: `congestionEmoji: String`, `onTap: () -> Unit`, `modifier: Modifier`
- 레이아웃: full-width, 높이 90dp
- 표시 요소: "현재 지하철 예상 혼잡도" 제목 + 이모지 10개 + `chevron.right` (Icons.AutoMirrored.Filled.KeyboardArrowRight 또는 유사)
- 전체 영역이 탭 가능 (onTap 연결)
- Light/Dark Preview 포함 (congestionEmoji 고정값 전달)

---

#### [x] Task 15 — `HomeHeaderActionButton.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeHeaderActionButton.kt`
- iOS `MainTableHeaderViewBtn` 대응
- 파라미터: `label: String`, `icon: ImageVector`, `onTap: () -> Unit`, `modifier: Modifier`
- 레이아웃: 높이 90dp, 너비는 호출 측에서 `fillMaxWidth(fraction)` 으로 결정
- 민원 버튼과 편집 버튼이 이 컴포넌트를 공유 (각 50%)
- Light/Dark Preview 포함

---

#### [x] Task 16 — `HomeHeaderSection.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeHeaderSection.kt`
- iOS `MainTableHeaderView` 대응
- 파라미터: `congestionEmoji: String`, `onCongestionTap: () -> Unit`, `onReportTap: () -> Unit`, `onEditTap: () -> Unit`, `modifier: Modifier`
- 레이아웃 순서:
  1. `HomeCongestionCard` (full-width)
  2. Row: `HomeHeaderActionButton("지하철 민원", ...)` + `HomeHeaderActionButton("편집", ...)` (각 50%)
  3. "실시간 현황" 라벨 텍스트
- Light/Dark Preview 포함

---

#### [x] Task 17 — `HomeGroupTabBar.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeGroupTabBar.kt`
- iOS `MainTableHeaderGroupView` 대응
- 파라미터: `currentGroup: SaveStationGroup`, `onGroupTap: (SaveStationGroup) -> Unit`, `modifier: Modifier`
- 레이아웃: 출근(ONE) 탭과 퇴근(TWO) 탭이 가로로 나란히
- 너비 애니메이션: `animateFloatAsState`로 선택된 탭 70% : 미선택 탭 30%
- Light/Dark Preview 포함 (ONE 선택 상태, TWO 선택 상태 각각)

---

#### [x] Task 18 — `HomeStationCard.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeStationCard.kt`
- iOS `MainTableViewArrivalCell` 대응
- 파라미터: `cell: HomeCellData`, `onCardTap: () -> Unit`, `onScheduleTap: () -> Unit`, `modifier: Modifier`
- 표시 요소:
  - 호선 색상 원형 뱃지 60dp (`StationLineCircle` 또는 인라인으로 구현, `SubwayLineMapper` 활용)
  - `{stationName} | {exceptionLastStation.ifEmpty { lastStation }}행` 텍스트
  - `{useFast}{stateMSG}` 상태 메시지 (schedule 타입이면 ⏱️ prefix 추가)
  - 도착 시간 영역: `Loading` 타입이면 로딩 인디케이터 + 텍스트 숨김, 그 외 `useTime` 텍스트 표시
  - 시간표 버튼 (타이머 아이콘, 호선 색상 배경 원형)
- `AnimatedTapBox`로 카드 전체 탭 처리
- Light/Dark Preview 포함 (Loading, Real, Schedule 타입 각각)

---

#### [x] Task 19 — `HomeEmptyStationCard.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeEmptyStationCard.kt`
- iOS `MainTableViewDefaultCell` 대응
- 파라미터: `onTap: () -> Unit`, `modifier: Modifier`
- 표시 요소: "버튼을 눌러서 지하철 역을 추가할 수 있어요!" 안내 텍스트
- 전체 카드 탭 시 `onTap()` 호출
- Light/Dark Preview 포함

---

### Phase 5. Screen 조립 + Navigation 연결

#### [x] Task 20 — `HomeScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeScreen.kt`
- 기존 placeholder를 실제 화면으로 재작성
- 시그니처:
  ```
  HomeScreen(
      viewModel: HomeViewModel,
      onNavigateToSearch: () -> Unit,
      onNavigateToDetail: (HomeCellData) -> Unit,
      onCongestionTap: () -> Unit,
      onReportTap: () -> Unit,
      onEditTap: () -> Unit,
  )
  ```
- `viewModel.uiState` collectAsStateWithLifecycle
- `viewModel.effect` LaunchedEffect collect → 각 Effect에 맞는 콜백 호출
- `HomeIntent.OnAppear`를 LaunchedEffect(Unit)으로 최초 1회 전달
- 요일 메시지: `HomeMainTitle.todayDayOfWeek()` 기반 string-array에서 `remember { messages.random() }`
- 레이아웃: `PullToRefreshBox` (Material3) → `LazyColumn`:
  - item: `HomeMainTitleHeader`
  - item: `HomeHeaderSection`
  - item: `HomeGroupTabBar`
  - if cells.isEmpty: item `HomeEmptyStationCard`
  - else: items(cells) `HomeStationCard`
- Light/Dark Preview 포함

---

#### [x] Task 21 — `RootScaffold.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/RootScaffold.kt`
- `HomeScreen()` 호출부에 신규 콜백 파라미터 전달:
  - `onNavigateToSearch` = `{ childNavController.navigate(TabRoute.Search.route) { ... } }`
  - `onNavigateToDetail` = `{ _ -> }` (빈 람다, Detail 미구현)
  - `onCongestionTap` = `{ }` (빈 람다)
  - `onReportTap` = `{ }` (빈 람다)
  - `onEditTap` = `{ }` (빈 람다)

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공
- [x] `HomeCellData.useTime` / `useFast` 단위 테스트 통과
- [x] `HomeViewModel` 단위 테스트 통과 (그룹 전환 / 실시간 업데이트 / 시간표 전환 / API 실패 흡수)
- [x] 전체 컴포넌트 Light/Dark Preview에서 레이아웃 정상 렌더링
- [x] `LoadModel`이 `TotalLoadModelImpl` 외부에서 직접 참조되지 않음

### 기능 (AC)
- [x] 화면 진입 시 요일별 타이틀 메시지가 랜덤으로 표시된다
- [x] 혼잡도/민원/편집 버튼 UI가 헤더에 표시된다 (탭 시 콜백 호출만)
- [x] 저장된 역이 현재 그룹 기준으로 로드되어 카드 목록에 표시된다
- [x] 각 역 카드는 진입 직후 로딩 인디케이터를 표시한다
- [x] 실시간 도착정보가 수신되면 카드가 도착 시간으로 업데이트된다
- [x] 도착 시간이 0초이고 code == 0이면 "곧 도착"이 표시된다
- [x] 출근/퇴근 탭 선택 시 해당 그룹의 역 목록으로 교체된다
- [x] 탭 너비 애니메이션이 동작한다 (선택 70% / 미선택 30%)
- [x] 시간 기반 자동 그룹 전환이 진입 시 적용된다
- [x] 시간표 버튼 탭 시 해당 카드가 시간표 모드로 전환된다
- [x] 저장된 역이 없으면 빈 목록 안내 카드가 표시된다
- [x] 빈 목록 카드 탭 시 onNavigateToSearch 콜백이 호출된다
- [x] Pull-to-Refresh 동작 시 실시간 데이터가 재로드된다
