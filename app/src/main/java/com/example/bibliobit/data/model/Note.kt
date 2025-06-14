package com.example.bibliobit.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Note(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_library_id") val userLibraryId: Long,
    @SerializedName("content") val content: String,
    @SerializedName("image") val image: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("created_at") val createdAt: Date? = null,
    @SerializedName("updated_at") val updatedAt: Date? = null
)