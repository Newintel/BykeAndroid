package com.example.bykeandroid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bykeandroid.api.ApiServices
import com.example.bykeandroid.data.Path
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import retrofit2.Response

class HomeViewModel : ViewModel() {
    private var viewModelJob = Job()

    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun getPaths(
        onResponse: (Response<List<Path>>?) -> Unit
    ) {
        coroutineScope.launch {
            try {
                onResponse(ApiServices.pathService.getMostPopularPaths())
            } catch (e: Exception) {
                println(e.message)
                onResponse(null)
            }
        }
    }

    fun getLength(
        path: Path,
        onResponse: (Double?, List<String>?) -> Unit
    ) {
        val latLongs = path.pathsteps?.sortedBy { it.id?.position }
            ?.map { LatLng(it.step!!.latitude!!, it.step.longitude!!) }

        if (latLongs == null) {
            onResponse(null, null)
            return
        }

        val directions: ArrayList<String> = ArrayList()

        if (latLongs.isEmpty()) {
            onResponse(0.0, directions)
            return
        }

        var length = 0.0

        coroutineScope.launch {
            var i = 0
            var errors = 0
            while (i < latLongs.size - 1) {
                val res = ApiServices.getDirections(latLongs[i++], latLongs[i])
                if (res.isSuccessful) {
                    res.body()?.let {
                        if (it.error_message != null && errors++ < 3) {
                            i--
                            return@let
                        } else {
                            if (errors >= 3) {
                                Log.e(
                                    "HomeViewModel",
                                    "Errors on directions from step ${i - 1} to step $i"
                                )
                            }
                            errors = 0
                        }
                        val route = it.routes?.filter { route ->
                            (route.legs?.size ?: 0) > 0
                        }?.minByOrNull { route ->
                            route.legs!!.sumOf { leg ->
                                leg.distance!!.value!!.toDouble()
                            }
                        }

                        route?.legs?.let { legs ->
                            for (leg in legs) {
                                length += leg.distance?.value?.toDouble() ?: 0.0
                                for (step in leg.steps!!) {
                                    directions.add(step.polyline!!.points!!)
                                }
                            }
                        }
                    }
                }
            }
            onResponse(length, directions)

        }
    }
}