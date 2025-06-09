package com.example.bibliobit.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.bibliobit.data.repository.ReadingProgressRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingStreak @Inject constructor(
    // ## DIPERBAIKI: Hanya butuh ReadingProgressRepository ##
    private val readingProgressRepository: ReadingProgressRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCurrentStreak(userId: String): Int { // userId tetap dibutuhkan untuk konteks, meski tidak dikirim ke API
        try {
            // 1. Ambil semua data progres baca dari server
            val readingProgressList = readingProgressRepository.getAllReadingProgress()

            if (readingProgressList.isEmpty()) {
                return 0 // Tidak ada aktivitas membaca, streak = 0
            }

            // 2. Konversi semua recordedAt ke LocalDate dan hapus duplikat hari
            val readingDates = readingProgressList
                .mapNotNull { it.recordedAt } // Ambil yang tidak null
                .map { date ->
                    Instant.ofEpochMilli(date.time)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                .distinct() // Hanya ambil tanggal unik
                .sorted() // Urutkan berdasarkan tanggal

            if (readingDates.isEmpty()) {
                return 0
            }

            // 3. Hitung streak dari tanggal terakhir mundur ke belakang
            var streak = 0
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            var lastDateInStreak = readingDates.last()

            // Periksa apakah hari membaca terakhir adalah hari ini atau kemarin
            if (lastDateInStreak == today || lastDateInStreak == yesterday) {
                streak = 1
                // Iterasi mundur dari tanggal kedua terakhir
                for (i in readingDates.indices.reversed().drop(1)) {
                    val currentDate = readingDates[i]
                    val daysBetween = ChronoUnit.DAYS.between(currentDate, lastDateInStreak)
                    if (daysBetween == 1L) {
                        streak++ // Hari berturut-turut, tambah streak
                    } else {
                        break // Streak terputus, hentikan perhitungan
                    }
                    lastDateInStreak = currentDate
                }
            } else {
                // Jika hari membaca terakhir bukan hari ini atau kemarin, streak adalah 0
                return 0
            }

            return streak

        } catch (e: Exception) {
            // Jika terjadi error saat mengambil data, anggap streak 0
            return 0
        }
    }
}