package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class LocalUser(
    @PrimaryKey val uid: String,
    val email: String,
    val username: String,
    val name: String,
    val profileImage: String? = null,
    val isSynced: Boolean = false
)