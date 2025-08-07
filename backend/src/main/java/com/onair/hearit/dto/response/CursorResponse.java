package com.onair.hearit.dto.response;

import java.util.List;

public record CursorResponse<T>(
        List<T> content,
        boolean isEmpty
) {
    public static <T> CursorResponse<T> from(List<T> cursorResult) {
        return new CursorResponse<>(
                cursorResult,
                cursorResult.isEmpty()
        );
    }
}
