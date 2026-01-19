# AI 백오피스 기능 구현 계획

## 개요

NotebookLM으로 생성한 오디오 파일을 백오피스에서 AI로 처리하여 콘텐츠를 등록하는 기능입니다.

> **핵심 결정사항 요약**
> - **API 경로**: `/admin/api/ai/*` (AdminSecurityConfig, 세션 기반 인증)
> - **JSON 매핑**: JPA AttributeConverter로 `List<ScriptSegment>` ↔ JSON 자동 변환
> - **MP3 처리**: mp3spi 라이브러리로 정확한 메타데이터 추출 후 바이트 기반 자르기
> - **지원 포맷**: MP3만 허용 (FFmpeg 의존성 제거, NotebookLM 출력을 MP3로 변환 후 업로드)
> - **파일 크기 제한**: 25MB (Groq Whisper API 제한)
> - **Groq Whisper API 응답**: 초 단위 float → 밀리초 int 변환
> - **재생 시간**: Groq Whisper API의 `duration` 또는 마지막 segment의 `end` 사용
> - **Admin 전용**: 동시성 고려 최소화, ThreadPool/재시도 전략은 추후 개선
> - **트랜잭션 분리**: `AiProcessStatusUpdater`로 상태 업데이트 분리 (REQUIRES_NEW)
> - **S3 파일 이동**: 복사 후 원본 삭제 방식 (S3 copyObject API 활용)
> - **Gemini Rate Limiting**: Semaphore + 요청 간격 제어 + Exponential Backoff 재시도

### 현재 프로세스
1. 사용자가 NotebookLM으로 오디오 생성
2. 외부 Python 스크립트로 처리 (Whisper STT + Gemini 교정 + 쇼츠 생성)
3. 관리자 페이지에서 수동으로 3개 파일 + 메타데이터 업로드

### 목표 프로세스
1. 사용자가 NotebookLM 오디오를 관리자 페이지에 업로드
2. 백엔드에서 자동으로 AI 처리 (STT, 교정, 쇼츠 생성, 메타데이터 생성)
3. 관리자가 결과 검토/수정 후 확인하면 Hearit으로 등록

---

## 아키텍처 결정사항

| 항목 | 선택 | 근거 |
|------|------|------|
| STT | 외부 API (Groq Whisper API - 무료) | 서버 GPU 불필요, 안정적, 비용 절감 |
| LLM | Google Gemini API | 기존 스크립트와 동일 |
| 프론트엔드 | Thymeleaf 확장 | 기존 관리자 페이지와 일관성 |
| 임시 저장 | S3 `/hearit/temp/` | 기존 S3 인프라 활용 |
| 오디오 처리 | mp3spi + 바이트 자르기 | MP3 메타데이터 정확히 추출, 바이트 기반 자르기 |
| 지원 포맷 | MP3만 허용 | FFmpeg 의존성 제거, 단순화 |
| 파일 크기 | 최대 25MB | Groq Whisper API 제한 준수 |
| JSON 매핑 | JPA AttributeConverter | List<ScriptSegment> ↔ JSON 자동 변환 |
| API 경로 | `/admin/api/ai/*` | AdminSecurityConfig 보안 적용, 세션 기반 인증 |
| 트랜잭션 분리 | AiProcessStatusUpdater (REQUIRES_NEW) | 비동기 처리 중 상태 업데이트 즉시 커밋, 폴링 실시간 반영 |
| Rate Limiting | Semaphore + Exponential Backoff | Gemini 무료 tier 15 RPM 제한 대응 |

---

## Phase 1: 백엔드 구현

### 1.1 새로운 패키지 구조

```
admin/
├── ai/
│   ├── presentation/
│   │   ├── AiProcessController.java      # AI 처리 관련 엔드포인트
│   │   └── AiResultController.java       # 결과 조회/수정 엔드포인트
│   ├── application/
│   │   ├── AiProcessingService.java      # AI 처리 오케스트레이션
│   │   ├── TranscriptionService.java     # STT 외부 API 연동
│   │   ├── ScriptCorrectionService.java  # LLM 대본 교정
│   │   └── MetadataGenerationService.java # LLM 메타데이터 생성
│   ├── infrastructure/
│   │   ├── groq/
│   │   │   └── GroqWhisperClient.java    # Groq Whisper API 클라이언트 (무료)
│   │   ├── gemini/
│   │   │   ├── GeminiClient.java         # Google Gemini API 클라이언트
│   │   │   └── GeminiConfig.java
│   │   └── audio/
│   │       └── Mp3AudioProcessor.java    # MP3 쇼츠 자르기 (Java 라이브러리)
│   ├── domain/
│   │   └── AiProcessResult.java          # AI 처리 결과 엔티티
│   └── dto/
│       ├── request/
│       │   ├── AiProcessRequest.java
│       │   ├── ScriptUpdateRequest.java
│       │   └── MetadataUpdateRequest.java
│       └── response/
│           ├── AiProcessStatusResponse.java
│           └── AiResultResponse.java
```

### 1.2 API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/admin/api/ai/process` | 원본 오디오 업로드 및 AI 처리 시작 |
| `GET` | `/admin/api/ai/process/{processId}/status` | 처리 상태 조회 (Polling용) |
| `GET` | `/admin/api/ai/results/{processId}` | 완료된 AI 처리 결과 조회 |
| `PUT` | `/admin/api/ai/results/{processId}/script` | 대본 수정 |
| `PUT` | `/admin/api/ai/results/{processId}/metadata` | 메타데이터(제목, 요약) 수정 |
| `POST` | `/admin/api/ai/results/{processId}/confirm` | 검토 완료 후 Hearit 등록 |
| `DELETE` | `/admin/api/ai/results/{processId}` | AI 결과 삭제 (취소) |

> **참고**: `/admin/api/*` 경로는 AdminSecurityConfig에서 자동으로 세션 기반 인증이 적용됩니다.

### 1.3 새로운 엔티티 및 DTO

#### ScriptSegment DTO
```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScriptSegment {
    private Integer id;       // 세그먼트 순번
    private Integer start;    // 시작 시간 (밀리초)
    private Integer end;      // 종료 시간 (밀리초)
    private String text;      // 대본 텍스트
}
```

#### AiProcessResult 엔티티

```java
@Entity
@Table(name = "ai_process_result")
@EntityListeners(AuditingEntityListener.class)  // @CreatedDate 사용을 위해 필요
public class AiProcessResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    // 원본 파일 정보
    private String originalFileName;
    private String originalFileKey;  // S3 temp 경로

    // 생성된 파일들 (S3 temp 경로)
    private String generatedOrgKey;   // 변환된 원본 MP3
    private String generatedShrKey;   // 1분 쇼츠 MP3
    private String generatedScrKey;   // 대본 JSON

    // AI 생성 결과 (AttributeConverter로 자동 변환)
    @Convert(converter = ScriptSegmentListConverter.class)
    @Column(columnDefinition = "JSON")
    private List<ScriptSegment> rawTranscript;     // STT 원본 결과

    @Convert(converter = ScriptSegmentListConverter.class)
    @Column(columnDefinition = "JSON")
    private List<ScriptSegment> correctedScript;   // 교정된 대본

    private String suggestedTitle;    // AI 제안 제목

    @Column(columnDefinition = "TEXT")
    private String suggestedSummary;  // AI 제안 요약

    // 수정된 값 (사용자 입력)
    @Convert(converter = ScriptSegmentListConverter.class)
    @Column(columnDefinition = "JSON")
    private List<ScriptSegment> editedScript;      // 사용자 수정 대본

    private String editedTitle;       // 사용자 수정 제목

    @Column(columnDefinition = "TEXT")
    private String editedSummary;     // 사용자 수정 요약

    private Integer playTime;         // 재생 시간 (초)

    // 에러 정보
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // 타임스탬프
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;  // 미확인 결과 만료 시간

    // 확인 후 생성된 Hearit
    @OneToOne(fetch = FetchType.LAZY)
    private Hearit confirmedHearit;
}
```

#### ScriptSegmentListConverter (JSON 자동 변환)

```java
@Converter
public class ScriptSegmentListConverter
    implements AttributeConverter<List<ScriptSegment>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ScriptSegment> segments) {
        if (segments == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(segments);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert segments to JSON", e);
        }
    }

    @Override
    public List<ScriptSegment> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json,
                new TypeReference<List<ScriptSegment>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON to segments", e);
        }
    }
}
```

#### AiProcessResultRepository

```java
public interface AiProcessResultRepository extends JpaRepository<AiProcessResult, Long> {

    /**
     * 만료된 미확인 결과 조회 (스케줄러용)
     */
    List<AiProcessResult> findByStatusNotAndExpiresAtBefore(
        ProcessStatus status, LocalDateTime expiresAt);

    /**
     * 특정 상태의 결과 목록 조회
     */
    List<AiProcessResult> findByStatusOrderByCreatedAtDesc(ProcessStatus status);

    /**
     * 최근 N일간 처리 결과 조회 (통계용)
     */
    @Query("SELECT r FROM AiProcessResult r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<AiProcessResult> findRecentResults(@Param("since") LocalDateTime since);
}
```

### 1.4 처리 상태 (ProcessStatus)

```java
public enum ProcessStatus {
    PENDING,           // 대기 중
    UPLOADING,         // 원본 파일 S3 업로드 중
    CONVERTING,        // MP3 변환 + 쇼츠 생성 중
    TRANSCRIBING,      // STT 처리 중
    CORRECTING,        // 대본 교정 중
    GENERATING_META,   // 메타데이터 생성 중
    COMPLETED,         // 처리 완료 (검토 대기)
    FAILED,            // 처리 실패
    CONFIRMED          // 검토 완료 및 Hearit 등록됨
}
```

### 1.5 AI 처리 플로우

```
[사용자]
    │
    │ POST /admin/api/ai/process (multipart/form-data: audioFile)
    ▼
[AiProcessController]
    │
    │ 1. 파일 검증
    │ 2. AiProcessResult 생성 (status: PENDING)
    │ 3. 비동기 처리 시작 (@Async)
    │
    ▼
[AiProcessingService] (비동기)
    │
    ├─ 1. 원본 파일 S3 업로드
    │     → /hearit/temp/original/{uuid}.mp3
    │     status: UPLOADING → CONVERTING
    │
    ├─ 2. AudioProcessor: 1분 쇼츠 생성
    │     mp3spi로 비트레이트/재생시간 추출 후 바이트 기반 자르기
    │     → /hearit/temp/org/{uuid}.mp3 (원본 복사)
    │     → /hearit/temp/shr/{uuid}.mp3 (60초 쇼츠)
    │     status: CONVERTING → TRANSCRIBING
    │
    ├─ 3. TranscriptionService: STT 처리
    │     Groq Whisper API 호출 (verbose_json)
    │     응답의 segments를 ScriptSegment 형식으로 변환 (초 → 밀리초)
    │     결과를 rawTranscript에 저장
    │     playTime 계산 (duration 또는 마지막 segment.end)
    │     status: TRANSCRIBING → CORRECTING
    │
    ├─ 4. ScriptCorrectionService: 대본 교정
    │     Gemini API 호출 (IT 전문용어 교정)
    │     결과를 correctedScript에 저장
    │     → /hearit/temp/scr/{uuid}.json 저장
    │     status: CORRECTING → GENERATING_META
    │
    ├─ 5. MetadataGenerationService: 메타데이터 생성
    │     Gemini API 호출 (제목, 요약 생성)
    │     suggestedTitle, suggestedSummary 저장
    │     status: GENERATING_META → COMPLETED
    │
    └─ [에러 발생 시]
          status: FAILED
          errorMessage 저장
          이미 생성된 S3 파일들 정리 (cleanup)
```

### 1.6 확인 후 Hearit 등록 플로우

```
[사용자]
    │
    │ POST /admin/api/ai/results/{processId}/confirm
    │ {
    │   categoryId: 1,
    │   keywordIds: [1, 2, 3],
    │   sources: [{sourceName: "...", sourceUrl: "..."}]
    │ }
    │
    ▼
[AiResultService.confirmAndCreateHearit()]
    │
    ├─ 1. S3 파일 이동 (temp → 정식 경로)
    │     /hearit/temp/org/{uuid}.mp3 → /hearit/audio/original/ORG_{uuid}.mp3
    │     /hearit/temp/shr/{uuid}.mp3 → /hearit/audio/short/SHR_{uuid}.mp3
    │     /hearit/temp/scr/{uuid}.json → /hearit/script/SCR_{uuid}.json
    │
    ├─ 2. Hearit 엔티티 생성
    │     (기존 AdminHearitService.addHearitMetaData 로직 재사용)
    │
    ├─ 3. AiProcessResult 상태 업데이트
    │     status = CONFIRMED
    │     confirmedHearit = hearit
    │
    └─ 4. temp 원본 파일 삭제
```

### 1.7 핵심 컴포넌트 구현

#### Mp3AudioProcessor (mp3spi 라이브러리 사용)

> **참고**:
> - mp3spi 라이브러리로 정확한 비트레이트/재생시간 추출
> - 바이트 기반 계산으로 자르기 (정확도 ±1초 이내)
> - MP3 파일만 지원 (FFmpeg 의존성 없음)

```java
@Component
@Slf4j
public class Mp3AudioProcessor {

    @Value("${ai.shorts.duration.seconds:60}")
    private int shortsDurationSeconds;

    /**
     * MP3 파일에서 앞부분 N초를 잘라 쇼츠 생성
     *
     * @param originalMp3 원본 MP3 바이트 배열
     * @param durationSeconds 자를 길이 (초)
     * @return 잘린 MP3 바이트 배열
     */
    public byte[] createShortClip(byte[] originalMp3, int durationSeconds) {
        try {
            Mp3Metadata metadata = extractMetadata(originalMp3);

            // 원본이 목표 시간보다 짧으면 전체 반환
            if (metadata.getDurationSeconds() <= durationSeconds) {
                return originalMp3;
            }

            // 바이트/초 계산
            int bytesPerSecond = (metadata.getBitrate() * 1000) / 8;
            int targetBytes = bytesPerSecond * durationSeconds;

            // 앞부분만 자르기
            return Arrays.copyOf(originalMp3, Math.min(targetBytes, originalMp3.length));

        } catch (Exception e) {
            log.error("Failed to create short clip", e);
            throw new AudioProcessingException("쇼츠 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * MP3 메타데이터 추출 (mp3spi 라이브러리 사용)
     */
    public Mp3Metadata extractMetadata(byte[] mp3Data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
             AudioInputStream ais = AudioSystem.getAudioInputStream(bais)) {

            AudioFormat format = ais.getFormat();

            // mp3spi가 제공하는 프로퍼티에서 비트레이트 추출
            Map<String, Object> properties = ((AudioFileFormat)
                AudioSystem.getAudioFileFormat(new ByteArrayInputStream(mp3Data)))
                .properties();

            int bitrate = (int) properties.getOrDefault("mp3.bitrate.nominal.bps", 128000) / 1000;
            long durationMicros = (long) properties.getOrDefault("duration", 0L);
            double durationSeconds = durationMicros / 1_000_000.0;

            // duration이 없으면 파일 크기로 계산
            if (durationSeconds == 0 && bitrate > 0) {
                durationSeconds = (mp3Data.length * 8.0) / (bitrate * 1000);
            }

            return new Mp3Metadata(bitrate, durationSeconds);

        } catch (UnsupportedAudioFileException | IOException e) {
            log.error("Failed to extract MP3 metadata", e);
            throw new AudioProcessingException("MP3 메타데이터 추출 실패", e);
        }
    }

    /**
     * MP3 파일 유효성 검증
     */
    public void validateMp3(byte[] data, String filename) {
        // 파일 크기 검증 (25MB 제한 - Groq Whisper API)
        if (data.length > 25 * 1024 * 1024) {
            throw new AudioProcessingException("파일 크기가 25MB를 초과합니다.");
        }

        // MP3 매직 바이트 검증 (ID3 태그 또는 프레임 싱크)
        if (!isValidMp3(data)) {
            throw new AudioProcessingException("유효하지 않은 MP3 파일입니다.");
        }

        // 확장자 검증
        if (!filename.toLowerCase().endsWith(".mp3")) {
            throw new AudioProcessingException("MP3 파일만 지원합니다.");
        }
    }

    private boolean isValidMp3(byte[] data) {
        if (data.length < 3) return false;

        // ID3v2 태그 체크
        if (data[0] == 'I' && data[1] == 'D' && data[2] == '3') {
            return true;
        }

        // MP3 프레임 싱크 워드 체크 (0xFF 0xFB, 0xFF 0xFA, 0xFF 0xF3, 0xFF 0xF2)
        if ((data[0] & 0xFF) == 0xFF && ((data[1] & 0xE0) == 0xE0)) {
            return true;
        }

        return false;
    }

    @Getter
    @AllArgsConstructor
    public static class Mp3Metadata {
        private int bitrate;           // kbps
        private double durationSeconds; // 재생 시간 (초)
    }
}
```

**의존성 추가 (build.gradle):**
```groovy
// admin/build.gradle
dependencies {
    implementation 'com.googlecode.soundlibs:mp3spi:1.9.5.4'
}
```

#### GroqWhisperClient (Groq API - 무료)

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class GroqWhisperClient {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_WHISPER_URL =
        "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final int MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 오디오 파일을 텍스트로 변환 (타임스탬프 포함)
     *
     * @return TranscriptionResult {
     *   duration: 120.5,
     *   segments: List<ScriptSegment>
     * }
     */
    public TranscriptionResult transcribe(byte[] audioData, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(audioData) {
            @Override
            public String getFilename() { return filename; }
        });
        body.add("model", "whisper-large-v3-turbo");
        body.add("response_format", "verbose_json");
        body.add("language", "ko");

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
            new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                WHISPER_URL, requestEntity, String.class);

            return parseWhisperResponse(response.getBody());

        } catch (Exception e) {
            log.error("Groq Whisper API call failed", e);
            throw new RuntimeException("STT 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Groq Whisper API 응답을 ScriptSegment 형식으로 변환
     *
     * Groq Whisper 응답 형식 (OpenAI 호환):
     * {
     *   "task": "transcribe",
     *   "language": "ko",
     *   "duration": 120.5,  // 전체 재생 시간 (초)
     *   "segments": [
     *     {
     *       "id": 0,
     *       "start": 0.0,    // 초 단위 float
     *       "end": 3.34,     // 초 단위 float
     *       "text": " 텍스트",
     *       ... (기타 필드들)
     *     }
     *   ]
     * }
     */
    private TranscriptionResult parseWhisperResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            double duration = root.get("duration").asDouble();
            JsonNode segmentsNode = root.get("segments");

            List<ScriptSegment> segments = new ArrayList<>();

            for (JsonNode seg : segmentsNode) {
                ScriptSegment scriptSegment = new ScriptSegment(
                    seg.get("id").asInt(),
                    (int)(seg.get("start").asDouble() * 1000),  // 초 → 밀리초
                    (int)(seg.get("end").asDouble() * 1000),    // 초 → 밀리초
                    seg.get("text").asText().trim()
                );
                segments.add(scriptSegment);
            }

            return new TranscriptionResult(duration, segments);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Groq Whisper response", e);
            throw new RuntimeException("Groq Whisper 응답 파싱 실패", e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class TranscriptionResult {
        private double duration;              // 전체 재생 시간 (초)
        private List<ScriptSegment> segments; // 변환된 대본
    }
}
```

#### GeminiClient

```java
@Component
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    /**
     * 텍스트 생성 (일반)
     */
    public String generateContent(String prompt) {
        // API 호출
    }

    /**
     * JSON 응답 형식으로 생성
     */
    public String generateContentWithJson(String prompt) {
        // response_mime_type: application/json 설정
    }
}
```

---

## Phase 2: 프론트엔드 구현 (Thymeleaf)

### 2.1 새로운 템플릿

```
templates/admin/
├── ai-upload.html         # AI 업로드 메인 페이지
├── ai-result.html         # AI 결과 검토/수정 페이지
└── fragments/
    └── ai-script-editor.html  # 대본 편집기 컴포넌트
```

### 2.2 ai-upload.html (AI 업로드 페이지)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>AI 콘텐츠 생성 - Hearit Admin</title>
    <!-- 기존 admin 스타일 재사용 -->
</head>
<body>
    <div class="container">
        <h1>AI 콘텐츠 생성</h1>

        <!-- CSRF 토큰 (JavaScript에서 사용) -->
        <meta name="_csrf" th:content="${_csrf.token}"/>
        <meta name="_csrf_header" th:content="${_csrf.headerName}"/>

        <!-- 1. 파일 업로드 영역 -->
        <section id="upload-section">
            <div class="upload-area" id="drop-zone">
                <input type="file" id="audio-file" accept=".mp3,audio/mpeg" />
                <p>NotebookLM에서 생성한 오디오 파일을 업로드하세요</p>
                <p class="hint">지원 형식: MP3 (최대 25MB)</p>
            </div>

            <div id="file-preview" style="display:none;">
                <span class="file-name"></span>
                <button class="btn-remove">제거</button>
            </div>

            <button id="start-process" class="btn-primary" disabled>
                AI 처리 시작
            </button>
        </section>

        <!-- 2. 처리 진행 상황 -->
        <section id="progress-section" style="display:none;">
            <div class="progress-container">
                <div class="progress-bar"></div>
                <div class="progress-text">0%</div>
            </div>

            <ul class="process-steps">
                <li data-step="UPLOADING">
                    <span class="icon">⏳</span> 파일 업로드
                </li>
                <li data-step="CONVERTING">
                    <span class="icon">⏳</span> 오디오 변환
                </li>
                <li data-step="TRANSCRIBING">
                    <span class="icon">⏳</span> 음성 인식 (STT)
                </li>
                <li data-step="CORRECTING">
                    <span class="icon">⏳</span> 대본 교정
                </li>
                <li data-step="GENERATING_META">
                    <span class="icon">⏳</span> 메타데이터 생성
                </li>
            </ul>

            <div id="error-message" class="alert-error" style="display:none;"></div>
        </section>
    </div>

    <script th:src="@{/js/ai-upload.js}"></script>
</body>
</html>
```

### 2.3 ai-result.html (결과 검토 페이지)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>AI 결과 검토 - Hearit Admin</title>
    <!-- CSRF 토큰 (JavaScript에서 사용) -->
    <meta name="_csrf" th:content="${_csrf.token}"/>
    <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
</head>
<body>
    <div class="container ai-result-container">
        <h1>AI 처리 결과 검토</h1>

        <!-- 1. 오디오 미리보기 -->
        <section class="audio-preview">
            <div class="audio-card">
                <h3>원본 오디오</h3>
                <audio id="original-audio" controls th:src="${result.orgAudioUrl}"></audio>
                <span class="play-time" th:text="${result.playTimeFormatted}"></span>
            </div>

            <div class="audio-card">
                <h3>쇼츠 (1분)</h3>
                <audio id="short-audio" controls th:src="${result.shrAudioUrl}"></audio>
            </div>
        </section>

        <!-- 2. 대본 편집기 -->
        <section class="script-editor">
            <div class="section-header">
                <h3>대본</h3>
                <p class="hint">클릭하면 해당 시점으로 이동합니다</p>
            </div>

            <div class="script-segments" id="script-container">
                <!-- JavaScript로 동적 렌더링 -->
            </div>

            <div class="script-actions">
                <button id="save-script" class="btn-secondary">대본 저장</button>
                <span id="script-save-status"></span>
            </div>
        </section>

        <!-- 3. 메타데이터 편집 -->
        <section class="metadata-form">
            <h3>콘텐츠 정보</h3>

            <div class="form-group">
                <label>제목 <span class="char-count">(0/35)</span></label>
                <input type="text" id="title" maxlength="35"
                       th:value="${result.suggestedTitle}" />
            </div>

            <div class="form-group">
                <label>요약 <span class="char-count">(0/250)</span></label>
                <textarea id="summary" maxlength="250" rows="4"
                          th:text="${result.suggestedSummary}"></textarea>
            </div>

            <div class="form-group">
                <label>카테고리</label>
                <select id="category">
                    <option th:each="cat : ${categories}"
                            th:value="${cat.id}"
                            th:text="${cat.name}"></option>
                </select>
            </div>

            <div class="form-group">
                <label>키워드</label>
                <div class="keyword-container">
                    <input type="text" id="keyword-search" placeholder="키워드 검색..." />
                    <div class="keyword-checkboxes" id="keyword-list">
                        <!-- JavaScript로 동적 렌더링 -->
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label>출처</label>
                <div id="sources-container">
                    <div class="source-row">
                        <input type="text" class="source-name" placeholder="출처명 (필수)" />
                        <input type="text" class="source-url" placeholder="URL (선택)" />
                        <button class="btn-remove-source">×</button>
                    </div>
                </div>
                <button id="add-source" class="btn-link">+ 출처 추가</button>
            </div>

            <button id="save-metadata" class="btn-secondary">메타데이터 저장</button>
        </section>

        <!-- 4. 최종 확인 -->
        <section class="confirm-section">
            <div class="confirm-buttons">
                <button id="confirm-register" class="btn-primary btn-large">
                    검토 완료 - Hearit 등록
                </button>
                <button id="cancel" class="btn-secondary">취소 (삭제)</button>
            </div>
        </section>
    </div>

    <script th:inline="javascript">
        const processId = [[${result.id}]];
        const scriptData = [[${result.correctedScript}]];
    </script>
    <script th:src="@{/js/ai-result.js}"></script>
</body>
</html>
```

### 2.4 JavaScript 구현

#### ai-upload.js

```javascript
class AiUploadManager {
    constructor() {
        this.fileInput = document.getElementById('audio-file');
        this.startButton = document.getElementById('start-process');
        this.progressSection = document.getElementById('progress-section');
        this.uploadSection = document.getElementById('upload-section');

        this.csrfToken = document.querySelector('meta[name="_csrf"]').content;
        this.csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        this.bindEvents();
    }

    bindEvents() {
        this.fileInput.addEventListener('change', (e) => this.onFileSelect(e));
        this.startButton.addEventListener('click', () => this.startProcess());
    }

    onFileSelect(e) {
        const file = e.target.files[0];
        if (file) {
            this.selectedFile = file;
            this.showFilePreview(file);
            this.startButton.disabled = false;
        }
    }

    async startProcess() {
        this.showProgress();

        const formData = new FormData();
        formData.append('audioFile', this.selectedFile);

        try {
            const response = await fetch('/admin/api/ai/process', {
                method: 'POST',
                body: formData,
                headers: { [this.csrfHeader]: this.csrfToken }
            });

            if (!response.ok) throw new Error('업로드 실패');

            const { processId } = await response.json();
            this.pollStatus(processId);

        } catch (error) {
            this.showError(error.message);
        }
    }

    async pollStatus(processId) {
        try {
            const response = await fetch(
                `/admin/api/ai/process/${processId}/status`
            );
            const { status, progress, errorMessage } = await response.json();

            this.updateProgress(status, progress);

            if (status === 'COMPLETED') {
                window.location.href = `/admin/ai-result/${processId}`;
            } else if (status === 'FAILED') {
                this.showError(errorMessage);
            } else {
                setTimeout(() => this.pollStatus(processId), 2000);
            }
        } catch (error) {
            this.showError('상태 조회 실패');
        }
    }

    updateProgress(status, progress) {
        const progressBar = document.querySelector('.progress-bar');
        const progressText = document.querySelector('.progress-text');

        progressBar.style.width = `${progress}%`;
        progressText.textContent = `${progress}%`;

        // 단계별 아이콘 업데이트
        const steps = ['UPLOADING', 'CONVERTING', 'TRANSCRIBING',
                       'CORRECTING', 'GENERATING_META'];
        const currentIndex = steps.indexOf(status);

        document.querySelectorAll('.process-steps li').forEach((li, index) => {
            const icon = li.querySelector('.icon');
            if (index < currentIndex) {
                icon.textContent = '✅';
            } else if (index === currentIndex) {
                icon.textContent = '🔄';
            }
        });
    }

    showProgress() {
        this.uploadSection.style.display = 'none';
        this.progressSection.style.display = 'block';
    }

    showError(message) {
        const errorDiv = document.getElementById('error-message');
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new AiUploadManager();
});
```

#### ai-result.js

```javascript
class AiResultManager {
    constructor(processId, scriptData) {
        this.processId = processId;
        this.scriptData = JSON.parse(scriptData);

        this.audioPlayer = document.getElementById('original-audio');
        this.scriptContainer = document.getElementById('script-container');

        this.csrfToken = document.querySelector('meta[name="_csrf"]').content;
        this.csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        this.init();
    }

    init() {
        this.renderScript();
        this.bindEvents();
        this.loadKeywords();
    }

    renderScript() {
        this.scriptContainer.innerHTML = this.scriptData.map((seg, index) => `
            <div class="segment" data-index="${index}"
                 data-start="${seg.start}" data-end="${seg.end}">
                <span class="timestamp">${this.formatTime(seg.start)}</span>
                <textarea class="segment-text" rows="2">${seg.text}</textarea>
            </div>
        `).join('');
    }

    formatTime(ms) {
        const seconds = Math.floor(ms / 1000);
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    bindEvents() {
        // 세그먼트 클릭 시 해당 시점 재생
        this.scriptContainer.addEventListener('click', (e) => {
            const segment = e.target.closest('.segment');
            if (segment) {
                const startMs = parseInt(segment.dataset.start);
                this.audioPlayer.currentTime = startMs / 1000;
                this.audioPlayer.play();
            }
        });

        // 대본 저장
        document.getElementById('save-script').addEventListener('click',
            () => this.saveScript());

        // 메타데이터 저장
        document.getElementById('save-metadata').addEventListener('click',
            () => this.saveMetadata());

        // 확인 및 등록
        document.getElementById('confirm-register').addEventListener('click',
            () => this.confirmAndRegister());

        // 취소
        document.getElementById('cancel').addEventListener('click',
            () => this.cancel());

        // 출처 추가
        document.getElementById('add-source').addEventListener('click',
            () => this.addSourceRow());

        // 글자수 카운터
        this.setupCharCounters();
    }

    collectScript() {
        return Array.from(this.scriptContainer.querySelectorAll('.segment'))
            .map(seg => ({
                id: parseInt(seg.dataset.index),
                start: parseInt(seg.dataset.start),
                end: parseInt(seg.dataset.end),
                text: seg.querySelector('.segment-text').value.trim()
            }));
    }

    async saveScript() {
        const segments = this.collectScript();

        try {
            await fetch(`/admin/api/ai/results/${this.processId}/script`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [this.csrfHeader]: this.csrfToken
                },
                body: JSON.stringify({ segments })
            });

            document.getElementById('script-save-status').textContent = '저장됨 ✓';
        } catch (error) {
            alert('대본 저장 실패');
        }
    }

    collectMetadata() {
        return {
            title: document.getElementById('title').value.trim(),
            summary: document.getElementById('summary').value.trim()
        };
    }

    async saveMetadata() {
        const metadata = this.collectMetadata();

        try {
            await fetch(`/admin/api/ai/results/${this.processId}/metadata`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [this.csrfHeader]: this.csrfToken
                },
                body: JSON.stringify(metadata)
            });

            alert('메타데이터가 저장되었습니다.');
        } catch (error) {
            alert('메타데이터 저장 실패');
        }
    }

    async confirmAndRegister() {
        // 확인 데이터 수집 (카테고리, 키워드, 출처만 - 대본/메타데이터는 DB에 저장된 값 사용)
        const confirmData = {
            categoryId: parseInt(document.getElementById('category').value),
            keywordIds: this.getSelectedKeywordIds(),
            sources: this.collectSources(),
            // 최종 대본/메타데이터도 함께 전송 (마지막 수정 반영)
            finalTitle: document.getElementById('title').value.trim(),
            finalSummary: document.getElementById('summary').value.trim(),
            finalScript: this.collectScript()
        };

        // 유효성 검사
        if (!confirmData.finalTitle || confirmData.finalTitle.length > 35) {
            alert('제목은 1-35자 이내로 입력해주세요.');
            return;
        }
        if (!confirmData.finalSummary || confirmData.finalSummary.length > 250) {
            alert('요약은 1-250자 이내로 입력해주세요.');
            return;
        }
        if (!confirmData.categoryId) {
            alert('카테고리를 선택해주세요.');
            return;
        }
        if (confirmData.sources.length === 0 || !confirmData.sources[0].sourceName) {
            alert('출처를 최소 1개 이상 입력해주세요.');
            return;
        }

        if (!confirm('이 콘텐츠를 Hearit으로 등록하시겠습니까?')) {
            return;
        }

        try {
            const response = await fetch(
                `/admin/api/ai/results/${this.processId}/confirm`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [this.csrfHeader]: this.csrfToken
                },
                body: JSON.stringify(confirmData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || '등록 실패');
            }

            alert('Hearit이 성공적으로 등록되었습니다!');
            window.location.href = '/admin/hearit-list';

        } catch (error) {
            alert('Hearit 등록 실패: ' + error.message);
        }
    }

    async cancel() {
        if (!confirm('이 AI 처리 결과를 삭제하시겠습니까? 복구할 수 없습니다.')) {
            return;
        }

        try {
            await fetch(`/admin/api/ai/results/${this.processId}`, {
                method: 'DELETE',
                headers: { [this.csrfHeader]: this.csrfToken }
            });

            window.location.href = '/admin/ai-upload';
        } catch (error) {
            alert('삭제 실패');
        }
    }

    // ... 추가 헬퍼 메서드들
}

document.addEventListener('DOMContentLoaded', () => {
    new AiResultManager(processId, JSON.stringify(scriptData));
});
```

---

## Phase 3: 설정 및 환경 구성

### 3.1 새로운 환경 변수

```properties
# application.properties

# Groq API (무료 Whisper STT)
groq.api.key=${GROQ_API_KEY}
groq.whisper.model=whisper-large-v3-turbo

# Google Gemini API (대본 교정 + 메타데이터 생성)
gemini.api.key=${GEMINI_API_KEY}
gemini.model=gemini-2.5-flash

# AI 처리 설정
ai.result.expiration.hours=24
ai.shorts.duration.seconds=60

# S3 임시 경로
aws.s3.temp.prefix=hearit/temp/
```

### 3.2 Spring Async 및 HTTP Client 설정

```java
@Configuration
@EnableAsync
public class AiConfig {

    @Bean
    public Executor aiProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ai-process-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public RestTemplate aiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 타임아웃 설정 (Groq Whisper API는 긴 오디오 처리 시 시간이 걸림)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));
        factory.setReadTimeout(Duration.ofMinutes(5));
        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }
}
```

### 3.3 진행률 계산 유틸리티

```java
public class ProcessStatusUtil {

    private static final Map<ProcessStatus, Integer> PROGRESS_MAP = Map.of(
        ProcessStatus.PENDING, 0,
        ProcessStatus.UPLOADING, 10,
        ProcessStatus.CONVERTING, 25,
        ProcessStatus.TRANSCRIBING, 50,
        ProcessStatus.CORRECTING, 70,
        ProcessStatus.GENERATING_META, 85,
        ProcessStatus.COMPLETED, 100,
        ProcessStatus.FAILED, 0,
        ProcessStatus.CONFIRMED, 100
    );

    public static int getProgress(ProcessStatus status) {
        return PROGRESS_MAP.getOrDefault(status, 0);
    }

    public static String getStatusMessage(ProcessStatus status) {
        return switch (status) {
            case PENDING -> "대기 중...";
            case UPLOADING -> "파일 업로드 중...";
            case CONVERTING -> "오디오 처리 중...";
            case TRANSCRIBING -> "음성 인식 중... (1-2분 소요)";
            case CORRECTING -> "대본 교정 중...";
            case GENERATING_META -> "메타데이터 생성 중...";
            case COMPLETED -> "처리 완료!";
            case FAILED -> "처리 실패";
            case CONFIRMED -> "등록 완료";
        };
    }
}
```

### 3.4 스케줄러 (임시 파일 정리)

```java
@Component
@RequiredArgsConstructor
public class AiResultCleanupScheduler {

    private final AiProcessResultRepository repository;
    private final FileStorage fileStorage;

    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시
    public void cleanupExpiredResults() {
        LocalDateTime now = LocalDateTime.now();

        List<AiProcessResult> expired = repository
            .findByStatusNotAndExpiresAtBefore(ProcessStatus.CONFIRMED, now);

        for (AiProcessResult result : expired) {
            // S3 temp 파일 삭제
            deleteIfExists(result.getOriginalFileKey());
            deleteIfExists(result.getGeneratedOrgKey());
            deleteIfExists(result.getGeneratedShrKey());
            deleteIfExists(result.getGeneratedScrKey());

            // DB 레코드 삭제
            repository.delete(result);
        }
    }

    private void deleteIfExists(String key) {
        if (key != null && !key.isBlank()) {
            try {
                fileStorage.deleteFile(key);
            } catch (Exception e) {
                // 로깅
            }
        }
    }
}
```

---

## Phase 4: DB 마이그레이션

### 4.1 Flyway 스크립트

```sql
-- V5__add_ai_process_result.sql

CREATE TABLE ai_process_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(30) NOT NULL,

    -- 원본 파일 정보
    original_file_name VARCHAR(255),
    original_file_key VARCHAR(500),

    -- 생성된 파일들 (S3 temp 경로)
    generated_org_key VARCHAR(500),
    generated_shr_key VARCHAR(500),
    generated_scr_key VARCHAR(500),

    -- AI 생성 결과
    raw_transcript JSON,
    corrected_script JSON,
    suggested_title VARCHAR(35),
    suggested_summary TEXT,

    -- 수정된 값 (사용자 입력)
    edited_script JSON,
    edited_title VARCHAR(35),
    edited_summary TEXT,

    -- 재생 시간
    play_time INT,

    -- 에러 정보
    error_message TEXT,

    -- 타임스탬프
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    expires_at DATETIME,

    -- 확인 후 생성된 Hearit
    confirmed_hearit_id BIGINT,

    -- 외래 키
    CONSTRAINT fk_ai_result_hearit
        FOREIGN KEY (confirmed_hearit_id) REFERENCES hearit(id) ON DELETE SET NULL,

    -- 인덱스
    INDEX idx_ai_result_status (status),
    INDEX idx_ai_result_expires_at (expires_at),
    INDEX idx_ai_result_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Phase 5: 테스트 계획

### 5.1 단위 테스트

| 테스트 클래스 | 테스트 항목 |
|--------------|------------|
| `Mp3AudioProcessorTest` | 메타데이터 추출, 쇼츠 생성, MP3 검증 |
| `GroqWhisperClientTest` | API 호출 모킹, 응답 파싱, 에러 처리 |
| `GeminiClientTest` | API 호출 모킹, JSON 응답 파싱 |
| `TranscriptionServiceTest` | STT 결과 변환 로직 |
| `ScriptCorrectionServiceTest` | 프롬프트 생성, 결과 파싱 |
| `MetadataGenerationServiceTest` | 프롬프트 생성, 결과 파싱 |
| `AiProcessingServiceTest` | 전체 플로우 오케스트레이션 |
| `ProcessStatusUtilTest` | 진행률 계산, 상태 메시지 |

### 5.2 통합 테스트

| 테스트 클래스 | 테스트 항목 |
|--------------|------------|
| `AiProcessControllerTest` | 엔드포인트 통합 테스트 |
| `AiResultControllerTest` | 결과 조회/수정/확인 테스트 |
| `S3IntegrationTest` | 파일 업로드/다운로드/삭제 (LocalStack) |

### 5.3 E2E 테스트 시나리오

1. **정상 플로우**
   - 오디오 업로드 → AI 처리 완료 → 대본 수정 → 메타데이터 수정 → 확인 → Hearit 등록

2. **에러 케이스**
   - STT API 실패 → status: FAILED, errorMessage 확인
   - LLM API 실패 → status: FAILED, errorMessage 확인
   - 파일 변환 실패 → status: FAILED, errorMessage 확인

3. **엣지 케이스**
   - 매우 긴 오디오 (30분+) → 처리 시간 확인
   - 1분 미만 오디오 → 쇼츠 = 원본 확인
   - 25MB 초과 파일 → "파일 크기가 25MB를 초과합니다" 에러
   - MP3가 아닌 파일 → "MP3 파일만 지원합니다" 에러
   - 손상된 MP3 파일 → "유효하지 않은 MP3 파일입니다" 에러

---

## 구현 순서

### Step 1: 기반 설정
- [ ] 환경 변수 추가 (API 키)
- [ ] AiConfig 클래스 생성 (Async + RestTemplate)
- [ ] mp3spi 의존성 추가 (admin/build.gradle)

### Step 2: DB + 엔티티
- [ ] Flyway 마이그레이션 스크립트 작성
- [ ] ScriptSegment DTO 생성
- [ ] ScriptSegmentListConverter (AttributeConverter) 생성
- [ ] AiProcessResult 엔티티 생성 (@EntityListeners 포함)
- [ ] ProcessStatus enum 생성
- [ ] ProcessStatusUtil 유틸리티 클래스 생성
- [ ] AiProcessResultRepository 생성
- [ ] AudioProcessingException 예외 클래스 생성

### Step 3: 인프라 레이어
- [ ] Mp3AudioProcessor (mp3spi 기반) 구현
- [ ] GroqWhisperClient (Groq API - 무료) 구현
- [ ] GeminiClient (Gemini API) 구현
- [ ] FileStorage에 moveFile(), deleteFile() 메서드 추가

### Step 4: 서비스 레이어
- [ ] TranscriptionService 구현
- [ ] ScriptCorrectionService 구현
- [ ] MetadataGenerationService 구현
- [ ] AiProcessingService 구현 (오케스트레이션)
- [ ] AiResultService 구현 (결과 조회/수정/확인)

### Step 5: 컨트롤러 레이어
- [ ] AiProcessController 구현 (API)
- [ ] AiResultController 구현 (API)
- [ ] AiViewController 구현 (페이지 렌더링 + 카테고리/키워드 데이터 전달)

### Step 6: 프론트엔드
- [ ] ai-upload.html 템플릿 작성 (CSRF 메타 태그 포함)
- [ ] ai-result.html 템플릿 작성 (CSRF 메타 태그 포함)
- [ ] ai-upload.js 구현
- [ ] ai-result.js 구현
- [ ] CSS 스타일링 (기존 admin 스타일 재사용)

### Step 7: 테스트
- [x] 단위 테스트 작성
- [x] 통합 테스트 작성
- [ ] E2E 테스트 수행

### Step 8: 운영
- [ ] 임시 파일 정리 스케줄러 구현
- [ ] 모니터링/로깅 설정
- [ ] API 비용 모니터링 설정

---

## 주의사항

1. **API 비용**: Gemini API 호출 비용 모니터링 필요 (Groq Whisper는 무료)
   - Groq Whisper: **무료** (rate limit 존재)
   - Gemini: 토큰 기반 과금
2. **파일 제한**:
   - **형식**: MP3만 지원 (NotebookLM 출력을 MP3로 변환 후 업로드 필요)
   - **크기**: 최대 25MB (Groq Whisper API 제한)
   - Spring의 `multipart.max-file-size`도 25MB 이상으로 설정 필요
3. **처리 시간**: 긴 오디오(30분+)의 경우 처리 시간이 수 분 소요될 수 있음
4. **임시 파일**: 만료된 임시 파일 정리 로직 필수 (24시간 후 자동 삭제)
5. **보안**: Admin 권한 체크, 파일 업로드 검증 필수 (CSRF 토큰 포함)
6. **JPA Auditing**: `@EnableJpaAuditing` 설정이 이미 존재하는지 확인 필요

---

## 구현 노트

구현 과정에서 발생한 이슈와 해결 방안을 기록합니다.

### 1. 트랜잭션 분리 (AiProcessStatusUpdater)

**문제**: `AiProcessingService.executeProcessing()`은 `@Async`로 비동기 실행되며 여러 단계를 거칩니다. 각 단계에서 상태를 업데이트하지만, 전체 메서드가 하나의 트랜잭션으로 묶이면 처리가 완료될 때까지 DB에 상태가 반영되지 않습니다.

**영향**: 프론트엔드에서 폴링으로 진행 상태를 조회할 때, 실제로는 STT 처리 중인데 DB에는 여전히 PENDING 상태로 조회됨.

**해결**: `AiProcessStatusUpdater` 클래스를 분리하고, 모든 상태 업데이트 메서드에 `@Transactional(propagation = Propagation.REQUIRES_NEW)`를 적용하여 각 상태 변경이 즉시 커밋되도록 함.

### 2. S3 파일 이동 로직 변경

**문제**: S3에는 파일 이동(move) API가 없음. 원래 계획은 `moveFile()` 메서드로 temp → 정식 경로 이동이었음.

**해결**: `copyFile()` 메서드로 복사 후, 원본 파일을 별도로 삭제하는 방식으로 구현. temp 파일 정리는 스케줄러에서 처리.

### 3. Gemini API Rate Limiting

**문제**: Gemini 무료 tier는 15 RPM(분당 요청) 제한이 있어, 연속 요청 시 429 에러 발생.

**해결**:
- `Semaphore(1)`로 동시 요청 1개로 제한
- 요청 간 최소 4초 간격 유지
- 429 에러 시 Exponential Backoff로 최대 3회 재시도 (5초 → 10초 → 20초)

### 4. 출처 관리 리스너 분리

**문제**: Hearit 생성 시 Source 엔티티도 함께 저장해야 하는데, 기존 `AdminHearitService`에서 Hearit 저장과 Source 저장이 결합되어 있어 AI 결과 confirm 시 재사용이 어려움.

**해결**: `SourceManageListener`를 분리하여 Hearit 엔티티의 `@PostPersist` 이벤트로 Source를 자동 저장하도록 리팩토링. AI confirm과 기존 수동 등록 모두 동일한 로직 사용.

### 5. AI 결과 페이지 데이터 전달

**문제**: AI 결과 검토 페이지에서 카테고리/키워드 목록, 교정된 대본 등의 데이터를 프론트엔드에 전달해야 함.

**해결**: Thymeleaf 템플릿에서 `th:inline="javascript"`로 JSON 데이터를 JavaScript 변수로 주입. `ScriptSegment` 리스트는 Jackson ObjectMapper로 직렬화.

---

## 테스트 현황

### 단위 테스트 (완료)

| 테스트 클래스 | 테스트 항목 |
|--------------|------------|
| `ProcessStatusUtilTest` | 진행률 계산, 상태 메시지 |
| `Mp3AudioProcessorTest` | 메타데이터 추출, 쇼츠 생성, MP3 검증 |
| `GroqWhisperClientTest` | API 호출 모킹, 응답 파싱, 에러 처리 |
| `GeminiClientTest` | API 호출 모킹, JSON 응답 파싱, Rate Limiting |
| `TranscriptionServiceTest` | STT 호출, 세그먼트 병합 |
| `ScriptCorrectionServiceTest` | 프롬프트 생성, 결과 파싱, 폴백 처리 |
| `MetadataGenerationServiceTest` | 프롬프트 생성, 결과 파싱 |
| `AiProcessingServiceTest` | 전체 플로우 오케스트레이션, 실패 처리 |

### 통합 테스트 (완료)

| 테스트 클래스 | 테스트 항목 |
|--------------|------------|
| `AiProcessControllerTest` | 처리 시작, 상태 조회 API |
| `AiResultControllerTest` | 결과 조회, 대본/메타데이터 수정, confirm, 삭제 API |

---

## 추후 개선사항 (현재 구현 범위 외)

1. ~~**API 재시도 전략**: Groq Whisper/Gemini API 실패 시 자동 재시도 (Spring Retry)~~ → **구현 완료** (GeminiClient에 Exponential Backoff 적용)
2. **ThreadPool 최적화**: 동시 처리 요청이 많아질 경우 ThreadPool 크기 조정
3. **처리 상태 알림**: 완료 시 이메일/슬랙 알림
4. **중복 콘텐츠 방지**: 파일 해시(SHA-256)로 중복 업로드 체크
5. **만료 시간 연장**: 사용자가 페이지를 열면 만료 시간 자동 연장
6. **쇼츠 자르기 개선**: 60초에 가장 가까운 문장 경계에서 자르기
7. **프론트엔드 폴링 개선**: WebSocket 또는 Server-Sent Events로 실시간 상태 업데이트
8. **롤백 전략**: 중간 단계 실패 시 이전 S3 파일들 자동 정리

---

## 기존 코드와의 관계

| 기존 코드 | 재사용 방식 |
|----------|------------|
| `AdminHearitService.addHearitMetaData()` | 확인 후 Hearit 생성 시 호출 |
| `FileStorage` | S3 업로드/다운로드/삭제 재사용, moveFile() 메서드 추가 필요 |
| `AdminSecurityConfig` | AI 엔드포인트도 동일한 보안 적용 |
| `hearit-create.html` CSS/JS | 스타일, 카테고리/키워드 로직 재사용 |
| `Hearit`, `FileUrls`, `Source` | 도메인 엔티티 그대로 사용 |
| `CategoryRepository`, `KeywordRepository` | AI 결과 페이지에서 카테고리/키워드 목록 조회 |

### FileStorage 확장 (S3 파일 이동)

```java
// FileStorage 인터페이스에 추가
public interface FileStorage {
    // 기존 메서드들...

    /**
     * S3 파일 이동 (copy + delete)
     * @param sourceKey 원본 경로
     * @param destinationKey 대상 경로
     */
    void moveFile(String sourceKey, String destinationKey);

    /**
     * S3 파일 삭제
     */
    void deleteFile(String key);
}

// S3FileStorage 구현
@Override
public void moveFile(String sourceKey, String destinationKey) {
    // 1. 복사
    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
        .sourceBucket(bucket)
        .sourceKey(sourceKey)
        .destinationBucket(bucket)
        .destinationKey(destinationKey)
        .build();
    s3Client.copyObject(copyRequest);

    // 2. 원본 삭제
    deleteFile(sourceKey);
}

@Override
public void deleteFile(String key) {
    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    s3Client.deleteObject(deleteRequest);
}
```

### AiResultViewController (카테고리/키워드 데이터 전달)

```java
@Controller
@RequiredArgsConstructor
public class AiResultViewController {

    private final AiProcessResultRepository resultRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordRepository keywordRepository;

    @GetMapping("/admin/ai-result/{processId}")
    public String aiResultPage(@PathVariable Long processId, Model model) {
        AiProcessResult result = resultRepository.findById(processId)
            .orElseThrow(() -> new EntityNotFoundException("AI 결과를 찾을 수 없습니다."));

        if (result.getStatus() != ProcessStatus.COMPLETED) {
            return "redirect:/admin/ai-upload";
        }

        model.addAttribute("result", result);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keywords", keywordRepository.findAll());

        return "admin/ai-result";
    }
}
```
