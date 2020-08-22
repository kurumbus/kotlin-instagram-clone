package com.kurumbus.instagram.models

data class User(
    val email: String = "",
    val username: String = "",
    val name: String? = null,
    val website: String? = null,
    val bio: String? = null,
    val phone: Long? = null,
    val photo: String? = null
    )