# SPEC: 좋아요(Reaction LIKE) 추가/취소

## 1. 메타

| 항목 | 값 |
|---|---|
| ID | `reaction-like` |
| 상태 | `active` |
| 연관 LLD | 없음 (단순 CRUD — 별도 설계 불필요) |
| 오라클 | `app/.../reaction/presentation/ReactionControllerTest`, `app/.../reaction/application/ReactionServiceTest` |
| 엔드포인트 | `POST /api/v1/hearits/{hearitId}/likes`, `DELETE /api/v1/hearits/{hearitId}/likes` |

## 2. 배경 / 목적

회원이 특정 Hearit(콘텐츠)에 "좋아요"를 남기고 취소할 수 있다. 좋아요는 추천·개인화의 신호로 쓰인다.
한 회원은 한 콘텐츠에 좋아요를 **중복으로 가질 수 없다**(DB unique: `user_uuid, hearit_id, type`).

## 3. Acceptance Criteria (Gherkin)

```gherkin
Feature: 좋아요 추가/취소

  Background:
    Given 콘텐츠 Hearit 이 존재한다

  # --- 추가: POST /api/v1/hearits/{hearitId}/likes ---

  Scenario: 회원이 좋아요를 추가한다
    Given 로그인한 회원이고 해당 콘텐츠에 좋아요를 누른 적이 없다
    When 좋아요 추가를 요청하면
    Then 201 Created 를 응답한다
    And 해당 회원-콘텐츠의 LIKE 가 1건 저장된다

  Scenario: 이미 좋아요한 콘텐츠에 다시 추가한다 (멱등)
    Given 로그인한 회원이고 이미 해당 콘텐츠에 좋아요를 눌렀다
    When 좋아요 추가를 다시 요청하면
    Then 201 Created 를 응답한다
    And 저장된 LIKE 는 여전히 1건이다 (중복 생성되지 않는다)

  Scenario: 비회원이 좋아요를 추가한다
    Given 인증되지 않은(게스트) 사용자다
    When 좋아요 추가를 요청하면
    Then 403 Forbidden 을 응답한다

  Scenario: 존재하지 않는 콘텐츠에 좋아요를 추가한다
    Given 로그인한 회원이다
    When 존재하지 않는 hearitId 로 좋아요 추가를 요청하면
    Then 404 Not Found 를 응답한다

  # --- 취소: DELETE /api/v1/hearits/{hearitId}/likes ---

  Scenario: 회원이 좋아요를 취소한다
    Given 로그인한 회원이고 해당 콘텐츠에 좋아요를 누른 상태다
    When 좋아요 취소를 요청하면
    Then 204 No Content 를 응답한다
    And 해당 회원-콘텐츠의 LIKE 가 삭제된다

  Scenario: 좋아요하지 않은 콘텐츠를 취소한다 (멱등)
    Given 로그인한 회원이고 해당 콘텐츠에 좋아요를 누른 적이 없다
    When 좋아요 취소를 요청하면
    Then 204 No Content 를 응답한다 (에러 없이 무시된다)

  Scenario: 비회원이 좋아요를 취소한다
    Given 인증되지 않은(게스트) 사용자다
    When 좋아요 취소를 요청하면
    Then 403 Forbidden 을 응답한다

  Scenario: 존재하지 않는 콘텐츠의 좋아요를 취소한다
    Given 로그인한 회원이다
    When 존재하지 않는 hearitId 로 좋아요 취소를 요청하면
    Then 404 Not Found 를 응답한다
```

## 4. 비-목적 (Out of Scope)

- LIKE 외 `ReactionType`(향후 확장) — 이 SPEC은 LIKE 만 다룬다.
- 좋아요 수(`likeCount`)·좋아요 여부(`isLiked`) 노출 — `hearit` 상세 응답(`HearitDetailResponse`) 소관.
- "내가 좋아요한 콘텐츠 목록" 조회 — 별도 기능.

## 5. 검증 경로

- **API 계약** — `ReactionControllerTest` (MockMvc + REST Docs): 201/204/403/404 대표 케이스. 통과 시 OpenAPI3/Swagger 문서 생성.
- **서비스/영속성** — `ReactionServiceTest` (`@DataJpaTest` + h2): 멱등 추가, 멱등 취소, 게스트 거부.
- **멱등성 근거** — `reaction` 테이블 unique constraint `(user_uuid, hearit_id, type)` (`V2026013100001__create_reaction_table.sql`). 동시성 중복은 `DataIntegrityViolationException` 으로 흡수.
