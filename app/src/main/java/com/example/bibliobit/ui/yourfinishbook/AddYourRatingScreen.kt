package com.example.bibliobit.ui.yourfinishbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.components.RatingBar
import com.example.bibliobit.ui.theme.abu2
import com.example.bibliobit.ui.theme.hijau1
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddYourRatingScreen(
    userId: String,
    bookId: Long,
    bookTitle: String,
    viewModel: AddYourRatingViewModel,
    onNavigateBack: () -> Unit
) {
    val readingProgress by viewModel.readingProgress.collectAsState()
    val firstReadingProgress by viewModel.firstReadingProgress.collectAsState()
    var rating by remember { mutableStateOf(0f) }

    // Load reading progress
    LaunchedEffect(bookId) {
        viewModel.loadReadingProgress(userId, bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Your Rating",
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = hijau1
                )
            )
        },
        containerColor = hijau1
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = bookTitle,
                style = MaterialTheme.typography.titleLarge,
                color = hitam,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Progress
            if (readingProgress.isEmpty() || firstReadingProgress == null) {
                Text(
                    text = "No reading progress yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = abu2,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                // First Dot: Start Reading Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = hijau5,
                        modifier = Modifier.size(16.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Day 1",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = hitam
                            )
                            Text(
                                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(firstReadingProgress!!.recordedAt),
                                style = MaterialTheme.typography.bodyMedium,
                                color = abu2
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start Reading!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = hitam
                        )
                    }
                }

                // Add a connecting line between the first and second dot
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .align(Alignment.Start)
                        .offset(x = 7.dp)
                        .background(hijau5)
                )

                // Remaining Progress Entries
                readingProgress.forEachIndexed { index, progress ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = hijau5,
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(progress.recordedAt),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = hitam
                                )
                                Spacer(modifier = Modifier.width(0.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val previousPage = if (index == 0) 0 else readingProgress[index - 1].pageRead
                            val pageDiff = progress.pageRead - previousPage
                            Text(
                                text = if (pageDiff >= 0) "Read ${progress.pageRead} Pages (+$pageDiff)" else "Read ${progress.pageRead} Pages ($pageDiff)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = hitam
                            )
                        }
                    }

                    if (index < readingProgress.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .align(Alignment.Start)
                                .offset(x = 7.dp)
                                .background(hijau5)
                        )
                    }
                }

                // Last Dot: I've read them all!
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .align(Alignment.Start)
                        .offset(x = 7.dp)
                        .background(hijau5)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = hijau5,
                        modifier = Modifier.size(16.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "I've read them all!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = hitam
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Rating Section
            Text(
                text = "What is the rating for this book?",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = hitam
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                RatingBar(
                    rating = rating,
                    modifier = Modifier.height(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 0f..5f,
                steps = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button1(
                onClick = {
                    viewModel.saveRating(userId, bookId, rating)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}