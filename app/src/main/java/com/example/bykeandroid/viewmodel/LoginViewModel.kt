package com.example.bykeandroid.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.example.bykeandroid.api.ApiServices
import com.example.bykeandroid.api.LoginResponse
import com.example.bykeandroid.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel : ViewModel() {
    private var viewModelJob = Job()

    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun connect(username : String, password: String, onResponse: (Response<LoginResponse>?) -> Unit) {
        coroutineScope.launch {
            try {
                onResponse(ApiServices.login(username, password))
            } catch (e: Exception) {
                println(e.message)
                onResponse(null)
            }
        }
    }
}