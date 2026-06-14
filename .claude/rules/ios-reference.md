# iOS 원본 프로젝트 참조 규칙

이 프로젝트는 iOS 앱 **지하철 민실씨**를 Android로 포팅한다.
기능 구현 시 아래 iOS 원본 코드를 기준으로 삼되, 코드를 직역하지 말고 Android 패턴으로 재설계한다.

## 원본 프로젝트 경로

```
/Users/yslee/Desktop/Project/SubwayWhen
```

## iOS → Android 기술 스택 대응

| 항목 | iOS | Android |
|------|-----|---------|
| 언어 | Swift | Kotlin |
| UI | SwiftUI / UIKit | Jetpack Compose |
| 아키텍처 | TCA / MVVM-C | MVI |
| 비동기 | RxSwift | Kotlin Coroutines + Flow |
| 네트워크 | Alamofire | Ktor |
| 테스트 | XCTest + Nimble + RxBlocking | JUnit + Kotest |
| 의존성 관리 | CocoaPods | Gradle (Version Catalog) |

## iOS → Android 아키텍처 대응

| iOS | Android |
|-----|---------|
| TCA State | UiState (data class 또는 sealed interface) |
| TCA Action | Intent (sealed interface) |
| TCA Effect | UiEffect (SharedFlow) |
| MVVM-C Coordinator | Navigation Compose (NavHost) |
| RxSwift Observable | Kotlin Flow / StateFlow |
| UserDefaults (FixInfo) | DataStore Preferences |
| CoreData | Room |

## 주요 기능 포팅 목록

| 기능 | iOS 위치 | 우선순위 | 상태 |
|------|----------|----------|------|
| 역 즐겨찾기 목록 (메인) | `Presentation/Main/` | P0 | 🚧 진행 예정 |
| 실시간 도착정보 | `Presentation/Detail/` | P0 | ❌ 미시작 |
| 역 검색 | `Presentation/Search/` | P0 | ✅ 완료 |
| 시간표 조회 | `Presentation/Detail/DetailResultSchedule/` | P1 | ❌ 미시작 |
| 그룹 관리 (출근/퇴근) | `Configuration/Entity/SaveStationGroup.swift` | P1 | ❌ 미시작 |
| 시간 기반 그룹 필터링 | `MainViewModel` | P1 | ❌ 미시작 |
| 설정 화면 | `Presentation/Setting/` | P1 | ✅ 완료 |
| 알림 (로컬 Push) | `Service/Notification/` | P2 | ✅ 완료 (출퇴근 AlarmManager 알림) |
| 위치 기반 역 검색 | `Service/Location/` | P2 | ✅ 완료 (LocationManager) |
| 혼잡도 정보 | `Service/Congestion/` | P2 | ✅ 완료 (CongestionModal + CongestionManager) |
| 민원 접수 | `Presentation/Report/` | P3 | ❌ 미시작 |
| 실시간 열차 위치 | `Presentation/Realtime/` | P3 | ❌ 미시작 |
| 홈 위젯 | `SubwayWhenHomeWidget/` | P3 | ❌ 미시작 |

## iOS 참조 경로

| 참조 대상 | iOS 경로 |
|----------|---------|
| 역 코드 매핑 데이터 | `SubwayWhen/Resource/Json/DetailStationIdList.plist` |
| 호선 색상 | `SubwayWhen/Resource/Assets.xcassets/` |
| 실시간 응답 모델 | `SubwayWhenNetworking/DataLoad/Entity/LiveArrival/` |
| 시간표 응답 모델 | `SubwayWhenNetworking/DataLoad/Entity/ScheduleArrival/` |
| 네트워크 로직 | `SubwayWhenNetworking/DataLoad/LoadModel/LoadModel.swift` |
| 저장 설정 모델 | `SubwayWhen/Configuration/Entity/` |
| 메인 화면 | `SubwayWhen/Presentation/Main/` |
| 역 검색 | `SubwayWhen/Presentation/Search/` |
| 역 상세 / 도착정보 | `SubwayWhen/Presentation/Detail/` |
| 설정 화면 | `SubwayWhen/Presentation/Setting/` |
| 알림 서비스 | `SubwayWhen/Service/Notification/NotificationManager.swift` |
| 위치 서비스 | `SubwayWhen/Service/Location/LocationManager.swift` |
| 혼잡도 서비스 | `SubwayWhen/Service/Congestion/CongestionManager.swift` |
| 민원 접수 | `SubwayWhen/Presentation/Report/` |
| 실시간 열차 위치 | `SubwayWhen/Presentation/Realtime/` |
| 튜토리얼 | `SubwayWhen/Presentation/Tutorial/` |

## 딥링크

iOS 위젯 탭 시 `subwaywhen://station?text={역명}` 스킴으로 앱 진입.
Android에서는 `AndroidManifest.xml`의 `intent-filter`로 대응한다.

| iOS | Android |
|-----|---------|
| `SceneDelegate.deepLinkMove()` | Activity `intent-filter` (scheme: `subwaywhen`, host: `station`) |
| `AppCoordinator.deepLinkAction()` | NavHost에서 쿼리파라미터 `text` 추출 후 해당 역 화면으로 이동 |
