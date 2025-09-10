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
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_uuid", "hearit_id"})
)
public class ExploreScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uuid", nullable = false, length = 36)
    private String userUuid;

    @Column(name = "hearit_id", nullable = false)
    private Long hearitId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "cursor_id")
    private Long cursorId;

    public ExploreScore(String userUuid, Long hearitId, Double score, Long cursorId) {
        validate(userUuid, hearitId, score);
        this.userUuid = userUuid.toString();
        this.hearitId = hearitId;
        this.score = score;
        this.cursorId = cursorId;
    }

    private void validate(String userUuid, Long hearitId, Double score) {
        validateUserUuid(userUuid);
        validateHearit(hearitId);
        validateScore(score);
    }

    private void validateUserUuid(String userUuid) {
        if (userUuid == null) {
            throw new IllegalArgumentException("userUuid는 null일 수 없습니다.");
        }
        if (userUuid.length() != 36) {
            throw new IllegalArgumentException("userUuid 형식이 올바르지 않습니다.");
        }
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
