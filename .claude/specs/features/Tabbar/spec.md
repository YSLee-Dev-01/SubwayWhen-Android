# Tabbar

## 무엇을 하는가
기본 탭바 디자인 및 탭 구성

## 동작 명세
탭바는 홈, 검색, 설정 탭으로 구성됨
Home, Search, Setting 폴더를 나누어서 구성

iOS 기준으로는 AppDelegate > SceneDelegate > RootViewController가 UITabbarViewController 구조
AOS 또한 AOS 기준에 맞춰서 탭바가 Root가 됨
단, 튜토리얼을 완료하지 않은 경우 TutorialVC가 Root 였다가 전환되는 방식

기능은 나중에 추가할 예정이기 때문에
View에는 해당 기능의 텍스트만 적고
ViewModel을 연동만 해둠
(Home, Search, Setting에 해당하는 파일 및 폴더 생성)

## 무엇이 잘못될 수 있는가
- 실패할 수 없음
- 실패하면 안됨

## Acceptance Criteria
- [x] 하단에 홈, 검색, 설정 탭바가 존재
- [x] 각 버튼을 누르면 해당 화면으로 이동
