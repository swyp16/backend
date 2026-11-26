# 0009. 성공은 DTO 그대로, 오류는 ProblemDetail + code

- 상태: 확정
- 날짜: 2026-11-26 (같은 날 셋업 중 리뷰를 반영해, 처리하지 못한 예외도 ProblemDetail로 내리도록 처리 클래스를 추가했다)
- 이슈: 14-37

## 맥락

11/25 회의에서 API 경로와 HTTP 메서드는 정했지만, 응답 본문 형식은 정하지 않았다. 백엔드 4명이 각자 형식을 만들기 전에 하나로 맞춰야 한다.

## 결정

- 성공 응답은 DTO를 그대로 반환한다. 공통 래퍼는 없다.
- 오류 응답은 Spring 내장 ProblemDetail(RFC 9457, `application/problem+json`)을 쓰고, `code` 속성을 붙인다.
- 비즈니스 오류는 `ErrorResponseException`을 상속한 예외로 던진다.
- 처리는 `ResponseEntityExceptionHandler`를 상속한 클래스 하나(`ApiExceptionHandler`)가 맡는다. 처리하지 못한 예외도 500 ProblemDetail로 내리고, `code`가 없는 오류에는 HTTP 상태 이름(`NOT_FOUND`, `INTERNAL_SERVER_ERROR` 등)을 넣는다.

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| 공통 래퍼 `{success, data, error}` | 프론트가 한 형식만 파싱하면 되지만, HTTP 상태와 의미가 겹치고 모든 응답을 감싸는 코드가 생긴다 |
| 자체 `ErrorResponse` + `ErrorCode` enum + `GlobalExceptionHandler` | 형식을 마음대로 정할 수 있지만 직접 만들고 유지해야 한다. 예외마다 핸들러가 늘어 비대해지기 쉽다 |

## 트레이드오프

- **얻는 것:**
  - 표준 형식이고, 처리 코드는 클래스 하나다(Spring 내장 처리를 상속).
  - 검증 실패, 404, 405, 처리하지 못한 예외까지 모든 오류가 같은 형식에 `code`를 가진다. 프론트는 오류 분기를 `code` 하나로 한다.
- **잃는 것:**
  - 필드 이름(`type`, `title`, `status`, `detail`, `instance`)이 표준에 묶인다.
  - `code` 값을 한곳에서 관리하지 않아, 도메인마다 겹치거나 어긋날 수 있다. 그렇게 되면 enum으로 모은다.
  - 기본 검증 오류 응답에는 필드별 오류 목록이 없다. 화면에서 필요하면 핸들러를 추가한다.

## 후속 확인

- 프론트와 오류 형식, `code` 목록 공유 방법을 맞춘다.
