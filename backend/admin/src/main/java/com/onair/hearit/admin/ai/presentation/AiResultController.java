package com.onair.hearit.admin.ai.presentation;

import com.onair.hearit.admin.ai.application.AiResultService;
import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.dto.request.ConfirmRequest;
import com.onair.hearit.admin.ai.dto.request.MetadataUpdateRequest;
import com.onair.hearit.admin.ai.dto.request.ScriptUpdateRequest;
import com.onair.hearit.admin.ai.dto.response.AiResultResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 처리 결과 API 컨트롤러
 * 결과 조회, 수정, 확인 및 Hearit 등록
 */
@RestController
@RequestMapping("/admin/api/ai/results")
@RequiredArgsConstructor
@Slf4j
public class AiResultController {

    private final AiResultService resultService;

    @Value("${aws.s3.bucket.url}")
    private String bucketUrl;

    /**
     * AI 처리 결과 조회
     * GET /admin/api/ai/results/{processId}
     */
    @GetMapping("/{processId}")
    public ResponseEntity<AiResultResponse> getResult(@PathVariable Long processId) {
        AiProcessResult result = resultService.getResult(processId);
        return ResponseEntity.ok(AiResultResponse.from(result, bucketUrl));
    }

    /**
     * 대본 수정
     * PUT /admin/api/ai/results/{processId}/script
     */
    @PutMapping("/{processId}/script")
    public ResponseEntity<Map<String, String>> updateScript(
            @PathVariable Long processId,
            @Valid @RequestBody ScriptUpdateRequest request) {

        resultService.updateScript(processId, request.segments());

        return ResponseEntity.ok(Map.of("message", "대본이 저장되었습니다."));
    }

    /**
     * 메타데이터(제목, 요약) 수정
     * PUT /admin/api/ai/results/{processId}/metadata
     */
    @PutMapping("/{processId}/metadata")
    public ResponseEntity<Map<String, String>> updateMetadata(
            @PathVariable Long processId,
            @Valid @RequestBody MetadataUpdateRequest request) {

        resultService.updateMetadata(processId, request.title(), request.summary());

        return ResponseEntity.ok(Map.of("message", "메타데이터가 저장되었습니다."));
    }

    /**
     * 검토 완료 및 Hearit 등록
     * POST /admin/api/ai/results/{processId}/confirm
     */
    @PostMapping("/{processId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmAndRegister(
            @PathVariable Long processId,
            @Valid @RequestBody ConfirmRequest request) {

        log.info("Hearit 등록 요청: processId={}, 카테고리={}", processId, request.categoryId());

        resultService.confirmAndCreateHearit(
                processId,
                request.categoryId(),
                request.keywordIds(),
                request.sources(),
                request.finalTitle(),
                request.finalSummary(),
                request.finalScript()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Hearit이 등록되었습니다.",
                "processId", processId
        ));
    }

    /**
     * AI 결과 삭제 (취소)
     * DELETE /admin/api/ai/results/{processId}
     */
    @DeleteMapping("/{processId}")
    public ResponseEntity<Map<String, String>> deleteResult(@PathVariable Long processId) {
        resultService.deleteResult(processId);

        return ResponseEntity.ok(Map.of("message", "AI 처리 결과가 삭제되었습니다."));
    }
}
