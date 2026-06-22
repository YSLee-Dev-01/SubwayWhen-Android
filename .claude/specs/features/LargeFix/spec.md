# LargeFix

## 무엇을 하는가
갤럭시 플립3 실기기 테스트에서 발견된 UI/UX 버그 및 불일치 사항을 일괄 수정한다.
홈·검색·편집·설정·상세 화면 전반에 걸쳐 레이아웃, 사이즈, 애니메이션, 기능 버그를 수정한다.

---

## 동작 명세

### 1. 공통 - 스크롤 컨텐츠 상단 타이틀 가림
- 트리거: 홈/검색/편집/설정 화면 진입
- 결과: 화면 최상단에 위치한 타이틀 뷰가 잘리지 않고 완전히 표시됨
- 원인: 스크롤 컨텐츠 최상단에 배치된 타이틀 뷰가 고정 상단 바(TopBar 혹은 그에 준하는 뷰)에 가려짐
- 수정 방법: LazyColumn/Column 등 스크롤 컨텐츠 영역에 TopBar 높이만큼 contentPadding.top 추가. 공통 TopBar(`CommonTopBar.kt`)를 사용한다면 해당 컴포넌트에서 일괄 처리.

### 2. Home 화면

#### 2-1. 빈 역 셀의 + 아이콘 사이즈
- 트리거: 저장된 역이 없을 때 홈화면 진입
- 결과: + 아이콘이 현재 사이즈의 60% (40% 감소)로 표시됨

#### 2-2. 혼잡도 Modal 닫기 버튼 하단 여백
- 트리거: 혼잡도 Modal 열기
- 결과: 닫기 버튼 하단 여백이 다른 Modal과 동일하게 표시됨
- 수정 방법: `CongestionModal`이 `CommonModalBottomSheet`을 사용하지 않고 있다면 `CommonModalBottomSheet`를 사용하도록 교체

#### 2-3. 혼잡도 그래프 고속터미널 좌우 여백
- 트리거: 혼잡도 Modal에서 "고속터미널" 선택
- 결과: 다른 역과 동일한 좌우 여백으로 그래프가 표시됨
- 수정 방법: 그래프 스케일(최대값)과 무관하게 그래프 컨테이너의 좌우 패딩을 모든 역에 동일하게 고정. 데이터 최대값은 각 역 데이터 그대로 유지.

### 3. Detail 화면

#### 3-1. 열차 도착 애니메이션 재개발
- 트리거: 상세 화면 진입 및 실시간 도착정보 수신
- 결과: iOS와 동일한 자연스러운 열차 위치 이동 애니메이션이 표시됨
- 수정 방법: iOS 원본(`SubwayWhen/Presentation/Detail/`) 참조하여 `DetailTrainPositionView.kt` 전면 재구현. 기존 구현은 삭제하고 iOS 애니메이션 기준으로 처음부터 재개발.

### 4. Edit 화면

#### 4-1. 역 Cell 사이즈 축소
- 트리거: 편집 화면 진입
- 결과:
  - 역 Cell 전체 높이/사이즈: 현재보다 20% 축소
  - 역 호선 원형 뱃지 사이즈: 현재보다 40% 축소
  - 폰트 사이즈: Cell/원형 비율에 맞게 비례 축소 (기존 값 × 축소 비율)

### 5. Search 화면

#### 5-1. 가까운 지하철역 찾기 기능 버그
- 트리거: 검색 화면에서 "가까운 지하철역 찾기" 실행 (실제 근처에 역이 존재하는 상황)
- 결과: 가까운 역 목록이 정상적으로 표시됨
- 현재 증상: 가까운 역이 있음에도 "가까운 지하철역이 없다"고 표시됨
- 수정 방법: `feature/search/vicinity/` 로직 디버깅 — 위치 권한 처리, GPS 좌표 수신, 역 거리 필터링 조건 점검

#### 5-2. 검색 결과 Cell 사이즈 축소
- 트리거: 검색어 입력 후 결과 표시
- 결과: Edit 화면 Cell과 동일한 비율로 표시됨
  - 역 Cell 전체 사이즈: Edit 화면 기준과 동일하게 20% 축소
  - 역 호선 원형 뱃지 사이즈: Edit 화면 기준과 동일하게 40% 축소

#### 5-3. 역 저장 완료 Modal 체크 애니메이션 속도
- 트리거: 역 저장 후 완료 Modal 표시
- 결과: 체크 애니메이션이 현재보다 2배 빠르게 재생됨
- 수정 방법: `SaveCompletedModal` 체크 애니메이션 duration을 현재값의 50%로 변경

### 6. Setting 화면

#### 6-1. 출퇴근 역 셀 원형 뱃지 사이즈 축소
- 트리거: 설정 화면 > 출퇴근 지하철역 섹션 진입
- 결과:
  - 출근/퇴근 시간 셀의 역 호선 원형 뱃지 사이즈: 현재보다 40% 축소
  - 폰트 사이즈: 원형 비율에 맞게 비례 축소

#### 6-2. 출근/퇴근 셀 상하행 표시 제거
- 트리거: 설정 화면 > 출퇴근 지하철역 섹션
- 결과: 출근/퇴근 시간 셀에서 상하행 텍스트/컴포넌트가 표시되지 않음
- 수정 방법: 해당 셀 컴포넌트에서 상하행 관련 뷰 제거

#### 6-3. 열차 아이콘 선택 시 원형 아이콘에 열차 아이콘 가림 버그
- 트리거: 설정 화면 > 열차 아이콘 섹션 > 아이콘 탭
- 결과: 선택된 열차 아이콘이 오른쪽 원형 아이콘 위에 정상적으로 표시됨
- 현재 증상: 열차 아이콘이 원형 아이콘에 가려져 보이지 않음
- 수정 방법: 열차 아이콘이 원형 아이콘 위에(Z-order 최상위) 렌더링되도록 수정

#### 6-4. 기타 섹션 Firebase 정보 표시
- 트리거: 설정 화면 > 기타 섹션 진입
- 결과: Firebase Realtime DB에서 가져온 정보가 기타 섹션에 표시됨
- 현재 증상: 아무런 정보가 표시되지 않음
- 수정 방법: iOS 원본(`SubwayWhen/Presentation/Setting/`) 기타 섹션 구현 참조. 동일한 Firebase 경로 및 데이터 구조로 Android에서 연동. 기존 `data/remote/firebase/` 활용.

---

## 무엇이 잘못될 수 있는가
- 공통 TopBar/contentPadding 수정 시 일부 화면에서 레이아웃 틀어짐 → 화면별 진입 확인 필요
- CongestionModal을 CommonModalBottomSheet으로 교체 시 기존 UX 차이 발생 가능 → 기능 동작 검증 필요
- 그래프 여백 고정 시 데이터가 많은 경우 그래프 바가 컨테이너를 넘칠 수 있음 → 최대값 클리핑 처리 확인
- Vicinity 위치 버그 수정 시 권한 거부 케이스와 정상 케이스 모두 테스트 필요
- Firebase 기타 섹션: 네트워크 미연결 시 빈 화면 또는 에러 처리 필요
- Edit/Search Cell 비례 축소 시 폰트가 너무 작아질 수 있음 → 최소 폰트 사이즈(`fontSizeSuperSmall` = 9sp) 이하로 내려가지 않도록 확인

---

## 무엇에 의존하는가

### 의존성
- `ui/common/CommonTopBar.kt` — 상단 타이틀 가림 수정
- `feature/home/modal/CongestionModal.kt` — 혼잡도 Modal 수정
- `ui/common/modal/CommonModalBottomSheet.kt` — 혼잡도 Modal 공통화
- `feature/detail/component/DetailTrainPositionView.kt` — 열차 도착 애니메이션 재개발
- `feature/edit/component/` — Edit 화면 Cell 사이즈
- `feature/search/vicinity/` — 가까운 역 찾기 버그
- `feature/search/component/` — 검색 결과 Cell 사이즈
- `feature/search/modal/SaveCompletedModal.kt` — 체크 애니메이션 속도
- `feature/setting/component/` 또는 `feature/setting/modal/WorkAlarmModal.kt` — 출퇴근 셀 수정
- `data/remote/firebase/` — 기타 섹션 Firebase 연동
- iOS 원본 `/Users/yslee/Desktop/Project/SubwayWhen` — 애니메이션, 기타 섹션 참조

### 제약
- 공통 컴포넌트(`CommonTopBar`, `CommonModalBottomSheet`) 수정 시 해당 컴포넌트를 사용하는 모든 화면에 영향 — 신중하게 수정
- Dimens.kt 토큰 규칙: 2회 이상 반복되는 값만 Dimens.kt에 추가
- iOS 원본 직접 참조 경로: `/Users/yslee/Desktop/Project/SubwayWhen`

---

## Acceptance Criteria
- [x] 홈/검색/편집/설정 화면에서 최상단 타이틀이 잘리지 않고 완전히 표시됨
- [x] 홈화면 빈 역 셀의 + 아이콘이 기존 대비 60% 크기로 표시됨
- [x] 혼잡도 Modal의 닫기 버튼 하단 여백이 다른 Modal과 동일함
- [x] 고속터미널 선택 시 혼잡도 그래프 좌우 여백이 다른 역과 동일함 (스케일은 유지)
- [x] Detail 화면 열차 도착 애니메이션이 iOS 기준으로 자연스럽게 동작함
- [x] Edit 화면 역 Cell이 기존 대비 80% 크기, 원형 뱃지가 60% 크기로 표시됨
- [ ] 가까운 지하철역 찾기 기능이 정상 동작하여 근처 역 목록을 표시함
- [x] 검색 결과 Cell 사이즈가 Edit 화면 Cell과 동일한 비율로 표시됨
- [x] 역 저장 완료 Modal의 체크 애니메이션이 기존보다 2배 빠르게 재생됨
- [x] 설정 화면 출퇴근 셀 원형 뱃지가 기존 대비 60% 크기로 표시됨
- [x] 설정 화면 출퇴근 셀에서 상하행 정보가 표시되지 않음
- [x] 설정 화면 열차 아이콘 탭 시 원형 아이콘 위에 열차 아이콘이 정상 표시됨
- [x] 설정 화면 기타 섹션에 Firebase에서 가져온 정보가 iOS와 동일하게 표시됨
