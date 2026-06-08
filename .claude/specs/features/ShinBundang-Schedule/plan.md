# Plan: ShinBundang-Schedule

## 참조 Spec
- @specs/features/ShinBundang-Schedule/spec.md

## 참조 Skill
- 신규 화면 생성이 아니므로 create-feature Skill 미사용 (홈 화면 내부 시간표 로직 보강 + Room DB 최초 도입 작업)

## 현재 상태 파악

### 재사용 (변경 없음)
- `data/network/NetworkResult.kt` — 네트워크 결과 래퍼. 신분당선도 동일하게 사용.
- `data/model/SaveStation.kt` — `stationName` / `updnLine` / `exceptionLastStation` / `line`(="신분당선") 사용. 변경 없음.
- `data/remote/firebase/FirebaseDataSource.kt` / `FirebaseDataSourceImpl.kt` — 변경 없음. 신분당선 Firebase 통신은 LoadModel에서 담당.
- `feature/home/HomeViewModel.kt` `handleScheduleTap()` — 시간표 버튼 탭 진입점. 코레일/서울 분기 구조 존재. 여기에 신분당선 분기를 추가.
- `data/remote/totalload/TotalLoadModelImpl.kt` `korailScheduleLoad()` — 상하행 판별·요일 필터·exceptionLastStation 필터·정렬을 TotalLoadModel 레이어에서 수행하는 기존 패턴. 신분당선도 동일 계층 구조를 따른다.

### 수정
- `gradle/libs.versions.toml`
  - Room 버전 및 라이브러리(`androidx-room-runtime`, `androidx-room-compiler`, 선택적으로 `androidx-room-testing`) 추가. (Room 최초 도입)
- `app/build.gradle.kts`
  - Room 의존성 추가(`implementation room-runtime`, `ksp room-compiler`). KSP 플러그인은 이미 적용되어 있으므로 추가 불필요.
- `data/remote/totalload/TotalLoadModel.kt` / `TotalLoadModelImpl.kt`
  - `shinbundangScheduleLoad(station, weekDay, isDisposable)` 함수 추가 (iOS `shinbundangScheduleLoad()` 대응). 버전 비교 → 로컬 DB 캐시 우선 → 필요 시 Firebase 조회 → 저장 → 필터링까지 통합 처리.
- `data/remote/loadmodel/LoadModel.kt` / `LoadModelImpl.kt`
  - `shinbundangScheduleVersionRequest(): Double?` — Firebase `SubwayWhen/ShinbundangLineScheduleVersion/version` 경로에서 버전 조회.
  - `shinbundangScheduleRequest(stationName: String): List<ShinbundangSchedule>?` — Firebase `SubwayWhenShinbundangScheduleData`(Keys/ScheduleList 구조)에서 해당 역 시간표 조회. iOS `loadModel.shinbundangScheduleVersionRequest/Reqeust` 직접 대응.
  - `LoadModelImpl` 생성자에 `FirebaseDatabase` 주입 추가 (DI 모듈도 함께 수정).
- `feature/home/HomeViewModel.kt`
  - `handleScheduleTap()`에 신분당선 분기 추가 (`station.line == "신분당선"` → `shinbundangScheduleLoad(...)`).
- `feature/home/mapper/HomeCellMapper.kt`
  - `List<ProcessedShinbundangSchedule>.toScheduleCell(prev)` 추가 — 현재 시각 이후 최근접 1건 선택, `stateMSG`(HH:mm)·`subPrevious`(N분)·`lastStation`(종착역행) 세팅. 매칭 없으면 기존 `운행 종료` 유지(코레일/서울 매퍼와 대칭).

### 신규
- `data/local/room/AppDatabase.kt` — Room `@Database`. 신분당선 시간표 엔티티 1개로 시작.
- `data/local/room/ShinbundangScheduleEntity.kt` — `@Entity`. 필드: `stationName`(PK), `scheduleData`(JSON 문자열), `scheduleVersion`(String).
- `data/local/room/ShinbundangScheduleDao.kt` — `@Dao`. `load(stationName)`, `insert(entity)`, `delete(stationName)`.
- `data/local/room/Converters.kt` — `List<ShinbundangSchedule>` ↔ JSON 문자열 `@TypeConverter` (kotlinx.serialization 사용). 또는 DAO 바깥(TotalLoadModelImpl)에서 직렬화하고 엔티티는 String만 보관 — 결정사항 참조.
- `data/remote/dto/scheduleArrival/shinbundang/ShinbundangScheduleModel.kt`
  - `ShinbundangSchedule`(`@Serializable`): `endStation`, `startStation`, `startTime`, `stationName`, `updown`, `week` (iOS `ShinbundangScheduleModel` 동일 필드).
  - `ProcessedShinbundangSchedule`: 필터/가공 후 Mapper에 전달할 경량 모델 (`startTime`, `startStation`, `endStation`). 코레일 `ProcessedKorailSchedule` 패턴과 대칭.
- `di/DatabaseModule.kt` — Room `@Provides`(`AppDatabase`, `ShinbundangScheduleDao`). FirebaseModule companion `@Provides` 패턴 차용.

### 삭제
- 없음.

## 핵심 동작 흐름 (iOS shinbundangScheduleLoad 대응)

iOS 로직(`TotalLoadModel.swift` 483~550)을 Android 계층으로 재설계한다.

1. **버전 조회**: Firebase `ShinbundangLineScheduleVersion/version`(Double).
2. **로컬 캐시 조회**: Room에서 `stationName`으로 엔티티 로드.
3. **버전 비교**:
   - `저장 버전(Double) >= Firebase 버전` 이고 로컬 시간표가 비어있지 않으면 → 로컬 데이터 사용 (Firebase 시간표 요청 생략).
   - 그 외 → Firebase에서 시간표 데이터 요청.
4. **저장 (`isDisposable == false`인 경우만)**:
   - 기존 엔티티가 있으면 먼저 삭제 후, 새 데이터 + 새 버전으로 insert.
   - `isDisposable == true`이면 저장하지 않고 데이터만 반환.
5. **필터링**: `updown == station.updnLine` && `week == 요청 요일(평일/주말)` && `!exceptionLastStation.contains(endStation)`.
6. **가공/정렬**: `ProcessedShinbundangSchedule`로 매핑 후 시작 시각 오름차순 정렬 → `NetworkResult.Success` 반환.

> iOS의 `isNow`/`isFirst`/`isWidget`은 홈 화면 위젯/실시간 옵션용. 현재 Android 홈 시간표는 "현재 시각 이후 최근접 1건"을 Mapper에서 선택하므로(코레일/서울과 동일), TotalLoadModel은 필터링된 전체 리스트만 반환하고 최근접 선택은 Mapper가 담당한다.

## 핵심 차이점 (iOS vs 현재 Android)

1. **로컬 캐시 = CoreData → Room**: iOS는 CoreData(`ShinbundangLineScheduleModel`)에 `scheduleData`(Binary, JSON 인코딩) + `scheduleVersion`(String) 보관. Android는 Room 엔티티로 동일 구조(`stationName` PK, `scheduleData` JSON String, `scheduleVersion` String) 재현. **Room은 이번 기능에서 최초 도입**이므로 DB/DAO/Module 신규 설정 필요.
2. **신분당선 Firebase root가 다름**: 시간표는 `SubwayWhenShinbundangScheduleData`(별도 root, `Keys` 배열에서 역 인덱스 찾고 `ScheduleList[index]` 조회), 버전은 `SubwayWhen/ShinbundangLineScheduleVersion/version`. 기존 `SubwayWhen/...` 노드와 경로가 다르므로 LoadModelImpl에서 별도 파싱.
3. **요일 구분**: iOS `calculateDayType`(공휴일 포함)을 현재 Android 홈은 `Calendar.DAY_OF_WEEK`로 평일/토/일 구분 후 `weekDay` 문자열 사용. 신분당선 `week` 필드는 "평일"/"주말" 2종이므로 `weekDay != "weekday"`이면 "주말"로 매핑.
4. **방향 반전 없음**: 신분당선은 9호선 같은 상하행 반전이 없으므로 `station.updnLine`("상행"/"하행")을 `updown` 필드와 그대로 비교.
5. **급행 없음**: 신분당선 모델에 `isFast` 필드 없음 → Mapper에서 `isFast` 미세팅(빈 문자열 유지).

## 기술적 결정사항

- **신분당선 Firebase 통신 위치 = LoadModel**: iOS와 동일하게 버전/시간표 조회는 `LoadModel`에 둔다(`shinbundangScheduleVersionRequest`, `shinbundangScheduleRequest`). `LoadModelImpl`은 현재 Ktor 전용이므로 `FirebaseDatabase`를 생성자에 새로 주입받아 구현. TotalLoadModel이 LoadModel을 통해 호출하고, FirebaseDataSource는 변경 없음.
- **가공 로직 위치 = TotalLoadModel**: 버전 비교·DB 조회/저장/삭제·요일/방향/exception 필터·정렬을 `TotalLoadModelImpl.shinbundangScheduleLoad()`에 둔다. ViewModel/Mapper는 가공 완료된 `List<ProcessedShinbundangSchedule>`만 받는다. (코레일 `korailScheduleLoad` 선례와 동일 계층)
- **반환 타입**: `suspend fun shinbundangScheduleLoad(station, weekDay, isDisposable): NetworkResult<List<ProcessedShinbundangSchedule>>`. 최근접 1건 선택 + HH:mm/N분 포맷은 Mapper(`toScheduleCell`)에서 처리 (서울/코레일과 대칭).
- **JSON 직렬화 위치**: Room 엔티티는 `scheduleData`를 **String(JSON)** 으로 보관한다. 직렬화/역직렬화는 TotalLoadModelImpl에서 kotlinx.serialization으로 수행(엔티티에 TypeConverter를 두지 않음) — Room이 kotlinx 컨버터를 기본 지원하지 않아 Converters 추가 시 보일러플레이트가 늘기 때문. (대안: TypeConverter 도입 — 단일 사용처라 과함으로 기각. 단, 다른 엔티티가 동일 타입을 쓰게 되면 그때 Converters로 승격.)
- **`isDisposable` 전달 경로**: 현재 홈 시간표 탭은 "한 번 보기" 성격이지만, iOS 기본 동작은 저장(=`isDisposable = false`)이다. 캐시 활용을 위해 홈에서는 `isDisposable = false`로 호출(저장 후 다음 조회 시 캐시 사용). ViewModel이 이 값을 명시적으로 전달.
- **DB 에러 처리**: Room 읽기/쓰기/삭제 실패는 try-catch로 감싸 로그 후 빈 목록 또는 Firebase 경로로 graceful fallback (spec "Room DB 에러 → 에러 로그 후 빈 목록 반환").

## 구현 순서

### Phase 1. Room 도입 (빌드 설정 + DB 스캐폴딩)
- `libs.versions.toml`에 Room 버전/라이브러리 추가.
- `app/build.gradle.kts`에 `implementation(room-runtime)` + `ksp(room-compiler)` 추가.
- `ShinbundangScheduleEntity`(`@Entity`, PK=`stationName`), `ShinbundangScheduleDao`(load/insert/delete), `AppDatabase`(`@Database`) 작성.
- `di/DatabaseModule.kt`로 `AppDatabase`/`Dao` 제공(`Room.databaseBuilder`).
- verify: Gradle sync + `:app:kspDebugKotlin` 성공(Room 스키마 생성), 앱 빌드 통과.

### Phase 2. 데이터 모델 (DTO)
- `data/remote/dto/scheduleArrival/shinbundang/ShinbundangScheduleModel.kt`:
  - `ShinbundangSchedule`(`@Serializable`, 6필드) + `ProcessedShinbundangSchedule`(startTime/startStation/endStation).
- verify: 컴파일 성공.

### Phase 3. LoadModel — 버전 + 시간표 조회
- `LoadModel`에 `suspend fun shinbundangScheduleVersionRequest(): Double?`, `suspend fun shinbundangScheduleRequest(stationName: String): List<ShinbundangSchedule>?` 추가.
- `LoadModelImpl` 구현 (iOS `loadModel.shinbundangScheduleVersionRequest/Reqeust` 대응):
  - 생성자에 `FirebaseDatabase` 주입 추가. DI 모듈(`NetworkModule` 또는 별도 모듈)에서 `FirebaseDatabase` 바인딩 추가.
  - 버전: Firebase `SubwayWhen/ShinbundangLineScheduleVersion/version`(Double).
  - 시간표: Firebase `SubwayWhenShinbundangScheduleData/Keys`에서 `stationName` 인덱스 → `ScheduleList[index]` 파싱 → `ShinbundangSchedule` 목록 반환. 실패 시 null.
- verify: 실제 노드 구조/필드명(endStation/startStation/startTime/stationName/updown/week) 매핑 일치 확인.

### Phase 4. TotalLoadModel — 통합 로직
- `TotalLoadModel`에 `suspend fun shinbundangScheduleLoad(station, weekDay, isDisposable): NetworkResult<List<ProcessedShinbundangSchedule>>` 선언.
- `TotalLoadModelImpl` 구현(핵심 동작 흐름 1~6):
  1. `loadModel.shinbundangScheduleVersionRequest()`로 버전 조회.
  2. Room에서 `stationName` 엔티티 로드.
  3. `저장버전 >= Firebase버전` && 로컬 데이터 존재 → 로컬 사용 / 아니면 `loadModel.shinbundangScheduleRequest(stationName)`로 Firebase 시간표 조회.
  4. `isDisposable == false`이면 기존 삭제 후 새 데이터+버전 저장(JSON 직렬화).
  5. `updown`/`week`(요일)/`exceptionLastStation` 필터.
  6. `ProcessedShinbundangSchedule` 매핑 + startTime 오름차순 정렬 → Success.
  - DI: `TotalLoadModelImpl` 생성자에 `ShinbundangScheduleDao` 주입.
- verify: 단위 테스트 — (a) 캐시 최신이면 Firebase 시간표 미호출, (b) 버전 낮으면 Firebase 조회 후 저장, (c) `isDisposable=true`이면 저장 미수행, (d) 방향/요일/exception 필터 동작.

### Phase 5. Mapper / ViewModel 연결
- `HomeCellMapper`에 `List<ProcessedShinbundangSchedule>.toScheduleCell(prev)` 추가(현재 시각 이후 최근접 1건, HH:mm/N분/종착역행, 없으면 운행 종료).
- `HomeViewModel.handleScheduleTap()`에 신분당선 분기 추가: `station.line == "신분당선"` → `totalLoadModel.shinbundangScheduleLoad(station, weekDay, isDisposable = false)` → `toScheduleCell` 매핑. (기존 korail/seoul 분기 유지)
- verify: 신분당선 저장 역에서 시간표 버튼 탭 시 올바른 방향·종착역·시각 표시. 두 번째 탭 시 캐시 사용(Firebase 시간표 요청 생략) 확인.

## 완료 조건
- [ ] 신분당선 시간표를 정상적으로 가져온다 (Spec AC).
- [ ] 로컬 DB 캐시가 있고 버전이 최신이면 Firebase 시간표 요청 없이 반환한다 (Spec AC).
- [ ] Firebase에서 받은 데이터가 `isDisposable == false`일 때 Room DB에 저장된다 (Spec AC).
- [ ] `isDisposable == true`이면 DB 저장 없이 데이터만 반환한다 (Spec AC).
- [ ] 버전이 낮은 기존 데이터는 삭제 후 새 데이터로 교체된다 (Spec AC).
- [ ] Firebase/Room 에러 시 빈 목록(또는 정보없음/운행종료)으로 graceful 처리되고 크래시가 없다 (Spec 오류 처리).
- [ ] HomeViewModel/Mapper가 Firebase/Room/LoadModel에 직접 의존하지 않고 TotalLoadModel만 사용한다 (Spec 제약).
- [ ] Room 도입 빌드 설정(libs.versions.toml, build.gradle.kts, DatabaseModule)이 정상 동작한다.
