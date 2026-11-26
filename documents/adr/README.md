# ADR

트레이드오프가 있었던 백엔드 결정을 남긴다.

- 한 문서에 결정 하나를 쓴다.
- 새 ADR은 [TEMPLATE.md](TEMPLATE.md)를 복사해 `{번호 4자리}-{영문 제목}.md`로 만들고, 아래 목록에 추가한다.
- 확정한 ADR은 상태 줄 말고는 고치지 않는다. 결정을 바꾸면 새 ADR을 쓰고, 기존 문서의 상태를 `대체됨({새 번호})`으로 바꾼다.

| 번호 | 제목 | 상태 | 이슈 |
|---|---|---|---|
| [0001](0001-java-25-spring-boot-4-2.md) | Java 25 + Spring Boot 4.2.0 | 확정 | 14-37 |
| [0002](0002-postgresql.md) | DB는 PostgreSQL | 확정 | 14-37 |
| [0003](0003-jpa-write-jooq-read.md) | 쓰기는 JPA, 복잡한 조회는 jOOQ | 확정 | 14-37 |
| [0004](0004-jwt-login.md) | 로그인 유지는 JWT | 확정 (세부는 담당자가 정한다) | 14-37 |
| [0005](0005-product-lookup-sync-timeout.md) | 상품 조회는 동기, 시간 초과 시 직접 입력 | 확정 | 14-37 |
| [0006](0006-ci-enforced-code-rules.md) | 기계로 판정할 수 있는 규칙은 CI가 막는다 | 확정 | 14-37 |
| [0007](0007-coderabbit-pr-review.md) | PR 1차 리뷰는 CodeRabbit | 확정 | 14-37 |
| [0008](0008-mock-controller-profile.md) | mock은 같은 인터페이스를 구현하는 컨트롤러를 프로필로 바꾼다 | 확정 | 14-37 |
| [0009](0009-api-response-format.md) | 성공은 DTO 그대로, 오류는 ProblemDetail + code | 확정 | 14-37 |
| [0010](0010-nanoid-12-entity-id.md) | 엔티티 ID는 NanoID 12자 | 확정 | 14-38 |
| [0011](0011-time-handling.md) | 시각은 TIMESTAMPTZ·Instant·UTC(마이크로초), 현재 시각은 Clock 빈으로 | 확정 | 14-38 |
| [0012](0012-money-as-long.md) | 금액은 원 단위 정수(long) | 확정 | 14-38 |
| [0013](0013-enum-as-string.md) | enum은 문자열로 저장 | 확정 | 14-38 |
| [0014](0014-common-columns.md) | 모든 테이블에 created_at·updated_at, BaseEntity가 채운다 | 확정 | 14-38 |
| [0015](0015-deletion-policy.md) | 삭제는 기본 물리 삭제, 참여·입금 이력은 지우지 않는다 | 확정 (회원 탈퇴 처리는 담당자가 정한다) | 14-38 |
| [0016](0016-branch-commit-pr-naming.md) | 브랜치·커밋·PR 이름에 이슈 키를 넣고, 커밋은 CI가 검사한다 | 확정 | 14-45 |
| [0017](0017-ci-gradle-cache-and-wrapper-validation.md) | CI의 Gradle 캐시는 setup-java로, wrapper 검증은 gradle/actions v5로 | 확정 | 14-37 |
| [0018](0018-coderabbit-enforcement.md) | CodeRabbit은 리뷰 완료와 지적 해결까지 강제한다 | 확정 (ruleset 적용은 나중에) | 14-45 |
| [0019](0019-discord-notification-via-actions.md) | 디스코드 알림은 GitHub 웹훅이 아니라 Actions로 보낸다 | 확정 (production 배포 알림은 CD를 만들 때) | 14-45 |
| [0020](0020-promotion-fast-forward-bot.md) | 승격은 `/ff` 댓글 봇이 FF로 반영한다 | 확정 (GitHub App 등록, ruleset, 머지 방식 설정은 나중에) | 14-45 |
| [0021](0021-promotion-pr-title-template-no-coderabbit.md) | 승격 PR은 제목과 템플릿을 고정하고 CodeRabbit 리뷰를 건너뛴다 | 확정 | 14-49 |
| [0022](0022-hotfix-to-production.md) | 핫픽스는 `hotfix/{이슈 키}` → `production`으로 넣고, 봇이 `develop`에 되합친다 | 확정 (GitHub App 등록과 ruleset 적용은 나중에) | 14-49 |
