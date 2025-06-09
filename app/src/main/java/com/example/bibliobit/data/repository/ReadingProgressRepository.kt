package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.remote.RemoteDataSource
import javax.inject.Inject

/**
 * ReadingProgressRepository yang sudah dirombak untuk arsitektur online-only.
 * - Tidak ada lagi dependensi ke ReadingProgressDao.
 * - Semua fungsi langsung memanggil RemoteDataSource (API).
 * - Logika sinkronisasi dan metode yang tidak didukung backend telah dihapus.
 */
class ReadingProgressRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Mengambil riwayat baca untuk entri perpustakaan tertentu dari server.
     */
    suspend fun getReadingProgressByUserLibraryId(userLibraryId: Long): List<ReadingProgress> {
        // Backend Anda belum mengimplementasikan controller ini,
        // namun API-nya sudah didefinisikan.
        return remoteDataSource.getReadingProgress(userLibraryId)
    }

    /**
     * Mengirim data progres baca baru ke server untuk dibuat.
     */
    suspend fun insert(readingProgress: ReadingProgress): ReadingProgress {
        // Backend Anda belum mengimplementasikan controller ini,
        // namun API-nya sudah didefinisikan.
        return remoteDataSource.createReadingProgress(readingProgress)
    }

    suspend fun getAllReadingProgress(): List<ReadingProgress> {
        return remoteDataSource.getAllReadingProgress()
    }
}