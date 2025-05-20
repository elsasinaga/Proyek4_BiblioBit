package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("genre") val genre: String?,
    @SerializedName("year") val year: Int?,
    @SerializedName("description") val description: String?,
    @SerializedName("isbn") val isbn: String?,
    @SerializedName("pages") val pages: Int,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("cover_photo_path") val coverPhotoPath: String?,
    @SerializedName("is_synced") val isSynced: Boolean = false
)