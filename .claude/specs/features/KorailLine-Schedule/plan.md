# Plan: KorailLine-Schedule

## 참조 Spec
- @specs/features/KorailLine-Schedule/spec.md

## 참조 Skill
- 신규 화면 생성이 아니므로 create-feature Skill 미사용 (홈 화면 내부 시간표 로직 보강 작업)

## 현재 상태 파악

### 재사용 (변경 없음)
- `data/remote/loadmodel/LoadModelImpl.kt` `korailScheduleLoad()` — 코레일 시간표(KorailHeader) 네트워크 통신점. 이미 정상 동작.
- `data/network/NetworkResult.kt` — 네트워크 결과 래퍼.
- `data/remote/firebase/FirebaseDataSourceImpl.kt` — Firebase Realtime DB 접근 패턴 (`database.reference.child(...)`). KorailTrainData 조회 시 동일 패턴 차용.
- `feature/home/HomeViewModel.kt` `handleScheduleTap()` — 코레일/서울 분기는 이미 존재 (`station.korailCode.isNotEmpty()`).
- `data/remote/dto/scheduleArrival/korail/KorailScheduleModel.kt` — `KorailHeader` / `KorailSchedule` DTO (응답 파싱은 정상).

### 수정
- `data/remote/dto/scheduleArrival/korail/KorailScheduleModel.kt`
  - 코레일 트레인 넘버 모델(`KorailTrainNumber`: endStation, isFast, line, startStation, trainNumber, week) 추가.
- `data/remote/firebase/FirebaseDataSource.kt` / `FirebaseDataSourceImpl.kt`
  - `SubwayWhen/KorailTrainData/value` 경로에서 트레인 넘버 목록을 가져오는 함수 추가.
- `data/remote/totalload/TotalLoadModel.kt` / `TotalLoadModelImpl.kt`
  - `korailScheduleLoad()`가 단순히 `KorailHeader`를 그대로 반환하는 현재 구조를 보완.
  - iOS `korailSchduleLoad()`의 가공 로직(상하행 구분 + 트레인 넘버 조인)을 TotalLoadModel 레이어로 흡수. (spec 제약: HomeViewModel/Mapper가 LoadModel·Firebase에 직접 의존하지 않고 TotalLoadModel만 사용)
- `feature/home/mapper/HomeCellMapper.kt`
  - `KorailHeader.toScheduleCell()`을 가공된 결과 모델 기준으로 재작성 (상하행/lastStation/급행 반영).

### 신규
- (선택) 가공된 코레일 시간표 결과를 담을 내부 모델 — 기존 `HomeCellData`로 충분하면 추가하지 않음. 결정사항 참조.

### 삭제
- 없음.

## 핵심 차이점 (iOS korailSchduleLoad vs 현재 Android)

현재 Android `KorailHeader.toScheduleCell()`은 응답 `body`에서 **시간만 비교**하여 가장 가까운 열차를 고른다. 다음 iOS 로직이 누락되어 있다.

1. **상하행 구분 (가장 중요)**
   - iOS: `trainCode`의 마지막 숫자가 짝수면 상행, 홀수면 하행. `scheduleSearch.upDown`과 일치하는 열차만 남긴다.
   - 현재 Android: 방향 필터 전혀 없음 → 반대 방향 열차가 섞여 잘못된 시간표를 보여줄 수 있음.

2. **트레인 넘버 조인 (lastStation / startStation / 급행)**
   - iOS: Firebase `KorailTrainData`에서 `KorailTrainNumber` 목록을 받아 `KorailSchedule.trainCode == KorailTrainNumber.trainNumber`로 매칭하여 종착역(endStation)/출발역(startStation)/급행 여부(isFast == "급행")를 주입.
   - 현재 Android: 코레일 응답에 종착역이 없어 `lastStation`을 이전 셀 값(`prev.lastStation`)으로 둠 → 종착역·급행 정보 부정확.

3. **요일별 트레인 넘버 필터**
   - iOS: dayType이 평일이면 `week == "평일"`, 아니면 `week == "주말"`인 트레인 넘버만 사용.
   - 현재 Android: 해당 데이터 자체가 없음.

4. **exceptionLastStation 필터**
   - iOS: 주입된 lastStation 기준으로 `exceptionLastStation.contains(lastStation)`인 열차 제외.
   - 현재 Android: 열차별 lastStation이 없어 적용 불가 → 위 2번 선행 필요.

5. **빈 결과 처리**
   - iOS: 결과 없으면 `정보없음`. 현재 Android: `운행 종료`. → 기존 Android 동작(운행 종료) 유지하되, 가공 후에도 매칭이 없으면 동일 처리.

## 기술적 결정사항

- **가공 로직 위치 = TotalLoadModel**: spec 제약("LoadModel에 직접 의존하지 않고 TotalLoadModel 사용")에 따라, 상하행 판별·트레인 넘버 조인·요일 필터 가공은 TotalLoadModelImpl에 둔다. HomeViewModel/Mapper는 가공 완료된 결과만 받는다. 대안(Mapper에서 가공)은 Firebase 의존을 feature 레이어로 끌어와 제약 위반이라 기각.

- **반환 타입**: `korailScheduleLoad()`가 `NetworkResult<KorailHeader>` 대신, 상하행/종착역/급행이 채워진 가공 결과(예: 가까운 열차 1건을 표현하는 경량 모델 또는 필터링된 `KorailSchedule` 리스트 + 트레인 넘버 메타)를 반환하도록 변경. 단순화를 위해 "현재 시각 이후 가장 가까운 1건"을 ViewModel이 고르도록 시간 필터는 Mapper에 남길 수도 있음 → 구현 시 한 곳으로 통일. 결정: **상하행/조인/요일 필터까지는 TotalLoadModel, 현재시각 기준 최근접 선택 + 표시 포맷은 Mapper**로 분리(서울 시간표 `toScheduleCell`과 대칭).

- **Firebase 캐싱 도입**: `KorailTrainNumber` 목록은 앱 세션 내 변경되지 않으므로, `FirebaseDataSourceImpl`에서 인메모리 캐시(`List<KorailTrainNumber>?` 변수)로 보관한다. 첫 호출 시 Firebase에서 읽고 이후 호출은 캐시에서 반환. 앱 재시작 시 초기화(세션 캐시).

- **상하행 텍스트 기준**: `SaveStation.updnLine`("상행"/"하행")을 그대로 사용. 코레일 노선(수인분당선/경춘선/경의중앙선)은 9호선 같은 방향 반전이 없으므로 반전 처리 불필요.

## 구현 순서

### Phase 1. 데이터 모델
- `KorailScheduleModel.kt`에 `KorailTrainNumber`(endStation, isFast, line, startStation, trainNumber, week) `@Serializable` data class 추가.
- verify: 컴파일 성공.

### Phase 2. Firebase — 트레인 넘버 조회
- `FirebaseDataSource`에 `getKorailTrainNumberList(): List<KorailTrainNumber>?` 추가.
- `FirebaseDataSourceImpl`에서 `SubwayWhen/KorailTrainData/value` 경로 파싱 (기존 getSearchQueryRecommendList 패턴 차용, 실패 시 null/빈 리스트).
- 인메모리 캐시 변수(`cachedKorailTrainNumbers`) 추가: 첫 호출 시 Firebase 조회 후 저장, 이후 호출은 캐시 반환.
- verify: 실제 노드 구조와 필드명(endStation/isFast/line/startStation/trainNumber/week) 매핑 일치 확인.

### Phase 3. TotalLoadModel — 가공 로직
- `TotalLoadModelImpl.korailScheduleLoad()` 보강:
  1. LoadModel로 `KorailHeader` 조회.
  2. Firebase로 해당 요일(평일/주말) 트레인 넘버 조회.
  3. `trainCode` 마지막 숫자 짝/홀 → 상/하행 판별 후 `station.updnLine`과 일치하는 열차만 필터.
  4. 시각이 비어있는 열차(`time` null/"") 제거.
  5. trainCode ↔ trainNumber 조인하여 lastStation/startStation/isFast(급행) 주입.
  6. `exceptionLastStation.contains(lastStation)` 열차 제외.
  7. 시간 오름차순 정렬.
- 인터페이스(`TotalLoadModel.korailScheduleLoad`) 반환 타입을 가공 결과에 맞게 조정 (NetworkResult 유지, data만 가공 모델로).
- verify: 단위 테스트 — 짝/홀 trainCode 입력 시 상하행 분리, 트레인 넘버 조인으로 종착역·급행 주입, 평일/주말 분기.

### Phase 4. Mapper / ViewModel 연결
- `HomeCellMapper.KorailHeader.toScheduleCell()`을 Phase 3 가공 결과 타입 기준으로 수정: 현재 시각 이후 최근접 1건 선택, `stateMSG`(HH:mm)·`subPrevious`(N분)·`lastStation`(종착역행)·`isFast`(급행) 세팅. 매칭 없으면 기존 `운행 종료` 유지.
- `HomeViewModel.handleScheduleTap()`은 분기 구조 유지, 반환 타입 변경에 맞춰 매핑 호출부만 조정.
- verify: 수인분당선/경춘선/경의중앙선 저장 역에서 시간표 버튼 탭 시 올바른 방향·종착역·시각 표시.

## 완료 조건
- [ ] 수인분당선/경춘선/경의중앙선 시간표를 정상적으로 가져온다 (Spec Acceptance Criteria).
- [ ] 상행/하행 방향이 `updnLine`과 일치하는 열차만 표시된다 (짝수=상행/홀수=하행).
- [ ] 종착역(행)·급행 여부가 트레인 넘버 조인으로 채워진다.
- [ ] 평일/주말에 따라 올바른 트레인 넘버 셋이 적용된다.
- [ ] 네트워크/Firebase 실패 시 `정보없음`(또는 기존 운행종료) 으로 graceful 처리되고 크래시가 없다.
- [ ] HomeViewModel/Mapper가 LoadModel·Firebase에 직접 의존하지 않고 TotalLoadModel만 사용한다 (Spec 제약).
