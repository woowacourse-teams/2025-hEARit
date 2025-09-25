package com.onair.hearit.common.dto.response;

import java.util.List;

public record CursorResponseV2<T>(
        List<T> content,
        boolean isEmpty
) {
    public static <T> CursorResponseV2<T> from(List<T> cursorResult) {
        return new CursorResponseV2<>(
                cursorResult,
                cursorResult.isEmpty()
        );
    }
}
