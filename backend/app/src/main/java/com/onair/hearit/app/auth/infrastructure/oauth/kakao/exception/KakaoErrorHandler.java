package com.onair.hearit.app.auth.infrastructure.oauth.kakao.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.auth.infrastructure.oauth.kakao.dto.KakaoErrorResponse;
import com.onair.hearit.app.exception.custom.UnauthorizedException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
@RequiredArgsConstructor
public class KakaoErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) {
        KakaoErrorResponse kakaoErrorResponse = getKakaoErrorResponse(response);
        if (kakaoErrorResponse.code().equals("-401")) {
            throw new UnauthorizedException("Kakao Error Message : " + kakaoErrorResponse.msg());
        }
        throw new KakaoClientException("Kakao Error Message : " + kakaoErrorResponse.msg());
    }

    private KakaoErrorResponse getKakaoErrorResponse(ClientHttpResponse response) {
        try {
            byte[] bodyBytes = response.getBody().readAllBytes();
            if (bodyBytes.length == 0) {
                throw new KakaoClientException("Kakao 에러 응답이 비어있습니다.");
            }
            String errorBody = new String(bodyBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(errorBody, KakaoErrorResponse.class);
        } catch (IOException ex) {
            throw new KakaoClientException("Kakao 예외 응답 파싱에 실패했습니다.");
        }
    }
}
