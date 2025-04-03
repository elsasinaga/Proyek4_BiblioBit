package com.example.bibliobit.utils

import androidx.room.TypeConverter
import com.example.bibliobit.data.model.BookStatus

class BookStatusConverter {
    @TypeConverter
    fun fromBookStatus(status: BookStatus?): String? {
        return status?.name // Simpan enum sebagai String (misalnya "PLAN_TO_READ")
    }

    @TypeConverter
    fun toBookStatus(status: String?): BookStatus? {
        return status?.let { BookStatus.valueOf(it) } // Konversi String kembali ke enum
    }
}