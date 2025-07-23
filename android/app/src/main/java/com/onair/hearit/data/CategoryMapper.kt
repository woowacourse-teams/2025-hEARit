package com.onair.hearit.data

import com.onair.hearit.data.dto.CategoryResponse
import com.onair.hearit.domain.Category

fun CategoryResponse.toDomain(): Category =
    Category(
        id = this.id,
        colorCode = this.colorCode,
        name = this.name,
    )
