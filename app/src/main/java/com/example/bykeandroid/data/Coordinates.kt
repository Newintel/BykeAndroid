package com.example.bykeandroid.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Coordinates (
    @SerialName("lat")
    val latitude: Double,
    @SerialName("long")
    val longitude: Double,
)