package com.onair.hearit.data

import com.onair.hearit.data.dto.CategoryResponse
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging

fun CategoryResponse.Content.toDomain(): Category =
    Category(
        id = this.id,
        colorCode = this.colorCode,
        name = this.name,
    )

fun CategoryResponse.toDomain(): PageResult<Category> =
    PageResult(
        items = content.map { it.toDomain() },
        paging =
            Paging(
                page = page,
                size = size,
                totalPages = totalPages,
                isFirst = isFirst,
                isLast = isLast,
            ),
    )
