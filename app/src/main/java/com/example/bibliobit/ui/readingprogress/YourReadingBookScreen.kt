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
    modifier: Modifier = Modifier,
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

    // Memuat data saat screen pertama kali dibuka
    LaunchedEffect(key1 = userLibraryId) {
        viewModel.initialize(userLibraryId)
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = hijau5)
        }
        return
    }

    error?.let {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        return
    }

    if (book == null || userLibrary == null || userLibrary?.status != BookStatus.READING) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Buku ini tidak dalam daftar bacaan Anda.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    // Main Content
    Column(
        modifier = modifier
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
            // Logika untuk menampilkan cover atau placeholder "No Cover"
            if (book?.coverPhotoPath.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier
                        .width(170.dp)
                        .height(255.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "No Cover",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Image(
                    painter = rememberAsyncImagePainter(book?.coverPhotoPath ?: ""),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .width(170.dp)
                        .height(255.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgressInfoCard(title = "Progres Anda", value = "${userLibrary?.lastPageRead ?: 0} / ${book?.pages ?: 0}")
                ProgressInfoCard(
                    title = "Dari ${firstReadingProgress?.recordedAt?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: "N/A"}",
                    value = "$daysBetweenStartAndLast Hari"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Judul Buku dan Favorit
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = book?.title ?: "Judul Tidak Diketahui",
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
        Text(text = "oleh ${book?.author ?: "Penulis Tidak Diketahui"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text(
            text = "Diterbitkan oleh ${book?.publisher ?: "Tidak Diketahui"}",
            style = MaterialTheme.typography.bodyMedium,
            color = hijau5
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Deskripsi (Sinopsis)
        Text(book?.description ?: "Tidak ada deskripsi.", style = MaterialTheme.typography.bodyLarge, color = abu2)

        Spacer(modifier = Modifier.height(16.dp))

        // Informasi Buku (ditambahkan dari screen lain)
        Text(
            text = "Informasi Buku",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = hitam
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Genre: ${book?.genre ?: "Tidak Diketahui"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("Jumlah Halaman: ${book?.pages ?: "Tidak Diketahui"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("Tanggal Terbit: ${book?.year ?: "Tidak Diketahui"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("ISBN: ${book?.isbn ?: "Tidak Diketahui"}", style = MaterialTheme.typography.bodyLarge, color = abu2)

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
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Edit Progres", style = MaterialTheme.typography.labelSmall)
            }
            Button1(
                onClick = {
                    onNavigateToSeeProgress(userLibrary?.id!!, book?.title ?: "No Title", book?.pages ?: 0)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Lihat Progres", style = MaterialTheme.typography.labelSmall)
            }
            Button1(
                onClick = {
                    onNavigateToNotes(userLibrary?.id!!, book?.title ?: "No Title")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Catatan", style = MaterialTheme.typography.labelSmall)
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