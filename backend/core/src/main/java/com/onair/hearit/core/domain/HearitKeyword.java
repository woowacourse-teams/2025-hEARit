package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.HearitKeywordDomainException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "hearit_keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HearitKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearit_id", nullable = false)
    private Hearit hearit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    public HearitKeyword(Hearit hearit, Keyword keyword) {
        validate(hearit, keyword);
        this.hearit = hearit;
        this.keyword = keyword;
    }

    private void validate(Hearit hearit, Keyword keyword) {
        validateHearit(hearit);
        validateKeyword(keyword);
    }

    private void validateHearit(Hearit hearit) {
        if (hearit == null) {
            throw new HearitKeywordDomainException("히어릿은 null이 될 수 없습니다.");
        }
    }

    private void validateKeyword(Keyword keyword) {
        if (keyword == null) {
            throw new HearitKeywordDomainException("키워드는 null이 될 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HearitKeyword hearitKeyword)) {
            return false;
        }
        if (this.id == null || hearitKeyword.id == null) {
            return false;
        }
        return Objects.equals(id, hearitKeyword.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
