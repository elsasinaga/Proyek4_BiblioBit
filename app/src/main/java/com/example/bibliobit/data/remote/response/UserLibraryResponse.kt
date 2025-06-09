package com.example.bibliobit.data.remote.response

import com.example.bibliobit.data.model.Book
import com.google.gson.annotations.SerializedName

data class UserLibraryResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("user_id") val userId: String,
    @SerializedName("book_id") val bookId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("last_page_read") val lastPageRead: Int?,
    @SerializedName("rating") val rating: Float?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("book") val book: Book? = null
)