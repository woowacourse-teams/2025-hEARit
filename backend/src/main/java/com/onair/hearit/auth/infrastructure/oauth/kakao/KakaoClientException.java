package com.onair.hearit.auth.infrastructure.oauth.kakao;

import com.onair.hearit.common.exception.ErrorCode;
import com.onair.hearit.common.exception.custom.HearitException;

public class KakaoClientException extends HearitException {

    public KakaoClientException(String detail) {
        super(ErrorCode.OAUTH_CLIENT_ERROR, detail);
    }
}
