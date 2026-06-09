# Plan: Setting (설정 화면)

## 참조 Spec
- @specs/features/Setting/spec.md

## 참조 Skill
- 신규 화면은 아니지만 기존 placeholder를 실제 화면으로 대체. 기존 feature 패턴(`feature/edit/`) 준수.

## 현재 상태 파악

- **신규**
  - `feature/setting/SettingContract.kt` — UiState / Intent / Effect 정의 (현재 없음)
  - `feature/setting/component/` — `SettingSectionHeader`, `SettingTimeRow`, `SettingArrowRow`, `SettingToggleRow`, `SettingTextFieldRow`
  - `feature/setting/modal/TrainIconModal.kt` — 열차 아이콘 선택 모달
  - `feature/setting/modal/LicenseModal.kt` — 오픈 라이선스 모달
  - `feature/setting/modal/ContentsModal.kt` — 기타 내용 모달
  - `LoadModel` 인터페이스 + `LoadModelImpl` — `getLicenses()`, `getContents()` Firebase 조회 메서드 추가
  - `TotalLoadModel` 인터페이스 + `TotalLoadModelImpl` — `getLicenses()`, `getContents()` 가공 후 반환 메서드 추가
- **재사용**
  - `LocalDataRepository.saveSetting` (StateFlow), `updateSaveSetting()` — 설정 로드/저장 (이미 구현됨)
  - `SaveSetting` data class — 모든 설정 필드 보유 (수정 불필요)
  - `CommonModalBottomSheet` — TrainIcon / License / Contents 모달 컨테이너
  - `LoadModelImpl` — 기존 `firebaseDatabase.reference.child(...).get().await()` 패턴 그대로 활용
  - `PrimaryButton` / `ModalSubButton` — 모달 확인 버튼
  - `AnimatedTapBox` — Row 탭 피드백
  - `MainBgCard` / `Dimens` 토큰 — 카드/여백 스타일
- **수정**
  - `feature/setting/SettingScreen.kt` — placeholder → 실제 섹션 리스트 렌더링
  - `feature/setting/SettingViewModel.kt` — 빈 ViewModel → MVI 로직 구현
  - `data/remote/loadmodel/LoadModel.kt` / `LoadModelImpl.kt` — `getLicenses()`, `getContents()` 추가
  - `data/remote/totalload/TotalLoadModel.kt` / `TotalLoadModelImpl.kt` — `getLicenses()`, `getContents()` 추가
  - `strings.xml` — 섹션/셀 타이틀 문자열 추가 (`setting_placeholder_title` 은 미사용화)
- **삭제**
  - 없음 (placeholder content 함수는 실제 구현으로 대체)

## 기술적 결정사항

- **UiState는 data class**: 섹션/셀이 정적 구조이고 로딩-성공-에러 단순 구분이 아니라 여러 상태 필드(현재 SaveSetting, 펼쳐진 TimeRow, 열린 모달 종류, 모달 데이터)를 동시에 들고 있어야 하므로 `data class`. (conventions.md 규칙: 복잡한 상태 필드가 많을 때 data class)
- **모달 표시 관리**: 어떤 모달이 열렸는지 `SettingUiState.activeModal: SettingModalType?` (sealed/enum: `TrainIcon` / `License` / `Contents` / `null`)로 단일 표현. iOS는 Coordinator로 분기하나 AOS는 Compose 조건부 렌더링으로 대응.
- **TimeRow 인라인 펼침**: iOS `selectedTimeViewType: TimeType?` 대응. `expandedTimeGroup: TimeGroup?`(Work/Leave/null)로 표현. 같은 그룹 재탭 → null로 토글, 다른 그룹 탭 → 교체. Stepper 임시값은 Compose `remember` 로컬 상태로 두고 저장 시에만 Intent 전달 (iOS `stepperValue` @State 대응).
- **설정 즉시 반영**: Toggle/TextField/Time/TrainIcon 변경은 `LocalDataRepository.updateSaveSetting()` 호출 → `saveSetting` StateFlow가 자동 갱신되므로 ViewModel은 이 Flow를 구독해 UiState에 반영. iOS의 `updateSavedSettings`와 동일한 단방향 흐름.
- **TextFieldRow 빈 값 처리**: 포커스 해제 시점에 빈 값이면 기본값 `☹️` 저장 (spec). 입력 중에는 마지막 1글자만 유지. 포커스 해제 이벤트는 Screen에서 감지해 Intent 전달.
- **Firebase License/Contents — LoadModel → TotalLoadModel 경로로 통일**: 기존 `shinbundangScheduleVersionRequest()` / `shinbundangScheduleRequest()` 패턴과 동일하게 `LoadModelImpl`에서 `firebaseDatabase.reference.child(...)` 직접 조회 → `TotalLoadModelImpl`이 받아 null 처리 후 반환. `FirebaseDataSource`는 건드리지 않음. License는 `List<String>`, Contents는 `String`으로 반환. `TotalLoadModel` 메서드 실패 시 빈 목록/빈 문자열 반환 → 모달은 빈 상태 표시.
- **liveActivity / alertGroupID 제외**: SaveSetting에 해당 필드 자체가 없으므로 토글·경고 표시 모두 미구현 (spec 제약 일치). iOS의 liveActivity 연동 토글 로직도 포팅하지 않음.
- **출근 알람 ArrowRow**: 표시만 하고 탭 콜백은 no-op (spec: 이번 스코프는 버튼만).
- **탭바 숨김/복원 미적용**: 모달이 설정 탭 내부에서 열리고 탭 전환으로 자동 처리되므로 `onTabBarVisibilityChange` 콜백 불필요 (spec 제약).

## 구현 순서

### Phase 1. Data — Firebase License/Contents 조회 (LoadModel → TotalLoadModel)
- `LoadModel.kt`: `suspend fun getLicenses(): List<String>?`, `suspend fun getContents(): String?` 시그니처 추가
- `LoadModelImpl.kt`: 두 메서드 구현 — `firebaseDatabase.reference.child("SubwayWhen/Licenses")` / `child("SubwayWhen/Contents")` 조회, try/catch null 반환 (기존 `shinbundangScheduleVersionRequest()` 패턴 동일)
- `TotalLoadModel.kt`: `suspend fun getLicenses(): List<String>`, `suspend fun getContents(): String` 시그니처 추가
- `TotalLoadModelImpl.kt`: `loadModel.getLicenses() ?: emptyList()`, `loadModel.getContents() ?: ""` 위임
- verify: 빌드 통과, 기존 메서드 영향 없음

### Phase 2. Contract — MVI 정의
- `SettingContract.kt` 작성
  - `SettingUiState(saveSetting, expandedTimeGroup, activeModal, modalLicenses, modalContents, isModalLoading)`
  - `SettingIntent`: `OnAppear`, `TimeGroupTapped(TimeGroup)`, `TimeSaved(group, time)`, `ToggleChanged(field, value)`, `CongestionLabelChanged(text)`, `CongestionLabelFocusLost`, `TrainIconTapped`, `TrainIconSelected(icon)`, `LicenseTapped`, `ContentsTapped`, `ModalDismissed`, `WorkAlarmTapped`(no-op)
  - `TimeGroup` enum (Work/Leave), `SettingModalType` (TrainIcon/License/Contents)
  - Toggle 대상 필드는 enum(`AutoReload`/`ScheduleAutoTime`/`SearchOverlap`)으로 표현 (KeyPath 대응)
- verify: 컴파일 통과

### Phase 3. ViewModel — 비즈니스 로직
- `SettingViewModel.kt`
  - 생성자 주입: `LocalDataRepository`, `TotalLoadModel`
  - `init`에서 `localDataRepository.saveSetting` 구독 → UiState.saveSetting 갱신
  - `onIntent` 분기:
    - Toggle/Congestion/Time/TrainIcon → `SaveSetting.copy(...)` 후 `updateSaveSetting`
    - CongestionLabelFocusLost → 빈 값이면 `☹️` 저장
    - TimeGroupTapped → `expandedTimeGroup` 토글
    - License/Contents/TrainIconTapped → `activeModal` 설정, License/Contents는 `viewModelScope`에서 `totalLoadModel.getLicenses()` / `getContents()` 호출 후 모달 데이터 갱신
    - ModalDismissed → `activeModal = null`, 모달 데이터 초기화
  - `StateFlow<SettingUiState>` 노출 (Effect 불필요 — 화면 전환 없음)
- verify: 컴파일 통과, 단위 동작은 Phase 6에서 확인

### Phase 4. Component — 셀 컴포저블
- `component/SettingSectionHeader.kt` — 섹션 타이틀
- `component/SettingTimeRow.kt` — 출근/퇴근 버튼 + 인라인 Stepper(+/- 0~23) + 저장 버튼, `expandedTimeGroup` 기반 펼침/접힘 (`AnimatedVisibility`)
- `component/SettingArrowRow.kt` — 타이틀 + 화살표, `onTap` 콜백 (`AnimatedTapBox`)
- `component/SettingToggleRow.kt` — 타이틀 + Switch
- `component/SettingTextFieldRow.kt` — 타이틀 + 1글자 TextField, 포커스 해제 콜백
- verify: 각 컴포넌트 Preview 렌더

### Phase 5. Modal — 3개 모달
- `modal/TrainIconModal.kt` — `CommonModalBottomSheet` + 이모지 그리드(🚃🚋🚝🚄🚅🚇🚈🚂), 현재 선택 하이라이트, 확인 시 `TrainIconSelected` + dismiss
- `modal/LicenseModal.kt` — `CommonModalBottomSheet` + 라이선스 텍스트 목록(스크롤), 로딩/빈 상태 처리
- `modal/ContentsModal.kt` — `CommonModalBottomSheet` + 내용 텍스트, 로딩/빈 상태 처리
- verify: Preview 렌더

### Phase 6. Screen — 조립
- `SettingScreen.kt`
  - `collectAsStateWithLifecycle()`로 UiState 구독
  - `LazyColumn`(또는 Column+verticalScroll)에 섹션 4개 + 셀 배치, 각 콜백 → `viewModel.onIntent`
  - `activeModal` 분기로 모달 표시
  - `CommonTopBar`("설정") + `Dimens` 여백 적용
  - placeholder 함수 제거
- `strings.xml`: 섹션/셀 타이틀(홈 화면/상세 화면/검색 화면/기타, 출근 알람/혼잡도 이모지/자동 새로고침/시간표 자동 정렬/중복 저장 방지/열차 아이콘/오픈 라이선스/기타, 출근 시간/퇴근 시간/저장 등) 추가
- verify: 앱 빌드 후 설정 탭 진입 → 섹션 표시, 토글/시간/이모지/모달 동작, 재진입 시 값 유지

## 완료 조건
- [ ] 설정 섹션 4개가 각 타이틀과 함께 표시된다
- [ ] 출근/퇴근 시간 버튼 탭 시 인라인 시간 선택 UI가 펼쳐지고 저장 시 DataStore에 반영된다
- [ ] "출근 알람" ArrowRow가 표시되고 탭해도 아무 동작이 없다
- [ ] 혼잡도 이모지 텍스트필드는 1자리만 허용하고 빈 값 저장 시 `☹️`로 대체된다
- [ ] 토글 항목(자동 새로고침/시간표 자동 정렬/중복 저장 방지)이 즉시 DataStore에 반영된다
- [ ] 열차 아이콘 모달에서 선택·저장 시 `detailVcTrainIcon`이 갱신된다
- [ ] 오픈 라이선스 모달에 Firebase 라이선스 목록이 표시된다
- [ ] 기타 모달에 Firebase 내용이 표시된다
- [ ] 앱 재시작 후에도 변경된 설정이 유지된다
- [ ] Spec Acceptance Criteria 충족
