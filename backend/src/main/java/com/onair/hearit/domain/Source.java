package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class Source {

    @Column(name = "sourceName")
    private String sourceName;

    @Column(name = "sourceUrl")
    private String sourceUrl;

    public Source(String sourceName, String sourceUrl) {
        validate(sourceName, sourceUrl);
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
    }

    private void validate(String sourceName, String sourceUrl) {
        if (((sourceName == null) || (sourceName.isEmpty()) || isInvalidLength(sourceName))) {
            throw new InvalidInputException("출처명(sourceName)은 250자 이하의 유효한 문자열이어야 합니다.");
        }
        if (isInvalidLength(sourceUrl)) {
            throw new InvalidInputException("출처 URL(sourceUrl)은 250자 이하의 유효한 문자열이어야 합니다.");
        }
    }

    private boolean isInvalidLength(String value) {
        return value.length() > 250;
    }
}
