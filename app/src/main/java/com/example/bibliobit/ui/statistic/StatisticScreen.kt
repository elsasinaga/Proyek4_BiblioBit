package com.example.bibliobit.ui.statistic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.ui.components.BarChart
import com.example.bibliobit.ui.components.StatisticFilterButton
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau4
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticViewModel
) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val statisticType by viewModel.statisticType.collectAsState()
    val pagesReadData by viewModel.pagesReadData.collectAsState()
    val totalPagesRead by viewModel.totalPagesRead.collectAsState()
    val booksFinishedData by viewModel.booksFinishedData.collectAsState()
    val totalBooksFinished by viewModel.totalBooksFinished.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()
    val finishedBooks by viewModel.finishedBooks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StatisticTypeDropdown(
            selectedType = statisticType,
            onTypeSelected = { viewModel.setStatisticType(it) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        StatisticFilterButton(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setFilter(it) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Text(text = if (statisticType == "pages") "Halaman yang dibaca" else "Buku yang selesai", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = abu2)
            Text(text = if (statisticType == "pages") totalPagesRead.toString() else totalBooksFinished.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = hitam)
            Spacer(modifier = Modifier.height(16.dp))

            BarChart(
                data = if (statisticType == "pages") pagesReadData else booksFinishedData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(text = if (statisticType == "pages") "Riwayat Membaca" else "Buku Selesai", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = hitam)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (statisticType == "pages") {
                    if (readingHistory.isEmpty()) {
                        item { Text("Tidak ada riwayat membaca pada periode ini.", color = abu2, modifier = Modifier.padding(vertical = 16.dp)) }
                    } else {
                        items(readingHistory) { progress ->
                            ReadingHistoryItem(progress = progress)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    if (finishedBooks.isEmpty()) {
                        item { Text("Tidak ada buku yang selesai dibaca pada periode ini.", color = abu2, modifier = Modifier.padding(vertical = 16.dp)) }
                    } else {
                        items(finishedBooks) { book ->
                            BookHistoryItem(book = book)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (selectedType == "pages") "Pages Read" else "Books Finished",
                color = abu2
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Pages Read") },
                onClick = {
                    onTypeSelected("pages")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Books Finished") },
                onClick = {
                    onTypeSelected("books")
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun ReadingHistoryItem(
    progress: ReadingProgress
) {
    val book = progress.userLibrary?.book
    if (book == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, hijau4, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = book.coverPhotoPath),
            contentDescription = "Book Cover",
            modifier = Modifier
                .width(50.dp)
                .height(75.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = hitam
            )
            Text(
                text = "Membaca ${progress.pageRead} halaman",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            progress.recordedAt?.let { date ->
                Text(
                    text = SimpleDateFormat("dd MMMM, HH:mm", Locale.getDefault()).format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun BookHistoryItem(
    book: Book
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, hijau4, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = book.coverPhotoPath),
            contentDescription = "Book Cover",
            modifier = Modifier
                .width(50.dp)
                .height(75.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = hitam
            )
            Text(
                text = "oleh ${book.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
