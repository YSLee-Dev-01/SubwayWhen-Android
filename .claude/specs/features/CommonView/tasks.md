# Tasks: CommonView

## 참조
- spec: `.claude/specs/features/CommonView/spec.md`
- plan: `.claude/specs/features/CommonView/plan.md`

## Task 목록

### Phase 0. Dimens.kt 정합 (ViewStyle 동기화)

#### [x] Task 1 — `Dimens.kt` (수정)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/theme/Dimens.kt`
- iOS `ViewStyle.swift` 토큰과 현재 `Dimens.kt` 값을 전수 대조
- `paddingTB` 불일치 판단: 신규 컴포저블에서 13.dp가 2회 이상 반복되면 유지, 그렇지 않으면 iOS 원본인 7.5.dp로 수정

---

### Phase 1. 형상/스타일 (Shape & Container)

#### [x] Task 2 — `TriangleShape.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/TriangleShape.kt`
- `androidx.compose.ui.graphics.Shape` 인터페이스 구현
- `Path.moveTo/lineTo/close`로 위쪽 꼭짓점 → 좌하 → 우하 삼각형 생성
- `@Preview` 추가 (Light/Dark 두 케이스)

---

#### [x] Task 3 — `StationLineCircle.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/StationLineCircle.kt`
- iOS `StationTitleViewInSUI` 대응 — 호선 컬러 원형 + 텍스트
- 파라미터: `title: String`, `lineColor: Color?`, `size: Dp`, `isFilled: Boolean`, `fontSize: TextUnit = Dimens.fontSizeMedium`
- `lineColor == null`이면 `Color.Gray` 폴백
- `isFilled` 값에 따라 fill/stroke 분기: `Box` + `Modifier.size(size).border(...).background(...)` + 중앙 `Text`
- `@Preview` 추가 (Light/Dark 두 케이스)

---

### Phase 2. 인터랙션 (Tap 애니메이션)

#### [x] Task 4 — `AnimatedTapBox.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/AnimatedTapBox.kt`
- iOS `AnimationButtonInSUI` 대응 — 눌림 시 scale + 배경색 전환 컨테이너
- 파라미터: `bgColor: Color`, `pressedColor: Color`, `alignment: AnimatedTapBoxAlignment`(`Leading`/`Center`/`Trailing` enum), `verticalPadding: Dp = 10.dp`, `onClick: () -> Unit`, `content: @Composable RowScope.() -> Unit`
- `MutableInteractionSource` + `collectIsPressedAsState` + `animateFloatAsState(targetValue = if (isPressed) Dimens.animationScale else 1f)`로 scale 애니메이션
- `animateColorAsState`로 `bgColor` ↔ `pressedColor` 배경색 전환
- `alignment`에 따라 Row 내부 `Spacer` 배치 분기 (Leading/Center/Trailing)
- `LaunchedEffect`로 100ms 지연 후 `onClick()` 호출 (iOS 0.1s 동작 동일)
- `@Preview` 추가 (Light/Dark 두 케이스)

---

### Phase 3. 상단바 (CommonTopBar / CommonTopBarScreen)

#### [x] Task 5 — `CommonTopBar.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/CommonTopBar.kt`
- iOS `NavigationBarInSUI` 대응 — 커스텀 상단바
- 파라미터: `title: String`, `isSubTitleVisible: Boolean`, `onBack: (() -> Unit)?`, `backIcon: ImageVector? = null`, `trailingIcon: ImageVector? = null`, `onTrailingClick: (() -> Unit)? = null`
- 레이아웃: `Row { backIcon? + Text(title, alpha = isSubTitleVisible.alpha) + Spacer(weight=1f) + trailingIcon? }`
- sub-title fade/slide: `animateFloatAsState(alpha)`, `animateDpAsState(offsetY)` (iOS 7.5pt → 7.5dp)
- `height = 45.dp`, `horizontalPadding = Dimens.paddingLR`
- `@Preview` 추가 (Light/Dark 두 케이스)

---

#### [x] Task 6 — `CommonTopBarScreen.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/CommonTopBarScreen.kt`
- iOS `NavigationBarScrollViewInSUI` 대응 — `CommonTopBar` + 스크롤 대형 타이틀 결합 스캐폴드
- 파라미터: `title: String`, `isLargeTitleHidden: Boolean = false`, `onBack: (() -> Unit)?`, `backIcon: ImageVector? = null`, `trailingIcon: ImageVector? = null`, `onTrailingClick: (() -> Unit)? = null`, `content: @Composable () -> Unit`
- `rememberScrollState()` 또는 `rememberLazyListState()`로 내부 offset 감지
- 임계값 비교 (`scrollState.value >= ~25.dp.toPx()`)로 `isSubTitleVisible` 토글
- `isLargeTitleHidden == false`일 때 본문 상단에 `Text(title, fontWeight = Heavy, fontSize = Dimens.fontSizeMainTitle)` 노출
- `OffsetScrollViewInSUI`의 별도 컴포저블은 만들지 않음 — 스크롤 offset은 본 스캐폴드 내부 상태로 처리
- `@Preview` 추가 (Light/Dark 두 케이스)

---

### Phase 4. 도메인 특화 행 (UpDownExceptionRow)

#### [x] Task 7 — `UpDownExceptionRow.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/UpDownExceptionRow.kt`
- iOS `UpDownExceptionViewInSUI` 대응 — 상/하행 + 제외역 표시 행
- 파라미터: `upDownText: String`, `exceptionText: String`
- 레이아웃: `Row { MainBgCard(상하행 텍스트) + MainBgCard(제외역 텍스트 + 아래 화살표 아이콘) }`
- 두 칸은 `Modifier.weight(1f)`로 균등 분배, `height = 40.dp`, `spacedBy = 20.dp`
- `exceptionText`가 빈 문자열이면 "제외 없음" 하드코딩 표시 (추후 string resource 분리)
- `@Preview` 추가 (Light/Dark 두 케이스)

---

### Phase 5. 모달 (Bottom Sheet)

#### [x] Task 8 — `ModalSubButton.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/modal/ModalSubButton.kt`
- iOS `ModalSubCustomButton` 대응 — 모달 내부 보조 버튼
- 파라미터: `text: String`, `bgColor: Color`, `textColor: Color`, `onClick: () -> Unit`
- `Box` + `RoundedCornerShape(Dimens.cornerRadius)` + 중앙 `Text(fontSize = Dimens.fontSizeMedium)`
- `@Preview` 추가 (Light/Dark 두 케이스)

---

#### [x] Task 9 — `CommonModalBottomSheet.kt` (신규)
**파일**: `app/src/main/java/com/yslee/subwaywhen/ui/common/modal/CommonModalBottomSheet.kt`
- iOS `ModalVCCustom` 대응 — Material3 `ModalBottomSheet` 래퍼
- 파라미터: `mainTitle: String`, `subTitle: String? = null`, `onDismiss: () -> Unit`, `confirmButton: (@Composable () -> Unit)? = null`, `content: @Composable ColumnScope.() -> Unit`
- `androidx.compose.material3.ModalBottomSheet`를 베이스로 사용
- 내부 레이아웃: `Column { mainTitle Text + subTitle Text(nullable) + content() + confirmButton?() }`
- `shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)`
- `sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)`
- `onDismissRequest = onDismiss`로 백드롭 탭 / 드래그 dismiss 처리
- `confirmButton == null`이면 버튼 영역 미렌더링
- `@Preview` 추가 (Light/Dark 두 케이스)

---

### Phase 6. Preview 검증

#### [x] Task 10 — Preview 렌더링 확인
**파일**: 신규 컴포저블 파일 전체 (Task 2 ~ Task 9)
- Android Studio Preview에서 각 컴포저블의 Light/Dark @Preview 정상 렌더링 확인
- spec Acceptance Criteria "화면 정상적으로 렌더링 한다" 충족 여부 검증

---

## 체크리스트

### 품질 (DoD)
- [ ] 빌드 성공
- [ ] 기존 테스트 회귀 없음
- [ ] 모든 신규 파일에서 `Dimens` / `Color` 토큰 사용, 매직 넘버 없음
- [ ] 2개 이상 반복되는 여백/크기 값은 `Dimens.kt`에 추가하고 참조

### 기능 (AC)
- [ ] 화면 정상적으로 렌더링 한다 (모든 신규 컴포저블 Compose Preview 확인)
- [ ] iOS Common SwiftUI 9종 중 `OffsetScrollViewInSUI`, `ExpandedViewInSUI`를 제외한 7종에 대응하는 Android 컴포저블이 `ui/common/` 하위에 존재
- [ ] iOS Common UIKit 3종에 대응하는 Android 컴포저블 존재 (`CommonModalBottomSheet`, `PrimaryButton`(재사용), `ModalSubButton`)
- [ ] `paddingTB` 불일치 판단 완료 (반복 여부 확인 후 값 확정)
