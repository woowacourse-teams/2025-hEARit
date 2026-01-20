package com.onair.hearit.admin.ai.presentation;

import com.onair.hearit.admin.ai.application.AiResultService;
import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.dto.response.AiResultResponse;
import com.onair.hearit.core.infrastructure.jpa.CategoryRepository;
import com.onair.hearit.core.infrastructure.jpa.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AiViewController {

    private final AiResultService resultService;
    private final CategoryRepository categoryRepository;
    private final KeywordRepository keywordRepository;

    @Value("${aws.s3.bucket.url}")
    private String bucketUrl;

    @GetMapping("/ai-upload")
    public String aiUploadPage() {
        return "admin/ai-upload";
    }

    @GetMapping("/ai-result/{processId}")
    public String aiResultPage(@PathVariable Long processId, Model model) {
        AiProcessResult result = resultService.getResult(processId);
        if (result.getStatus() != ProcessStatus.COMPLETED) {
            return "redirect:/admin/ai-upload";
        }
        AiResultResponse resultResponse = AiResultResponse.from(result, bucketUrl);
        model.addAttribute("result", resultResponse);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keywords", keywordRepository.findAll());

        return "admin/ai-result";
    }
}
