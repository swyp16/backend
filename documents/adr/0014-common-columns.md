# 0014. 모든 테이블에 created_at·updated_at, BaseEntity가 채운다

- 상태: 확정 (적용은 14-38)
- 날짜: 2026-11-26
- 이슈: 14-38

## 맥락

생성·수정 시각을 테이블마다 다르게 만들면 정렬과 운영 조회가 제각각이 된다.

## 결정

- 모든 테이블에 `created_at TIMESTAMPTZ NOT NULL`, `updated_at TIMESTAMPTZ NOT NULL`을 둔다.
- `BaseEntity`가 Spring Data JPA auditing으로 채운다. 시각은 `Clock` 빈에서 얻는다([0011](0011-time-handling.md)).
- 강제 수단:
  - ArchUnit: 모든 엔티티는 `BaseEntity`를 상속한다
  - 스키마 검사: 두 컬럼이 없는 테이블을 막는다

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| Hibernate `@CreationTimestamp`·`@UpdateTimestamp` | JVM 시계를 직접 써서 `Clock`으로 고정할 수 없다 |
| 테이블마다 필요한 곳만 | 기준이 흩어진다 |

## 트레이드오프

- **얻는 것:** 모든 데이터에 생성·수정 시각이 있고, 테스트에서 고정할 수 있다.
- **잃는 것:**
  - 수정되지 않는 이력 테이블에도 `updated_at`이 붙는다.
  - JPA를 거치지 않는 쓰기(jOOQ, SQL)는 두 컬럼을 직접 채워야 한다.
