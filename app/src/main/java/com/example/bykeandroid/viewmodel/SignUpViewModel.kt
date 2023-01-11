package com.example.bykeandroid.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bykeandroid.api.ApiServices
import com.example.bykeandroid.api.RegisterResponse
import com.example.bykeandroid.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response

class SignUpViewModel : ViewModel() {
    private var viewModelJob = Job()

    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun signIn(
        username: String,
        password: String,
        onResponse: (Response<RegisterResponse>?) -> Unit
    ) {
        val user = User(username = username, password = password)
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