package com.example.bibliobit.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.ui.library.LibraryViewModel
import com.example.bibliobit.ui.profile.ProfileViewModel
import com.example.bibliobit.utils.ReadingStreak
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    readingStreak: ReadingStreak,
    // ## DIPERBAIKI: Definisi callback disesuaikan untuk hanya menerima userLibraryId ##
    onNavigateToReadingBook: (userLibraryId: Long) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    var currentStreak by remember { mutableStateOf(0) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // ## DIPERBAIKI: Mengambil state dari ViewModel yang benar ##
    val readingBooks by libraryViewModel.libraryItems.collectAsState()
    val isLoading by libraryViewModel.isLoading.collectAsState()
    val errorMessage by libraryViewModel.errorMessage.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState() // Menggunakan uiState dari ProfileViewModel

    // LaunchedEffect untuk memuat data awal saat layar pertama kali ditampilkan
    LaunchedEffect(key1 = userId) {
        if (userId != null) {
            // Ambil data reading streak
            currentStreak = readingStreak.getCurrentStreak(userId)

            // Atur filter di LibraryViewModel untuk hanya mengambil buku dengan status "READING"
            // ProfileViewModel sudah auto-load dari init block-nya.
            libraryViewModel.setFilter(BookStatus.READING)
        } else {
            Log.d("HomeScreen", "No user logged in, cannot fetch data.")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Tambahkan scroll vertikal untuk konten
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // --- Bagian Header Profil ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello,",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light)
                )
                // ## DIPERBAIKI: Ambil nama dari profileUiState.user ##
                Text(
                    text = profileUiState.user?.name ?: "User",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            if (!profileUiState.user?.profileImage.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(profileUiState.user?.profileImage),
                    contentDescription = "Profile Photo",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = (profileUiState.user?.name?.firstOrNull()?.toString() ?: "U"))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Bagian Reading Streak ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Reading Streak",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "🔥", // Emoji api
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "$currentStreak days",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 32.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Bagian Continue Reading ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Continue Reading",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (readingBooks.isNotEmpty()) {
                IconButton(onClick = { /* Navigasi kini per item */ }) {
                    Icon(Icons.Default.ArrowForward, "See All")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Daftar Buku ---
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            readingBooks.isEmpty() -> {
                Text("You are not reading any books.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(readingBooks, key = { it.id!! }) { userLibrary ->
                        val book = userLibrary.book ?: return@items
                        ReadingBookItem(
                            bookTitle = book.title,
                            coverPhotoPath = book.coverPhotoPath,
                            lastPageRead = userLibrary.lastPageRead,
                            totalPages = book.pages,
                            onClick = {
                                // ## DIPERBAIKI: Kirim userLibrary.id yang sudah non-null ##
                                onNavigateToReadingBook(userLibrary.id!!)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingBookItem(
    bookTitle: String,
    coverPhotoPath: String?,
    lastPageRead: Int?,
    totalPages: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.width(120.dp).height(180.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (coverPhotoPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(coverPhotoPath),
                        contentDescription = "Book Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
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
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = bookTitle,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${lastPageRead ?: 0} / $totalPages pages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = if (lastPageRead != null && totalPages > 0) {
                lastPageRead.toFloat() / totalPages.toFloat()
            } else {
                0f
            },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF4CAF50),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}