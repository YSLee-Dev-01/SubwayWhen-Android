# KorailLine-Schedule

## 무엇을 하는가
코레일 노선의 지하철 시간표를 불러오기 위해 사용

/Users/yslee/Desktop/Project/SubwayWhen/SubwayWhenNetworking/DataLoad/TotalLoadModel.swift의 
korailSchduleLoad()를 참고한다.

## 동작 명세
- 트리거: 홈 화면에서 사용자가 시간표 버튼을 눌렀을 때 
- 결과: 네트워크 통신을 통해서 사용자에게 가장 가까운 시간표 정보를 제공
- 사이드이펙트: 네트워크
- 불변 조건: 홈 화면 내부에서 이루어져야 함

## 무엇이 잘못될 수 있는가
- 네트워크 에러

## 무엇에 의존하는가
### 의존성
- TotalLoadModel
    - LoadModel을 통해 가져온 데이터를 가공
- LoadModel
    - 네트워크 통신점

### 제약
- LoadModel에 직접 의존하지 않고 TotalLoadModel를 사용

## Acceptance Criteria
- [x] 수인분당선, 경춘선, 경의중앙선 시간표를 정상적으로 가져올 수 있다.
