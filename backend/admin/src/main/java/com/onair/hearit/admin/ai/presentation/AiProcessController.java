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

@RestController
@RequestMapping("/admin/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiProcessController {

    private final AiProcessingService processingService;
    private final AiResultService resultService;

    @PostMapping("/process")
    public ResponseEntity<Map<String, Long>> startProcess(
            @RequestParam("audioFile") MultipartFile audioFile) throws IOException {

        log.info("AI 처리 요청: 파일={}, 크기={}KB",
                audioFile.getOriginalFilename(),
                audioFile.getSize() / 1024);

        byte[] audioData = audioFile.getBytes();
        String filename = audioFile.getOriginalFilename();
        Long processId = processingService.startProcessing(audioData, filename);
        processingService.executeProcessing(processId, audioData);
        return ResponseEntity.ok(Map.of("processId", processId));
    }

    @GetMapping("/process/{processId}/status")
    public ResponseEntity<AiProcessStatusResponse> getStatus(@PathVariable Long processId) {
        AiProcessResult result = resultService.getStatus(processId);
        return ResponseEntity.ok(AiProcessStatusResponse.from(result));
    }
}
