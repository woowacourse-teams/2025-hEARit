# SPECs — 기능 명세 인덱스

> SPEC = 검증자 관점의 행동 명세("이 기능이 어떻게 동작해야 하나"). 파일은 `docs/specs/<id>.md`, 5섹션 구조는 아래.
> 이 인덱스는 백엔드 **기능 지도** 역할도 한다 — AI 에이전트가 "어떤 기능이 있나"를 한눈에 보는 진입점.

## 작성 규칙

- 1 기능(CUJ) = SPEC 1개가 기본. 엔드포인트마다 찍지 않는다.
- **소급 작성하지 않는다.** 기존 기능은 *바꿀 때* 또는 *명세를 확정하고 싶을 때* 누적한다.
- 5섹션: ① 메타(ID·상태·연관 LLD·오라클) ② 배경/목적 ③ Acceptance Criteria(Gherkin) ④ 비-목적 ⑤ 검증 경로
- 상태: `draft` / `active` / `archived`

## APP — 모바일 REST API

| 도메인 | 주요 기능 | 엔드포인트 prefix | SPEC |
|---|---|---|---|
| auth | 로그인/회원가입/토큰재발급/탈퇴 (JWT + Kakao OAuth) | `/api/v1/auth` | — |
| member | 내 정보 조회 | `/api/v1/members` | — |
| hearit | 콘텐츠 상세·목록(필터·정렬·페이징)·조회수 | `/api/v1/hearits` | — |
| hearit-search | 제목/키워드 검색 | `/api/v1/hearits/search` | — |
| file-source | 오디오/스크립트 URL 발급 | `/api/v1/hearits/{id}/*-url` | — |
| explore | 숏캐스트 개인화 탐색(스코어링, 커서) — **v1·v2** | `/api/v{1,2}/hearits/explore` | — |
| bookmark | 북마크 추가/삭제/목록 — **v1·v2** | `/api/v1·v2/bookmarks` | — |
| category | 카테고리 목록 | `/api/v1/categories` | — |
| keyword | 키워드 목록/단건 | `/api/v1/keywords` | — |
| playing-history | 재생기록 조회 + 기록(버퍼+배치 저장) | `/api/v1/playing-histories` | — |
| **reaction** | 좋아요 추가/취소 | `/api/v1/hearits/{id}/likes` | [reaction-like](./reaction-like.md) ✅ |
| recommendation | 북마크 카테고리 기반 추천 | `/api/v1/recommendations` | — |
| recommend-hearit | 홈 맞춤 추천 | `/api/v1/hearits/recommend` | — |
| advertisement | 랜덤 광고 | `/api/v1/advertisements` | — |

## ADMIN — 운영 + AI 파이프라인

| 영역 | 기능 |
|---|---|
| 콘텐츠 운영 API | Hearit·Category·Keyword·RecommendHearit·Storage CRUD (`/api/v1/admin/...`) |
| AI 파이프라인 | 업로드 → STT(Groq) → 전사 → 스크립트 교정 → 메타데이터 생성(Gemini) → 오디오 클리핑(FFmpeg) → S3. 결과 조회/수정/확정/삭제 (`/admin/api/ai/...`) |
| 운영 화면 | Thymeleaf 기반 업로드·결과·trace 뷰 (`/admin/...`) |

## 백그라운드 / 공통

- 스케줄러: `PlayingHistoryFlushScheduler`(ShedLock 배치 flush), `AiResultCleanupScheduler`
- 공통: JWT/OAuth 인증, `ProblemDetail`(RFC7807) 예외, Redis 버퍼 + Resilience4j Circuit Breaker, 로깅/마스킹(`core.log`)

> "—" 표시는 SPEC 미작성. 의도적 — 소급 금지, 런치/변경 시 누적한다.
