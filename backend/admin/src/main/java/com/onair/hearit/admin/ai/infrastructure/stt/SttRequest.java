package com.onair.hearit.admin.ai.infrastructure.stt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SttRequest {

    private final byte[] audioData;
    private final String filename;
    private final String language;

    public static SttRequest of(byte[] audioData, String filename) {
        return SttRequest.builder()
                .audioData(audioData)
                .filename(filename)
                .language("ko")
                .build();
    }

    public static SttRequest of(byte[] audioData, String filename, String language) {
        return SttRequest.builder()
                .audioData(audioData)
                .filename(filename)
                .language(language)
                .build();
    }
}
