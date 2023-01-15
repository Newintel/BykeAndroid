package com.example.bykeandroid.api

import com.example.bykeandroid.data.DirectionsReponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("key") key: String
    ): Response<DirectionsReponse>
}