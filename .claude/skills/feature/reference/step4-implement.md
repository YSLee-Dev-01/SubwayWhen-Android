# Step 4. 구현

## Step 4-1. 구현 준비

spec.md, plan.md, tasks.md 를 모두 읽고 미완료 Task 목록을 파악한다.
이미 `[x]` 로 표시된 Task 는 건너뛴다.

```
📋 구현할 Task 목록:
[tasks.md 의 미완료 Task 목록 출력]

구현을 시작하려면 '확인' 을 입력하세요.
```

---

## Step 4-2. Task별 순차 구현

'확인' 입력 후 미완료 Task를 **하나씩 순차적으로** 직접 구현한다.
SubAgent를 사용하지 않는다 — 이전 단계(spec/plan/탐색)에서 축적된 컨텍스트를 유지해야 하며, Task 간 의존성이 있어 병렬 실행이 불가능하기 때문이다.

각 Task마다 아래 순서를 따른다:
1. 해당 Task 구현
2. 코드 작성 후 빌드를 확인한다 (실패 시 2회까지 수정 시도)
3. 2회 초과 후에도 빌드가 실패하면 Task를 중단하고 사용자에게 보고한다
4. 해당 Task에 테스트 작성이 포함된 경우, 또는 기존 코드를 수정한 경우 관련 테스트를 실행한다
5. 완료 → `tasks.md` 해당 Task를 `[x]` 로 업데이트
6. 다음 Task 구현

모든 Task가 완료될 때까지 반복한다.

### 구현 규칙

- CLAUDE.md 의 코딩 컨벤션을 따른다
- 신규 파일 생성이 필요한 경우 plan.md 의 파일 경로를 따른다

### iOS → Compose 포팅 시 주의사항

iOS 원본을 Android Compose로 포팅할 때 자주 발생하는 함정:

- **`FontWeight.Heavy` 없음** → `FontWeight.ExtraBold` (800) 사용
- **하드코딩 색상 금지** — `Color.White` / `Color.Black` / `Color.Gray` 대신 `MaterialTheme.colorScheme.*` 사용 (`surface`, `onSurface`, `onSurfaceVariant`, `error` 등)
- **애니메이션 duration** — `tween(durationMillis = 250)` 대신 `tween(durationMillis = Dimens.animationDurationMs)` 사용
- **Dimens 토큰** — 2회 이상 반복되는 여백·크기는 `Dimens.kt`에 추가 후 참조
- **버튼 콜백 누락 주의** — iOS Button/TapGesture 핸들러가 있으면 AOS에도 반드시 콜백 파라미터로 노출해야 한다
- **문자열 suffix/prefix** — iOS에서 `"\(value)\(Strings.X.suffix)"` 패턴을 사용하면 AOS도 동일하게 구성한다
- **탭바 숨김** — 모달 open 시 `onTabBarVisibilityChange(false)`, 닫을 때 `onTabBarVisibilityChange(true)` 호출. 새 모달을 추가할 때마다 반드시 적용한다
- **모달 dismiss 애니메이션** — `CommonModalBottomSheet`의 `animatedDismiss` 콜백을 사용한다. `onDismiss`를 직접 호출하면 슬라이드 다운 없이 닫힘
- **탭 애니메이션** — iOS의 `scaleEffect` 탭 피드백은 `AnimatedTapBox`로 래핑하여 재현한다
- **Contract 파일** — 신규 화면은 `{기능명}Contract.kt`에 UiState, Intent, Effect를 함께 정의한다

---

## Step 4-3. 전체 코드 리뷰 + Critical 자동 수정 루프

모든 Task 완료 후 아래 루프를 실행한다.

### 루프

1. 변경된 전체 파일 목록을 대상으로 `review` 스킬을 실행한다
2. 🚨 Critical 이슈가 없으면 → 루프 종료, Step 4-4로 진행
3. 🚨 Critical 이슈가 있으면:
   - 해당 이슈를 직접 수정한다
   - 수정 완료 후 1번으로 돌아가 리뷰를 다시 실행한다
4. 2회 수정 시도 후에도 Critical이 남아있으면 사용자에게 보고하고 대기한다

---

## Step 4-4. 문서 업데이트

1. **spec.md AC 업데이트**
   - `## Acceptance Criteria` 항목 중 충족된 항목을 `- [ ]` → `- [x]` 로 변경

2. **구조 문서 업데이트** (새 파일/폴더 추가 또는 경로 변경이 있는 경우에만)
   - `.claude/CLAUDE.md` 관련 항목 업데이트
   - `.claude/rules/folder-structure.md` 관련 항목 업데이트

---

## Step 4-5.  전체 완료 보고

모든 Task 완료 및 Critical 이슈 해소 후 수행 결과를 보고 한다.

```
🎉 전체 구현 완료

📋 완료된 Task 목록:
[완료된 Task 번호 및 파일 목록 요약]

🔨 최종 빌드: 성공
🧪 테스트: 통과 / 생략

## 코드 리뷰 결과
[최종 리뷰 결과 출력]
[Critical 이슈가 있었다면 수정 완료 내역 출력]

[Acceptance Criteria 체크 결과 출력]

빌드 후 직접 동작을 확인해주세요.
```
