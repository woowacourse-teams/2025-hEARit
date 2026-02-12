package com.onair.hearit.admin.ai.infrastructure.audio;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface AudioProcessor {


    boolean supports(String filename);

    void validate(byte[] data, String filename);

    byte[] createShortClip(byte[] originalAudio, int durationSeconds);

    AudioMetadata extractMetadata(byte[] audioData);

    String getExtension();

    String getMimeType();

    @Getter
    @AllArgsConstructor
    class AudioMetadata {
        private final int bitrate;            // kbps
        private final double durationSeconds; // 재생 시간 (초)

        public int getDurationSecondsInt() {
            return (int) Math.ceil(durationSeconds);
        }
    }
}
