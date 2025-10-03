package com.onair.hearit.admin.dto.request;

import java.util.List;
import org.springframework.data.domain.Page;

public record AdminPagedResponse<T>(
        List<T> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean isFirst,
        boolean isLast
) {
    public static <T> AdminPagedResponse<T> from(Page<T> pageResult) {
        return new AdminPagedResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }
}
