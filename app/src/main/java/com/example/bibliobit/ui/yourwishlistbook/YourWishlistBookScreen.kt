package com.example.bibliobit.ui.yourwishlistbook

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam

@Composable
fun YourWishlistBookScreen(
    userId: String, // userId tetap ada untuk navigasi jika diperlukan nanti
    bookId: Long,
    viewModel: YourWishlistBookViewModel,
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    // ## DIPERBAIKI: Mengamati satu state utama ##
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book
    val userLibrary = uiState.userLibrary

    var isFavorite by remember { mutableStateOf(false) }

    // ## DIPERBAIKI: LaunchedEffect disederhanakan untuk memuat semua data ##
    LaunchedEffect(key1 = bookId) {
        viewModel.loadData(bookId)
    }

    // LaunchedEffect untuk menangani navigasi setelah status buku diubah
    LaunchedEffect(key1 = uiState.startReadingSuccess) {
        if (uiState.startReadingSuccess) {
            // Navigasi ke halaman buku yang sedang dibaca setelah status berhasil diubah
            val newLibraryId = uiState.userLibrary?.id
            if (newLibraryId != null) {
                navController.navigate(Screen.YourReadingBook.createRoute(newLibraryId)) {
                    popUpTo(Screen.Library.route) // Kembali ke library setelah aksi
                }
            } else {
                onNavigateBack() // Atau kembali saja jika ID tidak ditemukan
            }
        }
    }

    // ## DIPERBAIKI: Menambahkan penanganan state isLoading dan error ##
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (book == null || userLibrary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Book not found in your wishlist.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ## DIPERBAIKI: Mengakses data dari objek uiState.book dan uiState.userLibrary ##

        Spacer(modifier = Modifier.height(16.dp))

        // Cover Buku
        Image(
            painter = rememberAsyncImagePainter(book.coverPhotoPath),
            contentDescription = "Book Cover",
            modifier = Modifier
                .width(200.dp)
                .height(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Judul dan Tombol Favorit
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = book.title,
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
        Text(text = "by ${book.author}", style = MaterialTheme.typography.bodyLarge, color = hijau5)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = book.description ?: "No description available.", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Book Info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = hitam)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Genre: ${book.genre ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("Pages: ${book.pages}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        // ... Info buku lainnya ...

        Spacer(modifier = Modifier.height(32.dp))

        // Tombol Aksi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            // ## DIPERBAIKI: Logika tombol disederhanakan ##
            Button(
                onClick = { viewModel.startReading() },
                modifier = Modifier.width(150.dp)
            ) {
                Text("Start Reading!")
            }
            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.Notes.createRoute(userLibrary.id!!, book.title))
                },
                modifier = Modifier.width(150.dp)
            ) {
                Text("Add Notes")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}