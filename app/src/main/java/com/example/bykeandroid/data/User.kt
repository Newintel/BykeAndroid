package com.example.bykeandroid.data

import java.util.Date

data class User(
    var username: String? = "",
    var password: String? = "",
    var lastname: String? = "",
    var firstname: String? = "",
    var birthdayDate: Date? = null,
    var size: Int? = null,
    var weight: Int? = null,
) : java.io.Serializable