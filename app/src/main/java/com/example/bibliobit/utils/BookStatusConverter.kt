package com.example.bibliobit.utils

import android.util.Log
import androidx.room.TypeConverter
import com.example.bibliobit.data.model.BookStatus

class BookStatusConverter {
    @TypeConverter
    fun fromBookStatus(status: BookStatus?): String? {
        val result = status?.dbValue
        Log.d("BookStatusConverter", "Converting BookStatus to String: $status -> $result")
        return result
    }

    @TypeConverter
    fun toBookStatus(status: String?): BookStatus? {
        return try {
            val result = status?.let { value ->
                BookStatus.values().find { it.dbValue == value }
                    ?: throw IllegalArgumentException("No matching BookStatus for $value")
            }
            Log.d("BookStatusConverter", "Converting String to BookStatus: $status -> $result")
            result
        } catch (e: IllegalArgumentException) {
            Log.e("BookStatusConverter", "Invalid status value: $status", e)
            null // Kembalikan null jika status tidak valid
        }
    }
}