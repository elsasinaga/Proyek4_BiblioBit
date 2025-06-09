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
import com.example.bibliobit.data.model.UserLibrary
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
    // ## PERBAIKAN ##: Gunakan BookStatus? untuk filter, null berarti "all"
    var selectedFilter by remember { mutableStateOf<BookStatus?>(null) }
    var isDeleteMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.setUserId(userId) // Panggil fungsi yang benar
        }
    }

    val libraryItems by viewModel.libraryItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Bar (Asumsi komponen ini bisa menerima BookStatus? dan event)
        LibraryFilterBar(
            selectedFilter = selectedFilter,
            onFilterSelected = { status ->
                selectedFilter = status
                viewModel.setFilter(status) // Panggil fungsi yang benar
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Search Bar dan Tombol Hapus
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it) // Panggil fungsi yang benar
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search my library...", style = Typography.bodyLarge) },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(
                onClick = { isDeleteMode = !isDeleteMode },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Toggle Delete Mode",
                    tint = if (isDeleteMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Loading Indicator atau Daftar Buku
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (libraryItems.isEmpty()) {
            Text(
                text = "No books found",
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(libraryItems, key = { it.id!! }) { userLibrary ->
                    // ## PERBAIKAN ##: Ambil book dari userLibrary object
                    val book = userLibrary.book ?: return@items // Lewati jika data buku tidak ada

                    Box(
                        modifier = Modifier.clickable {
                            if (!isDeleteMode) {
                                // Navigasi ke detail buku
                                navController.navigate(Screen.BookDetail.createRoute(book.id))
                            }
                        }
                    ) {
                        // ## PERBAIKAN ##: Gunakan userLibrary.status
                        when (userLibrary.status) {
                            BookStatus.PLAN_TO_READ -> WishlistBookItem(
                                book = book,
                                showDeleteButton = isDeleteMode,
                                // ## PERBAIKAN ##: Kirim userLibrary.id yang benar untuk dihapus
                                onDelete = { viewModel.deleteBookFromLibrary(userLibrary.id!!) }
                            )
                            BookStatus.READING -> ReadingBookItem(
                                book = book,
                                lastPageRead = userLibrary.lastPageRead ?: 0,
                                totalPages = book.pages,
                                showDeleteButton = isDeleteMode,
                                onDelete = { viewModel.deleteBookFromLibrary(userLibrary.id!!) }
                            )
                            BookStatus.FINISH -> FinishBookItem(
                                book = book,
                                rating = userLibrary.rating ?: 0f,
                                showDeleteButton = isDeleteMode,
                                onDelete = { viewModel.deleteBookFromLibrary(userLibrary.id!!) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contoh komponen FilterBar yang sudah disesuaikan.
 * Anda bisa menggantinya dengan komponen Anda sendiri.
 */
@Composable
fun LibraryFilterBar(
    selectedFilter: BookStatus?,
    onFilterSelected: (BookStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = mapOf(
        "All" to null,
        "Wishlist" to BookStatus.PLAN_TO_READ,
        "Reading" to BookStatus.READING,
        "Finish" to BookStatus.FINISH
    )

    ScrollableTabRow(
        selectedTabIndex = filters.values.indexOf(selectedFilter),
        modifier = modifier,
        edgePadding = 0.dp
    ) {
        filters.forEach { (label, status) ->
            Tab(
                selected = selectedFilter == status,
                onClick = { onFilterSelected(status) },
                text = { Text(label) }
            )
        }
    }
}