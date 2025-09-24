package com.onair.hearit.app.dto.response;

import com.onair.hearit.common.domain.Bookmark;
import com.onair.hearit.common.domain.Category;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.Source;
import java.util.List;

public record BookmarkHearitResponseV2(
        Long hearitId,
        Long bookmarkId,
        String title,
        String summary,
        Integer playTime,
        Long lastPlayTime,
        Boolean isFinished,
        List<SourceResponse> sources,
        CategoryResponse category
) {
    public static BookmarkHearitResponseV2 of(Bookmark bookmark, Hearit hearit, PlayingHistory playingHistory) {
        List<SourceResponse> sources = getSources(hearit.getSources());
        return new BookmarkHearitResponseV2(
                hearit.getId(),
                bookmark.getId(),
                hearit.getTitle(),
                hearit.getSummary(),
                hearit.getPlayTime(),
                playingHistory != null ? playingHistory.getLastPlayTime() : null,
                playingHistory != null ? playingHistory.isFinished() : null,
                sources,
                CategoryResponse.from(hearit.getCategory()));
    }

    private static List<SourceResponse> getSources(List<Source> sources) {
        return sources.stream().map(SourceResponse::from).toList();
    }

    public record CategoryResponse(Long id, String name, String colorCode) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColorCode());
        }
    }

    public record SourceResponse(
            String sourceName,
            String sourceUrl
    ) {
        private static SourceResponse from(Source source) {
            return new SourceResponse(
                    source.getSourceName(),
                    source.getSourceUrl()
            );
        }
    }
}
