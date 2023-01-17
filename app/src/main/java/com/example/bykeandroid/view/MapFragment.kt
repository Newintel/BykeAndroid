package com.example.bykeandroid.view

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.example.bykeandroid.R
import com.example.bykeandroid.utils.decodePolyLines
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapFragment : Fragment() {
    private val args: MapFragmentArgs by navArgs()

    private val callback = OnMapReadyCallback { googleMap ->
        args.steps?.let { steps ->
            steps.forEach {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.step!!.latitude!!, it.step.longitude!!))
                        .title("${it.id!!.position} - ${it.step.location}")
                )
            }
            steps.find { it.id!!.position == 1 }?.let {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.step!!.latitude!!, it.step.longitude!!),
                        10f
                    )
                )
            }
        }
        args.polylines?.forEach { polyline ->
            val polylineOptions = PolylineOptions()
            polylineOptions.addAll(decodePolyLines(polyline))
            polylineOptions.width(10f)
            polylineOptions.color(R.color.colorPrimary)
            googleMap.addPolyline(polylineOptions)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity as MainActivity
        activity.bottomNavigationView.isVisible = false

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}