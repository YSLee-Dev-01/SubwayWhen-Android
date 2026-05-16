# Plan: Logger

## 참조 Spec
- @specs/features/Logger/spec.md

## 참조 iOS 원본
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Util/Logger/AppLogger.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Configuration/AppConfig.swift`

## 현재 상태 파악
- 신규:
  - `app/src/main/java/com/yslee/subwaywhen/core/logger/AppLogger.kt` (Logger 본체 + 카테고리별 미리 정의)
  - `app/src/test/java/com/yslee/subwaywhen/core/logger/AppLoggerTest.kt` (단위 테스트)
- 재사용:
  - Android 기본 `android.util.Log` (OSLog 대응)
- 수정: 없음
- 삭제: 없음

## 기술적 결정사항
- **Logger 구현체**: `android.util.Log` 사용
  - 이유: iOS OSLog와 1:1로 대응되는 표준 시스템 로깅 API. 추가 라이브러리 의존성 없이 LogLevel(error/info/debug)을 그대로 매핑 가능.
  - 대안: Timber → 외부 의존성 추가 필요하고 현재 요구사항(3개 레벨, 카테고리, on/off 플래그) 대비 과한 추상화.
- **Kotlin 형태**: `object` 싱글톤 + 내부 `class` 또는 `data class` 인스턴스
  - 이유: iOS의 `struct AppLogger` + `static let` extension 패턴을 Kotlin으로 자연스럽게 표현. 인스턴스 생성 없이 `AppLogger.Network.log(...)` 형태로 접근.
  - 대안: top-level function → 카테고리/플래그 상태를 가진 구조와 어긋남.
- **LogLevel 표현**: `enum class LogLevel { ERROR, INFO, DEBUG }`
  - 이유: iOS `enum LogLevel` 그대로 대응. when 분기로 `Log.e/i/d` 호출 매핑.
- **카테고리 범위**: `Network`, `Core`, `View` 3종만 (Coordinator/LiveActivity/CoreData 제외)
  - 이유: spec.md 명시 (`AOS에는 Network, Core, View만 있어도 충분해`). Android 아키텍처에는 Coordinator/CoreData가 존재하지 않으며, LiveActivity는 P2 이후 기능이라 현 시점 불필요.
- **totalLogEnabled 관리 위치**: `AppLogger.kt` 내부에 `const val`로 직접 선언
  - 이유: iOS는 `AppConfig.shared.enable*Log`로 분리했지만, Android 측에는 아직 `AppConfig` 대응 객체가 없고 Logger 외에 공유될 설정이 없음. 단순성 우선(가이드라인 2).
  - 대안: `BuildConfig.DEBUG` 활용 → 카테고리별 on/off 미지원으로 spec 요구 불충족.
  - 대안: 별도 `AppConfig` 클래스 신설 → 현 단계에서 사용처가 Logger 하나뿐이라 과한 추상화. 추후 다른 설정 항목이 생기면 그때 분리.
- **카테고리 prefix 이모지**: iOS와 동일 유지 (`🛜 Network`, `💪 Core`, `💬 View`)
  - 이유: iOS와 로그 포맷을 일치시켜 cross-platform 디버깅 시 인지 부담 감소.
- **log() 시그니처**: `fun log(level: LogLevel, message: String, enableLog: Boolean = true)`
  - 이유: iOS 시그니처(level, message, enableLog default true) 그대로 포팅. enableLog는 호출부 단위 on/off, totalLogEnabled는 카테고리 단위 on/off.
- **MVI 레이어 외부 유틸**: `core/logger/` 패키지에 배치
  - 이유: 특정 feature에 종속되지 않는 횡단 유틸. 기존 `core/FixInfo.kt`와 동일 위치 정책.
- **Hilt 주입 여부**: 주입하지 않음
  - 이유: 상태 없는 정적 접근(`AppLogger.Network.log(...)`)이 iOS 사용 방식과 일치하고, 테스트 시에도 별도 mock 불필요. ViewModel/Repository 어디서나 import만으로 호출 가능.

## 구현 순서

### Phase 1. Core Logger
- `core/logger/AppLogger.kt` 신규 작성
  - `enum class LogLevel { ERROR, INFO, DEBUG }` 정의
  - `class AppLogger(categoryName: String, totalLogEnabled: Boolean)` 본체에 `log(level, message, enableLog = true)` 메서드 구현
  - `totalLogEnabled == false` 또는 `enableLog == false` 면 즉시 return
  - level별로 `android.util.Log.e/i/d(TAG, "$categoryName: $message")` 호출 (TAG는 패키지 식별 위해 `subsystem` = `com.yslee.subwaywhen`)
  - `companion object` 또는 동반 `object AppLoggers` 로 미리 정의된 인스턴스 노출
    - `Network` (categoryName = `"🛜 Network"`, totalLogEnabled = `true`)
    - `Core` (categoryName = `"💪 Core"`, totalLogEnabled = `true`)
    - `View` (categoryName = `"💬 View"`, totalLogEnabled = `true`)

### Phase 2. 단위 테스트
- `core/logger/AppLoggerTest.kt` 신규 작성
  - `android.util.Log`는 JVM 단위 테스트에서 stub 호출 시 예외가 나므로 MockK의 `mockkStatic(Log::class)` 또는 Robolectric 없이 가능하면 verify 기반으로 검증
  - 테스트 케이스:
    1. `totalLogEnabled = true` + `enableLog = true` 일 때 `Log.i/e/d`가 호출되는지 (Acceptance #1: 로그가 정상 출력)
    2. `totalLogEnabled = false` 일 때 어떤 레벨로도 `Log.*`가 호출되지 않는지 (Acceptance #2: totalEnabled false 시 미출력)
    3. `totalLogEnabled = true` + `enableLog = false` 일 때 호출되지 않는지 (호출부 단위 off 검증)
    4. level별로 올바른 `Log.e / Log.i / Log.d` API에 매핑되는지

## 완료 조건
- [ ] `AppLogger.Network.log(LogLevel.INFO, "msg")` 호출 시 logcat에 `🛜 Network: msg` 형태로 정상 출력됨 (Spec Acceptance #1)
- [ ] 카테고리 인스턴스의 `totalLogEnabled = false` 인 상태에서 `log(...)` 호출해도 logcat에 출력되지 않음 (Spec Acceptance #2)
- [ ] `Network`, `Core`, `View` 3개 카테고리가 인스턴스 생성 없이 정적으로 접근 가능
- [ ] 단위 테스트(`AppLoggerTest`) 전부 통과
- [ ] 기존 빌드/테스트 회귀 없음
