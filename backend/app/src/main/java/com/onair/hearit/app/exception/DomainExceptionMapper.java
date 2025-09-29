package com.onair.hearit.app.exception;

import com.onair.hearit.core.domain.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DomainExceptionMapper {

    public HttpStatus toHttpStatus(DomainException ex) {
        String code = ex.getCode();

        return switch (code) {
            // 4xx 계열의 예측된 비즈니스 예외들
            case "BOOKMARK_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "CATEGORY_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "EXPLORE_SCORE_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "HEARIT_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "HEARIT_KEYWORD_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "KEYWORD_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "MEMBER_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "PLAYING_HISTORY_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;
            case "RECOMMEND_HEARIT_DOMAIN_ERROR" -> HttpStatus.BAD_REQUEST;

            // 서버 로직 오류
            case "USERINFO_DOMAIN_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;

            default -> {
                log.error("매핑되지 않은 DomainException code 발생: code={}, message={}", code, ex.getMessage());
                yield HttpStatus.INTERNAL_SERVER_ERROR;
            }
        };
    }
}
