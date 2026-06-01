# 컨벤션 및 의존성

## 코딩 컨벤션

- **패키지 구조**: `feature/{기능명}` 단위로 분리 (예: `feature/home`, `feature/search`, `feature/detail`)
- **MVI 파일 분리**: 화면마다 `Contract.kt` (UiState + Intent + Effect), `ViewModel.kt`, `Screen.kt` 3파일 세트로 구성
- **UiState**: sealed interface가 아닌 `data class`로 정의 (복잡한 상태 필드가 많을 때) — 단순한 로딩/성공/에러 구분이 필요한 경우에만 sealed interface 사용
- **단방향 데이터 흐름**: 사용자 인터랙션은 `onIntent(Intent)` 단일 진입점으로 ViewModel에 전달
- **비동기**: `viewModelScope.launch` + Flow 사용. `GlobalScope` 사용 금지
- **토큰 관리**: `local.properties`에서 읽어 `BuildConfig`로 주입. `BuildConfig`는 커밋하지 않음

## 폴더 구조 패턴

기능 화면 내부 구조:
```
feature/{기능명}/
├── {기능명}Contract.kt       # UiState, Intent, Effect
├── {기능명}Screen.kt         # Composable 진입점
├── {기능명}ViewModel.kt      # 비즈니스 로직
├── component/               # 화면 전용 서브 컴포저블
└── modal/                   # 모달 화면
    └── component/           # 모달 전용 서브 컴포저블
```

섹션이 복잡하면 하위 폴더로 분리 (예: `search/vicinity/`).

## 탭바 숨김/복원 패턴

모달이 열릴 때 탭바를 숨기고, 닫힐 때 복원한다.

- `RootScaffold`가 `isTabBarVisible` 상태를 소유
- 각 탭 화면에 `onTabBarVisibilityChange: (Boolean) -> Unit` 콜백 전달
- 화면에서 모달 open/close 시 해당 콜백 호출

```kotlin
// RootScaffold.kt
composable(TabRoute.Search.route) {
    SearchScreen(onTabBarVisibilityChange = { isTabBarVisible = it })
}

// Screen.kt
SearchScreen(onTabBarVisibilityChange: (Boolean) -> Unit) {
    // 모달 열 때
    onTabBarVisibilityChange(false)
    // 모달 닫을 때
    onTabBarVisibilityChange(true)
}
```

## CommonModalBottomSheet 사용법

iOS ModalVCCustom 대응. 새 모달 추가 시 이 컴포넌트를 사용한다.

```kotlin
CommonModalBottomSheet(
    mainTitle = "제목",
    subTitle = "부제목 (optional)",
    onDismiss = { /* state 초기화 */ },
    topDecoration = { /* Modal 흰 배경 위에 표시할 뷰 (DisposableView 등) */ },
    confirmButton = { animatedDismiss ->
        PrimaryButton(text = "확인", onClick = animatedDismiss)
    },
) {
    // 본문 content
}
```

- `animatedDismiss`: `sheetState.hide()` 후 `onDismiss()` 호출 — 슬라이드 다운 애니메이션 보장
- `onDismissRequest`로 직접 dismiss하면 애니메이션 없이 닫힘

## AnimatedTapBox

버튼·카드 탭 애니메이션이 필요하면 `AnimatedTapBox`로 래핑한다.
`Modifier.clickable` 대신 사용하여 iOS의 scaleEffect 탭 피드백을 재현한다.

## Dimens 토큰 규칙

`ui/theme/Dimens.kt` 는 iOS `ViewStyle.swift` 대응 토큰 파일이다.

- **ViewStyle과 실제 View의 값이 다를 수 있다** — ViewStyle 값을 무조건 따르지 말고 실제 View 기준으로 판단한다.
- **2회 이상 반복되는 여백·크기 값은 `Dimens.kt`에 추가하고 참조한다.**
- 단 1곳에서만 쓰이는 값은 컴포저블 내부에 인라인으로 둬도 무방하다.

현재 등록된 토큰:
| 토큰 | 값 | 용도 |
|------|----|------|
| `cornerRadius` | 15dp | 카드 라운드 |
| `paddingLR` | 20dp | 화면 좌우 여백 |
| `paddingTB` | 7.5dp | 화면 상하 여백 |
| `paddingInner` | 16dp | 내부 여백 |
| `animationDurationMs` | 250ms | 공통 애니메이션 duration |
| `animationScale` | 0.94f | 탭 스케일 애니메이션 |
| `tabBarBottomPadding` | 80dp | 탭바 위 콘텐츠 하단 여백 |
| `modalHorizontalMargin` | 10dp | 모달 좌우 여백 |
| `modalButtonHeight` | 50dp | 모달 확인 버튼 높이 |
| `fontSizeSuperSmall` | 9sp | |
| `fontSizeMediumSmall` | 11sp | |
| `fontSizeSmall` | 13sp | |
| `fontSizeMedium` | 15sp | |
| `fontSizeLarge` | 17sp | |
| `fontSizeMainTitleMedium` | 21sp | |
| `fontSizeMainTitle` | 23sp | |
| `fontSizeBigTitle` | 27sp | |

## iOS → Compose 변환 시 주의사항

- **`FontWeight.Heavy` 없음** → `FontWeight.ExtraBold` (800) 사용
- **하드코딩 색상 금지** — `Color.White` / `Color.Black` / `Color.Gray` 대신 `MaterialTheme.colorScheme.*` 사용
- **애니메이션 duration** — `tween(durationMillis = Dimens.animationDurationMs)` 사용
- **버튼 콜백 누락 주의** — iOS Button/TapGesture 핸들러가 있으면 AOS에도 반드시 콜백 파라미터로 노출
- **문자열 suffix/prefix** — iOS `"\(value)\(Strings.X.suffix)"` 패턴은 AOS도 동일하게 구성

## 의존성 관리

`libs.versions.toml` (Version Catalog)을 사용한다. `build.gradle.kts`에 버전 하드코딩 금지.

| 역할 | 라이브러리 |
|------|-----------|
| DI | Hilt |
| 네트워크 | Ktor |
| JSON | kotlinx.serialization |
| 로컬 저장 | DataStore Preferences |
| DB 캐시 | Room |
| 이미지 | Coil |
| Firebase | Firebase BOM (Analytics, Database) |
| 테스트 | JUnit + Kotest + Turbine + MockK |
