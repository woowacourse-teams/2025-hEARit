package com.onair.hearit.presentation

import android.os.Bundle

sealed class DetailResult {
    data class Category(
        val bundle: Bundle,
    ) : DetailResult()

    data class Keyword(
        val bundle: Bundle,
    ) : DetailResult()
}
