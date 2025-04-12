package com.example.bibliobit.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.ui.profile.ProfileViewModel
import com.example.bibliobit.ui.readingprogress.ReadingProgressViewModel
import com.example.bibliobit.ui.readingprogress.ReadingBook
import com.example.bibliobit.utils.ReadingStreak
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    readingStreak: ReadingStreak,
    onNavigateToReadingBook: (userId: String, bookId: Long) -> Unit = { _, _ -> },
    profileViewModel: ProfileViewModel = hiltViewModel(),
    readingProgressViewModel: ReadingProgressViewModel = hiltViewModel()
) {
    var currentStreak by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("User") }
    var profileImage by remember { mutableStateOf<String?>(null) } // State untuk URL foto profil
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Ambil data profil dari ProfileViewModel
    val profileData by profileViewModel.profileData.observeAsState()

    // Ambil daftar buku yang sedang dibaca dari ReadingProgressViewModel
    val readingBooks by readingProgressViewModel.readingBooks.collectAsState(initial = emptyList())

    LaunchedEffect(userId, profileData) {
        if (userId != null) {
            // Ambil reading streak
            scope.launch {
                currentStreak = readingStreak.getCurrentStreak(userId)
            }

            // Ambil nama dan URL foto profil dari profileData
            profileData?.let { profile ->
                userName = profile.name
                profileImage = profile.profileImage // Ambil profileImage dari model User
                Log.d("HomeScreen", "User name: $userName, Profile image URL: $profileImage")
            } ?: run {
                Log.d("HomeScreen", "Profile data not available")
            }

            // Ambil daftar buku yang sedang dibaca
            readingProgressViewModel.loadReadingBooks(userId)
        } else {
            Log.d("HomeScreen", "No user logged in")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Baris untuk teks "Hello", "Hi, [nama_pengguna]", dan foto profil
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kolom untuk teks "Hello" dan "Hi, [nama_pengguna]"
            Column(
                modifier = Modifier.weight(1f) // Mengambil ruang yang tersedia
            ) {
                // Teks "Hello," (normal, tipis)
                Text(
                    text = "Hello,",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Teks "Hi, [nama_pengguna]" (besar, bold)
                Text(
                    text = "Hi, $userName",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Foto profil
            if (profileImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImage),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(48.dp) // Ukuran foto profil
                        .clip(CircleShape), // Membuat foto berbentuk lingkaran
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder jika foto profil tidak tersedia
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.firstOrNull()?.toString() ?: "U", // Ambil inisial nama
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card untuk Reading Streak
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50) // Warna hijau untuk card
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Teks "Reading Streak" (putih, bold, rata kiri)
                Text(
                    text = "Reading Streak",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                // Baris untuk emoji api dan angka streak
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Emoji api
                    Text(
                        text = "\uD83D\uDD25",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Angka streak
                    Text(
                        text = "$currentStreak days",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bagian "Continue Reading" dengan tombol panah kanan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Continue Reading",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            if (readingBooks.isNotEmpty()) {
                IconButton(
                    onClick = {
                        val firstBook = readingBooks.first()
                        userId?.let { uid ->
                            onNavigateToReadingBook(uid, firstBook.bookId)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Continue Reading",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Daftar buku yang sedang dibaca (horizontal scroll)
        if (readingBooks.isEmpty()) {
            Text(
                text = "No books currently being read.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(readingBooks) { readingBook ->
                    ReadingBookItem(
                        bookTitle = readingBook.bookTitle,
                        coverPhotoPath = readingBook.coverPhotoPath,
                        lastPageRead = readingBook.lastPageRead,
                        totalPages = readingBook.totalPages,
                        onClick = {
                            userId?.let { uid ->
                                onNavigateToReadingBook(uid, readingBook.bookId)
                            }
                        }
                    )
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
    totalPages: Int?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cover buku
        if (coverPhotoPath != null) {
            Image(
                painter = rememberAsyncImagePainter(coverPhotoPath),
                contentDescription = "Book Cover",
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Judul buku
        Text(
            text = bookTitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Progres membaca
        Text(
            text = "${lastPageRead ?: "Unknown"} / ${totalPages ?: "Unknown"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = {
                if (lastPageRead != null && totalPages != null && totalPages > 0) {
                    lastPageRead.toFloat() / totalPages
                } else {
                    0f
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color(0xFF4CAF50),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}