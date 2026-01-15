rnd # CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build the entire project
./gradlew build

# Build without tests
./gradlew build -x test

# Run tests for all modules
./gradlew test

# Run tests for a specific module
./gradlew :app:test
./gradlew :core:test
./gradlew :admin:test

# Run a single test class
./gradlew :app:test --tests "com.onair.hearit.app.auth.AuthServiceTest"

# Run a single test method
./gradlew :app:test --tests "com.onair.hearit.app.auth.AuthServiceTest.loginSuccessTest"

# Generate Swagger UI (requires running tests first for REST Docs)
./gradlew :app:generateSwaggerUISample

# Create bootable JAR
./gradlew :boot:bootJar

# Start local services (MySQL + Redis)
docker-compose up -d

# Run the application
./gradlew :boot:bootRun
```

## Project Architecture

This is a multi-module Spring Boot 3.5.3 backend for "Hearit" - an audio learning content platform.

### Module Structure

```
hearit-backend/
├── core/          # Shared library - entities, repositories, configuration
├── app/           # REST API module - user-facing endpoints
├── admin/         # Admin web interface - Thymeleaf + file upload (S3)
└── boot/          # Application entry point - bundles app + admin into bootJar
```

**Dependency graph:** `boot → app → core` and `boot → admin → core`

Only the `boot` module produces an executable JAR. Other modules have `bootJar { enabled = false }`.

### Key Technology Stack

- **Java 21**, Spring Boot 3.5.3, Gradle
- **Database:** MySQL 8.0 with Master-Replica routing (DataSourceConfig)
- **Migrations:** Flyway (`core/src/main/resources/db/migration/`)
- **Auth:** JWT + Kakao OAuth
- **Caching:** Redis with Redisson, ShedLock for distributed scheduling
- **Logging:** Log4j2 with JSON structured logging
- **Testing:** JUnit 5, TestContainers (Redis), H2 in-memory DB
- **API Docs:** Spring REST Docs → OpenAPI3 → Swagger UI

### Package Organization

**Core module** (`com.onair.hearit.core`):

- `domain/` - JPA entities with business logic (Member, Hearit, Category, Bookmark, PlayingHistory, etc.)
- `infrastructure/jpa/` - Spring Data repositories
- `infrastructure/jdbc/` - JDBC for batch operations
- `config/` - DataSource, Security beans
- `log/` - Request logging, MDC, sensitive data masking

**App module** (`com.onair.hearit.app`):

- Feature packages: `auth/`, `hearit/`, `bookmark/`, `playinghistory/`, `explore/`, `category/`, `keyword/`,
  `recommendation/`, `member/`
- Each feature has: `application/` (services), `presentation/` (controllers), `dto/`
- `auth/infrastructure/jwt/` - JwtTokenProvider, JwtAuthenticationFilter
- `auth/infrastructure/oauth/kakao/` - Kakao OAuth integration

**Admin module** (`com.onair.hearit.admin`):

- Form-based login (not JWT)
- Thymeleaf views in `src/main/resources/templates/admin/`
- S3 file upload for content management

### Security Configuration

Two separate security filter chains:

1. **AdminSecurityConfig** (order=1): `/admin/**` - Form login, session-based
2. **ApiSecurityConfig**: `/api/**` - Stateless JWT, OAuth

Public API endpoints (no auth required):

- `GET /api/v1/hearits/**`, `/api/v1/categories/**`, `/api/v1/keywords/**`
- `POST /api/v1/auth/{login,kakao-login,signup,token/refresh}`

### Testing Patterns

- Test profiles: `application-fake-test.properties` (H2), `application-integration-test.properties` (MySQL)
- Test fixtures in `core/src/testFixtures/` - shared test utilities, DbHelper
- Use `@DataJpaTest` for repository tests, `@Import` for required services
- TestContainers for Redis in integration tests
- REST Docs tests generate OpenAPI spec for Swagger UI

### Database

Local development uses Docker containers:

- Production DB: port from `${MYSQL_PORT}`
- Test DB: port from `${TEST_MYSQL_PORT}`
- Redis: port from `${REDIS_PORT:-6379}`

Configure via `.env` file (not committed).
