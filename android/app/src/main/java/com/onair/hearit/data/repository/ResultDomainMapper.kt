package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.domain.DomainExceptionMapper.toDomainException

inline fun <T, R> Result<NetworkResult<List<T>>>.mapListOrThrowDomain(crossinline transform: (T) -> R): Result<List<R>> =
    mapOrThrowDomain { list -> list.map { transform(it) } }

inline fun <T, R> Result<NetworkResult<T>>.mapOrThrowDomain(transform: (T) -> R): Result<R> =
    mapCatching { result ->
        when (result) {
            is NetworkResult.Success -> transform(result.data)
            is NetworkResult.Failure -> throw toDomainException(result)
        }
    }
