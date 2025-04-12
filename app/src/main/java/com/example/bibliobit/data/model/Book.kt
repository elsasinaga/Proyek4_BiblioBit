package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val genre: String?,
    val year: Int?,
    val description: String?,
    val isbn: String?,
    val pages: Int,
    val publisher: String?,
    val coverPhotoPath: String? // Path ke file foto cover di penyimpanan internal
)