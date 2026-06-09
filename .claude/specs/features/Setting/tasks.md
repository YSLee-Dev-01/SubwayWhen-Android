# Tasks: Setting

## 참조
- spec: `.claude/specs/features/Setting/spec.md`
- plan: `.claude/specs/features/Setting/plan.md`

## Task 목록

### Phase 1. Data — Firebase License/Contents 조회

#### [x] Task 1 — `LoadModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModel.kt`
- `suspend fun getLicenses(): List<String>?` 시그니처 추가
- `suspend fun getContents(): String?` 시그니처 추가

---

#### [x] Task 2 — `LoadModelImpl.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModelImpl.kt`
- `getLicenses()` 구현: `firebaseDatabase.reference.child("SubwayWhen/Licenses").get().await()` 조회 후 `List<String>`으로 변환, 실패 시 null 반환 (기존 `shinbundangScheduleVersionRequest()` 패턴 차용)
- `getContents()` 구현: `firebaseDatabase.reference.child("SubwayWhen/Contents").get().await()` 조회 후 `String`으로 변환, 실패 시 null 반환

---

#### [x] Task 3 — `TotalLoadModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModel.kt`
- `suspend fun getLicenses(): List<String>` 시그니처 추가
- `suspend fun getContents(): String` 시그니처 추가

---

#### [x] Task 4 — `TotalLoadModelImpl.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModelImpl.kt`
- `getLicenses()` 구현: `loadModel.getLicenses() ?: emptyList()` 위임
- `getContents()` 구현: `loadModel.getContents() ?: ""` 위임

---

### Phase 2. Contract — MVI 정의

#### [x] Task 5 — `SettingContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingContract.kt`
- `SettingUiState` data class 정의: `saveSetting: SaveSetting`, `expandedTimeGroup: TimeGroup?`, `activeModal: SettingModalType?`, `modalLicenses: List<String>`, `modalContents: String`, `isModalLoading: Boolean` 필드 보유
- `TimeGroup` enum class 정의: `Work`, `Leave`
- `SettingModalType` sealed class(또는 enum) 정의: `TrainIcon`, `License`, `Contents`
- `SettingToggleField` enum class 정의: `AutoReload`, `ScheduleAutoTime`, `SearchOverlap` (각 필드를 `SaveSetting`의 어떤 필드로 매핑할지 명시)
- `SettingIntent` sealed interface 정의:
  - `OnAppear` — DataStore에서 설정 로드 트리거 (init 에서 자동 구독하므로 실제로는 미사용일 수 있음)
  - `TimeGroupTapped(group: TimeGroup)` — 인라인 시간 선택 펼침/접힘 토글
  - `TimeSaved(group: TimeGroup, time: Int)` — 저장 버튼 탭, DataStore 갱신 후 UI 닫힘
  - `ToggleChanged(field: SettingToggleField)` — 토글 탭, 해당 SaveSetting 필드 반전
  - `CongestionLabelChanged(text: String)` — 텍스트필드 입력, 마지막 1글자 유지
  - `CongestionLabelFocusLost` — 포커스 해제, 빈 값이면 `☹️` 저장
  - `TrainIconTapped` — 모달 열기
  - `TrainIconSelected(icon: String)` — 아이콘 선택 후 `detailVcTrainIcon` 갱신
  - `LicenseTapped` — 모달 열기 + Firebase 조회 시작
  - `ContentsTapped` — 모달 열기 + Firebase 조회 시작
  - `ModalDismissed` — `activeModal = null` + 모달 데이터 초기화
  - `WorkAlarmTapped` — no-op (이번 스코프 미구현)

---

### Phase 3. ViewModel — 비즈니스 로직

#### [x] Task 6 — `SettingViewModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingViewModel.kt`
- `@HiltViewModel`, `@Inject constructor(private val localDataRepository: LocalDataRepository, private val totalLoadModel: TotalLoadModel)` 선언
- `uiState: StateFlow<SettingUiState>` 노출 (Effect 불필요 — 화면 전환 없음)
- `init` 블록에서 `localDataRepository.saveSetting` Flow 구독 → `uiState.saveSetting` 갱신
- `fun onIntent(intent: SettingIntent)` 단일 진입점 구현:
  - `TimeGroupTapped`: `expandedTimeGroup`이 같은 그룹이면 null, 다른 그룹이면 교체
  - `TimeSaved`: `SaveSetting.copy(...)` 후 `localDataRepository.updateSaveSetting()` 호출, `expandedTimeGroup = null`
  - `ToggleChanged`: 해당 필드 반전 후 `updateSaveSetting()` 호출
  - `CongestionLabelChanged`: 마지막 1글자만 남긴 뒤 uiState의 `saveSetting.mainCongestionLabel` 임시 반영
  - `CongestionLabelFocusLost`: `saveSetting.mainCongestionLabel`이 빈 문자열이면 `☹️`로 `updateSaveSetting()` 호출, 그 외 현재 값으로 저장
  - `TrainIconTapped`: `activeModal = SettingModalType.TrainIcon`
  - `TrainIconSelected`: `detailVcTrainIcon` 갱신 후 `updateSaveSetting()`, `activeModal = null`
  - `LicenseTapped`: `activeModal = SettingModalType.License`, `isModalLoading = true`, `viewModelScope.launch`에서 `totalLoadModel.getLicenses()` 호출 후 `modalLicenses` 갱신, `isModalLoading = false`
  - `ContentsTapped`: `activeModal = SettingModalType.Contents`, `isModalLoading = true`, `viewModelScope.launch`에서 `totalLoadModel.getContents()` 호출 후 `modalContents` 갱신, `isModalLoading = false`
  - `ModalDismissed`: `activeModal = null`, `modalLicenses = emptyList()`, `modalContents = ""`, `isModalLoading = false`
  - `WorkAlarmTapped`: 아무 동작 없음

---

### Phase 4. Component — 셀 컴포저블

#### [x] Task 7 — `SettingSectionHeader.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/component/SettingSectionHeader.kt`
- 섹션 타이틀 텍스트 표시 컴포저블
- `title: String` 파라미터, `Dimens.paddingLR` 좌우 여백, `Dimens.fontSizeSmall` 폰트 크기

---

#### [x] Task 8 — `SettingTimeRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/component/SettingTimeRow.kt`
- `workTime: Int`, `leaveTime: Int`, `expandedGroup: TimeGroup?`, `onGroupTapped: (TimeGroup) -> Unit`, `onSave: (TimeGroup, Int) -> Unit` 파라미터
- 출근/퇴근 버튼 행: `AnimatedTapBox` 사용, 각 버튼 탭 시 `onGroupTapped` 호출
- 인라인 시간 선택 UI: `AnimatedVisibility`로 펼침/접힘, 0–23 범위의 +/- 버튼(Stepper), 현재 임시 값은 Compose `remember` 로컬 상태로 관리
- 저장 버튼 탭 시 `onSave` 호출

---

#### [x] Task 9 — `SettingArrowRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/component/SettingArrowRow.kt`
- `title: String`, `onTap: () -> Unit` 파라미터
- `AnimatedTapBox`로 래핑, 우측에 화살표 아이콘 표시
- `Dimens.paddingLR` 좌우 여백, `Dimens.paddingTB` 상하 여백

---

#### [x] Task 10 — `SettingToggleRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/component/SettingToggleRow.kt`
- `title: String`, `checked: Boolean`, `onToggle: () -> Unit` 파라미터
- 좌측 타이틀 텍스트 + 우측 `Switch` 컴포넌트
- `Dimens.paddingLR` 좌우 여백, `Dimens.paddingTB` 상하 여백

---

#### [x] Task 11 — `SettingTextFieldRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/component/SettingTextFieldRow.kt`
- `title: String`, `value: String`, `onValueChange: (String) -> Unit`, `onFocusLost: () -> Unit` 파라미터
- 좌측 타이틀 텍스트 + 우측 `TextField` (1글자 제한: `onValueChange`에서 마지막 1글자만 유지)
- 포커스 해제 이벤트는 `onFocusChanged` modifier로 감지 → `onFocusLost` 호출
- `Dimens.paddingLR` 좌우 여백, `Dimens.paddingTB` 상하 여백

---

### Phase 5. Modal — 3개 모달

#### [x] Task 12 — `TrainIconModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/TrainIconModal.kt`
- `CommonModalBottomSheet` 컨테이너 사용
- 이모지 목록: `🚃`, `🚋`, `🚝`, `🚄`, `🚅`, `🚇`, `🚈`, `🚂` (iOS 원본 동일)
- 현재 선택된 아이콘(`currentIcon: String`)은 하이라이트 처리 (배경색 또는 테두리 강조)
- 이모지 선택 시 로컬 상태로 임시 선택값 관리
- 확인 버튼(`PrimaryButton`) 탭 → `onIconSelected(selectedIcon)` 호출 후 `animatedDismiss` 실행
- `onIconSelected: (String) -> Unit`, `onDismiss: () -> Unit`, `currentIcon: String` 파라미터

---

#### [x] Task 13 — `LicenseModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/LicenseModal.kt`
- `CommonModalBottomSheet` 컨테이너 사용
- `licenses: List<String>`, `isLoading: Boolean`, `onDismiss: () -> Unit` 파라미터
- `isLoading`이 true이거나 `licenses`가 비어 있으면 빈 상태(또는 로딩 인디케이터 없이 빈 공간) 표시
- 라이선스 항목 목록을 스크롤 가능한 Column 또는 LazyColumn으로 표시
- 닫기 버튼(`PrimaryButton`) → `animatedDismiss` 실행 후 `onDismiss` 호출

---

#### [x] Task 14 — `ContentsModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/ContentsModal.kt`
- `CommonModalBottomSheet` 컨테이너 사용
- `contents: String`, `isLoading: Boolean`, `onDismiss: () -> Unit` 파라미터
- `isLoading`이 true이거나 `contents`가 빈 문자열이면 빈 상태 표시
- 내용 텍스트를 스크롤 가능한 Column으로 표시
- 닫기 버튼(`PrimaryButton`) → `animatedDismiss` 실행 후 `onDismiss` 호출

---

### Phase 6. Screen — 조립 및 strings.xml

#### [x] Task 15 — `strings.xml` (수정)
**파일**: `app/src/main/res/values/strings.xml`
- 섹션 타이틀 추가: `setting_section_home`("홈 화면"), `setting_section_detail`("상세 화면"), `setting_section_search`("검색 화면"), `setting_section_etc`("기타")
- 홈 섹션 셀 라벨: `setting_work_alarm`("출근 알람"), `setting_congestion_label`("혼잡도 이모지"), `setting_work_time`("출근 시간"), `setting_leave_time`("퇴근 시간"), `setting_time_save`("저장")
- 상세 섹션 셀 라벨: `setting_auto_reload`("자동 새로고침"), `setting_schedule_auto_time`("시간표 자동 정렬"), `setting_train_icon`("열차 아이콘")
- 검색 섹션 셀 라벨: `setting_search_overlap`("중복 저장 방지")
- 기타 섹션 셀 라벨: `setting_license`("오픈 라이선스"), `setting_contents`("기타")
- 기존 placeholder 문자열(`setting_placeholder_title` 등) 미사용 처리(삭제 또는 주석)

---

#### [x] Task 16 — `SettingScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingScreen.kt`
- `SettingViewModel` Hilt 주입, `uiState` → `collectAsStateWithLifecycle()`
- `CommonTopBar("설정")` 상단 배치
- `LazyColumn`(또는 `Column` + `verticalScroll`)으로 섹션 4개 순서대로 구성:
  - **섹션 1 (홈 화면)**: `SettingSectionHeader` → `SettingTimeRow`(출근/퇴근 시간) → `SettingArrowRow`("출근 알람", no-op) → `SettingTextFieldRow`("혼잡도 이모지")
  - **섹션 2 (상세 화면)**: `SettingSectionHeader` → `SettingToggleRow`("자동 새로고침") → `SettingToggleRow`("시간표 자동 정렬") → `SettingArrowRow`("열차 아이콘")
  - **섹션 3 (검색 화면)**: `SettingSectionHeader` → `SettingToggleRow`("중복 저장 방지")
  - **섹션 4 (기타)**: `SettingSectionHeader` → `SettingArrowRow`("오픈 라이선스") → `SettingArrowRow`("기타")
- 각 셀 콜백에서 `viewModel.onIntent(...)` 호출
- `uiState.activeModal` 분기로 모달 조건부 렌더링:
  - `SettingModalType.TrainIcon` → `TrainIconModal` 표시
  - `SettingModalType.License` → `LicenseModal` 표시
  - `SettingModalType.Contents` → `ContentsModal` 표시
- placeholder 함수 제거
- `Dimens.tabBarBottomPadding` 하단 패딩 적용

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공
- [ ] `SettingViewModel` 단위 테스트: Toggle 즉시 반영, CongestionLabel 빈 값 → `☹️` 대체, TimeSaved → `expandedTimeGroup = null` 동작 검증
- [ ] 기존 LoadModel / TotalLoadModel 메서드 변경 없이 빌드 통과 확인

### 기능 (AC)
- [ ] 설정 섹션 4개가 각 타이틀("홈 화면" / "상세 화면" / "검색 화면" / "기타")과 함께 표시된다
- [ ] 출근/퇴근 시간 버튼 탭 시 인라인 시간 선택 UI가 펼쳐지고, 저장 시 DataStore에 반영된다
- [ ] "출근 알람" ArrowRow가 표시되고 탭해도 아무 동작이 없다
- [ ] 혼잡도 이모지 텍스트필드는 1자리 이모지만 허용하고, 빈 값 저장 시 `☹️`로 대체된다
- [ ] 토글 항목(자동 새로고침, 시간표 자동 정렬, 중복 저장 방지) 탭 시 즉시 DataStore에 반영되고 UI에 표시된다
- [ ] 열차 아이콘 탭 시 TrainIconModal이 열리고 아이콘 선택 후 저장하면 `detailVcTrainIcon`이 갱신된다
- [ ] 오픈 라이선스 탭 시 LicenseModal이 열리고 Firebase에서 라이선스 목록이 표시된다
- [ ] 기타 탭 시 ContentsModal이 열리고 Firebase에서 내용이 표시된다
- [ ] 앱 재시작 후에도 변경된 설정이 유지된다
