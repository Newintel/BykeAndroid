package com.example.bykeandroid.data

@kotlinx.serialization.Serializable
class ExcursionId(
    val userId: Int?,
    val departure: String?,
)

@kotlinx.serialization.Serializable
class Excursion(
    val id : ExcursionId?,
    val arrival: String?,
    val path: Path?,
)