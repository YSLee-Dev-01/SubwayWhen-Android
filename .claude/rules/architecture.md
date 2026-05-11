# 아키텍처

## MVI 레이어 구조

```
Compose Screen
    ↓ Intent (사용자 액션)
ViewModel
    ↓ 상태 변환
State (StateFlow<UiState>)
    ↑ 렌더링
Compose Screen
```

- **Screen**: 상태를 받아 렌더링만 담당. 사용자 인터랙션은 `Intent`로 ViewModel에 전달.
- **ViewModel**: `Intent`를 받아 비즈니스 로직 처리 후 `UiState`를 갱신. Hilt로 주입.
- **UiState**: sealed interface (`Loading` / `Success(data)` / `Error(message)`).
- **SideEffect**: 일회성 이벤트(화면 전환, 토스트 등)는 `SharedFlow<UiEffect>`로 처리.
- **Repository**: 인터페이스로 정의, Hilt로 구현체 주입. 테스트 시 가짜 구현 교체 가능.
- **DataSource**: Remote(Ktor), Local(DataStore / Room).

## 화면 전환

Navigation Compose (`NavHost`)를 사용한다. Coordinator 패턴은 사용하지 않는다.
