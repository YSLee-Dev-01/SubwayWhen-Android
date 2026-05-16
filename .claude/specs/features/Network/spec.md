# Network

## 무엇을 하는가
네트워크 로직 생성

## 동작 명세
현재 네트워크에 대한 기본 로직이 없어
iOS를 기준으로 네트워크 로직을 포팅할거야

다만, iOS는 RxSwift와 TCA를 혼용하기 때문에
RxSwift 방식으로 되어 있지만, AOS는 처음부터 아키텍처를 짜기 때문에 MVI 방식으로 개발해도 괜찮아

이 때 iOS의 형식과 비슷하게
테스트에 용이하게
프로토콜 기반으로 설계를 해줘

## 기반
NetworkManager
- NetworkManagerProtocol 사용

LoadModel
- LoadModelProtocol 채택
- NetworkManagerProtocol 내부 사용

TotalLoadModel
- TotalLoadModelProtocol 채택
- LoadModelProtocol 내부 사용

## 규칙
개발 자체는 AOS 스타일에 맞게 해줘
- 코드 컨벤션, 규칙, 스타일 등

토큰을 주입하는 부분은 따로 함수 처리만 해줘
- 외부에서 .gitignore를 선 작업 후 처리할게

### 기능은 iOS와 동일하게

## Acceptance Criteria
- [x] 네트워크 통신 시 200 응답 → `NetworkResult.Success` 반환 (NetworkManagerTest MockEngine 200 케이스 통과)
