package com.example.bibliobit.ui.library

import com.example.bibliobit.data.model.Book

// Model sementara untuk menyimpan informasi status buku
data class LibraryItem(
    val book: Book,
    val status: String, // "wishlist", "reading", atau "finish"
    val lastPageRead: Int? = null, // Untuk status "reading"
    val rating: Float? = null // Untuk status "finish"
)

// Fungsi untuk menghasilkan data dummy
fun getDummyLibraryItems(): List<LibraryItem> {
    val books = listOf(
        Book(
            id = 1,
            title = "The Great Gatsby",
            author = "F. Scott Fitzgerald",
            genre = "Classic",
            year = 1925,
            description = "A novel about the American dream.",
            isbn = "978-0743273565",
            pages = 180,
            publisher = "Scribner",
            coverPhotoPath = null
        ),
        Book(
            id = 2,
            title = "1984",
            author = "George Orwell",
            genre = "Dystopia",
            year = 1949,
            description = "A dystopian novel about totalitarianism.",
            isbn = "978-0451524935",
            pages = 328,
            publisher = "Penguin",
            coverPhotoPath = null
        ),
        Book(
            id = 3,
            title = "To Kill a Mockingbird",
            author = "Harper Lee",
            genre = "Fiction",
            year = 1960,
            description = "A novel about racial injustice.",
            isbn = "978-0446310789",
            pages = 281,
            publisher = "Grand Central Publishing",
            coverPhotoPath = null
        ),
        Book(
            id = 4,
            title = "Pride and Prejudice",
            author = "Jane Austen",
            genre = "Romance",
            year = 1813,
            description = "A romantic novel about the Bennet sisters.",
            isbn = "978-0141439518",
            pages = 432,
            publisher = "Penguin Classics",
            coverPhotoPath = null
        )
    )

    return listOf(
        LibraryItem(
            book = books[0],
            status = "wishlist"
        ),
        LibraryItem(
            book = books[1],
            status = "reading",
            lastPageRead = 150
        ),
        LibraryItem(
            book = books[2],
            status = "finish",
            rating = 4.5f
        ),
        LibraryItem(
            book = books[3],
            status = "reading",
            lastPageRead = 200
        )
    )
}