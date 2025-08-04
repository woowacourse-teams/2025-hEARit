package com.onair.hearit.dto.response;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import java.time.LocalDateTime;

public record RandomHearitResponse(
        Long id,
        String title,
        Boolean isBookmarked,
        Long bookmarkId
) {
    public static RandomHearitResponse from(Hearit hearit) {
        return new RandomHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                false,
                null);
    }

    public static RandomHearitResponse fromWithBookmark(Hearit hearit, Bookmark bookmark) {
        return new RandomHearitResponse(
                hearit.getId(),
                hearit.getTitle(),
                true,
                bookmark.getId());
    }
}
