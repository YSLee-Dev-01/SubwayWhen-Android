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

## 구현 현황

| 기능 | 상태 | 주요 파일 |
|------|------|-----------|
| Splash | ✅ 완료 | `feature/splash/SplashViewModel.kt` |
| Tutorial | ✅ 완료 | `feature/tutorial/` |
| 탭바 + 네비게이션 | ✅ 완료 | `navigation/RootScaffold.kt`, `AppNavHost.kt` |
| 역 검색 | ✅ 완료 | `feature/search/` |
| 주변역 탐색 (Vicinity) | ✅ 완료 | `feature/search/vicinity/` |
| 역 저장 Modal | ✅ 완료 | `feature/search/modal/` |
| 공통 컴포넌트 | ✅ 완료 | `ui/common/` |
| 데이터 레이어 | ✅ 완료 | `data/` |
| 메인 화면 | ✅ 완료 | `feature/home/HomeScreen.kt`, `HomeViewModel.kt` |
| 실시간 도착정보 | ✅ 완료 | `feature/home/HomeViewModel.kt` (arrivalDataLoad), `mapper/HomeCellMapper.kt` |
| 시간표 조회 (서울/코레일) | ✅ 완료 | `feature/home/HomeViewModel.kt` (handleScheduleTap), `mapper/HomeCellMapper.kt` |
| 시간표 조회 (신분당선) | ✅ 완료 | `data/local/room/`, `data/remote/loadmodel/LoadModelImpl.kt`, `data/remote/totalload/TotalLoadModelImpl.kt` |
| 역 편집 | ✅ 완료 | `feature/edit/EditScreen.kt`, `EditViewModel.kt` |
| 설정 화면 | ✅ 완료 | `feature/setting/SettingScreen.kt`, `SettingViewModel.kt`, `component/`, `modal/` |
| 출퇴근 알림 Modal | ✅ 완료 | `feature/setting/modal/WorkAlarmModal.kt`, `core/notification/` |

---

## 폴더 구조

```
app/src/main/java/com/yslee/subwaywhen/
├── core/
│   ├── FixInfo.kt                    # DataStore 키 상수
│   ├── location/                     # 위치 권한 · GPS
│   └── notification/                 # 알림 스케줄러, AlarmReceiver, BootReceiver
├── data/
│   ├── local/
│   │   ├── room/                     # Room DB (AppDatabase, ShinbundangScheduleEntity, DAO)
│   │   └── ...                       # DataStore (PreferencesKeys, DataSources)
│   ├── model/                        # SaveStation, SaveStationGroup, SaveSetting
│   ├── network/                      # Ktor 클라이언트, NetworkResult
│   ├── remote/
│   │   ├── dto/                      # 응답 DTO (liveArrival, scheduleArrival 등)
│   │   ├── firebase/                 # Firebase Realtime DB
│   │   ├── loadmodel/                # LoadModel (API 호출 로직)
│   │   └── totalload/                # TotalLoadModel (병렬 호출 통합)
│   └── repository/                   # Repository 인터페이스 + Impl
├── di/                               # Hilt 모듈
├── feature/
│   ├── edit/                         # 역 편집 화면
│   │   ├── EditContract.kt           # UiState, Intent, Effect
│   │   ├── EditMapper.kt             # 그룹 분리/병합 매퍼
│   │   ├── EditScreen.kt             # Composable 진입점
│   │   ├── EditViewModel.kt          # 비즈니스 로직
│   │   └── component/               # EditStationRow, NotSaveAlertDialog
│   ├── home/                         # 메인 화면
│   ├── search/
│   │   ├── component/                # SearchTextField, 결과/추천 섹션
│   │   ├── modal/                    # SaveStationModal + SaveCompletedModal
│   │   │   └── component/
│   │   └── vicinity/                 # 주변역 섹션
│   │       ├── component/            # StationDetailCard, Row, MiniRow 등
│   │       └── modal/                # LocationListModal
│   ├── setting/                      # 설정 화면
│   ├── splash/
│   └── tutorial/
├── navigation/
│   ├── AppNavHost.kt                 # Splash → Tutorial → Root 흐름
│   ├── NavRoutes.kt                  # 최상위 라우트 상수
│   ├── RootScaffold.kt              # 탭바 + 탭 NavHost
│   └── TabRoute.kt                   # 탭 정의 (Home, Search, Setting)
└── ui/
    ├── common/
    │   ├── modal/                    # CommonModalBottomSheet, ModalSubButton
    │   ├── AnimatedTapBox.kt         # 탭 시 스케일 애니메이션 래퍼
    │   ├── CommonTopBar.kt           # 공통 상단바
    │   ├── MainBgCard.kt             # 메인 배경 카드
    │   ├── PrimaryButton.kt          # 주요 액션 버튼
    │   ├── StationLineCircle.kt      # 호선 색상 원형 뱃지
    │   ├── StationRow.kt             # 역 정보 공통 Row (호선 뱃지 + 역명, null = "역 선택")
    │   ├── SubwayLineMapper.kt       # 호선명 → Color 매핑
    │   ├── TriangleShape.kt          # 삼각형 커스텀 Shape
    │   └── UpDownExceptionRow.kt     # 상하행 예외 안내 행
    └── theme/
        ├── Color.kt
        ├── Dimens.kt                 # 간격·크기·폰트 토큰
        ├── Theme.kt
        └── Type.kt
```

---

## 규칙 문서 참조

| 문서 | 내용 |
|------|------|
| [ios-reference.md](rules/ios-reference.md) | iOS 원본 참조 경로, 기술 스택 대응, 기능 포팅 목록 |
| [architecture.md](rules/architecture.md) | MVI 레이어 구조, 화면 전환 |
| [conventions.md](rules/conventions.md) | 코딩 컨벤션, 의존성 목록, 공통 패턴 |

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
