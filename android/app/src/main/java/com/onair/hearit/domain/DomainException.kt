package com.onair.hearit.domain

sealed class DomainException(
    message: String? = null,
) : Exception(message) {
    data object NetworkConnection : DomainException()

    data object UserNotRegistered : DomainException()

    data class NoBookmark(
        val reason: String,
    ) : DomainException(reason)
}
