package com.example.bibliobit.data.model

data class User(
    val email: String,
    val uid: String,
    val username: String = "",
    val name: String = "",
    val profileImage: String? = null
)