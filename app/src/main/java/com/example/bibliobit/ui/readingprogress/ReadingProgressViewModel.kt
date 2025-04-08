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

    fun updateReadingProgress(
        userLibraryId: Long,
        pageRead: Int,
        recordedAt: Date,
        lastReadingDate: Date,
        isFinished: Boolean,
        totalPages: Int // Tambahkan parameter totalPages
    ) {
        viewModelScope.launch {
            // Jika isFinished true, pastikan pageRead diatur ke totalPages
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
                    lastPageRead = finalPageRead, // Gunakan finalPageRead
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