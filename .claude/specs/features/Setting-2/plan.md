# Plan: Setting-2 (출퇴근 알림 Modal)

## 참조 Spec
- @specs/features/Setting-2/spec.md

## 참조 Skill
신규 화면(Modal) 생성 시
- @skills/create-feature/SKILL.md

## 현재 상태 파악

### 신규
- `ui/common/StationRow.kt` — mainBG 카드 + StationLineCircle + 역명 + updnLine + `trailingContent` 슬롯을 갖는 공통 역 Row 컴포넌트. `EditStationRow`와 `WorkAlarmSelectView` 모두 이 컴포넌트를 래핑한다
- `feature/setting/modal/WorkAlarmModal.kt` — 출퇴근 알림 메인 Modal (권한 분기 포함)
- `feature/setting/modal/component/WorkAlarmStationView.kt` — 주말포함 스위치 + 출근/퇴근 역 선택 2단 영역 (iOS `SettingNotiStationView` 대응)
- `feature/setting/modal/component/WorkAlarmSelectView.kt` — Modal 내부 Push되는 역 선택 화면. `StationRow`를 체크 아이콘 trailing 슬롯과 함께 사용 (iOS `SettingNotiSelectModalVC` 대응)
- `core/notification/NotificationScheduler.kt` (인터페이스) + `NotificationSchedulerImpl.kt` — 출퇴근 알림 스케줄 등록/갱신 (iOS `NotificationManager` 대응, AlarmManager 또는 WorkManager 기반)
- `core/notification/NotificationPermission.kt` (또는 Screen 내 `rememberLauncherForActivityResult`) — `POST_NOTIFICATIONS` 권한 체크/요청 유틸
- `di/NotificationModule.kt` — `NotificationScheduler` 바인딩

### 재사용
- `CommonModalBottomSheet` — Modal 컨테이너. `topDecoration` 슬롯에 안내 텍스트 배치, `confirmButton` 슬롯에 저장/닫기 버튼 배치
- `StationLineCircle` — 역 미선택 시 gray 폴백(`lineColor = null`), "역 선택" 케이스 표현 가능
- `subwayLineColor` / `subwayLineDisplayName` — 호선명 매핑
- `LottieAnimation` + `rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.report))` — 권한 없음 분기 애니메이션 ("Report" → `report.json` 이미 존재)
- `LocalDataRepository.updateSaveSetting` / `saveStations` / `saveSetting` — 로컬 로드·저장
- `LocationManager` 권한 패턴(`core/location/`) — `POST_NOTIFICATIONS` 권한 처리 시 구조 참고

### 수정
- `data/model/SaveSetting.kt` — `alertGroupOneId: String = ""`, `alertGroupTwoId: String = ""` 필드 추가 (iOS `alertGroupOneID`/`alertGroupTwoID` 대응). 현재 누락 상태
- `data/local/PreferencesKeys.kt` — `ALERT_GROUP_ONE_ID`, `ALERT_GROUP_TWO_ID` stringPreferencesKey 추가
- `data/local/SettingLocalDataSource.kt` — `getSaveSetting` 매핑 / `updateSaveSetting` 쓰기에 위 두 키 반영
- `feature/setting/SettingContract.kt`
  - `SettingModalType`에 `WorkAlarm` 케이스 추가
  - `SettingUiState`에 알림 Modal용 상태 추가 (권한 여부, 주말포함, 출근/퇴근 선택 역, 내부 Push 대상 그룹 등)
  - `SettingIntent`에 케이스 추가 (주말 토글, 역 버튼 탭, 역 선택, 역 리셋, 저장, Push/Pop 등)
- `feature/setting/SettingViewModel.kt`
  - `WorkAlarmTapped` 처리 → 권한 체크 → Modal 상태 세팅 + 저장역/주말포함 로드
  - 주말 토글, 역 선택/리셋, 저장(역 ID + 주말포함 저장 → `NotificationScheduler.reschedule`) 로직 추가
  - `NotificationScheduler` 주입
- `feature/edit/component/EditStationRow.kt` — 내부 Row 비주얼을 `StationRow`로 교체. 삭제 버튼(좌) + `StationRow` + 드래그 핸들(우) 구조는 동일하게 유지
- `feature/setting/SettingScreen.kt` — `when(activeModal)`에 `SettingModalType.WorkAlarm -> WorkAlarmModal(...)` 분기 추가
- `app/src/main/AndroidManifest.xml` — `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` 추가 (minSdk 29이지만 13+ 런타임 권한 대비)

### 삭제
- 없음

## 기술적 결정사항

- **권한 체크 위치**: iOS는 `requestAuthorization`을 ViewModel에서 호출하지만, Android `POST_NOTIFICATIONS` 런타임 권한은 Activity/Compose Context가 필요하다. → Screen(Composable)에서 `ContextCompat.checkSelfPermission` + `rememberLauncherForActivityResult`로 권한을 판정해 결과를 Intent로 ViewModel에 전달한다. ViewModel은 Boolean만 받는다. (대안: ViewModel에서 Application Context 주입 → 권한 요청 UI를 띄울 수 없어 부적합)
  - minSdk 29에서는 `POST_NOTIFICATIONS`가 SDK 33부터이므로, 33 미만은 항상 권한 있음으로 간주한다.

- **EditStationRow 공통화**: `EditStationRow`는 `ReorderableCollectionItemScope` 확장 + 삭제 버튼/드래그 핸들이 강결합되어 있어 역 선택 화면에서 그대로 재사용할 수 없다. → mainBG 카드 비주얼(StationLineCircle + 역명 + updnLine + `trailingContent` 슬롯)을 `ui/common/StationRow.kt`로 추출한다. `EditStationRow`는 `StationRow`를 래핑(삭제 버튼 + 드래그 핸들 추가)하고, `WorkAlarmSelectView`는 `StationRow`를 래핑(체크 아이콘 trailing 슬롯)하여 양쪽이 동일 컴포넌트를 공유한다.

- **Modal 내부 Navigation Push**: 별도 NavHost를 Modal 안에 두지 않고, `WorkAlarmModal` 내부에서 `activeSelectGroup: TimeGroup?` 상태로 메인 뷰 ↔ 역 선택 뷰를 `AnimatedContent`(slide-in/out)로 전환한다. iOS의 push/pop을 Compose 단일 Modal 내 화면 전환으로 재현. (대안: 중첩 NavHost — 과한 복잡도)

- **저장역 ID 저장 모델**: iOS와 동일하게 `SaveSetting.alertGroupOneId/alertGroupTwoId`(역의 `id`) String 2개로 저장한다. 별도 배열 타입 도입하지 않음.

- **선택된 역 표시 데이터 소스**: 저장된 `alertGroupOneId`로 `saveStations`에서 매칭(`id == alertId && group == ONE`)하여 표시할 역을 찾는다. 매칭 실패/빈 문자열이면 "역 선택" gray 상태.

- **알림 스케줄러 구현 깊이**: Spec "이번 구현 범위에 포함". iOS는 `UNCalendarNotificationTrigger`로 요일별 반복. Android는 `AlarmManager`(setRepeating/요일별 setExact 재등록) 또는 `WorkManager`. → **요일별 반복 정확 알림이 목적이므로 `AlarmManager` + `BroadcastReceiver` 권장**. 0시 설정 시 무시, 주말포함이면 매일/아니면 월~금. reschedule는 전체 취소 후 재등록. (구현 범위·정확도는 사용자 확인 필요)

- **Lottie 에셋명**: iOS "Report" = Android `R.raw.report` (이미 존재). `SaveCompletedModal` 패턴 그대로 사용.

- **불변 조건 (Modal 항상 표시)**: `activeModal == WorkAlarm`이면 권한 여부와 무관하게 `CommonModalBottomSheet`를 띄우고, 내부 content/subTitle/confirmButton만 권한 분기로 교체한다.

## 구현 순서

### Phase 1. 데이터 레이어 (저장역 ID 영속화)
- `SaveSetting`에 `alertGroupOneId`, `alertGroupTwoId` 추가 → verify: 컴파일, 기본값 `""`
- `PreferencesKeys` 키 2개 추가 → verify: 키 문자열 중복 없음
- `SettingLocalDataSource` read/write 매핑 추가 → verify: 라운드트립(저장→로드) 값 일치

### Phase 2. 알림 인프라
- `AndroidManifest.xml`에 `POST_NOTIFICATIONS` 권한 추가
- `NotificationScheduler` 인터페이스 + Impl 작성 (reschedule: 전체 취소 후 출근/퇴근 역·시간·주말포함 기준 재등록, 0시/빈 역 무시) → verify: 단위 테스트로 "0시·빈 역 → 등록 안 됨", "주말포함 ON/OFF → 등록 요일 수" 검증
- `di/NotificationModule`로 바인딩 → verify: Hilt 그래프 컴파일

### Phase 3. Contract / ViewModel
- `SettingModalType.WorkAlarm` + UiState/Intent 확장
- `WorkAlarmTapped`(권한 Boolean 수신) → Modal open + 저장역/주말포함 로드
- 주말 토글 / 역 선택(그룹+역 id) / 역 리셋(빈값) / 저장 Intent 처리
- 저장 시 `updateSaveSetting`(역 id 2개 + 주말포함) → `NotificationScheduler.reschedule`
- → verify: ViewModel 단위 테스트 — 역 선택 후 저장 시 SaveSetting에 id 반영 + reschedule 호출(MockK), 권한 없음 분기 상태

### Phase 4. 공통 컴포넌트 추출 및 UI (Modal)
- `StationRow` 신규 작성 (`ui/common/StationRow.kt`) — mainBG 카드 + StationLineCircle + 역명 + updnLine + `trailingContent` 슬롯
- `EditStationRow` 리팩터링 — 내부 Row 비주얼을 `StationRow`로 교체 → verify: Edit 화면 기존 동작 유지
- `WorkAlarmStationView` — 주말 스위치+요일 텍스트, 출근/퇴근 역 버튼(StationLineCircle + 역명/"역 선택")
- `WorkAlarmSelectView` — 상단바(뒤로가기 ↔ 알림 끄기 bell.slash), 그룹별 저장역 목록 (`StationRow` + 체크 아이콘 trailing), 빈 목록 안내, 알림 끄기 버튼 숨김 처리
- `WorkAlarmModal` — `CommonModalBottomSheet` 조립. 권한 있음: topDecoration 안내 텍스트 + StationView + 저장 버튼 / 권한 없음: subTitle 교체 + Lottie(report) + 닫기 버튼. 내부 `AnimatedContent`로 메인 ↔ Select 전환
- `SettingScreen` when 분기에 `WorkAlarm` 연결
- → verify: Preview 렌더(권한 O/X, 역 선택/미선택, 빈 목록), 수동 시나리오 — 버튼 탭→Modal, 주말 토글 텍스트 변경, 역 선택 pop 반영, 저장

## 미해결 / 사용자 확인 필요
1. 알림 스케줄러 구현 정확도 범위: `AlarmManager` 요일별 반복까지 구현할지, 우선 인터페이스+간단 등록만 둘지
2. `POST_NOTIFICATIONS` 권한 요청 UX (거부 시 재요청/설정 유도 여부) — Spec 명시 없음, 현재는 "권한 없음 분기 UI만" 표시로 가정

## 완료 조건
- [ ] Spec Acceptance Criteria 충족
- [ ] 출퇴근 버튼 탭 → 권한 분기 Modal 표시
- [ ] 권한 O: 안내 텍스트 / 역 선택 UI / 저장 버튼, 권한 X: Lottie(report) / 권한 안내 문구 / 닫기 버튼
- [ ] 주말 스위치 ON/OFF → 요일 텍스트("월 화 수 목 금 토 일" / "월 화 수 목 금") 변경
- [ ] 역 미선택 → gray 원형 + "역 선택"
- [ ] 역 버튼 탭 → 해당 그룹 저장역 목록 Push, 역 탭 → pop + 선택 반영, 알림 끄기 → "역 선택" 리셋 + pop
- [ ] 저장역 없음 → "현재 저장되어 있는 지하철역이 없어요." + 알림 끄기 버튼 숨김
- [ ] 저장 → 역 id 2개 + 주말포함 로컬 저장 + `NotificationScheduler.reschedule` 호출
- [ ] 로드 실패 시 주말포함 기본값 true, 출퇴근 역 "역 선택" 폴백
