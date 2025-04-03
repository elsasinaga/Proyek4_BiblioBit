package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
data class UserLibrary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val bookId: Long,
    val status: BookStatus,
    val lastPageRead: Int? = null,
    val updatedAt: Date,
    val rating: Float? = null
) {
    init {
        if (status != BookStatus.FINISH && rating != null) {
            throw IllegalArgumentException("Rating hanya boleh diisi jika status adalah FINISH")
        }
        if (rating != null && (rating < 0f || rating > 5f)) {
            throw IllegalArgumentException("Rating harus antara 0 dan 5")
        }
    }
}