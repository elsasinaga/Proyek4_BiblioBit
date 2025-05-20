package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class LocalUser(
    @PrimaryKey
    @SerializedName("uid") val uid: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: String,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("is_synced") val isSynced: Boolean = false
)