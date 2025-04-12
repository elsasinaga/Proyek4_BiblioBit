package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getNotesByUserLibraryId(userLibraryId: Long): Flow<List<Note>> {
        val notesFlow = noteDao.getNotesByUserLibraryId(userLibraryId)
        println("Fetching notes for userLibraryId: $userLibraryId")
        return notesFlow
    }

    suspend fun insert(note: Note) {
        println("Inserting note into database: $note")
        noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        println("Updating note in database: $note")
        noteDao.update(note)
    }

    suspend fun getAllNotes(): List<Note> {
        println("Fetching all notes from database")
        return noteDao.getAllNotes()
    }
}