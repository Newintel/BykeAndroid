package com.example.bykeandroid.api

import android.util.Log
import okhttp3.Interceptor

class AuthInterceptor(private val credentials : String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        Log.i("request", original.url().toString())
        val request = original.newBuilder()
            .header("Authorization", "Bearer $credentials")
            .build()
        return chain.proceed(request)
    }
}