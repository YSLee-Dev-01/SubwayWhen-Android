# ShinBundang-Schedule

## 무엇을 하는가
신분당선의 지하철 시간표를 불러오기 위해 사용.
로컬 DB(Room)와 Firebase를 사용한다.

로컬 DB에 저장된 데이터가 있고 버전이 최신이면 로컬 DB 데이터를 사용.
없거나 버전이 낮은 경우에만 Firebase에서 데이터를 가져온 후 DB에 저장 후 표출.

Firebase의 신분당선 데이터는 버전(`Double`)으로 관리되며,
버전은 시간표 데이터와 동일한 Room 엔티티에 `String` 필드로 함께 저장.

iOS 참조:
- `SubwayWhenNetworking/DataLoad/TotalLoadModel.swift` — `shinbundangScheduleLoad()`
- `SubwayWhenNetworking/DataLoad/LoadModel/LoadModel.swift` — `shinbundangScheduleReqeust()`, `shinbundangScheduleVersionRequest()`
- `SubwayWhenNetworking/CoreData/` 전체

## 동작 명세
- 트리거: 홈 화면에서 사용자가 신분당선 역의 시간표 버튼을 눌렀을 때
- 결과:
  1. Firebase에서 버전 번호를 조회
  2. 로컬 DB에 같은 역 데이터가 있고 저장된 버전 >= Firebase 버전이면 → 로컬 DB 데이터 반환
  3. 없거나 버전이 낮으면 → Firebase에서 시간표 데이터 요청
     - `isDisposable = false`이면 로컬 DB에 저장 (기존 데이터가 있으면 먼저 삭제 후 저장)
     - `isDisposable = true`이면 저장하지 않고 데이터만 반환
  4. 상하행 / 평일·주말 / exceptionLastStation 필터링 후 `ResultSchedule` 목록 반환
- 사이드이펙트: Firebase 네트워크 요청, Room DB 읽기/쓰기/삭제
- 불변 조건: 홈 화면 내부에서 이루어져야 함 (ViewModel → TotalLoadModel → LoadModel/Room 계층)

## 무엇이 잘못될 수 있는가
- Firebase 네트워크 에러 → 빈 목록 반환
- Room DB 읽기/쓰기 에러 → 에러 로그 후 빈 목록 반환

## 무엇에 의존하는가
### 의존성
- `TotalLoadModel` — 버전 비교, 로컬 DB 조회/저장, 필터링까지 통합 처리
- `LoadModel` — Firebase 네트워크 통신 (버전 요청, 시간표 요청)
- Room DB — 신분당선 시간표 + 버전 로컬 캐시 (이번 기능에서 최초 도입)

### 제약
- LoadModel에 직접 의존하지 않고 TotalLoadModel을 사용
- 로컬 DB(Room)를 우선 사용 — 버전 비교 후 낮을 때만 Firebase 통신
- Firebase 통신 후 `isDisposable = false`이면 무조건 로컬 DB에 저장
- 버전은 Room 엔티티(`stationName`, `scheduleData`, `scheduleVersion`) 내에 함께 저장

## Acceptance Criteria
- [x] 신분당선 시간표를 정상적으로 가져올 수 있다
- [x] 로컬 DB에 캐시된 데이터가 있고 버전이 최신이면 Firebase 요청 없이 반환한다
- [x] Firebase에서 받은 데이터는 Room DB에 저장된다 (`isDisposable = false`인 경우)
- [x] `isDisposable = true`이면 DB 저장 없이 데이터만 반환한다
- [x] 버전이 낮은 기존 데이터는 삭제 후 새 데이터로 교체된다
