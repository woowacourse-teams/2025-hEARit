package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Source {

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "source_url")
    private String sourceUrl;

    public Source(String sourceName, String sourceUrl) {
        validate(sourceName, sourceUrl);
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
    }

    private void validate(String sourceName, String sourceUrl) {
        if (sourceName == null || sourceName.isBlank() || sourceName.length() > 250) {
            throw new InvalidInputException("출처명(sourceName)은 250자 이하의 유효한 문자열이어야 합니다.");
        }
    }
}
