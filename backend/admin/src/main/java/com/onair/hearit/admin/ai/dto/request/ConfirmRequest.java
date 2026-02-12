package com.onair.hearit.admin.ai.dto.request;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest.SourceCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ConfirmRequest(
        @NotNull Long categoryId,
        List<Long> keywordIds,
        @NotEmpty List<@Valid SourceCreateRequest> sources,
        @NotBlank @Size(max = 35) String finalTitle,
        @NotBlank @Size(max = 250) String finalSummary,
        List<ScriptSegment> finalScript
) {
}
