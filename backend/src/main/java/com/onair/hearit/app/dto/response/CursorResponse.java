package com.onair.hearit.app.dto.response;

import java.util.List;

public record CursorResponse<T>(
        List<T> content,
        boolean isEmpty,
        Long cursorId
) {
    public static <T> CursorResponse<T> from(List<T> cursorResult, Long cursorId) {
        return new CursorResponse<>(
                cursorResult,
                cursorResult.isEmpty(),
                cursorId
        );
    }
}
