# 0008. mock은 같은 인터페이스를 구현하는 컨트롤러를 프로필로 바꾼다

- 상태: 확정
- 날짜: 2026-11-26
- 이슈: 14-37

## 맥락

11/25 개발자 회의에서 "mock은 로컬에서 spring profile로 실행한다"고 정했다. 프론트가 백엔드 구현을 기다리지 않고 API 응답 형식에 맞춰 먼저 작업하려면 mock이 빨리 나와야 한다.

## 결정

- 경로와 Swagger 설명은 `*Api` 인터페이스에 둔다.
- 실제 컨트롤러(`@Profile("!mock")`)와 mock 컨트롤러(`@Profile("mock")`)가 같은 인터페이스를 구현한다.
- mock 서버도 PostgreSQL과 함께 뜬다(`docker compose --profile mock up --build`).

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| 정적 JSON mock(WireMock, json-server 등) | 백엔드 코드와 따로 관리되어, 계약이 어긋나도 알 수 없다 |
| mock 프로필은 DB 없이 기동 | 서비스와 Repository 빈마다 프로필을 달거나 자동 설정을 빼야 한다. 하나라도 빠지면 기동에 실패한다 |
| Swagger 예시 응답만 제공 | 프론트가 실제로 호출할 수 없다 |

## 트레이드오프

- **얻는 것:**
  - 실제 컨트롤러와 mock의 시그니처가 다르면 컴파일이 깨진다.
  - mock 서버에서도 Swagger 문서를 그대로 볼 수 있다.
- **잃는 것:**
  - API마다 인터페이스와 mock 클래스가 하나씩 늘어난다.
  - mock 서버를 띄우려면 Docker와 PostgreSQL이 필요하다. compose 한 줄로 해결된다.
  - 실제 구현이 끝난 뒤 mock 컨트롤러를 계속 둘지 지울지 정해야 한다.

## 후속 확인

- 프론트가 backend repo 없이 mock을 띄울 수 있게 이미지를 배포할지(GHCR 등)
- 실제 구현이 끝난 mock을 언제 정리할지
