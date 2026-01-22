package com.onair.hearit.admin.ai.infrastructure.transcription;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.stt.SttProvider;
import com.onair.hearit.admin.ai.infrastructure.stt.SttRequest;
import com.onair.hearit.admin.ai.infrastructure.stt.SttResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultSpeechTranscriber {

    private final SttProvider sttProvider;

    public TranscriptionResult transcribe(byte[] audioData, String filename) {
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("audioData는 null이거나 비어있을 수 없습니다");
        }

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("filename은 null이거나 비어있을 수 없습니다");
        }

        SttRequest request = SttRequest.of(audioData, filename);
        SttResponse response = sttProvider.transcribe(request);

        return new TranscriptionResult(response.getDuration(), response.getSegments());
    }

    public int getMaxFileSizeBytes() {
        return sttProvider.getMaxFileSizeBytes();
    }

    @Getter
    @AllArgsConstructor
    public static class TranscriptionResult {
        private final double duration;
        private final List<ScriptSegment> segments;

        public int getDurationSeconds() {
            return (int) Math.ceil(duration);
        }
    }
}
