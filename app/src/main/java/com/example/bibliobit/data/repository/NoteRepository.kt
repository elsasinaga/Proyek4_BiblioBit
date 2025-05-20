package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.Note
import com.example.bibliobit.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val remoteDataSource: RemoteDataSource
) {
    fun getNotesByUserLibraryId(userLibraryId: Long): Flow<List<Note>> {
        println("Fetching notes for userLibraryId: $userLibraryId")
        return noteDao.getNotesByUserLibraryId(userLibraryId)
    }

    suspend fun insert(note: Note) {
        println("Inserting note into database: $note")
        noteDao.insert(note.copy(isSynced = false))
        syncUnsyncedNotes()
    }

    suspend fun update(note: Note) {
        println("Updating note in database: $note")
        noteDao.update(note.copy(isSynced = false))
        syncUnsyncedNotes()
    }

    suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
        try {
            remoteDataSource.deleteNote(noteId)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllNotes(): List<Note> {
        println("Fetching all notes from database")
        return noteDao.getAllNotes()
    }

    suspend fun syncUnsyncedNotes() {
        try {
            val unsyncedNotes = noteDao.getUnsyncedNotes()
            if (unsyncedNotes.isNotEmpty()) {
                val syncedNotes = remoteDataSource.syncNotes(unsyncedNotes)
                syncedNotes.forEach { note ->
                    noteDao.insert(note.copy(isSynced = true))
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun syncNotesFromServer() {
        try {
            val serverNotes = remoteDataSource.getNotes()
            serverNotes.forEach { note ->
                noteDao.insert(note.copy(isSynced = true))
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
    }
}