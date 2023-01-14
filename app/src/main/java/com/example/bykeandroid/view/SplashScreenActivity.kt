package com.example.bykeandroid.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bykeandroid.R
import com.example.bykeandroid.api.ApiServices
import com.example.bykeandroid.utils.MyDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val job = Job()
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)
        coroutineScope.launch {
            try {
                val res = ApiServices.ping.ping()
                if (res.isSuccessful) {
                    startApp()
                }
            } catch (e: Exception) {
                MyDialog(getString(R.string.connection_failed), {
                    startApp()
                }, {
                    finish()
                }).show(supportFragmentManager, "dialog")
            }
        }
    }

    private fun startApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
