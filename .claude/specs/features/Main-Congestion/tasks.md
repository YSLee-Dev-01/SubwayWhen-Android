# Tasks: Main-Congestion

## 참조
- spec: `.claude/specs/features/Main-Congestion/spec.md`
- plan: `.claude/specs/features/Main-Congestion/plan.md`

## Task 목록

### Phase 1. 의존성 추가

#### [x] Task 1 — `libs.versions.toml` (수정)
**파일**: `gradle/libs.versions.toml`
- `[versions]` 섹션에 `vico` 버전 항목 추가
- `[libraries]` 섹션에 `vico-compose-m3` 라이브러리 항목 추가 (`com.patrykandpatrick.vico:compose-m3`)

---

#### [x] Task 2 — `build.gradle.kts` (수정)
**파일**: `app/build.gradle.kts`
- `dependencies` 블록에 `implementation(libs.vico.compose.m3)` 추가
- Gradle sync 후 빌드 통과 확인

---

### Phase 2. 데이터 레이어

#### [x] Task 3 — `HourlyCongestion.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/HourlyCongestion.kt`
- `HourlyCongestion(hour: Int, percent: Int, level: Int)` data class 정의
- 0~23시 한 시간대의 혼잡도 스냅샷을 담는 모델

---

#### [x] Task 4 — `CongestionManager.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/congestion/CongestionManager.kt`
- `suspend fun getCongestions(station: String): List<HourlyCongestion>` 함수 추가
  - 현재 `dayType()` 로 요일 판별 (WEEKDAY / SATURDAY / HOLIDAY)
  - 기존 private `getDayData()` 재사용하여 0~23시 정렬된 리스트 반환
  - 파싱 실패 / 역 없음 / 요일 데이터 없음 → `emptyList()` 반환 (crash 없음)
  - 1~4시 결측 시 `percent=0, level=0` 채움 (기존 로직 그대로 적용)

---

#### [x] Task 5 — `CongestionManagerTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/data/remote/congestion/CongestionManagerTest.kt`
- Kotest 기반 단위 테스트 작성
  - 존재하는 역: 0~23시 24개 항목 반환, hour 순 정렬 검증
  - 존재하지 않는 역: `emptyList()` 반환 검증
  - 빈 데이터(파싱 실패 시뮬레이션): `emptyList()` 반환 검증
  - 1~4시 결측 항목: percent=0, level=0 채움 검증

---

### Phase 3. MVI Contract + ViewModel

#### [x] Task 6 — `CongestionContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/CongestionContract.kt`
- `CongestionUiState` data class 정의
  - `selectedStation: String` — 현재 선택된 역
  - `availableStations: List<String>` — 지원 역 목록
  - `congestionData: List<HourlyCongestion>` — 선택 역의 시간대별 혼잡도
  - `nowHour: Int` — 현재 시각 (0~23)
- `CongestionIntent` sealed interface 정의
  - `OnAppear` — 모달 진입 시 초기 데이터 로드
  - `StationTap(station: String)` — 역 칩 선택

---

#### [x] Task 7 — `CongestionViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/CongestionViewModel.kt`
- `@HiltViewModel` + `ViewModel()` 상속
- `CongestionManager`, `LocalDataRepository` Hilt 주입
- `uiState: StateFlow<CongestionUiState>` 노출
- `onIntent(intent: CongestionIntent)` 단일 진입점
  - `OnAppear`: `getAvailableStations()` 호출, `Calendar.HOUR_OF_DAY`로 nowHour 설정, `saveSetting`에서 `mainCongestionBaseStation` 읽어 초기 역 선택, `getCongestions(selectedStation)` 호출해 상태 갱신
  - `StationTap`: 동일 역이면 no-op, 아니면 `selectedStation` 갱신 + `getCongestions()` 재조회 + `updateSaveSetting(copy(mainCongestionBaseStation = station))` 저장

---

#### [x] Task 8 — `CongestionViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/home/modal/CongestionViewModelTest.kt`
- MockK + Turbine 기반 ViewModel 단위 테스트 작성
  - `OnAppear` 시 `availableStations`, `selectedStation`, `congestionData`, `nowHour` 상태 갱신 검증
  - `StationTap(newStation)` 시 `selectedStation` 변경 + `updateSaveSetting` 호출 검증
  - `StationTap(sameStation)` 시 no-op (상태 변경 없음, `updateSaveSetting` 미호출) 검증

---

### Phase 4. UI 컴포넌트

#### [x] Task 9 — `CongestionStationChips.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/component/CongestionStationChips.kt`
- `LazyRow` 기반 수평 스크롤 칩 행
- 각 칩은 `AnimatedTapBox`로 래핑하여 탭 피드백 적용
- 선택된 역: 테두리 강조(AppIconColor) + `FontWeight.Bold`
- 미선택 역: 기본 테두리 + 일반 weight
- `availableStations`가 빈 리스트면 `LazyRow` 미표시
- `onStationTap: (String) -> Unit` 콜백 파라미터로 노출

---

#### [x] Task 10 — `CongestionChart.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/component/CongestionChart.kt`
- Vico `CartesianChartHost` 기반 라인 차트
- X축: 0, 3, 6, 9, 12, 15, 18, 21, 23시 레이블만 표시
- Y축: 30% stride grid + "%" 단위 레이블
- 현재 시간(`nowHour`) 포인트: 빨간 점 + "%값%" annotation (`selectedHour == null`일 때만 표시)
- 차트 탭 시: 해당 시간 수직선(RuleMark) + "N시 · M%" annotation 표시, `selectedHour` 상태 업데이트
- 현재 시간 포인트 재탭 시: annotation 해제 (`selectedHour = null`)
- 역 변경 또는 탭 선택 시 smooth 애니메이션 적용
- `congestionData: List<HourlyCongestion>` 빈 리스트면 라인 미표시 (crash 없음)
- `selectedHour: Int?`, `onHourSelect: (Int?) -> Unit`, `nowHour: Int` 파라미터로 노출

---

### Phase 5. 모달 조립 + 화면 연결

#### [x] Task 11 — `CongestionModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/CongestionModal.kt`
- `CommonModalBottomSheet` 사용
  - `mainTitle = "현재 지하철 예상 혼잡도"`
  - `subTitle = "선택된 지하철역의 예상 혼잡도를 확인할 수 있어요."`
  - `sheetHeight = 500.dp` (1곳 사용이므로 인라인 상수)
- `hiltViewModel<CongestionViewModel>()` 주입, `uiState` 수집
- 모달 진입 시 `OnAppear` Intent 전달 (`LaunchedEffect(Unit)`)
- `selectedHour: Int?` Composable `remember` 상태 보유 (역 변경 시 `null` 리셋)
- 본문 레이아웃: `CongestionStationChips` + `CongestionChart` 수직 배치
- `StationTap` Intent 전달 시 `selectedHour = null` 리셋 후 ViewModel에 전달
- `onDismiss: () -> Unit` 파라미터로 노출

---

#### [x] Task 12 — `HomeScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeScreen.kt`
- `onTabBarVisibilityChange: (Boolean) -> Unit` 파라미터 추가
- 혼잡도 모달 표시 상태 `var isCongestionModalVisible by remember { mutableStateOf(false) }` 추가
- `HomeEffect.NavigateToCongestion` effect 수신 시: `isCongestionModalVisible = true` + `onTabBarVisibilityChange(false)` 호출
- `isCongestionModalVisible == true`일 때 `CongestionModal` 표시
- 모달 `onDismiss` 시: `isCongestionModalVisible = false` + `onTabBarVisibilityChange(true)` 호출

---

#### [x] Task 13 — `RootScaffold.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/RootScaffold.kt`
- `HomeScreen` 호출부에 `onTabBarVisibilityChange = { isTabBarVisible = it }` 전달
- 기존 `onCongestionTap = {}` 빈 콜백 정리 (모달이 HomeScreen 내부에서 처리되므로 불필요하면 제거, 시그니처에 남아있다면 그대로 유지)

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공
- [ ] 단위 테스트 통과 (`CongestionManagerTest`, `CongestionViewModelTest`)
- [ ] Compose Preview 라이트/다크 렌더 확인 (`CongestionStationChips`, `CongestionChart`)

### 기능 (AC)
- [ ] 홈 화면 혼잡도 버튼 탭 시 모달이 슬라이드업으로 열린다
- [ ] 모달 진입 시 탭바가 숨겨지고, 닫힐 때 복원된다
- [ ] 지원 역 목록이 수평 스크롤 칩으로 표시되며, 마지막 저장 역이 초기 선택된다
- [ ] 선택된 역의 시간대별 혼잡도가 꺾은선 차트로 표시된다
- [ ] 현재 시간에 빨간 점과 "%값%" annotation이 표시된다
- [ ] 차트를 탭하면 해당 시간대 수직선과 "N시 · M%" annotation이 표시된다
- [ ] 현재 시간 포인트를 탭하면 annotation이 해제된다
- [ ] 역 변경 시 차트가 애니메이션으로 갱신되고 DataStore에 저장된다
- [ ] 요일(평일/토/일·공휴일)에 따라 올바른 데이터 집합이 사용된다
- [ ] JSON 파싱 실패 시 빈 차트를 보여주며 crash가 발생하지 않는다
