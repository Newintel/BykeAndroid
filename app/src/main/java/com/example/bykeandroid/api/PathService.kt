package com.example.bykeandroid.api

import com.example.bykeandroid.data.Path
import retrofit2.Response
import retrofit2.http.GET

interface PathService {
    @GET("paths/mostPopular")
    suspend fun getMostPopularPaths(): Response<List<Path>>
}