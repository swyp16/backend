# 0021. 승격 PR은 제목과 템플릿을 고정하고 CodeRabbit 리뷰를 건너뛴다

- 상태: 확정
- 날짜: 2026-11-27
- 이슈: 14-49

## 맥락

- [0020](0020-promotion-fast-forward-bot.md)에서 승격(`develop` → `production`, `production` → `main`)은 `/ff` 댓글 봇이 FF로 반영하기로 했다.
- 승격 PR에는 제목과 본문 형식이 없다. 피쳐 PR 템플릿(이슈, 변경 내용, 리뷰 포인트)은 승격 PR에 맞지 않는다.
- CodeRabbit이 승격 PR도 리뷰한다.
  - 승격 PR의 커밋은 피쳐 PR에서 이미 CodeRabbit 리뷰와 approve 2명을 거쳤다.
  - 리뷰가 끝날 때까지 CodeRabbit 체크가 진행 중이고, 봇은 진행 중인 체크가 있으면 `/ff`를 거부한다. PR #2에서 CodeRabbit 체크가 진행 중인 것을 확인했다.
  - 지적이 나와도 승격 PR에서는 고칠 수 없다. 수정은 피쳐 → `develop`으로 다시 들어와야 한다.
  - 시간당 리뷰 한도를 피쳐 PR과 나눠 쓴다.
- `auto_review.base_branches`는 기본 브랜치(`main`) 외에 추가로 리뷰할 브랜치 목록이다(CodeRabbit schema). `production`을 빼도 `production` → `main` PR은 리뷰된다. 또 `production`으로는 핫픽스 PR([0022](0022-hotfix-to-production.md))도 오고, 핫픽스 PR은 리뷰해야 한다.

## 결정

- 승격 PR 제목은 `Develop to production`, `Production to main`으로 고정한다. 날짜나 태그를 붙이지 않는다.
- 승격 PR 본문은 `.github/PULL_REQUEST_TEMPLATE/promotion.md`를 채운다.
  - 항목: 포함된 PR, 배포 영향(DB 마이그레이션, 설정·시크릿, API 변경), 체크리스트
  - `documents/git-workflow.md`에 제목과 템플릿이 채워진 채로 열리는 compare 링크를 둔다.
- CodeRabbit:
  - `base_branches`는 `develop`, `production` 그대로 둔다.
  - `ignore_title_keywords`에 두 고정 제목을 넣어 승격 PR 리뷰를 건너뛴다. 키워드는 대소문자를 구분하지 않는 부분 일치다.
- 피쳐 PR과 핫픽스 PR의 제목은 CI(`naming`)가 `[{브랜치의 이슈 키}] 요약`인지, 승격 PR 제목 문구가 들어 있지 않은지(대소문자 무시) 검사한다. 그래서 고정 제목 문구로 CodeRabbit을 건너뛸 수 없다. 제목을 고치면 다시 검사하도록 `naming`은 `edited` 이벤트도 받는 별도 워크플로(`.github/workflows/naming.yml`)로 둔다.

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| 승격 PR 제목에 날짜를 붙인다 | 고정 제목이어야 `ignore_title_keywords` 하나로 CodeRabbit을 끌 수 있다 |
| 승격 PR 본문은 매번 손으로 쓴다 | 사람마다 내용이 달라진다 |
| 승격 PR 본문을 봇이 자동으로 쓴다 | 지금은 템플릿 하나로 충분하다 |
| `base_branches`에서 `production`을 뺀다 | 기본 브랜치 `main`으로 가는 PR은 계속 리뷰되고, 핫픽스 PR이 리뷰되지 않는다 |
| CodeRabbit은 두고 `/ff`가 CodeRabbit 체크만 무시한다 | 중복 리뷰와 한도 소모가 그대로 남는다 |

## 트레이드오프

- **얻는 것:** 승격 PR을 열면 바로 `/ff`할 수 있다. CodeRabbit 한도를 피쳐 PR에만 쓴다.
- **잃는 것:**
  - 여러 피쳐 PR이 합쳐진 전체 diff를 CodeRabbit이 한 번 더 보는 기회가 없다.
  - CodeRabbit이 승격 PR을 건너뛰는 근거가 제목뿐이다. 승격 PR 제목을 바꾸면 리뷰가 돈다.
