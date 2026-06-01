# Main (메인 화면)

## 무엇을 하는가

앱의 핵심 화면. 사용자가 저장한 지하철 역 목록과 각 역의 실시간 도착정보를 한눈에 확인한다.
출근(ONE)/퇴근(TWO) 그룹 탭으로 시간대별 즐겨찾기를 분리하고, 설정된 시간 기준으로 자동 전환한다.

---

## 헤더 레이아웃 (iOS 원본 기준)

```
[혼잡도 카드 버튼 ────────────────────── full width, h=90]
 제목: "현재 지하철 예상 혼잡도"
 이모지 10개 + chevron.right
[민원 버튼 ─────────] [편집 버튼 ─────────]  (각 50%, h=90)
 "지하철 민원" + 아이콘   "편집" + 아이콘

실시간 현황
[그룹 탭: 출근(ONE) ──── 70%  | 퇴근(TWO) ── 30%]
```

---

## 동작 명세

### 화면 진입 시

- 트리거: 앱 시작 또는 홈 탭 선택
- 결과:
  1. 요일별 메인 타이틀 표시 (랜덤 메시지)
  2. 헤더 버튼 3개 표시 (혼잡도 / 민원 / 편집)
  3. 시간 기반 자동 그룹 전환 (mainGroupOneTime, mainGroupTwoTime 기준)
  4. 현재 그룹의 저장 역 목록 로드 → 각 역 카드를 loading 상태로 표시
  5. 실시간 도착정보를 역별로 병렬 요청 → 응답 오면 해당 카드 업데이트
- 사이드이펙트: 네트워크 API 호출 (stationArrivalRequest)

### 헤더 버튼 탭

- **이번 구현**: 버튼 UI만 배치하고 콜백 파라미터만 정의. 실제 화면 이동 없음.
- `onCongestionTap: () -> Unit` — 혼잡도 present (미래 연결)
- `onReportTap: () -> Unit` — 민원 push (미래 연결)
- `onEditTap: () -> Unit` — 편집 push (미래 연결)

### 그룹 탭 전환

- 트리거: 출근/퇴근 탭 버튼 탭
- 결과:
  1. 탭 너비 애니메이션 (선택됨 70% : 미선택 30%)
  2. 선택된 그룹의 역 목록으로 교체
  3. 새 목록의 실시간 도착정보 재요청

### 역 카드

- 각 카드 표시 요소:
  - 호선 색상 원형 뱃지 (호선명 텍스트 포함, 60dp)
  - `역명 | 종착역행` (exceptionLastStation 있으면 종착역 대신 사용)
  - 상태 메시지: `useFast + stateMSG` (schedule 타입은 ⏱️ prefix)
  - 도착 시간 영역:
    - `loading` 타입 → 도착 시간 텍스트 숨김 + 로딩 인디케이터 표시
    - `real` / `schedule` 타입 → 도착 시간 텍스트 표시 + 인디케이터 숨김
  - 시간표 버튼 (타이머 아이콘, 호선 색상 배경)
- 도착 시간 계산 규칙 (`useTime` 대응):
  - `real` & arrivalTime == "0" & code == "0" → "곧 도착"
  - `real` & arrivalTime == "0" & code == "1" → "도착"
  - `real` & arrivalTime == "0" & code == "2" → "출발"
  - `real` & arrivalTime == "0" & 그 외 → subPrevious에서 `(` 이전 문자열
  - `real` & arrivalTime > 0 & < 60 → "{N}초"
  - `real` & arrivalTime >= 60 → "{N/60}분"
  - `schedule` → subPrevious 그대로
- `useFast` 계산 규칙:
  - isFast == "급행" → "(급)"
  - isFast == "ITX" → "(ITX)"
  - 그 외 → 빈 문자열

### 시간표 버튼

- 트리거: 역 카드의 타이머 버튼 탭
- 결과:
  1. 해당 카드를 loading 상태로 전환, 역명 아래 "시간표 로드 중" 표시
  2. 시간표 API 요청 (seoulStationScheduleLoad 또는 korailScheduleLoad, korailCode 여부로 분기)
  3. 응답 데이터를 schedule 타입으로 카드 업데이트

### 빈 목록

- 트리거: 현재 그룹의 저장 역이 0개
- 결과: "버튼을 눌러서 지하철 역을 추가할 수 있어요!" 안내 카드 표시
- 탭 시: `onNavigateToSearch()` 콜백 호출

### Pull-to-Refresh

- 트리거: 목록을 당겨서 새로고침
- 결과: 화면 진입과 동일한 흐름 재실행

### 역 카드 탭

- 결과: `onNavigateToDetail(stationItem)` 콜백 호출 (Detail 화면 미구현 → 콜백만 정의)

---

## 이번 구현에서 제외하는 기능 (미래 구현)

- **혼잡도 이모지 데이터** (P2): 버튼 UI는 구성하되, 이모지는 "🫥🫥🫥🫥🫥🫥🫥🫥🫥🫥" 고정값 표시
- **중요 알림 카드** (Firebase): 데이터 연동 후 추가
- **민원/편집 실제 화면 이동** (P3/별도): 콜백 파라미터만 정의

---

## 무엇이 잘못될 수 있는가

- 실시간 도착정보 API 실패 → 해당 카드에 "정보 없음" 표시 (앱 크래시 없음)
- 시간표 API 실패 → 해당 카드 이전 상태(real) 유지
- 저장 역 없음 → 빈 목록 UI 표시
- DataStore 읽기 실패 → 빈 목록, 기본 설정값 사용

---

## 무엇에 의존하는가

### 의존성

- `data/repository/LocalDataRepository` — `saveStations: StateFlow<List<SaveStation>>`, `saveSetting: StateFlow<SaveSetting>`
- `data/remote/totalload/TotalLoadModel` — 모든 네트워크 데이터 접근의 진입점 (`arrivalDataLoad`, `seoulScheduleLoad`, `korailScheduleLoad`)
- `data/model/SaveStation`, `SaveStationGroup`, `SaveSetting`
- `data/remote/dto/liveArrival/LiveStationModel`, `RealtimeStationArrival`
- `data/remote/dto/scheduleArrival/` — 서울/코레일 스케줄 모델
- `ui/common/StationLineCircle`, `SubwayLineMapper`, `AnimatedTapBox`

### 제약

- **모든 데이터 접근은 `TotalLoadModel`을 통한다** — `HomeViewModel`은 `TotalLoadModel`만 주입받고 `LoadModel`을 직접 참조하지 않는다. 이번 구현에서 `TotalLoadModel` 인터페이스 메서드 정의 + `TotalLoadModelImpl` 구현 포함.
- Detail 화면 미구현 → 역 카드 탭 시 `onNavigateToDetail` 콜백만 정의, 실제 이동은 추후 연결
- `HomeScreen.kt`에 이미 `HomeViewModel` hiltViewModel 주입 구조가 있음 → 활용

---

## Acceptance Criteria

- [x] 화면 진입 시 요일별 타이틀 메시지가 랜덤으로 표시된다
- [x] 혼잡도/민원/편집 버튼 UI가 헤더에 표시된다 (탭 시 콜백 호출만)
- [x] 저장된 역이 현재 그룹 기준으로 로드되어 카드 목록에 표시된다
- [x] 각 역 카드는 진입 직후 로딩 인디케이터를 표시한다
- [x] 실시간 도착정보가 수신되면 카드가 도착 시간으로 업데이트된다
- [x] 도착 시간이 0초이고 code == 0이면 "곧 도착"이 표시된다
- [x] 출근/퇴근 탭 선택 시 해당 그룹의 역 목록으로 교체된다
- [x] 탭 너비 애니메이션이 동작한다 (선택 70% / 미선택 30%)
- [x] 시간 기반 자동 그룹 전환이 진입 시 적용된다
- [x] 시간표 버튼 탭 시 해당 카드가 시간표 모드로 전환된다
- [x] 저장된 역이 없으면 빈 목록 안내 카드가 표시된다
- [x] 빈 목록 카드 탭 시 onNavigateToSearch 콜백이 호출된다
- [x] Pull-to-Refresh 동작 시 실시간 데이터가 재로드된다
