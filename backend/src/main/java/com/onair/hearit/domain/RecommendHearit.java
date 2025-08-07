package com.onair.hearit.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
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

    //TODO 히어릿으로 변경해야함
    @JoinColumn(name = "hearit_id", nullable = false)
    private Long hearitId;

    @Column(name = "recommend_date", nullable = false)
    private LocalDate recommendDate;

    public RecommendHearit(Long hearitId, LocalDate recommendDate) {
        validateFields(hearitId, recommendDate);
        this.hearitId = hearitId;
        this.recommendDate = recommendDate;
    }

    private void validateFields(Long hearitId, LocalDate recommendDate) {
        if (hearitId == null) {
            throw new InvalidInputException("hearit Id는 null일 수 없습니다.");
        }
        if (recommendDate == null) {
            throw new InvalidInputException("recommendDate는 null일 수 없습니다.");
        }
    }
}
