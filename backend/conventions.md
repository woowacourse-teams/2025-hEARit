# Backend 코드 컨벤션

> hEARit 백엔드의 코딩 스타일·디렉토리 규칙 정본(SoT). PR 리뷰·AI 구현 시 이 문서를 기준으로 한다.
> 도메인 용어·모듈 구조·테스트 실행법은 [AGENTS.md](./AGENTS.md) 참조.

## 1. 스타일 파일

IntelliJ `Code Style - Java - Schema` 에서 **WootecoStyle** 을 적용해 코드 스타일을 일괄 통일한다.

- WootecoStyle: [`intellij-java-wooteco-style.xml`](https://github.com/woowacourse/woowacourse-docs/blob/main/styleguide/java/intellij-java-wooteco-style.xml)

## 2. 패키지 구조

계층형 아키텍처(Layered Architecture) 기반. 역할 기반으로 명확히 구분해 애매한 위치의 클래스를 방지하고, 패키지 선택의 혼동을 줄여 일관성·유지보수성을 높인다.

각 레이어의 **역할 규칙**:

| 레이어 | 역할 |
|---|---|
| `presentation` | Controller 등 API 요청 처리 |
| `application` | Service 등 비즈니스 로직 |
| `domain` | Entity, VO |
| `infrastructure` | JPA Repository, 외부 연동 |
| `common` | 공통 설정, 예외 처리 |
| `dto` | 계층 간 공용 DTO |

**모듈 배치** — 이 repo는 멀티모듈이라 위 레이어가 모듈에 걸쳐 배치된다 (상세는 [AGENTS.md](./AGENTS.md) §3·§4):

```
com.onair.hearit
├── core    // domain(Entity·VO), infrastructure(JPA/jdbc), 공통 인프라·로깅
└── app     // 도메인별 수직 분할 후 presentation / application / dto 레이어
    └── <domain>/{presentation, application, dto, infrastructure}
```

도메인 엔티티는 `core.domain` 에 집중하고, `app` 의 각 도메인은 presentation/application/dto 로 레이어를 나눈다.

## 3. 공통 스타일 가이드

### 네이밍

- 변수는 CamelCase 기본 — `userEmail`
- 패키지명은 단어가 달라져도 무조건 소문자 — `frontend`, `useremail`
- URL·파일명은 kebab-case — `/user-email-page`
- ENUM·상수는 대문자 — `NORMAL_STATUS`
- 함수명은 소문자 시작, 동사 — `getUserId()`, `isNormal()`
- 클래스명은 명사, UpperCamelCase — `UserEmail`
- 객체 이름을 함수 이름에 중복하지 않는다 — `line.getLength()` O / `line.getLineLength()` X
- 컬렉션은 복수형, 컬렉션명을 넣지 않는다 — `List ids`
- 의도가 드러나면 되도록 짧은 이름 — `retrieveUser()` X / `getUser()` O
- 함수는 하나의 역할. 부수효과가 있으면 메서드명으로 표현 — `move()` X / `moveAndKill()` O

### 메서드 네이밍

조회 함수는 일관성을 위해 Repository는 `findXXX`, Service는 되도록 `getXXX`, 외부 리소스/IO를 읽을 땐 `readXXX`.

| 구분 | 의미 | 상황 | 예시 | 특징 |
|---|---|---|---|---|
| get | 존재 확신하는 데이터 | 필드 접근, 존재 보장 참조 | `getUserId()`, `getName()` | 예외 없음 또는 NPE 가능 |
| find | 존재할 수도/없을 수도 | Repository, 조건 검색 | `findUserById()`, `findOrdersByDate()` | Optional 또는 null 반환 |
| read | 외부 리소스/IO·상태 읽기 | 파일, HTTP, DB 레코드 | `readFile()`, `readFromDB()` | side-effect free |

Controller·Service 메서드 네이밍 규칙:

| 요청 | controller | service |
|---|---|---|
| 목록 조회 | readXXX | getXXXs, findXXX |
| 단건 상세 조회 | readXXX | getXXX, findXXX |
| 등록 | createXXX | addXXX |
| 수정 | updateXXX | modifyXXX |
| 삭제 | deleteXXX | removeXXX |

정적 팩토리 메서드명:
- `from` — 파라미터 1개
- `of` — 파라미터 2개 이상

### 메서드 작성 순서

조회 쿼리를 상단에, 데이터 조작은 **RCUD**(read → create → update → delete) 순.

```java
public class ExampleService {

    // public → 바로 아래 관련 private 메서드 위치
    public void doSomething() {
        helperMethod();
    }

    // private 가 여러 public 에서 쓰이면 마지막 public 뒤로
    private void helperMethod() {
        // ...
    }
}
```

### 접근제어자 순서

- `static → public → private` 순.
- public 메서드 아래에 관련 private 바로 위치.
- private 가 여러 곳에서 쓰이면 가장 마지막 public 뒤에 정리.

### 어노테이션

길이가 짧은 순서대로 위에서부터(피라미드).

```java
@Test
@DisplayName
void testMethod() {
}
```

## 4. Controller

- `ResponseEntity` 를 사용한다.
  - 반환 상태코드를 메서드 안에서 쉽게 파악하기 위해
  - 통일성을 위해

## 5. Domain

- `equals` & `hashCode`
  - domain·entity 모두 재정의한다.
  - equals 시 `id` 만 비교한다.
  - equals 재정의 시 `instanceof` 로 구현한다.
- `id` 는 null 의미를 위해 `Long` 타입 사용.
- domain 생성자에서 null 검사와 규칙 검사를 실시한다.

## 6. DTO

- DTO 는 Controller → Service 까지 전달되며, 변환은 Service 에서 처리한다.
  - 예: `MemberCreateRequestDto` → `Member` 변환은 Service 내에서.
- DTO 정적 팩토리 메서드(`from`, `of`)를 DTO 내부에서 활용한다.
- request DTO 유효성 검사:
  - null 검사한다.
  - 도메인 규칙은 검사하지 않는다(도메인 안에서 검사).

## 7. Lombok

| 항목 | 규칙 |
|---|---|
| 금지 어노테이션 | `@Setter`, `@Data` |

값 변경을 예측하기 어려우므로 무분별한 사용을 제한하고, 행위 메서드로 의도를 표현한다.

- 예외: `@ConfigurationProperties` 바인딩 클래스(예: `admin` 의 `LlmProviderProperties`·`SttProviderProperties`)는 setter 바인딩을 위해 `@Setter` 사용을 허용한다. (constructor binding 으로 전환해 setter 없이 immutable 하게 두는 것도 가능 — 택1)

## 8. 테스트

### Controller
- `app` 모듈: **MockMvc + REST Docs**(`restdocs-api-spec`) 인수 테스트. 베이스 클래스 `ControllerTest`, 스니펫 헬퍼 `ApiDocSnippets` 사용. 이 테스트가 통과해야 OpenAPI3/Swagger 문서가 생성된다(문서생성 파이프라인이 여기에 묶여 있음).
- `admin` 모듈: RestAssured 인수 테스트.
- 해피케이스 테스트 모두 작성(API 당 테스트).
- 각 상태코드마다 대표 케이스 1개 이상(예: 401·403·404 각각 하나씩).

### Service
- **h2 DB 통합 테스트** — `@DataJpaTest` + 실제 Repository 로 검증한다(db 를 모킹하지 않는다).
- 모킹은 리팩터링 내성을 약화시키므로 db 에는 쓰지 않는다. 외부 클라이언트(예: Kakao OAuth)·비결정성 의존(예: 난수 생성기)에 한해 모킹을 허용한다.

### Repository
- `@Query` 직접 작성 시 테스트한다.
- JpaRepository 기본 제공 메서드·쿼리 메서드는 테스트하지 않는다(비용 절감).

### 테스트 네이밍
- 테스트 이름은 `@DisplayName`.
- 테스트 메서드명은 영어로 간단하게(테스트 대상 메서드명 + 상황 설명).

### 스타일
- BDD — given / when / then.

## 9. API 문서화

- 변동이 잦은 API 반영을 위해 Swagger 사용.

## 10. 오류 및 예외 처리

- Custom 예외를 상태 코드·title 과 함께 사용한다.

```json
{
  "type": "/요청/uri",
  "title": "에러 제목",
  "status": 400,
  "detail": "에러 상세 메시지"
}
```
