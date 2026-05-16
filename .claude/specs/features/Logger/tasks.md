# Tasks: Logger

## 참조
- spec: `.claude/specs/features/Logger/spec.md`
- plan: `.claude/specs/features/Logger/plan.md`

## Task 목록

### Phase 1. Core Logger

#### [x] Task 1 — `AppLogger.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/core/logger/AppLogger.kt`
- `enum class LogLevel { ERROR, INFO, DEBUG }` 정의
- `class AppLogger(categoryName: String, totalLogEnabled: Boolean)` 정의
  - `log(level: LogLevel, message: String, enableLog: Boolean = true)` 메서드 구현
  - `totalLogEnabled == false` 또는 `enableLog == false` 이면 즉시 return
  - level별로 `android.util.Log.e / Log.i / Log.d` 호출, TAG는 `"com.yslee.subwaywhen"`, 메시지 포맷은 `"$categoryName: $message"`
- `companion object` 로 카테고리별 사전 정의 인스턴스 노출
  - `Network` : `categoryName = "🛜 Network"`, `totalLogEnabled = true`
  - `Core` : `categoryName = "💪 Core"`, `totalLogEnabled = true`
  - `View` : `categoryName = "💬 View"`, `totalLogEnabled = true`

---

### Phase 2. 단위 테스트

#### [x] Task 2 — `AppLoggerTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/core/logger/AppLoggerTest.kt`
- `mockkStatic(Log::class)` 로 `android.util.Log` 정적 메서드 모킹
- 테스트 케이스 4개 작성
  1. `totalLogEnabled = true` + `enableLog = true` 일 때 레벨별로 `Log.i / Log.e / Log.d`가 정확히 1회 호출되는지 verify (Acceptance #1: 로그 정상 출력)
  2. `totalLogEnabled = false` 일 때 어떤 레벨로도 `Log.*`가 호출되지 않는지 verify (Acceptance #2: totalEnabled false 시 미출력)
  3. `totalLogEnabled = true` + `enableLog = false` 일 때 `Log.*`가 호출되지 않는지 verify (호출부 단위 off 검증)
  4. `LogLevel.ERROR → Log.e`, `LogLevel.INFO → Log.i`, `LogLevel.DEBUG → Log.d` 로 올바르게 매핑되는지 각각 verify

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공
- [x] 테스트 통과

### 기능 (AC)
- [x] `AppLogger.Network.log(LogLevel.INFO, "msg")` 호출 시 logcat에 `🛜 Network: msg` 형태로 정상 출력됨 (Spec Acceptance #1)
- [x] 카테고리 인스턴스의 `totalLogEnabled = false` 상태에서 `log(...)` 호출해도 logcat에 출력되지 않음 (Spec Acceptance #2)
- [x] `Network`, `Core`, `View` 3개 카테고리가 인스턴스 생성 없이 정적으로 접근 가능
