package com.onair.hearit.domain

sealed class DomainException : Exception() {
    data object NetworkConnection : DomainException()

    data object UserNotRegistered : DomainException()
}
