package com.example.bibliobit.ui.bookdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.Button2
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: BookDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Ambil semua state yang dibutuhkan dari ViewModel
    val book by viewModel.book.collectAsState()
    val userLibrary by viewModel.userLibrary.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Panggil fungsi untuk memuat data saat layar pertama kali ditampilkan
    LaunchedEffect(key1 = bookId) {
        viewModel.loadBookDetails(bookId)
    }

    // --- UI untuk Loading dan Error ---
    if (uiState.isLoading && book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    uiState.error?.let { error ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    // --- UI Utama ---
    // Tampilkan hanya jika data buku sudah berhasil dimuat
    book?.let { currentBook ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Cover Buku
            Image(
                painter = rememberAsyncImagePainter(model = currentBook.coverPhotoPath),
                contentDescription = currentBook.title,
                modifier = Modifier
                    .width(200.dp)
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Judul
            Text(
                text = currentBook.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = hitam,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Penulis & Penerbit
            Text(
                text = "by ${currentBook.author}",
                style = MaterialTheme.typography.bodyLarge,
                color = hitam
            )
            Text(
                text = "Published by ${currentBook.publisher ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
                color = hijau5
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Sinopsis
            Text(
                text = "Sinopsis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = hitam
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentBook.description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                color = abu2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Status (Bagian Interaktif)
            Text(
                text = "My Reading Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = hitam
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusMap = mapOf(
                    "Plan to Read" to BookStatus.PLAN_TO_READ,
                    "Reading" to BookStatus.READING,
                    "Finish" to BookStatus.FINISH
                )

                statusMap.forEach { (label, status) ->
                    val isSelected = userLibrary?.status == status

                    // ## INI BAGIAN YANG DIPERBAIKI ##
                    // Gunakan if/else untuk memanggil Composable secara langsung.
                    if (isSelected) {
                        Button1(
                            onClick = {
                                if (userId != null && !uiState.isLoading) {
                                    viewModel.updateBookStatus(userId, currentBook.id, status)
                                }
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            enabled = !uiState.isLoading
                        ) {
                            // Tampilkan loading indicator hanya di tombol yang sedang diupdate
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(text = label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        Button2(
                            onClick = {
                                if (userId != null && !uiState.isLoading) {
                                    viewModel.updateBookStatus(userId, currentBook.id, status)
                                }
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            enabled = !uiState.isLoading
                        ) {
                            Text(text = label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}