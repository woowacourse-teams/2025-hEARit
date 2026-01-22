package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.PlayingHistoryDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "playing_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_playing_history_member_hearit", columnNames = {"member_id", "hearit_id"})
        }
)
public class PlayingHistory {

    private static final int FINISHED_TIME_RANGE = 10;
    private static final int LAST_PLAY_TIME_PADDING = 1;
    private static final int MILLISECONDS_PER_SECOND = 1_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = true)
    private Long memberId;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID userUuid;

    @Column(name = "hearit_id", nullable = false)
    private Long hearitId;

    @Column(name = "last_play_time", nullable = false)
    private long lastPlayTime;

    @Column(name = "is_finished", nullable = false)
    private boolean isFinished;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlayingHistory(UUID userUuid, Hearit hearit, long lastPlayTime) {
        validateHearitPlayTime(hearit, lastPlayTime);
        this.userUuid = userUuid;
        this.hearitId = hearit.getId();
        this.lastPlayTime = lastPlayTime;
        this.isFinished = checkIsFinished(hearit, lastPlayTime);
    }

    private boolean checkIsFinished(Hearit hearit, long lastPlayTime) {
        return lastPlayTime >= (long) (hearit.getPlayTime() - FINISHED_TIME_RANGE) * MILLISECONDS_PER_SECOND;
    }

    private void validateHearitPlayTime(Hearit hearit, long lastPlayTime) {
        if (lastPlayTime < 0 ||
                lastPlayTime > (long) (hearit.getPlayTime() + LAST_PLAY_TIME_PADDING) * MILLISECONDS_PER_SECOND) {
            throw new PlayingHistoryDomainException("마지막 재생 시간은 히어릿의 총 재생 시간보다 작아야 합니다");
        }
    }
}
