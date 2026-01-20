package com.onair.hearit.admin.ai.infrastructure.transcription;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;


public interface SpeechTranscriber {


    TranscriptionResult transcribe(byte[] audioData, String filename);

    int getMaxFileSizeBytes();

    @Getter
    @AllArgsConstructor
    class TranscriptionResult {
        private final double duration;
        private final List<ScriptSegment> segments;
        public int getDurationSeconds() {
            return (int) Math.ceil(duration);
        }
    }
}
