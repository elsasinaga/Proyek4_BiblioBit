package com.example.bibliobit.ui.readingprogress

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourReadingBookScreen(
    userLibraryId: Long,
    viewModel: ReadingProgressViewModel,
    // Navigasi ditangani oleh callback, bukan NavController langsung
    onNavigateToAddProgress: (userLibraryId: Long, bookTitle: String, totalPages: Int) -> Unit,
    onNavigateToSeeProgress: (userLibraryId: Long, bookTitle: String, totalPages: Int) -> Unit,
    onNavigateToNotes: (userLibraryId: Long, bookTitle: String) -> Unit
) {
    val book by viewModel.book.collectAsState()
    val userLibrary by viewModel.userLibrary.collectAsState()
    val firstReadingProgress by viewModel.firstReadingProgress.collectAsState()
    val daysBetweenStartAndLast by viewModel.daysBetweenStartAndLast.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = userLibraryId) {
        viewModel.initialize(userLibraryId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = hijau5)
        }
        return
    }

    error?.let {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        return
    }

    if (book == null || userLibrary == null || userLibrary?.status != BookStatus.READING) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "This book is not in your reading list.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Cover dan Info Progres
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = rememberAsyncImagePainter(book?.coverPhotoPath ?: ""),
                contentDescription = "Book Cover",
                modifier = Modifier.width(170.dp).height(255.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgressInfoCard(title = "Your progress reading", value = "${userLibrary?.lastPageRead ?: 0} / ${book?.pages ?: 0}")
                ProgressInfoCard(
                    title = "From ${firstReadingProgress?.recordedAt?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "N/A"}",
                    value = "$daysBetweenStartAndLast Day(s)"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Judul Buku
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = book?.title ?: "Unknown Title",
                style = MaterialTheme.typography.titleLarge,
                color = hitam,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { isFavorite = !isFavorite }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else hijau5
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "by ${book?.author ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Sinopsis
        Text("Sinopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = hitam)
        Spacer(modifier = Modifier.height(8.dp))
        Text(book?.description ?: "No description available.", style = MaterialTheme.typography.bodyMedium, color = abu2)

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Aksi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button1(
                onClick = {
                    onNavigateToAddProgress(userLibrary?.id!!, book?.title ?: "No Title", book?.pages ?: 0)
                },
                modifier = Modifier
                    .weight(1f).height(50.dp)
            ) {
                Text("Edit Progress", style = MaterialTheme.typography.labelSmall)
            }
            Button1(
                onClick = {
                    onNavigateToSeeProgress(userLibrary?.id!!, book?.title ?: "No Title", book?.pages ?: 0)
                },
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("See Progress", style = MaterialTheme.typography.labelSmall)
            }
            Button1(
                onClick = {
                    onNavigateToNotes(userLibrary?.id!!, book?.title ?: "No Title")
                },
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Notes", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProgressInfoCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = hijau4.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = hitam, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = hijau5, fontWeight = FontWeight.Bold)
        }
    }
}