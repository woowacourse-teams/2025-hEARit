package com.onair.hearit.common.domain;


import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "playing_history")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayingHistory {

    private static final int FINISHED_TIME_RANGE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "hearit_id", nullable = false)
    private Long hearitId;

    @Column(name = "last_play_time", nullable = false)
    private int lastPlayTime;

    @Column(name = "is_finished", nullable = false)
    private boolean isFinished;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PlayingHistory(Long memberId, Hearit hearit, int lastPlayTime) {
        validateHearitPlayTime(hearit, lastPlayTime);
        this.memberId = memberId;
        this.hearitId = hearit.getId();
        this.lastPlayTime = lastPlayTime;
        this.isFinished = checkIsFinished(hearit, lastPlayTime);
    }

    private boolean checkIsFinished(Hearit hearit, int lastPlayTime) {
        return lastPlayTime >= hearit.getPlayTime() - FINISHED_TIME_RANGE;
    }

    private void validateHearitPlayTime(Hearit hearit, int lastPlayTime) {
        if (lastPlayTime > hearit.getPlayTime()) {
            throw new InvalidInputException("마지막 재생 시간은 히어릿의 총 재생 시간보다 작아야 합니다");
        }
    }
}
