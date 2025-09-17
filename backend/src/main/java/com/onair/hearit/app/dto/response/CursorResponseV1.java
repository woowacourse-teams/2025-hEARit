package com.onair.hearit.app.dto.response;

import java.util.List;

public record CursorResponseV1<T>(
        List<T> content,
        boolean isEmpty,
        long cursorId
) {
    public static <T> CursorResponseV1<T> from(CursorResponse<T> cursorResponse) {
        List<T> contents = cursorResponse.content();
        long cursorId = 0L;
        if(!contents.isEmpty()) {
            ExploredHearitResponse last = (ExploredHearitResponse) contents.getLast();
            cursorId = last.cursorId();;
        }
        return new CursorResponseV1<>(
                contents,
                contents.isEmpty(),
                cursorId
        );
    }
}
