# 0007. PR 1차 리뷰는 CodeRabbit

- 상태: 확정
- 날짜: 2026-11-26 (요금과 기능은 이 날짜에 공식 문서로 확인)
- 이슈: 14-37

## 맥락

사람 두 명이 approve하기 전에 1차 리뷰를 봇이 맡는다. 기계 규칙은 CI가 막으므로([0006](0006-ci-enforced-code-rules.md)), 봇에는 판단이 필요한 것을 맡긴다.

- 한 클래스·메서드가 너무 많은 책임을 지지 않는지
- 필요한 추상화만 있는지, 더 단순한 방법이 있는지
- 메서드명·변수명이 기존 코드와 맞는지

## 결정

- CodeRabbit을 쓴다.
- 위 세 가지를 repo의 설정 파일(`.coderabbit.yaml`)에 리뷰 지침으로 적는다.

## 선택 이유

- `backend`는 공개 repo라 오픈소스 플랜으로 무료다(Team 기능 포함).
- PR에 줄 단위로 리뷰한다.
- 별도 API 키나 결제가 필요 없다.
- 지침을 repo 파일로 관리해서 PR로 바꾼다.

## 제약

- 리뷰가 자동으로 돌지 않으면 PR에 `@coderabbitai review`를 댓글로 단다.
- 오픈소스 플랜에는 시간당 리뷰 한도가 있다.

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| GitHub Copilot 코드 리뷰 | 2026-06-01부터 사용량 과금이고, 리뷰가 AI 크레딧과 Actions 시간을 쓴다. 리뷰를 요청하는 사람에게 Copilot 플랜이 필요하다. 학생 플랜 신규 가입이 2026-04부터 중단됐다는 공지가 있고 재개 여부가 불확실하다 |
| Claude Code Action | Anthropic API(또는 Bedrock·Vertex) 사용량만큼 과금된다. 팀 공용 키를 누가 결제하고 관리할지 정해야 한다 |
| Gemini Code Assist | 공식 문서에서 확인되는 GitHub 리뷰는 엔터프라이즈판뿐이다. Google Cloud 결제 계정이 필요하고 Preview 상태다 |

## 트레이드오프

- **얻는 것:** 비용 없이 판단 영역의 1차 리뷰를 받는다.
- **잃는 것:**
  - 한도에 걸리면 리뷰가 밀린다.
  - 봇의 지적은 참고용이다. approve는 사람이 한다.


## 근거

- [CodeRabbit plans](https://docs.coderabbit.ai/management/plans)
- [GitHub Copilot 코드 리뷰 커스터마이즈](https://docs.github.com/en/copilot/tutorials/customize-code-review)
- [GitHub Copilot 학생 플랜 변경 (2026-03-13)](https://github.blog/changelog/2026-03-13-updates-to-github-copilot-for-students)
- [claude-code-action](https://github.com/anthropics/claude-code-action)
- [Gemini Code Assist on GitHub](https://docs.cloud.google.com/gemini/docs/code-review/review-repo-code)
