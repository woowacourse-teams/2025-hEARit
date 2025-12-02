package com.onair.hearit.di

import com.onair.hearit.data.TokenAuthenticator

object TokenAuthenticatorProvider {
    @Volatile
    private var authenticator: TokenAuthenticator? = null

    fun init() {
        authenticator =
            TokenAuthenticator(
                { DataSourceProvider.authLocalDataSource },
                { NetworkProvider.authServiceNoAuth },
            )
    }

    fun provide(): TokenAuthenticator? = authenticator
}
