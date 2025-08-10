package com.onair.hearit.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;

/*
 * HearitException이 아니라 RuntTIme을 직접 상속
 * 외부 API에서 넘어오는 형식을 그대로 따라야하므로 기존 HearitException 재활용 불가
 */

@Getter
@RequiredArgsConstructor
public class KakaoClientException extends RuntimeException {

    private HttpStatusCode statusCode;

    public KakaoClientException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
