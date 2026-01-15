package com.onair.hearit.admin.ai.dto.response;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;

public record AiProcessStatusResponse(
        Long processId,
        ProcessStatus status,
        int progress,
        String statusMessage,
        String errorMessage
) {
    public static AiProcessStatusResponse from(AiProcessResult result) {
        return new AiProcessStatusResponse(
                result.getId(),
                result.getStatus(),
                result.getProgress(),
                result.getStatusMessage(),
                result.getErrorMessage()
        );
    }
}
