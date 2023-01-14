package com.example.bykeandroid.api

import com.example.bykeandroid.data.Excursion
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExcursionService {
    @GET("excursions/user/{id}")
    suspend fun getExcursions(@Path("id") id : Int): Response<List<Excursion>>
}