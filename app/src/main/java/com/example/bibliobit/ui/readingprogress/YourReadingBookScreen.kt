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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau4
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam

@Composable
fun YourReadingBookScreen(
    userLibraryId: Long,
    viewModel: ReadingProgressViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val book = uiState.book
    val userLibrary = uiState.userLibrary

    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = userLibraryId) {
        viewModel.loadData(userLibraryId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = hijau5)
        }
        return
    }

    if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    if (book != null && userLibrary != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = rememberAsyncImagePainter(book.coverPhotoPath),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .width(170.dp)
                        .height(255.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProgressInfoCard(title = "Your progress reading", value = "${userLibrary.lastPageRead ?: 0} / ${book.pages}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
            Text(text = "by ${book.author}", style = MaterialTheme.typography.bodyLarge, color = abu2)

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Sinopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(book.description ?: "No description available.", style = MaterialTheme.typography.bodyMedium, color = abu2)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    // ## DIPERBAIKI: Hapus dua argumen terakhir yang tidak perlu ##
                    navController.navigate(
                        Screen.AddReadingProgress.createRoute(
                            userLibraryId = userLibrary.id!!,
                            bookTitle = book.title,
                            totalPages = book.pages
                        )
                    )
                }, modifier = Modifier.weight(1f)) {
                    Text("Edit Progress")
                }
                Button(onClick = {
                    navController.navigate(Screen.YourProgressReading.createRoute(userLibrary.id!!, book.title, book.pages))
                }, modifier = Modifier.weight(1f)) {
                    Text("See Progress")
                }
                Button(onClick = {
                    navController.navigate(Screen.Notes.createRoute(userLibrary.id!!, book.title))
                }, modifier = Modifier.weight(1f)) {
                    Text("Notes")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Book data not found.", style = MaterialTheme.typography.bodyLarge, color = abu2)
        }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = hitam)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = hijau5, fontWeight = FontWeight.Bold)
        }
    }
}