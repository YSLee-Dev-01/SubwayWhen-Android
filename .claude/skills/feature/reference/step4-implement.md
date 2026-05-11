# Step 4. 구현

## Step 4-1. 구현 준비

spec.md, plan.md, tasks.md 를 모두 읽고 미완료 Task 목록을 파악한다.
이미 `[x]` 로 표시된 Task 는 건너뛴다.

```
📋 구현할 Task 목록:
[tasks.md 의 미완료 Task 목록 출력]

구현을 시작하려면 '확인' 을 입력하세요.
```

## Step 4-2. Task별 SubAgent 순차 구현

'확인' 입력 후 미완료 Task를 하나씩 SubAgent로 구현한다.

각 Task마다 아래 사이클을 반복한다:

### 1. Task SubAgent 실행

Agent 도구를 model: "sonnet" 으로 아래 내용으로 실행한다:

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
- 해당 Task에 테스트 작성이 포함된 경우, 또는 기존 코드를 수정한 경우 관련 테스트를 실행한다
- 구현 완료 후 변경된 파일 목록, 빌드 결과, 테스트 결과를 반환한다
```

### 2. 코드 리뷰 실행

Task SubAgent가 반환한 변경 파일 목록을 `$ARGUMENTS`로 넘겨 `review` 스킬을 실행한다.

- 🚨 Critical 이슈가 있으면: Task SubAgent를 다시 실행하여 해당 이슈를 수정한다. 수정 후 리뷰를 재실행한다. Critical이 모두 해소될 때까지 반복한다. (2회 시도 후에도 해소되지 않으면 사용자에게 보고하고 대기한다)
- Critical 이슈가 없으면: 리뷰 결과를 저장해두고 다음 단계로 진행한다.

### 3. SubAgent 완료 후 tasks.md 업데이트

해당 Task를 `tasks.md` 에서 `[x]` 로 업데이트한다.

### 4. 인간 확인 요청

```
[인간 확인 요청 - 반드시 대기]
✅ Task N 완료
📝 변경 파일: [SubAgent가 반환한 파일 목록]
🔨 빌드: 성공
🧪 테스트: 통과 / 생략

[리뷰 결과 출력 — 이슈 없으면 "✅ 리뷰 이슈 없음"]

변경 내용을 확인해주세요.
계속하려면 '확인', 수정이 필요하면 내용을 알려주세요.
```

'확인' 입력 전까지 다음 Task SubAgent를 절대 실행하지 않는다.

## Step 4-3. 문서 업데이트

모든 Task 완료 후 아래 순서로 문서를 업데이트한다.

1. **spec.md AC 업데이트**
   - `## Acceptance Criteria` 항목 중 충족된 항목을 `- [ ]` → `- [x]` 로 변경

2. **구조 문서 업데이트** (새 파일/폴더 추가 또는 경로 변경이 있는 경우에만)
   - `.claude/CLAUDE.md` 관련 항목 업데이트
   - `.claude/rules/folder-structure.md` 관련 항목 업데이트

## Step 4-4. 전체 완료 보고

```
🎉 전체 구현 완료

[Acceptance Criteria 체크 결과 출력]

빌드 후 직접 동작을 확인해주세요.
```
