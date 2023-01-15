package com.example.bykeandroid.api

import android.util.Log
import okhttp3.Interceptor

class GoogleInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        Log.i("google request", original.url().toString())
        val request = original.newBuilder()
            .header("Content-Type", "text/plain")
            .build()
        return chain.proceed(request)
    }
}