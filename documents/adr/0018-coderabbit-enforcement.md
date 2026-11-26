# 0018. CodeRabbit은 리뷰 완료와 지적 해결까지 강제한다

- 상태: 확정 (ruleset 적용은 나중에)
- 날짜: 2026-11-27
- 이슈: 14-45

## 맥락

사람 approve 수처럼 CodeRabbit 리뷰도 머지 조건으로 걸고 싶다. CodeRabbit은 PR에 `CodeRabbit` 체크를 올리는데, 이 체크는 리뷰가 끝나면 성공이 된다. 리뷰를 건너뛰었거나 시간당 한도(오픈소스 플랜)에 걸린 경우도 성공이다. 그래서 체크만 필수로 걸면 "리뷰가 돌았다"까지만 강제되고 지적을 처리했는지는 강제되지 않는다.

## 결정

- `.coderabbit.yaml`의 `reviews.fail_commit_status: true`: 리뷰가 오류로 멈추면 체크가 실패한다.
- `develop` ruleset:
  - PR 필수, 사람 approve 2명
  - 필수 체크: `check`, `naming`, `CodeRabbit`
  - 대화 해결 필수: CodeRabbit 지적을 포함한 리뷰 스레드를 모두 해결해야 머지된다
  - approve는 협업자 누구나 할 수 있다. CODEOWNERS는 두지 않는다.
  - approve 뒤에 push해도 approve를 유지한다. 오래된 approve 무효화(Dismiss stale approvals)는 켜지 않는다.
- `production`, `main` 보호는 [0020](0020-promotion-fast-forward-bot.md)에서 정한다. 승격 PR은 CodeRabbit이 리뷰하지 않는다([0021](0021-promotion-pr-title-template-no-coderabbit.md)).

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| 리뷰 완료만 강제 | 지적을 무시하고 머지할 수 있다 |
| CODEOWNERS로 백엔드만 approve | 리뷰어를 구하기 어려워진다 |
| approve 뒤 push하면 approve 무효 | 사소한 수정마다 두 명에게 다시 받아야 한다 |
| CodeRabbit 자동 승인(`auto_approvals`)까지 사용 | CodeRabbit이 변경 요청과 승인을 직접 건다. 봇의 승인·변경 요청이 GitHub의 필요 approve 수에 어떻게 세어지는지 확인하지 못했다 |

## 트레이드오프

- **얻는 것:** CodeRabbit 지적을 고치거나 명시적으로 닫지 않으면 머지되지 않는다.
- **잃는 것:**
  - 시간당 한도에 걸리면 리뷰 없이 체크가 성공한다. 한도는 리뷰 정보에 표시되니, 그때는 `@coderabbitai review`로 다시 부른다.
  - 사소한 지적도 스레드를 닫는 손이 한 번 더 간다.
  - 백엔드를 모르는 사람의 approve로도 머지된다.
  - approve 뒤의 변경은 사람 리뷰 없이 들어갈 수 있다. 그 변경은 CI와 CodeRabbit만 본다.
