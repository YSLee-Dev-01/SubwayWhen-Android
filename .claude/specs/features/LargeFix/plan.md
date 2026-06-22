# Plan: LargeFix

## 참조 Spec
- @specs/features/LargeFix/spec.md

## 참조 Skill
- 신규 화면 생성 없음 — 기존 화면/컴포넌트 수정만 수행하므로 create-feature 미사용.

## 현재 상태 파악

### 신규
- 없음 (모든 항목이 기존 파일 수정/재구현).

### 재사용
- `ui/common/StationLineCircle.kt` — `size`/`fontSize` 파라미터를 이미 받으므로 호출부에서 값만 조정하면 뱃지 축소 가능.
- `ui/common/StationRow.kt` — Edit/Search/Setting 출퇴근 셀 공통 역 정보 Row. 단, `station.updnLine`(상하행)을 무조건 표시하므로 6-2에서 분기 필요.
- `ui/common/modal/CommonModalBottomSheet.kt` — 닫기 버튼 하단 여백을 일괄 관리(`confirmButton` 후 `Spacer(paddingLR)` + 외부 `Spacer(30dp)`).
- `data/remote/loadmodel/LoadModelImpl.kt#getLicenses()` — `SubwayWhen/Licenses` 노드를 읽는 함수가 이미 존재. 6-4에서 재사용.

### 수정
- **1. 타이틀 가림**: `ui/common/CommonTopBarScreen.kt` — `CommonTopBarScreen`/`CommonTopBarLazyContent` 모두 큰 타이틀에 `offset(y = -titleOffsetY)`로 **위로 7.5dp 끌어올려** TopBar(45dp)에 근접/가림. 5개 화면(Home/Search/Edit/Setting/Detail) 공통 진입점이므로 여기서 일괄 수정.
- **2-1. + 아이콘**: `feature/home/component/HomeEmptyStationCard.kt` — Lottie `size(102.dp)` → 60% (`61.dp` 내외).
- **2-2. 혼잡도 닫기 버튼 여백**: `feature/home/modal/CongestionModal.kt` — 이미 `CommonModalBottomSheet` 사용 중이나 `sheetHeight = 500.dp` 고정 + 본문 `CongestionChart`에 `padding(bottom = 20.dp)` 추가로 다른 Modal과 하단 여백이 달라짐. 고정 높이/중복 패딩 정리로 공통 여백 일치시킴.
- **2-3. 고속터미널 그래프 좌우 여백**: `feature/home/modal/component/CongestionChart.kt` — vico `FixedHourItemPlacer.getStartLayerMargin`이 `maxLabelWidth`(= Y축 라벨 폭) 반환. 고속터미널처럼 혼잡도(%) 최대값 자릿수가 크면 Y라벨 폭이 넓어져 시작 마진이 역마다 달라짐. 시작 마진을 데이터 스케일과 무관하게 고정.
- **3-1. 열차 애니메이션 재개발**: `feature/detail/component/DetailTrainPositionView.kt` — 전면 재구현. iOS `DetailArrivalView.swift` 기준.
- **4-1. Edit 셀 축소**: `feature/edit/component/EditStationRow.kt` + 호출되는 `StationRow` — 셀 높이 20%↓, 원형 뱃지 40%↓, 폰트 비례 축소.
- **5-1. 가까운 역 버그**: `feature/search/vicinity/SearchVicinityViewModel.kt` 디버깅 — 아래 "기술적 결정사항" 참조.
- **5-2. 검색 결과 셀 축소**: `feature/search/component/SearchResultSection.kt` — 4-1과 동일 비율(뱃지 40%↓, 폰트 비례).
- **5-3. 체크 애니메이션 속도**: `feature/search/modal/component/SaveCompletedModal.kt` — Lottie 재생 속도 2배(`speed = 2f`).
- **6-1 / 6-2. 출퇴근 셀**: `feature/setting/modal/component/WorkAlarmStationView.kt` (+ 6-2를 위해 `StationRow` 상하행 표시 제어 옵션 추가).
- **6-3. 열차 아이콘 가림**: `feature/setting/modal/TrainIconModal.kt` — 선택 아이콘 미리보기에서 열차 아이콘이 `size(15.dp)` Box 안에 그려져 clip/오버플로우로 원형 뒤로 가림. z-order 최상위 + 클립 해제.
- **6-4. 기타 섹션 Firebase**: `feature/setting/SettingViewModel.kt` + `feature/setting/modal/ContentsModal.kt` — Contents가 `SubwayWhen/Contents`(String, 빈 값) 읽어 미표시. iOS는 `SubwayWhen/Licenses` 리스트를 줄바꿈 join하여 표시. 데이터 소스를 Licenses로 교체.

### 삭제
- `DetailTrainPositionView.kt`의 기존 애니메이션 로직(`backMarker*`, `nextMarker*`, `targetOffsetPx` when 분기 등) — 3-1 재구현으로 대체.

## 기술적 결정사항

- **타이틀 가림(1) 수정 방식**: 큰 타이틀의 `offset(y = -titleOffsetY)`(위로 당김)를 제거하고 TopBar와의 간격을 양수 패딩으로 둔다. `CommonTopBarScreen`과 `CommonTopBarLazyContent` 두 곳 모두 동일 처리. → 대안: TopBar 높이만큼 contentPadding.top 추가. 그러나 실제 원인이 음수 offset이므로 offset 제거가 더 직접적. 5개 화면 공통 영향 → 각 화면 진입 시 타이틀/콘텐츠 시작 위치 육안 확인.
- **셀 축소 비율(4-1, 5-2, 6-1)**: 뱃지는 `StationLineCircle`의 `size`/`fontSize` 파라미터로 조정. `stationLineCircleSize`(70dp) 토큰은 Search 추천/모달 등 다른 곳에서도 쓰이므로 **토큰 값을 직접 줄이지 않고**, 축소가 필요한 호출부(Edit/Search결과/출퇴근)에 별도 축소 사이즈를 적용. 2회 이상 반복되면 `editStationCircleSize`(70×0.6=42dp) 같은 신규 토큰을 Dimens에 추가(토큰 규칙 준수). 폰트는 `fontSizeSmall`(13sp)→비례 축소 시 `fontSizeSuperSmall`(9sp) 미만으로 내려가지 않도록 `fontSizeMediumSmall`(11sp) 등 기존 토큰으로 맞춤.
- **상하행 표시 제거(6-2)**: `StationRow`에 `showUpDown: Boolean = true` 파라미터를 추가하여 기본 동작은 유지하고 출퇴근 셀(`WorkAlarmStationView`)에서만 `false` 전달. → 대안: 출퇴근 전용 Row 신설. 그러나 단일 플래그가 더 surgical.
- **가까운 역 버그(5-1) 원인 가설**: 실기기에서만 발생(에뮬레이터는 임시 데이터로 우회). 후보 — (a) `locationManager.locationAuthCheck()`가 권한 있어도 false 반환, (b) `locationRequest()`가 null 반환(타임아웃/콜드 GPS), (c) Kakao API 응답의 `category_group_code != "SW8"` 필터로 전부 탈락, (d) `radius=3000` 내 결과 0건. DTO 매핑(`category_group_code`→`category`)은 정상 확인됨. → **검증 순서**: `core/location/LocationManager`의 권한/좌표 수신 로깅 추가 → Kakao 응답 documents/필터 후 개수 로깅 → 실기기 로그로 어느 단계에서 0건이 되는지 특정 후 해당 지점 수정. 권한 거부/정상 두 케이스 모두 확인.
- **열차 애니메이션 재구현(3-1) 매핑**: iOS는 4개 `@State`(backStationPostion, nextStationPostion, trainPostion, borderPostion/borderSize)를 nowLoading 변화 시 단계적으로 구동. Compose에서는 `isLoading`을 trigger로 하는 단계별 애니메이션으로 옮긴다.
  - statusCode 의미(0:진입,1:도착,2:출발,3:전역출발,4:전역진입,5:전역도착,99:운행중).
  - 열차 아이콘 위치 = iOS `trainIconMoveValue(code)` 매핑(화면폭-80 기준 음수 오프셋, 우측 정렬 기준)을 Compose 좌표계로 변환.
  - 로딩 시: next 마커 +100/alpha0, back 마커 -100/alpha0, 열차 아이콘 숨김. 로딩 완료 시: half-slide(0.4s delay) → spring(0.5/0.7)로 마커 복귀 → 0.7s 후 열차 아이콘 표시 + trainPostion 이동(easeInOut 0.4s).
  - 트랙 바: 99(운행중)일 때 borderPostion=35/borderSize=1.2, 그 외 borderPostion=12 후 1.2배 확장 애니메이션.
  - 단, `DetailScreen`에서 전달하는 props(`prevStationName/currentStationName/nextStationName/statusCode/isFast/lineNumber/trainIcon/isLoading`)만으로 구현. iOS의 `nowSeconds` 기반 미세 이동(3분 도착 시 5px씩 당김)은 현 Contract에 `timerCount`가 있으나 별도 파라미터로 노출되어 있지 않으므로, 본 재구현 범위는 도착/위치 애니메이션까지로 한정하고 nowSeconds 미세이동은 필요 시 후속(스코프 확인 필요 — Acceptance엔 "자연스러운 위치 이동"만 명시).
- **혼잡도 그래프 좌우 여백(2-3)**: `FixedHourItemPlacer.getStartLayerMargin`이 Y라벨 폭에 비례하므로, 시작/끝 마진을 고정 상수로 반환하도록 변경(스케일=데이터 최대값은 `VerticalAxis.ItemPlacer.step` 그대로 유지). 바가 컨테이너를 넘치지 않는지(% 최대값 큰 역) 확인.
- **기타 Firebase(6-4)**: Android `ContentsTapped`에서 `getContents()`(String) 대신 `getLicenses()`(List<String>)를 호출하여 `"\n"`으로 join → `modalContents`에 주입. iOS와 동일한 `SubwayWhen/Licenses` 경로/구조 사용. 네트워크 미연결 시 빈 문자열 → 기존 빈 본문 처리 유지.
- **Dimens 토큰 규칙**: 2회 이상 반복되는 축소 사이즈/폰트만 토큰화. 단일 사용 값은 인라인.

## 구현 순서

### Phase 1. 공통 컴포넌트 (영향 범위 큰 것 우선)
- `CommonTopBarScreen.kt` 큰 타이틀 offset 수정(1) → verify: Home/Search/Edit/Setting/Detail 진입 시 최상단 타이틀 완전 표시.
- `StationRow.kt` `showUpDown` 파라미터 추가(6-2 전제) → verify: 기존 호출부(Edit 등) 상하행 표시 유지, 컴파일 통과.
- 필요한 축소 토큰을 `Dimens.kt`에 추가(반복되는 경우) → verify: 빌드.

### Phase 2. Home
- `HomeEmptyStationCard.kt` + 아이콘 60% 축소(2-1).
- `CongestionModal.kt` 닫기 버튼 하단 여백 정렬(2-2).
- `CongestionChart.kt` 시작/끝 마진 고정(2-3).
- verify: 홈 빈 상태 진입, 혼잡도 Modal 다른 Modal과 하단 여백 비교, 고속터미널 선택 시 좌우 여백 동일.

### Phase 3. Edit / Search 셀 축소
- `EditStationRow.kt`/`StationRow` 호출부 셀 20%↓·뱃지 40%↓·폰트 비례(4-1).
- `SearchResultSection.kt` 동일 비율(5-2).
- `SaveCompletedModal.kt` Lottie `speed = 2f`(5-3).
- verify: Edit/Search 셀 크기 일치 확인, 폰트 ≥ 9sp, 저장 완료 체크 2배 속도.

### Phase 4. Search Vicinity 버그 (5-1)
- `LocationManager` + `SearchVicinityViewModel.loadVicinityStations` 단계별 로깅 추가 → 실기기 재현 → 원인 단계 특정 → 수정 → 로깅 제거.
- verify: 실기기에서 근처 역 존재 시 목록 표시, 권한 거부 시 기존 거부 플로우 유지.

### Phase 5. Setting
- `WorkAlarmStationView.kt` 뱃지 40%↓·폰트 비례(6-1), `StationRow(showUpDown=false)`(6-2).
- `TrainIconModal.kt` 선택 아이콘 z-order/clip 수정(6-3).
- `SettingViewModel.ContentsTapped`를 `getLicenses()` join으로 교체(6-4).
- verify: 출퇴근 셀 뱃지 60% 크기·상하행 미표시, 열차 아이콘 탭 시 원형 위 표시, 기타 섹션에 라이선스 정보 표시.

### Phase 6. Detail 열차 애니메이션 재구현 (3-1)
- `DetailTrainPositionView.kt` 기존 로직 삭제 후 iOS `DetailArrivalView.swift` 기준 재구현.
- `DetailScreen.kt` 호출부 props 변경 없을 시 그대로, 추가 파라미터 필요 시 Contract에서 노출.
- verify: 상세 진입·15초 새로고침 시 statusCode별 열차 위치/마커 슬라이드/트랙바 확장이 iOS와 유사하게 자연스럽게 동작.

## 완료 조건
- [ ] Spec Acceptance Criteria 13개 항목 충족
- [ ] 공통 컴포넌트(`CommonTopBarScreen`, `StationRow`) 수정이 모든 사용 화면에서 레이아웃 회귀 없음
- [ ] Edit/Search/Setting 축소 폰트가 `fontSizeSuperSmall`(9sp) 미만으로 내려가지 않음
- [ ] Vicinity: 정상/권한 거부 두 케이스 모두 정상 동작
- [ ] 기타 섹션: 네트워크 미연결 시에도 크래시 없이 빈 본문 처리
- [ ] 빌드 및 기존 화면 진입 회귀 확인
