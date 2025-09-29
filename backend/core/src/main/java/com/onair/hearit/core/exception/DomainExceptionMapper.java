package com.onair.hearit.core.exception;

import com.onair.hearit.core.domain.exception.DomainErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class DomainExceptionMapper {

    public HttpStatus toHttpStatus(DomainErrorCode code) {
        return switch (code) {
            case BOOKMARK_DOMAIN_ERROR,
                    CATEGORY_DOMAIN_ERROR,
                    EXPLORE_SCORE_DOMAIN_ERROR,
                    HEARIT_DOMAIN_ERROR,
                    HEARIT_KEYWORD_DOMAIN_ERROR,
                    KEYWORD_DOMAIN_ERROR,
                    MEMBER_DOMAIN_ERROR,
                    PLAYING_HISTORY_DOMAIN_ERROR,
                    RECOMMEND_HEARIT_DOMAIN_ERROR -> HttpStatus.BAD_REQUEST;

            case USERINFO_DOMAIN_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
