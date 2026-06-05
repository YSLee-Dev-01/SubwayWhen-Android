# Tasks: Edit (저장한 역 편집)

## 참조
- spec: `.claude/specs/features/Edit/spec.md`
- plan: `.claude/specs/features/Edit/plan.md`

## Task 목록

### Phase 1. 의존성 / 라우팅 준비

#### [ ] Task 1 — `libs.versions.toml` + `app/build.gradle.kts` (수정)
**파일**: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- `libs.versions.toml` 에 `sh.calvin.reorderable:reorderable` 버전 항목 추가
- `app/build.gradle.kts` 의 `dependencies` 블록에 reorderable 라이브러리 참조 추가

---

#### [ ] Task 2 — `NavRoutes.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/NavRoutes.kt`
- `edit` 라우트 상수 추가 (예: `const val EDIT = "edit"`)

---

#### [ ] Task 3 — `RootScaffold.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/RootScaffold.kt`
- `EditScreen` composable 을 `RootScaffold` 내부 NavHost 에 `"edit"` 라우트로 등록
- `EditScreen` 진입 시 `onTabBarVisibilityChange(false)` 호출, 복귀 시 `true` 복원 연결

---

#### [ ] Task 4 — `HomeScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeScreen.kt`
- `onEditTap` 콜백에서 `navController.navigate(NavRoutes.EDIT)` 호출하도록 연결 (현재 no-op)

---

### Phase 2. 데이터/도메인 매핑

#### [ ] Task 5 — `EditMapper.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/EditMapper.kt`
- `List<SaveStation>` → `Pair<List<SaveStation>, List<SaveStation>>` (출근 `ONE`, 퇴근 `TWO`) 분리 함수
- `Pair<List<SaveStation>, List<SaveStation>>` → `List<SaveStation>` 병합 함수 (출근 먼저, 이후 퇴근 순서)

---

#### [ ] Task 6 — `EditMapperTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/edit/EditMapperTest.kt`
- 그룹별 분리 라운드트립 단위 테스트 (Kotest)
  - 출근/퇴근 혼합 리스트 → 분리 → 병합 후 원본과 동일한지 검증
  - 출근만 있는 경우, 퇴근만 있는 경우, 빈 리스트 케이스

---

### Phase 3. Contract / ViewModel

#### [ ] Task 7 — `EditContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/EditContract.kt`
- `EditUiState` data class 정의
  - `groupOne: List<SaveStation>` (출근)
  - `groupTwo: List<SaveStation>` (퇴근)
  - `isSaveEnabled: Boolean`
  - `showNotSaveDialog: Boolean`
- `EditIntent` sealed interface 정의
  - `OnAppear`
  - `DeleteStation(station: SaveStation)`
  - `MoveStation(fromSection: Int, fromIndex: Int, toSection: Int, toIndex: Int)`
  - `SaveTap`
  - `BackTap`
  - `DialogSave`
  - `DialogDiscard`
  - `DialogCancel`
- `EditEffect` sealed interface 정의
  - `NavigateBack`

---

#### [ ] Task 8 — `EditViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/EditViewModel.kt`
- Hilt `@HiltViewModel` 어노테이션, `LocalDataRepository` 주입
- `init` 블록에서 `localDataRepository.saveStations.value` 로 1회 초기 로드 → `lastSaved` 보관 + 현재 `groupOne`/`groupTwo` 상태 갱신
- `onIntent(intent: EditIntent)` 단일 진입점으로 Intent 처리
  - `OnAppear`: 필요 시 재로드 (init 에서 처리하므로 no-op 가능)
  - `DeleteStation`: 해당 섹션에서 아이템 제거 후 `isSaveEnabled` 재계산
  - `MoveStation`: 섹션 내/간 이동 처리, cross-section 이동 시 `group` 필드 반대 그룹으로 갱신, `isSaveEnabled` 재계산
  - `SaveTap` / `DialogSave`: `EditMapper` 로 병합 후 `localDataRepository.updateSaveStations(...)` 호출 → `lastSaved` 갱신 → `NavigateBack` 방출
  - `BackTap`: dirty 여부(`isSaveEnabled`) 확인 — dirty 이면 `showNotSaveDialog=true`, 아니면 `NavigateBack` 방출
  - `DialogDiscard`: `showNotSaveDialog=false` + `NavigateBack` 방출
  - `DialogCancel`: `showNotSaveDialog=false`
- `isSaveEnabled` 계산: `current (groupOne + groupTwo) != lastSaved`

---

#### [ ] Task 9 — `EditViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/edit/EditViewModelTest.kt`
- Turbine + MockK 기반 ViewModel 단위 테스트
  - 초기 로드: 그룹 분리 및 `isSaveEnabled=false` 확인
  - `DeleteStation`: 해당 아이템 제거 및 `isSaveEnabled=true` 확인
  - `MoveStation` (동일 섹션): 순서 변경 확인
  - `MoveStation` (cross-section): `group` 필드 변경 및 `isSaveEnabled=true` 확인
  - `SaveTap`: `updateSaveStations` 호출 및 `NavigateBack` Effect 방출 확인
  - `BackTap` (dirty): `showNotSaveDialog=true` 확인
  - `BackTap` (clean): `NavigateBack` Effect 방출 확인
  - `DialogDiscard`: `NavigateBack` Effect 방출 확인
  - `DialogCancel`: `showNotSaveDialog=false` 확인

---

### Phase 4. UI — 행 컴포넌트

#### [ ] Task 10 — `EditStationRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/component/EditStationRow.kt`
- 좌측: 빨간 원형 "−" 삭제 버튼 (`AnimatedTapBox` 래핑, `onDelete: () -> Unit` 콜백)
- 본문: `StationLineCircle` (호선 색상 원형 뱃지) + 역명 + 상하행 텍스트
- 우측: 드래그 핸들 아이콘 (`Icons.Filled.Menu` 또는 `DragHandle`) — reorderable 라이브러리의 `ReorderableItem` drag 제스처 연결
- `@Preview` (Light / Dark 각 1개)

---

### Phase 5. UI — 다이얼로그 컴포넌트

#### [ ] Task 11 — `NotSaveAlertDialog.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/component/NotSaveAlertDialog.kt`
- Material3 `AlertDialog` 사용
- title: `"수정된 지하철역이 저장되지 않았어요.\n저장하지 않을 경우 변경된 내용은\n적용되지 않아요."`
- confirmButton: `"저장"` → `onSave: () -> Unit` 콜백
- dismissButton: `"저장하지 않음"` → `onDiscard: () -> Unit` 콜백
- 본문 영역 Text 버튼: `"취소"` → `onCancel: () -> Unit` 콜백
- `@Preview` 1개

---

### Phase 6. UI — Screen

#### [ ] Task 12 — `EditScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/EditScreen.kt`
- `@HiltViewModel` 로 주입된 `EditViewModel` 수신, `uiState` collectAsStateWithLifecycle
- `LaunchedEffect(Unit)` 에서 `onTabBarVisibilityChange(false)` 호출, `DisposableEffect` 로 `onTabBarVisibilityChange(true)` 복원
- `CommonTopBar(title = "편집", onBack = { viewModel.onIntent(EditIntent.BackTap) })`
- `BackHandler(enabled = true)` → `EditIntent.BackTap` Intent 전달
- `LazyColumn` 내 두 섹션 구성
  - 섹션 헤더: `"출근"` / `"퇴근"` 텍스트
  - 각 섹션 아이템: `EditStationRow` (reorderable 라이브러리 `ReorderableItem` 래핑)
- `groupOne`과 `groupTwo` 모두 비었을 때 중앙에 `"현재 저장되어 있는 지하철역이 없어요."` 텍스트 표시
- 하단 `PrimaryButton(text = "저장", enabled = uiState.isSaveEnabled, onClick = { SaveTap })`
- `uiState.showNotSaveDialog == true` 일 때 `NotSaveAlertDialog` 표시 (각 콜백에 Intent 연결)
- `LaunchedEffect` 로 `EditEffect.NavigateBack` 수집 → `onNavigateBack()` 콜백 호출
- 파라미터: `onNavigateBack: () -> Unit`, `onTabBarVisibilityChange: (Boolean) -> Unit`

---

### Phase 7. 통합 검증

#### [ ] Task 13 — 통합 동작 확인
- 홈 편집 버튼 탭 → Edit 화면 진입 및 탭바 숨김 확인
- 저장된 역이 출근/퇴근 섹션으로 분리되어 표시 (출근 먼저) 확인
- 좌측 "−" 버튼으로 행 삭제 후 `isSaveEnabled=true` 확인
- 드래그 핸들로 동일 섹션 내 순서 변경 확인
- 드래그 핸들로 다른 섹션으로 이동 시 `group` 필드 변경 확인
- "저장" 버튼 탭 후 홈으로 복귀, 변경 내용이 홈에 즉시 반영 확인 (`saveStations` Flow 자동 갱신)
- dirty 상태에서 뒤로가기 → 3버튼 다이얼로그 표출 확인
  - "저장" → 저장 후 pop
  - "저장하지 않음" → 저장 없이 pop
  - "취소" → 다이얼로그만 닫힘
- 저장된 역이 없는 경우 → "현재 저장되어 있는 지하철역이 없어요." 안내, 화면 정상 렌더링 확인
- 편집 화면 복귀 시 탭바 복원 확인

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공
- [ ] 테스트 통과 (`EditMapperTest`, `EditViewModelTest`)
- [ ] Compose Preview 확인 (`EditStationRow`, `NotSaveAlertDialog`)

### 기능 (AC)
- [ ] 홈 편집 버튼 탭 시 Edit 화면 진입, 탭바 숨김
- [ ] 저장된 역이 출근/퇴근 섹션으로 분리되어 표시 (출근 먼저)
- [ ] 좌측 "−" 버튼으로 행 삭제 가능
- [ ] 우측 드래그 핸들로 같은 섹션 내 순서 변경 가능
- [ ] 우측 드래그 핸들로 다른 섹션으로 이동 시 `group` 필드가 변경되어 저장됨
- [ ] 변경사항이 있을 때만 "저장" 버튼 활성화
- [ ] "저장" 버튼 탭 시 `LocalDataRepository` 에 반영 후 화면 pop
- [ ] dirty 상태에서 뒤로가기 시 "저장 / 저장하지 않음 / 취소" 3버튼 다이얼로그 표출
- [ ] "저장" → 저장 후 pop / "저장하지 않음" → 그대로 pop / "취소" → 다이얼로그만 닫힘
- [ ] 저장된 역이 없을 경우 "현재 저장되어 있는 지하철역이 없어요." 안내 표출, 편집 화면 자체는 정상 렌더링
