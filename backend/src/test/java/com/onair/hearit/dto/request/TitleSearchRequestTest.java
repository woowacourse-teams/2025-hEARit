package com.onair.hearit.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TitleSearchRequestTest {
    static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("정상값이면 객체가 생성되고 violations가 없다.")
    void validInput() {
        TitleSearchRequest req = new TitleSearchRequest("search", 0, 10);
        Set<ConstraintViolation<TitleSearchRequest>> violations = validator.validate(req);
        assertAll(
            () -> assertThat(req.search()).isEqualTo("search"),
            () -> assertThat(req.page()).isEqualTo(0),
            () -> assertThat(req.size()).isEqualTo(10),
            () -> assertThat(violations).isEmpty()
        );
    }

    @Test
    @DisplayName("검색어가 20자 초과이면 violations가 있다.")
    void searchTooLong() {
        String longSearch = "a".repeat(21);
        TitleSearchRequest req = new TitleSearchRequest(longSearch, 0, 10);
        Set<ConstraintViolation<TitleSearchRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("search, page, size가 null이면 violations가 있다.")
    void nullFields() {
        TitleSearchRequest req1 = new TitleSearchRequest(null, 0, 10);
        TitleSearchRequest req2 = new TitleSearchRequest("search", null, 10);
        TitleSearchRequest req3 = new TitleSearchRequest("search", 0, null);
        assertAll(
            () -> assertThat(validator.validate(req1)).isNotEmpty(),
            () -> assertThat(validator.validate(req2)).isNotEmpty(),
            () -> assertThat(validator.validate(req3)).isNotEmpty()
        );
    }

    @Test
    @DisplayName("page, size가 범위를 벗어나면 violations가 있다.")
    void pageOrSizeOutOfRange() {
        TitleSearchRequest req1 = new TitleSearchRequest("search", -1, 10);
        TitleSearchRequest req2 = new TitleSearchRequest("search", 0, -5);
        TitleSearchRequest req3 = new TitleSearchRequest("search", 0, 21);
        assertAll(
            () -> assertThat(validator.validate(req1)).isNotEmpty(),
            () -> assertThat(validator.validate(req2)).isNotEmpty(),
            () -> assertThat(validator.validate(req3)).isNotEmpty()
        );
    }
} 