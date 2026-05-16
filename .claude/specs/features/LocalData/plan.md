# Plan: LocalData

## 참조 Spec
- @specs/features/LocalData/spec.md

## 참조 Skill
- 없음 (신규 화면 없음, 데이터 레이어 작업)

## 현재 상태 파악

### 신규
- `data/model/SaveStation.kt` — iOS `SaveStation.swift` 대응 데이터 모델
- `data/model/SaveStationGroup.kt` — iOS `SaveStationGroup.swift` 대응 enum (`ONE`, `TWO`)
- `data/local/PreferencesKeys.kt` — DataStore Preferences 키를 한 곳에 모은 객체 (Spec 요구: 키 분리)
- `data/local/StationLocalDataSource.kt` — SaveStation 리스트 read/write
- `data/repository/LocalDataRepository.kt` (interface) — 상위 레이어가 사용할 단일 진입점
- `data/repository/LocalDataRepositoryImpl.kt` — SettingLocalDataSource + StationLocalDataSource 위임
- `core/FixInfo.kt` — Hilt 주입형 싱글톤. 인메모리 `StateFlow<SaveSetting>` / `StateFlow<List<SaveStation>>` 보유. 변경 시 DataStore에 즉시 저장 (iOS `didSet` 대응)
- `data/local/SaveStationSerializer.kt` — `List<SaveStation>` ↔ JSON 직렬화 헬퍼 (kotlinx.serialization)

### 재사용
- `data/local/SettingLocalDataSource.kt` — 기존 구조 유지. 불필요 필드 제거 및 일괄 업데이트 메서드 추가
- `di/AppModule.kt` — DataStore 제공 모듈 그대로 사용
- `di/RepositoryModule.kt` — `LocalDataRepository` 바인딩 추가
- `SubwayWhenApplication.kt` — 앱 시작 시 FixInfo 초기 로딩 트리거 위치
- `TutorialRepositoryImpl.kt` — 내부적으로 `SettingLocalDataSource` 직접 사용 중. 이번 작업과 동작 동일하므로 그대로 유지 (Surgical Changes)

### 수정
- `data/model/SaveSetting.kt`
  - `liveActivity` 제거 (iOS 전용 Live Activity 기능, AOS에 없음)
  - `alertGroupOneId`, `alertGroupTwoId` 제거 (Spec에서 AOS 불필요 명시)
- `data/local/SettingLocalDataSource.kt`
  - 위 3개 필드 키 제거
  - 전체 `SaveSetting` 단위로 저장하는 `updateSaveSetting(setting)` 메서드 추가 (iOS `didSet` 대응을 FixInfo에서 호출)
  - 키 상수를 `PreferencesKeys`로 이동 (Spec 요구: 키 분리)

### 삭제
- 없음 (기존 코드 호환 유지)

## 기술적 결정사항

- **DataStore Preferences 채택**: iOS UserDefaults 대응. 이미 `libs.versions.toml` 및 `AppModule`에 셋업되어 있음. SharedPreferences 대비 코루틴/Flow 친화적, 트랜잭션 안전.
- **SaveSetting 저장 방식 = Preferences 키 분리 저장**: iOS는 struct 통째로 PropertyListEncoder 인코딩이지만, AOS Preferences 컨벤션은 key-value 분리. 이미 기존 `SettingLocalDataSource`가 이 방식이므로 일관성 유지.
- **SaveStation 리스트 저장 방식 = JSON 문자열 1개 키**: 리스트는 Preferences에 직접 저장 불가. kotlinx.serialization으로 `List<SaveStation>`을 JSON 문자열화하여 단일 키에 저장. 이미 프로젝트에 kotlinx.serialization 의존성 존재.
- **FixInfo = Hilt `@Singleton` + `StateFlow`**: iOS는 `static` 전역 접근이지만, Android에서는 테스트 가능성·생명주기 안전성을 위해 Hilt 싱글톤. 외부에는 `StateFlow<SaveSetting>` / `StateFlow<List<SaveStation>>`로 노출하여 단방향 흐름 유지.
- **iOS `didSet` 대응**: FixInfo 내부에서 값 변경 메서드(`updateSaveSetting(...)`, `updateSaveStations(...)`)를 두고, 호출 시점에 (1) 메모리 StateFlow 갱신 (2) 코루틴으로 DataStore에 영구 저장을 동시에 수행. 호출부는 변경만 하면 자동 영속화됨.
- **앱 시작 시 초기 로딩 위치**: `SubwayWhenApplication.onCreate()`에서 FixInfo의 `initialize()`를 호출하여 DataStore → StateFlow로 1회 적재. 이후엔 메모리 캐시로 즉시 응답, 변경은 FixInfo 메서드를 통해 영속화.
- **alertGroupOneId/TwoId 제외**: Spec 명시. iOS에서는 NotificationManager 식별자였으나, AOS는 WorkManager/AlarmManager로 알림 구현 시 별도 ID 체계 사용 예정이므로 SaveSetting에 둘 필요 없음.
- **liveActivity 제외**: iOS Live Activity 전용 기능. AOS는 대응 기능이 없으므로 제거 (Simplicity First).
- **키 명명 분리**: Spec 요구. `PreferencesKeys` 단일 파일에 `object PreferencesKeys` 형태로 모아 관리. 이름 충돌·오타 방지.
- **kotlinx.serialization 채택**: 이미 의존성 있음. Moshi/Gson 추가 도입 회피.

## 구현 순서

### Phase 1. 데이터 모델
- `data/model/SaveStationGroup.kt` — `enum class SaveStationGroup { ONE, TWO }`. `@Serializable` 부착.
- `data/model/SaveStation.kt` — `@Serializable data class` 로 iOS 9개 필드 1:1 매핑.
  - 필드: `id`, `stationName`, `stationCode`, `updnLine`, `line`, `lineCode`, `group: SaveStationGroup`, `exceptionLastStation`, `korailCode`
  - `subwayLineData`, `widgetUseText` 같은 iOS 계산 프로퍼티는 미구현 (각 기능 화면에서 필요 시 별도 작업으로 분리)
- `data/model/SaveSetting.kt` — 기존 파일 수정. `liveActivity`, `alertGroupOneId`, `alertGroupTwoId` 3개 필드 삭제. 초기값은 iOS와 동일하게 유지.
- verify: 컴파일 성공.

### Phase 2. 로컬 영속 계층
- `data/local/PreferencesKeys.kt` — `object PreferencesKeys` 신설. SaveSetting 10개 키 + SaveStation JSON 1개 키.
- `data/local/SettingLocalDataSource.kt` — 수정.
  - 키 정의를 `PreferencesKeys`로 이동 (companion object 비움).
  - 삭제 필드 3개에 해당하는 키 제거 및 매핑 코드 제거.
  - `suspend fun updateSaveSetting(setting: SaveSetting)` 추가: 모든 필드를 한 번에 `edit { ... }` 트랜잭션으로 기록.
  - 기존 `getSaveSetting()` 시그니처 유지 (TutorialRepository가 사용 중).
  - 기존 `updateTutorialSeen(value)` 유지 (호환).
- `data/local/StationLocalDataSource.kt` — 신설.
  - `getSaveStations(): Flow<List<SaveStation>>` — JSON 디코딩, 실패 시 빈 리스트.
  - `suspend fun updateSaveStations(list: List<SaveStation>)` — JSON 인코딩 후 저장.
- verify: `SettingLocalDataSource` / `StationLocalDataSource` 단위 테스트 (Kotest + Turbine, 인메모리 DataStore) 통과.

### Phase 3. 리포지토리
- `data/repository/LocalDataRepository.kt` (interface)
  - `val saveSetting: StateFlow<SaveSetting>`
  - `val saveStations: StateFlow<List<SaveStation>>`
  - `suspend fun updateSaveSetting(setting: SaveSetting)`
  - `suspend fun updateSaveStations(stations: List<SaveStation>)`
- `data/repository/LocalDataRepositoryImpl.kt` — `FixInfo`에 위임 (생성자 주입).
- `di/RepositoryModule.kt` — `LocalDataRepository` 바인딩 추가.
- verify: 컴파일 + Hilt 그래프 정상.

### Phase 4. FixInfo (전역 캐시 + 자동 저장)
- `core/FixInfo.kt` — `@Singleton class FixInfo @Inject constructor(...)`
  - 의존: `SettingLocalDataSource`, `StationLocalDataSource`, `CoroutineScope` (앱 전역 스코프)
  - 내부 `MutableStateFlow<SaveSetting>`, `MutableStateFlow<List<SaveStation>>` 보유, 외부엔 `StateFlow`로 노출.
  - `suspend fun initialize()` — DataStore에서 1회 read하여 StateFlow에 적재.
  - `suspend fun updateSaveSetting(new: SaveSetting)` — StateFlow 갱신 + DataStore 저장 (iOS `didSet` 대응).
  - `suspend fun updateSaveStations(list: List<SaveStation>)` — 동일 패턴.
- `SubwayWhenApplication.kt` — `onCreate()`에서 Hilt entry point로 `FixInfo` 획득 후 `applicationScope.launch { fixInfo.initialize() }`.
  - 앱 전역 `CoroutineScope`(SupervisorJob + Dispatchers.IO)를 Hilt에서 제공하도록 `AppModule`에 추가.
- verify: 앱 실행 → 설정 변경 → 앱 재실행 시 값 유지 (수동 확인 + 통합 테스트).

### Phase 5. 테스트
- `SettingLocalDataSourceTest` — read/write 라운드트립, 기본값 fallback.
- `StationLocalDataSourceTest` — JSON 라운드트립, 깨진 JSON 시 빈 리스트.
- `FixInfoTest` — `initialize()` 후 StateFlow에 값 적재, `updateXxx` 호출 시 StateFlow 갱신 + DataSource 호출(MockK).
- verify: 모든 테스트 통과.

## 완료 조건
- [ ] Spec Acceptance Criteria: 앱 재실행 후 SaveSetting / SaveStation 값이 유지된다.
- [ ] Spec Acceptance Criteria: JSON 인코딩/디코딩 실패가 없다 (`StationLocalDataSourceTest`로 검증).
- [ ] `liveActivity`, `alertGroupOneId`, `alertGroupTwoId`가 SaveSetting에서 제거되었다.
- [ ] DataStore Preferences 키가 `PreferencesKeys` 한 파일에 분리되어 있다.
- [ ] FixInfo가 앱 시작 시 자동 초기화되고, 값 변경 시 자동으로 영속화된다.
- [ ] 기존 `TutorialRepositoryImpl` 동작이 깨지지 않는다.
- [ ] 모든 단위 테스트 통과.
