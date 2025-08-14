package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "recommend_hearit")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendHearit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearit_id", nullable = false)
    private Hearit hearit;

    @Column(name = "recommend_date", nullable = false)
    private LocalDate recommendDate;

    public RecommendHearit(Hearit hearit, LocalDate recommendDate) {
        validateFields(hearit, recommendDate);
        this.hearit = hearit;
        this.recommendDate = recommendDate;
    }

    private void validateFields(Hearit hearit, LocalDate recommendDate) {
        if (hearit == null) {
            throw new InvalidInputException("hearit은 null일 수 없습니다.");
        }
        if (recommendDate == null) {
            throw new InvalidInputException("recommendDate는 null일 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecommendHearit recommendHearit)) {
            return false;
        }
        if (this.id == null || recommendHearit.id == null) {
            return false;
        }
        return Objects.equals(id, recommendHearit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
