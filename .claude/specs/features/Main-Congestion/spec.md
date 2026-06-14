# Main-Congestion

## 무엇을 하는가
홈 화면에서 특정 역의 시간대별 혼잡도를 꺾은선 그래프로 보여주는 바텀시트 모달.  
사용자는 지원하는 역 중 하나를 선택하고, 현재 시간 기준 혼잡도와 시간대별 흐름을 한눈에 파악한다.  
마지막으로 선택한 역은 DataStore에 저장되어 다음 진입 시 유지된다.

## 동작 명세

- **트리거**: 홈 화면에서 "혼잡도" 버튼 탭
- **결과**:
  - `CommonModalBottomSheet`가 슬라이드업으로 표시됨 (mainTitle: "현재 지하철 예상 혼잡도", subTitle: "선택된 지하철역의 예상 혼잡도를 확인할 수 있어요.")
  - 상단: 지원 역 목록 수평 스크롤 칩 — 선택된 역은 테두리 강조 + bold
  - 중앙: 시간대(0~23시)별 혼잡도(%) 꺾은선 차트 (Vico 또는 MPAndroidChart)
    - 현재 시간(nowHour) 데이터 포인트: 빨간 점 + annotation ("%값%")
    - 차트 탭 시 해당 시간대 수직선(RuleMark) + annotation ("N시 · M%")
    - 현재 시간 탭 시 annotation 해제
  - X축: 0, 3, 6, 9, 12, 15, 18, 21, 23시 레이블
  - Y축: 30% 단위 grid + 레이블
  - 역 변경 또는 탭 선택 시 차트 smooth 애니메이션
  - 탭바 숨김
- **사이드이펙트**:
  - 역 변경 시 `SaveSetting.mainCongestionBaseStation`을 DataStore에 저장
- **불변 조건**:
  - 혼잡도 데이터는 `assets/CongestionData.json`에서만 로드 (네트워크 없음)
  - 요일(평일 / 토요일 / 일요일·공휴일)에 따라 다른 데이터 집합 사용
  - 공휴일 판별은 DataStore에 저장된 `holidayList`를 사용

## 무엇이 잘못될 수 있는가

- `CongestionData.json` 파싱 실패 → 빈 데이터로 처리 (빈 차트 표시, crash 없음)
- 선택된 역에 해당 요일 데이터 없음 → 빈 차트 표시
- `availableStationList`가 비어 있음 → 역 선택 칩 미표시, 차트 공백

## 무엇에 의존하는가

### 의존성
- `assets/CongestionData.json` — 역별·요일별·시간별 혼잡도 정적 데이터
  - 구조: `{ stations: { "역명": { hourly_congestion: { weekday/saturday/sunday: { "시": { percent, level } } } } } }`
- `DataStore` — `mainCongestionBaseStation` 키 (기본값: "강남")
- `DataStore` — `holidayList` 키 (공휴일 목록, 문자열 리스트 "yyyyMMdd")
- `SaveSetting.kt` — `mainCongestionBaseStation: String` 필드 (이미 존재)
- 차트 라이브러리 — Vico (`com.patrykandpatrick.vico`) 추가 필요
- `CommonModalBottomSheet` — 기존 공통 모달 컴포넌트

### 제약
- 지원 역 목록은 `CongestionData.json`에 키로 존재하는 역만 표시
- 1~4시 데이터가 없으면 percent=0, level=0으로 채움 (iOS 동일 처리)
- 모달 높이: 500dp (iOS 기준)

## Acceptance Criteria

- [ ] 홈 화면 혼잡도 버튼 탭 시 모달이 슬라이드업으로 열린다
- [ ] 모달 진입 시 탭바가 숨겨지고, 닫힐 때 복원된다
- [ ] 지원 역 목록이 수평 스크롤 칩으로 표시되며, 마지막 저장 역이 초기 선택된다
- [ ] 선택된 역의 시간대별 혼잡도가 꺾은선 차트로 표시된다
- [ ] 현재 시간에 빨간 점과 "%값%" annotation이 표시된다
- [ ] 차트를 탭하면 해당 시간대 수직선과 "N시 · M%" annotation이 표시된다
- [ ] 현재 시간 포인트를 탭하면 annotation이 해제된다
- [ ] 역 변경 시 차트가 애니메이션으로 갱신되고 DataStore에 저장된다
- [ ] 요일(평일/토/일·공휴일)에 따라 올바른 데이터 집합이 사용된다
- [ ] JSON 파싱 실패 시 빈 차트를 보여주며 crash가 발생하지 않는다
