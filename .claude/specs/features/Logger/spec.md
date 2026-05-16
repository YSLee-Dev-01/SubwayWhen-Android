# Logger

## 무엇을 하는가
앱에서 로그를 출력할 때 사용하는 유틸

## 동작 명세
iOS에서는 시스템에서 제공해주는 OSLog를 통해서 작업했어

AppLogger는 카테고리와 TotalEnabled 값을 받아
함수 log()를 통해 로그 레벨과 메시지를 받아

LogLevel은 3개야
error, info, debug

iOS는 Extension을 통해 AppLogger를 미리 정의했어
각 카테고리는 Network, Core, View, Coordinator, LiveActivity, CoreData로 나누었어
static으로 선언해서 인스턴스 생성 없이 접근 가능해

AOS에는 Network, Core, View만 있어도 충분해

자세한 기능은 AppLogger를 참고해줘 

## Acceptance Criteria
- [x] 로그가 정상적으로 출력 되는지
- [x] TotalEnabled이 false인데 로그가 출력 되는지
