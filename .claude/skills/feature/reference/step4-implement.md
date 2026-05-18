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

'확인' 입력 후 미완료 Task를 **하나씩 순차적으로** SubAgent로 구현한다.

각 Task마다 아래 순서를 따른다:
1. Task SubAgent 실행 (model: sonnet)
2. 완료 → `tasks.md` 해당 Task를 `[x]` 로 업데이트
3. 다음 Task SubAgent 실행

모든 Task가 완료될 때까지 반복한다.

### Task SubAgent 프롬프트 템플릿

```
역할: Android 개발자
목표: 아래 Task를 구현한다.

## 컨텍스트 파일
- `.claude/specs/features/$ARGUMENTS/spec.md`
- `.claude/specs/features/$ARGUMENTS/plan.md`
- `.claude/specs/features/$ARGUMENTS/tasks.md`
- `.claude/CLAUDE.md` 및 관련 rules 파일

## 수행할 Task
[해당 Task 번호 및 내용 전달]

## 구현 규칙
- CLAUDE.md 의 코딩 컨벤션을 따른다
- 신규 파일 생성이 필요한 경우 plan.md 의 파일 경로를 따른다
- 코드 작성 후 빌드를 확인한다 (실패 시 2회까지 수정 시도)
- 2번을 초과하여 빌드가 실패된 경우 Task를 중단하고 사용자에게 보고한다.
- 해당 Task에 테스트 작성이 포함된 경우, 또는 기존 코드를 수정한 경우 관련 테스트를 실행한다
- 구현 완료 후 변경된 파일 목록, 빌드 결과, 테스트 결과를 반환한다

## iOS → Compose 포팅 시 주의사항

iOS 원본을 Android Compose로 포팅할 때 자주 발생하는 함정:

- **`FontWeight.Heavy` 없음** → `FontWeight.ExtraBold` (800) 사용
- **하드코딩 색상 금지** — `Color.White` / `Color.Black` / `Color.Gray` 대신 `MaterialTheme.colorScheme.*` 사용 (`surface`, `onSurface`, `onSurfaceVariant`, `error` 등)
- **애니메이션 duration** — `tween(durationMillis = 250)` 대신 `tween(durationMillis = Dimens.animationDurationMs)` 사용
- **Dimens 토큰** — 2회 이상 반복되는 여백·크기는 `Dimens.kt`에 추가 후 참조
- **버튼 콜백 누락 주의** — iOS Button/TapGesture 핸들러가 있으면 AOS에도 반드시 콜백 파라미터로 노출해야 한다
- **문자열 suffix/prefix** — iOS에서 `"\(value)\(Strings.X.suffix)"` 패턴을 사용하면 AOS도 동일하게 구성한다
```

---

## Step 4-3. 전체 코드 리뷰 + Critical 자동 수정 루프

모든 Task 완료 후 아래 루프를 실행한다.

### 루프

1. 변경된 전체 파일 목록을 대상으로 `review` 스킬을 실행한다
2. 🚨 Critical 이슈가 없으면 → 루프 종료, Step 4-4로 진행
3. 🚨 Critical 이슈가 있으면:
   - SubAgent를 실행하여 해당 이슈를 수정한다
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
