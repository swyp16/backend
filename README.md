# moadream backend

| 항목 | 버전 |
|---|---|
| Java | 25 |
| Spring Boot | 4.2.0 |
| PostgreSQL | 18 |
| Gradle | 9.9.2 (wrapper) |

데이터 접근은 쓰기에 JPA, 복잡한 조회에 jOOQ를 쓰고, 스키마는 Flyway로 관리한다. 라이브러리 버전은 Boot BOM을 그대로 따르고 개별로 지정하지 않는다.

## 준비

- **JDK:** 따로 맞출 필요가 없다. Gradle이 JDK 25로 빌드를 돌리고(`gradle/gradle-daemon-jvm.properties`), 없으면 내려받는다. `./gradlew` 실행에는 JDK 17 이상이 필요하다.
- **Docker:** 로컬 DB와 테스트(Testcontainers)에 쓴다.

## 실행

```bash
docker compose up -d                                        # 로컬 PostgreSQL
./gradlew bootRun --args='--spring.profiles.active=local'
```

- API 문서: http://localhost:8080/swagger-ui.html
- 상태 확인: http://localhost:8080/actuator/health

프론트 연동용 mock 서버는 아래로 띄운다. 주소는 같다.

```bash
docker compose --profile mock up --build
```

테스트와 규칙 검사(CI와 같음):

```bash
./gradlew check
```

## 프로필

| 프로필 | 용도 |
|---|---|
| `local` | 로컬 개발. compose의 PostgreSQL에 붙는다 |
| `mock` | mock 컨트롤러가 응답한다. 프론트 연동용 |
| `develop` | 개발 서버 |
| `production` | 운영 서버. Swagger가 꺼진다 |

`develop`, `production`의 DB 접속 정보는 환경변수(`SPRING_DATASOURCE_URL` 등)로 넣는다. 비밀값은 repo에 두지 않는다.

## 문서

- [코딩 규칙](documents/coding-rules.md): 패키지와 API, 응답 형식, mock, 엔티티와 테이블, DB, 테이블 쓰기 담당, CI가 막는 것
- [브랜치·커밋·PR](documents/git-workflow.md): 이름 규칙, 머지, 승격, 핫픽스, 알림
- [ADR](documents/adr/README.md): 트레이드오프가 있었던 결정
