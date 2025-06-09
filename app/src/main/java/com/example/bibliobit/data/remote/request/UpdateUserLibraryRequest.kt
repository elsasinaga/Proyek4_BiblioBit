package com.example.bibliobit.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdateUserLibraryRequest(
    @SerializedName("status") val status: String,
    @SerializedName("last_page_read") val lastPageRead: Int? = null,
    @SerializedName("rating") val rating: Float? = null
)