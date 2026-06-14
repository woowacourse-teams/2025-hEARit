# AGENTS.md — hEARit Backend

> AI 에이전트가 이 백엔드에서 작업하기 전에 읽는 정본(canonical) 문서.
> 컨벤션·도메인 용어·테스트 실행법·주요 경로·검증(오라클) 앵커를 여기서 찾는다.
> 안드로이드는 `../android/AGENTS.md`, 팀·횡단 문서는 GitHub Wiki를 참조.

## 1. 서비스 한 줄 정의

개발자용 IT 팟캐스트 학습 플랫폼 **hEARit** 의 백엔드 API.
모바일 앱(Android/iOS)이 소비하는 REST API와, 콘텐츠 운영용 Admin을 제공한다.

## 2. 기술 스택

- **Language / Build** — Java 21 (toolchain), Gradle multi-module, `group=com.onair`, `version=1.3.7`
- **Framework** — Spring Boot 3.5.3 (Web MVC, Data JPA, Security, Validation)
- **DB / Cache** — MySQL 8.0, Redis (Redisson client)
- **마이그레이션** — Flyway (`core` 모듈의 `db/migration/*.sql`)
- **회복탄력성 / 동시성** — Resilience4j(Circuit Breaker), ShedLock(분산 스케줄러 락)
- **인증** — JWT(jjwt) + Kakao OAuth
- **문서화** — Spring REST Docs + `restdocs-api-spec` → OpenAPI3 → Swagger UI (테스트가 문서를 생성)
- **로깅** — Log4j2 (core `log/` 패키지에 마스킹·DB trace 커스텀)
- **테스트** — JUnit 5, AssertJ, RestAssured, MockMvc, Mockito, Testcontainers(Redis), Awaitility

## 3. 모듈 구조

```
backend/
├── core/    # 공통 핵심: 도메인 엔티티, JPA, 인프라, 로깅. 다른 모듈이 의존.
│            #   java-library + java-test-fixtures (테스트 fixture 제공)
├── app/     # 모바일 앱용 REST API (이 repo 작업의 대부분). core 의존.
├── admin/   # 콘텐츠 운영 Admin (Thymeleaf + S3 업로드 + FFmpeg 오디오 자르기). core 의존.
└── boot/    # 실행 조립 모듈. app+admin 의존, bootJar 생성, Flyway 구동.
            #   진입점: com.onair.hearit.HearitApplication
```

의존 방향: `boot → app, admin → core`. **core는 어떤 상위 모듈도 의존하지 않는다**(역방향 의존 금지).

## 4. 패키지·레이어 컨벤션

> 코딩 스타일·네이밍·테스트 규칙 전체는 [conventions.md](./conventions.md) 가 정본. 아래는 실제 모듈 배치 요약.

`app` 모듈은 도메인별 수직 분할 후, 도메인 안에서 레이어 분할한다.

```
com.onair.hearit.app.<domain>/
├── presentation/    # @RestController. 요청/응답 변환, URL은 /api/v1/...
├── application/     # @Service. 비즈니스 로직.
├── dto/             # request/response/param/converter
└── infrastructure/  # 외부 연동·버퍼·스케줄러 (필요한 도메인만)
```

도메인 엔티티는 **core** 의 `core.domain` 에 모여 있다. 엔티티는 `@NoArgsConstructor(PROTECTED)` + 정적 팩토리 / 검증 로직을 갖고, 위반 시 도메인별 `*DomainException` 을 던진다.

## 5. 도메인 용어 (Ubiquitous Language)

| 용어 | 의미 |
|---|---|
| **Hearit** | 팟캐스트 콘텐츠 1편(핵심 엔티티). title·summary·playTime·viewCount·오디오 파일(`FileUrls`)·출처(`Source`) 보유 |
| **Short-cast** | 1분 분량 숏폼. `explore` 도메인이 점수화해 스크롤 탐색 제공 |
| **Explore / ExploreScore** | 콘텐츠 기반 필터링으로 숏캐스트를 개인화 정렬하는 추천 점수 (`scorefactor`·`scoreprocessor`) |
| **RecommendHearit** | 홈 화면 맞춤 추천 콘텐츠 (전략 패턴 `strategy/`) |
| **Bookmark** | 사용자가 저장한 카테고리/콘텐츠 |
| **PlayingHistory** | 재생 기록. 빈번한 저장을 **버퍼 + 스케줄러 배치**로 일괄 저장 (`infrastructure/buffer`, `infrastructure/scheduler`) |
| **Reaction** | 콘텐츠 반응 (`ReactionType`) |
| **Keyword / HearitKeyword** | 검색·분류용 키워드와 Hearit 연결 |
| **Member / UserInfo / UserType** | 회원. 비회원도 일부 기능 이용 가능 |
| **Category** | 콘텐츠 카테고리 |
| **Advertisement** | 광고 콘텐츠 |

## 5-1. 주의 — 모르면 틀리는 지점 (Footguns)

> 코드에 근거가 확인된 비자명 제약만 기록. 작업 전 반드시 숙지.

- **재생 기록은 DB에 직접 쓰지 않는다.** `PlayingHistoryService` 는 `playingHistoryBuffer.add()` 로 버퍼에 적재하고, 실제 DB 반영은 `PlayingHistoryFlushScheduler`(ShedLock 분산 락)가 배치로 flush 한다. 버퍼는 Redis(Circuit Breaker로 보호) → 장애 시 in-memory map 으로 fallback. 재생 기록 저장 로직을 `repository.save()` 직접 호출로 바꾸면 이 배치/락 설계가 깨진다.

## 6. 주요 경로 (빠른 진입점)

- **기능 지도 / SPEC 인덱스** — `docs/specs/README.md` (백엔드 전체 기능·엔드포인트 한눈에)
- 도메인 엔티티 — `core/src/main/java/com/onair/hearit/core/domain/`
- 컨트롤러(앱 API) — `app/src/main/java/com/onair/hearit/app/<domain>/presentation/`
- 전역 예외 처리 — `app/.../app/exception/ApiGlobalExceptionHandler.java`
- 도메인 에러 코드 — `core/.../domain/exception/DomainErrorCode.java`
- DB 마이그레이션 — `core/src/main/resources/db/migration/`
- 로컬 인프라 — `backend/docker-compose.yml` (MySQL prod/test, Redis)
- CI — `.github/workflows/backend-dev-ci.yml`, `backend-prod-ci.yml`

## 7. 빌드 · 테스트 실행법

```bash
# (작업 디렉토리: backend/)
./gradlew test              # 전체 테스트 (JUnit5). CI와 동일
./gradlew build -x test     # 테스트 제외 빌드 (CI 2단계)
./gradlew :app:test         # app 모듈만
./gradlew test --tests 'com.onair.hearit.app.hearit.*'   # 특정 패키지

# 로컬 인프라 (테스트/구동 전 MySQL·Redis 필요)
docker compose up -d        # backend/docker-compose.yml (.env 필요)
```

- 테스트는 **MySQL + Redis** 를 요구한다 (CI는 service container로 MySQL/Redis 기동).
- REST Docs 테스트가 통과해야 OpenAPI/Swagger 문서가 생성된다 — 문서와 코드 불일치 방지 장치.

## 8. 검증 축 — 오라클 앵커

> "결과가 올바른가"를 판정하는 근거 위치. PR 본문 `oracle_ref:` 가 가리킬 후보.

- **API 행동 명세** — 각 컨트롤러에 대응하는 `app/.../<domain>/presentation/<X>ControllerTest.java` (`app` 은 **MockMvc + REST Docs**, `admin` 은 RestAssured). 생성된 OpenAPI3 스펙이 계약(contract) 역할.
  - 공통 베이스 클래스 `ControllerTest` → import `com.onair.hearit.app.fixture.ControllerTest`
  - REST Docs 스니펫 헬퍼 `ApiDocSnippets` → import **`com.onair.hearit.fixture.ApiDocSnippets`** (주의: 파일은 `app/src/test/.../app/fixture/` 에 있지만 패키지 선언은 `com.onair.hearit.fixture` 로 `.app.` 이 빠진다 — import 시 디렉토리 경로 그대로 쓰면 컴파일 실패)
- **도메인 불변식** — `core.domain` 엔티티의 검증 로직 + `*DomainException` / `DomainErrorCode`. "무엇이 통과/실패인가"의 1차 기준.
- **테스트 fixture** — `core` 의 `testFixtures` (도메인 객체 생성 헬퍼). 새 테스트는 여기 재사용.
- 향후 판정 기준 문서는 `docs/oracles/` 에 평문으로 누적 (Step 2).

## 9. PR 의도 참조 라인 (Harness Engineering)

신규 기능 런치 PR 본문에 아래 3라인을 채운다 (앞으로의 PR부터, 소급 금지):

```
spec: docs/specs/<id>.md
guide: docs/lld/<...>.md  또는  docs/adr/<...>.md
oracle_ref: docs/oracles/<...>.md  또는  대응 *ControllerTest 경로
```

탐색·실험 PR은 생략 가능. AI가 판단할 수 없는 빈 곳은 임의로 채우지 말고 `TODO(질문):` 로 명시한다.

## 10. 브랜치 / 배포

- `develop-be` — 백엔드 dev 통합 브랜치 (PR 대상, CI 작동)
- `release` — 운영(prod) 기준
- CD: `backend-dev-cd.yml` / `backend-prod-cd.yml`, 수동 릴리즈·롤백 워크플로우 별도
