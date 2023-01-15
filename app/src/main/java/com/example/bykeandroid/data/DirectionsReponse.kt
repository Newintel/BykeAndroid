package com.example.bykeandroid.data

@kotlinx.serialization.Serializable
class DirectionsReponse(
    val routes: List<Route>?,
    val status: String?,
    val error_message: String?,
)

@kotlinx.serialization.Serializable
class Route(
    val legs: List<Leg>?,
)

@kotlinx.serialization.Serializable
class Leg(
    val distance: Distance?,
    val duration: Duration?,
    val end_address: String?,
    val end_location: Location?,
    val start_address: String?,
    val start_location: Location?,
    val steps: List<GoogleStep>?,
)

@kotlinx.serialization.Serializable
class GoogleStep(
    val distance: Distance?,
    val duration: Duration?,
    val end_location: Location?,
    val html_instructions: String?,
    val maneuver: String?,
    val polyline: Polyline?,
    val start_location: Location?,
    val travel_mode: String?,
)

@kotlinx.serialization.Serializable
class Location(
    val lat: Double?,
    val lng: Double?,
)

@kotlinx.serialization.Serializable
class Polyline(
    val points: String?,
)

@kotlinx.serialization.Serializable
class Distance(
    val text: String?,
    val value: Int?,
)

@kotlinx.serialization.Serializable
class Duration(
    val text: String?,
    val value: Int?,
)

