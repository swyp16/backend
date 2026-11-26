# 0022. 핫픽스는 `hotfix/{이슈 키}` → `production`으로 넣고, 봇이 `develop`에 되합친다

- 상태: 확정 (GitHub App 등록과 ruleset 적용은 나중에)
- 날짜: 2026-11-27
- 이슈: 14-49

## 맥락

- 승격은 FF로만 한다([0020](0020-promotion-fast-forward-bot.md)). `production`이 `develop`의 조상이어야 다음 승격이 된다.
- 운영의 급한 버그를 `develop`을 거쳐 고치면, `develop`에 아직 내보내면 안 되는 커밋이 쌓여 있을 때 그 커밋까지 같이 나간다.
- `production`에 핫픽스 커밋 H를 직접 넣으면 `develop`에 H가 없다. 그러면 다음 `develop` → `production` 승격이 FF가 아니라서 봇이 거부한다.
- H를 `develop`에 Rebase and merge(또는 cherry-pick)로 넣으면 SHA가 바뀌어서 여전히 FF가 안 된다. H 자체가 `develop`의 조상이 되어야 한다.

## 결정

- 핫픽스는 `production`에서 `hotfix/{이슈 키}` 브랜치를 따서 고치고, `production`으로 PR을 올린다.
- 반영 조건은 피쳐 PR과 같고, 봇이 확인한다.
  - CI(`check`, `naming`)와 CodeRabbit 체크 통과
  - 협업자 approve 2명(협업자가 아닌 계정의 리뷰는 세지 않는다)
  - 해결하지 않은 리뷰 대화 없음
- 반영은 `/ff` 댓글로 한다. 봇이 `production`을 PR head로 FF한다.
- 봇은 이어서 `develop`에도 합친다.
  - `develop`이 H의 조상이면 FF한다.
  - 갈라졌으면 GitHub Merges API로 머지 커밋(`Merge hotfix/{이슈 키} into develop`)을 만든다. H는 그대로 `develop`의 조상이 된다.
  - 이미 들어 있으면 아무것도 하지 않는다.
- 합치다 충돌하면 봇이 PR 댓글로 알리고 실패로 끝난다. `production`은 이미 옮겨진 상태다. 원래 핫픽스 PR이 열려 있으면 먼저 닫는다(같은 브랜치에 `develop`을 합쳐 올리면 그 PR에 `develop` 전체가 실린다). 그다음 사람이 `develop`에서 같은 이름의 브랜치를 다시 만들어 `production`을 merge하고, `develop`으로 PR을 올린다. approve 2명을 받은 뒤 `/ff`하면, 봇이 `production`을 포함한 브랜치인지 확인하고 `develop`을 FF한다.
- `naming` 검사는 `production`으로 오는 `hotfix/` PR에도 돈다. 머지 커밋은 검사하지 않는다.
- CodeRabbit은 `production`으로 오는 PR을 리뷰한다. 승격 PR은 제목으로 건너뛴다([0021](0021-promotion-pr-title-template-no-coderabbit.md)).
- 봇 App은 `develop` ruleset의 우회 목록에도 넣는다. 머지 커밋과 FF를 직접 쓰기 때문이다.

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| 핫픽스도 `develop`을 거친다 | `develop`에 아직 내보내면 안 되는 커밋이 있으면 같이 나간다 |
| 사람이 `production` → `develop` PR을 머지 커밋으로 합친다 | `develop`에서 머지 커밋 방식을 켜야 한다. 그러면 피쳐 PR도 실수로 머지 커밋으로 합쳐질 수 있다 |
| `develop`에 cherry-pick | SHA가 달라 다음 승격이 FF가 안 된다 |
| 핫픽스 승인 1명 이하 | 운영에 바로 나가는 변경이다. 피쳐와 같은 기준을 둔다 |

## 트레이드오프

- **얻는 것:**
  - `develop`의 상태와 상관없이 운영 버그만 내보낸다.
  - 되합치기를 사람이 잊지 않는다. 다음 승격도 그대로 FF된다.
- **잃는 것:**
  - 핫픽스가 있을 때마다 `develop`에 머지 커밋이 생긴다. 그 부분은 이력이 선형이 아니다. `develop` ruleset에 "선형 이력 필수"를 걸 수 없다.
  - 급한 상황에도 approve 2명을 기다려야 한다.
  - 충돌이 나면 `production`만 먼저 바뀌고, `develop`은 사람이 합칠 때까지 핫픽스가 빠진 상태로 남는다. 그사이에는 승격할 수 없다.
  - 봇 App이 `develop`에도 직접 쓸 수 있다.
