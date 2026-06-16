# Detail

## 무엇을 하는가
저장된 역을 탭했을 때 해당 역·방향의 **실시간 도착 정보**와 **시간표**를 한 화면에서 확인할 수 있는 상세 화면.
홈 화면은 요약 카드만 보여주고, Detail은 두 번째 열차·애니메이션·시간표 전체를 제공한다.

---

## 동작 명세

### 진입
- 트리거: 홈 화면의 역 카드 탭 → `DetailSendModel`을 인자로 받아 진입
- `DetailSendModel` 필드: `upDown`, `stationName`, `lineNumber`, `stationCode`, `lineCode`, `exceptionLastStation`, `korailCode`

### 화면 구성 (3개 섹션)

#### 상단 헤더
- 호선 번호 + 역명 표시 (CommonTopBar 스타일)
- 이전역 / 현재역 / 다음역 이름 표시 (실시간 데이터 로드 후 반영)

#### 실시간 도착 섹션 (`DetailArrivalView`)
- 첫 번째·두 번째 열차 정보 표시
  - 도착 시간 (예: "3분", "곧 도착")
  - 열차 상태 코드 → 메시지 변환 (0=진입, 1=도착, 2=출발, 3=전역출발, 4=전역진입, 5=전역도착, 99=운행중)
  - 급행 여부, 목적지, 열차 번호
- **애니메이션 (필수)**
  - 열차 아이콘이 이전역 → 현재역 → 다음역 구간에서 열차 상태 코드에 따라 위치 이동
  - 새로고침 시 아이콘이 목적지 방향으로 슬라이드 이동 후 새 위치로 재배치
  - 급행 열차는 아이콘 구분 표시 (일반 / 급행)
  - `AnimatedTapBox` 및 `tween(durationMillis = Dimens.animationDurationMs)` 사용
- 남은 새로고침 카운트다운 타이머 표시 (15초 → 0초)
- 수동 새로고침 버튼 (1.2초 쿨타임)
- `...` 버튼 → Realtime 화면으로 이동 **(TODO: Realtime 화면 미구현, 버튼만 배치하고 동작은 추후 연결)**
- 데이터 없음 상태 안내
- exceptionLastStation 설정 시: 두 번째 열차 "⛔ 제외 행 설정됨" 안내

#### 시간표 섹션 (`DetailScheduleView`)
- 현재 시간 이후 열차만 표시 (최대 N개, 2열 그리드)
- 각 항목: "N분 후 / HH:MM / 목적지행 / 급행여부"
- 라인 유형별 처리
  - `.Seoul`: 서울교통공사 API
  - `.Korail`: 코레일 API (시간 형식 `0HMMSS` → `HH:MM` 변환)
  - `.Shinbundang`: Room DB (0시 → 24시 처리)
  - `.Unowned`: 공항철도·우이신설·경강선·서해선·GTX-A → "시간표 미제공" 표시
- "시간표 더보기" 버튼 → `DetailResultSchedule` 화면으로 이동

### 자동 새로고침
- 화면 진입 시 실시간 API + 시간표 API 동시 호출
- 이후 15초마다 실시간 API 재호출 + 시간표 정렬 재수행
- 화면 이탈 시 타이머 취소

### 사이드이펙트
- 실시간 도착 API 호출 (`singleLiveAsyncData`)
- 시간표 API 호출 (`scheduleDataFetchAsyncData`) — 라인별 분기
- 15초 반복 타이머 (ViewModel 내 coroutine)

### 불변 조건
- `DetailSendModel`은 진입 시 반드시 전달되어야 함 (null 없음)
- 시간표 유형(`.Unowned`)이면 시간표 API 호출 안 함

---

## 무엇이 잘못될 수 있는가
- 실시간 API 실패 → 빈 상태 + 재시도 안내 (타이머는 유지)
- 시간표 API 실패 → 빈 상태 표시
- 현재 시간 이후 열차 없음 → 전체 시간표 표시 (필터 비활성화)
- 네트워크 없음 → 마지막 성공 데이터 유지 + 오류 메시지

---

## 무엇에 의존하는가

### 의존성
- `TotalLoadModel.singleLiveAsyncData` — 실시간 도착 정보 (이미 HomeViewModel에서 사용)
- `TotalLoadModel.scheduleDataFetchAsyncData` — 시간표 조회 (이미 HomeViewModel에서 사용)
- `SubwayLineMapper` — 호선 색상 매핑
- `data/model/SaveStation` — 저장 역 모델 (진입 파라미터 원천)

### 공통 뷰 활용 (신규 컴포넌트 생성 금지 — 기존 것 사용)
| 컴포넌트 | 위치 | 사용처 |
|---------|------|-------|
| `CommonTopBar` | `ui/common/CommonTopBar.kt` | 상단 헤더 (뒤로가기 + 역명) |
| `StationLineCircle` | `ui/common/StationLineCircle.kt` | 호선 색상 원형 뱃지 |
| `AnimatedTapBox` | `ui/common/AnimatedTapBox.kt` | 새로고침 버튼·시간표 더보기 버튼 탭 애니메이션 |
| `MainBgCard` | `ui/common/MainBgCard.kt` | 실시간·시간표 섹션 배경 카드 |
| `UpDownExceptionRow` | `ui/common/UpDownExceptionRow.kt` | 상하행 예외 안내 (exceptionLastStation 설정 시) |
| `PrimaryButton` | `ui/common/PrimaryButton.kt` | 시간표 더보기 버튼 |
| `CommonModalBottomSheet` | `ui/common/modal/` | 제외 행 확인 다이얼로그 |

### 제약
- 홈 화면에서 `SaveStation` → `DetailSendModel` 변환 후 Navigation argument로 전달
- Navigation argument 직렬화 필요 (`Parcelable` 또는 JSON 직렬화)
- 15초 타이머는 `viewModelScope` 안에서 관리 (`GlobalScope` 금지)
- Jetpack Compose에는 iOS Live Activity 대응 없음 → 해당 기능 제외

---

## 하위 화면

### DetailResultSchedule (시간표 전체 보기)
- 시간표 더보기 버튼 탭 시 진입
- 시간(Hour)별 섹션 분리: "10시", "11시" ...
- 각 섹션 내: 분, 목적지, 시작역, 급행 여부 표시
- 현재 시간 섹션으로 자동 스크롤
- 제외 행 버튼: 특정 목적지행 열차 제외 → `exceptionLastStation` 갱신 후 Detail 재로딩

---

## Acceptance Criteria
- [x] 홈 화면 역 카드 탭 시 Detail 화면으로 진입
- [x] 진입 즉시 실시간 도착 정보 로딩 → 첫 번째·두 번째 열차 표시
- [x] 진입 즉시 시간표 로딩 → 현재 시간 이후 열차 표시
- [x] 15초마다 실시간 정보 자동 새로고침 + 카운트다운 UI
- [x] 수동 새로고침 버튼 동작 (1.2초 쿨타임)
- [x] 라인 유형별 시간표 처리 (Seoul / Korail / Shinbundang / Unowned)
- [x] 시간표 더보기 → DetailResultSchedule 화면 이동
- [x] DetailResultSchedule: 시간별 섹션 + 현재 시간 자동 스크롤
- [x] DetailResultSchedule: 제외 행 설정 → Detail 실시간 정보 재로딩
- [x] 화면 이탈 시 타이머 자동 취소
- [x] 실시간/시간표 API 실패 시 빈 상태 처리

## 알려진 제한
- `DetailTrainPositionView`의 다음역 이름이 표시되지 않음: `liveArrivalSplit`이 `backStationName`(이전역)만 조회하고 `nextStationName`(다음역)은 미구현 → 별도 task 필요
