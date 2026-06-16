# Tasks: Detail (역 상세 — 실시간 도착 + 시간표)

## 참조
- spec: `.claude/specs/features/Detail/spec.md`
- plan: `.claude/specs/features/Detail/plan.md`

## Task 목록

---

### Phase 1. 진입 모델 · 네비게이션 배선

#### [x] Task 1 — `DetailSendModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/DetailSendModel.kt`
- `@Serializable` data class 정의
- 필드: `upDown: String`, `stationName: String`, `lineNumber: String`, `stationCode: String`, `lineCode: String`, `exceptionLastStation: String`, `korailCode: String`
- `HomeCellData` → `DetailSendModel` 변환 확장 함수 또는 top-level 함수 포함 (같은 파일 또는 별도 mapper 파일)

---

#### [x] Task 2 — `NavRoutes.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/NavRoutes.kt`
- `Detail` 라우트 상수 추가: `"detail/{model}"` (JSON 직렬화 string argument)
- `DetailResultSchedule` 라우트 상수 추가: `"detail_result_schedule/{model}"` (시간표 전달 인자 포함)
- argument 키 상수 추가: `ARG_DETAIL_MODEL`, `ARG_RESULT_SCHEDULE_MODEL`

---

#### [x] Task 3 — `RootScaffold.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/RootScaffold.kt`
- `NavHost` 내부에 `Detail` composable 등록: `enterTransition` = `slideInHorizontally`, `popExitTransition` = `slideOutHorizontally` (`tween(Dimens.animationDurationMs)`)
- `Detail` composable 내 `onTabBarVisibilityChange(false)` 호출 연동 (진입 시 탭바 숨김, 이탈 시 복원)
- `Detail` composable 내 JSON argument를 `DetailSendModel`로 역직렬화 후 `DetailScreen`에 전달
- `DetailResultSchedule` composable 등록: `slideInHorizontally` 전환, 탭바 숨김 유지
- `HomeScreen` `onNavigateToDetail` 람다: 현재 빈 람다 → `HomeCellData` → `DetailSendModel` 변환 후 JSON 인코딩 → `childNavController.navigate(NavRoutes.Detail + "/${encoded}")` 연결

---

### Phase 2. 실시간 도착 섹션

#### [x] Task 4 — `DetailContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/DetailContract.kt`
- `DetailUiState` data class 정의
  - `sendModel: DetailSendModel` (진입 파라미터 보관)
  - `firstArrival: DetailArrivalItem?` (첫 번째 열차)
  - `secondArrival: DetailArrivalItem?` (두 번째 열차)
  - `prevStationName: String`, `currentStationName: String`, `nextStationName: String`
  - `scheduleItems: List<DetailScheduleItem>` (현재 시간 이후 필터 결과)
  - `isArrivalLoading: Boolean`, `isScheduleLoading: Boolean`
  - `timerCount: Int` (15 → 0 카운트다운)
  - `isRefreshCooldown: Boolean` (1.2초 쿨타임 플래그)
  - `arrivalError: Boolean`, `scheduleError: Boolean`
- `DetailArrivalItem` data class: `useTime: String`, `statusMessage: String`, `destination: String`, `trainNo: String`, `isFast: Boolean`, `statusCode: String`
- `DetailScheduleItem` data class: `minutesLater: Int`, `timeLabel: String`, `destination: String`, `isFast: Boolean`
- `DetailIntent` sealed interface: `OnAppear`, `OnDisappear`, `Refresh`, `ScheduleMoreTap`, `RealtimeTap`, `ExceptionRowTap`, `Back`
- `DetailEffect` sealed interface: `NavigateToResultSchedule(scheduleItems)`, `NavigateBack`

---

#### [x] Task 5 — `DetailMapper.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/mapper/DetailMapper.kt`
- `RealtimeStationArrival` → `DetailArrivalItem` 변환 함수
  - `useState` (0~5, 99) → 상태 메시지 문자열 변환 (0=진입, 1=도착, 2=출발, 3=전역출발, 4=전역진입, 5=전역도착, 99=운행중)
  - `useTime` 규칙: `arrivalTime==0` + 코드별 텍스트 / `<60초` → "N초" / `>=60초` → "N분"
  - `isFast` → `Boolean` 변환 ("급행"/"ITX" → true)
- `ScheduleStationModel` → `DetailScheduleItem` 변환 함수 (Seoul 타입)
- `ProcessedKorailSchedule` → `DetailScheduleItem` 변환 함수 (Korail, `0HMMSS` → `HH:MM` 파싱)
- `ProcessedShinbundangSchedule` → `DetailScheduleItem` 변환 함수 (Shinbundang, 0시 → 24시 처리)
- 현재 시간 이후 필터 함수 (결과 empty 시 전체 리스트 반환)
- 호선명 기준 `.Unowned` 판별 함수 (공항철도·우이신설·경강선·서해선·GTX-A)
- `minutesLater` 계산: 현재 시각 대비 분 차이

---

#### [x] Task 6 — `DetailViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/DetailViewModel.kt`
- `@HiltViewModel`, `SavedStateHandle`로 `DetailSendModel` 주입
- `TotalLoadModel.liveArrivalSplit(stationName, line)` 호출 후 `upDown` 값으로 방향 리스트 선택
- 첫·두 번째 열차 추출 + `backStationName`/`nextStationName` 반영 → `DetailMapper`로 `DetailArrivalItem` 변환
- `loadSchedule()`: `DetailSendModel.lineCode` 기준 라인 분기
  - 신분당선 → `shinbundangScheduleLoad`
  - korailCode 보유 → `korailScheduleLoad`
  - Unowned → API 호출 생략, `scheduleItems = emptyList()` + `isUnowned = true`
  - 그 외 → `seoulScheduleLoad`
- 15초 카운트다운 타이머: `viewModelScope.launch` + `delay` 루프, 0 도달 시 `loadArrival()` 재호출 + 시간표 정렬 재수행
- `timerJob` 프로퍼티로 관리, `OnDisappear` Intent 수신 시 취소
- `Refresh` Intent 처리: `isRefreshCooldown` 체크 후 1.2초 쿨타임 적용 (`viewModelScope.launch { delay(1200) }`)
- `NetworkResult.Failure` → `arrivalError = true` (타이머는 유지)

---

#### [x] Task 7 — `component/DetailArrivalSection.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/component/DetailArrivalSection.kt`
- `MainBgCard` 래퍼 사용
- 첫 번째·두 번째 열차 표시 블록: `useTime`, `statusMessage`, `destination`, `trainNo`, `isFast` 표시
- `exceptionLastStation` 비어있지 않으면 두 번째 열차 자리에 `UpDownExceptionRow` 표시
- 데이터 없음 상태 안내 텍스트 (첫·두 번째 열차 모두 null, `arrivalError = false` 일 때)
- `...` 버튼 (`AnimatedTapBox` 래핑): `onRealtimeTap` 콜백 연결 + `// TODO: Realtime 화면 연결` 주석
- 실패 상태 안내 텍스트 (`arrivalError = true` 일 때)

---

### Phase 3. 열차 위치 애니메이션 · 타이머 · 새로고침

#### [x] Task 8 — `component/DetailTrainPositionView.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/component/DetailTrainPositionView.kt`
- 이전역 / 현재역 / 다음역 이름 표시 (가로 3구간 레이아웃)
- 열차 아이콘 위치: `statusCode` → 위치 float 인덱스 매핑
  - 0(진입)/4(전역진입) → 이전역 쪽
  - 1(도착)/5(전역도착) → 현재역
  - 2(출발)/3(전역출발)/99(운행중) → 현재역 ~ 다음역 사이
- `animateFloatAsState(targetValue, tween(durationMillis = Dimens.animationDurationMs))` 로 슬라이드
- 급행 여부에 따라 아이콘 분기 (일반 / 급행 아이콘)
- 새로고침 시 아이콘이 목적지 방향 끝으로 슬라이드 후 새 위치에 재배치

---

#### [x] Task 9 — `component/DetailTimerRefreshView.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/component/DetailTimerRefreshView.kt`
- 카운트다운 숫자 표시 (`timerCount: Int`, 15 → 0)
- 수동 새로고침 버튼 (`AnimatedTapBox` 래핑, `onRefresh` 콜백)
- `isRefreshCooldown = true` 시 버튼 비활성화 처리 (시각적 dim 또는 클릭 무시)

---

### Phase 4. 시간표 섹션 (Detail 내 요약)

#### [x] Task 10 — `component/DetailScheduleSection.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/component/DetailScheduleSection.kt`
- `MainBgCard` 래퍼 사용
- 2열 `LazyVerticalGrid` (또는 `FlowRow`) 로 `DetailScheduleItem` 표시
  - 각 셀: "N분 후 / HH:MM / 목적지행 / (급)" 텍스트
  - `AnimatedTapBox` 래핑 (탭 피드백)
- Unowned 라인일 때: "시간표 미제공" 안내 텍스트만 표시, 그리드 생략
- `scheduleError = true` 시: 빈 상태 안내
- `PrimaryButton("시간표 더보기")`: `onScheduleMoreTap` 콜백 연결

---

#### [x] Task 11 — `DetailScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/DetailScreen.kt`
- `CommonTopBar` 사용: 뒤로가기 버튼 + 역명 표시
- `LazyColumn` 내 순서: 이전/현재/다음역 헤더 → `DetailArrivalSection` → `DetailTrainPositionView` → `DetailTimerRefreshView` → `DetailScheduleSection`
- `LaunchedEffect(Unit)` 에서 `onIntent(DetailIntent.OnAppear)` 전송
- `DisposableEffect` 또는 `LaunchedEffect` + lifecycle 로 `OnDisappear` Intent 전송 (화면 이탈 시 타이머 취소)
- `Effect` 수집: `NavigateToResultSchedule` → `onScheduleMoreTap(scheduleItems)` 콜백 호출, `NavigateBack` → `onBack()` 콜백 호출
- 파라미터: `sendModel: DetailSendModel`, `onBack: () -> Unit`, `onScheduleMoreTap: (List<DetailScheduleItem>) -> Unit`, `onTabBarVisibilityChange: (Boolean) -> Unit`
- `SideEffect` 로 진입 시 `onTabBarVisibilityChange(false)` 호출

---

### Phase 5. DetailResultSchedule (시간표 전체 보기)

#### [x] Task 12 — `resultschedule/DetailResultScheduleContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/resultschedule/DetailResultScheduleContract.kt`
- `DetailResultScheduleUiState` data class
  - `hourSections: List<HourSection>` (시간별 그룹)
  - `isExceptionModalVisible: Boolean`
  - `selectedDestination: String?` (제외 행 선택 대상)
- `HourSection` data class: `hour: Int`, `label: String` ("10시" 형식), `items: List<DetailScheduleItem>`
- `DetailResultScheduleIntent` sealed interface: `OnAppear`, `ExceptionButtonTap(destination)`, `ExceptionConfirm`, `ExceptionDismiss`, `Back`
- `DetailResultScheduleEffect` sealed interface: `NavigateBackWithException(exceptionLastStation: String)`, `NavigateBack`

---

#### [x] Task 13 — `resultschedule/DetailResultScheduleViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/resultschedule/DetailResultScheduleViewModel.kt`
- `@HiltViewModel`, `SavedStateHandle`로 시간표 리스트(`List<DetailScheduleItem>`) JSON 역직렬화 주입
- `OnAppear` 수신 시 `DetailScheduleItem` 리스트 → `HourSection` 리스트로 그룹화 (hour별 분리)
- 현재 시 인덱스 계산 (자동 스크롤 타겟용 `State` 노출)
- `ExceptionButtonTap` → `selectedDestination` 갱신, `isExceptionModalVisible = true`
- `ExceptionConfirm` → `NavigateBackWithException(selectedDestination)` Effect 방출
- `ExceptionDismiss` → 모달 닫기

---

#### [x] Task 14 — `resultschedule/component/DetailResultScheduleHourHeader.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/resultschedule/component/DetailResultScheduleHourHeader.kt`
- 시간 섹션 헤더 컴포저블: "10시", "11시" 등 텍스트 표시
- 현재 시간 섹션 강조 처리 (폰트 굵기 또는 색상 차이)

---

#### [x] Task 15 — `resultschedule/component/DetailResultScheduleCell.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/resultschedule/component/DetailResultScheduleCell.kt`
- 시간표 셀 컴포저블: 분, 목적지, 시작역, 급행 여부 표시
- `AnimatedTapBox` 래핑 불필요 (탭 이벤트 없음)

---

#### [x] Task 16 — `resultschedule/DetailResultScheduleScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/resultschedule/DetailResultScheduleScreen.kt`
- `CommonTopBar` 사용: 뒤로가기 + "시간표" 제목
- `LazyColumn` + `rememberLazyListState()` 로 시간별 섹션 렌더링
  - 섹션 헤더: `DetailResultScheduleHourHeader`
  - 섹션 아이템: `DetailResultScheduleCell`
- 현재 시간 섹션으로 자동 스크롤: `LaunchedEffect(hourSections)` 에서 `lazyListState.animateScrollToItem(currentHourIndex)` 호출
- 제외 행 버튼 (`AnimatedTapBox` 래핑) → `ExceptionButtonTap` Intent 전송
- `isExceptionModalVisible = true` 시 `CommonModalBottomSheet` 표시: 확인 → `ExceptionConfirm`, 닫기 → `ExceptionDismiss`
- `Effect` 수집: `NavigateBackWithException(exception)` → `onNavigateBackWithException(exception)` 콜백 호출, `NavigateBack` → `onBack()` 호출
- `SideEffect` 로 진입 시 `onTabBarVisibilityChange(false)` 호출

---

#### [x] Task 17 — `RootScaffold.kt` `DetailResultSchedule` 라우트 연결 (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/RootScaffold.kt`
- `DetailResultSchedule` composable 등록: `slideInHorizontally` 전환
- `DetailScreen` `onScheduleMoreTap` 콜백: 시간표 리스트 JSON 인코딩 → `navigate(NavRoutes.DetailResultSchedule + "/${encoded}")`
- `DetailResultScheduleScreen` `onNavigateBackWithException(exception)` 콜백: `DetailSendModel.exceptionLastStation` 갱신 후 Detail 재로딩 (popBackStack + navigate 또는 `SavedStateHandle` 결과 반환)

---

### Phase 6. 통합 · 마감

#### [x] Task 18 — `HomeCellData` → `DetailSendModel` 변환 정확도 검증 (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/DetailSendModel.kt` (또는 매핑 위치)
- `HomeCellData` 필드 매핑 확인
  - `updnLine` → `upDown`
  - `stationName` → `stationName`
  - `line` → `lineNumber`
  - `stationCode` → `stationCode`
  - `lineCode` → `lineCode`
  - `exceptionLastStation` → `exceptionLastStation`
  - `korailCode` → `korailCode`
- `StationTap` Intent 처리에서 `NavigateToDetail(cell)` Effect 방출 경로 확인 (`HomeViewModel` 기존 로직 유지)

---

#### [x] Task 19 — 에러·빈 상태 점검 및 마감 (검토)
**파일**: 전체 신규 파일
- 실시간 API 실패 시 `arrivalError = true` + 타이머 유지 확인
- 네트워크 없음 시 마지막 성공 데이터 유지 로직 확인
- 현재 시간 이후 열차 없음 시 전체 시간표 표시 확인 (빈 리스트 → 필터 비활성화)
- 미사용 import 정리
- Dimens 2회 이상 반복값 토큰화 검토 (`Dimens.kt` 기존 토큰 재사용 우선)
- `@Preview` 어노테이션: 각 컴포저블 하단에 Light/Dark 프리뷰 추가

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공 (미사용 import 없음)
- [ ] `GlobalScope` 미사용 (타이머는 `viewModelScope` 내 관리)
- [ ] 신규 공통 컴포넌트 생성 없음 (기존 `ui/common/` 것만 활용)
- [ ] 하드코딩 색상 없음 (`MaterialTheme.colorScheme.*` 사용)
- [ ] `@Serializable` `DetailSendModel` JSON 직렬화·역직렬화 정상 동작
- [ ] Dimens 토큰 준수 (2회 이상 반복값은 `Dimens.kt` 참조)

### 기능 (AC)
- [ ] 홈 화면 역 카드 탭 시 Detail 화면으로 진입 + 탭바 숨김
- [ ] 진입 즉시 실시간 도착 정보 로딩 → 첫 번째·두 번째 열차 표시
- [ ] 진입 즉시 시간표 로딩 → 현재 시간 이후 열차 표시
- [ ] 15초마다 실시간 정보 자동 새로고침 + 카운트다운 UI (15 → 0)
- [ ] 수동 새로고침 버튼 동작 (1.2초 쿨타임)
- [ ] 열차 위치 애니메이션 (`statusCode` → 이전/현재/다음역 구간 이동)
- [ ] 라인 유형별 시간표 처리 (Seoul / Korail / Shinbundang / Unowned)
- [ ] 시간표 더보기 → DetailResultSchedule 화면 이동
- [ ] DetailResultSchedule: 시간별 섹션 + 현재 시간 자동 스크롤
- [ ] DetailResultSchedule: 제외 행 설정 → Detail 실시간 정보 재로딩
- [ ] 화면 이탈 시 타이머 자동 취소
- [ ] 실시간/시간표 API 실패 시 빈 상태 처리
- [ ] `...` 버튼 배치 + `// TODO: Realtime 화면 연결` 주석
