package com.example.bibliobit.ui.readingprogress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun YourProgressReadingScreen(
    userLibraryId: Long,
    bookTitle: String,
    totalPages: Int,
    viewModel: ReadingProgressViewModel,
    onNavigateBack: () -> Unit,
) {
    // ## DIPERBAIKI: Hanya observe satu state utama ##
    val uiState by viewModel.uiState.collectAsState()

    // ## DIPERBAIKI: LaunchedEffect disederhanakan ##
    // Cukup panggil loadData sekali untuk mengambil semua data yang diperlukan
    LaunchedEffect(key1 = userLibraryId) {
        viewModel.loadData(userLibraryId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = bookTitle,
            style = MaterialTheme.typography.titleLarge,
            color = hitam,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Gunakan when untuk menangani semua kondisi UI dari satu state
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                // Tampilkan timeline jika data berhasil dimuat
                ProgressTimeline(
                    progressHistory = uiState.progressHistory,
                    isFinished = uiState.userLibrary?.status == BookStatus.FINISH,
                    totalPages = totalPages
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProgressTimeline(
    progressHistory: List<ReadingProgress>,
    isFinished: Boolean,
    totalPages: Int
) {
    // ## DIPERBAIKI: Logika UI dipisah ke Composable sendiri agar lebih rapi ##
    val firstProgress = progressHistory.firstOrNull()

    if (progressHistory.isEmpty() || firstProgress == null) {
        Text(
            text = "No reading progress yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    // Entri pertama: Mulai Membaca
    TimelineItem(
        date = firstProgress.recordedAt,
        day = 1,
        description = "Start Reading!"
    )

    // Entri progres selanjutnya
    var lastPage = 0
    progressHistory.forEach { progress ->
        val pageDiff = progress.pageRead - lastPage
        TimelineItem(
            date = progress.recordedAt,
            day = null, // Hari tidak ditampilkan untuk progres biasa
            description = "Read until page ${progress.pageRead} (+${pageDiff} pages)"
        )
        lastPage = progress.pageRead
    }

    // Entri terakhir jika sudah selesai
    if (isFinished) {
        TimelineItem(
            date = null, // Tanggal bisa diambil dari userLibrary.updatedAt jika perlu
            day = null,
            description = "I've read them all! 🎉"
        )
    }
}

@Composable
private fun TimelineItem(date: java.util.Date?, day: Int?, description: String) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    Column {
        Spacer(modifier = Modifier.height(8.dp))
        // Garis vertikal
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(24.dp)
                .offset(x = 7.dp)
                .background(hijau5)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Titik timeline
            Surface(shape = CircleShape, color = hijau5, modifier = Modifier.size(16.dp)) {}
            Spacer(modifier = Modifier.width(16.dp))

            // Konten teks
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day?.let { "Day $it" } ?: dateFormat.format(date!!),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = hitam
                    )
                    if (day != null && date != null) {
                        Text(
                            text = dateFormat.format(date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = abu2
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = hitam
                )
            }
        }
    }
}