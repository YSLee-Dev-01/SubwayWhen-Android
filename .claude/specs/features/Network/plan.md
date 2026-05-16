# Plan: Network

## 참조 Spec
- @specs/features/Network/spec.md

## 참조 iOS 원본
- `SubwayWhenNetworking/DataLoad/NetworkManager.swift`
- `SubwayWhenNetworking/DataLoad/LoadModel/LoadModel.swift`
- `SubwayWhenNetworking/DataLoad/TotalLoadModel.swift`
- `SubwayWhenNetworking/DataLoad/Entity/Protocol/{NetworkManagerProtocol, LoadModelProtocol, TotalLoadProtocol, URLSessionProtocol}.swift`
- `SubwayWhenNetworking/DataLoad/LoadModel/BundleExtension.swift`

## 현재 상태 파악

### 신규
- `data/network/` 패키지 전체 (Ktor 기반 NetworkManager + 인터페이스)
- `data/remote/loadmodel/` 패키지 (LoadModel + 인터페이스)
- `data/network/TokenProvider.kt` (토큰 주입을 위한 단일 진입점 — 본문은 비워두고 외부에서 `.gitignore` 처리 후 채울 자리)
- `data/network/NetworkResult.kt` (iOS `Result<T, URLError>` 대응 — `sealed interface`)
- `data/remote/dto/` 패키지 (iOS Entity 디렉토리 대응 — DTO들)
- `di/NetworkModule.kt` (Hilt — HttpClient / NetworkManager / LoadModel 바인딩)
- `gradle/libs.versions.toml` 에 Ktor / kotlinx.serialization 항목 추가
- `app/build.gradle.kts` 에 Ktor / kotlinx.serialization 플러그인·의존성 추가
- 테스트: `data/network/NetworkManagerTest.kt`, `data/remote/loadmodel/LoadModelTest.kt`

### 재사용
- `viewModelScope` 기반 코루틴, Hilt DI 구조 (현 `di/AppModule.kt`, `di/RepositoryModule.kt` 패턴을 그대로 따름)

### 수정
- `gradle/libs.versions.toml`: Ktor, kotlinx.serialization 의존성 추가
- `app/build.gradle.kts`: 플러그인(kotlinx-serialization) + Ktor 의존성 추가

### 삭제
- 없음

## 기술적 결정사항

- **비동기 타입**: iOS `Single<Result<T, URLError>>` → Kotlin `suspend fun ... : NetworkResult<T>`
  - 이유: iOS Single은 "1회성 결과"라 Flow보다 `suspend`가 정확히 대응. 호출부도 `viewModelScope.launch { ... }` 한 줄로 끝남.
  - 대안: `Flow<NetworkResult<T>>` 검토했으나, 단일 응답 API에 Flow는 과함.

- **에러 표현**: `NetworkResult<T>` sealed interface (`Success(T)` / `Failure(NetworkError)`)
  - iOS의 `URLError` 케이스(`badURL` / `badServerResponse` / `cannotParseResponse` / `notConnectedToInternet`)를 그대로 매핑한 `NetworkError` enum 보유.
  - 예외 던지지 않고 값으로 반환 — iOS와 동작 일치, ViewModel에서 try/catch 불필요.

- **HTTP 클라이언트**: Ktor `HttpClient(OkHttp)` + `ContentNegotiation(Json)` + `HttpTimeout`
  - 이유: 프로젝트 컨벤션이 Ktor 명시. OkHttp 엔진은 안드로이드 표준.
  - 타임아웃 10초 (iOS와 동일).
  - JSON: `kotlinx.serialization` (컨벤션 명시).
  - 테스트 시 `HttpClient(MockEngine { ... })`을 생성자에 직접 주입 — `HttpClientProvider` 불필요.

- **상태코드 매핑**: iOS와 동일하게 `200..300 → Success`, `300..400 → badServerResponse`, 그 외 → `badURL`, 파싱 실패 → `cannotParseResponse`, 네트워크 끊김(예외) → `notConnectedToInternet`
  - Acceptance Criteria(`200 OK`)와 직접 매핑.

- **프로토콜(인터페이스) 분리**: 2단 구조 (iOS 4단에서 불필요 레이어 제거)
  - `NetworkManager` (interface) → `LoadModel` (interface)
  - `HttpClientProvider` 제거: Ktor는 `HttpClient(MockEngine { ... })`으로 생성자 주입이 가능해 iOS의 `URLSessionProtocol`이 필요했던 이유가 없음.
  - `TotalLoadModel`: 의존 도메인 모델 미존재로 후속 spec에서 생성.
  - 인터페이스 이름에 `Protocol` suffix 없음 (Android 관용: 인터페이스가 짧은 이름, 구현체가 `Impl` suffix).
  - Hilt `@Binds`로 인터페이스 ↔ 구현체 연결.

- **토큰 주입**: `TokenProvider` 인터페이스 하나로 격리, 함수 형태(`fun token(key: TokenKey): String`)
  - 이유: spec "토큰을 주입하는 부분은 따로 함수 처리만" 명시. 실제 값 로딩 방식(BuildConfig / local.properties / Firebase Remote Config 등)은 외부 작업 이후 결정.
  - 우선 `DefaultTokenProvider`는 빈 문자열 반환 stub 으로 두고, Hilt 바인딩만 걸어 둠.
  - `TokenKey` enum: `LIVE`, `SEOUL`, `KORAIL`, `KAKAO`, `REALTIME` (iOS plist 키와 1:1 대응).

- **URL 빌드**: Ktor `HttpRequestBuilder` + `url { ... }` DSL 사용 (직접 문자열 percent-encoding 안 함)
  - 이유: iOS는 `addingPercentEncoding` 수동 호출했지만, Ktor가 query 인코딩을 안전하게 처리. iOS 코드의 ` ` 제거 로직(`replacingOccurrences(of: " ", with: "")`)도 Ktor URL DSL에서 자연스럽게 회피 가능.

- **iOS Firebase Realtime DB 의존 메서드 처리**: 본 Plan 범위에서 **제외**
  - 대상: `korailTrainNumberLoad`, `defaultViewListRequest`, `importantDataLoad`(Firebase 측), `shinbundangScheduleReqeust`, `shinbundangScheduleVersionRequest`, `searchQueryRecommendListRequest`
  - 이유: Firebase BOM 추가, `google-services.json` 배포, DB 권한·스킴 설정 등 인프라 작업이 spec 외. Acceptance Criteria(`통신 시 200`)는 HTTP 엔드포인트만으로 충족 가능.
  - 후속 spec에서 Firebase 통합 시 `LoadModelProtocol`에 메서드 추가하는 형태로 확장.

- **TotalLoadModel 도메인 변환 로직 처리**: 인터페이스만 생성, 구현체는 후속 spec
  - `TotalLoadModel` 인터페이스는 메서드 없는 빈 인터페이스로 생성 — 레이어 자리 확보.
  - 구현체(`TotalLoadModelImpl`)는 메인/상세 spec에서 도메인 모델 확정 후 생성.
  - Hilt 바인딩도 구현체 없으므로 이번 Plan에서는 제외.

- **DTO 범위**: `LoadModel`에서 사용하는 응답 모델은 본 Plan의 HTTP 엔드포인트가 다루는 것만 포함
  - `LiveStationModel`(+ `RealtimeStationArrival`), `ScheduleStationModel`(+ `ScheduleStationArrival`, `SearchSTNTimeTableByFRCodeService`), `SearchStaion`(+ `SearchInfoBySubwayNameService`), `VicinityStationsData`(+ `VicinityDocumentData`), `KorailHeader`(+ `KorailScdule`), `SubwayNoticeResponse`(+ `SubwayNotice`), `RealtimeTrainPositionResponse`(+ `RealtimeTrainPosition`)
  - iOS Entity의 `Codable` 키 명을 그대로 가져오되, Kotlin은 `@SerialName` 으로 매핑.
  - 위치: `data/remote/dto/` (Room `@Entity`와 혼동 방지를 위해 `entity/` 대신 `dto/` 사용)

- **부역명 매핑**: iOS `arrivalStationNameChack` 의 24개 switch 케이스
  - `LoadModel` 내부 `private fun` 으로 그대로 이식 (Map<String, String> + getOrDefault).
  - 외부에 노출하지 않음 (iOS도 private).

- **테스트 전략**: spec "테스트에 용이하게" 충족
  - `NetworkManager` 단위 테스트: Ktor `MockEngine` 으로 200 / 301 / 500 / 파싱 실패 / 예외 4 케이스 검증 → `NetworkResult` 매핑 확인. 200 케이스가 Acceptance Criteria 직접 검증.
  - `LoadModel` 단위 테스트: 가짜 `NetworkManager`(MockK) 주입 → URL 조립과 토큰 키 사용이 의도대로 되는지(스파이로 호출 인자 캡처), 부역명 치환이 정확한지 확인.

## 구현 순서

### Phase 1. 의존성 셋업
- `gradle/libs.versions.toml`에 Ktor(core / okhttp / content-negotiation / serialization-json / logging) 및 kotlinx-serialization 플러그인·라이브러리 추가
- `app/build.gradle.kts`에 플러그인·의존성 적용
- 검증: 프로젝트 sync 통과 + `./gradlew :app:assembleDebug` 빌드 성공

### Phase 2. 네트워크 코어 (NetworkManager)
- `data/network/NetworkResult.kt` — sealed interface (`Success(data)` / `Failure(error)`)
- `data/network/NetworkError.kt` — enum (`BadUrl`, `BadServerResponse`, `CannotParseResponse`, `NotConnectedToInternet`)
- `data/network/TokenKey.kt`, `data/network/TokenProvider.kt` (인터페이스 + stub 구현체 `DefaultTokenProvider` — 본문 빈 문자열 반환)
- `data/network/NetworkManager.kt` (인터페이스 `NetworkManager` + 구현체 `NetworkManagerImpl`)
  - `suspend fun <reified T> requestData(url: String): NetworkResult<T>`
  - `suspend fun <reified T> requestData(url: String, headers: Map<String, String>, query: Map<String, String>): NetworkResult<T>`
  - iOS 두 오버로드를 그대로 대응. inline + reified로 `Decodable.Type` 인자 불필요화.
  - 테스트 시 `HttpClient(MockEngine { ... })`을 생성자에 직접 주입 (`HttpClientProvider` 없음)
- `di/NetworkModule.kt`: `@Binds NetworkManager`, `@Provides HttpClient`, `@Binds TokenProvider`
- 테스트: `NetworkManagerTest` — MockEngine 시나리오 5개 (200, 300대, 500, 파싱 실패, 예외)
- 검증: `./gradlew :app:test` 통과 + 200 성공 케이스가 `NetworkResult.Success` 로 매핑됨 (Acceptance Criteria)

### Phase 3. LoadModel
- `data/remote/dto/` 하위에 iOS Entity 구조 그대로 DTO 작성 (`liveArrival/`, `scheduleArrival/seoul/`, `scheduleArrival/korail/`, `stationSearch/`, `vicinityStation/`, `subwayNotice/`, `realtimePosition/` 서브패키지)
  - 모든 클래스 `@Serializable` + `@SerialName` (iOS JSON 키 그대로)
- `data/remote/loadmodel/LoadModel.kt` — 인터페이스 `LoadModel`, HTTP 엔드포인트만 (Firebase 제외)
  - `stationArrivalRequest(stationName)`, `seoulStationScheduleLoad(...)`, `korailScheduleLoad(...)`, `stationSearch(...)`, `vicinityStationsLoad(x, y)`, `subwayNoticeRequest()`, `realtimePositionRequest(subwayLine)` — 총 7개
- `data/remote/loadmodel/LoadModelImpl.kt` — `LoadModel` 구현체
  - 생성자 주입: `NetworkManager`, `TokenProvider`
  - URL 조립 시 iOS 와 동일한 엔드포인트 사용 (`http://swopenapi.seoul.go.kr/...`, `http://openapi.seoul.go.kr:8088/...`, `https://openapi.kric.go.kr/...`, `https://dapi.kakao.com/...`)
  - `arrivalStationNameCheck` private 함수 이식 (Map 기반)
  - 9호선 / DayType / 상하행 분기 로직 iOS와 동일
- `di/NetworkModule.kt`에 `@Binds LoadModel`
- 테스트: `LoadModelTest` — 가짜 `NetworkManager`로 URL/토큰/부역명/9호선 분기 검증 (각 메서드 1개씩 + 부역명 케이스 1개)
- 검증: `./gradlew :app:test` 통과

### Phase 4. TotalLoadModel (인터페이스만)
- `data/remote/totalload/TotalLoadModel.kt` — 메서드 없는 빈 인터페이스. 레이어 존재 선언.
- 검증: 컴파일 통과

## 패키지 구조 (최종)

```
com.yslee.subwaywhen
├── data
│   ├── network
│   │   ├── NetworkManager.kt              // interface
│   │   ├── NetworkManagerImpl.kt          // 구현체
│   │   ├── NetworkResult.kt
│   │   ├── NetworkError.kt
│   │   ├── TokenKey.kt
│   │   └── TokenProvider.kt               // 토큰 주입 함수 (외부 작업 후 채움)
│   └── remote
│       ├── dto                            // iOS Entity 대응 DTO (entity/ 대신 dto/)
│       │   ├── liveArrival/
│       │   ├── scheduleArrival/
│       │   ├── stationSearch/
│       │   ├── vicinityStation/
│       │   ├── subwayNotice/
│       │   └── realtimePosition/
│       ├── loadmodel
│       │   ├── LoadModel.kt               // interface (Protocol suffix 없음)
│       │   └── LoadModelImpl.kt           // 구현체
│       └── totalload
│           └── TotalLoadModel.kt          // interface only (메서드 없음, 레이어 자리 확보)
└── di
    └── NetworkModule.kt
```

## 완료 조건

- [ ] Spec Acceptance Criteria 충족: 정상 응답(200) 시 `NetworkResult.Success` 반환 — `NetworkManagerTest` MockEngine 200 케이스 통과로 검증
- [ ] `NetworkManager` / `LoadModel` / `TotalLoadModel` 인터페이스 분리 — spec "프로토콜 기반 설계" 충족
- [ ] `LoadModelImpl`이 내부에서 `NetworkManager` 사용 — spec "기반" 구조 충족
- [ ] 토큰 주입은 `TokenProvider.token(key)` 단일 함수로 격리, 본문은 외부 작업 자리로 비워둠 — spec "토큰을 주입하는 부분은 따로 함수 처리만" 충족
- [ ] 가짜 구현 교체로 단위 테스트 작성 가능 — `NetworkManagerTest`, `LoadModelTest` 통과
- [ ] `./gradlew :app:assembleDebug` / `./gradlew :app:test` 모두 성공
- [ ] AOS 컨벤션 준수: `libs.versions.toml` 사용, Hilt 주입, `viewModelScope`/`suspend` 비동기, 패키지 `feature/`·`data/` 분리 — `conventions.md` 항목 충족
