// File: ui/yourwishlistbook/YourWishlistBookScreen.kt

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.components.Button1 // <-- Pastikan import ini ada
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam

@Composable
fun YourWishlistBookScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    viewModel: YourWishlistBookViewModel = hiltViewModel(),
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book
    val userLibrary = uiState.userLibrary

    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = bookId) {
        viewModel.loadData(bookId)
    }

    LaunchedEffect(key1 = uiState.startReadingSuccess) {
        if (uiState.startReadingSuccess) {
            val newLibraryId = uiState.newLibraryId
            if (newLibraryId != null) {
                navController.navigate(Screen.YourReadingBook.createRoute(newLibraryId)) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            } else {
                onNavigateBack()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}")
                }
            }
            book == null || userLibrary == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Book not found in your wishlist.")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    if (book.coverPhotoPath.isNullOrEmpty()) {
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
                    } else {
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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

                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = hitam
                    )
                    Text(
                        text = "Published by ${book.publisher ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = hijau5
                    )

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(
                        text = book.description ?: "No description available.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = abu2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Book Info",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = hitam
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Genre: ${book.genre ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
                    Text("Number of Pages: ${book.pages ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
                    Text("Date Published: ${book.year ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
                    Text("ISBN: ${book.isbn ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)

                    Spacer(modifier = Modifier.height(32.dp))

                    // ===================================================================
                    // Tombol Aksi (DIUBAH MENGGUNAKAN Button1)
                    // ===================================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        Button1(
                            onClick = { viewModel.startReading() },
                            modifier = Modifier.width(150.dp).height(48.dp)
                        ) {
                            Text("Start Reading!")
                        }

                        // Menggunakan Button1 juga untuk "Add Notes" sesuai permintaan
                        // Jika ingin tampilan berbeda, Anda bisa gunakan Button2
                        Button1(
                            onClick = {
                                userLibrary.id?.let {
                                    navController.navigate(Screen.Notes.createRoute(it, book.title))
                                }
                            },
                            modifier = Modifier.width(150.dp).height(48.dp)
                        ) {
                            Text("Add Notes")
                        }
                    }
                    // ===================================================================

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}