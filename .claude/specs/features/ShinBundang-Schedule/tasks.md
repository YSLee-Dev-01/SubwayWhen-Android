# Tasks: ShinBundang-Schedule

## 참조
- spec: `.claude/specs/features/ShinBundang-Schedule/spec.md`
- plan: `.claude/specs/features/ShinBundang-Schedule/plan.md`

## Task 목록

### Phase 1. Room 도입 (빌드 설정 + DB 스캐폴딩)

#### [x] Task 1 — `libs.versions.toml`
**파일**: `gradle/libs.versions.toml`
- Room 버전(`androidx-room = "2.x.x"`) 추가
- `androidx-room-runtime`, `androidx-room-compiler` 라이브러리 항목 추가

---

#### [x] Task 2 — `app/build.gradle.kts`
**파일**: `app/build.gradle.kts`
- `implementation(libs.androidx.room.runtime)` 의존성 추가
- `ksp(libs.androidx.room.compiler)` 의존성 추가 (KSP 플러그인은 기존 설정 유지)

---

#### [x] Task 3 — `ShinbundangScheduleEntity.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/room/ShinbundangScheduleEntity.kt`
- `@Entity` data class `ShinbundangScheduleEntity` 정의
- 필드: `stationName`(PK, String), `scheduleData`(String, JSON 직렬화된 시간표), `scheduleVersion`(String)

---

#### [x] Task 4 — `ShinbundangScheduleDao.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/room/ShinbundangScheduleDao.kt`
- `@Dao` interface `ShinbundangScheduleDao` 정의
- `suspend fun load(stationName: String): ShinbundangScheduleEntity?` — PK로 단건 조회
- `suspend fun insert(entity: ShinbundangScheduleEntity)` — `@Insert(onConflict = REPLACE)` 삽입
- `suspend fun delete(stationName: String)` — PK로 단건 삭제

---

#### [x] Task 5 — `AppDatabase.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/room/AppDatabase.kt`
- `@Database(entities = [ShinbundangScheduleEntity::class], version = 1)` `RoomDatabase` 서브클래스 정의
- `abstract fun shinbundangScheduleDao(): ShinbundangScheduleDao` 추상 함수 선언

---

#### [x] Task 6 — `DatabaseModule.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/DatabaseModule.kt`
- `@Module @InstallIn(SingletonComponent::class)` Hilt 모듈 정의
- `@Provides @Singleton fun provideAppDatabase(context: Application): AppDatabase` — `Room.databaseBuilder` 로 인스턴스 제공
- `@Provides @Singleton fun provideShinbundangScheduleDao(db: AppDatabase): ShinbundangScheduleDao` — DAO 제공

---

### Phase 2. 데이터 모델 (DTO)

#### [x] Task 7 — `ShinbundangScheduleModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/dto/scheduleArrival/shinbundang/ShinbundangScheduleModel.kt`
- `@Serializable` data class `ShinbundangSchedule` 정의: `endStation`, `startStation`, `startTime`, `stationName`, `updown`, `week` 6개 필드
- data class `ProcessedShinbundangSchedule` 정의 (직렬화 불필요): `startTime`, `startStation`, `endStation` 3개 필드 — 코레일 `ProcessedKorailSchedule` 패턴과 대칭

---

### Phase 3. LoadModel — 버전 + 시간표 조회

#### [x] Task 8 — `LoadModel.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModel.kt`
- `suspend fun shinbundangScheduleVersionRequest(): Double?` 함수 선언 추가
- `suspend fun shinbundangScheduleRequest(stationName: String): List<ShinbundangSchedule>?` 함수 선언 추가

---

#### [x] Task 9 — `LoadModelImpl.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModelImpl.kt`
- 생성자에 `FirebaseDatabase` 주입 추가
- `shinbundangScheduleVersionRequest()` 구현: Firebase `SubwayWhen/ShinbundangLineScheduleVersion/version` 경로에서 Double 값 조회, 실패 시 null 반환
- `shinbundangScheduleRequest(stationName)` 구현: Firebase `SubwayWhenShinbundangScheduleData/Keys` 배열에서 `stationName` 인덱스 탐색 → `ScheduleList[index]` 파싱 → `List<ShinbundangSchedule>` 반환, 실패 시 null 반환

---

#### [x] Task 10 — DI 모듈 수정 (LoadModelImpl `FirebaseDatabase` 바인딩)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/NetworkModule.kt` (또는 관련 DI 모듈)
- `LoadModelImpl` 생성자에 추가된 `FirebaseDatabase` 파라미터에 대해 `@Provides` 바인딩 추가 (이미 제공 중이면 기존 바인딩 재사용)

---

### Phase 4. TotalLoadModel — 통합 로직

#### [x] Task 11 — `TotalLoadModel.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModel.kt`
- `suspend fun shinbundangScheduleLoad(station: SaveStation, weekDay: String, isDisposable: Boolean): NetworkResult<List<ProcessedShinbundangSchedule>>` 함수 선언 추가

---

#### [x] Task 12 — `TotalLoadModelImpl.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModelImpl.kt`
- 생성자에 `ShinbundangScheduleDao` 주입 추가
- `shinbundangScheduleLoad()` 구현 (핵심 동작 흐름 순서):
  1. `loadModel.shinbundangScheduleVersionRequest()`로 Firebase 버전 조회
  2. Room에서 `station.stationName`으로 엔티티 로드
  3. 저장 버전(Double) >= Firebase 버전이고 로컬 데이터가 비어있지 않으면 로컬 데이터 사용 (Firebase 시간표 요청 생략), 그 외에는 `loadModel.shinbundangScheduleRequest(stationName)`로 Firebase 시간표 조회
  4. `isDisposable == false`이면: 기존 엔티티 존재 시 먼저 delete 후, 새 데이터(JSON 직렬화)와 새 버전으로 insert
  5. `updown == station.updnLine` 및 `week` 요일 필터 적용 (`weekDay != "weekday"`이면 "주말", 아니면 "평일" 비교), `station.exceptionLastStation`에 `endStation`이 포함되면 제외
  6. `ProcessedShinbundangSchedule`로 매핑 후 `startTime` 오름차순 정렬 → `NetworkResult.Success` 반환
  - Firebase/Room 에러는 try-catch로 감싸 로그 후 `NetworkResult.Error` 또는 빈 목록으로 graceful 처리

---

### Phase 5. Mapper / ViewModel 연결

#### [x] Task 13 — `HomeCellMapper.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/mapper/HomeCellMapper.kt`
- `List<ProcessedShinbundangSchedule>.toScheduleCell(prev: ScheduleCell): ScheduleCell` 확장 함수 추가
- 현재 시각 이후 가장 가까운 1건 선택
- `stateMSG` → `HH:mm` 형식 시각, `subPrevious` → `N분 후`, `lastStation` → 종착역행 세팅
- 신분당선은 `isFast` 없으므로 빈 문자열 유지
- 매칭 열차 없으면 기존 `운행 종료` 동작 유지 — 코레일/서울 매퍼와 대칭

---

#### [x] Task 14 — `HomeViewModel.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeViewModel.kt`
- `handleScheduleTap()` 내에 신분당선 분기 추가: `station.line == "신분당선"` 조건 처리
- `totalLoadModel.shinbundangScheduleLoad(station, weekDay, isDisposable = false)` 호출
- 결과를 `toScheduleCell()` 매퍼로 변환하여 UiState 갱신
- 기존 korail/서울 분기 구조 변경 없음

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공 (Gradle sync + `:app:kspDebugKotlin` 포함)
- [ ] 캐시 최신 시 Firebase 시간표 미호출 단위 테스트 통과
- [ ] 버전 낮을 때 Firebase 조회 후 Room 저장 단위 테스트 통과
- [ ] `isDisposable = true` 시 저장 미수행 단위 테스트 통과
- [ ] 방향/요일/exceptionLastStation 필터 단위 테스트 통과
- [ ] HomeViewModel/Mapper가 LoadModel·Firebase·Room에 직접 의존하지 않음 (TotalLoadModel만 사용)

### 기능 (AC)
- [ ] 신분당선 시간표를 정상적으로 가져올 수 있다
- [ ] 로컬 DB에 캐시된 데이터가 있고 버전이 최신이면 Firebase 요청 없이 반환한다
- [ ] Firebase에서 받은 데이터는 `isDisposable = false`일 때 Room DB에 저장된다
- [ ] `isDisposable = true`이면 DB 저장 없이 데이터만 반환한다
- [ ] 버전이 낮은 기존 데이터는 삭제 후 새 데이터로 교체된다
- [ ] Firebase/Room 에러 시 크래시 없이 graceful 처리된다
