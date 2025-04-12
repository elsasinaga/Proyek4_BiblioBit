package com.example.bibliobit.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.bibliobit.data.repository.ReadingProgressDao
import com.example.bibliobit.data.repository.UserLibraryDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class ReadingStreak @Inject constructor(
    @ApplicationContext private val context: Context,
    private val readingProgressDao: ReadingProgressDao,
    private val userLibraryDao: UserLibraryDao
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCurrentStreak(userId: String): Int {
        // Ambil semua ReadingProgress untuk pengguna ini
        val readingProgressList = readingProgressDao.getReadingProgressByUserId(userId).firstOrNull() ?: emptyList()

        if (readingProgressList.isEmpty()) {
            return 0 // Tidak ada aktivitas membaca, streak = 0
        }

        // Konversi semua recordedAt ke LocalDate dan hapus duplikat hari
        val readingDates = readingProgressList
            .map { record ->
                Instant.ofEpochMilli(record.recordedAt.time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .distinct() // Hanya ambil tanggal unik (jika pengguna membaca beberapa kali dalam sehari)
            .sorted() // Urutkan berdasarkan tanggal

        if (readingDates.isEmpty()) {
            return 0
        }

        // Hitung streak
        var streak = 1 // Mulai dari 1 karena ada setidaknya 1 hari
        var previousDate: LocalDate? = null

        for (currentDate in readingDates) {
            if (previousDate == null) {
                previousDate = currentDate
                continue
            }

            val daysBetween = ChronoUnit.DAYS.between(previousDate, currentDate)
            if (daysBetween == 1L) {
                // Hari berturut-turut, tambah streak
                streak++
            } else if (daysBetween > 1L) {
                // Streak terputus, reset streak ke 1
                streak = 1
            }
            previousDate = currentDate
        }

        // Periksa apakah streak masih berlanjut hingga hari ini
        val today = LocalDate.now()
        val lastReadingDate = readingDates.last()
        val daysSinceLastReading = ChronoUnit.DAYS.between(lastReadingDate, today)

        return if (daysSinceLastReading > 1) {
            0 // Streak terputus jika tidak membaca hingga hari ini
        } else {
            streak
        }
    }
}