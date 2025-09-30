package com.onair.hearit.app.explore.dto;

import java.util.List;

public record CursorResponseV1<T>(
        List<T> content,
        boolean isEmpty,
        long cursorId
) {
    public static <T> CursorResponseV1<T> from(CursorResponseV2<T> cursorResponseV2) {
        List<T> contents = cursorResponseV2.content();
        long cursorId = 0L;
        if (!contents.isEmpty()) {
            ExploredHearitResponse last = (ExploredHearitResponse) contents.getLast();
            cursorId = last.cursorId();
        }
        return new CursorResponseV1<>(
                contents,
                contents.isEmpty(),
                cursorId
        );
    }
}
