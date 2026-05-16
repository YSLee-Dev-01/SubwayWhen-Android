# Tasks: Network

## 참조
- spec: `.claude/specs/features/Network/spec.md`
- plan: `.claude/specs/features/Network/plan.md`

## Task 목록

### Phase 1. 의존성 셋업

#### [x] Task 1 — `libs.versions.toml` (수정)
**파일**: `gradle/libs.versions.toml`
- Ktor 버전 추가 (`ktor = "..."`)
- kotlinx-serialization 버전 추가 (`kotlinx-serialization = "..."`)
- Ktor 라이브러리 항목 추가: `ktor-core`, `ktor-okhttp`, `ktor-content-negotiation`, `ktor-serialization-json`, `ktor-logging`
- kotlinx-serialization 플러그인·라이브러리 항목 추가

---

#### [x] Task 2 — `build.gradle.kts` (수정)
**파일**: `app/build.gradle.kts`
- `kotlinx-serialization` 플러그인 적용
- Ktor 의존성 추가 (core / okhttp / content-negotiation / serialization-json / logging)
- kotlinx-serialization 라이브러리 의존성 추가
- 검증: `./gradlew :app:assembleDebug` 빌드 성공

---

### Phase 2. 네트워크 코어 (NetworkManager)

#### [x] Task 3 — `NetworkResult.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/network/NetworkResult.kt`
- `sealed interface NetworkResult<out T>` 정의
- `Success(val data: T)` 케이스
- `Failure(val error: NetworkError)` 케이스

---

#### [x] Task 4 — `NetworkError.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/network/NetworkError.kt`
- `enum class NetworkError` 정의
- iOS `URLError` 케이스와 1:1 대응: `BadUrl`, `BadServerResponse`, `CannotParseResponse`, `NotConnectedToInternet`

---

#### [x] Task 5 — `TokenKey.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/network/TokenKey.kt`
- `enum class TokenKey` 정의
- iOS plist 키와 1:1 대응: `LIVE`, `SEOUL`, `KORAIL`, `KAKAO`, `REALTIME`

---

#### [x] Task 6 — `TokenProvider.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/network/TokenProvider.kt`
- `interface TokenProvider` 정의 — `fun token(key: TokenKey): String`
- `class DefaultTokenProvider : TokenProvider` stub 구현체 — 빈 문자열 반환, 외부 작업 후 채울 자리로 주석 표시
- 실제 토큰 값은 채우지 않음 (`.gitignore` 처리 후 외부에서 작업)

---

#### [x] Task 7 — `NetworkManager.kt` + `NetworkManagerImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/network/NetworkManager.kt`
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/network/NetworkManagerImpl.kt`
- `interface NetworkManager` 정의
  - `suspend fun <T> requestData(url: String): NetworkResult<T>` (오버로드 1 — 헤더·쿼리 없음)
  - `suspend fun <T> requestData(url: String, headers: Map<String, String>, query: Map<String, String>): NetworkResult<T>` (오버로드 2)
  - iOS 두 오버로드를 그대로 대응, `inline` + `reified` 타입 파라미터로 `Decodable.Type` 인자 불필요화
- `class NetworkManagerImpl(private val client: HttpClient) : NetworkManager` 구현체
  - Ktor `HttpClient(OkHttp)` + `ContentNegotiation(Json)` + `HttpTimeout` (10초, iOS와 동일)
  - 상태코드 매핑: `200..299 → Success`, `300..399 → BadServerResponse`, 그 외 → `BadUrl`, 파싱 실패 예외 → `CannotParseResponse`, 네트워크 끊김 예외 → `NotConnectedToInternet`
  - 테스트 시 `HttpClient(MockEngine { ... })`을 생성자에 직접 주입 (별도 Provider 없음)

---

#### [x] Task 8 — `NetworkModule.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/NetworkModule.kt`
- `@Module @InstallIn(SingletonComponent::class)` Hilt 모듈
- `@Provides @Singleton HttpClient` — `HttpClient(OkHttp)` 인스턴스 제공
- `@Binds @Singleton NetworkManager` — `NetworkManagerImpl` 바인딩
- `@Binds @Singleton TokenProvider` — `DefaultTokenProvider` 바인딩

---

#### [x] Task 9 — `NetworkManagerTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/data/network/NetworkManagerTest.kt`
- Ktor `MockEngine`을 사용하여 `NetworkManagerImpl` 단위 테스트
- 시나리오 5개 검증:
  - 200 응답 → `NetworkResult.Success` 반환 (Acceptance Criteria 직접 검증)
  - 300대 응답 → `NetworkResult.Failure(NetworkError.BadServerResponse)`
  - 500 응답 → `NetworkResult.Failure(NetworkError.BadUrl)`
  - 파싱 실패 (잘못된 JSON) → `NetworkResult.Failure(NetworkError.CannotParseResponse)`
  - 네트워크 예외 발생 → `NetworkResult.Failure(NetworkError.NotConnectedToInternet)`
- Kotest `shouldBe` / `shouldBeInstanceOf` 사용

---

### Phase 3. LoadModel

#### [x] Task 10 — DTO 파일들 (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/dto/` 하위

- `liveArrival/LiveStationModel.kt` — `LiveStationModel`, `RealtimeStationArrival`
- `scheduleArrival/seoul/ScheduleStationModel.kt` — `ScheduleStationModel`, `ScheduleStationArrival`, `SearchSTNTimeTableByFRCodeService`
- `scheduleArrival/korail/KorailScheduleModel.kt` — `KorailHeader`, `KorailScdule`
- `stationSearch/SearchStationModel.kt` — `SearchStaion`, `SearchInfoBySubwayNameService`
- `vicinityStation/VicinityStationsData.kt` — `VicinityStationsData`, `VicinityDocumentData`
- `subwayNotice/SubwayNoticeResponse.kt` — `SubwayNoticeResponse`, `SubwayNotice`
- `realtimePosition/RealtimeTrainPositionResponse.kt` — `RealtimeTrainPositionResponse`, `RealtimeTrainPosition`

모든 클래스에 `@Serializable` 적용, JSON 키는 iOS `Codable` 키와 동일하게 `@SerialName`으로 매핑

---

#### [x] Task 11 — `LoadModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModel.kt`
- `interface LoadModel` 정의
- HTTP 엔드포인트 7개 메서드 선언 (Firebase 의존 메서드 제외):
  - `suspend fun stationArrivalRequest(stationName: String): NetworkResult<LiveStationModel>`
  - `suspend fun seoulStationScheduleLoad(stationCode: String, weekDay: String, upDown: String, stationLine: String): NetworkResult<ScheduleStationModel>`
  - `suspend fun korailScheduleLoad(stationCode: String, weekDay: String, upDown: String): NetworkResult<KorailHeader>`
  - `suspend fun stationSearch(stationName: String): NetworkResult<SearchStaion>`
  - `suspend fun vicinityStationsLoad(x: String, y: String): NetworkResult<VicinityStationsData>`
  - `suspend fun subwayNoticeRequest(): NetworkResult<SubwayNoticeResponse>`
  - `suspend fun realtimePositionRequest(subwayLine: String): NetworkResult<RealtimeTrainPositionResponse>`

---

#### [x] Task 12 — `LoadModelImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModelImpl.kt`
- `class LoadModelImpl(private val networkManager: NetworkManager, private val tokenProvider: TokenProvider) : LoadModel` 구현체
- 각 메서드별 URL 조립 (iOS와 동일한 엔드포인트):
  - 실시간 도착: `http://swopenapi.seoul.go.kr/...`
  - 서울 시간표: `http://openapi.seoul.go.kr:8088/...`
  - 코레일 시간표: `https://openapi.kric.go.kr/...`
  - 역 검색: `http://openapi.seoul.go.kr:8088/...`
  - 주변 역 검색: `https://dapi.kakao.com/...`
  - 지하철 공지: `http://openapi.seoul.go.kr:8088/...`
  - 실시간 열차 위치: `http://swopenapi.seoul.go.kr/...`
- `private fun arrivalStationNameCheck(stationName: String): String` 이식
  - iOS `arrivalStationNameChack`의 24개 케이스를 `Map<String, String>` + `getOrDefault`로 구현
  - 외부에 노출하지 않음
- 9호선 분기, DayType, 상하행 분기 로직 iOS와 동일하게 이식
- Ktor `url { ... }` DSL 사용 (수동 percent-encoding 없음)

---

#### [x] Task 13 — `NetworkModule.kt` 수정 (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/NetworkModule.kt`
- `@Binds @Singleton LoadModel` — `LoadModelImpl` 바인딩 추가

---

#### [x] Task 14 — `LoadModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/data/remote/loadmodel/LoadModelTest.kt`
- MockK로 가짜 `NetworkManager` 주입하여 `LoadModelImpl` 단위 테스트
- 검증 항목:
  - 각 메서드(7개)별 URL 조립이 의도대로 되는지 호출 인자 캡처로 확인 (최소 1개 메서드씩)
  - 토큰 키(`TokenKey`)가 올바른 키로 조회되는지 검증
  - `arrivalStationNameCheck` — 부역명 치환 케이스 1개 이상 검증 (예: "서울역" → "서울")
  - 9호선 분기 로직 검증 (일반 / 9호선 케이스)
- Kotest DSL 사용

---

### Phase 4. TotalLoadModel (인터페이스만)

#### [x] Task 15 — `TotalLoadModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/remote/totalload/TotalLoadModel.kt`
- `interface TotalLoadModel` 정의 (메서드 없는 빈 인터페이스)
- 레이어 존재 선언 용도 — 구현체는 메인/상세 spec 확정 후 생성 예정
- 주석으로 후속 spec 작업 예정임을 표기

---

## 체크리스트

### 품질 (DoD)
- [ ] `./gradlew :app:assembleDebug` 빌드 성공
- [ ] `./gradlew :app:test` 테스트 통과
- [ ] 모든 인터페이스에 `Protocol` suffix 없음 (Android 관용: 구현체에 `Impl` suffix)
- [ ] 버전 하드코딩 없음 — 모든 의존성은 `libs.versions.toml`에서 관리
- [ ] `GlobalScope` 사용 없음 — `suspend fun` + 호출부 `viewModelScope.launch` 구조
- [ ] `DefaultTokenProvider` 본문 비어있음 — 실제 토큰 값 커밋 없음

### 기능 (AC)
- [ ] 정상 응답(200) 시 `NetworkResult.Success` 반환 — `NetworkManagerTest` MockEngine 200 케이스 통과로 검증
- [ ] `NetworkManager` / `LoadModel` / `TotalLoadModel` 인터페이스 분리 — 프로토콜 기반 설계 충족
- [ ] `LoadModelImpl`이 내부에서 `NetworkManager` 사용 — spec 기반 구조 충족
- [ ] 토큰 주입이 `TokenProvider.token(key)` 단일 함수로 격리됨
