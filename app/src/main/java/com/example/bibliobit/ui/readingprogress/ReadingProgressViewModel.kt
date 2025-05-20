// ReadingProgressViewModel.kt
package com.example.bibliobit.ui.readingprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.ReadingProgressRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ReadingBook(
    val bookId: Long,
    val bookTitle: String,
    val coverPhotoPath: String?,
    val lastPageRead: Int?,
    val totalPages: Int?
)

@HiltViewModel
class ReadingProgressViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
    private val bookRepository: BookRepository,
    private val readingProgressRepository: ReadingProgressRepository
) : ViewModel() {

    private val _userLibrary = MutableStateFlow<UserLibrary?>(null)
    val userLibrary: StateFlow<UserLibrary?> = _userLibrary.asStateFlow()

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _readingProgress = MutableStateFlow<List<ReadingProgress>>(emptyList())
    val readingProgress: StateFlow<List<ReadingProgress>> = _readingProgress.asStateFlow()

    private val _firstReadingProgress = MutableStateFlow<ReadingProgress?>(null)
    val firstReadingProgress: StateFlow<ReadingProgress?> = _firstReadingProgress.asStateFlow()

    private val _daysBetweenStartAndLast = MutableStateFlow<Long>(0L)
    val daysBetweenStartAndLast: StateFlow<Long> = _daysBetweenStartAndLast.asStateFlow()

    private val _readingBooks = MutableStateFlow<List<ReadingBook>>(emptyList())
    val readingBooks: StateFlow<List<ReadingBook>> = _readingBooks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var userId: String? = null
    private var bookId: Long? = null
    private var userLibraryId: Long? = null

    init {
        viewModelScope.launch {
            firstReadingProgress.combine(userLibrary) { firstProgress, userLibrary ->
                if (firstProgress == null || userLibrary == null) {
                    println("Cannot calculate days: firstProgress=$firstProgress, userLibrary=$userLibrary")
                    0L
                } else {
                    val startDate = firstProgress.recordedAt.time
                    val lastDate = userLibrary.updatedAt.time
                    val days = TimeUnit.MILLISECONDS.toDays(lastDate - startDate)
                    println("Calculated days between start and last: $days (startDate=${firstProgress.recordedAt}, lastDate=${userLibrary.updatedAt})")
                    days.coerceAtLeast(0L)
                }
            }.collect { days ->
                _daysBetweenStartAndLast.value = days
            }
        }
    }

    fun initialize(userId: String, bookId: Long) {
        this.userId = userId
        this.bookId = bookId
        viewModelScope.launch {
            val userLibrary = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
            if (userLibrary != null) {
                this@ReadingProgressViewModel.userLibraryId = userLibrary.id
                println("Initialized with userLibraryId: ${userLibrary.id}")
                loadReadingDataByUserLibraryId(userLibrary.id)
            } else {
                println("UserLibrary not found for userId=$userId, bookId=$bookId")
            }
        }
    }

    fun initializeWithUserLibraryId(userLibraryId: Long) {
        this.userLibraryId = userLibraryId
        loadReadingDataByUserLibraryId(userLibraryId)
    }

    private suspend fun loadUserLibrary(userId: String, bookId: Long): UserLibrary? {
        val userLibraryData = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
        println("Result of getUserLibraryByBookId(userId=$userId, bookId=$bookId): $userLibraryData")
        _userLibrary.value = userLibraryData
        println("Loaded UserLibrary: $userLibraryData")
        return userLibraryData
    }

    private suspend fun loadUserLibraryById(userLibraryId: Long): UserLibrary? {
        val userLibraryData = userLibraryRepository.getUserLibraryById(userLibraryId)
        println("Result of getUserLibraryById(userLibraryId=$userLibraryId): $userLibraryData")
        _userLibrary.value = userLibraryData
        println("Loaded UserLibrary: $userLibraryData")
        return userLibraryData
    }

    fun loadReadingData(userId: String, bookId: Long) {
        viewModelScope.launch {
            println("Loading data for userId: $userId, bookId: $bookId")
            val userLibraryData = loadUserLibrary(userId, bookId)

            val bookData = bookRepository.getBookById(bookId).firstOrNull()
            _book.value = bookData
            println("Loaded Book: $bookData")

            userLibraryData?.let {
                println("Fetching First ReadingProgress for userLibraryId: ${it.id}")
                val firstProgress = readingProgressRepository.getFirstReadingProgress(it.id)
                _firstReadingProgress.value = firstProgress
                println("Loaded FirstReadingProgress: $firstProgress")

                println("Fetching ReadingProgress for userLibraryId: ${it.id}")
                readingProgressRepository.getReadingProgressByUserLibraryId(it.id)
                    .collect { progressList ->
                        _readingProgress.value = progressList
                        println("Loaded ReadingProgress: $progressList")
                    }
            }
        }
    }

    fun loadReadingDataByUserLibraryId(userLibraryId: Long) {
        viewModelScope.launch {
            println("Loading data for userLibraryId: $userLibraryId")
            val userLibraryData = loadUserLibraryById(userLibraryId)

            userLibraryData?.let {
                val bookData = bookRepository.getBookById(it.bookId).firstOrNull()
                _book.value = bookData
                println("Loaded Book: $bookData")

                println("Fetching First ReadingProgress for userLibraryId: ${it.id}")
                val firstProgress = readingProgressRepository.getFirstReadingProgress(it.id)
                _firstReadingProgress.value = firstProgress
                println("Loaded FirstReadingProgress: $firstProgress")

                println("Fetching ReadingProgress for userLibraryId: ${it.id}")
                readingProgressRepository.getReadingProgressByUserLibraryId(it.id)
                    .collect { progressList ->
                        _readingProgress.value = progressList
                        println("Loaded ReadingProgress: $progressList")
                    }
            }
        }
    }

    fun loadReadingBooks(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            println("Loading reading books for userId: $userId at 02:47 PM WIB, May 18, 2025")
            try {
                // Sinkronkan data dari server sebelum mengambil data lokal
                bookRepository.syncBooksFromServer()
                userLibraryRepository.syncUserLibraryFromServer()
                readingProgressRepository.syncReadingProgressFromServer()

                userLibraryRepository.getUserLibrary(userId).collect { userLibraries ->
                    val readingBooks = mutableListOf<ReadingBook>()
                    for (userLibrary in userLibraries) {
                        if (userLibrary.status == BookStatus.READING) {
                            val book = bookRepository.getBookById(userLibrary.bookId).firstOrNull()
                            if (book != null) {
                                readingBooks.add(
                                    ReadingBook(
                                        bookId = userLibrary.bookId,
                                        bookTitle = book.title,
                                        coverPhotoPath = book.coverPhotoPath,
                                        lastPageRead = userLibrary.lastPageRead,
                                        totalPages = book.pages
                                    )
                                )
                            }
                        }
                    }
                    _readingBooks.value = readingBooks.sortedBy { it.bookTitle }
                    println("Loaded reading books: $readingBooks at 02:47 PM WIB, May 18, 2025")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load reading books: ${e.message}"
                println("Error loading reading books at 02:47 PM WIB, May 18, 2025: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertStartReadingProgress(
        userLibraryId: Long,
        startDate: Date,
        lastReadingDate: Date,
        totalPages: Int
    ) {
        viewModelScope.launch {
            val startProgress = ReadingProgress(
                userLibraryId = userLibraryId,
                pageRead = 0,
                recordedAt = startDate
            )
            println("Inserting Start Reading Progress: $startProgress")
            try {
                readingProgressRepository.insert(startProgress)
                println("Inserted Start Reading Progress successfully: $startProgress")
            } catch (e: Exception) {
                println("Failed to insert Start Reading Progress: ${e.message}")
            }

            var currentUserLibrary = _userLibrary.value
            if (currentUserLibrary == null) {
                println("UserLibrary is null, attempting to reload")
                currentUserLibrary = loadUserLibraryById(userLibraryId)
            }

            if (currentUserLibrary == null && userId != null && bookId != null) {
                println("UserLibrary not found, creating a new one")
                currentUserLibrary = UserLibrary(
                    id = userLibraryId,
                    userId = userId!!,
                    bookId = bookId!!,
                    status = BookStatus.READING,
                    lastPageRead = 0,
                    updatedAt = startDate,
                    rating = null
                )
                userLibraryRepository.insert(currentUserLibrary)
                println("Inserted new UserLibrary: $currentUserLibrary")
            }

            if (currentUserLibrary != null) {
                val updatedUserLibrary = currentUserLibrary.copy(
                    lastPageRead = 0,
                    updatedAt = lastReadingDate,
                    status = BookStatus.READING
                )
                userLibraryRepository.update(updatedUserLibrary)
                _userLibrary.value = updatedUserLibrary
                println("Updated UserLibrary: $updatedUserLibrary")

                println("Fetching First ReadingProgress for userLibraryId: $userLibraryId")
                val firstProgress = readingProgressRepository.getFirstReadingProgress(userLibraryId)
                _firstReadingProgress.value = firstProgress
                println("Updated FirstReadingProgress: $firstProgress")

                loadReadingDataByUserLibraryId(userLibraryId)
            } else {
                println("UserLibrary is still null after reload, cannot update")
            }
        }
    }

    fun updateReadingProgress(
        userLibraryId: Long,
        pageRead: Int,
        recordedAt: Date,
        lastReadingDate: Date,
        isFinished: Boolean,
        totalPages: Int
    ) {
        viewModelScope.launch {
            val finalPageRead = if (isFinished && pageRead == 0) totalPages else pageRead
            println("Updating ReadingProgress for userLibraryId: $userLibraryId, pageRead: $finalPageRead, recordedAt: $recordedAt, lastReadingDate: $lastReadingDate, isFinished: $isFinished, totalPages: $totalPages")

            val readingProgress = ReadingProgress(
                userLibraryId = userLibraryId,
                pageRead = finalPageRead,
                recordedAt = recordedAt
            )
            println("Inserting ReadingProgress: $readingProgress")
            try {
                readingProgressRepository.insert(readingProgress)
                println("Inserted ReadingProgress successfully: $readingProgress")
            } catch (e: Exception) {
                println("Failed to insert ReadingProgress: ${e.message}")
            }

            var currentUserLibrary = _userLibrary.value
            if (currentUserLibrary == null) {
                println("UserLibrary is null, attempting to reload")
                currentUserLibrary = loadUserLibraryById(userLibraryId)
            }

            if (currentUserLibrary == null && userId != null && bookId != null) {
                println("UserLibrary not found, creating a new one")
                currentUserLibrary = UserLibrary(
                    id = userLibraryId,
                    userId = userId!!,
                    bookId = bookId!!,
                    status = BookStatus.READING,
                    lastPageRead = 0,
                    updatedAt = recordedAt,
                    rating = null
                )
                userLibraryRepository.insert(currentUserLibrary)
                println("Inserted new UserLibrary: $currentUserLibrary")
            }

            if (currentUserLibrary != null) {
                val updatedUserLibrary = currentUserLibrary.copy(
                    lastPageRead = finalPageRead,
                    updatedAt = lastReadingDate,
                    status = if (isFinished) BookStatus.FINISH else BookStatus.READING
                )
                userLibraryRepository.update(updatedUserLibrary)
                _userLibrary.value = updatedUserLibrary
                println("Updated UserLibrary: $updatedUserLibrary")

                println("Fetching First ReadingProgress for userLibraryId: $userLibraryId")
                val firstProgress = readingProgressRepository.getFirstReadingProgress(userLibraryId)
                _firstReadingProgress.value = firstProgress
                println("Updated FirstReadingProgress: $firstProgress")

                if (firstProgress != null) {
                    val startDate = firstProgress.recordedAt.time
                    val lastDate = lastReadingDate.time
                    val days = TimeUnit.MILLISECONDS.toDays(lastDate - startDate)
                    println("Calculated days between start and last: $days (startDate=${firstProgress.recordedAt}, lastDate=$lastReadingDate)")
                    _daysBetweenStartAndLast.value = days.coerceAtLeast(0L)
                }

                loadReadingDataByUserLibraryId(userLibraryId)
            } else {
                println("UserLibrary is still null after reload, cannot update")
            }
        }
    }
}