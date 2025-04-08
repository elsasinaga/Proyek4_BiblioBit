package com.example.bibliobit.ui.yourfinishbook

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.RatingBar
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau1
import com.example.bibliobit.ui.theme.hijau2
import com.example.bibliobit.ui.theme.hijau4
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourFinishBookScreen(
    userId: String,
    bookId: Long,
    viewModel: YourFinishBookViewModel,
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    val book by viewModel.book.collectAsState(initial = null)
    val userLibrary by viewModel.userLibrary.collectAsState(initial = null)
    var isFavorite by remember { mutableStateOf(false) }

    // Load book and user library data
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
        viewModel.loadUserLibrary(userId, bookId)
    }

    if (book == null || userLibrary == null) {
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
            Spacer(modifier = Modifier.height(16.dp))

            // Cover Buku dan Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (book?.coverPhotoPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(book?.coverPhotoPath),
                        contentDescription = "Book Cover",
                        modifier = Modifier
                            .width(170.dp)
                            .height(255.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
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
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Your Rating dalam Kotak
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, hijau4, RoundedCornerShape(8.dp)),
                    color = hijau2,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Rating",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = hitam,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RatingBar(
                            rating = userLibrary?.rating ?: 0f,
                            modifier = Modifier.height(20.dp)
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

            // Tombol Add Rating, Read Again!, dan Add Notes
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button1(
                    onClick = {
                        navController.navigate(
                            Screen.AddYourRating.createRoute(userId, bookId, book?.title ?: "")
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Add Rating",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Button1(
                    onClick = {
                        viewModel.readAgain(userId, bookId)
                        navController.navigate(
                            Screen.YourReadingBook.createRoute(userId, bookId)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Read Again!",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Button1(
                    onClick = { /* Add Notes logic will be implemented later */ },
                    modifier = Modifier
                        .weight(1f)
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