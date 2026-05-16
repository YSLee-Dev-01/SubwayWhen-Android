# Tasks: LocalData

## 참조
- spec: `.claude/specs/features/LocalData/spec.md`
- plan: `.claude/specs/features/LocalData/plan.md`

## Task 목록

### Phase 1. 데이터 모델

#### [x] Task 1 — `SaveStationGroup.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/model/SaveStationGroup.kt`
- `enum class SaveStationGroup { ONE, TWO }` 정의
- `@Serializable` 어노테이션 부착 (kotlinx.serialization)

---

#### [x] Task 2 — `SaveStation.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/model/SaveStation.kt`
- `@Serializable data class SaveStation` 정의
- 필드 9개: `id: String`, `stationName: String`, `stationCode: String`, `updnLine: String`, `line: String`, `lineCode: String`, `group: SaveStationGroup`, `exceptionLastStation: String`, `korailCode: String`
- iOS `subwayLineData`, `widgetUseText` 계산 프로퍼티는 미구현 (별도 작업으로 분리)
- 각 필드 초기값은 iOS `SaveStation.swift` 참조하여 설정

---

#### [x] Task 3 — `SaveSetting.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/model/SaveSetting.kt`
- `liveActivity: Boolean` 필드 삭제 (iOS Live Activity 전용, AOS 미지원)
- `alertGroupOneId: String` 필드 삭제 (Spec 명시: AOS 불필요)
- `alertGroupTwoId: String` 필드 삭제 (Spec 명시: AOS 불필요)
- 나머지 10개 필드 및 초기값은 iOS `SaveSetting.swift` 참조하여 유지

---

### Phase 2. 로컬 영속 계층

#### [x] Task 4 — `PreferencesKeys.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/PreferencesKeys.kt`
- `object PreferencesKeys` 로 모든 DataStore 키를 한 곳에 정의
- `SettingLocalDataSource` companion object에 있던 SaveSetting 관련 키 10개 이동
  - `MAIN_CONGESTION_LABEL`, `MAIN_GROUP_ONE_TIME`, `MAIN_GROUP_TWO_TIME`
  - `DETAIL_AUTO_RELOAD`, `DETAIL_SCHEDULE_AUTO_TIME`, `SEARCH_OVERLAP_ALERT`
  - `TUTORIAL_SUCCESS`, `DETAIL_VC_TRAIN_ICON`, `IS_WEEKEND_NOTIFICATION_ENABLED`
  - `MAIN_CONGESTION_BASE_STATION`
- SaveStation 리스트 JSON 저장용 키 1개 추가: `SAVE_STATIONS_JSON`
- 삭제 대상 키 3개는 이동하지 않음: `LIVE_ACTIVITY`, `ALERT_GROUP_ONE_ID`, `ALERT_GROUP_TWO_ID`

---

#### [x] Task 5 — `SettingLocalDataSource.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/SettingLocalDataSource.kt`
- companion object 내 모든 키 상수를 `PreferencesKeys` 참조로 교체 후 companion object 비움
- `LIVE_ACTIVITY`, `ALERT_GROUP_ONE_ID`, `ALERT_GROUP_TWO_ID` 키 및 매핑 코드 제거
- `getSaveSetting()` — 삭제된 3개 필드 매핑 코드 제거, 시그니처 유지 (TutorialRepositoryImpl 호환)
- `suspend fun updateSaveSetting(setting: SaveSetting)` 추가: 전체 `SaveSetting` 10개 필드를 단일 `edit { ... }` 트랜잭션으로 DataStore에 저장 (iOS `didSet` 대응, FixInfo에서 호출)
- 기존 `updateTutorialSeen(value: Boolean)` 유지 (TutorialRepositoryImpl 호환)

---

#### [x] Task 6 — `StationLocalDataSource.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/StationLocalDataSource.kt`
- `@Singleton class StationLocalDataSource @Inject constructor(dataStore: DataStore<Preferences>)` 정의
- `fun getSaveStations(): Flow<List<SaveStation>>` — `PreferencesKeys.SAVE_STATIONS_JSON` 키로 JSON 문자열 읽기 → kotlinx.serialization으로 `List<SaveStation>` 디코딩, 키 없거나 디코딩 실패 시 빈 리스트 반환
- `suspend fun updateSaveStations(list: List<SaveStation>)` — `List<SaveStation>`을 JSON 인코딩 후 `PreferencesKeys.SAVE_STATIONS_JSON` 키에 저장

---

### Phase 3. 리포지토리

#### [x] Task 7 — `LocalDataRepository.kt` (신규, interface)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/repository/LocalDataRepository.kt`
- `interface LocalDataRepository` 정의
- `val saveSetting: StateFlow<SaveSetting>`
- `val saveStations: StateFlow<List<SaveStation>>`
- `suspend fun updateSaveSetting(setting: SaveSetting)`
- `suspend fun updateSaveStations(stations: List<SaveStation>)`

---

#### [x] Task 8 — `LocalDataRepositoryImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/repository/LocalDataRepositoryImpl.kt`
- `@Singleton class LocalDataRepositoryImpl @Inject constructor(private val fixInfo: FixInfo)` 정의
- `LocalDataRepository` 구현: 모든 멤버를 `FixInfo`에 위임

---

#### [x] Task 9 — `RepositoryModule.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/RepositoryModule.kt`
- `LocalDataRepository` → `LocalDataRepositoryImpl` 바인딩 `@Binds` 메서드 추가
- 기존 `TutorialRepository` 바인딩 유지

---

### Phase 4. FixInfo 및 앱 초기화

#### [x] Task 10 — `FixInfo.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/core/FixInfo.kt`
- `@Singleton class FixInfo @Inject constructor(settingDataSource: SettingLocalDataSource, stationDataSource: StationLocalDataSource, applicationScope: CoroutineScope)` 정의
- 내부 `MutableStateFlow<SaveSetting>` 보유, 외부엔 `val saveSetting: StateFlow<SaveSetting>` 로 노출
- 내부 `MutableStateFlow<List<SaveStation>>` 보유, 외부엔 `val saveStations: StateFlow<List<SaveStation>>` 로 노출
- `suspend fun initialize()` — DataStore에서 1회 read하여 각 StateFlow에 초기값 적재
- `suspend fun updateSaveSetting(new: SaveSetting)` — StateFlow 갱신 + `settingDataSource.updateSaveSetting(new)` 호출 (iOS `didSet` 대응)
- `suspend fun updateSaveStations(list: List<SaveStation>)` — StateFlow 갱신 + `stationDataSource.updateSaveStations(list)` 호출

---

#### [x] Task 11 — `AppModule.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/AppModule.kt`
- 앱 전역 `CoroutineScope`(`SupervisorJob + Dispatchers.IO`) `@Provides` 추가 — `@ApplicationScope` 한정자(Qualifier) 신설하여 구분
- 기존 DataStore `@Provides` 유지

---

#### [x] Task 12 — `SubwayWhenApplication.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/SubwayWhenApplication.kt`
- `@HiltAndroidApp` 유지
- `onCreate()` 오버라이드 추가
- Hilt EntryPoint를 통해 `FixInfo` 획득
- `applicationScope.launch { fixInfo.initialize() }` 호출하여 앱 시작 시 DataStore → StateFlow 1회 적재

---

### Phase 5. 테스트

#### [x] Task 13 — `SettingLocalDataSourceTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/data/local/SettingLocalDataSourceTest.kt`
- 인메모리 DataStore 사용 (테스트 전용 `PreferenceDataStoreFactory`)
- `getSaveSetting()` — 저장된 값 없을 때 기본값 반환 검증
- `updateSaveSetting()` → `getSaveSetting()` 라운드트립 — 저장한 `SaveSetting`이 그대로 읽히는지 검증
- `updateTutorialSeen(true)` → `getSaveSetting().tutorialSuccess == true` 검증 (TutorialRepository 호환 확인)
- Kotest + Turbine 사용

---

#### [x] Task 14 — `StationLocalDataSourceTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/data/local/StationLocalDataSourceTest.kt`
- 인메모리 DataStore 사용
- `getSaveStations()` — 저장된 값 없을 때 빈 리스트 반환 검증
- `updateSaveStations()` → `getSaveStations()` JSON 라운드트립 — 저장한 리스트가 동일하게 읽히는지 검증 (모든 9개 필드 포함)
- 깨진 JSON이 DataStore에 있을 때 빈 리스트로 fallback 검증
- Kotest + Turbine 사용

---

#### [x] Task 15 — `FixInfoTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/core/FixInfoTest.kt`
- MockK로 `SettingLocalDataSource`, `StationLocalDataSource` 모킹
- `initialize()` 호출 후 `saveSetting` StateFlow에 DataSource 반환값이 적재되는지 검증
- `initialize()` 호출 후 `saveStations` StateFlow에 DataSource 반환값이 적재되는지 검증
- `updateSaveSetting(new)` 호출 시 StateFlow 갱신 + `settingDataSource.updateSaveSetting(new)` 호출 검증
- `updateSaveStations(list)` 호출 시 StateFlow 갱신 + `stationDataSource.updateSaveStations(list)` 호출 검증
- Kotest + MockK 사용

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공
- [ ] 테스트 통과 (Task 13 · 14 · 15)
- [ ] `liveActivity`, `alertGroupOneId`, `alertGroupTwoId` 가 `SaveSetting` 및 `SettingLocalDataSource` 에서 완전히 제거되었는지 확인
- [ ] DataStore 키가 `PreferencesKeys` 한 파일에만 정의되어 있는지 확인
- [ ] 기존 `TutorialRepositoryImpl` 빌드 및 테스트 통과 (회귀 없음)

### 기능 (AC)
- [ ] 앱을 재실행해도 저장한 `SaveSetting` / `SaveStation` 값이 유지된다
- [ ] JSON 인코딩 / 디코딩 과정이 실패하지 않는다 (`StationLocalDataSourceTest` 로 검증)
