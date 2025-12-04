package com.onair.hearit.data

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.mapper.ExceptionMapper

inline fun <T, R> NetworkResult<List<T>>.toDomainResultList(crossinline transform: (T) -> R): Result<List<R>> =
    when (this) {
        is NetworkResult.Success -> Result.success(data.map { transform(it) })
        is NetworkResult.Failure -> Result.failure(ExceptionMapper.mapToThrowable(this))
    }

inline fun <T, R> NetworkResult<T>.toDomainResult(transform: (T) -> R): Result<R> =
    when (this) {
        is NetworkResult.Success -> Result.success(transform(data))
        is NetworkResult.Failure -> Result.failure(ExceptionMapper.mapToThrowable(this))
    }

fun <T> NetworkResult<T>.toDomainResult(): Result<T> =
    when (this) {
        is NetworkResult.Success -> Result.success(data)
        is NetworkResult.Failure -> Result.failure(ExceptionMapper.mapToThrowable(this))
    }
