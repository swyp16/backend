# 0020. 승격은 `/ff` 댓글 봇이 FF로 반영한다

- 상태: 확정 (GitHub App 등록, ruleset, 머지 방식 설정은 나중에)
- 날짜: 2026-11-27
- 이슈: 14-45

## 맥락

- `develop` → `production` → `main` 승격에서 커밋 SHA를 그대로 유지하고 싶다. 세 브랜치에서 같은 커밋이 같은 해시를 가져야 한다.
- GitHub PR 머지 버튼에는 FF가 없다.
  - Create a merge commit: 머지 커밋이 생긴다.
  - Squash and merge: 커밋을 하나로 합친다.
  - Rebase and merge: FF할 수 있는 상태여도 커밋을 항상 새로 써서 SHA와 committer가 바뀐다.
- 사람이 로컬에서 `git push origin develop:production`을 하면 FF는 된다. 하지만 사람이 보호 브랜치에 직접 push하는 것은 정책으로 막는다.
- 피쳐 → `develop`은 SHA가 바뀌어도 상관없다.

## 결정

- 피쳐 → `develop`: Rebase and merge 버튼으로 합친다. repo 설정에서 merge commit과 squash는 끈다.
- 승격(`develop` → `production`, `production` → `main`):
  - 사람이 PR을 연다. 쓰기 권한이 있는 사람이 `/ff` 댓글을 단다.
  - `.github/workflows/fast-forward.yml`(`issue_comment`)이 확인을 마친 뒤 GitHub App 토큰으로 `PATCH git/refs/heads/{base}`(`force=false`)를 호출한다.
  - 확인 항목:
    - 댓글 첫 줄이 `/ff`인지
    - 작성자 권한이 write 이상인지(OWNER·MEMBER·COLLABORATOR가 아니면 토큰을 만들기 전에 거른다)
    - PR이 열려 있고 같은 repo의 브랜치인지
    - 브랜치 쌍이 맞는지
    - 협업자(OWNER·MEMBER·COLLABORATOR)의 리뷰만 세서, 남은 변경 요청이 없는지
    - 승인 수가 기준 이상인지(승격 0명, 핫픽스 2명은 [0022](0022-hotfix-to-production.md))
    - 해결하지 않은 리뷰 대화가 없는지
    - head 커밋의 체크가 모두 통과했는지(같은 이름은 가장 최근 것만 보고, 디스코드 알림 `notify`는 빼고 본다)
    - `check`가 성공했는지(핫픽스는 `naming`, `CodeRabbit`도)
    - 커밋 상태가 있으면 success인지
    - base가 head의 조상인지(compare `ahead`)
  - 실패하면 이유를, 성공하면 옮긴 커밋을 PR 댓글로 남긴다. 봇은 PR을 닫지 않는다. `gh pr close`는 head SHA 조건을 받지 않아, 그사이 head에 올라온 커밋이 있는 PR까지 닫을 수 있다.
- 토큰은 GitHub App으로 만든다.
  - 권한: Contents·Workflows·Pull requests 쓰기, Checks·Commit statuses 읽기. 승격에 `.github/workflows/` 변경이 섞이면 Workflows 쓰기 없이는 ref 갱신이 거부된다.
  - client id는 변수 `FF_APP_CLIENT_ID`에, private key는 시크릿 `FF_APP_PRIVATE_KEY`에 둔다.
- 브랜치 보호:
  - `develop`: [0018](0018-coderabbit-enforcement.md)대로 건다. force push와 삭제도 막는다.
  - `production`, `main`: ruleset "Restrict updates"와 force push·삭제 금지를 건다. 우회 목록에는 봇 App만 넣고, 우회 방식은 항상(Always)으로 한다. 승인과 체크는 봇이 확인한다.
- 배포 태그는 봇이 달지 않는다. 따로 정한다.
- 처음 한 번은 수동으로 옮긴다. `issue_comment` 워크플로는 기본 브랜치(`main`)에 있는 파일로 돈다. 그래서 이 워크플로가 `main`에 들어가기 전에는 `/ff`가 동작하지 않는다. ruleset을 걸기 전에 `develop` → `production` → `main`을 한 번 수동으로 FF한다.

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| 승격도 Rebase and merge 버튼 | SHA가 바뀌어 `production`·`main`의 이력이 `develop`과 달라진다 |
| 승격은 Create a merge commit | 승격마다 머지 커밋이 생긴다. 다음 승격 때 `production`에만 있는 머지 커밋 때문에 FF할 수 없다 |
| 사람이 로컬에서 FF push | 사람의 직접 push를 막는 정책과 맞지 않는다. 확인 절차가 사람 손에 달린다 |
| `GITHUB_TOKEN`으로 ref 갱신 | 이 토큰으로 한 push는 다른 워크플로를 트리거하지 않는다. CI와 디스코드 알림이 돌지 않는다 |
| 개인 토큰(PAT) | 특정 사람 계정에 묶인다. 우회 권한이 사람 계정에 붙고, 그 사람이 빠지면 끊긴다 |

## 트레이드오프

- **얻는 것:**
  - `production`과 `main`이 `develop`과 같은 SHA를 가진다.
  - 사람은 보호 브랜치에 직접 쓸 수 없다.
  - 승격 확인 절차가 코드로 고정된다.
- **잃는 것:**
  - 승격 PR은 머지 버튼 대신 댓글로 반영한다. 버튼은 ruleset 때문에 막힌다.
  - GitHub App 하나와 키를 관리해야 한다. 키가 새면 `production`·`main`을 옮길 수 있다.
  - 워크플로는 `main`에 있는 버전으로 돈다. 워크플로를 고치면 `main`까지 승격해야 반영된다.
  - GitHub가 PR을 merged로 표시하지 않으면 PR이 열린 채 남아 사람이 닫아야 한다. 기본 브랜치가 아닌 base를 ref 갱신으로 옮겼을 때 GitHub가 merged로 표시하는지는 문서에 없다.

## 후속 확인

- 첫 `/ff`에서 PR이 merged로 표시되는지 확인한다.
- 봇은 실행 시점의 PR head를 반영한다. `/ff` 댓글을 단 뒤에 `develop`이 움직이면 댓글 단 사람이 본 커밋보다 뒤 커밋이 올라간다. 문제가 되면 `/ff {sha}`로 커밋을 지정하게 바꾼다.
