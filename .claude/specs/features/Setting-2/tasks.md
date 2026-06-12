# Tasks: Setting-2 (출퇴근 알림 Modal)

## 참조
- spec: `.claude/specs/features/Setting-2/spec.md`
- plan: `.claude/specs/features/Setting-2/plan.md`

## Task 목록

### Phase 1. 데이터 레이어 (저장역 ID 영속화)

#### [x] Task 1 — `SaveSetting.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/model/SaveSetting.kt`
- `alertGroupOneId: String = ""` 필드 추가 (iOS `alertGroupOneID` 대응)
- `alertGroupTwoId: String = ""` 필드 추가 (iOS `alertGroupTwoID` 대응)

---

#### [x] Task 2 — `PreferencesKeys.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/PreferencesKeys.kt`
- `ALERT_GROUP_ONE_ID = stringPreferencesKey("alert_group_one_id")` 추가
- `ALERT_GROUP_TWO_ID = stringPreferencesKey("alert_group_two_id")` 추가

---

#### [x] Task 3 — `SettingLocalDataSource.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/SettingLocalDataSource.kt`
- `getSaveSetting()` 매핑에 `alertGroupOneId` / `alertGroupTwoId` 읽기 추가 (기본값 `""`)
- `updateSaveSetting()` 쓰기에 `alertGroupOneId` / `alertGroupTwoId` 저장 추가

---

### Phase 2. 알림 인프라

#### [x] Task 4 — `AndroidManifest.xml` (수정)
**파일**: `app/src/main/AndroidManifest.xml`
- `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` 추가
- `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />` 추가 (AlarmManager 기반 구현 대비)

---

#### [x] Task 5 — `NotificationScheduler.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/core/notification/NotificationScheduler.kt`
- `NotificationScheduler` 인터페이스 정의
- `reschedule(setting: SaveSetting, saveStations: List<SaveStation>)` 메서드 선언
  - 출근/퇴근 역 ID, 출근/퇴근 시간, 주말 포함 여부를 파라미터로 받아 알림 재등록

---

#### [x] Task 6 — `NotificationSchedulerImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/core/notification/NotificationSchedulerImpl.kt`
- `NotificationScheduler` 인터페이스 구현
- `reschedule` 구현: 기존 알림 전체 취소 후 재등록
  - `alertGroupOneId` 또는 `alertGroupTwoId`가 빈 문자열이면 해당 알림 등록 생략
  - 출근/퇴근 시간이 `0`이면 해당 알림 등록 생략
  - `isWeekendNotificationEnabled = true`이면 매일(월~일), `false`이면 월~금 반복 등록
  - `AlarmManager` + 요일별 `setExact` 방식 사용

---

#### [x] Task 7 — `NotificationAlarmReceiver.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/core/notification/NotificationAlarmReceiver.kt`
- `BroadcastReceiver` 구현 — AlarmManager 알람 수신 시 알림(Notification) 발행
- 인텐트에서 역명, 호선, 시간 정보를 추출하여 알림 표시
- `AndroidManifest.xml`에 `<receiver>` 등록 추가

---

#### [x] Task 8 — `NotificationModule.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/NotificationModule.kt`
- `@Module` + `@InstallIn(SingletonComponent::class)` Hilt 모듈 작성
- `NotificationScheduler` 인터페이스를 `NotificationSchedulerImpl`에 바인딩

---

### Phase 3. Contract / ViewModel

#### [x] Task 9 — `SettingContract.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingContract.kt`
- `SettingModalType`에 `WorkAlarm` 케이스 추가
- `SettingUiState`에 알림 Modal 상태 필드 추가
  - `hasNotificationPermission: Boolean = false`
  - `isWeekendIncluded: Boolean = true`
  - `workAlarmGroupOneStation: SaveStation? = null` (선택된 출근 역, null = "역 선택")
  - `workAlarmGroupTwoStation: SaveStation? = null` (선택된 퇴근 역, null = "역 선택")
  - `workAlarmSelectGroup: TimeGroup? = null` (현재 역 선택 Push 대상 그룹, null = 메인 뷰)
  - `groupOneStations: List<SaveStation> = emptyList()` (출근 그룹 저장역 목록)
  - `groupTwoStations: List<SaveStation> = emptyList()` (퇴근 그룹 저장역 목록)
- `SettingIntent`에 케이스 추가
  - `WorkAlarmOpened(hasPermission: Boolean)` — Modal 열기 + 권한 여부 수신
  - `WeekendToggled` — 주말 포함 스위치 토글
  - `WorkAlarmStationTapped(group: TimeGroup)` — 역 버튼 탭 → 역 선택 뷰로 Push
  - `WorkAlarmStationSelected(station: SaveStation)` — 역 선택 → pop + 선택 반영
  - `WorkAlarmStationReset(group: TimeGroup)` — 알림 끄기 버튼 탭 → "역 선택" 리셋 + pop
  - `WorkAlarmSelectPopped` — 역 선택 뷰에서 뒤로가기 → pop
  - `WorkAlarmSaved` — 저장 버튼 탭

---

#### [x] Task 10 — `SettingViewModel.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingViewModel.kt`
- `NotificationScheduler` Hilt 주입 추가
- `WorkAlarmOpened` 처리
  - `hasNotificationPermission` 상태 세팅
  - `activeModal = SettingModalType.WorkAlarm` 세팅
  - 로컬에서 `saveStations` 로드 → 그룹별 필터링하여 `groupOneStations` / `groupTwoStations` 세팅
  - 로컬에서 `saveSetting` 로드 → `alertGroupOneId` / `alertGroupTwoId`로 현재 선택 역 매칭
  - `isWeekendIncluded = saveSetting.isWeekendNotificationEnabled` 세팅
  - 로드 실패 시 주말 포함 기본값 `true`, 출퇴근 역 `null` 폴백
- `WeekendToggled` 처리 → `isWeekendIncluded` 반전
- `WorkAlarmStationTapped` 처리 → `workAlarmSelectGroup = group` 세팅
- `WorkAlarmStationSelected` 처리 → 해당 그룹 선택 역 업데이트, `workAlarmSelectGroup = null`
- `WorkAlarmStationReset` 처리 → 해당 그룹 선택 역 `null` 리셋, `workAlarmSelectGroup = null`
- `WorkAlarmSelectPopped` 처리 → `workAlarmSelectGroup = null`
- `WorkAlarmSaved` 처리
  - `saveSetting`에 `alertGroupOneId`, `alertGroupTwoId`, `isWeekendNotificationEnabled` 업데이트 후 `updateSaveSetting` 호출
  - `NotificationScheduler.reschedule(setting, saveStations)` 호출
  - Modal dismiss (`activeModal = null`)

---

### Phase 4. 공통 컴포넌트 추출 및 UI

#### [x] Task 11 — `StationRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/StationRow.kt`
- `MainBgCard` 배경 + `StationLineCircle` + 역명 Text + updnLine Text + `trailingContent: @Composable () -> Unit` 슬롯을 갖는 공통 역 Row 컴포저블
- `SaveStation`을 받아 렌더링
- 호선 미선택(null) 시 gray 컬러 원형, "역 선택" 텍스트로 폴백 처리

---

#### [x] Task 12 — `EditStationRow.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/edit/component/EditStationRow.kt`
- 내부 mainBG 역 정보 영역을 `StationRow`로 교체
- 외부 구조(삭제 버튼 좌측 + StationRow 중앙 weight=1f + 드래그 핸들 우측)는 동일하게 유지

---

#### [x] Task 13 — `WorkAlarmStationView.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/component/WorkAlarmStationView.kt`
- iOS `SettingNotiStationView` 대응
- 주말 포함 토글 Row
  - `Switch` 컴포넌트
  - 스위치 ON: "월 화 수 목 금 토 일" / OFF: "월 화 수 목 금" 텍스트 표시
- 출근시간 | 퇴근시간 헤더 Row
- 출근 역 버튼 + 퇴근 역 버튼 Row
  - 각 버튼: `StationRow` 래핑, `AnimatedTapBox`로 탭 피드백
  - 역 미선택(null)일 때: gray `StationLineCircle` + "역 선택" 텍스트

---

#### [x] Task 14 — `WorkAlarmSelectView.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/component/WorkAlarmSelectView.kt`
- iOS `SettingNotiSelectModalVC` 대응
- 상단 바: 뒤로가기 버튼(좌) ↔ 알림 끄기 버튼(우, bell.slash 아이콘, 역이 있을 때만 표시)
- 그룹별 저장역 목록: `StationRow`에 체크 아이콘 trailing 슬롯 — 현재 선택 역에만 체크 표시
- 역이 없을 때: "현재 저장되어 있는 지하철역이 없어요." 텍스트, 알림 끄기 버튼 숨김
- 각 역 탭 → `onStationSelected(station)` 콜백 호출
- 알림 끄기 버튼 탭 → `onAlarmOff()` 콜백 호출
- 뒤로가기 버튼 탭 → `onBack()` 콜백 호출

---

#### [x] Task 15 — `WorkAlarmModal.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/modal/WorkAlarmModal.kt`
- iOS `SettingNotiModal` 대응
- `CommonModalBottomSheet` 컨테이너 사용
- `AnimatedContent`로 메인 뷰 ↔ 역 선택 뷰(`WorkAlarmSelectView`) 전환 (slide-in/out)
  - `workAlarmSelectGroup == null`이면 메인 뷰, non-null이면 역 선택 뷰
- **권한 있음 분기**
  - `topDecoration`: "출근/퇴근 시간이 0시로 설정되어 있으면 알림이 울리지 않아요." 안내 텍스트 (Modal 열 때 슬라이드인 / 닫힐 때 슬라이드아웃 애니메이션)
  - `title`: "출퇴근 알림"
  - `subTitle`: "출퇴근 시간에 맞게 정해놓은 지하철역으로 알림을 주는 기능이에요."
  - content: `WorkAlarmStationView`
  - `confirmButton`: 저장 버튼 → `WorkAlarmSaved` Intent
- **권한 없음 분기**
  - `topDecoration`: 없음 (숨김)
  - `title`: "출퇴근 알림"
  - `subTitle`: "알림 권한이 설정되어 있지 않아요."
  - content: Lottie(`R.raw.report`) 애니메이션
  - `confirmButton`: "닫기" 버튼 → `ModalDismissed` Intent

---

#### [x] Task 16 — `SettingScreen.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingScreen.kt`
- 출퇴근 지하철역 버튼 탭 시 `POST_NOTIFICATIONS` 권한 체크
  - `ContextCompat.checkSelfPermission` 사용 (SDK 33 미만은 항상 권한 있음으로 간주)
  - 결과를 `WorkAlarmOpened(hasPermission = ...)` Intent로 ViewModel에 전달
- `when(activeModal)` 분기에 `SettingModalType.WorkAlarm -> WorkAlarmModal(...)` 추가
  - Modal 열림 시 `onTabBarVisibilityChange(false)` 호출
  - Modal 닫힘 시 `onTabBarVisibilityChange(true)` 호출
  - `WorkAlarmModal`에 상태(권한 여부, 주말 포함, 선택 역, 역 목록 등) 및 Intent 콜백 전달

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공
- [ ] 기존 EditScreen 동작 유지 (`EditStationRow` 리팩터링 후 회귀 없음)
- [ ] `SettingViewModelTest` — 역 선택 후 저장 시 `SaveSetting`에 id 반영 + `reschedule` 호출 확인 (MockK)
- [ ] `SettingViewModelTest` — 권한 없음 분기 상태 확인
- [ ] `SettingViewModelTest` — 로드 실패 시 폴백값(주말 포함 true, 역 null) 확인

### 기능 (AC)
- [ ] 출퇴근 지하철역 버튼 탭 시 Modal이 표시된다
- [ ] 알림 권한 있을 때: 안내 텍스트, 역 선택 UI, 저장 버튼이 표시된다
- [ ] 알림 권한 없을 때: Lottie 애니메이션, 권한 없음 안내 문구, 닫기 버튼이 표시된다
- [ ] 주말 포함 스위치 ON/OFF 시 요일 텍스트가 변경된다
- [ ] 역 미선택 시 gray 원형 뱃지 + "역 선택" 텍스트가 표시된다
- [ ] 역 버튼 탭 시 해당 그룹의 저장역 목록으로 Push 화면 진입
- [ ] 역 선택 화면에서 역 탭 시 pop하며 선택 역이 업데이트된다
- [ ] 역 선택 화면에서 알림 끄기 버튼 탭 시 해당 역이 "역 선택"으로 리셋된다
- [ ] 저장역 없을 때 "현재 저장되어 있는 지하철역이 없어요." 표시 및 알림 끄기 버튼 숨김
- [ ] 저장 버튼 탭 시 역 ID + 주말포함 여부가 로컬에 저장되고 알림 스케줄이 갱신된다
