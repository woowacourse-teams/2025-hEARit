package com.onair.hearit.presentation.detail

interface PlayerDetailClickListener {
    fun onClickCategory(
        id: Long,
        name: String,
    )

    fun onClickSource(sourceUrl: String)

    fun onClickKeyword(term: String)
}
