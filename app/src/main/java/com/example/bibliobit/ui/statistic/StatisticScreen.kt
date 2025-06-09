package com.example.bibliobit.ui.statistic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import yang benar cukup satu kali
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.ui.components.BarChart
import com.example.bibliobit.ui.components.StatisticFilterButton
import com.example.bibliobit.ui.theme.abu3
import com.example.bibliobit.ui.theme.hijau4
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StatisticScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticViewModel
) {
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.setUserId(userId)
        }
    }

    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val statisticType by viewModel.statisticType.collectAsState()
    val pagesReadData by viewModel.pagesReadData.collectAsState()
    val totalPagesRead by viewModel.totalPagesRead.collectAsState()
    val booksFinishedData by viewModel.booksFinishedData.collectAsState()
    val totalBooksFinished by viewModel.totalBooksFinished.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()
    val finishedBooks by viewModel.finishedBooks.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Dropdown untuk memilih jenis statistik
        StatisticTypeDropdown(
            selectedType = statisticType,
            onTypeSelected = { type ->
                viewModel.setStatisticType(type)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Buttons
        StatisticFilterButton(
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                viewModel.setFilter(filter)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Statistik
        Text(
            text = if (statisticType == "pages") "Halaman yang dibaca" else "Buku yang selesai",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = abu3
        )
        Text(
            text = if (statisticType == "pages") totalPagesRead.toString() else totalBooksFinished.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = abu3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bar Chart
        BarChart(
            data = if (statisticType == "pages") pagesReadData else booksFinishedData,
            isScrollable = selectedFilter == "day",
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Book History atau Reading History
        Text(
            text = if (statisticType == "pages") "Reading History" else "Finished Books",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = abu3
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f, fill = false)) {
            if (statisticType == "pages") {
                if (readingHistory.isEmpty()) {
                    Text("No reading history available.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(readingHistory) { entry ->
                            ReadingHistoryItem(entry = entry)
                        }
                    }
                }
            } else {
                if (finishedBooks.isEmpty()) {
                    Text("No finished books available.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(finishedBooks) { book ->
                            BookHistoryItem(book = book)
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
                color = abu3
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
    entry: ReadingHistoryEntry
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, hijau4, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(entry.book.coverPhotoPath),
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
                text = entry.book.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = abu3
            )
            Text(
                text = "Read up to page ${entry.progress.pageRead}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            entry.progress.recordedAt?.let { date ->
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
            // ## DIPERBAIKI: Typo 'Imagepainter' menjadi 'ImagePainter' ##
            painter = rememberAsyncImagePainter(book.coverPhotoPath),
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
                color = abu3
            )
            Text(
                text = "by ${book.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}