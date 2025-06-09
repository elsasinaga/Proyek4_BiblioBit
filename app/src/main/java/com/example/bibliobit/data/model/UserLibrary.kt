package com.example.bibliobit.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class UserLibrary(
    @SerializedName("id") val id: Long? = null, // ID dari tabel user_library, bisa null saat membuat baru
    @SerializedName("user_id") val userId: String,
    @SerializedName("book_id") val bookId: Long,
    @SerializedName("status") val status: BookStatus,
    @SerializedName("last_page_read") val lastPageRead: Int? = null,
    @SerializedName("rating") val rating: Float? = null,
    @SerializedName("book") val book: Book? = null, // Untuk menampung data buku terkait saat fetch
    // Timestamp bisa di-handle dengan TypeAdapter Gson jika formatnya tidak standar
    @SerializedName("created_at") val createdAt: Date? = null,
    @SerializedName("updated_at") val updatedAt: Date? = null
)