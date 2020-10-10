package com.kurumbus.instagram.models

import com.google.firebase.database.Exclude

data class User(
    val email: String = "",
    val username: String = "",
    val name: String? = null,
    val website: String? = null,
    val bio: String? = null,
    val phone: Long? = null,
    val photo: String? = null,
    val follows: Map<String, Boolean> = emptyMap(),
    val followers: Map<String, Boolean> = emptyMap(),
    @Exclude val uid: String? = null
    )