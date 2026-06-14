# Oracles — 판정 기준

> 오라클 = "무엇이 통과인가"를 정의하는 판정 기준(외부 관측 관점). 테스트는 그 기준의 *실행 형태*다.
> PR 본문 `oracle_ref:` 가 가리키는 대상.

## 이 repo 의 1차 오라클

대부분의 판정 기준은 이미 코드에 살아 있다. 별도 문서를 만들기 전에 아래를 oracle 로 참조한다:

- **API 계약** — 각 도메인의 `*ControllerTest` (app: MockMvc + REST Docs) → 생성된 OpenAPI3 스펙
- **도메인 불변식** — `core.domain` 엔티티의 검증 로직 + `*DomainException` / `DomainErrorCode`
- **에러 응답 규약** — `ProblemDetail`(RFC7807) `type/title/status/detail` (`ErrorCode` enum 이 상태코드 정의)

## 별도 오라클 문서를 쓰는 경우

코드 테스트만으로 "통과 기준"이 안 드러나는 **횡단·운영 판정**일 때 `docs/oracles/<주제>.md` 로 명문화한다. 예: 추천 결과의 품질 기준, 배치 정합성 기준, 응답 SLA.

> 현재는 코드 오라클로 충분 — 문서 오라클은 필요 시 누적.
