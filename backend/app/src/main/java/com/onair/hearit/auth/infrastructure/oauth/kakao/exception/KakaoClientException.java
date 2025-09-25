package com.onair.hearit.auth.infrastructure.oauth.kakao.exception;

import com.onair.hearit.exception.ErrorCode;
import com.onair.hearit.exception.custom.HearitException;

public class KakaoClientException extends HearitException {

    public KakaoClientException(String detail) {
        super(ErrorCode.OAUTH_CLIENT_ERROR, detail);
    }
}
