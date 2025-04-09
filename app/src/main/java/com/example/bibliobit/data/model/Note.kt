package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = UserLibrary::class,
            parentColumns = ["id"],
            childColumns = ["user_library_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_library_id") val userLibraryId: Long,
    val content: String,
    val image: String?,
    @ColumnInfo(name = "created_at") val createdAt: Date,
    @ColumnInfo(name = "updated_at") val updatedAt: Date? = null // Nullable, karena catatan baru belum memiliki updatedAt
)