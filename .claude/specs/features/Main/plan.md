# Plan: Main (메인 화면)

## 참조 Spec
- @specs/features/Main/spec.md

## 참조 Swift 파일
- /Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Main

## 현재 상태 파악

### 신규 생성 파일

#### data/remote/totalload (데이터 레이어)
- `data/remote/totalload/TotalLoadModel.kt` — 인터페이스 메서드 정의 (현재 빈 인터페이스 → 실제 메서드 추가)
  - `arrivalDataLoad(stations: List<SaveStation>): Flow<IndexedValue<NetworkResult<LiveStationModel>>>` — 여러 역 실시간 병렬 로드, 도착 순으로 Flow 방출
  - `seoulScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<ScheduleStationModel>` — 서울 시간표
  - `korailScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<KorailHeader>` — 코레일 시간표
- `data/remote/totalload/TotalLoadModelImpl.kt` — LoadModel 주입, arrivalDataLoad는 `channelFlow` + 역별 `launch` 병렬 처리

#### di
- `di/NetworkModule.kt` — `TotalLoadModel` → `TotalLoadModelImpl` Hilt Singleton 바인딩 추가

#### feature/home (메인 화면 본체)
- `feature/home/HomeContract.kt` — `HomeUiState`, `HomeIntent`, `HomeEffect` 정의
- `feature/home/HomeCellData.kt` — 카드 1건의 표시 모델 (iOS `MainTableViewCellData` 대응). `useTime`, `useFast` 계산 프로퍼티 포함
- `feature/home/HomeCellType.kt` — `enum class HomeCellType { Loading, Real, Schedule }` (iOS `MainTableViewCellType` 대응)
- `feature/home/HomeMainTitle.kt` — 요일별 랜덤 타이틀 메시지 셋 + 선택 로직 (iOS `MainModel.mainTitleLoad` 대응)

#### feature/home/mapper (데이터 매핑)
- `feature/home/mapper/HomeCellMapper.kt` — 변환 헬퍼 묶음
  - `SaveStation.toLoadingCell()` → `HomeCellData(type=Loading)`
  - `LiveStationModel.toRealCells(base: SaveStation)` — 응답에서 updnLine 매칭하여 `HomeCellData(type=Real)` 생성
  - `ScheduleStationModel.toScheduleCell(prev: HomeCellData)` → `HomeCellData(type=Schedule)` (서울 시간표)
  - `KorailHeader.toScheduleCell(prev: HomeCellData)` → `HomeCellData(type=Schedule)` (코레일 시간표)
  - `timeGroup(oneTime: Int, twoTime: Int, nowHour: Int): SaveStationGroup?` — iOS `MainModel.timeGroup` 포팅

#### feature/home/component (헤더/탭/카드 구성요소)
- `feature/home/component/HomeHeaderSection.kt` — 혼잡도 카드 + 민원/편집 버튼 행 묶음 (iOS `MainTableHeaderView`)
- `feature/home/component/HomeCongestionCard.kt` — 90dp 높이 혼잡도 카드 버튼 (제목 + 이모지 10개 + chevron) (iOS `MainTableHeaderSubView`)
- `feature/home/component/HomeHeaderActionButton.kt` — 민원/편집 공용 버튼 (제목 + 아이콘, 50%/50%) (iOS `MainTableHeaderViewBtn`)
- `feature/home/component/HomeGroupTabBar.kt` — 출근(ONE)/퇴근(TWO) 탭. 선택 70% : 미선택 30% 너비 애니메이션 (iOS `MainTableHeaderGroupView`)
- `feature/home/component/HomeStationCard.kt` — 역 카드 1건 (호선 원형 뱃지 + 역명 + 상태 + 도착 시간/인디케이터 + 시간표 버튼) (iOS `MainTableViewArrivalCell`)
- `feature/home/component/HomeEmptyStationCard.kt` — 저장 역 0개 안내 카드 (iOS `MainTableViewDefaultCell`)
- `feature/home/component/HomeMainTitleHeader.kt` — 상단 타이틀 텍스트 (요일 메시지)

### 재사용 (변경 없음)
- `data/repository/LocalDataRepository` — `saveStations`, `saveSetting` StateFlow 구독
- `data/remote/loadmodel/LoadModel` — TotalLoadModelImpl 내부에서만 사용
- `data/model/SaveStation`, `SaveStationGroup`, `SaveSetting` — 그대로 사용
- `data/remote/dto/liveArrival/LiveStationModel` (`RealtimeStationArrival`) — 매핑 입력
- `data/remote/dto/scheduleArrival/seoul/ScheduleStationModel`, `korail/KorailScheduleModel` — 시간표 매핑 입력
- `ui/common/StationLineCircle`, `SubwayLineMapper`, `AnimatedTapBox`, `ui/theme/Dimens`

### 수정 파일
- `data/remote/totalload/TotalLoadModel.kt` — 빈 인터페이스 → 3개 메서드 추가
- `di/NetworkModule.kt` — TotalLoadModel 바인딩 추가 (파일이 없으면 신규 생성)
- `feature/home/HomeViewModel.kt` — 빈 ViewModel을 실제 로직으로 채움. `TotalLoadModel`, `LocalDataRepository` Hilt 주입
- `feature/home/HomeScreen.kt` — placeholder를 실제 화면으로 교체. 헤더 + 그룹 탭 + LazyColumn(역 카드) + Pull-to-Refresh. 콜백 파라미터 추가
- `navigation/RootScaffold.kt` — `HomeScreen` 호출부에 신규 콜백 전달 (`onNavigateToSearch` = 검색탭 이동, 나머지는 빈 람다)
- `app/src/main/res/values/strings.xml` — 메인 전용 문자열 추가
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
  - 요일별 string-array (`home_main_title_monday` ~ `_weekend`)

---

## 기술적 결정사항

- **TotalLoadModel이 모든 데이터 접근의 진입점**: `HomeViewModel`은 `TotalLoadModel`만 주입받고 `LoadModel`을 직접 참조하지 않는다. `LoadModel`은 `TotalLoadModelImpl` 내부에서만 사용.
- **`arrivalDataLoad`는 `channelFlow` + 역별 `launch` 병렬 처리**: 각 역 요청이 독립적으로 발사되고, 응답이 오는 순서대로 `IndexedValue<NetworkResult<LiveStationModel>>`를 Flow로 방출한다. iOS `totalLiveDataLoad`의 RxSwift `merge` 동작 대응.
- **그룹/저장역 변경 시 in-flight 요청 cancel**: ViewModel에서 `loadJob: Job?` 보관, 새 요청 시 `cancel()`. iOS의 `share()` + filter 패턴을 Coroutine cancellation으로 단순화.
- **korail/서울 시간표 분기는 ViewModel(Mapper)에서**: `SaveStation.korailCode.isNotEmpty()` 여부로 `TotalLoadModel.korailScheduleLoad` vs `seoulScheduleLoad` 호출을 결정한다. TotalLoadModel은 두 메서드를 각각 노출.
- **`HomeCellData` 표시 모델을 feature 레이어에 신규 정의**: iOS `MainTableViewCellData`의 `useTime`/`useFast` 계산 규칙을 한 곳에 모음. DTO를 화면이 직접 참조하지 않는다.
- **요일별 메시지는 string-array 리소스 + Screen에서 random**: ViewModel은 요일 인덱스(Calendar.DAY_OF_WEEK)만 계산하고, Screen에서 `stringArrayResource` + `remember { messages.random() }` 로 처리.
- **혼잡도 이모지는 고정 `"🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥"`**: UiState의 `congestionEmoji: String` 상수로 주입. CongestionManager 연동 후 교체 가능.
- **호선 원형 뱃지 60dp**: 메인 카드 전용 1회 사용이므로 컴포저블 인라인. 재사용 시 `Dimens.homeStationCircleSize`로 승격.
- **Pull-to-Refresh는 Material3 `PullToRefreshBox`**: 별도 라이브러리 추가 없음.
- **콜백만 정의**: `onNavigateToDetail`, `onCongestionTap`, `onReportTap`, `onEditTap`은 파라미터만 정의. `RootScaffold`는 현재 빈 람다 전달.

---

## 구현 순서

### Phase 1. TotalLoadModel 구현 + DI 등록
- `TotalLoadModel.kt` — 3개 메서드 추가
- `TotalLoadModelImpl.kt` — `channelFlow` 기반 병렬 arrival, schedule 위임
- `NetworkModule.kt` — TotalLoadModel 바인딩
- 검증: 컴파일 성공

### Phase 2. 표시 모델 + 매핑 + 문자열 리소스
- `HomeCellType.kt`, `HomeCellData.kt` (`useTime`/`useFast` 계산 포함)
- `HomeCellMapper.kt`
- `HomeMainTitle.kt` (요일 인덱스 계산)
- `strings.xml` 문자열 추가
- 검증: `HomeCellData.useTime` 단위 테스트 (JUnit + Kotest)

### Phase 3. Contract + ViewModel
- `HomeContract.kt`
  - `HomeUiState(mainTitle, congestionEmoji, currentGroup, cells, isRefreshing)`
  - `HomeIntent`: `OnAppear`, `Refresh`, `GroupTap`, `StationTap`, `ScheduleTap`, `CongestionTap`, `ReportTap`, `EditTap`, `EmptyAddTap`
  - `HomeEffect`: `NavigateToSearch`, `NavigateToDetail`, `NavigateToCongestion`, `NavigateToReport`, `NavigateToEdit`
- `HomeViewModel.kt`
  - `TotalLoadModel`, `LocalDataRepository` Hilt 주입
  - `init`: `saveStations` + `saveSetting` 결합 → 자동 그룹 결정 → 데이터 로드
  - `loadGroupData()`: 로딩 카드 즉시 표시 → `arrivalDataLoad` Flow collect → 카드 교체
  - `handleScheduleTap()`: 로딩 전환 → korail/서울 분기 → 결과 카드 교체
  - API 실패 → 카드 상태 메시지(`home_arrival_no_data`)로 흡수
- 검증: ViewModel 단위 테스트 (MockK + Turbine)

### Phase 4. 정적 컴포넌트
- `HomeMainTitleHeader.kt`
- `HomeCongestionCard.kt` (full-width, h=90dp, 이모지 고정)
- `HomeHeaderActionButton.kt` (50% width, 아이콘 + 텍스트)
- `HomeHeaderSection.kt` (혼잡도 + 민원/편집 + "실시간 현황" 라벨)
- `HomeGroupTabBar.kt` (`animateFloatAsState`로 70/30 너비 전환)
- `HomeStationCard.kt` (60dp 원형, 역명|종착역, 상태, 도착시간/인디케이터, 시간표 버튼)
- `HomeEmptyStationCard.kt`
- 각 컴포넌트 Light/Dark Preview 동봉, stateless
- 검증: Preview 레이아웃 확인

### Phase 5. Screen 조립 + Navigation 연결
- `HomeScreen.kt` 재작성
  - 시그니처: `HomeScreen(viewModel, onNavigateToSearch, onNavigateToDetail, onCongestionTap, onReportTap, onEditTap)`
  - `PullToRefreshBox` → `LazyColumn` { Header, GroupTabBar, 역 카드들 or 빈 카드 }
- `RootScaffold.kt` 호출부 수정
- 검증: 빌드 + 홈 탭 동작 확인 (헤더/탭/카드/빈 상태/Pull-to-Refresh)

---

## 완료 조건
- [ ] Spec Acceptance Criteria 13개 항목 모두 충족
- [ ] `TotalLoadModel` 인터페이스 메서드 정의 및 Impl 구현 완료
- [ ] `HomeViewModel` 단위 테스트 통과 (그룹 전환 / 실시간 업데이트 / 시간표 전환)
- [ ] `HomeCellData.useTime` / `useFast` 단위 테스트 통과
- [ ] Light/Dark Preview에서 헤더·탭·카드·빈 상태가 의도대로 렌더링
- [ ] 빌드 성공
- [ ] `LoadModel`은 `TotalLoadModelImpl` 외부에서 직접 참조되지 않음
- [ ] Detail/혼잡도/민원/편집은 콜백 정의까지만 — 실제 화면 이동 없음
