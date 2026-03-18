package com.onair.hearit.app.explore.dto;

public record ExploreCursor(
        double score,
        long hearitId
) {
    public static ExploreCursor initial() {
        return new ExploreCursor(Double.MAX_VALUE, Long.MAX_VALUE);
    }

    public boolean isInitial() {
        return score == Double.MAX_VALUE;
    }
}
