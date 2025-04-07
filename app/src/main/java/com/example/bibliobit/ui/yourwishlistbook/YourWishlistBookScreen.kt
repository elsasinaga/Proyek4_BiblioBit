package com.example.bibliobit.ui.yourwishlistbook

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourWishlistBookScreen(
    userId: String,
    bookId: Long,
    viewModel: YourWishlistBookViewModel,
    onNavigateBack: () -> Unit
) {
    val book by viewModel.book.collectAsState(initial = null)
    var isFavorite by remember { mutableStateOf(false) }

    // Load book data
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    if (book == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Wishlist Book",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        ),
                        color = hitam,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = hitam
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

            // Judul dan Tombol Favorit
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
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else hijau5
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Publisher
            Text(
                text = book?.publisher ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge,
                color = hijau5
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Tombol Start Reading dan Add Notes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally), // Memastikan tombol berada di tengah
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button1(
                    onClick = {
                        viewModel.startReading(userId, bookId)
                        // Optional: Navigate back or to another screen after starting reading
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .width(120.dp) // Atur lebar tetap untuk memperkecil tombol
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Start Reading!",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Button1(
                    onClick = { /* Add Notes logic will be implemented later */ },
                    modifier = Modifier
                        .width(120.dp) // Atur lebar tetap untuk memperkecil tombol
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Add Notes",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}