package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;

public record HearitDetailResponse(
        Long id,
        String title,
        String summary,
        String source,
        Integer playTime,
        LocalDateTime createdAt,
        Boolean isBookmarked,
        Long bookmarkId
) {

    public static HearitDetailResponse of(Hearit hearit, Boolean isBookmarked) {
        return new HearitDetailResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                isBookmarked,
                null);
    }

    public static HearitDetailResponse of(Hearit hearit, Bookmark bookmark) {
        return new HearitDetailResponse(
                hearit.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getSource(),
                hearit.getPlayTime(),
                hearit.getCreatedAt(),
                true,
                bookmark.getId());
    }
}
