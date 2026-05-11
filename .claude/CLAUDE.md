## 프로젝트 개요
지하철 민원 접수부터 실시간 도착 정보, 시간표 정보까지 **'한눈에, 빠르고 편하게'** 확인할 수 있는 AOS 앱

iOS 앱 **지하철 민실씨**를 Android로 포팅하는 프로젝트.
iOS 원본 프로젝트를 기능 기준으로 삼되, 아키텍처는 Android 관용 방식으로 재설계한다.

---

## 프로젝트 기본 정보

- **패키지**: `com.yslee.subwaywhen`
- **minSdk**: 29 (Android 10)
- **targetSdk / compileSdk**: 36
- **언어**: Kotlin
- **UI**: Jetpack Compose

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Kotlin |
| UI | Jetpack Compose |
| 아키텍처 | MVI |
| 비동기 | Kotlin Coroutines + Flow |
| 네트워크 | Ktor |
| 테스트 | JUnit + Kotest |
| 의존성 관리 | Gradle (Version Catalog) |

---

## 규칙 문서 참조

| 문서 | 내용 |
|------|------|
| [ios-reference.md](rules/ios-reference.md) | iOS 원본 참조 경로, 기술 스택 대응, 기능 포팅 목록 |
| [architecture.md](rules/architecture.md) | MVI 레이어 구조, 화면 전환 |
| [conventions.md](rules/conventions.md) | 코딩 컨벤션, 의존성 목록 |

---

## 주의 사항

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
