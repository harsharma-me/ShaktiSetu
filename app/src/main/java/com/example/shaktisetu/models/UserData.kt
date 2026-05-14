package com.example.shaktisetu.models

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val pin: String? = null,
    val address: String = "",
    val profile_photo_url: String = ""
)

@Serializable
data class UserDataResponse(
    val id: Long,
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val pin: String?,
    val address: String,
    val profile_photo_url: String = ""
)
