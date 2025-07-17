package com.onair.hearit.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.auth.dto.response.KakaoErrorResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoErrorHandler implements ErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        KakaoErrorResponse kakaoErrorResponse = extractResponseFrom(response.getBody());

        throw new KakaoClientException(
                response.getStatusCode(), kakaoErrorResponse.msg());
    }

    private KakaoErrorResponse extractResponseFrom(InputStream errorStream) {
        try (errorStream) {
            String errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(errorBody, KakaoErrorResponse.class);
        } catch (IOException e) {
            throw new KakaoClientException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 오류 응답을 파싱할 수 없습니다.");
        }
    }
}
