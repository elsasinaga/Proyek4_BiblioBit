package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.bibliobit.utils.BookStatusConverter
import java.util.Date

@Entity(
    tableName = "user_library",
    foreignKeys = [
        ForeignKey(
            entity = LocalUser::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["bookId"])
    ]
)
@TypeConverters(DateConverter::class, BookStatusConverter::class, BookConverter::class)
data class UserLibrary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val bookId: Long,
    val status: BookStatus,
    val lastPageRead: Int? = null,
    val updatedAt: Date,
    val rating: Float? = null,
    val createdAt: Date? = null,
    val isSynced: Boolean = false,
    val book: Book? = null
)