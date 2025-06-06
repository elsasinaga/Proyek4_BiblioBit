package com.example.bibliobit.data.model

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateConverter {
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    @TypeConverter
    fun fromString(value: String?): Date? {
        return value?.let { format.parse(it) }
    }

    @TypeConverter
    fun dateToString(date: Date?): String? {
        return date?.let { format.format(it) }
    }
}