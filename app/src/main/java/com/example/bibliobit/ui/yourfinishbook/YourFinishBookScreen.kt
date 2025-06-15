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
    modifier: Modifier = Modifier, // 1. Tambahkan parameter modifier di sini
    onNavigateBack: () -> Unit
) {
    val book by viewModel.book.collectAsState()
    val userLibrary by viewModel.userLibrary.collectAsState()
    var isFavorite by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load book dan userLibrary data
    LaunchedEffect(key1 = bookId) {
        viewModel.loadBook(bookId)
        viewModel.loadUserLibrary(userId, bookId)
    }

    if (book == null || userLibrary == null) {
        Box(
            modifier = modifier.fillMaxSize(), // Gunakan modifier di sini juga
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier // 2. Terapkan modifier dari parameter ke Column utama
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
            Image(
                painter = rememberAsyncImagePainter(book?.coverPhotoPath ?: ""),
                contentDescription = "Book Cover",
                modifier = Modifier
                    .width(170.dp)
                    .height(255.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

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
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
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

        Text(
            text = book?.publisher ?: "Unknown",
            style = MaterialTheme.typography.bodyLarge,
            color = hijau5
        )

        Spacer(modifier = Modifier.height(16.dp))

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
        Text("Authors: ${book?.author ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("Genre: ${book?.genre ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("Number of Pages: ${book?.pages ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("Date Published: ${book?.year ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)
        Text("ISBN: ${book?.isbn ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge, color = abu2)

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Aksi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button1(
                onClick = {
                    navController.navigate(
                        Screen.AddYourRating.createRoute(userId, bookId, book?.title ?: "")
                    )
                },
                modifier = Modifier.weight(1f).height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Add Rating", style = MaterialTheme.typography.labelSmall)
            }
            Button1(
                onClick = {
                    scope.launch {
                        viewModel.readAgain(userId, bookId)
                        Toast.makeText(context, "Book set to Reading. Progress reset!", Toast.LENGTH_SHORT).show()

                        // Navigasi ke Reading Book Screen setelah status diubah
                        val updatedLibraryItem = viewModel.userLibrary.value
                        if (updatedLibraryItem != null) {
                            navController.navigate(Screen.YourReadingBook.createRoute(updatedLibraryItem.id!!)) {
                                popUpTo(Screen.Home.route) // Kembali ke Home setelah aksi
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Read Again!", style = MaterialTheme.typography.labelSmall)
            }
            Button1(
                onClick = {
                    val userLibraryId = userLibrary?.id ?: 0L
                    val bookTitle = book?.title ?: "Unknown Title"
                    navController.navigate(Screen.Notes.createRoute(userLibraryId, bookTitle))
                },
                modifier = Modifier.weight(1f).height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Add Notes", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
