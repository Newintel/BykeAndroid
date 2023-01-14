package com.example.bykeandroid.api

import com.example.bykeandroid.data.User
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://bykeapi-production.up.railway.app"

private var retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

object ApiServices {
    val ping: Ping by lazy { retrofit.create(Ping::class.java) }
    lateinit var loginService: LoginService
    lateinit var excursionService: ExcursionService

    init {
        loadServices()
    }

    private fun loadServices() {
        loginService = retrofit.create(LoginService::class.java)
        excursionService = retrofit.create(ExcursionService::class.java)
    }

    private fun addCredentials(loginResponse: LoginResponse) {
        loginResponse.token?.let {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(it))
                .build()
            retrofit = retrofit.newBuilder().client(client).build()
            loadServices()
        }
    }

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val res = loginService.login(User(username = username, password = password))
        res.body()?.let {
            addCredentials(it)
        }
        return res
    }

    fun logout() {
        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        loadServices()
    }
}
