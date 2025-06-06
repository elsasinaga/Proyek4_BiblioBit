package com.example.bibliobit.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromBook(book: Book?): String? {
        return book?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toBook(json: String?): Book? {
        return if (json == null) null else gson.fromJson(json, object : TypeToken<Book>() {}.type)
    }
}