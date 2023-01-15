package com.example.bykeandroid.utils

import com.google.android.gms.maps.model.LatLng


fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun decodePolyLines(poly: String): List<LatLng> {
    val len = poly.length
    var index = 0
    val decoded: MutableList<LatLng> = ArrayList()
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = poly[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = poly[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        decoded.add(
            LatLng(
                lat / 100000.0,
                lng / 100000.0
            )
        )
    }
    return decoded
}