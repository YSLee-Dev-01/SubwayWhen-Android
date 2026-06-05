# Plan: Edit (저장한 역 편집)

## 참조 Spec
- @specs/features/Edit/spec.md

## 참조 Skill
- @skills/feature/SKILL.md (spec → plan → tasks → 구현 워크플로우)

## iOS 원본 참조
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Edit/EditVC.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Edit/EditViewModel.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Edit/EditModel.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Edit/Sub/EditViewCell.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Edit/Coordinator/EditCoordinator.swift` (notSaveAlert 동작 참고)

## 현재 상태 파악

### 신규 (생성)
- `feature/edit/EditContract.kt` — `EditUiState`, `EditIntent`, `EditEffect`
- `feature/edit/EditViewModel.kt` — 비즈니스 로직 (그룹별 정렬, 삭제, 이동, 그룹 변경, 저장, 뒤로가기 가드)
- `feature/edit/EditScreen.kt` — Compose 진입점 (Reorderable List + back handler + 미저장 팝업)
- `feature/edit/component/EditStationRow.kt` — 행 카드 (좌측 삭제 버튼 / 본문 / 우측 드래그 핸들)
- `feature/edit/component/NotSaveAlertDialog.kt` — "저장 / 저장하지 않음 / 취소" 액션 시트 형태 다이얼로그
- `navigation/NavRoutes.kt` 에 `Edit` 라우트 상수 추가

### 재사용
- `data/model/SaveStation`, `SaveStationGroup` — 그룹 분리/이동의 기준 모델
- `data/repository/LocalDataRepository` — `saveStations` StateFlow 구독 + `updateSaveStations(...)` 호출로 저장
- `ui/common/CommonTopBar` — 상단바 (뒤로가기 + "편집" 타이틀)
- `ui/common/PrimaryButton` — 하단 "저장" 버튼
- `ui/common/StationLineCircle`, `SubwayLineMapper` — 행의 호선 색상 원형 뱃지
- `ui/common/AnimatedTapBox` — 삭제 버튼 탭 피드백
- `ui/theme/Dimens` — 여백/폰트 토큰 (반복되는 신규 값이 있으면 추가)

### 수정
- `feature/home/HomeScreen.kt` / `RootScaffold.kt` 의 `onEditTap` 콜백 → Edit 화면으로 네비게이션 연결
  - 현재 `onEditTap = {}` (no-op) 이므로 실제 네비게이션 트리거 추가 필요
- `navigation/RootScaffold.kt` 의 탭 NavHost 에 `EditScreen` composable 추가
  - 또는 `AppNavHost.kt` 에 풀 스크린 라우트로 추가 (탭바 숨김 처리 필요)
- 신규 화면에서 탭바를 숨겨야 하므로 `RootScaffold` 의 `isTabBarVisible` 제어 경로 결정 필요

### 삭제
- 없음

## 기술적 결정사항

- **화면 위치 결정 — `RootScaffold` 내부 composable**: iOS는 `hidesBottomBarWhenPushed = true` 로 push 하므로, AOS도 `RootScaffold` 내부 NavHost 에 추가하고 `onTabBarVisibilityChange(false)` 로 탭바를 숨긴다. (대안: `AppNavHost` 풀스크린 라우트로 빼면 탭바 자동 미노출이지만, 기존 `Search` 모달 패턴과 일관성을 위해 `RootScaffold` 내부 + 탭바 숨김 방식 선택)
- **드래그 정렬 라이브러리**: Compose 표준에 reorderable list가 없으므로 `sh.calvin.reorderable:reorderable` 의존성 추가 권장. 단순 LazyColumn drag handle 패턴으로 출근/퇴근 두 섹션 간 이동까지 지원 가능. (대안: AnchoredDraggable + 수동 indexOf 스왑 — 구현 비용 큼. iOS는 두 섹션 간 이동을 지원하므로 cross-section drag가 가능한 라이브러리 채택)
- **저장 트리거**: iOS `FixInfo.saveStation = newValue` 와 동일하게 `LocalDataRepository.updateSaveStations(...)` 1회 호출. ViewModel 내부에서 `lastSaved` 와 `current` 두 리스트를 들고 비교해 dirty 여부 계산.
- **뒤로가기 가드**: Android `BackHandler` 사용. dirty 상태일 때만 가로채 다이얼로그를 띄우고, 아니면 시스템 back 통과.
- **미저장 다이얼로그 형식**: Material3 `AlertDialog` 사용. iOS actionSheet 3버튼("저장" / "저장하지 않음" / "취소") 그대로 매핑. spec 본문 오타("않았아요." / "않아요,")는 자연스러운 한국어("않았어요." / "않아요.")로 표기.
- **빈 상태**: iOS `noListLabel` 대응 — `cells` 양 섹션 모두 비었을 때 "현재 저장되어 있는 지하철역이 없어요." 텍스트 중앙 표시.
- **저장 버튼 활성화**: iOS와 동일하게 dirty 일 때만 enabled. dirty=false 이면 disabled (PrimaryButton 의 enabled 속성).
- **삭제 시 알림 ID 정리 / 신분당선 시간표 정리**: iOS 의 `notiManager.notiRemove`, `coreDataManager.shinbundangScheduleDataRemove` 는 AOS 미구현 기능(알림 P2, Room 캐시 미사용)이므로 본 작업 범위에서 제외한다.
- **위젯 reload**: iOS `WidgetCenter.shared.reloadTimelines(...)` 는 AOS 위젯 미구현(P3)이므로 제외.
- **이동 시 그룹 자동 변경**: iOS는 cross-section 이동 시 옮긴 아이템의 `group` 을 반대 그룹으로 갱신한다. AOS도 동일하게 처리한다.
- **정렬 순서**: spec "출근이 먼저" → 저장 직렬화 순서는 `ONE` 전체 + `TWO` 전체 (iOS `groupOne.items + groupTwo.items` 와 동일).
- **불변 조건 대응**: spec "데이터를 로드하지 못하더라도 편집 화면은 표출 되어야 함" → `LocalDataRepository.saveStations` 가 빈 리스트더라도 화면은 정상 렌더, "저장 역 없음" 안내만 표출. 별도 에러 UiState 분기 불필요.

## 구현 순서

### Phase 1. 의존성 / 라우팅 준비
- `libs.versions.toml` + `app/build.gradle.kts` 에 reorderable 라이브러리 추가
- `navigation/NavRoutes.kt` 또는 `RootScaffold` 내부에 `edit` 라우트 상수 정의
- `RootScaffold` 에 `EditScreen` composable 등록 + 탭바 visibility 콜백 연결
- `HomeScreen` 의 `onEditTap` → `childNavController.navigate("edit")` 연결
- → 검증: 홈에서 편집 버튼 탭 시 빈 EditScreen 표시되고 탭바 숨김

### Phase 2. 데이터/도메인 매핑
- `LocalDataRepository.saveStations` 를 받아 `Pair<List<SaveStation>, List<SaveStation>>` (출근, 퇴근) 으로 분리하는 매퍼 작성 (EditViewModel 내부 private function 또는 별도 mapper 파일)
- 양 리스트를 합쳐 `List<SaveStation>` 으로 직렬화하는 매퍼 (저장 시 사용)
- → 검증: 단위 테스트 (Kotest) — 그룹별 분리/병합 라운드트립

### Phase 3. Contract / ViewModel
- `EditContract.kt`
  - `EditUiState(groupOne: List<SaveStation>, groupTwo: List<SaveStation>, isSaveEnabled: Boolean, showNotSaveDialog: Boolean)`
  - `EditIntent`: `OnAppear`, `DeleteStation(SaveStation)`, `MoveStation(fromSection, fromIndex, toSection, toIndex)`, `SaveTap`, `BackTap`, `DialogSave`, `DialogDiscard`, `DialogCancel`
  - `EditEffect`: `NavigateBack`
- `EditViewModel.kt`
  - `init` 에서 `localDataRepository.saveStations.value` 로 1회 초기 로드 → `lastSaved` 보관 + 현재 상태 갱신
  - `isSaveEnabled` 계산: `current != lastSaved`
  - `DeleteStation` / `MoveStation` 처리 (cross-section 시 group 필드 갱신)
  - `SaveTap` / `DialogSave` 처리: `localDataRepository.updateSaveStations(...)` 호출 → dirty=false → `NavigateBack` 방출
  - `BackTap` 처리: dirty 이면 `showNotSaveDialog=true`, 아니면 `NavigateBack`
  - `DialogDiscard`: dialog 닫고 `NavigateBack`
  - `DialogCancel`: dialog 닫기만
- → 검증: ViewModel 단위 테스트 (Turbine + MockK) — 삭제/이동/저장/뒤로가기 시나리오

### Phase 4. UI — 행 컴포넌트
- `component/EditStationRow.kt`
  - 좌측: 빨간 원형 "−" 버튼 (`AnimatedTapBox` 래핑)
  - 본문: `StationLineCircle` + 역명 + 상하행
  - 우측: 드래그 핸들 아이콘 (`Icons.Filled.DragHandle` 또는 `Menu`)
  - 라이브러리 제공 `ReorderableItem` 으로 감싸 drag 제스처 연결
- → 검증: Preview (Light/Dark)

### Phase 5. UI — Screen
- `EditScreen.kt`
  - `CommonTopBar(title="편집", onBack = { viewModel.onIntent(BackTap) })`
  - `LazyColumn` 내 두 섹션 헤더("출근" / "퇴근") + 각 섹션 아이템
  - 양 섹션 모두 빈 경우 중앙에 "현재 저장되어 있는 지하철역이 없어요." 표시
  - 하단 `PrimaryButton(text="저장", enabled=isSaveEnabled, onClick={SaveTap})`
  - `BackHandler(enabled=true)` → `BackTap` Intent
  - `showNotSaveDialog == true` 일 때 `NotSaveAlertDialog` 표시
  - `LaunchedEffect` 로 `EditEffect.NavigateBack` 수집 → `onNavigateBack()` 콜백 호출
  - `LaunchedEffect(Unit)` 에서 `onTabBarVisibilityChange(false)`, `DisposableEffect` 로 복원
- → 검증: 실기기/에뮬레이터 수동 확인 — 삭제, 동일 섹션 내 이동, 두 섹션 간 이동, 저장, 뒤로가기 가드

### Phase 6. 다이얼로그
- `component/NotSaveAlertDialog.kt`
  - Material3 `AlertDialog`
  - title: "수정된 지하철역이 저장되지 않았어요.\n저장하지 않을 경우 변경된 내용은\n적용되지 않아요."
  - confirmButton: "저장" → `DialogSave`
  - dismissButton: "저장하지 않음" → `DialogDiscard`
  - 추가 Text 버튼: "취소" → `DialogCancel`
- → 검증: Preview + 수동 확인

### Phase 7. 통합 및 정리
- HomeScreen → Edit 네비게이션 동작 확인
- Edit 저장 후 Home 으로 복귀 시 그룹/순서가 반영되는지 확인 (Home 이 `localDataRepository.saveStations` 를 구독하므로 자동 반영 예상)
- 탭바 visibility 복원 확인
- 빈 상태 → 편집 진입 → "역 없음" 안내 표시 확인

## 완료 조건
- [ ] 홈 편집 버튼 탭 시 Edit 화면 진입, 탭바 숨김
- [ ] 저장된 역이 출근/퇴근 섹션으로 분리되어 표시 (출근 먼저)
- [ ] 좌측 "−" 버튼으로 행 삭제 가능
- [ ] 우측 드래그 핸들로 같은 섹션 내 순서 변경 가능
- [ ] 우측 드래그 핸들로 다른 섹션으로 이동 시 `group` 필드가 변경되어 저장됨
- [ ] 변경사항이 있을 때만 "저장" 버튼 활성화
- [ ] "저장" 버튼 탭 시 `LocalDataRepository` 에 반영 후 화면 pop
- [ ] dirty 상태에서 뒤로가기 시 "저장 / 저장하지 않음 / 취소" 3버튼 다이얼로그 표출
- [ ] "저장" 버튼 → 저장 후 pop / "저장하지 않음" → 그대로 pop / "취소" → 다이얼로그만 닫힘
- [ ] 저장된 역이 없을 경우 "현재 저장되어 있는 지하철역이 없어요." 안내 표출, 편집 화면 자체는 정상 렌더링
- [ ] Spec Acceptance Criteria 충족
