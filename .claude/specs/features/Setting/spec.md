# Setting (설정 화면)

## 무엇을 하는가
앱 동작 방식을 사용자가 직접 제어하는 화면.
홈 화면 그룹 시간, 상세 화면 자동 새로고침·시간표 정렬, 열차 아이콘, 검색 중복 방지, 오픈 라이선스·기타 내용을 설정할 수 있다.
iOS `SettingView.swift` + `SettingFeature.swift` 포팅.

---

## 화면 구성 (섹션 × 셀)

### 섹션 1. 홈 화면 (`"홈 화면"`)
| 셀 | 타입 | 설명 |
|----|------|------|
| (라벨 없음) | **TimeRow** | 출근/퇴근 그룹 시간 설정. 각 그룹 버튼 탭 → 하단에 시간 선택 UI 인라인 펼침. 저장 시 `mainGroupOneTime` / `mainGroupTwoTime` 갱신 |
| 출근 알람 | **ArrowRow** | 탭 시 `notiModal` — **이번 스코프는 버튼만 표시, 탭해도 아무 동작 없음** |
| 혼잡도 이모지 | **TextFieldRow** | 이모지 1자리 입력. 빈 값 저장 시 기본값 `☹️` 대입. `mainCongestionLabel` 갱신 |

### 섹션 2. 상세 화면 (`"상세 화면"`)
| 셀 | 타입 | 설명 |
|----|------|------|
| 자동 새로고침 | **ToggleRow** | `detailAutoReload` 토글 |
| 시간표 자동 정렬 | **ToggleRow** | `detailScheduleAutoTime` 토글 |
| 열차 아이콘 | **ArrowRow** | 탭 시 `TrainIconModal` 열림 → 아이콘 선택 후 `detailVcTrainIcon` 갱신 |

> iOS `liveActivity` 항목은 Android에 해당 기능이 없으므로 제외.

### 섹션 3. 검색 화면 (`"검색 화면"`)
| 셀 | 타입 | 설명 |
|----|------|------|
| 중복 저장 방지 | **ToggleRow** | `searchOverlapAlert` 토글 |

### 섹션 4. 기타 (`"기타"`)
| 셀 | 타입 | 설명 |
|----|------|------|
| 오픈 라이선스 | **ArrowRow** | 탭 시 `LicenseModal` 열림. Firebase Realtime DB에서 라이선스 목록 조회 |
| 기타 | **ArrowRow** | 탭 시 `ContentsModal` 열림. Firebase Realtime DB에서 기타 내용 조회 |

---

## 동작 명세

### 진입
- 탭바 "설정" 탭 탭 → `SettingScreen` 진입
- `onAppear` 시 DataStore에서 현재 `SaveSetting` 로드

### TimeRow (그룹 시간 설정)
- 출근/퇴근 버튼 탭 → 해당 그룹의 인라인 시간 선택 UI 펼침 (0–23시, Stepper 또는 +/- 버튼)
  - 이미 펼쳐진 그룹 재탭 시 닫힘
  - 다른 그룹 탭 시 이전 그룹 닫히고 새 그룹 펼침
- 저장 버튼 탭 → `SaveSetting` 갱신 후 UI 닫힘
- 시간이 0인데 알람이 설정된 경우(alertGroupOneID/TwoID가 있는 경우): 아이콘 경고 표시 → **본 스코프에서는 해당 필드가 없으므로 표시 생략**

### ToggleRow
- 토글 탭 → 해당 `SaveSetting` 필드 즉시 갱신 후 반영

### TextFieldRow (혼잡도 이모지)
- 1글자로 제한 (입력 시 마지막 1글자만 유지)
- 포커스 해제 시 빈 값이면 `☹️` 저장

### ArrowRow → TrainIconModal
- `CommonModalBottomSheet` 사용
- 선택 가능한 열차 이모지 목록 표시 (iOS 원본과 동일 이모지 세트: 🚃🚋🚝🚄🚅🚇🚈🚂 )
- 현재 선택된 아이콘 하이라이트
- 확인 탭 → `detailVcTrainIcon` 갱신 후 모달 닫힘

### ArrowRow → LicenseModal
- `CommonModalBottomSheet` 사용
- `TotalLoadModel.getLicenses()` 호출 → 텍스트 목록 표시 (내부: `LoadModel` → Firebase `SubwayWhen/Licenses`)
- 로딩 중 / 실패 시 빈 내용 표시

### ArrowRow → ContentsModal
- `CommonModalBottomSheet` 사용
- `TotalLoadModel.getContents()` 호출 → 텍스트 표시 (내부: `LoadModel` → Firebase `SubwayWhen/Contents`)
- 로딩 중 / 실패 시 빈 내용 표시

---

## 무엇이 잘못될 수 있는가
- DataStore 초기화 전 `SaveSetting` 읽기 → `isInitialized` 대기 (기존 `LocalDataRepository` 패턴 따름)
- Firebase 조회 실패 → 모달 내 빈 상태 표시 (에러 메시지 불필요)
- 혼잡도 이모지 빈 값 저장 → 기본값 `☹️` 대입

---

## 무엇에 의존하는가

### 의존성
- `LocalDataRepository` — `saveSetting`, `updateSaveSetting` (이미 구현됨)
- `TotalLoadModel` — `getLicenses()`, `getContents()` (내부적으로 `LoadModel` → Firebase Realtime DB 경로)
- `CommonModalBottomSheet` — TrainIcon / License / Contents 모달

### 제약
- `liveActivity`, `alertGroupOneID`, `alertGroupTwoID` 항목은 이번 스코프 제외
- 탭바 모달 숨김/복원 패턴은 적용하지 않음 (모달이 설정 탭 내부에서 열림, 현재 탭바는 이미 탭 전환으로 자동 처리됨)

---

## Acceptance Criteria
- [x] 설정 섹션 4개가 각 타이틀("홈 화면" / "상세 화면" / "검색 화면" / "기타")과 함께 표시된다
- [x] 출근/퇴근 시간 버튼 탭 시 인라인 시간 선택 UI가 해당 방향으로 펼쳐지고, 저장 시 DataStore에 반영된다
- [x] "출근 알람" ArrowRow가 표시되고 탭해도 아무 동작이 없다
- [x] 혼잡도 이모지 텍스트필드는 1자리 이모지만 허용하고, 빈 값 저장 시 `☹️`로 대체된다
- [x] 토글 항목(자동 새로고침, 시간표 자동 정렬, 중복 저장 방지) 탭 시 즉시 DataStore에 반영되고 UI에 표시된다
- [x] 열차 아이콘 탭 시 TrainIconModal이 열리고 아이콘 선택 후 저장하면 `detailVcTrainIcon`이 갱신된다
- [x] 오픈 라이선스 탭 시 LicenseModal이 열리고 Firebase에서 라이선스 목록이 표시된다
- [x] 기타 탭 시 ContentsModal이 열리고 Firebase에서 내용이 표시된다
- [x] 앱 재시작 후에도 변경된 설정이 유지된다
