# Tasks: KorailLine-Schedule

## 참조
- spec: `.claude/specs/features/KorailLine-Schedule/spec.md`
- plan: `.claude/specs/features/KorailLine-Schedule/plan.md`

## Task 목록

### Phase 1. 데이터 모델

#### [x] Task 1 — `KorailScheduleModel.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/dto/scheduleArrival/korail/KorailScheduleModel.kt`
- `@Serializable` data class `KorailTrainNumber` 추가: `endStation`, `isFast`, `line`, `startStation`, `trainNumber`, `week` 필드

---

### Phase 2. Firebase — 트레인 넘버 조회

#### [x] Task 2 — `FirebaseDataSource.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/firebase/FirebaseDataSource.kt`
- `getKorailTrainNumberList(): List<KorailTrainNumber>?` 함수 선언 추가

---

#### [x] Task 3 — `FirebaseDataSourceImpl.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/firebase/FirebaseDataSourceImpl.kt`
- 인메모리 캐시 변수 `cachedKorailTrainNumbers: List<KorailTrainNumber>?` 추가 (초기값 null)
- `getKorailTrainNumberList()` 구현: 캐시가 비어있으면 Firebase `SubwayWhen/KorailTrainData/value` 경로에서 조회 후 캐시에 저장, 이후 호출은 캐시 반환
- 기존 `getSearchQueryRecommendList()` 패턴(database.reference.child 접근 방식) 차용, 실패 시 null 반환

---

### Phase 3. TotalLoadModel — 가공 로직

#### [x] Task 4 — `TotalLoadModel.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModel.kt`
- `korailScheduleLoad()` 반환 타입을 가공된 결과(`NetworkResult<List<KorailSchedule>>` 또는 필요 시 신규 모델)로 변경

---

#### [x] Task 5 — `TotalLoadModelImpl.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModelImpl.kt`
- `korailScheduleLoad()` 보강:
  1. `LoadModel`로 `KorailHeader` 조회
  2. 요일 판별: 현재 날짜가 평일이면 `week == "평일"`, 아닌 경우 `week == "주말"` 트레인 넘버만 `FirebaseDataSource.getKorailTrainNumberList()`로 취득
  3. `KorailSchedule.trainCode` 마지막 숫자 짝수 → 상행, 홀수 → 하행으로 판별하여 `station.updnLine` 값과 일치하는 열차만 필터
  4. `time`이 null 또는 빈 문자열인 열차 제거
  5. `trainCode == trainNumber` 매칭으로 각 열차에 `endStation`(lastStation), `startStation`, `isFast`(급행 여부: `"급행"` 일치 여부) 주입
  6. `station.exceptionLastStation`에 주입된 `endStation`이 포함되면 해당 열차 제외
  7. 시간 오름차순 정렬 후 반환
  8. `LoadModel` 또는 Firebase 실패 시 `NetworkResult.Error` 또는 빈 리스트로 graceful 처리

---

### Phase 4. Mapper / ViewModel 연결

#### [x] Task 6 — `HomeCellMapper.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/mapper/HomeCellMapper.kt`
- `KorailHeader.toScheduleCell()` (또는 대응 함수)를 Phase 3 반환 타입 기준으로 수정
- 현재 시각 이후 가장 가까운 열차 1건 선택
- `stateMSG` → `HH:mm` 형식 시각, `subPrevious` → `N분 후`, `lastStation` → 종착역행, 급행 여부 반영
- 가공 후 매칭 열차 없으면 기존 `운행 종료` 동작 유지

---

#### [x] Task 7 ��� `HomeViewModel.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeViewModel.kt`
- `handleScheduleTap()` 내 코레일 분기(`station.korailCode.isNotEmpty()`) 의 `TotalLoadModel.korailScheduleLoad()` 호출부를 Task 4·5 변경된 반환 타입에 맞게 조정
- Mapper 호출부만 수정, 분기 구조 및 나머지 서울 로직 변경 없음

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공
- [ ] 상하행 분리 단위 테스트 통과 (짝수 trainCode → 상행, 홀수 trainCode → 하행)
- [ ] 트레인 넘버 조인 단위 테스트 통과 (종착역·급행 주입 확인)
- [ ] 평일/주말 분기 단위 테스트 통과
- [ ] HomeViewModel/Mapper가 LoadModel·Firebase에 직접 의존하지 않음 (TotalLoadModel만 사용)

### 기능 (AC)
- [ ] 수인분당선/경춘선/경의중앙선 시간표를 정상적으로 가져온다
- [ ] 상행/하행 방향이 `updnLine`과 일치하는 열차만 표시된다
- [ ] 종착역(행)·급행 여부가 트레인 넘버 조인으로 올바르게 채워진다
- [ ] 평일/주말에 따라 올바른 트레인 넘버 셋이 적용된다
- [ ] 네트워크/Firebase 실패 시 크래시 없이 graceful 처리된다
