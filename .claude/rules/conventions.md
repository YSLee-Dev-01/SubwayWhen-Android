# 컨벤션 및 의존성

## 코딩 컨벤션

- **패키지 구조**: `feature/{기능명}` 단위로 분리 (예: `feature/main`, `feature/search`, `feature/detail`)
- **UiState**: sealed interface로 정의 — `Loading` / `Success(data)` / `Error(message)`
- **단방향 데이터 흐름**: 사용자 인터랙션은 `onIntent(Intent)` 단일 진입점으로 ViewModel에 전달
- **비동기**: `viewModelScope.launch` + Flow 사용. `GlobalScope` 사용 금지
- **토큰 관리**: `local.properties`에서 읽어 `BuildConfig`로 주입. `BuildConfig`는 커밋하지 않음

## Dimens 토큰 규칙

`ui/theme/Dimens.kt` 는 iOS `ViewStyle.swift` 대응 토큰 파일이다.

- **ViewStyle과 실제 View의 값이 다를 수 있다** — ViewStyle 값을 무조건 따르지 말고 실제 View 기준으로 판단한다.
- **2회 이상 반복되는 여백·크기 값은 `Dimens.kt`에 추가하고 참조한다.**
- 단 1곳에서만 쓰이는 값은 컴포저블 내부에 인라인으로 둬도 무방하다.

## 의존성 관리

`libs.versions.toml` (Version Catalog)을 사용한다. `build.gradle.kts`에 버전 하드코딩 금지.

| 역할 | 라이브러리 |
|------|-----------|
| DI | Hilt |
| 네트워크 | Ktor |
| JSON | kotlinx.serialization |
| 로컬 저장 | DataStore Preferences |
| DB 캐시 | Room |
| 이미지 | Coil |
| Firebase | Firebase BOM (Analytics, Database) |
| 테스트 | JUnit + Kotest + Turbine + MockK |
