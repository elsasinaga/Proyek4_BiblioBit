package com.example.bibliobit.ui.readingprogress

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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau1
import com.example.bibliobit.ui.theme.hijau2
import com.example.bibliobit.ui.theme.hijau4
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourReadingBookScreen(
    userId: String,
    bookId: Long,
    viewModel: ReadingProgressViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddProgress: (userLibraryId: Long, bookTitle: String, totalPages: Int, userId: String, bookId: Long) -> Unit,
    onNavigateToSeeProgress: (userLibraryId: Long) -> Unit,
    navController: NavController
) {
    val book by viewModel.book.collectAsState()
    val userLibrary by viewModel.userLibrary.collectAsState()
    val firstReadingProgress by viewModel.firstReadingProgress.collectAsState()
    val daysBetweenStartAndLast by viewModel.daysBetweenStartAndLast.collectAsState()
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(userId, bookId) {
        viewModel.initialize(userId, bookId)
    }

    if (book == null || userLibrary == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = hijau5
            )
        }
        return
    }

    if (userLibrary?.status != BookStatus.READING) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This book is not currently being read.",
                style = MaterialTheme.typography.bodyLarge,
                color = abu2
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(8.dp))

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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
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
                            text = "Your progress reading",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = hitam,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${userLibrary?.lastPageRead ?: 0}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = hijau5
                            )
                            Text(
                                text = " / ${book?.pages ?: 0}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = hijau5
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            text = "From ${
                                firstReadingProgress?.recordedAt?.let {
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                                } ?: "Not yet started"
                            }",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = hitam,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$daysBetweenStartAndLast",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = hijau5
                            )
                            Text(
                                text = " Day",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = hijau5
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            text = "Sinopsis",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = hitam
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = book?.description ?: "No description available.",
            style = MaterialTheme.typography.bodyLarge,
            color = abu2
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button1(
                onClick = {
                    val userLibrary = viewModel.userLibrary.value
                    val book = viewModel.book.value
                    if (userLibrary != null && book != null) {
                        onNavigateToAddProgress(
                            userLibrary.id,
                            book.title,
                            book.pages ?: 0,
                            userId,
                            bookId
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                backgroundColor = hijau4
            ) {
                Text(
                    text = "Edit Progress",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button1(
                onClick = {
                    val userLibrary = viewModel.userLibrary.value
                    if (userLibrary != null) {
                        onNavigateToSeeProgress(userLibrary.id)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                backgroundColor = hijau4
            ) {
                Text(
                    text = "See Progress",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button1(
                onClick = {
                    val userLibrary = viewModel.userLibrary.value
                    val book = viewModel.book.value
                    if (userLibrary != null && book != null) {
                        navController.navigate(
                            Screen.Notes.createRoute(userLibrary.id, book.title)
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                backgroundColor = hijau4
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