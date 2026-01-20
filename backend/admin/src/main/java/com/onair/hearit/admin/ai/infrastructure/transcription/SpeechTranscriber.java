package com.onair.hearit.admin.ai.infrastructure.transcription;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 음성을 텍스트로 변환하는 행위를 정의하는 인터페이스
 */
public interface SpeechTranscriber {

    /**
     * 오디오를 텍스트로 변환
     *
     * @param audioData 오디오 바이트 배열
     * @param filename 파일명 (확장자 포함)
     * @return 변환 결과
     */
    TranscriptionResult transcribe(byte[] audioData, String filename);

    /**
     * 지원하는 최대 파일 크기 (바이트)
     */
    int getMaxFileSizeBytes();

    /**
     * 변환 결과 DTO
     */
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
