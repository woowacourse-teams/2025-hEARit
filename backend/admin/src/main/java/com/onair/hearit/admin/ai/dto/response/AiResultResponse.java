package com.onair.hearit.admin.ai.dto.response;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import java.time.LocalDateTime;
import java.util.List;

public record AiResultResponse(
        Long id,
        ProcessStatus status,
        String originalFileName,
        String orgAudioUrl,
        String shrAudioUrl,
        List<ScriptSegment> rawTranscript,
        List<ScriptSegment> correctedScript,
        String suggestedTitle,
        String suggestedSummary,
        List<ScriptSegment> editedScript,
        String editedTitle,
        String editedSummary,
        List<ScriptSegment> finalScript,
        String finalTitle,
        String finalSummary,
        Integer playTime,
        String playTimeFormatted,

        LocalDateTime createdAt,
        LocalDateTime completedAt,
        LocalDateTime expiresAt
) {
    public static AiResultResponse from(AiProcessResult result, String bucketUrl) {
        return new AiResultResponse(
                result.getId(),
                result.getStatus(),
                result.getOriginalFileName(),
                toUrl(bucketUrl, result.getGeneratedOrgKey()),
                toUrl(bucketUrl, result.getGeneratedShrKey()),
                result.getRawTranscript(),
                result.getCorrectedScript(),
                result.getSuggestedTitle(),
                result.getSuggestedSummary(),
                result.getEditedScript(),
                result.getEditedTitle(),
                result.getEditedSummary(),
                result.getFinalScript(),
                result.getFinalTitle(),
                result.getFinalSummary(),
                result.getPlayTime(),
                formatPlayTime(result.getPlayTime()),
                result.getCreatedAt(),
                result.getCompletedAt(),
                result.getExpiresAt()
        );
    }

    private static String toUrl(String bucketUrl, String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return bucketUrl + "/" + key;
    }

    private static String formatPlayTime(Integer seconds) {
        if (seconds == null) {
            return "0:00";
        }
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }
}
