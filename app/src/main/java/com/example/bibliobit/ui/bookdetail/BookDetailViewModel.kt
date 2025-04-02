package com.example.bibliobit.ui.bookdetail

import androidx.lifecycle.ViewModel
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    fun getBookById(bookId: Long): Flow<Book?> {
        return bookRepository.getBookById(bookId)
    }
}