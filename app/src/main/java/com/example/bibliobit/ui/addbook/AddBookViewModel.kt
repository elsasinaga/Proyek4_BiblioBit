package com.example.bibliobit.ui.addbook

//import androidx.constraintlayout.helper.widget.Flow
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.ViewModel
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    // Daftar buku yang diambil dari BookRepository
    val books: Flow<List<Book>> = bookRepository.getAllBooks()

    suspend fun insertBook(book: Book) {
        bookRepository.insertBook(book)
    }
}