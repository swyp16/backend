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
| `develop` | 개발 서버(`dev-api.moadream.kr`) |
| `production` | 운영 서버(`api.moadream.kr`). Swagger가 꺼진다 |

`develop`, `production`의 DB 접속 정보는 환경변수(`SPRING_DATASOURCE_URL` 등)로 넣는다. 비밀값은 repo에 두지 않는다.

## 규칙

### 패키지와 API

- 도메인별로 패키지를 나눈다: `kr.moadream.<도메인>` (예: `member`, `wish`, `funding`, `message`, `support`)
- 경로는 `/v1/<도메인>/<리소스>`다. 메서드는 GET, POST, DELETE를 쓰고, 필요할 때만 PUT, PATCH를 쓴다.
- 요청·응답 DTO는 record이고 이름은 `*Request`, `*Response`다. 엔티티를 받지 않는다.

### 응답 형식

- **성공:** DTO를 그대로 반환한다. 공통 래퍼는 없다.
- **오류:** ProblemDetail(RFC 9457, `application/problem+json`)에 `code`를 붙인다. 비즈니스 오류는 `ErrorResponseException`을 상속해 던진다. 변환은 `kr.moadream.common.ApiExceptionHandler`가 한다.

```java
public class FundingClosedException extends ErrorResponseException {

    public FundingClosedException() {
        super(HttpStatus.CONFLICT);
        getBody().setDetail("이미 종료된 펀딩입니다.");
        getBody().setProperty("code", "FUNDING_CLOSED");
    }
}
```

```json
{"status": 409, "title": "Conflict", "detail": "이미 종료된 펀딩입니다.", "code": "FUNDING_CLOSED"}
```

검증 실패, 없는 경로, 처리하지 못한 예외 같은 그 밖의 오류도 같은 형식이다. 이때 `code`는 HTTP 상태 이름(`BAD_REQUEST`, `NOT_FOUND`, `INTERNAL_SERVER_ERROR` 등)이다.

### mock

경로와 Swagger 설명은 `*Api` 인터페이스에 두고, 실제 컨트롤러와 mock 컨트롤러가 같은 인터페이스를 구현한다.

```java
@RequestMapping("/v1/fundings")
public interface FundingApi {

    @GetMapping("/{fundingId}")
    FundingResponse get(@PathVariable long fundingId);
}

@RestController
@Profile("!mock")
class FundingController implements FundingApi {
    // 서비스를 부른다
}

@RestController
@Profile("mock")
class FundingMockController implements FundingApi {

    @Override
    public FundingResponse get(long fundingId) {
        return new FundingResponse(fundingId, "에어팟 프로", 300000, 120000);
    }
}
```

API를 만들 때 mock 컨트롤러도 함께 만든다.

### DB

- **스키마 변경:** Flyway로 한다(`src/main/resources/db/migration/V{번호}__{설명}.sql`, 하위 폴더도 된다). 앱은 스키마를 만들지 않고 검증만 한다(`ddl-auto: validate`).
- **쓰기와 조회:** 쓰기는 JPA로 한다. 메서드 이름 쿼리로 안 되는 조회는 jOOQ로 쓰고, `@Query`는 쓰지 않는다.
- **jOOQ 코드:** 빌드 때 마이그레이션 SQL에서 자동으로 생성된다(`kr.moadream.jooq`). DB 접속은 필요 없다. jOOQ가 해석하지 못하는 구문(PL/pgSQL 함수 등)은 `-- [jooq ignore start]`와 `-- [jooq ignore stop]` 사이에 둔다.
- **SQL 문자열 금지:** 문자열을 받는 jOOQ API(`where("...")` 등)는 쓰지 않는다. 생성된 테이블과 컬럼으로 쿼리를 만든다.
- **flush:** JPA로 쓴 직후 같은 트랜잭션에서 jOOQ로 읽으려면 먼저 flush한다.
- **시간 타입:** 시간은 `Instant`로 다룬다(`LocalDateTime` 금지). `timestamptz` 컬럼은 jOOQ에서 `OffsetDateTime`으로 생성된다.
- **트랜잭션:** 서비스가 연다.

### CI가 막는 것

| 도구 | 규칙 |
|---|---|
| Checkstyle | 들여쓰기 4칸, 미사용·와일드카드 import |
| ArchUnit | DTO의 엔티티 의존, 컨트롤러의 Repository·엔티티 의존, 컨트롤러의 `@Transactional`, 패키지 순환, `@Query`·`@NativeQuery`, 문자열을 받는 jOOQ API, `LocalDateTime` |

### 브랜치·커밋·PR

| 항목 | 규칙 | 예 |
|---|---|---|
| 브랜치 | `feature/{이슈 키}` | `feature/14-38` |
| 커밋 | `{타입}({이슈 키}): 요약`. 타입은 `feat`, `fix`, `refactor`, `test`, `docs`, `chore` | `feat(14-38): 엔티티 공통 규칙 추가` |
| PR 제목 | `[{이슈 키}] 요약` | `[14-38] 엔티티 공통 규칙` |

- 피쳐 브랜치에서 `develop`으로 PR을 올린다. 사람 두 명이 approve하면 FF 머지한다.
- PR 본문은 템플릿(`.github/pull_request_template.md`)을 채운다.
- 커밋마다 이슈 키를 넣는다. CI가 `develop`으로 오는 PR의 브랜치 이름과 모든 커밋 메시지를 검사한다.
- 배포는 `develop` → `production` PR, 그다음 `production` → `main` PR로 한다. 배포할 때 `production`에 태그 `v{YYYYMMDD}-{짧은 해시}`를 단다.
- CodeRabbit이 1차 리뷰를 한다. 자동으로 돌지 않으면 PR에 `@coderabbitai review` 댓글을 단다.
