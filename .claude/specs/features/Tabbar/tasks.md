# Tasks: Tabbar

## 참조
- spec: `.claude/specs/features/Tabbar/spec.md`
- plan: `.claude/specs/features/Tabbar/plan.md`

## Task 목록

### Phase 1. 의존성 확인 및 기반 준비

#### [x] Task 1 — `libs.versions.toml` 의존성 확인
**파일**: `gradle/libs.versions.toml`
- `Icons.Filled.Home`, `Icons.Filled.Search`, `Icons.Filled.Settings`가 현재 `compose-material3` 또는 `material-icons-core` 에 포함되어 있는지 확인
- 미포함 시 `material-icons-extended` 라이브러리를 Version Catalog에 추가하고 `app/build.gradle.kts`에서 참조

---

#### [x] Task 2 — `strings.xml` 문자열 리소스 추가
**파일**: `app/src/main/res/values/strings.xml`
- 탭 라벨 추가: `tab_home`, `tab_search`, `tab_setting`
- 각 탭 화면 텍스트 추가: `home_placeholder_title="홈"`, `search_placeholder_title="검색"`, `setting_placeholder_title="설정"`

---

### Phase 2. 탭 라우트 정의

#### [x] Task 3 — `TabRoute.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/TabRoute.kt`
- `sealed class TabRoute(val route: String, val labelRes: Int, val icon: ImageVector)` 정의
- `object Home : TabRoute("tab_home", R.string.tab_home, Icons.Filled.Home)` — iOS `UITabBarItem(image: house, tag: 0)` 대응
- `object Search : TabRoute("tab_search", R.string.tab_search, Icons.Filled.Search)` — iOS `UITabBarItem(image: magnifyingglass, tag: 1)` 대응
- `object Setting : TabRoute("tab_setting", R.string.tab_setting, Icons.Filled.Settings)` — iOS `UITabBarItem(image: gearshape, tag: 2)` 대응
- `companion object { val all = listOf(Home, Search, Setting) }` 추가

---

### Phase 3. 각 탭 ViewModel + Screen

#### [x] Task 4 — `HomeViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeViewModel.kt`
- `@HiltViewModel class HomeViewModel @Inject constructor() : ViewModel()` 빈 ViewModel 정의
- 비즈니스 로직, UiState, Intent, Effect 없음 — 추후 기능 추가 시 MVI 구조를 갖출 자리만 마련

---

#### [x] Task 5 — `HomeScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeScreen.kt`
- `@Composable fun HomeScreen(viewModel: HomeViewModel = hiltViewModel())` 정의
- 화면 중앙에 `Text(stringResource(R.string.home_placeholder_title))` 표시 (`Box` + `contentAlignment = Alignment.Center`)
- `viewModel`은 `hiltViewModel()`로 주입만 하고 상태 사용 없음
- `@Preview` 추가 (Light/Dark)

---

#### [x] Task 6 — `SearchViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchViewModel.kt`
- `HomeViewModel`과 동일한 패턴의 빈 `@HiltViewModel` 정의

---

#### [x] Task 7 — `SearchScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt`
- `HomeScreen`과 동일한 패턴으로 구현
- 화면 중앙에 `Text(stringResource(R.string.search_placeholder_title))` 표시
- `@Preview` 추가 (Light/Dark)

---

#### [x] Task 8 — `SettingViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingViewModel.kt`
- `HomeViewModel`과 동일한 패턴의 빈 `@HiltViewModel` 정의

---

#### [x] Task 9 — `SettingScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingScreen.kt`
- `HomeScreen`과 동일한 패턴으로 구현
- 화면 중앙에 `Text(stringResource(R.string.setting_placeholder_title))` 표시
- `@Preview` 추가 (Light/Dark)

---

### Phase 4. RootScaffold (탭바 + 내부 NavHost)

#### [x] Task 10 — `RootScaffold.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/RootScaffold.kt`
- `@Composable fun RootScaffold()` 정의 — iOS `AppCoordinator.setTabbarController()` 대응
- 내부에서 `rememberNavController()`로 자식 navController 생성
- `Scaffold(bottomBar = { NavigationBar { ... } })` 구성:
  - `TabRoute.all.forEach`로 `NavigationBarItem` 3개 렌더링
  - `selected`: `currentDestination?.hierarchy?.any { it.route == tab.route } == true`
  - `onClick`: 단일 인스턴스 패턴 — `popUpTo(graph.findStartDestination().id) { saveState = true }`, `launchSingleTop = true`, `restoreState = true`
  - `icon`: `Icon(tab.icon, contentDescription = stringResource(tab.labelRes))`
  - `label`: `Text(stringResource(tab.labelRes))`
  - `colors`: `selectedIconColor = AppIconColor`, `selectedTextColor = AppIconColor`, `indicatorColor = MaterialTheme.colorScheme.background`
- `NavHost(navController = childNavController, startDestination = TabRoute.Home.route, modifier = Modifier.padding(innerPadding))`:
  - `composable(TabRoute.Home.route) { HomeScreen() }`
  - `composable(TabRoute.Search.route) { SearchScreen() }`
  - `composable(TabRoute.Setting.route) { SettingScreen() }`
- `@Preview` 추가 (Light/Dark)

---

### Phase 5. AppNavHost 통합 (Home → Root)

#### [x] Task 11 — `NavRoutes.kt` 수정
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/NavRoutes.kt`
- `Home` 상수 제거
- `Root` 상수 추가

---

#### [x] Task 12 — `AppNavHost.kt` 수정
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/AppNavHost.kt`
- `composable(NavRoutes.Home) { HomePlaceholderScreen() }` 삭제
- `composable(NavRoutes.Root) { RootScaffold() }` 추가
- `SplashEffect.NavigateToHome` 처리 시 navigate 대상 `NavRoutes.Home` → `NavRoutes.Root`로 변경 / `popUpTo` 인자 동일 갱신
- `TutorialEffect.NavigateToHome` 처리 시 navigate 대상 `NavRoutes.Home` → `NavRoutes.Root`로 변경 / `popUpTo` 인자 동일 갱신

---

#### [x] Task 13 — `HomePlaceholderScreen.kt` 삭제
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomePlaceholderScreen.kt`
- Task 12에서 `AppNavHost`의 호출처가 제거되어 고아가 된 파일을 삭제
- 삭제 전 호출처가 `AppNavHost`에서 완전히 제거되었는지 확인

---

### Phase 6. Preview 검증

#### [x] Task 14 — Preview 및 동작 확인
- Android Studio Preview로 `RootScaffold`, `HomeScreen`, `SearchScreen`, `SettingScreen` 렌더링 확인 (Light/Dark)
- 실기기/에뮬레이터 시나리오 확인:
  - 앱 첫 실행(튜토리얼 미완료) → 튜토리얼 → "시작하기" → 탭바 노출 + Home 탭 선택 상태
  - 탭 클릭 시 해당 화면(홈/검색/설정 텍스트)으로 이동
  - 앱 재진입(튜토리얼 완료 후) → Splash → 탭바 노출

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공
- [ ] 기존 `TutorialViewModelTest` 회귀 없음
- [ ] `HomePlaceholderScreen.kt` 삭제 완료 및 호출처 없음 확인
- [ ] 매직 문자열 없음 (탭 라벨/화면 텍스트 전부 `strings.xml` 참조)
- [ ] 선택된 탭 tint는 `AppIconColor` 사용

### 기능 (AC)
- [ ] 하단에 홈, 검색, 설정 탭바가 존재
- [ ] 각 버튼을 누르면 해당 화면으로 이동 (텍스트로 식별 가능)
- [ ] 튜토리얼 완료 여부에 따른 Root 분기 동작 유지
