package com.example.bibliobit.data.model

import com.google.gson.annotations.SerializedName

data class UserLibraryResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("book_id")
    val bookId: Long,
    @SerializedName("status")
    val status: String,
    @SerializedName("last_page_read")
    val lastPageRead: Int? = null,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("rating")
    val rating: Float? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("book")
    val book: Book? = null
)