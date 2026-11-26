# 브랜치·커밋·PR

## 이름

| 항목 | 규칙 | 예 |
|---|---|---|
| 브랜치 | `feature/{이슈 키}` | `feature/14-38` |
| 핫픽스 브랜치 | `hotfix/{이슈 키}` | `hotfix/14-60` |
| 커밋 | `{타입}({이슈 키}): 요약`. 타입은 `feat`, `fix`, `refactor`, `test`, `docs`, `chore` | `feat(14-38): 엔티티 공통 규칙 추가` |
| PR 제목 | `[{이슈 키}] 요약`. 키는 브랜치와 같다 | `[14-38] 엔티티 공통 규칙` |

커밋마다 이슈 키를 넣는다. CI(`.github/workflows/naming.yml`)가 `develop`으로 오는 PR과 핫픽스 PR의 브랜치 이름, PR 제목, 모든 커밋 메시지를 검사한다. 머지 커밋은 검사하지 않는다. PR 제목에는 승격 PR 제목(`Develop to production`, `Production to main`)을 넣지 않는다.

## 피쳐 → `develop`

- 피쳐 브랜치에서 `develop`으로 PR을 올린다. 본문은 템플릿(`.github/pull_request_template.md`)을 채운다.
- CodeRabbit이 1차 리뷰를 한다. 자동으로 돌지 않으면 PR에 `@coderabbitai review` 댓글을 단다.
- 사람 두 명이 approve하면 Rebase and merge로 합친다. approve는 협업자 누구나 할 수 있고, approve 뒤에 push해도 유지된다. 리뷰 대화는 모두 해결해야 한다.

## 승격 (`develop` → `production` → `main`)

- 배포는 `develop` → `production` PR, 그다음 `production` → `main` PR로 한다. 배포할 때 `production`에 태그 `v{YYYYMMDD}-{짧은 해시}`를 단다.
- 제목은 `Develop to production`, `Production to main`으로 고정하고, 본문은 승격 템플릿(`.github/PULL_REQUEST_TEMPLATE/promotion.md`)을 채운다. 아래 링크로 열면 제목과 템플릿이 채워진다.
  - [Develop to production](https://github.com/swyp16/backend/compare/production...develop?expand=1&template=promotion.md&title=Develop%20to%20production)
  - [Production to main](https://github.com/swyp16/backend/compare/main...production?expand=1&template=promotion.md&title=Production%20to%20main)
- 머지 버튼 대신 `/ff` 댓글로 반영한다. 봇과 승격 템플릿은 `main`에 있는 파일로 동작한다. 봇이 체크 통과와 FF 가능 여부를 확인하고 base 브랜치를 PR head 커밋으로 옮긴다(`.github/workflows/fast-forward.yml`, 변수 `FF_APP_CLIENT_ID`, 시크릿 `FF_APP_PRIVATE_KEY`).
- CodeRabbit은 승격 PR을 리뷰하지 않는다.
- 핫픽스가 `develop`에 머지 커밋으로 합쳐진 뒤에도 승격은 FF로 된다.

## 핫픽스 (`hotfix/{이슈 키}` → `production`)

- 운영의 급한 버그는 `production`에서 `hotfix/{이슈 키}`를 따서 고치고 `production`으로 PR을 올린다. 제목과 본문은 피쳐 PR과 같다.
- CI(`check`, `naming`), CodeRabbit 리뷰, approve 2명, 대화 해결을 마친 뒤 `/ff` 댓글로 반영한다.
- 봇이 `production`을 옮긴 뒤 `develop`에도 합친다. `develop`이 핫픽스 커밋의 조상이면 FF하고, 아니면 머지 커밋을 만든다.
- 합치다 충돌하면 봇이 PR 댓글로 알린다. 그때는 원래 핫픽스 PR이 닫혔는지 먼저 확인하고, 열려 있으면 닫는다. 그다음 `develop`에서 브랜치를 다시 만들어 `production`을 merge하고, `develop`으로 PR을 올려 approve 2명을 받은 뒤 `/ff`한다.
  ```bash
  git fetch origin
  git switch -C hotfix/14-60 origin/develop
  git merge origin/production          # 충돌을 풀고 커밋한다
  git push --force-with-lease origin hotfix/14-60
  ```

## 알림

- 디스코드 알림은 PR 생성, `develop`·`production`·`main` 반영, `v*` 태그 생성 때 간다(`.github/workflows/discord.yml`, 시크릿 `DISCORD_WEBHOOK_URL`).
