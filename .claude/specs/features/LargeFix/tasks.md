# Tasks: LargeFix

## 참조
- spec: `.claude/specs/features/LargeFix/spec.md`
- plan: `.claude/specs/features/LargeFix/plan.md`

## Task 목록

### Phase 1. 공통 컴포넌트 (영향 범위 큰 것 우선)

#### [x] Task 1 — `CommonTopBarScreen.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/CommonTopBarScreen.kt`
- 큰 타이틀에 적용된 `offset(y = -titleOffsetY)` 음수 오프셋을 제거하여 TopBar에 가려지던 타이틀 가림 문제 수정
- `CommonTopBarScreen`과 `CommonTopBarLazyContent` 두 컴포넌트 모두 동일하게 처리
- 오프셋 제거 후 TopBar와의 간격이 자연스럽게 유지되는지 확인 (필요 시 양수 패딩으로 조정)

---

#### [x] Task 2 — `StationRow.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/StationRow.kt`
- `showUpDown: Boolean = true` 파라미터 추가
- `circleSize: Dp`, `circleFontSize: TextUnit` 파라미터 추가 (기본값 = 기존 값)
- `showUpDown = false`일 때 상하행 관련 뷰(`updnLine` 텍스트/컴포넌트)를 표시하지 않도록 분기 처리
- 기본값 `true`이므로 기존 호출부(Edit 등) 동작은 그대로 유지

---

#### [x] Task 3 — `Dimens.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/theme/Dimens.kt`
- Edit/Search결과/출퇴근 셀에서 2회 이상 반복되는 원형 뱃지 축소 사이즈를 토큰으로 추가
  - 예: `stationCircleSizeSmall` (기존 뱃지 사이즈 × 0.6, 약 42dp) — Edit/Search결과/출퇴근 공통 사용
- 폰트 축소 후 기존 토큰(`fontSizeMediumSmall` = 11sp 등)으로 대응 가능하면 신규 추가 불필요, 대응 불가 시에만 추가
- 반복 사용 여부 확인 후 단일 사용 값은 인라인으로 유지 (토큰 규칙 준수)

---

### Phase 2. Home 화면

#### [x] Task 4 — `HomeEmptyStationCard.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/component/HomeEmptyStationCard.kt`
- + 아이콘(Lottie) size를 현재 값의 60%로 축소 (102dp → 61dp)

---

#### [x] Task 5 — `CongestionModal.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/CongestionModal.kt`
- `sheetHeight = 500.dp` 고정 높이 제거 — 콘텐츠 높이에 따라 자동 조정되도록 변경
- `CongestionChart`에 적용된 중복 `padding(bottom = 20.dp)` 제거하여 다른 Modal과 닫기 버튼 하단 여백을 일치시킴
- `CommonModalBottomSheet`의 기본 하단 여백 처리를 그대로 활용

---

#### [x] Task 6 — `CongestionChart.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/modal/component/CongestionChart.kt`
- vico `FixedHourItemPlacer.getStartLayerMargin`이 Y축 라벨 폭(`maxLabelWidth`)을 반환하여 역마다 시작 마진이 달라지는 문제 수정
- 시작/끝 마진을 데이터 스케일과 무관한 고정 상수로 반환하도록 변경 (`context.density * 40f`)
- 데이터 스케일(최대값)은 `VerticalAxis.ItemPlacer.step` 그대로 유지

---

### Phase 3. Edit / Search 셀 축소 및 저장 완료 Modal

#### [x] Task 7 — `EditStationRow.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/component/EditStationRow.kt`
- 셀 전체 높이를 현재 대비 20% 축소 (paddingTB 7.5dp → 6dp)
- `StationRow` 호출부에 `circleSize = Dimens.stationCircleSizeSmall`, `circleFontSize = Dimens.fontSizeSuperSmall` 적용

---

#### [x] Task 8 — `SearchResultSection.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/component/SearchResultSection.kt`
- Edit 화면 Cell과 동일한 비율로 축소 (원형 뱃지 40%↓, 폰트 비례 축소)
- `stationCircleSizeSmall` 토큰 재사용, 폰트 `fontSizeSuperSmall`(9sp)

---

#### [x] Task 9 — `SaveCompletedModal.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/modal/component/SaveCompletedModal.kt`
- Lottie 체크 애니메이션 재생 속도를 현재값의 2배로 변경 (`speed = 2f`)

---

### Phase 4. Search Vicinity 버그 수정

#### [x] Task 10 — `LocationManagerImpl.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/core/location/LocationManagerImpl.kt`
- `getCurrentLocation` null 반환 시 `getLastLocation()` 폴백 추가 (GPS 콜드 스타트 대응)
- `locationAuthCheck`에 `ACCESS_COARSE_LOCATION` 폴백 추가

---

### Phase 5. Setting 화면

#### [x] Task 11 — `WorkAlarmStationView.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/component/WorkAlarmStationView.kt`
- `StationRow` 호출 시 `showUpDown = false`, `circleSize = Dimens.stationCircleSizeSmall`, `circleFontSize = Dimens.fontSizeSuperSmall` 전달

---

#### [x] Task 12 — `TrainIconModal.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/TrainIconModal.kt`
- 열차 아이콘을 담던 `Box(size=15dp)` 제거 → `Text`를 Row에 직접 배치 (Z-order 버그 + size 제약 해제)

---

#### [x] Task 13 — `SettingViewModel.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingViewModel.kt`
- `ContentsTapped` Intent 처리에서 `getContents()`(String 단건) 대신 `getLicenses()`(List<String>)를 호출하도록 교체
- `getLicenses()` 결과 리스트를 `"\n"`으로 join하여 `modalContents`에 주입

---

### Phase 6. Detail 열차 애니메이션 재구현

#### [x] Task 14 — `DetailTrainPositionView.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/detail/component/DetailTrainPositionView.kt`
- 기존 애니메이션 로직 전체 삭제 후 iOS `DetailArrivalView.swift` 기준으로 재구현
- `LaunchedEffect(isLoading)` 기반 단계별 애니메이션:
  - 로딩 시: 마커 slide out + 열차 숨김
  - 완료 시: 400ms delay → spring 마커 복귀 → 700ms 후 열차 표시 + easeInOut(400ms) 이동
- 트랙 바: iOS borderPosition/borderSize 방식 (offset + scale) 재현
- statusCode별 열차 위치 매핑 유지

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공
- [ ] 공통 컴포넌트(`CommonTopBarScreen`, `StationRow`) 수정이 홈/검색/편집/설정/상세 모든 화면에서 레이아웃 회귀 없음
- [x] Edit/Search/Setting 축소 후 폰트가 `fontSizeSuperSmall`(9sp) 미만으로 내려가지 않음 (fontSizeSuperSmall = 9sp 사용)
- [ ] Vicinity: 실기기 정상 케이스(근처 역 존재)와 권한 거부 케이스 모두 정상 동작
- [x] 기타 섹션: 네트워크 미연결 시에도 크래시 없이 빈 본문 처리 (getLicenses() emptyList() → "" join)

### 기능 (AC)
- [x] 홈/검색/편집/설정 화면에서 최상단 타이틀이 잘리지 않고 완전히 표시됨
- [x] 홈화면 빈 역 셀의 + 아이콘이 기존 대비 60% 크기로 표시됨
- [x] 혼잡도 Modal의 닫기 버튼 하단 여백이 다른 Modal과 동일함
- [x] 고속터미널 선택 시 혼잡도 그래프 좌우 여백이 다른 역과 동일함 (스케일은 유지)
- [x] Detail 화면 열차 도착 애니메이션이 iOS 기준으로 자연스럽게 동작함
- [x] Edit 화면 역 Cell이 기존 대비 80% 크기, 원형 뱃지가 60% 크기로 표시됨
- [ ] 가까운 지하철역 찾기 기능이 정상 동작하여 근처 역 목록을 표시함
- [x] 검색 결과 Cell 사이즈가 Edit 화면 Cell과 동일한 비율로 표시됨
- [x] 역 저장 완료 Modal의 체크 애니메이션이 기존보다 2배 빠르게 재생됨
- [x] 설정 화면 출퇴근 셀 원형 뱃지가 기존 대비 60% 크기로 표시됨
- [x] 설정 화면 출퇴근 셀에서 상하행 정보가 표시되지 않음
- [x] 설정 화면 열차 아이콘 탭 시 원형 아이콘 위에 열차 아이콘이 정상 표시됨
- [x] 설정 화면 기타 섹션에 Firebase에서 가져온 정보가 iOS와 동일하게 표시됨
