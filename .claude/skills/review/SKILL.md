---
name: review
description: iOS 기능 비교 및 AOS 코드 품질을 병렬로 검증한다.
argument-hint: "[파일 또는 폴더 경로] (생략 시 git diff 기준)"
allowed-tools:
  - Read
  - Bash(git diff --name-only *)
  - Agent
---

# review

변경된 코드를 두 가지 관점에서 병렬로 검증한다.

---

## 리뷰 대상 파일 결정

- `$ARGUMENTS` 가 있으면: 해당 파일/폴더를 리뷰 대상으로 삼는다
- `$ARGUMENTS` 가 없으면: `git diff --name-only HEAD` 로 변경된 파일 목록을 가져온다

---

## SubAgent 병렬 실행

리뷰 대상 파일 목록을 확정한 후, 아래 두 SubAgent를 **동시에** 실행한다.

### iOS 비교 SubAgent
Agent 도구를 model: "sonnet" 로 `reference/ios-compare-agent.md` 의 지시를 따르되,
리뷰 대상 파일 목록을 프롬프트에 포함하여 실행한다.

### AOS 품질 SubAgent
Agent 도구를 model: "sonnet" 으로 `reference/aos-quality-agent.md` 의 지시를 따르되,
리뷰 대상 파일 목록을 프롬프트에 포함하여 실행한다.

---

## 결과 취합 및 보고

두 SubAgent 결과를 받은 후 아래 형식으로 보고한다:

```
## 코드 리뷰 결과

### 🚨 Critical (반드시 수정)
- [iOS비교/AOS품질] 내용 (파일:라인)

### ⚠️ Important (수정 권장)
- [iOS비교/AOS품질] 내용 (파일:라인)

### 💡 Suggestion (선택)
- [iOS비교/AOS품질] 내용 (파일:라인)

### ✅ 양호한 부분
- 내용
```

이슈가 없으면: `✅ 리뷰 이슈 없음` 을 출력한다.
