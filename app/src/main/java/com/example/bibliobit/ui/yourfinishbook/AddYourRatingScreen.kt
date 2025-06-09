package com.example.bibliobit.ui.yourfinishbook

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bibliobit.ui.components.RatingBar

@Composable
fun AddYourRatingScreen(
    userId: String, // userId dan bookId tetap dibutuhkan untuk mengambil data awal
    bookId: Long,
    bookTitle: String,
    viewModel: AddYourRatingViewModel,
    onNavigateBack: () -> Unit
) {
    // ## DIPERBAIKI: Hanya observe satu state utama dari ViewModel ##
    val uiState by viewModel.uiState.collectAsState()
    val userLibrary = uiState.userLibrary

    // State untuk menampung nilai rating yang akan diubah oleh pengguna
    // Nilai awal diambil dari data yang sudah ada, atau 0 jika belum ada rating
    var rating by remember(userLibrary?.rating) {
        mutableStateOf(userLibrary?.rating ?: 0f)
    }

    val context = LocalContext.current

    // Muat data userLibrary saat layar pertama kali dibuka
    LaunchedEffect(key1 = bookId) {
        viewModel.loadUserLibrary(bookId)
    }

    // LaunchedEffect untuk menangani setelah rating berhasil disimpan
    LaunchedEffect(key1 = uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Rating saved!", Toast.LENGTH_SHORT).show()
            onNavigateBack() // Kembali ke layar sebelumnya
        }
    }

    // Tampilkan loading indicator jika data sedang dimuat
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Tampilkan pesan error jika terjadi kesalahan
    uiState.error?.let {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    // Tampilan utama
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = bookTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "What is the rating for this book?",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Komponen RatingBar untuk menampilkan bintang
        RatingBar(
            rating = rating,
            modifier = Modifier.height(48.dp) // Beri ukuran lebih besar
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Slider untuk mengubah nilai rating
        Slider(
            value = rating,
            onValueChange = { rating = it },
            valueRange = 0f..5f,
            steps = 9, // 0.5, 1, 1.5, ..., 5.0 (10 step)
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = String.format("%.1f", rating), // Tampilkan nilai rating
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.weight(1f)) // Spacer untuk mendorong tombol ke bawah

        // Tombol simpan
        Button(
            onClick = {
                viewModel.saveRating(rating)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Save Rating")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}