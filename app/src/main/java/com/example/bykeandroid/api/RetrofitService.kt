package com.example.bykeandroid.api

import com.example.bykeandroid.data.User
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO : commenter la ligne et ajouter/décommenter son IP

//private const val HOST_IP = "192.168.1.12" // IP Eduroam de Franck
private const val HOST_IP = "192.168.237.243" // IP partage de co de Franck
private const val BASE_URL = "http://$HOST_IP:8080"

private var retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

object ApiServices {
    val loginService: Login by lazy { retrofit.create(Login::class.java) }
    val ping: Ping by lazy { retrofit.create(Ping::class.java) }

    private fun addCredentials(loginResponse: LoginResponse) {
        loginResponse.token?.let {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(it))
                .build()
            retrofit = retrofit.newBuilder().client(client).build()
        }
    }

    suspend fun login(username : String, password: String) : Response<LoginResponse> {
        val res = loginService.login(User(username, password))
        res.body()?.let {
            addCredentials(it)
        }
        return res
    }
}