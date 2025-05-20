package com.example.bibliobit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
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
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id") val id: Long = 0,
    @ColumnInfo(name = "user_library_id")
    @SerializedName("user_library_id") val userLibraryId: Long,
    @SerializedName("content") val content: String,
    @SerializedName("image") val image: String?,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at") val createdAt: Date,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at") val updatedAt: Date? = null,
    @SerializedName("is_synced") val isSynced: Boolean = false
)