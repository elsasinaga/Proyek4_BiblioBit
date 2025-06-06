package com.example.bibliobit.ui.bookdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.remote.RemoteDataSource
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.Button2
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: BookDetailViewModel = hiltViewModel(),
    remoteDataSource: RemoteDataSource, // Di-inject melalui Hilt, bukan hiltViewModel
    onNavigateBack: () -> Unit
) {
    val book by viewModel.getBookById(bookId).collectAsState(initial = null)
    val userLibrary by viewModel.userLibrary.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var isFavorite by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf<BookStatus?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Sinkronkan selectedStatus dengan userLibrary
    LaunchedEffect(userLibrary) {
        selectedStatus = userLibrary?.status
    }

    // Ambil userId dari FirebaseAuth
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Load userLibrary saat screen dimuat
    LaunchedEffect(userId, bookId) {
        if (userId != null) {
            viewModel.loadUserLibrary(userId, bookId)
        }
    }

    // Tampilkan loading
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Tampilkan error jika ada
    uiState.error?.let { error ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Tampilkan UI utama jika buku tersedia
    if (book == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cover Buku
        if (book?.coverPhotoPath != null) {
            Image(
                painter = rememberAsyncImagePainter(book?.coverPhotoPath),
                contentDescription = "Book Cover",
                modifier = Modifier
                    .width(200.dp)
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .width(200.dp)
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally),
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Judul dan Tombol Favorit + Tombol +
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = book?.title ?: "Unknown Title",
                style = MaterialTheme.typography.titleLarge,
                color = hitam,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else hijau5
                    )
                }
                IconButton(onClick = { /* Tambahkan logika untuk tombol + */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = hijau5
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Publisher dan Ikon Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = book?.publisher ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge,
                color = hijau5
            )
            IconButton(onClick = { /* Tambahkan logika untuk share */ }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_share),
                    contentDescription = "Share",
                    tint = hijau5
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sinopsis
        Text(
            text = "Sinopsis",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = hitam
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Deskripsi
        Text(
            text = book?.description ?: "No description available.",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Informasi Buku
        Text(
            text = "Book Info",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = hitam
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Authors: ${book?.author ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2
        )
        Text(
            text = "Genre: ${book?.genre ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2
        )
        Text(
            text = "Number of Pages: ${book?.pages ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2
        )
        Text(
            text = "Date Published: ${book?.year ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2
        )
        Text(
            text = "ISBN: ${book?.isbn ?: "Unknown"}",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status (Plan to Read, Reading, Finish)
        Text(
            text = "Status",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = hitam
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tombol "Plan to Read"
            if (selectedStatus == BookStatus.PLAN_TO_READ) {
                Button1(
                    onClick = {
                        selectedStatus = BookStatus.PLAN_TO_READ
                        if (userId != null) {
                            coroutineScope.launch {
                                viewModel.updateBookStatus(
                                    userId = userId,
                                    bookId = bookId,
                                    status = BookStatus.PLAN_TO_READ,
                                    onSyncRequired = { updatedUserLibrary ->
                                        remoteDataSource.updateBookStatus(updatedUserLibrary.bookId, updatedUserLibrary.status)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Plan to Read",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                Button2(
                    onClick = {
                        selectedStatus = BookStatus.PLAN_TO_READ
                        if (userId != null) {
                            coroutineScope.launch {
                                viewModel.updateBookStatus(
                                    userId = userId,
                                    bookId = bookId,
                                    status = BookStatus.PLAN_TO_READ,
                                    onSyncRequired = { updatedUserLibrary ->
                                        remoteDataSource.updateBookStatus(updatedUserLibrary.bookId, updatedUserLibrary.status)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Plan to Read",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Tombol "Reading"
            if (selectedStatus == BookStatus.READING) {
                Button1(
                    onClick = {
                        selectedStatus = BookStatus.READING
                        if (userId != null) {
                            coroutineScope.launch {
                                viewModel.updateBookStatus(
                                    userId = userId,
                                    bookId = bookId,
                                    status = BookStatus.READING,
                                    lastPageRead = userLibrary?.lastPageRead ?: 0,
                                    onSyncRequired = { updatedUserLibrary ->
                                        remoteDataSource.updateBookStatus(updatedUserLibrary.bookId, updatedUserLibrary.status)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Reading",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                Button2(
                    onClick = {
                        selectedStatus = BookStatus.READING
                        if (userId != null) {
                            coroutineScope.launch {
                                viewModel.updateBookStatus(
                                    userId = userId,
                                    bookId = bookId,
                                    status = BookStatus.READING,
                                    lastPageRead = userLibrary?.lastPageRead ?: 0,
                                    onSyncRequired = { updatedUserLibrary ->
                                        remoteDataSource.updateBookStatus(updatedUserLibrary.bookId, updatedUserLibrary.status)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Reading",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Tombol "Finish"
            if (selectedStatus == BookStatus.FINISH) {
                Button1(
                    onClick = {
                        selectedStatus = BookStatus.FINISH
                        if (userId != null) {
                            coroutineScope.launch {
                                viewModel.updateBookStatus(
                                    userId = userId,
                                    bookId = bookId,
                                    status = BookStatus.FINISH,
                                    rating = userLibrary?.rating ?: 0f,
                                    onSyncRequired = { updatedUserLibrary ->
                                        remoteDataSource.updateBookStatus(updatedUserLibrary.bookId, updatedUserLibrary.status)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Finish",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                Button2(
                    onClick = {
                        selectedStatus = BookStatus.FINISH
                        if (userId != null) {
                            coroutineScope.launch {
                                viewModel.updateBookStatus(
                                    userId = userId,
                                    bookId = bookId,
                                    status = BookStatus.FINISH,
                                    rating = userLibrary?.rating ?: 0f,
                                    onSyncRequired = { updatedUserLibrary ->
                                        remoteDataSource.updateBookStatus(updatedUserLibrary.bookId, updatedUserLibrary.status)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Finish",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}