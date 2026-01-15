# AI 백오피스 기능 구현 계획

## 개요

NotebookLM으로 생성한 오디오 파일을 백오피스에서 AI로 처리하여 콘텐츠를 등록하는 기능입니다.

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
| STT | 외부 API (OpenAI Whisper API) | 서버 GPU 불필요, 안정적 |
| LLM | Google Gemini API | 기존 스크립트와 동일 |
| 프론트엔드 | Thymeleaf 확장 | 기존 관리자 페이지와 일관성 |
| 임시 저장 | S3 `/hearit/temp/` | 기존 S3 인프라 활용 |
| 오디오 처리 | Java 라이브러리 (mp3spi) | FFmpeg 설치 불필요, MP3 입력 전제 |

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
│   │   ├── openai/
│   │   │   ├── WhisperClient.java        # OpenAI Whisper API 클라이언트
│   │   │   └── WhisperConfig.java
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
| `POST` | `/api/v1/admin/ai/process` | 원본 오디오 업로드 및 AI 처리 시작 |
| `GET` | `/api/v1/admin/ai/process/{processId}/status` | 처리 상태 조회 (Polling용) |
| `GET` | `/api/v1/admin/ai/results/{processId}` | 완료된 AI 처리 결과 조회 |
| `PUT` | `/api/v1/admin/ai/results/{processId}/script` | 대본 수정 |
| `PUT` | `/api/v1/admin/ai/results/{processId}/metadata` | 메타데이터(제목, 요약) 수정 |
| `POST` | `/api/v1/admin/ai/results/{processId}/confirm` | 검토 완료 후 Hearit 등록 |
| `DELETE` | `/api/v1/admin/ai/results/{processId}` | AI 결과 삭제 (취소) |

### 1.3 새로운 엔티티: AiProcessResult

```java
@Entity
@Table(name = "ai_process_result")
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

    // AI 생성 결과
    @Column(columnDefinition = "JSON")
    private String rawTranscript;     // STT 원본 결과 JSON

    @Column(columnDefinition = "JSON")
    private String correctedScript;   // 교정된 대본 JSON

    private String suggestedTitle;    // AI 제안 제목

    @Column(columnDefinition = "TEXT")
    private String suggestedSummary;  // AI 제안 요약

    // 수정된 값 (사용자 입력)
    @Column(columnDefinition = "JSON")
    private String editedScript;      // 사용자 수정 대본

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
    │ POST /api/v1/admin/ai/process (multipart/form-data: audioFile)
    ▼
[AiProcessController]
    │
    │ 1. AiProcessResult 생성 (status: PENDING)
    │ 2. 비동기 처리 시작 (@Async)
    │
    ▼
[AiProcessingService] (비동기)
    │
    ├─ 1. 원본 파일 S3 업로드
    │     → /hearit/temp/original/{uuid}.m4a
    │     status: UPLOADING → CONVERTING
    │
    ├─ 2. AudioProcessor: MP3 변환 + 1분 쇼츠 생성
    │     FFmpeg 실행
    │     → /hearit/temp/org/{uuid}.mp3
    │     → /hearit/temp/shr/{uuid}.mp3
    │     status: CONVERTING → TRANSCRIBING
    │
    ├─ 3. TranscriptionService: STT 처리
    │     OpenAI Whisper API 호출
    │     결과를 rawTranscript에 저장
    │     playTime 계산
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
```

### 1.6 확인 후 Hearit 등록 플로우

```
[사용자]
    │
    │ POST /api/v1/admin/ai/results/{processId}/confirm
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

#### Mp3AudioProcessor (Java 라이브러리)

> **참고**: NotebookLM에서 MP3로 제공되므로 FFmpeg 없이 Java 라이브러리로 처리합니다.
> 재생 시간은 OpenAI Whisper API 응답의 `duration` 필드에서 추출합니다.

```java
@Component
public class Mp3AudioProcessor {

    @Value("${ai.shorts.duration.seconds:60}")
    private int shortsDurationSeconds;

    /**
     * MP3 파일에서 앞부분 N초를 잘라 쇼츠 생성
     *
     * 방법 1: mp3spi 라이브러리 사용
     * 방법 2: 바이트 단위로 MP3 프레임 파싱 (더 간단)
     */
    public byte[] createShortClip(byte[] originalMp3, int durationSeconds) {
        // MP3 프레임 구조를 분석하여 지정된 시간만큼 자르기
        // 또는 mp3spi + javax.sound.sampled 활용

        // 간단한 구현: MP3 비트레이트 기반 계산
        // 예: 256kbps = 32KB/초 → 60초 = 약 1.92MB

        // 정밀한 구현이 필요하면 mp3spi 라이브러리 사용:
        // - Maven: com.googlecode.soundlibs:mp3spi:1.9.5.4
        // - AudioInputStream으로 디코딩 후 원하는 길이만큼 읽기
    }

    /**
     * MP3 파일의 재생 시간 조회 (초 단위)
     *
     * 참고: Whisper API 응답에 duration이 포함되어 있으므로
     * 이 메서드는 백업용으로만 사용
     */
    public int getDurationSeconds(byte[] mp3Data) {
        // MP3 헤더에서 비트레이트 추출 후 계산
        // 또는 mp3spi로 AudioInputStream 열어서 프레임 수 계산
    }
}
```

**의존성 추가 (build.gradle):**
```groovy
// MP3 처리용 (필요시)
implementation 'com.googlecode.soundlibs:mp3spi:1.9.5.4'
implementation 'com.googlecode.soundlibs:tritonus-share:0.3.7.4'
```

**대안 - 더 간단한 방식:**
- Whisper API가 `duration` 값을 반환하므로 재생 시간 조회는 불필요
- 쇼츠 자르기: MP3 비트레이트 기반으로 바이트 단위 계산 (정확도 ±1초)
- 정밀한 자르기가 필요하면 클라이언트(브라우저)에서 Web Audio API로 처리 가능
```

#### WhisperClient (OpenAI API)

```java
@Component
public class WhisperClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String WHISPER_URL =
        "https://api.openai.com/v1/audio/transcriptions";

    /**
     * 오디오 파일을 텍스트로 변환 (타임스탬프 포함)
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
        body.add("model", "whisper-1");
        body.add("response_format", "verbose_json");
        body.add("language", "ko");

        // API 호출 및 응답 파싱
        // ...
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

        <!-- 1. 파일 업로드 영역 -->
        <section id="upload-section">
            <div class="upload-area" id="drop-zone">
                <input type="file" id="audio-file" accept=".m4a,.mp3,.wav" />
                <p>NotebookLM에서 생성한 오디오 파일을 업로드하세요</p>
                <p class="hint">지원 형식: M4A, MP3, WAV</p>
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
            const response = await fetch('/api/v1/admin/ai/process', {
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
                `/api/v1/admin/ai/process/${processId}/status`
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
            await fetch(`/api/v1/admin/ai/results/${this.processId}/script`, {
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
            await fetch(`/api/v1/admin/ai/results/${this.processId}/metadata`, {
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
        const confirmData = {
            title: document.getElementById('title').value.trim(),
            summary: document.getElementById('summary').value.trim(),
            categoryId: parseInt(document.getElementById('category').value),
            keywordIds: this.getSelectedKeywordIds(),
            sources: this.collectSources(),
            script: this.collectScript()
        };

        // 유효성 검사
        if (!confirmData.title || confirmData.title.length > 35) {
            alert('제목은 1-35자 이내로 입력해주세요.');
            return;
        }
        if (!confirmData.summary || confirmData.summary.length > 250) {
            alert('요약은 1-250자 이내로 입력해주세요.');
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
                `/api/v1/admin/ai/results/${this.processId}/confirm`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [this.csrfHeader]: this.csrfToken
                },
                body: JSON.stringify(confirmData)
            });

            if (!response.ok) throw new Error('등록 실패');

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
            await fetch(`/api/v1/admin/ai/results/${this.processId}`, {
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

# OpenAI API (Whisper STT)
openai.api.key=${OPENAI_API_KEY}
openai.whisper.model=whisper-1

# Google Gemini API (대본 교정 + 메타데이터 생성)
gemini.api.key=${GEMINI_API_KEY}
gemini.model=gemini-2.5-flash

# AI 처리 설정
ai.result.expiration.hours=24
ai.shorts.duration.seconds=60

# S3 임시 경로
aws.s3.temp.prefix=hearit/temp/
```

### 3.2 Spring Async 설정

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor aiProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ai-process-");
        executor.initialize();
        return executor;
    }
}
```

### 3.3 스케줄러 (임시 파일 정리)

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
| `Mp3AudioProcessorTest` | 쇼츠 생성 (1분 자르기) |
| `WhisperClientTest` | API 호출 모킹, 응답 파싱 |
| `GeminiClientTest` | API 호출 모킹, JSON 응답 파싱 |
| `TranscriptionServiceTest` | STT 결과 변환 로직 |
| `ScriptCorrectionServiceTest` | 프롬프트 생성, 결과 파싱 |
| `MetadataGenerationServiceTest` | 프롬프트 생성, 결과 파싱 |
| `AiProcessingServiceTest` | 전체 플로우 오케스트레이션 |

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
   - 지원하지 않는 포맷 → 적절한 에러 메시지

---

## 구현 순서

### Step 1: 기반 설정
- [ ] 환경 변수 추가 (API 키)
- [ ] Spring Async 설정

### Step 2: DB + 엔티티
- [ ] Flyway 마이그레이션 스크립트 작성
- [ ] AiProcessResult 엔티티 생성
- [ ] ProcessStatus enum 생성
- [ ] AiProcessResultRepository 생성

### Step 3: 인프라 레이어
- [ ] Mp3AudioProcessor (쇼츠 자르기) 구현
- [ ] WhisperClient (OpenAI API) 구현
- [ ] GeminiClient (Gemini API) 구현
- [ ] FileStorage temp 경로 지원 추가

### Step 4: 서비스 레이어
- [ ] TranscriptionService 구현
- [ ] ScriptCorrectionService 구현
- [ ] MetadataGenerationService 구현
- [ ] AiProcessingService 구현 (오케스트레이션)
- [ ] AiResultService 구현 (결과 조회/수정/확인)

### Step 5: 컨트롤러 레이어
- [ ] AiProcessController 구현
- [ ] AiResultController 구현
- [ ] AdminViewController에 AI 페이지 라우팅 추가

### Step 6: 프론트엔드
- [ ] ai-upload.html 템플릿 작성
- [ ] ai-result.html 템플릿 작성
- [ ] ai-upload.js 구현
- [ ] ai-result.js 구현
- [ ] CSS 스타일링

### Step 7: 테스트
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] E2E 테스트 수행

### Step 8: 운영
- [ ] 임시 파일 정리 스케줄러 구현
- [ ] 모니터링/로깅 설정
- [ ] API 비용 모니터링 설정

---

## 주의사항

1. **API 비용**: OpenAI Whisper, Gemini API 호출 비용 모니터링 필요
2. **처리 시간**: 긴 오디오의 경우 처리 시간이 수 분 소요될 수 있음
3. **임시 파일**: 만료된 임시 파일 정리 로직 필수
4. **동시성**: 여러 AI 처리 요청이 동시에 들어올 경우 리소스 관리 필요
5. **보안**: Admin 권한 체크, 파일 업로드 검증 필수

---

## 기존 코드와의 관계

| 기존 코드 | 재사용 방식 |
|----------|------------|
| `AdminHearitService.addHearitMetaData()` | 확인 후 Hearit 생성 시 호출 |
| `FileStorage` | S3 업로드/다운로드/삭제 재사용 |
| `AdminSecurityConfig` | AI 엔드포인트도 동일한 보안 적용 |
| `hearit-create.html` CSS/JS | 스타일, 카테고리/키워드 로직 재사용 |
| `Hearit`, `FileUrls`, `Source` | 도메인 엔티티 그대로 사용 |
