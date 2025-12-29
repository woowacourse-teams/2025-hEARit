package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.ExploreScoreDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID userUuid;

    @Column(name = "hearit_id", nullable = false)
    private Long hearitId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "cursor_id")
    private Long cursorId;

    public ExploreScore(UUID userUuid, Long hearitId, Double score, Long cursorId) {
        validate(userUuid, hearitId, score);
        this.userUuid = userUuid;
        this.hearitId = hearitId;
        this.score = score;
        this.cursorId = cursorId;
    }

    private void validate(UUID userUuid, Long hearitId, Double score) {
        validateUserUuid(userUuid);
        validateHearit(hearitId);
        validateScore(score);
    }

    private void validateUserUuid(UUID userUuid) {
        if (userUuid == null) {
            throw new ExploreScoreDomainException("userUuid는 null일 수 없습니다.");
        }
    }

    private void validateHearit(Long hearitId) {
        if (hearitId == null) {
            throw new ExploreScoreDomainException("히어릿은 null이 될 수 없습니다.");
        }
    }

    private void validateScore(Double score) {
        if (score == null) {
            throw new ExploreScoreDomainException("점수는 null이 될 수 없습니다.");
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
