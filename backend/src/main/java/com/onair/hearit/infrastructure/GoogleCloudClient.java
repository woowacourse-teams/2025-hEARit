package com.onair.hearit.infrastructure;

import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GoogleCloudClient {

    public static final String API_URL = "/v1/text:synthesize";

    private final RestClient restClient;
    private final String apiKey;

    public byte[] generateAudio(String script) {
        GoogleTtsResponse response = restClient.post()
                .uri(API_URL + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(GoogleTtsRequest.of(script))
                .retrieve()
                .body(GoogleTtsResponse.class);

        if (response == null || response.audioContent() == null) {
            throw new IllegalArgumentException("TTS 응답 본문 변환 실패");
        }
        return Base64.getDecoder().decode(response.audioContent());
    }

    private record GoogleTtsRequest(Input input, Voice voice, AudioConfig audioConfig) {

        public static GoogleTtsRequest of(String script) {
            return new GoogleTtsRequest(
                    new Input(script),
                    new Voice("ko-KR", "FEMALE"),
                    new AudioConfig("MP3", 1.0)
            );
        }

        private record Input(String text) {
        }

        private record Voice(String languageCode, String ssmlGender) {
        }

        private record AudioConfig(String audioEncoding, double speakingRate) {
        }
    }

    private record GoogleTtsResponse(String audioContent) {
    }
}
