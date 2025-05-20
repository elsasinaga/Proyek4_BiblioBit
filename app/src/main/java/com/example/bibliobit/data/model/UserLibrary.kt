package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
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
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id") val id: Long = 0,
    @SerializedName("user_id") val userId: String,
    @SerializedName("book_id") val bookId: Long,
    @SerializedName("status") val status: BookStatus,
    @SerializedName("last_page_read") val lastPageRead: Int? = null,
    @SerializedName("updated_at") val updatedAt: Date,
    @SerializedName("rating") val rating: Float? = null,
    @SerializedName("is_synced") val isSynced: Boolean = false
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