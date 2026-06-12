# Setting-2

## 무엇을 하는가
Setting 화면에서 출퇴근 지하철역 버튼을 누르면 Modal을 띄운다.

Modal은 공통 modal을 사용한다.

### 알림 권한 분기

Modal 진입 시 알림 권한을 체크한다.

**권한 있음 (정상 흐름)**

Modal 밖 상단에 "출근/퇴근 시간이 0시로 설정되어 있으면 알림이 울리지 않아요." 텍스트를 표시한다.
- Modal이 열릴 때 아래서 위로 슬라이드인 애니메이션
- Modal이 닫힐 때 위에서 아래로 슬라이드아웃 애니메이션

Modal 안에는 위에서 아래 순서대로 아래와 같이 표시한다.
- title: 출퇴근 알림
- subTitle: 출퇴근 시간에 맞게 정해놓은 지하철역으로 알림을 주는 기능이에요.

주말 포함 : [토글 스위치] + 요일 텍스트
- 스위치 ON → 텍스트: "월 화 수 목 금 토 일"
- 스위치 OFF → 텍스트: "월 화 수 목 금"

출근시간  | 퇴근시간
선택된 역 | 선택된 역

호선(공통 원형 뷰) 역 이름
- 역이 선택되지 않은 경우 gray 컬러, "역 선택"으로 표시

저장 버튼
- 주말포함 여부, 출근 역 ID, 퇴근 역 ID를 로컬에 저장
- 저장 완료 후 알림 스케줄을 갱신한다 (NotificationManager.reschedule)


**권한 없음**

안내 텍스트 영역은 숨긴다.
Modal subTitle을 "알림 권한이 설정되어 있지 않아요."로 대체한다.
저장 버튼을 "닫기" 버튼으로 대체한다.
역/주말 포함 영역 대신 Lottie 애니메이션("Report")을 표시한다.

### 역 선택 화면 (SelectModal)

선택된 역 버튼을 누르면 Modal 내부에서 Navigation Push로 역 선택 화면을 표시한다.

구조:
- 상단 바: 뒤로가기 버튼 ↔ 알림 끄기 버튼 (종 X 아이콘, bell.slash)
- 해당 그룹(출근/퇴근)으로 저장된 지하철 역 목록

역 목록 상태:
- 역이 있으면: 알림 끄기 버튼 표시. 현재 선택된 역에 체크 표시
- 역이 없으면: "현재 저장되어 있는 지하철역이 없어요." 텍스트 표시, 알림 끄기 버튼 숨김

Cell은 Edit 화면에서 사용하는 Cell을 그대로 사용한다.
- Cell tap → pop하며, 해당 그룹의 선택된 역에 업데이트
- 알림 끄기 버튼 tap → "역 선택" 기본값으로 리셋 후 pop

SettingScreen의 `SettingModalType`에 `WorkAlarm` 케이스 추가 후 연결한다.

iOS 참조:
`/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Setting/SettingNotiModal`

## 동작 명세
- 트리거: 설정 화면에서 출퇴근 지하철역 버튼을 눌렀을 때
- 결과: 알림 권한 체크 → 권한 있으면 정상 Modal, 없으면 권한 안내 Modal 표시
- 사이드이펙트:
  - 진입 시: 로컬에서 저장된 역 ID, 주말포함 여부 로드
  - 저장 시: 역 ID 배열 + 주말포함 여부 로컬 저장 → 알림 스케줄 갱신
- 불변 조건: Modal은 권한 여부에 관계없이 항상 표시되어야 함

## 무엇이 잘못될 수 있는가
- 로컬 데이터를 불러오지 못할 수 있음
  → 주말 포함은 기본값(true)으로 세팅, 출퇴근 역은 "역 선택" 상태로 둔다.
- 알림 권한이 없음
  → 권한 없음 분기 UI로 표시. 저장 동작 없이 닫기만 가능.

## 무엇에 의존하는가
### 의존성
- Setting 화면 (`SettingContract.kt` — `SettingModalType.WorkAlarm` 추가 필요)
- 로컬 데이터 (DataStore: `isWeekendNotificationEnabled`, 저장역 ID)
- 알림 권한 (Android `POST_NOTIFICATIONS` 퍼미션 체크)
- NotificationManager (알림 스케줄 갱신 — 이번 구현 범위에 포함)

### 제약
- 이미 만들어진 로직 재활용 (EditStationRow Cell, CommonModalBottomSheet, StationLineCircle)
- 역 목록은 `SaveStationGroup.one` / `.two`로 필터링하여 그룹별 표시

## Acceptance Criteria
- [x] 출퇴근 지하철역 버튼 탭 시 Modal이 표시된다
- [x] 알림 권한 있을 때: 안내 텍스트, 역 선택 UI, 저장 버튼이 표시된다
- [x] 알림 권한 없을 때: Lottie 애니메이션, 권한 없음 안내 문구, 닫기 버튼이 표시된다
- [x] 주말 포함 스위치 ON/OFF 시 요일 텍스트가 변경된다
- [x] 역 미선택 시 gray 원형 뱃지 + "역 선택" 텍스트가 표시된다
- [x] 역 버튼 탭 시 해당 그룹의 저장역 목록으로 Push 화면 진입
- [x] 역 선택 화면에서 역 탭 시 pop하며 선택 역이 업데이트된다
- [x] 역 선택 화면에서 알림 끄기 버튼 탭 시 해당 역이 "역 선택"으로 리셋된다
- [x] 저장 역이 없을 때 "현재 저장되어 있는 지하철역이 없어요." 표시
- [x] 저장 버튼 탭 시 역 ID + 주말포함 여부가 로컬에 저장되고 알림 스케줄이 갱신된다
