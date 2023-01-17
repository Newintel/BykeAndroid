package com.example.bykeandroid.api

import com.example.bykeandroid.data.Step
import retrofit2.Response
import retrofit2.http.POST

interface StepService {
    @POST("steps")
    suspend fun postStep(step: Step)
}