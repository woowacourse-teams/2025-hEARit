package com.onair.hearit.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.onair.hearit.core.domain.exception.DomainErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

@DisplayName("DomainExceptionMapper 단위 테스트")
class DomainExceptionMapperTest {

    private DomainExceptionMapper domainExceptionMapper;

    @BeforeEach
    void setUp() {
        domainExceptionMapper = new DomainExceptionMapper();
    }

    @DisplayName("400 Bad Request로 매핑되어야 하는 도메인 에러 코드들을 테스트한다.")
    @ParameterizedTest
    @EnumSource(value = DomainErrorCode.class, names = {
            "BOOKMARK_DOMAIN_ERROR",
            "CATEGORY_DOMAIN_ERROR",
            "EXPLORE_SCORE_DOMAIN_ERROR",
            "HEARIT_DOMAIN_ERROR",
            "HEARIT_KEYWORD_DOMAIN_ERROR",
            "KEYWORD_DOMAIN_ERROR",
            "MEMBER_DOMAIN_ERROR",
            "PLAYING_HISTORY_DOMAIN_ERROR",
            "RECOMMEND_HEARIT_DOMAIN_ERROR"
    })
    void maps4xxErrorsToBadRequest(DomainErrorCode errorCode) {
        // when
        HttpStatus status = domainExceptionMapper.toHttpStatus(errorCode);

        // then
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("USERINFO_DOMAIN_ERROR는 500 Internal Server Error로 매핑되어야 한다.")
    @Test
    void mapsUserInfoErrorToInternalServerError() {
        // given
        DomainErrorCode errorCode = DomainErrorCode.USERINFO_DOMAIN_ERROR;

        // when
        HttpStatus status = domainExceptionMapper.toHttpStatus(errorCode);

        // then
        assertThat(status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
