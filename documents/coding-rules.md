# 코딩 규칙

## 패키지와 API

- 도메인별로 패키지를 나눈다: `kr.moadream.<도메인>` (예: `member`, `wish`, `funding`, `message`, `support`)
- 경로는 `/v1/<도메인>/<리소스>`다. 메서드는 GET, POST, DELETE를 쓰고, 필요할 때만 PUT, PATCH를 쓴다.
- 요청·응답 DTO는 record이고 이름은 `*Request`, `*Response`다. 엔티티를 받지 않는다.

## 응답 형식

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

## mock

경로와 Swagger 설명은 `*Api` 인터페이스에 두고, 실제 컨트롤러와 mock 컨트롤러가 같은 인터페이스를 구현한다.

```java
@RequestMapping("/v1/fundings")
public interface FundingApi {

    @GetMapping("/{fundingId}")
    FundingResponse get(@PathVariable String fundingId);
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
    public FundingResponse get(String fundingId) {
        return new FundingResponse(fundingId, "에어팟 프로", 300000, 120000);
    }
}
```

API를 만들 때 mock 컨트롤러도 함께 만든다.

## 엔티티와 테이블

- **PK:** 모든 엔티티는 `BaseEntity`를 상속한다. PK는 NanoID 12자(영문 대소문자와 숫자)이고, 저장할 때 자체 생성기(`NanoId`)가 채운다. 생성자나 필드 초기화로 ID를 넣지 않는다. 외부(URL, API)에도 이 ID를 그대로 쓴다. `@Id`, `@GeneratedValue`를 직접 쓰지 않는다.
- **공통 컬럼:** 모든 테이블에 아래 세 컬럼을 둔다. `created_at`, `updated_at`은 JPA로 저장·수정할 때 `Clock` 기준으로 자동으로 채워진다. JPA를 거치지 않는 쓰기(jOOQ, 시드 SQL)는 `id`와 두 시각을 직접 넣는다. DB 기본값은 없다.
  ```sql
  id         VARCHAR(12) PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  ```
- **시각:** DB는 `TIMESTAMPTZ`, Java는 `Instant`(`LocalDateTime` 금지), JSON은 ISO-8601 UTC(`"2026-12-20T03:00:00Z"`)다. 정밀도는 마이크로초다(PostgreSQL 기준, `Clock` 빈도 마이크로초 단위).
- **현재 시각:** `Instant.now()` 등을 직접 부르지 않고 `Clock` 빈을 주입받아 `Instant.now(clock)`으로 얻는다. `LocalDate.now(clock)`처럼 `now`에는 항상 `clock`을 넘긴다. 테스트에서는 `@TestBean`으로 `Clock` 빈을 바꿔 시각을 고정한다.
- **날짜 계산:** 마감일처럼 날짜를 다룰 때는 `CommonConfig.KST`(Asia/Seoul)로 계산한다. 마감일이 12/20이면 마감 시각은 12/21 00:00 KST이고, 그 시각부터 마감이다.
- **금액:** 원 단위 정수다. Java `long`, DB `BIGINT`, JSON 숫자. 금액 필드 이름은 `...Amount` 또는 `...Price`로 끝낸다.
- **enum:** `@Enumerated(EnumType.STRING)`으로 저장하고 컬럼은 `VARCHAR`다. 순서값이나 PostgreSQL enum 타입은 쓰지 않는다.
- **삭제:** 기본은 물리 삭제다. 참여 내역과 입금 상태 이력은 지우지 않는다. 회원 탈퇴 시 개인정보(이름, 계좌번호) 처리는 회원 기능 담당자가 정한다.

## DB

- **스키마 변경:** Flyway로 한다(`src/main/resources/db/migration/V{번호}__{설명}.sql`, 하위 폴더도 된다). 앱은 스키마를 만들지 않고 검증만 한다(`ddl-auto: validate`).
- **쓰기와 조회:** 쓰기는 JPA로 한다. 메서드 이름 쿼리로 안 되는 조회는 jOOQ로 쓰고, `@Query`는 쓰지 않는다.
- **jOOQ 코드:** 빌드 때 마이그레이션 SQL에서 자동으로 생성된다(`kr.moadream.jooq`). DB 접속은 필요 없다. jOOQ가 해석하지 못하는 구문(PL/pgSQL 함수 등)은 `-- [jooq ignore start]`와 `-- [jooq ignore stop]` 사이에 둔다. 테이블·컬럼을 만들거나 바꾸는 구문은 넣지 않는다. 넣으면 생성 코드에서 빠진다.
- **SQL 문자열 금지:** 문자열을 받는 jOOQ API(`where("...")` 등)는 쓰지 않는다. 생성된 테이블과 컬럼으로 쿼리를 만든다.
- **flush:** JPA로 쓴 직후 같은 트랜잭션에서 jOOQ로 읽으려면 먼저 flush한다.
- **시간 타입:** 시각 규칙은 위의 엔티티와 테이블 절을 따른다. `timestamptz` 컬럼은 jOOQ에서 `OffsetDateTime`으로 생성된다.
- **트랜잭션:** 서비스가 연다.

## 테이블 쓰기 담당

테이블마다 쓰기(생성·수정·삭제)는 담당 도메인의 서비스 한곳에서만 한다. 다른 도메인은 그 서비스를 부른다. 기능 담당자가 테이블을 만들 때 아래에 적는다.

| 테이블 | 쓰기 담당 도메인(패키지) | 담당자 |
|---|---|---|

## CI가 막는 것

| 도구 | 규칙 |
|---|---|
| Checkstyle | 들여쓰기 4칸, 미사용·와일드카드 import |
| ArchUnit | DTO의 엔티티 의존, 컨트롤러의 Repository·엔티티 의존, 컨트롤러의 `@Transactional`, 도메인 사이 패키지 순환, `@Query`·`@NativeQuery`, 문자열을 받는 jOOQ API, `LocalDateTime`, 현재 시각 직접 호출, `BaseEntity` 미상속·직접 정의한 PK, 이름이 `Amount`·`Price`로 끝나는데 `long`·`Long`이 아닌 필드, 문자열이 아닌 enum 저장 |
| 스키마 검사 | 시간대 없는 `TIMESTAMP` 컬럼, `id VARCHAR(12)` PK·`created_at`·`updated_at`이 없는 테이블 (마이그레이션을 적용한 실제 DB로 검사) |
