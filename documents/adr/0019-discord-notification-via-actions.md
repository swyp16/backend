# 0019. 디스코드 알림은 GitHub 웹훅이 아니라 Actions로 보낸다

- 상태: 확정 (production 배포 알림은 CD를 만들 때)
- 날짜: 2026-11-27
- 이슈: 14-45

## 맥락

GitHub 웹훅을 Discord에 연결했더니 메시지가 너무 많았다. 웹훅 설정은 이벤트 종류 단위라 "Pull requests"를 켜면 생성·머지뿐 아니라 수정, 라벨, 리뷰 요청, 새 커밋까지 모두 온다. 태그를 받으려고 "Branch or tag creation"을 켜면 피쳐 브랜치 생성도 온다. 또 FF로 반영하면 버튼 머지가 아니라서, 기본 브랜치가 아닌 `develop`·`production`으로의 push에는 PR이 "merged"로 표시되지 않을 수 있다.

받고 싶은 알림은 PR 생성, `develop`·`production`·`main` 반영(머지), 태그 생성, production 배포다.

## 결정

- GitHub 웹훅은 끄고 `.github/workflows/discord.yml`에서 보낸다.
  - PR 생성: `pull_request`의 `opened`만
  - 머지: `develop`·`production`·`main`에 push될 때. 버튼 머지와 FF를 가리지 않는다. 반영된 커밋 목록(이슈 키 포함)과 비교 링크를 보낸다
  - 태그: `v*` 태그 push
- Discord 웹훅 주소는 repo 시크릿 `DISCORD_WEBHOOK_URL`에 둔다. 시크릿이 없으면 알림을 건너뛴다.
- PR 제목과 커밋 메시지는 `jq`로만 다루고, 전체 호출(`@everyone` 등)은 막는다(`allowed_mentions` 비움).
- production 배포 알림은 CD 워크플로를 만들 때 같은 시크릿으로 붙인다.

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| GitHub 웹훅 이벤트 조정 | 이벤트 안의 동작을 고를 수 없어 원하는 알림만 받을 수 없다 |
| 외부 알림 봇·서비스 | 계정과 권한을 따로 관리해야 한다. 워크플로 하나로 충분하다 |

## 트레이드오프

- **얻는 것:** 원하는 순간에만, 원하는 문구로 알린다. FF 반영도 잡힌다.
- **잃는 것:**
  - 알림 한 번마다 Actions 실행이 하나 생긴다(공개 repo라 비용은 없다).
  - 알림 문구를 바꾸려면 워크플로를 고쳐야 한다.
  - 포크에서 올린 PR은 시크릿을 받지 못해 PR 생성 알림이 가지 않는다(팀은 같은 repo 브랜치를 쓴다).
