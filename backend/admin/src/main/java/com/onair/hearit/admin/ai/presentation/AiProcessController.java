package com.onair.hearit.admin.ai.presentation;

import com.onair.hearit.admin.ai.application.AiProcessingService;
import com.onair.hearit.admin.ai.application.AiResultService;
import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.dto.response.AiProcessStatusResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 처리 API 컨트롤러
 * 오디오 업로드 및 처리 상태 조회
 */
@RestController
@RequestMapping("/admin/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiProcessController {

    private final AiProcessingService processingService;
    private final AiResultService resultService;

    /**
     * AI 처리 시작
     * POST /admin/api/ai/process
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Long>> startProcess(
            @RequestParam("audioFile") MultipartFile audioFile) throws IOException {

        log.info("AI 처리 요청: 파일={}, 크기={}KB",
                audioFile.getOriginalFilename(),
                audioFile.getSize() / 1024);

        byte[] audioData = audioFile.getBytes();
        String filename = audioFile.getOriginalFilename();

        // 1. 동기: 엔티티 생성
        Long processId = processingService.startProcessing(audioData, filename);

        // 2. 비동기: AI 처리 실행
        processingService.executeProcessing(processId, audioData);

        return ResponseEntity.ok(Map.of("processId", processId));
    }

    /**
     * 처리 상태 조회 (Polling용)
     * GET /admin/api/ai/process/{processId}/status
     */
    @GetMapping("/process/{processId}/status")
    public ResponseEntity<AiProcessStatusResponse> getStatus(@PathVariable Long processId) {
        AiProcessResult result = resultService.getStatus(processId);
        return ResponseEntity.ok(AiProcessStatusResponse.from(result));
    }
}
