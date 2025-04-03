package com.example.bibliobit.ui.library

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor() : ViewModel() {

    private val _filter = MutableStateFlow("all")
    private val _searchQuery = MutableStateFlow("")
    private val dummyData = getDummyLibraryItems()

    // Flow untuk daftar buku yang akan ditampilkan
    val libraryItems: Flow<List<LibraryItem>> = combine(
        _filter,
        _searchQuery
    ) { filter, query ->
        // Filter berdasarkan status
        var filteredItems = if (filter == "all") {
            dummyData
        } else {
            dummyData.filter { it.status == filter }
        }

        // Filter berdasarkan pencarian
        if (query.isNotEmpty()) {
            filteredItems = filteredItems.filter {
                it.book.title.contains(query, ignoreCase = true) ||
                        it.book.author.contains(query, ignoreCase = true)
            }
        }

        filteredItems
    }

    fun setFilter(filter: String) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}