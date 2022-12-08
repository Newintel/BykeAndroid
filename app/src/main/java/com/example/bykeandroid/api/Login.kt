package com.example.bykeandroid.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST;

interface Login {
    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
}