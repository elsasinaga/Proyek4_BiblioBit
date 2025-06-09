package com.example.bibliobit.data.mapper

import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.remote.response.UserLibraryResponse
import java.text.SimpleDateFormat
import java.util.Locale

fun UserLibraryResponse.toDomain(): UserLibrary {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    return UserLibrary(
        id = this.id,
        userId = this.userId,
        bookId = this.bookId,
        status = BookStatus.valueOf(this.status),
        lastPageRead = this.lastPageRead,
        rating = this.rating,
        book = this.book,
        createdAt = this.createdAt?.let { runCatching { dateFormat.parse(it) }.getOrNull() },
        updatedAt = this.updatedAt?.let { runCatching { dateFormat.parse(it) }.getOrNull() }
    )
}

fun List<UserLibraryResponse>.toDomain(): List<UserLibrary> {
    return this.map { it.toDomain() }
}