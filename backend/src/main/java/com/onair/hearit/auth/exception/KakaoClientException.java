package com.onair.hearit.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public class KakaoClientException extends RuntimeException {

    private HttpStatusCode statusCode;

    public KakaoClientException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
