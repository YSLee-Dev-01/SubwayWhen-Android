# Plan: CommonView

## 참조 Spec
- @specs/features/CommonView/spec.md

## 참조 iOS 원본
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Common/ViewStyle.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Common/SwiftUI/`
  - `OffsetScrollViewInSUI.swift`
  - `NavigationBarInSUI.swift`
  - `NavigationBarScrollViewInSUI.swift`
  - `UpDownExceptionViewInSUI.swift`
  - `MainStyleViewInSUI.swift`
  - `StationTitleViewInSUI.swift`
  - `AnimationButtonInSUI.swift`
  - `ExpandedViewInSUI.swift`
  - `TriangleInSUI.swift`
- `/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhen/Presentation/Common/UIKit/`
  - `ModalVCCustom.swift`
  - `Sub/ModalCustomButton.swift`
  - `Sub/ModalSubCustomButton.swift`

## 현재 상태 파악
- 신규:
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/StationLineCircle.kt` — iOS `StationTitleViewInSUI` 대응 (호선 컬러 원형 + 텍스트)
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/AnimatedTapBox.kt` — iOS `AnimationButtonInSUI` 대응 (눌림 시 scale + 배경색 전환 컨테이너)
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/CommonTopBar.kt` — iOS `NavigationBarInSUI` 대응 (커스텀 상단바)
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/CommonTopBarScreen.kt` — iOS `NavigationBarScrollViewInSUI` 대응 (CommonTopBar + 스크롤 대형 타이틀 결합 스캐폴드)
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/UpDownExceptionRow.kt` — iOS `UpDownExceptionViewInSUI` 대응 (상/하행 + 제외역 표시 행)
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/TriangleShape.kt` — iOS `Triangle` 대응 (커스텀 삼각형 `androidx.compose.ui.graphics.Shape`)
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/modal/CommonModalBottomSheet.kt` — iOS `ModalVCCustom` 대응 (Material3 `ModalBottomSheet` 래퍼)
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/modal/ModalSubButton.kt` — iOS `ModalSubCustomButton` 대응 (서브 버튼 스타일)
- 재사용:
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/MainBgCard.kt` — iOS `MainStyleViewInSUI` 대응. 이미 구현되어 있어 그대로 사용.
  - `app/src/main/java/com/yslee/subwaywhen/ui/common/PrimaryButton.kt` — iOS `ModalCustomButton` 대응. 이미 구현되어 있어 그대로 사용.
  - `app/src/main/java/com/yslee/subwaywhen/ui/theme/Color.kt` — `MainColorLight`/`MainColorDark` 등 컬러 토큰.
- 수정:
  - `app/src/main/java/com/yslee/subwaywhen/ui/theme/Dimens.kt` — iOS `ViewStyle` 대응 토큰. `paddingTB` 불일치 확인 후 정합:
    - iOS `ViewStyle.padding.mainStyleViewTB` = 7.5pt → 현재 Android `paddingTB` = 13.dp (의도적 변경인지 오기인지 확인 필요)
- 삭제: 없음

## iOS → Android 컴포넌트 매핑

| iOS (원본) | Android (신규/재사용) | 비고 |
|------------|-----------------------|------|
| `MainStyleViewInSUI` | `MainBgCard` (재사용) | 기 구현 |
| `ModalCustomButton` | `PrimaryButton` (재사용) | 기 구현 |
| `StationTitleViewInSUI` | `StationLineCircle` (신규) | 호선 색을 `String` 키 대신 `Color`/`LineColorToken`으로 받음 (iOS의 `Color.init(name)`은 AOS에서 매퍼 도입 전 단계라 호출부에서 색을 주입) |
| `AnimationButtonInSUI` | `AnimatedTapBox` (신규) | `MutableInteractionSource` + `collectIsPressedAsState` + `animateFloatAsState` 조합 (PrimaryButton과 같은 패턴) |
| `NavigationBarInSUI` | `CommonTopBar` (신규) | 좌측 back 아이콘, 우측 trailing 아이콘, 스크롤 시 노출되는 sub-title의 fade+slide 애니메이션 포함 |
| `NavigationBarScrollViewInSUI` | `CommonTopBarScreen` (신규) | `CommonTopBar` + `LazyColumn` (또는 `Column` + `verticalScroll`)로 대형 타이틀과 본문을 감싸는 스캐폴드 |
| `OffsetScrollViewInSUI` + `ScrollOffsetKey` | 별도 컴포넌트 없음 — `CommonTopBarScreen` 내부에 흡수 | AOS는 `LazyListState.firstVisibleItemScrollOffset` / `firstVisibleItemIndex`로 임계값 도달 여부를 직접 계산 가능. 외부에 노출할 가치가 없으므로 별도 컴포넌트로 만들지 않고 스캐폴드 내부 상태로 처리 (spec: "AOS에서는 다른 방법으로 감지할 경우 제외") |
| `ExpandedViewInSUI` | 제외 | Compose에서는 `Modifier.weight(1f)` / `Modifier.fillMaxWidth()` + `horizontalArrangement = Arrangement.Start/End/Center`로 동일 효과를 한 줄로 표현 가능. 단일 책임 컴포저블로 감싸는 가치가 없음 (spec: "AOS에서는 다른 방법으로 사용할 경우 제외") |
| `UpDownExceptionViewInSUI` | `UpDownExceptionRow` (신규) | 좌측 상/하행 라벨 박스 + 우측 제외역 표시 박스를 Row로 배치, 각 박스는 `MainBgCard`로 감쌈 |
| `Triangle` (커스텀 Shape) | `TriangleShape` (신규) | `androidx.compose.ui.graphics.Shape` 구현체. `Path.moveTo/lineTo/close`로 위쪽 꼭짓점 → 좌하 → 우하 삼각형 생성 |
| `ModalVCCustom` (UIKit) | `CommonModalBottomSheet` (신규) | Material3 `ModalBottomSheet` 위에 메인 타이틀 / 서브 타이틀 / (선택적) 확인 버튼 슬롯을 노출. iOS의 핸드바·드래그 dismiss·키보드 추적·탭바 숨김 등은 Material3 `ModalBottomSheet`가 표준 동작으로 제공하므로 추가 구현 불필요 |
| `ModalSubCustomButton` (UIKit) | `ModalSubButton` (신규) | 모달 내부 보조 버튼. cornerRadius 15dp + 본문 텍스트, 색은 호출부에서 지정 |
| `ViewStyle.swift` 토큰 | `Dimens.kt` (수정) | fontSize / radius / animation 토큰은 일치. `paddingTB`: iOS 7.5pt → 현 Android 13.dp — 불일치 확인 후 정합 필요 |

## 기술적 결정사항

- **`Dimens.kt` 토큰 추가 규칙**: ViewStyle과 실제 View 간의 여백·크기 값이 다를 수 있음
  - 2개 이상의 컴포저블에서 동일 값이 반복되면 `Dimens.kt`에 추가하고 참조
  - 단 1곳에서만 쓰이는 값은 컴포저블 내부에 인라인으로 남겨도 무방
  - Phase 0의 `paddingTB` 불일치도 이 규칙으로 판단: 실제 View에서 13.dp가 2회 이상 반복되면 `paddingTB = 13.dp` 그대로 유지, 아니면 iOS 원본 7.5.dp로 정합

- **공통 컴포넌트 배치 패키지**: `ui/common/` (기존 위치 유지)
  - 이유: `MainBgCard`, `PrimaryButton`이 이미 `ui/common`에 존재. feature 종속이 없는 횡단 UI 요소는 한 곳에 모아 검색·재사용성을 높임.
  - 대안: `core/ui/` → 다른 모듈 분리 시점에 검토. 현재는 단일 모듈이라 불필요.

- **컴포저블 네이밍 규칙**: iOS의 `XxxInSUI` / `XxxVCCustom` suffix는 모두 제거하고 Android Compose 관용 표기를 사용
  - `NavigationBarInSUI` → `CommonTopBar` (Material `TopAppBar`와 구분하기 위한 `Common` prefix)
  - `NavigationBarScrollViewInSUI` → `CommonTopBarScreen` (스캐폴드 역할이므로 `Screen` suffix)
  - `MainStyleViewInSUI` → `MainBgCard` (이미 결정됨)
  - `StationTitleViewInSUI` → `StationLineCircle` (역 이름이 아니라 "호선 원형"이 본질, iOS 네이밍이 모호하므로 의미 기반으로 재명명)
  - `AnimationButtonInSUI` → `AnimatedTapBox` (Material `Button`과 충돌 방지 + "박스를 눌렀을 때 애니메이션"이라는 의미 강조)
  - `UpDownExceptionViewInSUI` → `UpDownExceptionRow` (좌우 두 칸을 Row로 배치하는 컨테이너)
  - `Triangle` → `TriangleShape` (Compose `Shape` 인터페이스 구현체임을 명확히)
  - `ModalVCCustom` → `CommonModalBottomSheet` (Compose의 `ModalBottomSheet`와 일관)
  - `ModalSubCustomButton` → `ModalSubButton` (`Custom` suffix 제거)
  - 이유: AOS 컨벤션은 `XxxScreen`/`XxxBar`/`XxxRow`처럼 역할 기반 네이밍. 플랫폼 의존(SUI/VC) suffix는 Android 코드에서 노이즈.

- **`OffsetScrollViewInSUI` 제외**
  - 이유: iOS는 `GeometryReader` + `PreferenceKey`를 사용해 스크롤 offset을 강제로 끌어내는 우회였음. Compose는 `LazyListState.firstVisibleItemIndex` / `firstVisibleItemScrollOffset` / `ScrollState.value`를 1급 API로 제공하므로 별도 래퍼가 불필요. `CommonTopBarScreen`이 내부 상태로 임계값을 감지해 sub-title 노출 여부를 결정.
  - spec.md에서 "AOS에서는 다른 방법으로 감지할 경우 제외" 명시.

- **`ExpandedViewInSUI` 제외**
  - 이유: Compose의 `Row` / `Column` + `Arrangement` + `Modifier.weight`로 한 줄에 동일 효과 표현 가능. 단일 책임 래퍼를 만드는 비용 > 사용성. 호출부에서 `Row { Spacer(Modifier.weight(1f)); content() }` 식으로 직접 표기.
  - spec.md에서 "AOS에서는 다른 방법으로 사용할 경우 제외" 명시.

- **`CommonModalBottomSheet`는 Material3 `ModalBottomSheet`를 베이스로 함**
  - 이유: iOS `ModalVCCustom`의 핵심 기능(슬라이드업, 드래그 dismiss, 백드롭 탭 dismiss, 키보드 회피, 둥근 상단 corner)은 모두 `ModalBottomSheet`가 표준으로 제공. 직접 `Dialog`로 동등 동작을 구현하면 200+ LOC가 되며 가이드라인 2(Simplicity First) 위배.
  - 대안: `androidx.compose.ui.window.Dialog` → 키보드/드래그/탭바 처리를 수동 구현해야 함. 채택 안 함.
  - 대안: AlertDialog → 사이즈/높이 자유도가 부족.

- **`CommonModalBottomSheet` API 구조**
  - `mainTitle: String`, `subTitle: String?`, `onDismiss: () -> Unit`, `confirmButton: (@Composable () -> Unit)? = null`, `content: @Composable ColumnScope.() -> Unit`
  - iOS의 `btnTitle != ""` 분기 → `confirmButton == null`이면 버튼 영역 자체를 그리지 않음.
  - `onWillPresent` / `onWillDismiss` / `onDidDismiss`는 사용처가 생기면 그때 도입. 현 시점에는 `onDismiss` 단일 콜백으로 충분 (가이드라인 2).

- **`StationLineCircle` API**
  - `title: String`, `lineColor: Color?`, `size: Dp`, `isFilled: Boolean`, `fontSize: TextUnit = Dimens.fontSizeMedium`
  - iOS는 `lineColor: String?`로 색상 asset 이름을 받지만, AOS는 호선 컬러 매퍼가 아직 없는 단계 → 호출부에서 `Color`를 직접 주입. 매퍼는 별도 spec에서 도입.
  - `lineColor == null`이면 회색 폴백 (iOS 동작 동일).

- **`AnimatedTapBox` API**
  - `bgColor: Color`, `pressedColor: Color`, `alignment: AnimatedTapBoxAlignment`(`Leading`/`Center`/`Trailing` enum), `verticalPadding: Dp = 10.dp`, `onClick: () -> Unit`, `content: @Composable RowScope.() -> Unit`
  - iOS의 `0.1s` 지연 후 액션 실행은 UX 일관성을 위해 동일하게 적용 (`LaunchedEffect`로 지연 트리거).
  - 대안: 지연 없이 즉시 실행 → iOS와 시각/감각 차이가 발생. iOS 동작 그대로 유지.

- **`CommonTopBar` / `CommonTopBarScreen` API**
  - `CommonTopBar(title, isSubTitleVisible, onBack, backIcon, trailingIcon, onTrailingClick)`
  - `CommonTopBarScreen(title, isLargeTitleHidden = false, onBack, backIcon, trailingIcon, onTrailingClick, content)`
  - iOS는 SF Symbol 문자열을 받지만 AOS는 `ImageVector` 또는 `Painter`를 받음. 호출부에서 Material Icons `Icons.AutoMirrored.Filled.ArrowBack` 등을 주입.
  - sub-title 표시 임계값: iOS와 동일하게 "최초 offset 대비 25px 이상 스크롤" → AOS는 `firstVisibleItemScrollOffset >= 25.dp.toPx()` 또는 `25.dp` 동등치로 환산.

- **테스트 전략**: 본 spec의 Acceptance Criteria는 "화면 정상 렌더링"이므로 단위 테스트 대신 **Compose Preview**(`@Preview`)로 각 컴포저블의 렌더링을 검증
  - 이유: 공통 UI 컴포넌트는 로직이 거의 없고, ViewModel/State와 무관. 화면 단위 회귀는 향후 feature 단위 통합 단계에서 검증.
  - 대안: `androidx.compose.ui.test` 기반 instrumented test → 셋업 비용 대비 얻는 신뢰도가 낮음. 도입 시점은 feature 화면이 갖춰진 후로 미룸.

## 구현 순서

### Phase 0. Dimens.kt 정합 (ViewStyle 동기화)
- `ui/theme/Dimens.kt` 토큰을 `ViewStyle.swift`와 대조 검증:

  | ViewStyle.swift | Dimens.kt (현재) | 일치 여부 |
  |-----------------|-----------------|-----------|
  | `FontSize.superSmallSize` = 9 | `fontSizeSuperSmall` = 9.sp | ✅ |
  | `FontSize.mediumSmallSize` = 11 | `fontSizeMediumSmall` = 11.sp | ✅ |
  | `FontSize.smallSize` = 13 | `fontSizeSmall` = 13.sp | ✅ |
  | `FontSize.mediumSize` = 15 | `fontSizeMedium` = 15.sp | ✅ |
  | `FontSize.largeSize` = 17 | `fontSizeLarge` = 17.sp | ✅ |
  | `FontSize.mainTitleMediumSize` = 21 | `fontSizeMainTitleMedium` = 21.sp | ✅ |
  | `FontSize.mainTitleSize` = 23 | `fontSizeMainTitle` = 23.sp | ✅ |
  | `FontSize.bigTitleSize` = 27 | `fontSizeBigTitle` = 27.sp | ✅ |
  | `Layer.radius` = 15 | `cornerRadius` = 15.dp | ✅ |
  | `padding.mainStyleViewLR` = 20 | `paddingLR` = 20.dp | ✅ |
  | `padding.mainStyleViewTB` = 7.5 | `paddingTB` = 13.dp | ⚠️ 불일치 |
  | `AnimateView.speed` = 0.25 | `animationSpeed` = 0.25f | ✅ |
  | `AnimateView.size` = 0.94 | `animationScale` = 0.94f | ✅ |

- `paddingTB` 처리 방침: 사용자 확인 후 7.5.dp로 수정하거나 현재값(13.dp) 유지 결정

### Phase 1. 형상/스타일 (Shape & Container)
- `ui/common/TriangleShape.kt` 신규 작성
  - `androidx.compose.ui.graphics.Shape` 구현, `Path`로 위쪽 꼭짓점 → 좌하 → 우하 삼각형
- `ui/common/StationLineCircle.kt` 신규 작성
  - `Box` + `Modifier.size(size).border(...).background(...)` + 중앙 `Text`
  - `lineColor == null` 시 `Color.Gray` 폴백, `isFilled`에 따라 fill/stroke 분기

### Phase 2. 인터랙션 (Tap 애니메이션)
- `ui/common/AnimatedTapBox.kt` 신규 작성
  - `MutableInteractionSource` + `collectIsPressedAsState` + `animateFloatAsState(targetValue = if (isPressed) Dimens.animationScale else 1f)` 로 scale 애니메이션
  - 배경색은 `animateColorAsState`로 `bgColor` ↔ `pressedColor` 전환
  - `alignment`에 따라 Row 내부 `Spacer` 배치 분기 (Leading/Center/Trailing)
  - tap → `LaunchedEffect(키 갱신)`로 100ms 지연 후 `onClick()` 호출

### Phase 3. 상단바 (CommonTopBar / CommonTopBarScreen)
- `ui/common/CommonTopBar.kt` 신규 작성
  - `Row { backIcon? + Text(title, alpha = isSubTitleVisible.alpha) + Spacer(weight=1f) + trailingIcon? }`
  - sub-title fade/slide: `animateFloatAsState(alpha)`, `animateDpAsState(offsetY)` (iOS 7.5pt → 7.5dp)
  - `height = 45.dp`, `horizontalPadding = Dimens.paddingLR`
- `ui/common/CommonTopBarScreen.kt` 신규 작성
  - `Column { CommonTopBar(...) + verticalScrollable Column(or LazyColumn) }`
  - 내부 `rememberScrollState()` 또는 `rememberLazyListState()`로 offset 감지
  - 임계값 비교 (`scrollState.value >= ~25.dp.toPx()`)로 `isSubTitleVisible` 토글
  - `isLargeTitleHidden == false`일 때 본문 상단에 `Text(title, fontWeight=Heavy, fontSize=Dimens.fontSizeMainTitle)` 노출

### Phase 4. 도메인 특화 행 (UpDownExceptionRow)
- `ui/common/UpDownExceptionRow.kt` 신규 작성
  - `Row { MainBgCard(상하행 텍스트) + MainBgCard(제외역 텍스트 + arrowtriangle.down 아이콘) }`
  - 두 칸은 `Modifier.weight(1f)`로 균등 분배, `height = 40.dp`, `spacedBy = 20.dp`
  - 제외역 빈 문자열일 때는 "제외 없음" 문구만 (Strings 리소스 키 신설은 본 plan 범위 외 — 현 단계는 한국어 하드코딩 후 추후 string resource 분리)

### Phase 5. 모달 (Bottom Sheet)
- `ui/common/modal/ModalSubButton.kt` 신규 작성
  - `Box` + `RoundedCornerShape(Dimens.cornerRadius)` + 중앙 `Text(fontSize = Dimens.fontSizeMedium)`
  - 색은 파라미터로 받음 (배경/텍스트)
- `ui/common/modal/CommonModalBottomSheet.kt` 신규 작성
  - `androidx.compose.material3.ModalBottomSheet`을 베이스로 사용
  - 내부 레이아웃: `Column { mainTitle Text + subTitle Text(nullable) + content() + confirmButton?() }`
  - `shape = RoundedCornerShape(topStart=25.dp, topEnd=25.dp)`, `sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)`
  - `onDismissRequest = onDismiss` 로 백드롭 탭 / 드래그 dismiss 처리

### Phase 6. Preview 검증
- 각 신규 컴포저블 파일 하단에 `@Preview` 추가 (Light/Dark 두 케이스)
- Android Studio Preview로 렌더링 확인 → spec Acceptance ("화면 정상적으로 랜더링")

## 완료 조건
- [ ] Spec Acceptance Criteria 충족: 모든 신규 공통 컴포저블이 Compose Preview에서 정상 렌더링됨
- [ ] iOS Common SwiftUI 9종 중 `OffsetScrollViewInSUI`, `ExpandedViewInSUI`를 제외한 7종에 대응되는 Android 컴포저블이 `ui/common/` 하위에 존재
- [ ] iOS Common UIKit 3종(`ModalVCCustom`, `ModalCustomButton`, `ModalSubCustomButton`)에 대응되는 Android 컴포저블이 존재 (`CommonModalBottomSheet`, `PrimaryButton`(재사용), `ModalSubButton`)
- [ ] 2개 이상 반복되는 여백/크기 값은 `Dimens.kt`에 추가하고 참조함
- [ ] `paddingTB` 불일치 판단 완료 (반복 여부 확인 후 값 확정)
- [ ] 모든 신규 파일은 `Dimens` / `Color` 토큰을 사용하며 매직 넘버 없음
- [ ] 기존 빌드/테스트 회귀 없음
