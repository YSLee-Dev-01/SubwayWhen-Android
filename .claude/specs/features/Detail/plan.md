# Plan: Detail (역 상세 — 실시간 도착 + 시간표)

## 참조 Spec
- @specs/features/Detail/spec.md

## 참조 Skill
신규 화면 생성 시
- @skills/create-feature/SKILL.md (해당 시)
- Contract / Screen / ViewModel 3파일 세트 + component/ 패턴 (conventions.md)

## 참조 iOS 코드
- /Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Detail

---

## 현재 상태 파악

### 신규
- `feature/detail/` 폴더 (Detail 화면 + DetailResultSchedule 하위 화면)
  - `DetailContract.kt` — UiState, Intent, Effect
  - `DetailScreen.kt` — Composable 진입점
  - `DetailViewModel.kt` — 실시간/시간표 로드 + 15초 타이머
  - `DetailSendModel.kt` — 진입 파라미터 모델 (`@Serializable`, iOS DetailSendModel 대응)
  - `component/` — DetailArrivalSection, DetailScheduleSection, 열차 위치 애니메이션 뷰, 카운트다운/새로고침 뷰
  - `resultschedule/` (하위 화면)
    - `DetailResultScheduleContract.kt`
    - `DetailResultScheduleScreen.kt`
    - `DetailResultScheduleViewModel.kt`
    - `component/` — 시간(Hour) 섹션 헤더, 시간표 셀
- `feature/detail/mapper/DetailMapper.kt` — DTO → 표시 모델 변환 (실시간/시간표 라인별 분기)
- Navigation 라우트 상수: `NavRoutes.Detail`, `NavRoutes.DetailResultSchedule` (인자 직렬화 포함)

### 재사용 (신규 컴포넌트 생성 금지 — 기존 것 사용)
| 컴포넌트 | 위치 | 사용처 |
|---------|------|-------|
| `CommonTopBar` | `ui/common/CommonTopBar.kt` | 상단 헤더 (뒤로가기 + 역명, isSubTitleVisible 스크롤 연동) |
| `StationLineCircle` | `ui/common/StationLineCircle.kt` | 호선 색상 원형 뱃지 |
| `SubwayLineMapper` | `ui/common/SubwayLineMapper.kt` | 호선명 → Color |
| `AnimatedTapBox` | `ui/common/AnimatedTapBox.kt` | 새로고침 버튼 · 시간표 더보기 · `...` 버튼 · 시간표 셀 탭 |
| `MainBgCard` | `ui/common/MainBgCard.kt` | 실시간 섹션 · 시간표 섹션 배경 카드 |
| `UpDownExceptionRow` | `ui/common/UpDownExceptionRow.kt` | exceptionLastStation 설정 시 두 번째 열차 "제외 행 설정됨" 안내 |
| `PrimaryButton` | `ui/common/PrimaryButton.kt` | 시간표 더보기 버튼 |
| `CommonModalBottomSheet` | `ui/common/modal/` | DetailResultSchedule 제외 행 확인 다이얼로그 |
| `Dimens` | `ui/theme/Dimens.kt` | 여백 · 폰트 · `animationDurationMs` |

### 의존 (데이터 레이어 — 기존 그대로 사용, 시그니처 수정 없음)
- `TotalLoadModel.liveArrivalSplit(stationName, line)` — 실시간 도착 정보 (상/하행 분리). spec의 iOS `singleLiveAsyncData` 대응. `upDown` 값으로 상/하행 리스트 선택.
- `TotalLoadModel.seoulScheduleLoad / korailScheduleLoad / shinbundangScheduleLoad` — 시간표. spec의 iOS `scheduleDataFetchAsyncData` 라인 분기 대응 (HomeViewModel.handleScheduleTap 동일 패턴).
- DTO: `LiveStationModel` / `RealtimeStationArrival`(`useState` 보유), `ScheduleStationModel`, `ProcessedKorailSchedule`, `ProcessedShinbundangSchedule`
- `NetworkResult` — Success / Failure 분기

### 수정
- `navigation/NavRoutes.kt` — Detail · DetailResultSchedule 라우트 상수 추가
- `navigation/RootScaffold.kt` — Detail / DetailResultSchedule `composable` 등록, slide 전환, 탭바 숨김(`onTabBarVisibilityChange(false)`) 연동
- `navigation/RootScaffold.kt` `HomeScreen(onNavigateToDetail = ...)` — 현재 빈 람다 → Detail 라우트로 이동 연결
- `feature/home/HomeScreen.kt` / `HomeContract` — `NavigateToDetail(cell)` Effect를 `onNavigateToDetail(DetailSendModel)` 콜백으로 전달 (HomeCellData → DetailSendModel 변환). 기존 Effect 시그니처 유지 범위에서 최소 변경.

### 삭제
- 없음

---

## 기술적 결정사항

- **실시간 API: 기존 `liveArrivalSplit` 재사용**: spec이 인용한 iOS `singleLiveAsyncData`는 Android에 동명 메서드가 없다. 이미 존재하는 `liveArrivalSplit(stationName, line)`이 상/하행 분리 + `backStationName` 파생까지 수행하므로 그대로 쓰고, `DetailSendModel.upDown`으로 방향 리스트를 선택한다. → TotalLoadModel 시그니처 신규 추가하지 않음 (Simplicity First).
- **시간표 API: HomeViewModel.handleScheduleTap 라인 분기 재사용**: 신분당선 → `shinbundangScheduleLoad`, korailCode 보유 → `korailScheduleLoad`, 그 외 → `seoulScheduleLoad`. `.Unowned`(공항철도·우이신설·경강선·서해선·GTX-A) 판별은 매퍼/ViewModel에서 호선명 기준으로 처리하고 API 호출 생략 → "시간표 미제공".
- **진입 파라미터 전달: JSON 직렬화**: `DetailSendModel`에 `@Serializable` 부여 후 `kotlinx.serialization`으로 인코딩하여 Navigation string argument로 전달. 프로젝트가 Parcelable 미사용·kotlinx.serialization 표준이므로 일관성 위해 JSON 선택. (SaveStation도 `@Serializable`)
- **15초 타이머: viewModelScope coroutine**: `viewModelScope.launch` 안에서 `delay` 루프로 카운트다운(15→0). ViewModel `onCleared` / 화면 이탈 시 Job 취소. `GlobalScope` 금지 규칙 준수.
- **수동 새로고침 1.2초 쿨타임**: 마지막 새로고침 시각 비교로 쿨타임 중 Intent 무시. iOS `lastRefreshTime` 대응.
- **열차 위치 애니메이션: code → 위치 인덱스 매핑 후 `animateFloatAsState`**: 이전역(0) / 현재역(1) / 다음역(2) 3구간에서 열차 상태 code(0~5,99)를 위치값으로 환산, `tween(durationMillis = Dimens.animationDurationMs)`로 슬라이드. 급행 여부에 따라 아이콘 분기. Live Activity는 대응 없음 → 제외 (spec 명시).
- **상태 모델: data class UiState**: 필드가 많고(실시간 리스트·시간표 리스트·타이머·로딩 플래그·역명) sealed보다 data class 적합 (conventions.md).
- **`...` 버튼(Realtime) — TODO 처리**: 버튼만 배치, 탭 시 동작은 빈 콜백 + `// TODO: Realtime 화면 연결` 주석. Realtime 화면 미구현 (ios-reference.md P3 ❌).

---

## 구현 순서

### Phase 1. 진입 모델 · 네비게이션 배선
- `DetailSendModel.kt` 작성 (`@Serializable`, 필드: upDown / stationName / lineNumber / stationCode / lineCode / exceptionLastStation / korailCode)
- `HomeCellData` → `DetailSendModel` 변환 함수 (매퍼 또는 확장 함수)
- `NavRoutes`에 Detail / DetailResultSchedule 라우트 + 인자 키 추가
- `RootScaffold`에 `composable` 등록(slide 전환), Home `onNavigateToDetail` 연결, 탭바 숨김 콜백 연동
- 검증: 홈 역 카드 탭 → 빈 Detail 화면 진입 + 인자(역명/호선) 표시 + 탭바 숨김

### Phase 2. 실시간 도착 섹션
- `DetailContract.kt` — UiState(실시간 리스트·로딩·타이머·이전/현재/다음역명), Intent(OnAppear / Refresh / ScheduleMore / ExceptionRowTap / RealtimeTap / Back / OnDisappear), Effect(NavigateToResultSchedule / NavigateBack)
- `DetailViewModel` — `liveArrivalSplit` 호출 후 `upDown`으로 방향 선택, 첫·두 번째 열차 추출, `backStationName`/`nextStationName` 반영
- `DetailMapper` — `RealtimeStationArrival` → 도착시간 표시(useTime 규칙)·상태(useState)·급행·목적지·열차번호
- `component/DetailArrivalSection` — MainBgCard 위에 1·2번째 열차, exceptionLastStation 시 UpDownExceptionRow, `...` 버튼(TODO), 데이터 없음 안내
- 검증: 진입 즉시 첫·두 번째 열차 표시, 실패 시 빈 상태 + 재시도 안내

### Phase 3. 열차 위치 애니메이션 · 타이머 · 새로고침
- `component/DetailTrainPositionView` — 이전/현재/다음역 3구간 + 열차 아이콘 위치 `animateFloatAsState(tween(animationDurationMs))`, 급행/일반 아이콘 분기
- 15초 카운트다운 타이머(viewModelScope) → 0초 도달 시 실시간 재호출 + 시간표 정렬 재수행, 화면 이탈 시 취소
- 수동 새로고침 버튼(AnimatedTapBox) + 1.2초 쿨타임, 카운트다운 UI(15→0)
- 검증: 15초 주기 자동 새로고침·카운트다운 동작, 수동 새로고침 쿨타임, 새로고침 시 아이콘 슬라이드, 화면 이탈 시 타이머 정지

### Phase 4. 시간표 섹션 (Detail 내 요약)
- `DetailViewModel` — 라인 분기 시간표 로드(`Unowned` 시 호출 생략), 현재 시간 이후 필터(없으면 전체 표시)
- `DetailMapper` — Seoul/Korail(`0HMMSS`→`HH:MM`)/Shinbundang(0시→24시) → "N분 후 / HH:MM / 목적지행 / 급행" 통일 모델
- `component/DetailScheduleSection` — 2열 그리드, "시간표 미제공"(Unowned), PrimaryButton "시간표 더보기"
- 검증: 라인 유형별 시간표 표시, 현재 시간 이후 필터, Unowned 안내

### Phase 5. DetailResultSchedule (시간표 전체 보기)
- `resultschedule/` Contract/Screen/ViewModel 3파일 + `component/`
- 시간(Hour)별 섹션 분리, 셀(분·목적지·시작역·급행), 현재 시간 섹션 자동 스크롤(`LazyColumn` + `rememberLazyListState`)
- 제외 행 버튼 → `CommonModalBottomSheet` 확인 → `exceptionLastStation` 갱신 후 Detail 재로딩(Effect로 결과 반환 또는 SavedStateHandle)
- RootScaffold 라우트 등록 + Detail → ResultSchedule 인자 전달
- 검증: 시간별 섹션 + 자동 스크롤, 제외 행 설정 시 Detail 실시간 재로딩

### Phase 6. 통합 · 마감
- HomeCellData → DetailSendModel 변환 정확도 확인(상/하행·korailCode·exceptionLastStation)
- 에러/빈 상태(실시간 실패 시 타이머 유지, 네트워크 없음 시 마지막 데이터 유지) 점검
- 미사용 import 정리, Dimens 2회 이상 반복값 토큰화 검토
- 검증: 빌드 통과, Acceptance Criteria 전 항목 수동 확인

---

## 완료 조건
- [ ] Spec Acceptance Criteria 전 항목 충족
- [ ] 홈 역 카드 탭 → Detail 진입, 실시간·시간표 즉시 로딩
- [ ] 15초 자동 새로고침 + 카운트다운 + 열차 위치 애니메이션
- [ ] 수동 새로고침 1.2초 쿨타임
- [ ] 라인 유형별 시간표(Seoul/Korail/Shinbundang/Unowned) 처리
- [ ] DetailResultSchedule 시간별 섹션·자동 스크롤·제외 행 설정 → Detail 재로딩
- [ ] 화면 이탈 시 타이머 취소
- [ ] 실시간/시간표 API 실패 시 빈 상태 처리
- [ ] `...` 버튼 배치 + Realtime 연결 TODO 주석
- [ ] 신규 공통 컴포넌트 생성 없이 기존 것만 활용
- [ ] 빌드 통과 · 미사용 import 정리
