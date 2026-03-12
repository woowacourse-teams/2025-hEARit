package com.onair.hearit.app.cluster.dto;

/*
 * 히어릿 군집화(Clustering)을 위한 원본 통계 Feature (After Normalization)
 */
public record NormalizedHearitClusterFeature(
        Long hearitId,
        double viewCount,
        double likeCount,
        double bookmarkCount,
        double avgPlayTime,
        double completionRate,
        double recencyScore
) {

    public NormalizedHearitClusterFeature {
        validateHearitId(hearitId);
        validateRange(viewCount);
        validateRange(likeCount);
        validateRange(bookmarkCount);
        validateRange(avgPlayTime);
        validateRange(completionRate);
        validateRange(recencyScore);
    }

    private void validateHearitId(Long hearitId) {
        if (hearitId == null) {
            throw new IllegalArgumentException("hearitId는 null일 수 없습니다.");
        }
    }

    private void validateRange(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("정규화 된 값은 0.0~1.0 사이여야 합니다.");
        }
    }

    public double[] vector() {
        return new double[]{
                viewCount,
                likeCount,
                bookmarkCount,
                avgPlayTime,
                completionRate,
                recencyScore
        };
    }
}
