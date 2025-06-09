package com.example.bibliobit.ui.yourfinishbook

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.RatingBar
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun YourFinishBookScreen(
    userId: String,
    bookId: Long,
    viewModel: YourFinishBookViewModel,
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    // ## DIPERBAIKI: Gunakan satu state dari ViewModel ##
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book
    val userLibrary = uiState.userLibrary

    var isFavorite by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = bookId) {
        viewModel.loadData(bookId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (book == null || userLibrary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Book data not found.")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ... (Kode untuk menampilkan Cover, Rating, Judul, Deskripsi, dll tidak berubah)
        // ... (Anda bisa salin dari file lama Anda, pastikan menggunakan `book` dan `userLibrary` dari uiState)

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(book.coverPhotoPath),
                contentDescription = "Book Cover",
                modifier = Modifier.width(170.dp).height(255.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, hijau4, RoundedCornerShape(8.dp)),
                color = hijau2,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Rating", style = MaterialTheme.typography.bodySmall, color = hitam)
                    Spacer(modifier = Modifier.height(4.dp))
                    RatingBar(rating = userLibrary.rating ?: 0f, modifier = Modifier.height(20.dp))
                }
            }
        }
        // ... Sisa UI lainnya ...

        Spacer(modifier = Modifier.height(16.dp))

        // ## DIPERBAIKI: Tombol Aksi dengan logika baru ##
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tombol Add/Edit Rating
            Button(
                onClick = {
                    navController.navigate(Screen.AddYourRating.createRoute(userId, bookId, book.title))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Edit Rating")
            }

            // Tombol Read Again
            Button(
                onClick = {
                    scope.launch {
                        val updatedLibraryItem = viewModel.readAgain()
                        if (updatedLibraryItem != null) {
                            Toast.makeText(context, "Book set to Reading. Progress reset!", Toast.LENGTH_SHORT).show()
                            // Navigasi dengan userLibraryId yang baru/diperbarui
                            navController.navigate(Screen.YourReadingBook.createRoute(updatedLibraryItem.id!!)) {
                                popUpTo(Screen.Home.route) // Kembali ke Home setelah aksi
                            }
                        } else {
                            Toast.makeText(context, "Failed to update status.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Read Again")
            }

            // Tombol Notes
            Button(
                onClick = {
                    navController.navigate(Screen.Notes.createRoute(userLibrary.id!!, book.title))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Notes")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}