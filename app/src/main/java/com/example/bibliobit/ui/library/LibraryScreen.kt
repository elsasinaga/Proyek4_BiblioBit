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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    var isDeleteMode by remember { mutableStateOf(false) }

    val selectedFilter by viewModel.filter.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ===================================================================
    // PERBAIKAN: Logika untuk menampilkan loading indicator secara langsung
    // ===================================================================

    // State lokal untuk menandakan kita baru saja kembali ke layar ini
    var shouldShowLoadingOnResume by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Saat kembali, NYALAKAN state loading lokal & panggil refresh
                shouldShowLoadingOnResume = true
                viewModel.refreshLibrary()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Efek ini akan mematikan loading lokal setelah ViewModel selesai bekerja
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            shouldShowLoadingOnResume = false
        }
    }
    // ===================================================================


    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.setUserId(userId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ... Sisa kode UI (FilterBar, SearchBar) tidak berubah ...
        val selectedFilterKey = when (selectedFilter) {
            BookStatus.PLAN_TO_READ -> "wishlist"
            BookStatus.READING -> "reading"
            BookStatus.FINISH -> "finish"
            null -> "all"
        }

        FilterBar(
            selectedFilter = selectedFilterKey,
            onFilterSelected = { filterKey ->
                val newStatus = when (filterKey) {
                    "wishlist" -> BookStatus.PLAN_TO_READ
                    "reading" -> BookStatus.READING
                    "finish" -> BookStatus.FINISH
                    else -> null
                }
                viewModel.setFilter(newStatus)
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it)
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

        // ===================================================================
        // PERBAIKAN: Kondisi untuk menampilkan UI utama
        // ===================================================================
        // Tampilkan loading jika ViewModel sedang loading ATAU jika kita baru kembali ke layar ini
        if (isLoading || shouldShowLoadingOnResume) {
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
            // Tampilkan daftar buku jika tidak loading
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(libraryItems, key = { it.id!! }) { userLibrary ->
                    val book = userLibrary.book ?: return@items
                    Box(
                        modifier = Modifier.clickable {
                            if (!isDeleteMode) {
                                when (userLibrary.status) {
                                    BookStatus.READING -> navController.navigate(Screen.YourReadingBook.createRoute(userLibrary.id!!))
                                    BookStatus.FINISH -> navController.navigate(Screen.YourFinishBook.createRoute(book.id))
                                    BookStatus.PLAN_TO_READ -> navController.navigate(Screen.YourWishlistBook.createRoute(book.id))
                                }
                            }
                        }
                    ) {
                        when (userLibrary.status) {
                            BookStatus.PLAN_TO_READ -> WishlistBookItem(book = book, showDeleteButton = isDeleteMode, onDelete = { viewModel.deleteBookFromLibrary(userLibrary.id!!) })
                            BookStatus.READING -> ReadingBookItem(book = book, lastPageRead = userLibrary.lastPageRead ?: 0, totalPages = book.pages, showDeleteButton = isDeleteMode, onDelete = { viewModel.deleteBookFromLibrary(userLibrary.id!!) })
                            BookStatus.FINISH -> FinishBookItem(book = book, rating = userLibrary.rating ?: 0f, showDeleteButton = isDeleteMode, onDelete = { viewModel.deleteBookFromLibrary(userLibrary.id!!) })
                        }
                    }
                }
            }
        }
    }
}