# 0011. 시각은 TIMESTAMPTZ·Instant·UTC(마이크로초), 현재 시각은 Clock 빈으로

- 상태: 확정
- 날짜: 2026-11-26
- 이슈: 14-38

## 맥락

펀딩의 마감, 기간 연장, 목표 달성 판정이 모두 시각에 달려 있다.

## 결정

- DB는 `TIMESTAMPTZ`, Java는 `Instant`, JSON은 ISO-8601 UTC(`"2026-12-20T03:00:00Z"`)다. `LocalDateTime`은 쓰지 않는다.
- 정밀도는 마이크로초다. PostgreSQL `TIMESTAMPTZ`가 마이크로초까지만 저장해서, `Clock` 빈도 `Clock.tick(..., 1µs)`로 마이크로초 단위로 끊는다. 리눅스의 JDK 시계는 나노초 값을 내므로 그대로 두면 메모리의 값과 DB 값이 달라진다.
- 현재 시각은 `Clock` 빈(`CommonConfig`)을 주입받아 얻는다. `Instant.now()`, `LocalDate.now()`, `System.currentTimeMillis()`, `Clock.systemUTC()` 등을 직접 부르지 않는다.
- 날짜 계산은 `CommonConfig.KST`(Asia/Seoul)로 한다. **마감일이 D이면 마감 시각은 D+1 00:00 KST이고, 그 시각부터 마감이다.**
- 강제 수단:
  - ArchUnit: `LocalDateTime` 사용, 현재 시각 직접 호출
  - 스키마 검사: 시간대 없는 `TIMESTAMP` 컬럼(Hibernate 기동 검증은 이 둘을 구분하지 못해 따로 둠)

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| `LocalDateTime` + KST 저장 | 서버·DB 시간대 설정에 따라 값의 의미가 바뀐다 |
| 현재 시각 직접 호출 | 마감 판정 테스트에서 시각을 고정할 방법이 없다 |
| 마감 시각 = D 23:59:59 KST | 초 단위 경계가 애매하고, "이 시각부터 마감"이라는 비교가 한 번으로 끝나지 않는다 |

## 트레이드오프

- **얻는 것:** 저장값의 의미가 환경에 따라 바뀌지 않는다. 시각 의존 로직을 테스트에서 고정할 수 있다.
- **잃는 것:**
  - 화면 표시와 날짜 계산마다 KST 변환이 필요하다.
  - `Clock`을 모든 시각 의존 클래스에 주입해야 한다.
  - jOOQ 생성 코드는 `TIMESTAMPTZ`를 `OffsetDateTime`으로 만든다. jOOQ로 읽을 때는 `Instant`로 바꿔 쓴다.
