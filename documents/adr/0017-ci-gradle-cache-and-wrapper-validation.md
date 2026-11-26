# 0017. CI의 Gradle 캐시는 setup-java로, wrapper 검증은 gradle/actions v5로

- 상태: 확정
- 날짜: 2026-11-26
- 이슈: 14-37

## 맥락

이 repo는 공개 repo라 외부 PR도 CI를 돌린다. PR이 `gradle-wrapper.jar`를 바꿔 CI에서 임의 코드를 실행할 수 있다. Gradle 캐시와 wrapper 검증은 보통 `gradle/actions/setup-gradle`로 하지만, v6부터 캐시 기능이 별도 약관(Gradle Terms of Use)을 따르는 상용 컴포넌트로 분리됐다. v6을 쓰면 그 약관에 동의한 것이 된다.

## 결정

- `gradle/actions/wrapper-validation@v5`로 `gradle-wrapper.jar`를 먼저 검증한다.
- Gradle 캐시는 `actions/setup-java@v6`의 `cache: gradle`로 한다.
- `setup-gradle@v6`은 쓰지 않는다.

## 검토한 대안

| 대안 | 탈락 이유 |
|---|---|
| `setup-gradle@v6` | 캐시 컴포넌트의 별도 약관에 동의해야 한다. 팀 결정 없이 동의하지 않는다 |
| wrapper 검증 없음 | 공개 repo PR로 바뀐 wrapper가 CI에서 실행될 수 있다 |

## 트레이드오프

- **얻는 것:** 약관 동의 없이 캐시와 wrapper 검증을 모두 쓴다.
- **잃는 것:**
  - `setup-java` 캐시는 `setup-gradle`보다 단순하다(구성 캐시, 빌드 결과 요약 없음).
  - wrapper 검증은 구버전(v5)이라, 보안 수정이 v6에만 나오면 다시 본다.
