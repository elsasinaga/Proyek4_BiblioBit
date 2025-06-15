package com.example.bibliobit.data.model

import com.google.gson.annotations.SerializedName

data class GoogleBook(
    @SerializedName("google_id") val googleId: String?, // <-- TAMBAHKAN INI
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("year") val year: Int?,
    @SerializedName("pages") val pages: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("isbn") val isbn: String?,
    @SerializedName("cover_photo_path") val coverPhotoPath: String?,
    @SerializedName("genre") val genre: String?
)
