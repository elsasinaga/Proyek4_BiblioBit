package com.example.bibliobit.data.remote.request

import com.google.gson.annotations.SerializedName

data class UserLibraryRequest(
    @SerializedName("book_id") val bookId: Long,
    @SerializedName("status") val status: String
)