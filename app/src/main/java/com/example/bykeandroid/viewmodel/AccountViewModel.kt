package com.example.bykeandroid.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bykeandroid.api.ApiServices
import com.example.bykeandroid.data.Excursion
import com.example.bykeandroid.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    private var viewModelJob = Job()
    var user: User? = null

    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun loadUser(onSuccess: (User) -> Unit) {
        coroutineScope.launch {
            try {
                val res = ApiServices.loginService.getUser()
                if (res.isSuccessful) {
                    res.body()?.let {
                        user = it
                        onSuccess(it)
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    fun loadUserExcursions(onSuccess: (List<Excursion>) -> Unit) {
        if (user == null) {
            println("User is null")
            return
        }

        coroutineScope.launch {
            try {
                val res = ApiServices.excursionService.getExcursions(user!!.id!!)
                if (res.isSuccessful) {
                    res.body()?.let {
                        onSuccess(it)
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    fun logout() {
        ApiServices.logout()
    }
}