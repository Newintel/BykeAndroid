package com.example.bykeandroid.api

import com.example.bykeandroid.BuildConfig
import com.example.bykeandroid.data.DirectionsReponse
import com.example.bykeandroid.data.User
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://bykeapi-production.up.railway.app/"
private const val BASE_DISTANCE_URL = "https://maps.googleapis.com/maps/api/"

private var retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private var retrofitDistance = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_DISTANCE_URL)
    .client(
        OkHttpClient.Builder()
        .addInterceptor(GoogleInterceptor())
        .build()
    )
    .build()

object ApiServices {
    val ping: Ping by lazy { retrofit.create(Ping::class.java) }
    val google : GoogleService by lazy { retrofitDistance.create(GoogleService::class.java) }
    lateinit var loginService: LoginService
    lateinit var excursionService: ExcursionService
    lateinit var pathService: PathService

    init {
        loadServices()
    }

    private fun loadServices() {
        loginService = retrofit.create(LoginService::class.java)
        excursionService = retrofit.create(ExcursionService::class.java)
        pathService = retrofit.create(PathService::class.java)
    }

    private fun addCredentials(loginResponse: LoginResponse) {
        loginResponse.token?.let {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(it))
                .build()
            retrofit = retrofit.newBuilder().client(client).build()
            loadServices()
        }
    }

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        val res = loginService.login(User(username = username, password = password))
        res.body()?.let {
            addCredentials(it)
        }
        return res
    }

    fun logout() {
        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        loadServices()
    }

    // GOOGLE API
    suspend fun getDirections(a : LatLng, b : LatLng) : Response<DirectionsReponse> {
        val origin = a.let { "${it.latitude},${it.longitude}" }
        val destination = b.let{ "${it.latitude},${it.longitude}" }

        return google.getDirections(origin, destination, "bicycling", BuildConfig.googleKey)
    }

}
