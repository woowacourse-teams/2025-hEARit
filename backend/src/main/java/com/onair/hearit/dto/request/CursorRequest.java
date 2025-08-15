package com.onair.hearit.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CursorRequest(
        @NotNull
        @Min(0)
        long cursorId,

        @NotNull
        @Min(1) @Max(20)
        int size
) {
}
