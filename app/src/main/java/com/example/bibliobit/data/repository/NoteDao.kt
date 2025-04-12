package com.example.bibliobit.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bibliobit.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query("SELECT * FROM notes WHERE user_library_id = :userLibraryId ORDER BY created_at DESC")
    fun getNotesByUserLibraryId(userLibraryId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>
}