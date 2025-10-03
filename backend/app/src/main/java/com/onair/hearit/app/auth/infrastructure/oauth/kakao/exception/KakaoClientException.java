package com.onair.hearit.app.auth.infrastructure.oauth.kakao.exception;

import com.onair.hearit.app.exception.ErrorCode;
import com.onair.hearit.app.exception.custom.HearitException;

public class KakaoClientException extends HearitException {

    public KakaoClientException(String detail) {
        super(ErrorCode.OAUTH_CLIENT_ERROR, detail);
    }
}
