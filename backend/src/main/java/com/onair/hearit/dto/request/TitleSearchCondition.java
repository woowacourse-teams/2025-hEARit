package com.onair.hearit.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TitleSearchCondition(
        @NotNull @Size(max = 20) String search,
        @NotNull @Min(0) Integer page,
        @NotNull @Min(0) @Max(20) Integer size
) {
}