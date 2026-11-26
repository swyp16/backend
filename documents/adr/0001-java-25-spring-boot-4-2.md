# 0001. Java 25 + Spring Boot 4.2.0

- 상태: 확정
- 날짜: 2026-11-26 (11/25 개발자 회의 결정의 버전 확정)
- 이슈: 14-37

## 맥락

11/25 개발자 회의에서 Java 25, Spring Boot 4.2.0으로 정했다. 4.2.0은 2026-11-19에 Maven Central에 배포된 첫 버전(.0)이다.

## 결정

- Java 25, Gradle 9.9.2(wrapper), Spring Boot 4.2.0
- 라이브러리 버전은 **Boot BOM이 관리하는 값을 그대로** 쓰고 개별로 덮어쓰지 않는다. Boot를 올릴 때는 jOOQ 코드 생성 플러그인을 Boot BOM의 jOOQ 버전에 맞춰 함께 올린다.

| 구분 | 버전 |
|---|---|
| Boot가 관리 | Spring Framework 7.1.0, Spring Security 7.2.0, Hibernate 7.4.15, jOOQ 3.21.9, Flyway 12.11.0, PostgreSQL JDBC 42.7.14, HikariCP 7.1.0, Jackson 3.1.8, JUnit 6.1.3, Testcontainers 2.0.5, Thymeleaf 3.1.7 |
| 직접 지정 | springdoc-openapi 3.2.1, ArchUnit 1.5.1, Checkstyle 14.4.0, jOOQ 코드 생성 플러그인 3.21.9(Boot BOM의 jOOQ와 같게), foojay toolchain resolver 1.0.0 |

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| Boot 4.1.3 | 패치가 쌓여 안정적이지만 무료 지원이 2027-07-31에 끝난다 |
| Boot 4.0.x | 무료 지원이 2026-12-31에 끝나 출시(12/20) 직후 업그레이드가 필요하다 |

## 트레이드오프

- **얻는 것:** 무료 지원 기간이 가장 길다(2027-12-31). springdoc 3.2.1이 Boot 4.2.0 기반이라 핵심 의존성은 맞는다.
- **잃는 것:** .0 버전이라 버그나 서드파티 미대응을 먼저 겪을 수 있다. Sentry starter는 4.2 호환이 확인되지 않았다. start.spring.io는 Sentry를 Boot 4.0.x~4.1 미만 범위에서만 제공한다.
- 문제가 생겨 4.1.3으로 내리는 비용은 빌드 파일의 Boot 버전 한 줄과 두 버전 사이의 API 차이다.

## 후속 확인

- Sentry starter의 Boot 4.2 호환
- PostgreSQL 메이저 버전은 [0002](0002-postgresql.md)
