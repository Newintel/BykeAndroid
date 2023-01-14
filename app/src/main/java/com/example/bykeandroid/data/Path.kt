package com.example.bykeandroid.data

@kotlinx.serialization.Serializable
class Path(
    val id: Int?,
    val name: String?,
    val creator: User?,
)