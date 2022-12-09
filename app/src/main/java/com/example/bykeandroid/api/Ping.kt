package com.example.bykeandroid.api

import retrofit2.Response
import retrofit2.http.GET


interface Ping {
    @GET("ping")
    suspend fun ping(): Response<Any>
}