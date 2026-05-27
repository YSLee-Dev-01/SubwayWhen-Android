# Plan: Search-2 (검색 결과 저장 Modal)

## 참조 Spec
- @specs/features/Search-2/spec.md

## 참조 Skill
신규 화면 생성 시
- @skills/create-feature/SKILL.md

---

## 현재 상태 파악

### 신규
- `feature/search/modal/SaveStationModalContract.kt` — UiState / Intent / Effect (sealed)
- `feature/search/modal/SaveStationModalViewModel.kt` — MVI ViewModel, 저장/중복/Disposable 로직
- `feature/search/modal/SaveStationModal.kt` — Modal 본체 Composable (호선원·역명·출퇴근·중간종착역TF·상/하행 버튼)
- `feature/search/modal/component/DisposableView.kt` — Modal 상단의 일회성 진입 View (임시 UI만, Detail 연동은 다음 spec)
- `feature/search/modal/component/SaveCompletedModal.kt` — 저장 완료 Modal (타이틀·로띠·확인 버튼·축하 애니메이션)
- `data/repository/ModalLineMapper.kt` (or `feature/search/modal/LineToKorailCodeMapper.kt`) — iOS `ModalModel.useLineTokorailCode` 대응 (호선명 → 코레일 코드)

### 재사용
- `ui/common/modal/CommonModalBottomSheet.kt` — 메인/서브 title, 하단 본문 컨테이너 그대로 활용 (이미 구현됨)
- `ui/common/modal/ModalSubButton.kt` — 상행/하행/출퇴근/notService 버튼에 활용
- `ui/common/StationLineCircle.kt` — 호선 원형 표시
- `ui/common/SubwayLineMapper.kt` — `subwayLineDisplayName` / `subwayLineColor` 활용
- `data/model/SaveStation.kt`, `SaveStationGroup.kt` — 저장 모델 (이미 정의됨)
- `data/repository/LocalDataRepository.kt` — `saveStations` StateFlow + `updateSaveStations` API 활용 (저장 경로)
- `core/FixInfo.kt` — DataStore 영속화 경로 (Repository 통해서 간접 사용)
- `app/src/main/res/raw/congratulations.json` — 축하 애니메이션 (이미 존재)
- `app/src/main/res/raw/tutorial_success.json` — 저장 완료 체크 애니메이션 (iOS `CheckMark.json` 대응 후보, 또는 iOS `TutorialSuccess.json` 그대로 사용)
- Lottie 라이브러리 — `libs.versions.toml`에 `lottie-compose 6.6.6` 이미 추가되어 있음 (별도 의존성 추가 불필요)

### 수정
- `feature/search/SearchContract.kt`
    - `SearchUiState`에 `selectedStation: SearchStationInfo?` 추가 (모달 표출용)
    - `SearchUiState`에 `isSaveCompletedModalVisible: Boolean` 또는 별도 상태 추가
    - `SearchIntent`에 `ModalDismissed`, (필요 시 `SaveCompletedDismissed`) 추가
- `feature/search/SearchViewModel.kt`
    - `ResultStationTapped` 처리: `selectedStation` 설정 → Modal 표출
    - `QueryRecommendStationTapped` 처리도 동일 (이미 결과 갱신 후 탭 → 동일 흐름 합류)
    - 저장 완료 후 Effect로 `ShowSaveCompletedModal` 발행 또는 UiState 토글
- `feature/search/SearchScreen.kt`
    - `SaveStationModal` 호출 (`selectedStation`이 not null일 때 표시)
    - `SaveCompletedModal` 호출 (저장 완료 시)

### 삭제
- 없음

---

## 기술적 결정사항

- **Modal 호출 방식**: Material3 `ModalBottomSheet` (이미 `CommonModalBottomSheet`로 래핑됨) 사용.
    - 사유: iOS의 "화면 일부만 덮는" overFullScreen + custom height(`381`) 동작을 가장 자연스럽게 매핑. Compose 표준이며 드래그 dismiss/백드롭 탭 dismiss 모두 자동.
    - 대안: 별도 Dialog 구현 — 제외 (이미 CommonModalBottomSheet 존재).

- **저장 로직 위치**: `SaveStationModalViewModel` 내부에서 `LocalDataRepository.updateSaveStations` 호출.
    - 사유: iOS `ModalViewModel.stationSave`와 1:1 대응. SearchViewModel을 비대하게 만들지 않음.
    - 대안: SearchViewModel에 통합 — 제외 (모달 재사용 가능성 고려).

- **저장 완료 모달 분리**: 별도 Composable + 별도 ViewModel은 만들지 않고, 저장 결과는 SaveStationModalViewModel의 Effect로 발행 → SearchScreen에서 두 번째 BottomSheet/Dialog로 표시.
    - 사유: simplicity. 저장 완료 모달은 단순 표시 + 확인 버튼만 있음.
    - 대안: 별도 ViewModel — 제외 (상태 없음).

- **중복 저장 처리**: iOS는 `searchOverlapAlert` 설정에 따라 알림 후 미저장. Android는 동일하게 `SaveSetting.searchOverlapAlert` (이미 SaveSetting에 존재한다고 가정, 없으면 무조건 중복 차단)로 처리.
    - 결정: spec에 명시 없음 → iOS 동작 그대로 차용. 중복 시 Toast/AlertDialog 1개로 안내 후 모달 유지.
    - **유의**: spec의 "잘못될 수 있는 것"에 "로컬 데이터 저장 실패"가 있으나, 중복 알림은 동작 명세에 없음. 일단 iOS 패리티를 위해 중복 차단만 구현하고, 알림 UI는 SnackBar 또는 간단한 Dialog로 처리.

- **DataStore 저장 구조**: 변경 없음. 기존 `StationLocalDataSource.updateSaveStations(list)` 그대로 사용.
    - `SaveStation` 모델은 이미 `kotlinx.serialization.Serializable` + JSON 직렬화 구성 완료.
    - id는 `UUID.randomUUID().toString()` 사용 (iOS와 동일).

- **호선명 → 코레일 코드 매핑**: iOS `ModalModel.useLineTokorailCode` 그대로 Kotlin 함수로 포팅.
    - 위치: `feature/search/modal/LineToKorailCodeMapper.kt` (단일 함수, 인터페이스 없음)
    - 매핑: 경의중앙→K4, 수인분당→K1, 경춘→K2, 우이→UI, 신분당→D1, 공항→A1, 그 외→""

- **상/하행 텍스트**: iOS `upDownText(isUp:)` 동작 포팅 필요.
    - 2호선만 "내선/외선", 그 외는 "상행/하행"
    - 위치: `ui/common/SubwayLineMapper.kt`에 `subwayLineUpDownText(line: String, isUp: Boolean): String` 추가 (재사용 대비)

- **Lottie 의존성**: 이미 `libs.versions.toml`에 `lottie 6.6.6` + `lottie-compose` 정의됨. **추가 작업 불필요.**
    - `app/build.gradle.kts`의 dependencies에 `implementation(libs.lottie.compose)`가 이미 선언되어 있는지 확인 필요. 미선언 시 추가.
    - Lottie 파일은 `res/raw/congratulations.json` (축하) / `res/raw/tutorial_success.json` (저장 완료 체크) 이미 존재 → 그대로 사용.

- **축하 애니메이션 오버레이**: 저장 완료 Modal **위에** Lottie 애니메이션을 송출 (spec 명세).
    - 구현: `SaveCompletedModal` Composable 내부에서 `Box`로 BottomSheet content와 풀스크린 Lottie를 분리하거나, Modal 표출 시 SearchScreen 최상단에 `LottieAnimation`을 `zIndex` 높여 오버레이.
    - 결정: SearchScreen 레벨에서 `Box`로 감싸고, `isSaveCompletedModalVisible` 시 Lottie를 fullScreen overlay로 띄움 (확인 버튼 탭 시 두 가지 모두 dismiss).

- **DisposableView**: spec에 "View만 임시로 만들어줘" → 클릭 시 동작은 TODO 주석 + 빈 람다. UI만 iOS 디자인 충실 재현.

---

## 구현 순서

### Phase 1. 모델/매퍼 레이어
- `feature/search/modal/LineToKorailCodeMapper.kt` 생성 — `lineToKorailCode(line: String): String` 단일 함수
- `ui/common/SubwayLineMapper.kt` 에 `subwayLineUpDownText(line: String, isUp: Boolean): String` 추가
- 검증: 단위 테스트(`LineToKorailCodeMapperTest`, `SubwayLineMapperTest`) — 경의중앙/수인분당/공항/2호선/일반 케이스

### Phase 2. Modal ViewModel/Contract
- `feature/search/modal/SaveStationModalContract.kt` 생성
    - `SaveStationModalUiState(group, exceptionLastStation, station, isServiceSupported)`
    - `SaveStationModalIntent(GroupToggled, ExceptionChanged, UpButtonTapped, DownButtonTapped, DisposableUpTapped, DisposableDownTapped, Dismissed)`
    - `SaveStationModalEffect(SaveCompleted, AlreadyExists, DisposableDetailNavigate(...), Close)`
- `feature/search/modal/SaveStationModalViewModel.kt` 생성
    - `LocalDataRepository` 주입 (Hilt `@AssistedInject` 또는 `@HiltViewModel` + 초기 데이터를 SavedStateHandle/argument로 전달)
    - 상/하행 탭 시: 중복 체크 → `updateSaveStations` 호출 → Effect 발행
    - 그룹 토글: ONE ↔ TWO
- 검증: 단위 테스트
    - 신규 저장 시 `LocalDataRepository.updateSaveStations`가 기존 list + 새 SaveStation으로 호출됨
    - 중복 저장 시 `AlreadyExists` Effect 발행 + repository 미호출
    - 그룹 토글 동작
    - 콤마 입력 그대로 `exceptionLastStation`에 저장

### Phase 3. Modal UI Composable
- `feature/search/modal/component/DisposableView.kt` — 임시 UI (제목 라벨 + 상/하행 작은 버튼 2개), `AppIconColor` 유사 컬러 + alpha 0.7
- `feature/search/modal/SaveStationModal.kt`
    - `CommonModalBottomSheet`를 사용하여 mainTitle="지하철 역 추가", subTitle="그룹, 제외 행을 선택 후 상/하행 버튼을 누르면 저장할 수 있어요."
    - 본문 레이아웃 (iOS와 동일 배치):
        - 상단: `StationLineCircle`(line 색·이름) + 역명 Text
        - 중간 (좌우 2열): 출퇴근 토글 버튼 / 중간 종착역 제거 OutlinedTextField (콤마 placeholder)
        - 하단 (좌우 2열): 상행/내선 버튼(빨강) / 하행/외선 버튼(파랑)
        - 서비스 미지원 노선(`lineCode == ""`)일 경우 하나의 검정 풀와이드 버튼만 노출
    - 위에 `DisposableView` 오버레이 (BottomSheet 외부 또는 sheet 상단)
    - Preview: 일반 1호선, 2호선(내선/외선), 비서비스 노선
- 검증: Preview 다크/라이트 모두 정상, iOS 스크린샷과 시각적 비교

### Phase 4. 저장 완료 Modal + 축하 애니메이션
- `feature/search/modal/component/SaveCompletedModal.kt`
    - `CommonModalBottomSheet` 사용, mainTitle/subTitle은 iOS 동일 문구 그대로
    - 본문: `LottieAnimation`(res/raw/tutorial_success.json) + 확인 버튼(`ModalSubButton`)
- SearchScreen 최상단에 `Box` overlay — `isSaveCompletedModalVisible` 시 `LottieAnimation`(congratulations.json) 풀스크린, `iterations = 1`, 종료 후 자동 제거
- 확인 버튼 탭 시 `Close` Intent → 모달 + 오버레이 모두 dismiss
- 검증: Preview, 확인 버튼 탭 시 dismiss 정상 동작

### Phase 5. SearchScreen 통합
- `SearchUiState`에 `selectedStation`, `isSaveCompletedModalVisible` 추가
- `SearchViewModel.ResultStationTapped` / `QueryRecommendStationTapped` 후속 처리: `selectedStation = item`
- `SearchScreen`에서 `selectedStation` not null일 때 `SaveStationModal` 호출, `SaveCompletedModal`은 별도 상태에 의해 호출
- `SaveStationModalEffect.SaveCompleted` 수신 시 SearchViewModel이 `selectedStation = null`, `isSaveCompletedModalVisible = true`로 갱신
- 검증: 통합 동작 — 검색 결과 탭 → 저장 모달 → 상행 탭 → 저장 모달 닫힘 → 저장 완료 모달 + 애니메이션 표출 → 확인 → 모두 닫힘

### Phase 6. 의존성/리소스 점검
- `app/build.gradle.kts`에 `implementation(libs.lottie.compose)` 선언 여부 확인, 없으면 추가
- DI: `SaveStationModalViewModel`을 `@HiltViewModel`로 등록 (또는 AssistedInject + `hiltViewModel<>(creationCallback = ...)`)
    - 결정: 초기 데이터(`SearchStationInfo`)는 SavedStateHandle 또는 Composable parameter로 전달. AssistedInject로 station 주입 권장 (네비게이션 미사용 케이스).

---

## 완료 조건
- [ ] Spec Acceptance Criteria 충족
    - [ ] 검색 결과 로딩 성공 시 Modal에 호선 색/호선명/역명/출퇴근/상하행/중간종착역 TF가 모두 표시됨
    - [ ] 상행/하행 버튼 탭 시 SaveStation이 `LocalDataRepository`를 통해 DataStore에 저장됨
    - [ ] 출근/퇴근 토글 정상 동작 (ONE ↔ TWO)
    - [ ] 중간 종착역 콤마 구분 입력값이 `exceptionLastStation`에 그대로 저장됨
    - [ ] 저장 완료 후 SaveStationModal 닫힘 + SaveCompletedModal 표출 + 축하 애니메이션(congratulations.json) 송출
    - [ ] 저장 완료 Modal의 확인 버튼 탭 시 Modal + 오버레이 모두 dismiss
    - [ ] 2호선만 "내선/외선", 그 외 "상행/하행" 표기
    - [ ] 비서비스 노선(`lineCode == ""`)은 "해당 노선은 서비스를 지원하지 않아요." 버튼만 표시
- [ ] DisposableView UI만 임시 구현 (탭 동작은 TODO)
- [ ] 단위 테스트 통과 (LineToKorailCodeMapper, SaveStationModalViewModel)
- [ ] Compose Preview 다크/라이트 정상
