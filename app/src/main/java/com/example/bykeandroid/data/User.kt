package com.example.bykeandroid.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var id: Int? = null,
    var username: String? = "",
    var password: String? = "",
    var lastname: String? = "",
    var firstname: String? = "",
    var birthdate: String? = null,
    var size: Int? = null,
    var weight: Int? = null,
)