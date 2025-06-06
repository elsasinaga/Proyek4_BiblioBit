package com.example.bibliobit.utils

import com.example.bibliobit.data.model.BookStatus
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class BookStatusAdapter : TypeAdapter<BookStatus>() {
    override fun write(out: JsonWriter, value: BookStatus?) {
        out.value(value?.dbValue)
    }

    override fun read(`in`: JsonReader): BookStatus {
        val status = `in`.nextString()
        return BookStatus.entries.find { it.dbValue == status }
            ?: throw IllegalArgumentException("Status tidak valid: $status")
    }
}