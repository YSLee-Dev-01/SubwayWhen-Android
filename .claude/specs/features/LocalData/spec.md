# LocalData

## 무엇을 하는가
기기 로컬에 데이터를 저장하는 로직 

## 동작 명세
Swift에서는 UserDefaults를 사용하고 있어
AOS에서도 비슷한 방식이 있다면 차용해서 사용해줘

iOS는 struct을 인코딩해서 저장하고 있어
iOS는 SaveSetting에 설정을,
SaveStations에는 지하철 역 리스트를 저장해

SaveSetting에는 
mainCongestionLabel - 메인화면 혼잡도 아이콘
mainGroupOneTime - 출근 시간 
mainGroupTwoTime - 퇴근 시간
detailAutoReload - 상세화면 자동 새로고침 여부
detailScheduleAutoTime  - 상세화면 시간표 정렬 여부
searchOverlapAlert - 중복 저장 방지 여부
alertGroupOneID - 출근 시간 알림 ID
alertGroupTwoID - 퇴근 시간 알림 ID
tutorialSuccess - 튜토리얼 완료 여부
detailVCTrainIcon - 상세화면 열차 아이콘
isWeekendNotificationEnabled - 주말 알림 수신 여부
mainCongestionBaseStaton - 메인화면 혼잡도 역

alertGroupOneID, alertGroupTwoID는 iOS에서 NotificationManager에서 구분하기 위한 값이야
AOS에서는 필요가 없다면 만들지 않아도 돼

SaveStations에는
id - 고유 ID
stationName - 역 이름
stationCode - 역 코드
updnLine - 상행, 하행, 외선, 내선
line - 호선
lineCode - 호선 코드
group - 출퇴근 그룹
exceptionLastStation - 제외 행
korailCode - 코레일 전용 코드

group는 SaveStationGroup로 저장되고, enum이야
case는 one, two가 존재해

각 값의 타입은 Swift 파일을 직접 참조해
각 값의 초기 값은 Swift 파일을 직접 참조해

iOS에서는 SaveSetting,  SaveStations 정보를 FixInfo라는 struct에 담아서 관리해
각 값은 static으로 관리되기 때문에 인스턴스를 만들지 않아도 접근이 가능해

didSet 을 사용해서 값이 변경되면 바로 로컬에 저장해
AOS에서도 비슷한 방식을 사용해줘

Key 값은 iOS에서는 같이 명시되어 있지만, AOS에서는 따로 분리해서 작업해줘

FixInfo에 값 주입은 앱이 시작됐을 때 바로 진행해

## Acceptance Criteria
- [x 앱을 재실행해도 기기 내부에 데이터를 저장하는지 ] 
- [x 인코딩 과정이 실패하지 않는지 ] 
