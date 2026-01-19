# AI 백오피스 리팩토링 계획

## 개요

AI 백오피스 기능의 코드 품질 개선 및 유지보수성 향상을 위한 리팩토링 계획입니다.

---

## 현재 코드 분석

### 긍정적인 점

| 항목 | 설명 |
|------|------|
| **패키지 구조** | presentation, application, infrastructure, domain, dto로 잘 분리됨 |
| **비동기 처리** | `@Async`와 ThreadPool을 활용한 비동기 처리 적절히 구현 |
| **트랜잭션 분리** | `AiProcessStatusUpdater`로 상태 업데이트 트랜잭션 분리 (REQUIRES_NEW) |
| **Rate Limiting** | Gemini API의 Semaphore + Exponential Backoff 재시도 구현 |
| **JSON 변환** | JPA AttributeConverter로 `List<ScriptSegment>` ↔ JSON 자동 변환 |

---

## 리팩토링 대상

### 1. 코드 중복 제거 (High Priority)

#### 1.1 파일 삭제 유틸리티 중복

**현재 상태**: `deleteIfExists()` 메서드가 3곳에 중복

```
- AiProcessingService.java:157-165
- AiResultService.java:139-147
- AiResultCleanupScheduler.java (예상)
```

**해결 방안**: `TempFileManager` 유틸리티 클래스 생성

```java
// admin/ai/infrastructure/storage/TempFileManager.java
@Component
@RequiredArgsConstructor
@Slf4j
public class TempFileManager {

    private final FileStorage fileStorage;

    public void deleteIfExists(String key) {
        if (key == null || key.isBlank()) return;
        try {
            fileStorage.deleteFile(key);
        } catch (Exception e) {
            log.debug("파일 삭제 실패 (무시): {}", key);
        }
    }

    public void cleanupTempFiles(AiProcessResult result) {
        deleteIfExists(result.getOriginalFileKey());
        deleteIfExists(result.getGeneratedOrgKey());
        deleteIfExists(result.getGeneratedShrKey());
        deleteIfExists(result.getGeneratedScrKey());
    }
}
```

---

#### 1.2 JSON 파싱 유틸리티 중복

**현재 상태**: JSON 추출 메서드가 여러 서비스에 중복

```
- ScriptCorrectionService.extractJsonArray():113-121
- MetadataGenerationService.extractJsonObject():84-91
```

**해결 방안**: `JsonExtractionUtil` 유틸리티 클래스 생성

```java
// admin/ai/util/JsonExtractionUtil.java
public final class JsonExtractionUtil {

    private JsonExtractionUtil() {}

    public static String extractJsonArray(String response) {
        return extractJson(response, '[', ']');
    }

    public static String extractJsonObject(String response) {
        return extractJson(response, '{', '}');
    }

    private static String extractJson(String response, char startChar, char endChar) {
        int start = response.indexOf(startChar);
        int end = response.lastIndexOf(endChar);

        if (start == -1 || end == -1 || start >= end) {
            return response;
        }
        return response.substring(start, end + 1);
    }
}
```

---

### 2. 설정 관리 통합 (Medium Priority)

#### 2.1 분산된 @Value 어노테이션

**현재 상태**: AI 관련 설정이 여러 클래스에 분산

```
- AiProcessingService: expirationHours, shortsDurationSeconds, tempPrefix
- AiResultService: bucketUrl
- AiResultController: bucketUrl (중복!)
- GeminiClient: apiKey, model
- GroqWhisperClient: apiKey, model
```

**해결 방안**: `AiProperties` 설정 클래스로 통합

```java
// admin/ai/config/AiProperties.java
@Configuration
@ConfigurationProperties(prefix = "ai")
@Getter
@Setter
public class AiProperties {

    private Result result = new Result();
    private Shorts shorts = new Shorts();
    private S3 s3 = new S3();
    private Groq groq = new Groq();
    private Gemini gemini = new Gemini();

    @Getter @Setter
    public static class Result {
        private int expirationHours = 24;
    }

    @Getter @Setter
    public static class Shorts {
        private int durationSeconds = 60;
    }

    @Getter @Setter
    public static class S3 {
        private String tempPrefix = "hearit/temp/";
        private String bucketUrl;
    }

    @Getter @Setter
    public static class Groq {
        private String apiKey;
        private String model = "whisper-large-v3-turbo";
    }

    @Getter @Setter
    public static class Gemini {
        private String apiKey;
        private String model = "gemini-2.5-flash";
    }
}
```

**application.properties 변경**:
```properties
ai.result.expiration-hours=24
ai.shorts.duration-seconds=60
ai.s3.temp-prefix=hearit/temp/
ai.s3.bucket-url=${AWS_S3_BUCKET_URL}
ai.groq.api-key=${GROQ_API_KEY}
ai.groq.model=whisper-large-v3-turbo
ai.gemini.api-key=${GEMINI_API_KEY}
ai.gemini.model=gemini-2.5-flash
```

---

### 3. 테스트 용이성 개선 (High Priority)

#### 3.1 GeminiClient의 static 필드 문제

**현재 상태**: Rate limiting에 static 필드 사용 → 테스트 어려움

```java
// GeminiClient.java:32-38
private static final Semaphore rateLimiter = new Semaphore(1);
private static volatile long lastRequestTime = 0;
```

**해결 방안**: `RateLimiter` 컴포넌트 분리

```java
// admin/ai/infrastructure/ratelimit/GeminiRateLimiter.java
@Component
@Slf4j
public class GeminiRateLimiter {

    private final Semaphore semaphore;
    private final long minIntervalMs;
    private volatile long lastRequestTime = 0;

    public GeminiRateLimiter(
            @Value("${ai.gemini.rate-limit.permits:1}") int permits,
            @Value("${ai.gemini.rate-limit.min-interval-ms:4000}") long minIntervalMs) {
        this.semaphore = new Semaphore(permits);
        this.minIntervalMs = minIntervalMs;
    }

    public <T> T executeWithRateLimit(Supplier<T> action) throws InterruptedException {
        semaphore.acquire();
        try {
            waitForMinInterval();
            T result = action.get();
            lastRequestTime = System.currentTimeMillis();
            return result;
        } finally {
            semaphore.release();
        }
    }

    private void waitForMinInterval() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;

        if (elapsed < minIntervalMs && lastRequestTime > 0) {
            try {
                Thread.sleep(minIntervalMs - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

---

### 4. 예외 처리 개선 (Medium Priority)

#### 4.1 예외 클래스 세분화

**현재 상태**: `AudioProcessingException` 하나로 모든 예외 처리

**해결 방안**: 목적별 예외 클래스 분리

```java
// admin/ai/exception/AiProcessingException.java (기존 이름 변경)
public abstract class AiProcessingException extends RuntimeException {
    protected AiProcessingException(String message) {
        super(message);
    }
    protected AiProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

// admin/ai/exception/AiApiException.java
public class AiApiException extends AiProcessingException {
    private final String apiName;
    private final int statusCode;

    public AiApiException(String apiName, String message, int statusCode) {
        super(String.format("%s API 호출 실패: %s", apiName, message));
        this.apiName = apiName;
        this.statusCode = statusCode;
    }
}

// admin/ai/exception/AiFileProcessingException.java
public class AiFileProcessingException extends AiProcessingException {
    public AiFileProcessingException(String message) {
        super(message);
    }
}

// admin/ai/exception/AiValidationException.java
public class AiValidationException extends AiProcessingException {
    public AiValidationException(String message) {
        super(message);
    }
}
```

---

### 5. 책임 분리 개선 (Medium Priority)

#### 5.1 AiResultController의 bucketUrl 제거

**현재 상태**: Controller에서 bucketUrl 주입 후 Response DTO에 전달

```java
// AiResultController.java:37-38
@Value("${aws.s3.bucket.url}")
private String bucketUrl;
```

**해결 방안**: Service 레이어에서 URL 조합 처리

```java
// AiResultService에 메서드 추가
public AiResultResponse getResultResponse(Long processId) {
    AiProcessResult result = getResult(processId);
    return AiResultResponse.from(result, bucketUrl);
}

// AiResultController 수정
@GetMapping("/{processId}")
public ResponseEntity<AiResultResponse> getResult(@PathVariable Long processId) {
    return ResponseEntity.ok(resultService.getResultResponse(processId));
}
```

---

#### 5.2 AiResultResponse의 URL 조합 로직

**현재 상태**: Response DTO 내에서 URL 조합

```java
// AiResultResponse.java:66-71
private static String toUrl(String bucketUrl, String key) {
    if (key == null || key.isBlank()) return null;
    return bucketUrl + "/" + key;
}
```

**해결 방안**: `S3UrlResolver` 유틸리티 클래스 생성

```java
// admin/ai/infrastructure/storage/S3UrlResolver.java
@Component
@RequiredArgsConstructor
public class S3UrlResolver {

    private final AiProperties aiProperties;

    public String toUrl(String key) {
        if (key == null || key.isBlank()) return null;
        String bucketUrl = aiProperties.getS3().getBucketUrl();
        return bucketUrl + "/" + key;
    }
}
```

---

### 6. ProcessStatus와 ProcessStatusUtil 통합 (High Priority)

#### 6.1 별도 Util 클래스 제거

**현재 상태**: ProcessStatus enum과 ProcessStatusUtil 클래스가 분리

```java
// ProcessStatus.java - 단순 enum
public enum ProcessStatus {
    PENDING, UPLOADING, CONVERTING, ...
}

// ProcessStatusUtil.java - progress와 message를 별도 Map으로 관리
private static final Map<ProcessStatus, Integer> PROGRESS_MAP = Map.of(...);
public static String getStatusMessage(ProcessStatus status) { ... }
```

**해결 방안**: enum에 progress와 message를 필드로 포함

```java
// admin/ai/domain/ProcessStatus.java
@Getter
@RequiredArgsConstructor
public enum ProcessStatus {
    PENDING(0, "대기 중..."),
    UPLOADING(10, "파일 업로드 중..."),
    CONVERTING(25, "오디오 처리 중..."),
    TRANSCRIBING(50, "음성 인식 중... (1-2분 소요)"),
    CORRECTING(70, "대본 교정 중..."),
    GENERATING_META(85, "메타데이터 생성 중..."),
    COMPLETED(100, "처리 완료!"),
    FAILED(0, "처리 실패"),
    CONFIRMED(100, "등록 완료");

    private final int progress;
    private final String message;
}
```

**변경 후 사용**:
```java
// Before
int progress = ProcessStatusUtil.getProgress(status);
String message = ProcessStatusUtil.getStatusMessage(status);

// After
int progress = status.getProgress();
String message = status.getMessage();
```

**삭제 대상**: `ProcessStatusUtil.java`

---

### 7. @Value 주입 → 생성자 주입 (Medium Priority)

#### 7.1 필드 주입에서 생성자 주입으로 변경

**현재 상태**: @Value로 필드 주입 (테스트하기 어려움)

```java
// Mp3AudioProcessor.java
@Value("${ai.shorts.duration.seconds:60}")
private int shortsDurationSeconds;

// GeminiClient.java
@Value("${gemini.api.key}")
private String apiKey;
```

**해결 방안**: private final + 생성자 주입

```java
// Mp3AudioProcessor.java
@Component
@Slf4j
public class Mp3AudioProcessor {

    private final int shortsDurationSeconds;

    public Mp3AudioProcessor(
            @Value("${ai.shorts.duration.seconds:60}") int shortsDurationSeconds) {
        this.shortsDurationSeconds = shortsDurationSeconds;
    }
}

// GeminiClient.java
@Component
@Slf4j
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            @Qualifier("aiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }
}
```

**장점**:
- 불변성 보장 (final)
- 테스트 시 모킹 용이
- 의존성 명시적 표현
- NPE 조기 발견

**적용 대상**:
- `Mp3AudioProcessor`
- `GeminiClient`
- `GroqWhisperClient`
- `AiProcessingService`
- `AiResultService`

---

### 8. M4A 오디오 포맷 지원 (Medium Priority)

#### 8.1 AudioProcessor 인터페이스 추상화

**현재 상태**: MP3만 지원하는 `Mp3AudioProcessor`

```java
// Mp3AudioProcessor.java
public void validateMp3(byte[] data, String filename) {
    if (!filename.toLowerCase().endsWith(".mp3")) {
        throw AudioProcessingException.unsupportedFormat("MP3 파일만 지원합니다.");
    }
}
```

**해결 방안**: AudioProcessor 인터페이스 + 포맷별 구현체

```java
// admin/ai/infrastructure/audio/AudioProcessor.java
public interface AudioProcessor {

    boolean supports(String filename);

    void validate(byte[] data, String filename);

    byte[] createShortClip(byte[] originalAudio, int durationSeconds);

    AudioMetadata extractMetadata(byte[] audioData);

    @Getter
    @AllArgsConstructor
    class AudioMetadata {
        private final int bitrate;
        private final double durationSeconds;

        public int getDurationSecondsInt() {
            return (int) Math.ceil(durationSeconds);
        }
    }
}
```

```java
// admin/ai/infrastructure/audio/Mp3AudioProcessor.java
@Component
@Slf4j
public class Mp3AudioProcessor implements AudioProcessor {

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".mp3");
    }

    @Override
    public void validate(byte[] data, String filename) {
        // MP3 검증 로직
    }

    // ... 기존 로직
}
```

```java
// admin/ai/infrastructure/audio/M4aAudioProcessor.java
@Component
@Slf4j
public class M4aAudioProcessor implements AudioProcessor {

    @Override
    public boolean supports(String filename) {
        return filename != null &&
               (filename.toLowerCase().endsWith(".m4a") ||
                filename.toLowerCase().endsWith(".aac"));
    }

    @Override
    public void validate(byte[] data, String filename) {
        // M4A/AAC 검증 로직
        // ftyp 매직 바이트 확인: "ftyp" at offset 4
    }

    @Override
    public byte[] createShortClip(byte[] originalAudio, int durationSeconds) {
        // M4A는 AAC 컨테이너이므로 단순 바이트 자르기 어려움
        // 옵션 1: FFmpeg 사용
        // 옵션 2: JAVE2 라이브러리 사용
        // 옵션 3: mp4parser 라이브러리 사용
    }

    @Override
    public AudioMetadata extractMetadata(byte[] audioData) {
        // mp4parser 또는 JAudioTagger로 메타데이터 추출
    }
}
```

```java
// admin/ai/infrastructure/audio/AudioProcessorResolver.java
@Component
@RequiredArgsConstructor
public class AudioProcessorResolver {

    private final List<AudioProcessor> processors;

    public AudioProcessor resolve(String filename) {
        return processors.stream()
                .filter(p -> p.supports(filename))
                .findFirst()
                .orElseThrow(() -> AudioProcessingException.unsupportedFormat(
                        "지원하지 않는 오디오 형식입니다: " + filename));
    }
}
```

**AiProcessingService 수정**:
```java
// Before
mp3Processor.validateMp3(audioData, filename);
byte[] shortsData = mp3Processor.createShortClip(audioData, shortsDurationSeconds);

// After
AudioProcessor processor = audioProcessorResolver.resolve(filename);
processor.validate(audioData, filename);
byte[] shortsData = processor.createShortClip(audioData, shortsDurationSeconds);
```

**추가 의존성** (M4A 지원 시):
```groovy
// admin/build.gradle
implementation 'com.googlecode.mp4parser:isoparser:1.9.56'  // M4A 파싱
// 또는
implementation 'ws.schild:jave-all-deps:3.3.1'  // FFmpeg 래퍼
```

**지원 포맷**:
| 포맷 | 확장자 | 라이브러리 | 비고 |
|------|--------|-----------|------|
| MP3 | .mp3 | mp3spi | 현재 지원 |
| M4A/AAC | .m4a, .aac | mp4parser | 추가 예정 |

---

### 9. AiProcessStatusUpdater 개선 (Low Priority)

#### 9.1 중복 조회 로직 통합

**현재 상태**: `findById()`와 `findResultById()` 메서드 중복

```java
// AiProcessStatusUpdater.java:21-25, 104-107
public AiProcessResult findById(Long processId) { ... }
private AiProcessResult findResultById(Long processId) { ... }
```

**해결 방안**: 하나의 메서드로 통합

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public AiProcessResult findById(Long processId) {
    return resultRepository.findById(processId)
            .orElseThrow(() -> new AdminNotFoundException("AI 처리 결과", processId.toString()));
}
```

---

### 10. 프롬프트 관리 개선 (Low Priority)

#### 10.1 프롬프트 템플릿 외부화

**현재 상태**: 프롬프트가 서비스 클래스 내 상수로 정의

```java
// ScriptCorrectionService.java:26-50
private static final String CORRECTION_PROMPT_TEMPLATE = """
    당신은 IT/기술 전문 콘텐츠의 대본 교정 전문가입니다...
    """;
```

**해결 방안**: 프롬프트 리소스 파일로 분리

```
resources/
└── ai/
    └── prompts/
        ├── script-correction.txt
        └── metadata-generation.txt
```

```java
// admin/ai/infrastructure/prompt/PromptLoader.java
@Component
public class PromptLoader {

    @Value("classpath:ai/prompts/script-correction.txt")
    private Resource scriptCorrectionPrompt;

    @Value("classpath:ai/prompts/metadata-generation.txt")
    private Resource metadataGenerationPrompt;

    public String getScriptCorrectionPrompt() throws IOException {
        return new String(scriptCorrectionPrompt.getInputStream().readAllBytes(),
                          StandardCharsets.UTF_8);
    }

    public String getMetadataGenerationPrompt() throws IOException {
        return new String(metadataGenerationPrompt.getInputStream().readAllBytes(),
                          StandardCharsets.UTF_8);
    }
}
```

---

### 11. 로깅 일관성 개선 (Low Priority)

#### 11.1 로깅 포맷 통일

**현재 상태**: 로깅 형식이 일관되지 않음

```java
// 다양한 형식
log.info("AI 처리 시작: 파일={}, 크기={}KB", ...);
log.debug("원본 파일 업로드 중: {}", ...);
log.info("Groq Whisper 파싱 완료: 재생시간={:.1f}초, 세그먼트={}개", ...);
```

**해결 방안**: 로깅 형식 표준화

```java
// 표준 형식
// [작업명] 상세내용 - key=value, key=value

log.info("[AI처리시작] 파일={}, 크기={}KB", filename, size);
log.debug("[파일업로드] key={}", originalFileKey);
log.info("[STT완료] duration={}초, segments={}개", duration, segments.size());
```

---

## 리팩토링 우선순위

| 우선순위 | 항목 | 예상 소요 | 영향도 |
|---------|------|----------|-------|
| **1** | 코드 중복 제거 (TempFileManager) | 30분 | 높음 |
| **2** | ProcessStatus + ProcessStatusUtil 통합 | 30분 | 높음 |
| **3** | 테스트 용이성 (GeminiRateLimiter 분리) | 1시간 | 높음 |
| **4** | @Value → 생성자 주입 (private final) | 1시간 | 높음 |
| **5** | 설정 관리 통합 (AiProperties) | 1시간 | 중간 |
| **6** | M4A 오디오 포맷 지원 (AudioProcessor 추상화) | 2시간 | 중간 |
| **7** | 예외 처리 개선 | 30분 | 중간 |
| **8** | 책임 분리 (URL 조합) | 30분 | 낮음 |
| **9** | AiProcessStatusUpdater 개선 | 15분 | 낮음 |
| **10** | 프롬프트 외부화 | 30분 | 낮음 |
| **11** | 로깅 일관성 | 30분 | 낮음 |

---

## 리팩토링 후 패키지 구조

```
admin/ai/
├── application/
│   ├── AiProcessingService.java
│   ├── AiResultService.java
│   ├── AiProcessStatusUpdater.java
│   ├── TranscriptionService.java
│   ├── ScriptCorrectionService.java
│   └── MetadataGenerationService.java
│
├── config/
│   ├── AiConfig.java
│   └── AiProperties.java              # NEW
│
├── domain/
│   ├── AiProcessResult.java
│   ├── ProcessStatus.java             # MODIFIED (progress, message 필드 추가)
│   └── ScriptSegmentListConverter.java
│   # ProcessStatusUtil.java           # DELETED
│
├── dto/
│   ├── ScriptSegment.java
│   ├── request/
│   │   ├── ConfirmRequest.java
│   │   ├── MetadataUpdateRequest.java
│   │   └── ScriptUpdateRequest.java
│   └── response/
│       ├── AiProcessStatusResponse.java
│       └── AiResultResponse.java
│
├── exception/
│   ├── AiProcessingException.java     # RENAMED (기존 AudioProcessingException)
│   ├── AiApiException.java            # NEW
│   ├── AiFileProcessingException.java # NEW
│   └── AiValidationException.java     # NEW
│
├── infrastructure/
│   ├── audio/
│   │   ├── AudioProcessor.java        # NEW (인터페이스)
│   │   ├── AudioProcessorResolver.java # NEW
│   │   ├── Mp3AudioProcessor.java     # MODIFIED (implements AudioProcessor)
│   │   └── M4aAudioProcessor.java     # NEW
│   ├── gemini/
│   │   └── GeminiClient.java          # MODIFIED (생성자 주입)
│   ├── groq/
│   │   └── GroqWhisperClient.java     # MODIFIED (생성자 주입)
│   ├── jpa/
│   │   └── AiProcessResultRepository.java
│   ├── prompt/
│   │   └── PromptLoader.java          # NEW
│   ├── ratelimit/
│   │   └── GeminiRateLimiter.java     # NEW
│   └── storage/
│       ├── TempFileManager.java       # NEW
│       └── S3UrlResolver.java         # NEW
│
├── presentation/
│   ├── AiProcessController.java
│   ├── AiResultController.java
│   └── AiViewController.java
│
├── scheduler/
│   └── AiResultCleanupScheduler.java
│
└── util/
    └── JsonExtractionUtil.java        # NEW
```

---

## 구현 체크리스트

### Phase 1: 핵심 리팩토링 (High Priority)

- [x] TempFileManager 생성 및 중복 코드 대체
- [x] ProcessStatus + ProcessStatusUtil 통합 (enum에 필드 추가)
- [ ] GeminiRateLimiter 분리
- [x] @Value → 생성자 주입 변경 (private final)
- [ ] AiProperties 설정 클래스 생성
- [ ] 예외 클래스 세분화

### Phase 2: 구조 개선 (Medium Priority)

- [x] AudioProcessor 인터페이스 추상화
- [x] M4aAudioProcessor 구현
- [x] AudioProcessorResolver 생성
- [ ] JsonExtractionUtil 생성
- [ ] S3UrlResolver 생성 및 적용
- [ ] AiResultController에서 bucketUrl 제거
- [ ] AiProcessStatusUpdater 중복 메서드 통합

### Phase 3: 품질 향상 (Low Priority)

- [x] 프롬프트 외부화 (PromptLoader)
- [ ] 로깅 포맷 통일
- [ ] 테스트 코드 개선

---

## 테스트 영향도

| 리팩토링 항목 | 기존 테스트 영향 | 추가 테스트 필요 |
|--------------|-----------------|-----------------|
| TempFileManager | 없음 | TempFileManagerTest |
| ProcessStatus 통합 | ProcessStatusUtilTest 삭제 | 없음 (enum 메서드 테스트) |
| GeminiRateLimiter | GeminiClientTest 수정 | GeminiRateLimiterTest |
| 생성자 주입 변경 | 테스트 빈 설정 변경 | 없음 |
| AiProperties | 설정 주입 방식 변경 | AiPropertiesTest |
| AudioProcessor 추상화 | Mp3AudioProcessorTest 수정 ✓ | M4aAudioProcessorTest ✓, AudioProcessorResolverTest ✓ |
| 예외 클래스 | 예외 타입 검증 수정 | 없음 |

---

## 주의사항

1. **점진적 리팩토링**: 한 번에 모든 변경을 하지 않고, 단계별로 진행
2. **테스트 우선**: 각 리팩토링 전에 기존 테스트가 통과하는지 확인
3. **하위 호환성**: API 응답 구조는 변경하지 않음
4. **롤백 계획**: 각 단계별로 git commit 생성

---

## 참고 문서

- [AI_BACKOFFICE_PLAN.md](./AI_BACKOFFICE_PLAN.md) - 원본 구현 계획
- [CLAUDE.md](./CLAUDE.md) - 프로젝트 가이드
