package com.onair.hearit.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "explore_score",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "hearit_id"})
)
public class ExploreScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "hearit_id", nullable = false)
    private Long hearitId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "cursor_id")
    private Long cursorId;

    public ExploreScore(Long memberId, Long hearitId, Double score, Long cursorId) {
        validate(hearitId, score);
        this.memberId = memberId;
        this.hearitId = hearitId;
        this.score = score;
        this.cursorId = cursorId;
    }

    private void validate(Long hearitId, Double score) {
        validateHearit(hearitId);
        validateScore(score);
    }

    private void validateHearit(Long hearitId) {
        if (hearitId == null) {
            throw new IllegalArgumentException("히어릿은 null이 될 수 없습니다.");
        }
    }

    private void validateScore(Double score) {
        if (score == null) {
            throw new IllegalArgumentException("점수는 null이 될 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExploreScore exploreScore)) {
            return false;
        }
        if (this.id == null || exploreScore.id == null) {
            return false;
        }
        return Objects.equals(id, exploreScore.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
