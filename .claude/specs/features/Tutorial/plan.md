# Plan: Tutorial

## 참조 Spec
- @specs/features/Tutorial/spec.md

## 참조 Skill
신규 화면(`feature/tutorial`)을 생성하므로 feature 스킬의 단계별 흐름을 따른다.
- @skills/feature/SKILL.md
- @skills/feature/reference/step3-tasks.md
- @skills/feature/reference/step4-implement.md

## 현재 상태 파악

현재 Android 프로젝트는 초기 상태이다. 아래 기반 요소가 **모두 미존재**한다.

- 신규:
  - `res/values/strings.xml` — 앱 전체 문자열 리소스. iOS `Strings.swift`의 화면별 struct(`Strings.Common`, `Strings.Tutorial` 등)를 Android 네이밍 prefix로 대응 (`common_*`, `tutorial_*`, `main_*` …). 모든 화면이 이 파일을 공유하며, 코드에 문자열 리터럴을 직접 작성하지 않는다.
  - `feature/tutorial/` 패키지 전체 (Screen / ViewModel / UiState / Intent / Effect)
  - 튜토리얼 페이지 콘텐츠 모델 (`TutorialPage` 등)
  - `data/model/SaveSetting.kt` — 앱 전체 설정을 담는 data class (iOS `SaveSetting` struct 대응). 여러 기능에서 공유하므로 `feature/` 외부에 위치.
  - `data/local/SettingLocalDataSource.kt` — DataStore Preferences에서 개별 키를 읽어 `SaveSetting`으로 조립하고, `SaveSetting`을 받아 개별 키로 분해·저장. `@Inject constructor(dataStore)`로 주입받아 사용.
  - Repository 인터페이스 + 구현체 (`TutorialRepository` / `TutorialRepositoryImpl`) — `SettingLocalDataSource`를 주입받아 `tutorialSuccess` 키만 다룸
  - 앱 진입 분기를 담당하는 루트 컴포넌트 (Splash → 분기 → Tutorial / Home)
  - Navigation Compose `NavHost` 및 라우트 정의 (`Splash`, `Tutorial`, `Home`)
  - Home 화면 placeholder (튜토리얼 종료 후 이동 대상 — 본 기능 범위에서는 빈 화면 수준)
  - Hilt 셋업: `@HiltAndroidApp` Application 클래스, DI 모듈 (DataStore / Repository 바인딩)
  - Splash 화면: AndroidX SplashScreen API + 커스텀 스플래시 테마/아이콘으로 구성. `installSplashScreen()`으로 플래그 조회 완료까지 splash 유지.
  - `ui/common/` 패키지: 재사용 가능한 공통 Compose 컴포넌트 (iOS `Presentation/Common/SwiftUI/` 대응)
  - `ui/theme/Dimens.kt`: iOS `ViewStyle.swift` 대응 디자인 토큰 (cornerRadius, padding, fontSize, 애니메이션 speed/scale)
  - Version Catalog(`libs.versions.toml`) 의존성 추가, `app/build.gradle.kts` 갱신
  - 튜토리얼 이미지 리소스: iOS 원본에서 복사 (`/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Resource/Assets.xcassets/Tutorial_Img/`) → `app/src/main/res/drawable/`
  - Lottie 리소스: iOS 원본에서 복사 (`/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Resource/Lottie/Congratulations.json`, `TutorialSuccess.json`) → `app/src/main/res/raw/`
  - 테스트: `TutorialViewModel` 단위 테스트 (Turbine + MockK + Kotest)
- 재사용:
  - `ui/theme/` (SubwayWhenTheme, Color, Type) — 튜토리얼 화면 스타일에 사용
- 수정:
  - `MainActivity.kt` — Greeting 데모 제거, `@AndroidEntryPoint` 부착, `installSplashScreen()` 호출, `setContent`에서 `NavHost` 진입점 호스팅
  - `AndroidManifest.xml` — Application 클래스(`android:name`) 지정, 스플래시 테마 적용
- 삭제:
  - `MainActivity.kt` 내 `Greeting` / `GreetingPreview` (데모 코드)

## 기술적 결정사항

- **`SaveSetting` data class (`data/model/`)**: iOS `SaveSetting` struct의 Android 대응. 앱 전체 설정을 하나의 타입으로 표현. `feature/` 외부 공통 레이어에 위치하여 여러 기능에서 재사용. 현재는 `tutorialSuccess: Boolean` 외 iOS 원본 필드를 그대로 포팅하되, 아직 구현되지 않은 기능의 필드는 기본값으로 초기화.
- **로컬 저장 = DataStore Preferences (Hilt 주입)**: iOS `FixInfo.saveSetting`(전역 static)과 달리, Android는 `SettingLocalDataSource → TutorialRepository → ViewModel` 계층을 통해 모두 `@Inject constructor`로 주입받아 사용한다. 어느 계층에서도 직접 생성·전역 접근 금지. DataStore는 `SaveSetting`을 통째로 직렬화하지 않고 **필드별 개별 `PreferencesKey`로 저장**하며, `SettingLocalDataSource`가 키 ↔ `SaveSetting` 조립/분해를 담당한다.
- **진입 조건 = 플래그 단독**: iOS는 `!tutorialSuccess && saveStation.isEmpty`(역 즐겨찾기 비어있음)를 함께 본다. Android는 아직 즐겨찾기 기능이 없으므로 **플래그(`tutorialSeen == false`) 단독**으로 판단한다. 추후 즐겨찾기 도입 시 조건 확장 여지를 남기되, 지금은 추가 추상화하지 않는다.
- **스플래시 화면 = AndroidX SplashScreen API + 커스텀 테마**: iOS는 기본 Launch Screen을 사용하지만, Android는 별도로 스플래시를 구성해야 한다. `androidx.core:core-splashscreen` 라이브러리 + `res/values/themes.xml`에 `Theme.SplashScreen` 기반 테마를 만들고 앱 아이콘을 windowSplashScreenAnimatedIcon으로 설정한다. `installSplashScreen().setKeepOnScreenCondition`으로 플래그 조회 완료까지 splash를 유지하여 깜빡임 없이 분기한다.
- **분기 로직 위치 = 루트 ViewModel(또는 Splash 라우트의 ViewModel)**: `MainActivity`에서 직접 분기하지 않고, 시작 라우트(`Splash`)에서 플래그를 읽어 `Tutorial` 또는 `Home`으로 이동시킨다. MVI 단방향 흐름 유지.
- **튜토리얼 UI = Compose `HorizontalPager`**: iOS의 `UICollectionViewCompositionalLayout(paging)` 대응. 페이지 인덱스 = UiState, "다음" 버튼 탭 = Intent. 마지막 페이지에서 "시작하기" 버튼 → 종료 처리.
- **튜토리얼 종료 처리(불변 조건: 종료 후 홈)**: "시작하기" Intent 수신 → Repository로 플래그 저장 → `NavigateToHome` SideEffect 발행(SharedFlow) → `NavHost`에서 `Home`으로 이동하며 `Tutorial` 백스택 제거(`popUpTo(Splash) { inclusive = true }` 또는 `Tutorial` inclusive).
- **에러 처리(무엇이 잘못될 수 있는가: 로컬 값 로드 실패)**: 플래그 조회 실패 시 안전 기본값으로 **"튜토리얼 미시청"으로 간주하지 않고 홈으로 보낸다 vs 튜토리얼을 보여준다** 중 — spec의 AC("한 번 본 사람은 다시 보면 안 됨")를 더 중시하여, 조회 실패 시 **이미 시청한 것으로 간주(홈으로)** 한다. 즉 read 실패는 `true` fallback. (반대 선택지: 처음 사용자 경험 보장을 위해 `false` fallback — 채택하지 않음. 재노출 리스크가 더 큼.)
- **페이지 콘텐츠**: iOS `TutorialModel.createTutorialList()`의 7개 페이지(환영 → 5개 안내 → 시작하기) 텍스트/버튼 라벨을 그대로 포팅. 이미지는 iOS 원본(`/…/Tutorial_Img/Tutorial_One.imageset/` 등)에서 적절한 밀도의 PNG를 복사하여 `app/src/main/res/drawable/`에 배치한다. (다크 모드 이미지는 `drawable-night/`에 별도 배치)
- **공통 Compose 컴포넌트 (`ui/common/`)**: iOS `Presentation/Common/SwiftUI/`에 대응하는 재사용 Compose 컴포넌트를 저장하는 패키지. 튜토리얼에서 사용하는 아래 컴포넌트를 먼저 정의하고, 이후 기능 개발 시 추가한다:
  - `MainBgCard` (iOS `MainStyleViewInSUI` / `MainStyleUIView` 대응): `MainColor` 배경 + `cornerRadius`를 가진 Card 컨테이너 컴포저블
  - `PrimaryButton` (iOS `ModalCustomButton` 대응): 텍스트 + 탭 시 `Dimens.animationScale` scale 애니메이션 버튼. **`containerColor: Color` 파라미터를 받아 호출 측에서 색상을 지정**한다 (iOS `ModalCustomButton(bgColor:)` 대응). 첫/중간 페이지는 `AppIconColor`, 마지막 페이지는 `MainColor`로 호출.
- **String 리소스 = 앱 전체 공유 `strings.xml`**: iOS `Strings.swift`와 동일하게 모든 화면의 문자열을 `res/values/strings.xml` 한 곳에서 관리한다. 코드에 문자열 리터럴 직접 작성 금지. iOS struct 구조(`Strings.Common`, `Strings.Tutorial` …)는 Android에서 네이밍 prefix로 대응한다:
  - `common_*` ← `Strings.Common` (공통: 취소, 확인, 닫기 등)
  - `tutorial_*` ← Tutorial 화면 전용
  - `main_*` ← `Strings.Main`, `setting_*` ← `Strings.Setting` 등 화면 추가 시 동일 규칙으로 확장
  Compose에서는 `stringResource(R.string.*)` 로 참조한다.
- **고정 헤더 타이틀**: iOS `TutorialVC`의 `TitleView`("지하철 민실씨를 \n설치해주셔서 감사합니다.")에 대응하는 고정 헤더가 `HorizontalPager` 위에 항상 표시된다. 마지막 페이지 진입 시 이 헤더의 텍스트 색상도 흰색으로 전환, 이탈 시 원복.
- **페이지 타입 3종 분리**: iOS와 동일하게 첫/중간/마지막 페이지를 별도 컴포저블로 구현한다.
  - `TutorialFirstPage`: `MainColor` 배경 → 그 위에 Lottie(`Congratulations`) 전체를 덮음 → Lottie 위에 환영 텍스트(중앙) + `PrimaryButton(containerColor=AppIconColor)`(하단) 가 fade-in.
  - `TutorialMiddlePage` (index 1~5): 상단 제목 + `MainBgCard` 내부에 이미지 + `PrimaryButton(containerColor=AppIconColor)`.
  - `TutorialLastPage`: Lottie(`TutorialSuccess`)가 초기에 `offset(y = -110.dp)` 상태로 시작 → 재생 완료 후 `offset(y = 0)`으로 **아래로** 슬라이드 → 버튼(`PrimaryButton(containerColor=MainColor)`) fade-in.
  `TutorialPage` 모델은 `type: TutorialPageType(First / Middle / Last)`로 구분하거나, sealed class로 정의한다.
- **마지막 페이지 배경색 전환**: 마지막 페이지(index 6) 진입 시 화면 전체 배경 + 헤더 영역이 `AppIconColor`로 `animateColorAsState` 전환, 헤더 텍스트도 흰색으로 변경. 이탈 시 `systemBackground`·기본 텍스트 색상으로 원복. `currentIndex`로 분기.
- **스와이프 비활성화**: `HorizontalPager(userScrollEnabled = false)` — 버튼으로만 페이지 이동.
- **Lottie**: iOS 원본 `Congratulations.json` / `TutorialSuccess.json`을 `res/raw/`에 복사하여 사용. `libs.versions.toml`에 Lottie for Android 의존성 추가.
- **`ui/theme/Dimens.kt`**: iOS `ViewStyle.swift` 디자인 토큰을 Android `object`로 정의. `cornerRadius=15.dp`, `paddingLR=20.dp`, `paddingTB=7.5.dp`, `animationSpeed=0.25`, `animationScale=0.94`. `ui/common/`의 컴포넌트들이 이 값을 참조.
- **Hilt 도입**: conventions에 DI=Hilt 명시. 본 기능에서 Application 클래스 + DataStore/Repository 모듈을 최소 범위로 추가한다.
- **테스트 범위**: `TutorialViewModel`만 단위 테스트(플래그 read 분기, "시작하기" 시 플래그 저장 + NavigateToHome effect, read 실패 시 홈 분기). DataStore 실제 I/O는 가짜 Repository로 대체. Compose UI 테스트는 범위 외.

## 구현 순서

### Phase 1. 프로젝트 기반 (의존성 / DI / 진입점)
- `libs.versions.toml`에 의존성 추가: Hilt, DataStore Preferences, Navigation Compose, AndroidX SplashScreen, lifecycle-viewmodel-compose, **Lottie for Android**, (테스트) Turbine·MockK·Kotest·coroutines-test. KSP 플러그인.
- `app/build.gradle.kts` 갱신: 위 의존성 적용, Hilt/KSP 플러그인, `buildFeatures` 유지.
- `SubwayWhenApplication`(`@HiltAndroidApp`) 추가, `AndroidManifest.xml`에 `android:name` 및 스플래시 테마 지정.
- 스플래시 테마 작성: `res/values/themes.xml`에 `Theme.SplashScreen` 기반 `Theme.SubwayWhen.Splash` 정의 (windowSplashScreenAnimatedIcon = 앱 아이콘). 다크 모드용 `res/values-night/themes.xml` 별도 작성.
- `MainActivity` 수정: `@AndroidEntryPoint`, `installSplashScreen().setKeepOnScreenCondition { !isReady }` 패턴으로 데이터 조회 동안 splash 유지, `setContent`에서 앱 루트 컴포저블 호스팅. `Greeting`/`GreetingPreview` 삭제.
- DI 모듈: `DataStore<Preferences>` 제공 모듈, Repository 바인딩 모듈.

### Phase 2. 데이터 레이어 (로컬 저장)

계층 구조: `DataStore` → `SettingLocalDataSource` → `TutorialRepository` → `TutorialViewModel`
모든 계층은 Hilt `@Inject constructor`로 주입받아 사용한다. 직접 접근(싱글톤 object, companion object 등) 금지.

- **`SaveSetting` data class** (`data/model/SaveSetting.kt`): iOS `SaveSetting` struct 대응. 앱 전체 설정 필드를 보유. 기본값은 iOS `init()` 기준으로 동일하게 설정. (예: `tutorialSuccess = false`)
- **DI 모듈 (`AppModule`)**: `DataStore<Preferences>` 인스턴스를 `@Singleton @Provides`로 제공. Application 수명과 동일한 단일 인스턴스를 Hilt가 관리.
- **`SettingLocalDataSource`** (`data/local/`, `@Singleton`): `@Inject constructor(private val dataStore: DataStore<Preferences>)`로 주입받음. `SaveSetting`의 각 필드에 대응하는 `PreferencesKey`를 `companion object`에 정의. `getSaveSetting(): Flow<SaveSetting>` (모든 키를 읽어 조립) / `updateTutorialSeen(value: Boolean): suspend` (해당 키만 갱신). 읽기 실패 시 `catch { emit(SaveSetting()) }` fallback (기본값 반환).
- **`TutorialRepository`** 인터페이스: `isTutorialSeen(): Flow<Boolean>`, `markTutorialSeen(): suspend`.
- **`TutorialRepositoryImpl`** (`@Singleton`): `@Inject constructor(private val dataSource: SettingLocalDataSource)`로 주입받아 `tutorialSuccess` 키만 다룸. Hilt 모듈에서 `TutorialRepository → TutorialRepositoryImpl` 바인딩.

### Phase 3. 화면 진입 분기 (Splash → Tutorial / Home)
- Navigation 라우트 정의: `Splash`, `Tutorial`, `Home`.
- `AppNavHost`(루트 컴포저블): `startDestination = Splash`.
- Splash 라우트: 진입 시 ViewModel이 `isTutorialSeen()` 조회 → `true`면 `Home`으로, `false`면 `Tutorial`로 navigate, `popUpTo(Splash){ inclusive = true }`. 조회 동안 splash 유지(`setKeepOnScreenCondition`)와 연동.
- `Home` 라우트: placeholder Compose 화면(빈/간단한 텍스트). 추후 메인 기능에서 대체.

### Phase 4. 디자인 토큰 + 공통 컴포넌트
**`ui/theme/Dimens.kt`** (iOS `ViewStyle.swift` 대응):
- `cornerRadius = 15.dp`, `paddingLR = 20.dp`, `paddingTB = 7.5.dp`
- `animationSpeed = 0.25f`, `animationScale = 0.94f`
- `fontSize*` (superSmall=9, mediumSmall=11, small=13, medium=15, large=17, mainTitleMedium=21, mainTitle=23, bigTitle=27)

**`ui/common/`** (iOS `Presentation/Common/SwiftUI/` 대응), `Dimens`를 참조:
- `MainBgCard`: `MainColor` 배경 + `Dimens.cornerRadius`를 가진 컨테이너 컴포저블 (iOS `MainStyleViewInSUI` 대응)
- `PrimaryButton`: 텍스트 + `MainColor` 배경 + 탭 시 `Dimens.animationScale` scale 애니메이션 버튼 컴포저블 (iOS `ModalCustomButton` 대응)
- 이후 기능 추가 시 공통 컴포넌트는 이 패키지에 계속 추가한다.

### Phase 5. Tutorial 화면 (MVI: feature/tutorial)
**리소스 복사 및 정의:**
- 이미지: iOS 원본 → `app/src/main/res/drawable/tutorial_one.png` ~ `tutorial_five.png`. 다크 모드는 `drawable-night/`.
- Lottie: `Congratulations.json`, `TutorialSuccess.json` → `app/src/main/res/raw/`.
- 문자열: `strings.xml`에 `common_*` (공통 문자열) + `tutorial_*` (튜토리얼 전용 — 페이지 제목 7개, 버튼 라벨 7개, 헤더 타이틀) 섹션을 정의. 이후 화면 추가 시 동일 파일에 해당 prefix 섹션 추가.

**모델:**
- `TutorialPageType`: sealed class — `First` / `Middle(imageRes: Int, titleRes: Int)` / `Last`
- `TutorialPage(type, buttonLabelRes: Int)` + 7개 페이지 목록 상수 (문자열은 `R.string.*` 참조)

**MVI:**
- `TutorialUiState`: sealed interface — `Success(pages, currentIndex, isLastPage: Boolean)`. `isLastPage`로 배경색 전환 분기.
- `TutorialIntent`: `NextClicked`, `PageChanged(index)`, `FinishClicked`.
- `TutorialEffect`: `NavigateToHome`.
- `TutorialViewModel`(`@HiltViewModel`): `PageChanged` 수신 시 `currentIndex`와 `isLastPage` 갱신. `FinishClicked` 시 `repository.markTutorialSeen()` 호출 후 `NavigateToHome` 발행.

**UI (`TutorialScreen`):**
- 상단: 고정 헤더(`stringResource(R.string.tutorial_header_title)`) — `currentIndex`에 따라 텍스트 색상 `animateColorAsState`: 마지막 → 흰색, 그 외 → 기본.
- 하단: `HorizontalPager(userScrollEnabled = false)` — 스와이프 비활성화, 버튼으로만 이동.
- 화면 전체 배경색 `animateColorAsState`: 마지막 페이지 → `AppIconColor`, 그 외 → `systemBackground`.
- 페이지 타입에 따라 컴포저블 분기:
  - `TutorialFirstPageContent`: `MainColor` 배경 → Lottie(`congratulations`) 전체 덮음 → 환영 텍스트(중앙 fade-in) + `PrimaryButton(containerColor=AppIconColor)`(하단 fade-in)
  - `TutorialMiddlePageContent`: 상단 제목(`stringResource`) + `MainBgCard`(이미지 + `PrimaryButton(containerColor=AppIconColor)`)
  - `TutorialLastPageContent`: Lottie(`tutorial_success`) 초기 `offset(y=-110.dp)` → 재생 완료 후 **아래로** 슬라이드(`offset(y=0)`) → `PrimaryButton(containerColor=MainColor)` fade-in
- `Effect` 수집 → `onNavigateToHome` 콜백 호출.

**Navigation:**
- `Tutorial` 라우트 → `TutorialScreen` 호스팅. `onNavigateToHome` → `popUpTo(Tutorial){ inclusive = true }` 후 `Home`.

### Phase 6. 테스트
- `TutorialViewModelTest` (JUnit + Kotest + Turbine + MockK):
  - "시작하기" Intent → `repository.markTutorialSeen()` 호출 검증 + `NavigateToHome` effect 방출.
  - `NextClicked`/`PageChanged` → 현재 인덱스 갱신 검증.
  - (분기 ViewModel을 별도로 둔 경우) Splash ViewModel: `isTutorialSeen()==true` → Home 라우트, `false` → Tutorial 라우트, 조회 throw → Home 라우트(`true` fallback).

## 완료 조건
- [ ] Spec Acceptance Criteria 충족
  - [ ] 튜토리얼 마지막 단계("시작하기") 후 홈 화면이 표시된다
  - [ ] 스플래시 이후 화면(튜토리얼 또는 홈)이 정상 표출된다
  - [ ] 튜토리얼을 한 번 본 사용자는 앱 재진입 시 튜토리얼이 표시되지 않고 바로 홈으로 진입한다
- [ ] 빌드 성공
- [ ] `TutorialViewModel` 단위 테스트 통과
- [ ] `MainActivity`의 데모 코드(`Greeting`) 제거, Hilt/SplashScreen 적용 반영
