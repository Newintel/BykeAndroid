package com.example.bykeandroid.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO : commenter la ligne et ajouter/décommenter son IP

//private const val HOST_IP = "192.168.1.12" // IP Eduroam de Franck
private const val HOST_IP = "192.168.237.243" // IP partage de co de Franck
private const val BASE_URL = "http://$HOST_IP:8080"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

object ApiServices {
    val loginService: Login by lazy { retrofit.create(Login::class.java) }
    val ping: Ping by lazy { retrofit.create(Ping::class.java) }
}