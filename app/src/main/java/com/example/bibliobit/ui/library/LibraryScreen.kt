package com.example.bibliobit.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.ui.components.FilterBar
import com.example.bibliobit.ui.components.FinishBookItem
import com.example.bibliobit.ui.components.ReadingBookItem
import com.example.bibliobit.ui.components.WishlistBookItem
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.Typography
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel,
    navController: NavHostController
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    var isDeleteMode by remember { mutableStateOf(false) }

    // Ambil userId dari FirebaseAuth
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.setUserId(userId)
        }
    }

    // Gunakan LaunchedEffect untuk mengontrol pemanggilan filter dan search
    LaunchedEffect(selectedFilter, searchQuery) {
        viewModel.setFilter(selectedFilter)
        viewModel.setSearchQuery(searchQuery)
    }

    // Ambil data dari ViewModel
    val libraryItems by viewModel.libraryItems.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Filter Bar
        FilterBar(
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                selectedFilter = filter
                viewModel.setFilter(filter)
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it)
                },
                modifier = Modifier
                    .weight(1f), // Mengambil sisa ruang
                placeholder = { Text("Search books...", style = Typography.bodyLarge) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Tombol Hapus di Samping Search Bar
            IconButton(
                onClick = { isDeleteMode = !isDeleteMode },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = if (isDeleteMode) "Cancel Delete Mode" else "Enter Delete Mode",
                    tint = if (isDeleteMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Book List
        if (libraryItems.isEmpty()) {
            Text(
                text = "No books in library",
                style = Typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(libraryItems) { (book, userLibrary) ->
                    Box(
                        modifier = Modifier
                            .clickable {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                when (userLibrary.status) {
                                    BookStatus.PLAN_TO_READ -> {
                                        navController.navigate(
                                            Screen.YourWishlistBook.createRoute(userId, book.id)
                                        )
                                    }
                                    BookStatus.READING -> {
                                        navController.navigate(
                                            Screen.YourReadingBook.createRoute(userId, book.id)
                                        )
                                    }
                                    BookStatus.FINISH -> {
                                        navController.navigate(
                                            Screen.YourFinishBook.createRoute(userId, book.id)
                                        ) // Ubah navigasi ke YourFinishBookScreen
                                    }
                                }
                            }
                    ) {
                        when (userLibrary.status) {
                            BookStatus.PLAN_TO_READ -> WishlistBookItem(
                                book = book,
                                showDeleteButton = isDeleteMode,
                                onDelete = { viewModel.deleteBookFromLibrary(book.id) })
                            BookStatus.READING -> ReadingBookItem(
                                book = book,
                                lastPageRead = userLibrary.lastPageRead ?: 0,
                                totalPages = book.pages ?: 300,
                                showDeleteButton = isDeleteMode,
                                onDelete = { viewModel.deleteBookFromLibrary(book.id) }
                            )
                            BookStatus.FINISH -> FinishBookItem(
                                book = book,
                                rating = userLibrary.rating ?: 0f,
                                showDeleteButton = isDeleteMode,
                                onDelete = { viewModel.deleteBookFromLibrary(book.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}