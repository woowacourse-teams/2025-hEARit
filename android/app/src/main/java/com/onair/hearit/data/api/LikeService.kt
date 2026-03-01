package com.onair.hearit.data.api

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface LikeService {
    @POST("api/v1/hearits/{hearitId}/likes")
    suspend fun postLike(
        @Path("hearitId") hearitId: Long,
    ): Response<Unit>

    @DELETE("api/v1/hearits/{hearitId}/likes")
    suspend fun deleteLike(
        @Path("hearitId") hearitId: Long,
    ): Response<Unit>
}
