package com.example.bykeandroid.api

import com.example.bykeandroid.data.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class LoginResponse {
    val token: String? = null
}

class RegisterResponse {
    val id: String? = null
}

interface LoginService {
    @POST("login")
    suspend fun login(
        @Body user: User
    ): Response<LoginResponse>

    @POST("login/register")
    suspend fun register(
        @Body user: User
    ): Response<RegisterResponse>

    @GET("login/user")
    suspend fun getUser(): Response<User>
}