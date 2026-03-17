package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.HearitClusterDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    군집화에 활용될 컬럼(Feature)과 타겟 군집(Target Cluster)을 저장하는 테이블

    - hearit_id: 히어릿 고유 식별자 (PK)
    - view_count: 히어릿 전체 재생(조회) 횟수
    - like_count: 히어릿 좋아요 누적 횟수
    - bookmark_count: 히어릿 북마크 누적 횟수
    - avg_play_time: 히어릿 평균 재생 시간 (초 단위 평균)
    - completion_rate: 히어릿 재생 완료율 (0.0 ~ 1.0)
    - created_at: 히어릿 최신성 점수를 위한 업로드 날짜
    - cluster_id: Clustering 결과로 할당된 군집 ID
    - updated_at: 마지막 군집화 배치 수행 시각
 */
@Entity
@Getter
@Table(name = "hearit_cluster")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HearitCluster {

    @Id
    @Column(name = "hearit_id")
    private Long hearitId;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "bookmark_count", nullable = false)
    private long bookmarkCount;

    @Column(name = "avg_play_time", nullable = false)
    private double avgPlayTime;

    @Column(name = "completion_rate", nullable = false)
    private double completionRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "cluster_id", nullable = false)
    private int clusterId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public HearitCluster(Long hearitId, long viewCount, long likeCount, long bookmarkCount, double avgPlayTime,
                         double completionRate, LocalDateTime createdAt, int clusterId,
                         LocalDateTime updatedAt) {
        validate(hearitId, createdAt, updatedAt);
        this.hearitId = hearitId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.bookmarkCount = bookmarkCount;
        this.avgPlayTime = avgPlayTime;
        this.completionRate = completionRate;
        this.createdAt = createdAt;
        this.clusterId = clusterId;
        this.updatedAt = updatedAt;
    }

    private void validate(Long hearitId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (hearitId == null) {
            throw new HearitClusterDomainException("히어릿은 null이 될 수 없습니다.");
        }
        if (createdAt == null) {
            throw new HearitClusterDomainException("히어릿 생성 날짜는 null이 될 수 없습니다.");
        }
        if (updatedAt == null) {
            throw new HearitClusterDomainException("행을 업데이트한 날짜는 null이 될 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HearitCluster that)) {
            return false;
        }
        return Objects.equals(hearitId, that.hearitId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hearitId);
    }
}
