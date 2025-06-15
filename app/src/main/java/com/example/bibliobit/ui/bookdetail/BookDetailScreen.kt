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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.Button2
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    viewModel: BookDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val book by viewModel.book.collectAsState()
    val userLibrary by viewModel.userLibrary.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = bookId) {
        viewModel.loadBookDetails(bookId)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (uiState.isLoading && book == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Box
        }

        uiState.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return@Box
        }

        book?.let { currentBook ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Cover Buku
                val painter = rememberAsyncImagePainter(model = currentBook.coverPhotoPath)
                if (painter.state is coil.compose.AsyncImagePainter.State.Error || currentBook.coverPhotoPath.isNullOrEmpty()) {
                    Surface(modifier = Modifier.width(200.dp).height(300.dp).clip(RoundedCornerShape(8.dp)).align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) {
                        Box(contentAlignment = Alignment.Center) { Text(text = "No Cover", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                    }
                } else {
                    Image(painter = painter, contentDescription = currentBook.title, modifier = Modifier.width(200.dp).height(300.dp).clip(RoundedCornerShape(8.dp)).align(Alignment.CenterHorizontally), contentScale = ContentScale.Crop)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Judul dan Tombol Aksi
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = currentBook.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = hitam, modifier = Modifier.weight(1f))
                    Row {
                        IconButton(onClick = { isFavorite = !isFavorite }) { Icon(imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = if (isFavorite) MaterialTheme.colorScheme.error else hijau5) }
                        IconButton(onClick = { /* TODO */ }) { Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = hijau5) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Penerbit dan Tombol Share
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Published by ${currentBook.publisher ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = hijau5)
                    IconButton(onClick = { /* TODO */ }) { Icon(painter = painterResource(id = android.R.drawable.ic_menu_share), contentDescription = "Share", tint = hijau5) }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Sinopsis, Book Info, dan Status Buttons
                Text(text = "Sinopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = hitam)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentBook.description ?: "No description available.", style = MaterialTheme.typography.bodyMedium, color = abu2)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Book Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = hitam)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Author: ${currentBook.author}", style = MaterialTheme.typography.bodyMedium, color = abu2)
                Text(text = "Genre: ${currentBook.genre ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = abu2)
                Text(text = "Number of Pages: ${currentBook.pages ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = abu2)
                Text(text = "Date Published: ${currentBook.year ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = abu2)
                Text(text = "ISBN: ${currentBook.isbn ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = abu2)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "My Reading Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = hitam)
                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Status dengan perbaikan font
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statusMap = mapOf("Plan to Read" to BookStatus.PLAN_TO_READ, "Reading" to BookStatus.READING, "Finish" to BookStatus.FINISH)
                    statusMap.forEach { (label, status) ->
                        val isSelected = userLibrary?.status == status
                        val buttonModifier = Modifier.weight(1f).height(40.dp)
                        if (isSelected) {
                            Button1(onClick = { }, modifier = buttonModifier, enabled = true) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp // <-- Perbaikan Font
                                )
                            }
                        } else {
                            Button2(onClick = { if (userId != null && !uiState.isLoading) { viewModel.updateBookStatus(userId, currentBook.id, status) } }, modifier = buttonModifier, enabled = !uiState.isLoading) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp // <-- Perbaikan Font
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}