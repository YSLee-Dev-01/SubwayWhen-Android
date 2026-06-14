# Plan: Main-Congestion (혼잡도 모달)

## 참조 Spec
- @specs/features/Main-Congestion/spec.md

## 참조 Skill
- 신규 화면(모달) 생성이지만 기존 `feature/home` 하위에 모달을 추가하는 형태 — 별도 SKILL 불필요.

## 현재 상태 파악

### 이미 존재 (재사용)
- **`data/remote/congestion/CongestionManager.kt`** — `@Singleton`, assets `congestion_data.json` lazy 로드.
  - `getAvailableStations(): List<String>` 존재 (키 정렬 반환)
  - `getLevel(station, hour): Int?` 존재
  - `dayType()` (WEEKDAY/SATURDAY/HOLIDAY) — `holidayList` + 요일 판별 로직 완비
  - `getDayData()` — 1~4시 결측 시 `percent=0, level=0` 채움 로직 완비 (private)
- **`data/remote/dto/congestion/CongestionModels.kt`** — `CongestionDataSet / StationCongestion / DayCongestion / CongestionLevel` DTO 완비.
- **`app/src/main/assets/congestion_data.json`** — 데이터 파일 이미 존재 (iOS 원본과 동일 구조).
- **`data/model/SaveSetting.kt`** — `mainCongestionBaseStation: String = "강남"` 필드 존재.
- **DataStore 저장/로드** — `SettingLocalDataSource` + `PreferencesKeys.MAIN_CONGESTION_BASE_STATION` 완비.
- **`data/repository/LocalDataRepository`** — `saveSetting: StateFlow<SaveSetting>`, `suspend updateSaveSetting(setting)` 존재 (DataStore 저장 경로).
- **`ui/common/modal/CommonModalBottomSheet.kt`** — `sheetHeight` 파라미터 지원 (500dp 지정 가능).
- **`ui/common/AnimatedTapBox.kt`** — 칩 탭 애니메이션 래퍼.
- **`feature/home/HomeScreen.kt`** — `onCongestionTap` 콜백 + `HomeEffect.NavigateToCongestion` 이미 emit 중.
- **`HomeViewModel`** — 이미 `CongestionManager` 주입받아 사용 중 (이모지 계산).
- **탭바 숨김 패턴** — `RootScaffold`의 `isTabBarVisible` + 각 화면 `onTabBarVisibilityChange` 콜백.

### 신규
- 차트 라이브러리 **Vico** 의존성 추가 (`libs.versions.toml` + `app/build.gradle.kts`).
- `data/remote/congestion/CongestionManager.kt`에 시간대별 전체 데이터 반환 함수 추가
  (iOS `getCongestions(station:)` 대응 — 0~23시 정렬된 `(hour, percent, level)` 리스트).
- `feature/home/modal/CongestionModal.kt` — `CommonModalBottomSheet` 기반 모달 Composable.
- `feature/home/modal/component/` — 역 선택 칩 행, 혼잡도 라인 차트 컴포넌트.
- 혼잡도 모달 상태 보유 방식 결정 (아래 기술적 결정사항 참조).

### 수정
- `feature/home/HomeScreen.kt` — 혼잡도 모달 open/close 시 `onTabBarVisibilityChange` 호출, 모달 표시 상태 관리.
  (현재 HomeScreen은 `onTabBarVisibilityChange`를 받지 않으므로 시그니처 추가 필요.)
- `navigation/RootScaffold.kt` — `HomeScreen`에 `onTabBarVisibilityChange = { isTabBarVisible = it }` 전달.
  (현재 `onCongestionTap = {}` 빈 콜백 — 모달이 HomeScreen 내부에서 열리도록 변경.)

### 삭제
- 없음.

## 기술적 결정사항

- **차트 라이브러리: Vico 채택**: spec이 Vico 또는 MPAndroidChart 중 선택 허용. Vico는 Compose 네이티브 API
  (`CartesianChartHost`)를 제공하고 smooth 애니메이션·축 커스터마이징을 지원해 Compose 프로젝트에 적합.
  대안 MPAndroidChart는 `AndroidView` 래핑이 필요해 제외.
- **모달 위치: `feature/home/modal/`**: 혼잡도 모달은 홈 화면 전용이므로 `feature/home` 하위에 배치 (컨벤션의
  `feature/{기능}/modal/` 패턴). 별도 탭 라우트를 만들지 않고 HomeScreen 내부 `remember` 상태로 열고 닫는다
  (`SaveStationModal` 등 기존 모달 패턴과 동일).
- **모달 상태 보유: 별도 ViewModel 신설 vs HomeViewModel 확장**:
  → **별도 경량 상태(Composable `remember` + CongestionManager 직접 호출은 지양)**. MVI 컨벤션을 따라
  `feature/home/modal/`에 `CongestionContract` + `CongestionViewModel`(hiltViewModel)을 신설한다.
  HomeViewModel은 카드 이모지 계산용으로 이미 CongestionManager를 쓰고 있어 책임이 다르므로 분리한다.
  (iOS도 `CongestionModalFeature`로 별도 분리됨.)
- **데이터 접근: ViewModel → CongestionManager 직접 주입**: CongestionManager는 이미 `@Singleton`이며
  Repository를 거치지 않는 정적 assets 로더이므로 ViewModel에 직접 주입한다 (HomeViewModel과 동일 방식).
- **선택 역 저장 경로: `LocalDataRepository.updateSaveSetting`**: 역 변경 시 `saveSetting.value`를 받아
  `copy(mainCongestionBaseStation = station)`로 업데이트. 초기 선택값도 `saveSetting`에서 읽는다.
- **`selectedHour` 상태는 모달 로컬 UI 상태**: iOS의 `@State selectedHour`처럼 차트 탭 강조는 순수 UI 상태이므로
  Composable `remember`로 보유 (ViewModel 상태에 넣지 않음). 역 변경 시 `selectedHour = null` 리셋.
- **빈 데이터 처리**: CongestionManager 신규 함수가 파싱 실패/역 없음/요일 데이터 없음 시 `emptyList()` 반환 →
  차트는 빈 리스트면 라인 미표시 (crash 없음). `availableStations`가 비면 칩 행 미표시.
- **모달 높이 500dp**: `Dimens`에 토큰 추가 여부는 1곳 사용이므로 모달 내부 인라인 상수로 둔다 (Dimens 규칙).

## 구현 순서

### Phase 1. 의존성 추가
- `gradle/libs.versions.toml` — `vico` 버전 + `vico-compose-m3` 라이브러리 항목 추가.
- `app/build.gradle.kts` — `implementation(libs.vico.compose.m3)` 추가.
- verify: Gradle sync 성공 / 빌드 통과.

### Phase 2. 데이터 레이어 (CongestionManager 확장)
- `CongestionManager`에 `suspend fun getCongestions(station): List<HourlyCongestion>` 추가
  (0~23시 정렬, 결측 시간대는 1~4시 한정 0 채움 기존 로직 재사용, 없으면 emptyList).
  - 반환 모델: `(hour: Int, percent: Int, level: Int)` — 기존 `CongestionLevel` 재사용 + hour 묶음
    (예: `feature/home/modal`에 `HourlyCongestion` data class 신설, 또는 dto에 추가).
  - `getDayData()`가 private이므로 신규 함수에서 재사용하도록 정리.
- verify: 단위 테스트 — 존재 역/없는 역/빈 데이터 케이스에서 정렬·결측 채움·emptyList 동작 확인 (Kotest).

### Phase 3. MVI Contract + ViewModel
- `feature/home/modal/CongestionContract.kt` — `CongestionUiState(selectedStation, availableStations,
  congestionData, nowHour)`, `CongestionIntent(OnAppear, StationTap(station))`, Effect 불필요(저장은 내부 처리).
- `feature/home/modal/CongestionViewModel.kt` (hiltViewModel) —
  - `OnAppear`: `availableStations = getAvailableStations()`, `nowHour = Calendar HOUR_OF_DAY`,
    `selectedStation = saveSetting.value.mainCongestionBaseStation`, `congestionData = getCongestions(selected)`.
  - `StationTap`: 동일 역이면 no-op, 아니면 `selectedStation` 갱신 + `updateSaveSetting` 저장 + 데이터 재조회.
- verify: ViewModel 테스트 — 역 탭 시 state 갱신 + `updateSaveSetting` 호출 검증 (MockK + Turbine).

### Phase 4. UI 컴포넌트
- `feature/home/modal/component/CongestionStationChips.kt` — 수평 스크롤 `LazyRow`, 선택 역 테두리(AppIconColor)+bold,
  `AnimatedTapBox` 탭 → `StationTap`. 빈 리스트면 미표시.
- `feature/home/modal/component/CongestionChart.kt` — Vico 라인 차트:
  - X축 레이블 0,3,6,9,12,15,18,21,23시, Y축 30% stride grid + "%" 레이블.
  - 현재 시간 포인트 빨간 점 + "%값%" annotation (`selectedHour == null`일 때만).
  - 차트 탭 → 해당 시간 RuleMark(수직선) + "N시 · M%" annotation, 현재 시간 탭 시 `selectedHour=null`.
  - 역 변경/탭 선택 시 smooth 애니메이션.
- verify: Compose Preview에서 칩/차트 렌더 확인 (라이트·다크).

### Phase 5. 모달 조립 + 화면 연결
- `feature/home/modal/CongestionModal.kt` — `CommonModalBottomSheet`(mainTitle "현재 지하철 예상 혼잡도",
  subTitle "선택된 지하철역의 예상 혼잡도를 확인할 수 있어요.", sheetHeight 500dp) 안에 칩 + 차트 배치.
  `onAppear` 시 Intent 전달, `selectedHour` 로컬 remember.
- `feature/home/HomeScreen.kt` — `onTabBarVisibilityChange: (Boolean) -> Unit` 파라미터 추가,
  혼잡도 모달 표시 상태 `remember`. `NavigateToCongestion` effect 수신 시 모달 open + 탭바 숨김,
  닫힐 때 모달 close + 탭바 복원.
- `navigation/RootScaffold.kt` — `HomeScreen(onTabBarVisibilityChange = { isTabBarVisible = it }, ...)` 연결.
  (`onCongestionTap` 빈 콜백 정리 — 모달이 HomeScreen 내부에서 처리되도록.)
- verify: 빌드 통과 + 수동 확인 — 혼잡도 버튼 탭 시 슬라이드업, 탭바 숨김/복원, 칩 선택·차트 갱신.

## 완료 조건
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
