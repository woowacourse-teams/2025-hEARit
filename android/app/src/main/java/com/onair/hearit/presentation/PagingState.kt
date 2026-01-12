package com.onair.hearit.presentation

data class PagingState(
    val currentPage: Int = 0,
    val isLastPage: Boolean = false,
    val isLoading: Boolean = false,
) {
    fun canLoadMore(): Boolean = !isLoading && !isLastPage

    fun startLoading(): PagingState = copy(isLoading = true)

    fun finishLoading(
        nextPage: Int,
        isLast: Boolean,
    ): PagingState =
        copy(
            currentPage = nextPage,
            isLastPage = isLast,
            isLoading = false,
        )

    fun failLoading(): PagingState = copy(isLoading = false)

    fun reset(): PagingState = PagingState()
}
