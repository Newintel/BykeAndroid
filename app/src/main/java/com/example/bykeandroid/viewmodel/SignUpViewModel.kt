package com.example.bykeandroid.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bykeandroid.api.ApiServices
import com.example.bykeandroid.api.RegisterResponse
import com.example.bykeandroid.data.User
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response

class SignUpViewModel : ViewModel() {
    private var viewModelJob = Job()

    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
        .build()

    fun signIn(
        user : User,
        onResponse: (Response<RegisterResponse>?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val res = ApiServices.loginService.register(user)
                onResponse(res)
            } catch (e: Exception) {
                onResponse(null)
            }
        }
    }
}