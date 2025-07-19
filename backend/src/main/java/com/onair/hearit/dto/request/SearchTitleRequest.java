package com.onair.hearit.dto.request;

public record SearchTitleRequest(
        String title,
        int page,
        int size
) {
}
