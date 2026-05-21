# Plan: Tabbar

## 참조 Spec
- @specs/features/Tabbar/spec.md

## 참조 iOS 원본
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Application/AppDelegate.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Application/SceneDelegate.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Application/Coordinator/AppCoordinator.swift` — Root 분기(`start()`) 및 `setTabbarController()`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Main/Coordinator/MainCoordinator.swift` — Home 탭 아이콘 `house`, tag 0
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Search/Coordinator/SearchCoordinator.swift` — Search 탭 아이콘 `magnifyingglass`, tag 1
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Setting/Coordinator/SettingCoordinator.swift` — Setting 탭 아이콘 `gearshape`, tag 2

## 현재 상태 파악

### 재사용
- `app/src/main/java/com/yslee/subwaywhen/navigation/AppNavHost.kt` — 현재 `Splash` → `Tutorial`/`Home` 분기 NavHost. 분기 로직 자체는 유지.
- `app/src/main/java/com/yslee/subwaywhen/navigation/NavRoutes.kt` — 라우트 상수 보관소. 라우트 추가 위치.
- `app/src/main/java/com/yslee/subwaywhen/feature/splash/SplashViewModel.kt` — 튜토리얼 완료 여부 기반 Root 분기 로직 (이미 구현됨, 변경 없음).
- `app/src/main/java/com/yslee/subwaywhen/feature/home/HomePlaceholderScreen.kt` — 현재 빈 `Box`. 본 spec에서 "기능 텍스트만 표시"로 내용을 채우고 ViewModel 연동.
- `app/src/main/java/com/yslee/subwaywhen/ui/theme/Color.kt` — `AppIconColor`(선택된 탭 tint), `MainColorLight/Dark`(탭바 배경) 토큰.
- `app/src/main/java/com/yslee/subwaywhen/ui/theme/Dimens.kt` — `fontSize*` 토큰.
- `app/src/main/res/values/strings.xml` — 탭 라벨 / Home·Search·Setting 화면 텍스트 추가 위치.

### 신규
- `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeScreen.kt` — Home 탭 화면. "홈" 텍스트만 표시. `HomeViewModel`을 `hiltViewModel()`로 주입만 해둠(상태 사용 X).
- `app/src/main/java/com/yslee/subwaywhen/feature/home/HomeViewModel.kt` — `@HiltViewModel` 빈 ViewModel(연동 자리만 마련, 비즈니스 로직 없음).
- `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchScreen.kt` — Search 탭 화면. "검색" 텍스트만 표시.
- `app/src/main/java/com/yslee/subwaywhen/feature/search/SearchViewModel.kt` — `@HiltViewModel` 빈 ViewModel.
- `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingScreen.kt` — Setting 탭 화면. "설정" 텍스트만 표시.
- `app/src/main/java/com/yslee/subwaywhen/feature/setting/SettingViewModel.kt` — `@HiltViewModel` 빈 ViewModel.
- `app/src/main/java/com/yslee/subwaywhen/navigation/RootScaffold.kt` — 하단 탭바(`NavigationBar`) + 내부 `NavHost`(Home/Search/Setting 자식 라우트)를 호스팅하는 컨테이너. iOS `AppCoordinator.setTabbarController()` 대응.
- `app/src/main/java/com/yslee/subwaywhen/navigation/TabRoute.kt` — 3개 탭 라우트(`home` / `search` / `setting`)와 표시 정보(아이콘, 라벨 리소스)를 묶은 sealed class 또는 enum.
- `app/src/main/res/drawable/ic_tab_home.xml` — Material Symbols `home` 아이콘(Compose Material Icons 활용 가능 시 별도 drawable 불필요, Phase 1에서 판단).
- `app/src/main/res/drawable/ic_tab_search.xml` — Material Symbols `search` 아이콘.
- `app/src/main/res/drawable/ic_tab_setting.xml` — Material Symbols `settings` 아이콘.
- `strings.xml`에 `tab_home` / `tab_search` / `tab_setting` 라벨, `home_placeholder_title="홈"` / `search_placeholder_title="검색"` / `setting_placeholder_title="설정"` 추가.

### 수정
- `app/src/main/java/com/yslee/subwaywhen/navigation/AppNavHost.kt` — `Home` composable 라우트를 `HomePlaceholderScreen()` 호출에서 `RootScaffold()` 호스팅으로 교체. 라우트 명은 `Home` → `Root`로 변경(이름이 의미와 더 잘 맞음). Splash/Tutorial → `Root` 전이 시 `popUpTo`도 `Root`로 갱신.
- `app/src/main/java/com/yslee/subwaywhen/navigation/NavRoutes.kt` — `Home` 상수를 `Root`로 교체(또는 `Root` 추가 후 `Home`은 RootScaffold 내부 자식 라우트로 의미 분리).
- `app/src/main/java/com/yslee/subwaywhen/feature/home/HomePlaceholderScreen.kt` — 사용처(`AppNavHost`) 변경으로 본 파일은 더 이상 참조되지 않음. 동일 패키지에 신규 `HomeScreen.kt`가 그 자리를 대신함. (삭제 여부는 아래 "삭제" 항목 참조.)

### 삭제
- `app/src/main/java/com/yslee/subwaywhen/feature/home/HomePlaceholderScreen.kt` — 본 변경으로 인해 호출처가 사라지는 "본 변경이 만든 고아"이므로 가이드라인 3(Surgical Changes)에 따라 함께 제거. 빈 `Box`만 있던 placeholder.

## iOS → Android 매핑

| iOS | Android | 비고 |
|-----|---------|------|
| `AppDelegate` / `SceneDelegate` → `AppCoordinator.start()` 분기 | `AppNavHost` + `SplashViewModel` (기존) | Splash 라우트에서 `isTutorialSeen`으로 분기 — 이미 구현됨, 본 spec에서 변경 없음 |
| `UITabBarController` (Root) | `RootScaffold` (Scaffold + `NavigationBar` + 내부 `NavHost`) | Compose 관용 방식 |
| `UITabBarItem(image: house, tag: 0)` | `TabRoute.Home` (아이콘 `Icons.Filled.Home`) | tag 0 → 첫번째 탭 |
| `UITabBarItem(image: magnifyingglass, tag: 1)` | `TabRoute.Search` (아이콘 `Icons.Filled.Search`) | tag 1 → 두번째 탭 |
| `UITabBarItem(image: gearshape, tag: 2)` | `TabRoute.Setting` (아이콘 `Icons.Filled.Settings`) | tag 2 → 세번째 탭 |
| `MainCoordinator.navigation` (UINavigationController) | Home 탭 내부 `NavHost`(자식 그래프) — 본 spec 범위 밖, 단일 화면만 노출 | 추후 상세 화면 push 도입 시 탭별 nested NavHost 확장 |
| `tabBar.tintColor = AppIconColor` | `NavigationBar` `selectedIconColor = AppIconColor` | 동일 토큰 재사용 |
| 탭바 배경 = `.systemBackground` | `NavigationBar` `containerColor = MaterialTheme.colorScheme.background` | Light/Dark 자동 |
| 탭 아이템에 `title: nil` (아이콘만) | `NavigationBarItem`의 `label`을 항상 노출(`alwaysShowLabel = true`) 또는 미노출(`label = null`) — Phase 2 결정 | iOS는 라벨 없음 |

## 기술적 결정사항

- **Root는 `RootScaffold`(Composable)로 구현하고, `AppNavHost`의 자식 라우트 한 개(`Root`)로 위치시킨다.**
  - 이유: 현재 `AppNavHost`가 `Splash`/`Tutorial`/`Home` 분기를 담당하고 있다. 탭바는 튜토리얼 완료 후 진입하는 화면이므로 Splash·Tutorial과 동일 레이어의 형제 라우트가 되어야 한다. `RootScaffold`는 내부에 자체 `NavHost`를 갖고 탭 간 전환을 처리한다(nested navigation).
  - 대안: `AppNavHost`에 Home/Search/Setting 라우트를 평탄하게 추가하고 Scaffold는 각 라우트별로 중복 호스팅 → 탭바 상태가 라우트마다 다시 그려져 깜빡임 발생. 채택 안 함.
  - 대안: Scaffold는 전체 앱 루트(`MainActivity.setContent`)에 두고 Splash/Tutorial에서는 `Scaffold.bottomBar`를 숨김 처리 → 분기 조건이 Scaffold 내부로 새어들어가 단방향 흐름이 깨짐. 채택 안 함.

- **`Home` 라우트명을 `Root`로 개명한다.**
  - 이유: 기존 `Home`은 "튜토리얼 종료 후 가는 화면"이라는 의미였고 그 자리에 단순 placeholder가 있었다. 본 spec에서 그 자리에 들어가는 것은 "탭바 컨테이너 전체(Root)"이지 Home 탭 한 개가 아니다. `Home`은 RootScaffold 내부 자식 라우트(첫 탭)로 의미가 좁아진다. 이름과 역할을 일치시켜야 추후 혼동이 없다.
  - 영향 범위: `AppNavHost`의 destination 정의, `SplashEffect.NavigateToHome` / `TutorialEffect.NavigateToHome`의 콜백에서 navigate하는 라우트 문자열. effect/콜백명은 그대로 두되 navigate 대상만 `NavRoutes.Root`로 변경(외부 의미상 "홈으로 간다 = 탭바를 띄운다"로 자연스러움).
  - 대안: `Home` 라우트명을 유지한 채 내부에서 Scaffold를 호스팅 → 향후 `Home`이 가리키는 것이 "탭바 전체"인지 "Home 탭"인지 모호. 채택 안 함.

- **탭 간 전환 시 백스택은 단일 인스턴스로 유지한다 (Material3 권장 패턴).**
  - `navController.navigate(tab.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }`
  - 이유: 탭을 여러 번 눌러도 백스택이 쌓이지 않고, 다른 탭에 갔다 돌아와도 스크롤/상태 복원.
  - iOS `UITabBarController`도 탭 전환 시 NavigationStack을 보존하는데, 위 패턴이 가장 가까운 대응.

- **탭바 아이콘은 Compose `Icons.Filled.Home / Search / Settings`를 사용한다.**
  - 이유: SF Symbols `house` / `magnifyingglass` / `gearshape`의 시각적 대응이 충분하고, 별도 drawable 추가 없이 사용 가능. 가이드라인 2(Simplicity First).
  - 대안: Material Symbols vector drawable을 `res/drawable/`에 별도 추가 → 추가 파일 5개의 비용 대비 시각적 이득 없음. 채택 안 함.
  - 단, `Icons.Filled.Settings`는 `material-icons-extended` 의존성 또는 기본 `material-icons-core`에 포함되어 있는지 Phase 1에서 확인. 없으면 vector drawable 추가로 전환.

- **탭 라벨 표시 여부: iOS는 라벨 없음(`title: nil`)이지만 AOS는 `NavigationBarItem`에 라벨을 표시한다.**
  - 이유: AOS Material3 디자인 가이드는 탭바 라벨을 권장. 접근성(TalkBack) 측면에서도 라벨이 있는 편이 안전. 라벨은 `strings.xml`의 `tab_home`/`tab_search`/`tab_setting`을 참조.
  - 대안: 라벨을 숨겨 iOS 시각과 일치시킴 → 접근성 손해. 채택 안 함.

- **각 탭 ViewModel은 빈 `@HiltViewModel`로 만들고 화면은 텍스트만 표시한다.**
  - spec 명시: "기능은 나중에 추가할 예정이기 때문에 View에는 해당 기능의 텍스트만 적고 ViewModel을 연동만 해둠".
  - ViewModel에 `@Inject constructor()`만 두고 상태/Intent/Effect는 정의하지 않는다. 추후 기능 추가 시 MVI(UiState/Intent/Effect) 구조를 갖출 자리만 마련.
  - 화면은 중앙에 `stringResource(R.string.home_placeholder_title)` 등을 표시하는 `Box` + `Text`로 최소화.

- **Splash → Tutorial → Root 전이 시 백스택 정리는 기존 규칙 유지.**
  - `Splash`에서 `Root`로 갈 때: `popUpTo(NavRoutes.Splash) { inclusive = true }`
  - `Tutorial`에서 `Root`로 갈 때: `popUpTo(NavRoutes.Tutorial) { inclusive = true }`
  - 이유: 사용자가 탭바 화면에서 백 버튼을 눌렀을 때 Splash/Tutorial로 돌아가면 안 됨. 기존 `AppNavHost` 동작과 동일.

- **iOS의 딥링크(`subwaywhen://station?text=`) 시 첫 탭(`tabbar.selectedIndex = 0`)으로 이동시키는 로직은 본 spec 범위 밖.**
  - 이유: spec.md에 딥링크 요구사항 없음. 향후 별도 spec에서 다룬다.

- **테스트 전략**
  - spec의 Acceptance Criteria는 "하단 탭바 존재" + "탭을 누르면 해당 화면으로 이동"이라는 시각적/네비게이션 동작이며, 각 화면은 텍스트 한 줄뿐.
  - 단위 테스트보다 **Compose Preview**(`@Preview`)로 `RootScaffold` 렌더링 + 수동 탭 확인이 비용 대비 적정. 가이드라인 2.
  - 대안: `androidx.navigation:navigation-testing` + Compose UI test → 셋업 비용 > 검증 가치. 추후 탭별 화면에 로직이 들어갈 때 도입.

- **`Dimens.kt`에 탭바 전용 토큰을 추가하지 않는다.**
  - 이유: Material3 `NavigationBar`의 기본 높이/패딩을 그대로 사용. 본 변경에서 2회 이상 반복되는 신규 여백 값이 없음(`Dimens.kt` 토큰 추가 규칙).

## 구현 순서

### Phase 1. 의존성 확인 및 기반 준비
- `Icons.Filled.Home/Search/Settings`가 현재 의존성(`material-icons-core` 또는 `compose-material3` 기본)에 포함되어 있는지 확인.
  - 미포함 시 `material-icons-extended` 추가 또는 `res/drawable/`에 vector drawable 3개 추가.
- `strings.xml`에 탭 라벨/화면 텍스트 추가:
  - `tab_home`, `tab_search`, `tab_setting`
  - `home_placeholder_title`, `search_placeholder_title`, `setting_placeholder_title`

### Phase 2. 탭 라우트 정의
- `navigation/TabRoute.kt` 신규:
  - sealed class `TabRoute(val route: String, val labelRes: Int, val icon: ImageVector)`
  - `object Home : TabRoute("tab_home", R.string.tab_home, Icons.Filled.Home)`
  - `object Search : TabRoute("tab_search", R.string.tab_search, Icons.Filled.Search)`
  - `object Setting : TabRoute("tab_setting", R.string.tab_setting, Icons.Filled.Settings)`
  - `companion object { val all = listOf(Home, Search, Setting) }`

### Phase 3. 각 탭 ViewModel + Screen
- `feature/home/HomeViewModel.kt` (신규): `@HiltViewModel class HomeViewModel @Inject constructor() : ViewModel()` — 빈 ViewModel.
- `feature/home/HomeScreen.kt` (신규): `@Composable fun HomeScreen(viewModel: HomeViewModel = hiltViewModel())` — 중앙에 `Text(stringResource(R.string.home_placeholder_title))`.
- `feature/search/SearchViewModel.kt` (신규): 동일 패턴.
- `feature/search/SearchScreen.kt` (신규): 동일 패턴.
- `feature/setting/SettingViewModel.kt` (신규): 동일 패턴.
- `feature/setting/SettingScreen.kt` (신규): 동일 패턴.

### Phase 4. RootScaffold (탭바 + 내부 NavHost)
- `navigation/RootScaffold.kt` 신규:
  - `@Composable fun RootScaffold()` — 내부에서 `rememberNavController()`로 자식 navController 생성.
  - `Scaffold(bottomBar = { NavigationBar { TabRoute.all.forEach { tab -> NavigationBarItem(...) } } })`
  - `NavigationBarItem`:
    - `selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true`
    - `onClick`: 단일 인스턴스 패턴으로 navigate (위 "기술적 결정사항" 참조)
    - `icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) }`
    - `label = { Text(stringResource(tab.labelRes)) }`
    - `colors = NavigationBarItemDefaults.colors(selectedIconColor = AppIconColor, selectedTextColor = AppIconColor, indicatorColor = MaterialTheme.colorScheme.background)`
  - `NavHost(navController = childNavController, startDestination = TabRoute.Home.route, modifier = Modifier.padding(innerPadding))`:
    - `composable(TabRoute.Home.route) { HomeScreen() }`
    - `composable(TabRoute.Search.route) { SearchScreen() }`
    - `composable(TabRoute.Setting.route) { SettingScreen() }`

### Phase 5. AppNavHost 통합 (Home → Root)
- `navigation/NavRoutes.kt` 수정: `Home` 상수 제거, `Root` 상수 추가.
- `navigation/AppNavHost.kt` 수정:
  - `composable(NavRoutes.Home) { HomePlaceholderScreen() }` 삭제, `composable(NavRoutes.Root) { RootScaffold() }` 추가.
  - `SplashEffect.NavigateToHome`/`TutorialEffect.NavigateToHome` 처리 시 navigate 대상 `NavRoutes.Home` → `NavRoutes.Root`로 일괄 교체. `popUpTo` 인자도 동일.
- `feature/home/HomePlaceholderScreen.kt` 삭제(고아 제거).

### Phase 6. Preview 검증
- `RootScaffold` 하단에 `@Preview` 추가 (Light/Dark).
- `HomeScreen`/`SearchScreen`/`SettingScreen` 각각에 `@Preview` 추가.
- Android Studio Preview로 렌더링 확인.
- 실기기/에뮬레이터에서 탭 전환 동작 확인:
  - 앱 첫 실행(튜토리얼 미완료) → 튜토리얼 → "시작하기" → 탭바 노출 + Home 탭 선택 상태
  - 탭 클릭 시 해당 화면으로 이동
  - 앱 재진입(튜토리얼 완료 후) → Splash → 탭바 노출

## 완료 조건
- [ ] Spec Acceptance Criteria 충족
  - [ ] 하단에 홈, 검색, 설정 탭바가 존재
  - [ ] 각 버튼을 누르면 해당 화면으로 이동 (텍스트로 식별 가능)
- [ ] 튜토리얼 완료 여부에 따른 Root 분기 동작 유지(기존 `SplashViewModel` 로직 회귀 없음)
- [ ] 탭바 라벨/아이콘이 `strings.xml` + Material Icons로 정의됨(매직 문자열 없음)
- [ ] 선택된 탭 tint는 `AppIconColor` 사용(iOS와 동일)
- [ ] `HomePlaceholderScreen.kt` 삭제, 호출처(`AppNavHost`)가 `RootScaffold`로 대체됨
- [ ] 빌드 성공, 기존 `TutorialViewModelTest` 회귀 없음
