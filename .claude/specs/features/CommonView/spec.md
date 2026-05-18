# CommonView

## 무엇을 하는가
기본 화면을 구성하는 공통 뷰

## 동작 명세
iOS 폴더 기준 /SubwayWhen/SubwayWhen/Presentation/Common에 해당 하는 View
단, iOS는 SwiftUI와 UIKit이 따로따로 개발되어 있지만, AOS는 Jetpack Compose에 해당하는 View만 제작

SwiftUI
OffsetScrollViewInSUI - Offset을 감지하기 위한 View (AOS에서는 다른 방법으로 감지할 경우 제외)
NavigationBarScrollViewInSUI - 커스텀 네비게이션바를 가진 스크롤 뷰
NavigationBarInSUI - 커스텀 네비게이션바
UpDownExceptionViewInSUI - 제외행을 보여주기 위한 뷰
MainStyleViewInSUI - 메인 스타일 뷰
StationTitleViewInSUI - 지하철 호선을 보여주기 위한 뷰
AnimationButtonInSUI - 버튼의 눌림 애니메이션을 보여주기 위한 뷰
ExpandedViewInSUI - 한쪽으로 정렬 후 확장하기 위한 뷰 (AOS에서는 다른 방법으로 사용할 경우 제외)
Triangle - 커스텀 삼각형 뷰

UIKit
ModalVCCustom - 모달 기본 VC
ModalCustomButton - 모달 기본 버튼
ModalSubCustomButton - 모달 서브 버튼

만약 더 필요한 공통 뷰가 있다면 제작해도 돼
네이밍은 iOS 기반이니, AOS에 맞춰서 따로 만들어줘

ViewStyle은 View의 공통 스타일을 담고있는 파일이야

## Acceptance Criteria
- [x] 화면 정상적으로 렌더링 한다.
