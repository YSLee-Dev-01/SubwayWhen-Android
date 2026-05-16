# Tasks: Tutorial

## 참조
- spec: `.claude/specs/features/Tutorial/spec.md`
- plan: `.claude/specs/features/Tutorial/plan.md`

## Task 목록

### Phase 1. 프로젝트 기반 (의존성 / DI / 진입점)

#### [x] Task 1 — `libs.versions.toml` (수정)
**파일**: `gradle/libs.versions.toml`
- Hilt 버전 및 라이브러리 항목 추가 (`hilt-android`, `hilt-compiler`, KSP 플러그인)
- DataStore Preferences 버전 및 라이브러리 항목 추가
- Navigation Compose 버전 및 라이브러리 항목 추가
- AndroidX SplashScreen(`core-splashscreen`) 버전 및 라이브러리 항목 추가
- `lifecycle-viewmodel-compose` 버전 및 라이브러리 항목 추가
- Lottie for Android 버전 및 라이브러리 항목 추가
- 테스트용 항목 추가: Turbine, MockK, Kotest(`kotest-runner-junit5`, `kotest-assertions-core`), `kotlinx-coroutines-test`

---

#### [x] Task 2 — `app/build.gradle.kts` (수정)
**파일**: `app/build.gradle.kts`
- `plugins` 블록에 Hilt, KSP 플러그인 적용
- `dependencies` 블록에 Task 1에서 추가한 라이브러리 참조 적용
- `buildFeatures` 기존 설정 유지

---

#### [x] Task 3 — `SubwayWhenApplication.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/SubwayWhenApplication.kt`
- `Application`을 상속하고 `@HiltAndroidApp` 어노테이션 부착

---

#### [x] Task 4 — `AndroidManifest.xml` (수정)
**파일**: `app/src/main/AndroidManifest.xml`
- `<application>` 태그에 `android:name=".SubwayWhenApplication"` 지정
- `<activity>` 태그에 `android:theme="@style/Theme.SubwayWhen.Splash"` 스플래시 테마 지정

---

#### [x] Task 5 — `themes.xml` (신규)
**파일**: `app/src/main/res/values/themes.xml`
- `Theme.SplashScreen` 기반으로 `Theme.SubwayWhen.Splash` 테마 정의
- `windowSplashScreenAnimatedIcon`에 앱 아이콘 지정
- 스플래시 종료 후 전환할 테마(`postSplashScreenTheme`) 지정

---

#### [x] Task 6 — `themes.xml` (다크 모드, 신규)
**파일**: `app/src/main/res/values-night/themes.xml`
- 다크 모드용 `Theme.SubwayWhen.Splash` 테마 정의

---

#### [x] Task 7 — `MainActivity.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/MainActivity.kt`
- `@AndroidEntryPoint` 어노테이션 부착
- `Greeting` / `GreetingPreview` 데모 코드 삭제
- `installSplashScreen().setKeepOnScreenCondition { !isReady }` 패턴으로 플래그 조회 완료까지 스플래시 유지
- `setContent`에서 `AppNavHost` 루트 컴포저블 호스팅

---

#### [x] Task 8 — `AppModule.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/AppModule.kt`
- `@Module`, `@InstallIn(SingletonComponent::class)` 선언
- `@Singleton @Provides`로 `DataStore<Preferences>` 인스턴스 제공 (Application context 사용)

---

#### [x] Task 9 — `RepositoryModule.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/di/RepositoryModule.kt`
- `@Module`, `@InstallIn(SingletonComponent::class)` 선언
- `@Binds`로 `TutorialRepository → TutorialRepositoryImpl` 바인딩

---

### Phase 2. 데이터 레이어 (로컬 저장)

#### [x] Task 10 — `SaveSetting.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/model/SaveSetting.kt`
- iOS `SaveSetting` struct 대응 data class 정의
- 앱 전체 설정 필드 보유 (iOS `init()` 기준 기본값 동일하게 설정)
- `tutorialSuccess: Boolean = false` 필드 포함

---

#### [x] Task 11 — `SettingLocalDataSource.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/local/SettingLocalDataSource.kt`
- `@Singleton`, `@Inject constructor(private val dataStore: DataStore<Preferences>)` 선언
- `companion object`에 `SaveSetting` 각 필드에 대응하는 `PreferencesKey` 정의
- `getSaveSetting(): Flow<SaveSetting>` — 모든 키를 읽어 `SaveSetting`으로 조립, 읽기 실패 시 `catch { emit(SaveSetting()) }` fallback (기본값 반환)
- `updateTutorialSeen(value: Boolean): suspend` — `tutorialSuccess` 키만 갱신

---

#### [x] Task 12 — `TutorialRepository.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/repository/TutorialRepository.kt`
- 인터페이스 정의
- `isTutorialSeen(): Flow<Boolean>` 선언
- `markTutorialSeen(): suspend` 선언

---

#### [x] Task 13 — `TutorialRepositoryImpl.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/data/repository/TutorialRepositoryImpl.kt`
- `@Singleton`, `@Inject constructor(private val dataSource: SettingLocalDataSource)` 선언
- `TutorialRepository` 구현
- `isTutorialSeen()`: `dataSource.getSaveSetting()`에서 `tutorialSuccess` 필드만 추출
- `markTutorialSeen()`: `dataSource.updateTutorialSeen(true)` 위임

---

### Phase 3. 화면 진입 분기 (Splash → Tutorial / Home)

#### [x] Task 14 — `NavRoutes.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/NavRoutes.kt`
- `Splash`, `Tutorial`, `Home` 라우트 문자열 상수 정의

---

#### [x] Task 15 — `SplashViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/splash/SplashViewModel.kt`
- `@HiltViewModel`, `@Inject constructor(private val repository: TutorialRepository)` 선언
- `isTutorialSeen()` 조회 후 결과에 따라 `NavigateToTutorial` 또는 `NavigateToHome` Effect 발행 (SharedFlow)
- 조회 실패(throw) 시 홈으로 분기 (`true` fallback)
- `isReady: StateFlow<Boolean>` — 조회 완료 여부를 `MainActivity`의 `setKeepOnScreenCondition`과 연동

---

#### [x] Task 16 — `AppNavHost.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/navigation/AppNavHost.kt`
- `NavHost(startDestination = NavRoutes.Splash)` 정의
- `Splash` 라우트: `SplashViewModel` Effect 수집 → `true`면 `Home`으로, `false`면 `Tutorial`로 navigate, `popUpTo(Splash){ inclusive = true }`
- `Tutorial` 라우트: `TutorialScreen` 호스팅, `onNavigateToHome` 콜백 — `popUpTo(Tutorial){ inclusive = true }` 후 `Home`으로 이동
- `Home` 라우트: placeholder 컴포저블 호스팅

---

#### [x] Task 17 — `HomePlaceholderScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/home/HomePlaceholderScreen.kt`
- 빈 화면 수준의 placeholder Compose 화면 정의 (추후 메인 기능에서 대체)

---

### Phase 4. 디자인 토큰 + 공통 컴포넌트

#### [x] Task 18 — `Dimens.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/theme/Dimens.kt`
- iOS `ViewStyle.swift` 대응 디자인 토큰을 `object`로 정의
- `cornerRadius = 15.dp`, `paddingLR = 20.dp`, `paddingTB = 7.5.dp`
- `animationSpeed = 0.25f`, `animationScale = 0.94f`
- fontSize 토큰: `fontSizeSuperSmall=9.sp`, `fontSizeMediumSmall=11.sp`, `fontSizeSmall=13.sp`, `fontSizeMedium=15.sp`, `fontSizeLarge=17.sp`, `fontSizeMainTitleMedium=21.sp`, `fontSizeMainTitle=23.sp`, `fontSizeBigTitle=27.sp`

---

#### [x] Task 19 — `MainBgCard.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/MainBgCard.kt`
- iOS `MainStyleViewInSUI` 대응 컨테이너 컴포저블
- `MainColor` 배경 + `Dimens.cornerRadius` 적용
- `content` 슬롯 파라미터로 내부 콘텐츠를 받음

---

#### [x] Task 20 — `PrimaryButton.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/PrimaryButton.kt`
- iOS `ModalCustomButton` 대응 버튼 컴포저블
- `text: String`, `containerColor: Color`, `onClick: () -> Unit` 파라미터 정의
- 탭 시 `Dimens.animationScale` scale 애니메이션 적용

---

### Phase 5. Tutorial 화면 (MVI: feature/tutorial)

#### [x] Task 21 — 리소스 복사 (이미지 / Lottie)
**파일**: `app/src/main/res/drawable/`, `app/src/main/res/drawable-night/`, `app/src/main/res/raw/`
- iOS 원본(`/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Resource/Assets.xcassets/Tutorial_Img/`)에서 적절한 밀도 PNG 복사
  - `tutorial_one.png` ~ `tutorial_five.png` → `app/src/main/res/drawable/`
  - 다크 모드 이미지 → `app/src/main/res/drawable-night/`
- iOS 원본(`/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Resource/Lottie/`)에서 Lottie JSON 복사
  - `Congratulations.json` → `app/src/main/res/raw/congratulations.json`
  - `TutorialSuccess.json` → `app/src/main/res/raw/tutorial_success.json`

---

#### [x] Task 22 — `strings.xml` (신규)
**파일**: `app/src/main/res/values/strings.xml`
- `common_*` 섹션: 취소, 확인, 닫기 등 공통 문자열
- `tutorial_*` 섹션: 헤더 타이틀 1개, 페이지 제목 7개, 버튼 라벨 7개 (iOS `TutorialModel.createTutorialList()` 기준 텍스트 포팅)

---

#### [x] Task 23 — `TutorialPageModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/tutorial/TutorialPageModel.kt`
- `TutorialPageType` sealed class 정의: `First`, `Middle(imageRes: Int, titleRes: Int)`, `Last`
- `TutorialPage(type: TutorialPageType, buttonLabelRes: Int)` data class 정의
- 7개 페이지 목록 상수 정의 (문자열은 `R.string.*` 참조)

---

#### [x] Task 24 — `TutorialContract.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/tutorial/TutorialContract.kt`
- `TutorialUiState`: sealed interface — `Success(pages, currentIndex: Int, isLastPage: Boolean)`
- `TutorialIntent`: sealed interface — `NextClicked`, `PageChanged(index: Int)`, `FinishClicked`
- `TutorialEffect`: sealed interface — `NavigateToHome`

---

#### [x] Task 25 — `TutorialViewModel.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/tutorial/TutorialViewModel.kt`
- `@HiltViewModel`, `@Inject constructor(private val repository: TutorialRepository)` 선언
- `uiState: StateFlow<TutorialUiState>` — 초기값 `Success(pages=7개 목록, currentIndex=0, isLastPage=false)`
- `effect: SharedFlow<TutorialEffect>` 정의
- `onIntent(intent: TutorialIntent)` 단일 진입점
  - `NextClicked`: `currentIndex + 1`로 `PageChanged` 처리 위임
  - `PageChanged(index)`: `currentIndex`와 `isLastPage(index == 6)` 갱신
  - `FinishClicked`: `viewModelScope.launch`에서 `repository.markTutorialSeen()` 호출 후 `NavigateToHome` Effect 발행

---

#### [x] Task 26 — `TutorialFirstPageContent.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/tutorial/TutorialFirstPageContent.kt`
- `MainColor` 배경 전체
- Lottie(`congratulations`) 화면 전체 덮음
- 환영 텍스트 중앙 배치, fade-in 애니메이션
- `PrimaryButton(containerColor = AppIconColor)` 하단 배치, fade-in 애니메이션

---

#### [x] Task 27 — `TutorialMiddlePageContent.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/tutorial/TutorialMiddlePageContent.kt`
- `Middle(imageRes, titleRes)` 타입 파라미터 수신
- 상단 제목 (`stringResource(titleRes)`)
- `MainBgCard` 내부에 이미지 + `PrimaryButton(containerColor = AppIconColor)` 배치

---

#### [x] Task 28 — `TutorialLastPageContent.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/tutorial/TutorialLastPageContent.kt`
- Lottie(`tutorial_success`) 초기 `offset(y = -110.dp)` 상태 시작
- 재생 완료 후 `offset(y = 0)`으로 아래로 슬라이드 애니메이션
- `PrimaryButton(containerColor = MainColor)` fade-in

---

#### [x] Task 29 — `TutorialScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/feature/tutorial/TutorialScreen.kt`
- `TutorialViewModel` 주입, `uiState` / `effect` 수집
- 화면 전체 배경색: `animateColorAsState` — 마지막 페이지(`isLastPage=true`) → `AppIconColor`, 그 외 → `systemBackground`
- 상단 고정 헤더: `stringResource(R.string.tutorial_header_title)` 표시, `animateColorAsState` — 마지막 페이지 → 흰색 텍스트, 그 외 → 기본 텍스트 색상
- `HorizontalPager(userScrollEnabled = false)` — 버튼으로만 페이지 이동
- 페이지 타입에 따라 `TutorialFirstPageContent` / `TutorialMiddlePageContent` / `TutorialLastPageContent` 분기
- `Effect` 수집 → `onNavigateToHome` 콜백 호출
- `onNavigateToHome: () -> Unit` 파라미터 정의

---

### Phase 6. 테스트

#### [x] Task 30 — `TutorialViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/tutorial/TutorialViewModelTest.kt`
- MockK로 `TutorialRepository` 가짜 구현 주입
- Kotest + Turbine + JUnit 조합으로 작성
- 테스트 케이스:
  - `FinishClicked` Intent → `repository.markTutorialSeen()` 호출 검증 + `NavigateToHome` Effect 방출 검증
  - `NextClicked` Intent → `currentIndex` 1 증가 검증
  - `PageChanged(index)` Intent → `currentIndex` 갱신 + `isLastPage` 올바른 분기 검증 (index=6일 때 `true`, 그 외 `false`)

---

#### [x] Task 31 — `SplashViewModelTest.kt` (신규)
**파일**: `app/src/test/java/com/yslee/subwaywhen/feature/splash/SplashViewModelTest.kt`
- MockK로 `TutorialRepository` 가짜 구현 주입
- 테스트 케이스:
  - `isTutorialSeen()` == `true` → `NavigateToHome` Effect 방출 검증
  - `isTutorialSeen()` == `false` → `NavigateToTutorial` Effect 방출 검증
  - `isTutorialSeen()` throw → `NavigateToHome` Effect 방출 검증 (`true` fallback)

---

## 체크리스트

### 품질 (DoD)
- [x] 빌드 성공
- [x] `TutorialViewModel` 단위 테스트 통과
- [x] `SplashViewModel` 단위 테스트 통과
- [x] `MainActivity` 데모 코드(`Greeting` / `GreetingPreview`) 제거 확인

### 기능 (AC)
- [x] 튜토리얼 마지막 단계("시작하기") 후 홈 화면이 표시된다
- [x] 스플래시 이후 화면(튜토리얼 또는 홈)이 정상 표출된다
- [x] 튜토리얼을 한 번 본 사용자는 앱 재진입 시 튜토리얼이 표시되지 않고 바로 홈으로 진입한다
