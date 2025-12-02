package com.onair.hearit.domain.exception

sealed class DomainException : Exception() {
    data object NetworkConnection : DomainException()

    data object UserNotRegistered : DomainException()
}
