package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.Note
import com.example.bibliobit.data.remote.RemoteDataSource
import javax.inject.Inject

/**
 * NoteRepository yang sudah dirombak total untuk arsitektur online-only.
 * - Tidak ada lagi dependensi ke NoteDao.
 * - Semua fungsi langsung memanggil RemoteDataSource (API).
 * - Semua logika sinkronisasi telah dihapus.
 */
class NoteRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Mengambil semua catatan untuk entri perpustakaan tertentu dari server.
     * Mengembalikan List<Note>, bukan lagi Flow.
     */
//    suspend fun getNotesByUserLibraryId(userLibraryId: Long): List<Note> {
//        return remoteDataSource.getNotes(userLibraryId)
    }

    /**
     * Mengirim catatan baru ke server untuk dibuat.
     */
//    suspend fun insert(note: Note): Note {
//        // Memastikan ID adalah null agar backend tahu ini adalah operasi 'create'
//        return remoteDataSource.createNote(note.copy(id = null))
//    }
//
//    /**
//     * Mengirim pembaruan catatan ke server.
//     * Membutuhkan ID catatan yang akan diperbarui.
//     */
//    suspend fun update(note: Note): Note {
//        val noteId = note.id ?: throw IllegalArgumentException("Note ID cannot be null for an update.")
//        return remoteDataSource.updateNote(noteId, note)
//    }
//
//    /**
//     * Menghapus catatan dari server.
//     */
//    suspend fun deleteNote(noteId: Long) {
//        // Cukup panggil remoteDataSource, tidak ada lagi operasi DAO
//        remoteDataSource.deleteNote(noteId)
//    }
//}