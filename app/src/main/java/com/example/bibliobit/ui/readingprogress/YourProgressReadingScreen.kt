package com.example.bibliobit.ui.readingprogress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourProgressReadingScreen(
    userLibraryId: Long,
    bookTitle: String,
    totalPages: Int,
    viewModel: ReadingProgressViewModel,
    onNavigateBack: () -> Unit,
) {
    // ## PERBAIKAN 1: Ambil state secara terpisah dari ViewModel baru ##
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userLibrary by viewModel.userLibrary.collectAsState()
    val progressHistory by viewModel.progressHistory.collectAsState()

    // ## PERBAIKAN 2: Panggil fungsi `initialize` yang baru, bukan `loadData` ##
    LaunchedEffect(key1 = userLibraryId) {
        viewModel.initialize(userLibraryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress History") },
                // Anda bisa menambahkan tombol kembali di sini jika NavHost Anda meneruskannya
                // navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(...) } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
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

            // ## PERBAIKAN 3: Gunakan state yang sudah dipisah untuk menampilkan UI ##
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    ProgressTimeline(
                        progressHistory = progressHistory,
                        isFinished = userLibrary?.status == BookStatus.FINISH,
                        totalPages = totalPages
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProgressTimeline(
    progressHistory: List<ReadingProgress>,
    isFinished: Boolean,
    totalPages: Int
) {
    if (progressHistory.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "No reading progress yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = abu2
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Balik urutan agar item pertama (start reading) ada di atas
        val sortedHistory = progressHistory.sortedBy { it.recordedAt }
        val firstProgress = sortedHistory.firstOrNull()

        // Entri pertama: Mulai Membaca
        if (firstProgress != null) {
            item {
                TimelineItem(
                    date = firstProgress.recordedAt,
                    day = 1,
                    description = "Start Reading!"
                )
            }
        }

        // Entri progres selanjutnya
        var lastPage = 0
        items(sortedHistory) { progress ->
            val pageDiff = progress.pageRead - lastPage
            if (pageDiff > 0) { // Hanya tampilkan jika ada progres halaman
                TimelineItem(
                    date = progress.recordedAt,
                    day = null, // Hari tidak ditampilkan untuk progres biasa
                    description = "Read until page ${progress.pageRead} (+${pageDiff} pages)"
                )
            }
            lastPage = progress.pageRead
        }

        // Entri terakhir jika sudah selesai
        if (isFinished) {
            item {
                TimelineItem(
                    date = null,
                    day = null,
                    description = "I've read them all! 🎉"
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(date: Date?, day: Int?, description: String) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier.height(IntrinsicSize.Min) // Membuat Row setinggi kontennya
    ) {
        // Kolom untuk titik dan garis
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Garis atas (tidak ada untuk item pertama)
            if (day == null) {
                Box(modifier = Modifier.width(2.dp).weight(0.4f).background(hijau5))
            } else {
                Spacer(modifier = Modifier.weight(0.4f))
            }

            Surface(shape = CircleShape, color = hijau5, modifier = Modifier.size(16.dp)) {}

            // Garis bawah
            Box(modifier = Modifier.width(2.dp).weight(0.6f).background(hijau5))
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Konten teks
        Column(modifier = Modifier.padding(bottom = 24.dp, top = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day?.let { "Day $it" } ?: (date?.let { d -> dateFormat.format(d) } ?: ""),
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