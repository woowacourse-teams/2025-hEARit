package com.onair.hearit.presentation.detail

interface PlayerDetailClickListener {
    fun onClickCategory(
        id: Long,
        name: String,
        colorCode: String,
    )

    fun onClickSource(
        name: String,
        url: String,
    )

    fun onClickKeyword(term: String)
}
