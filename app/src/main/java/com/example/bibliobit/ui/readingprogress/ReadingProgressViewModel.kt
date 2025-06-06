package com.example.bibliobit.ui.readingprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.remote.ApiService
import com.example.bibliobit.data.remote.UserLibraryResponse
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private val readingProgressRepository: ReadingProgressRepository,
    private val apiService: ApiService
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

    private val _isLoading = MutableStateFlow<Boolean>(true)
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

    fun initialize(userId: String, bookId: Long, token: String) {
        this.userId = userId
        this.bookId = bookId
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val dateFormat = SimpleDateFormat("hh:mm a z", Locale.getDefault())
            val currentTime = dateFormat.format(Date())
            println("Initializing with userId: $userId, bookId: $bookId at $currentTime")

            try {
                syncDataFromServer(token)

                val userLibrary = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
                if (userLibrary != null) {
                    this@ReadingProgressViewModel.userLibraryId = userLibrary.id
                    println("Initialized with userLibraryId: ${userLibrary.id}")
                    loadReadingDataByUserLibraryId(userLibrary.id)
                } else {
                    println("UserLibrary not found for userId=$userId, bookId=$bookId")
                    _errorMessage.value = "Book not found in your reading list"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize: ${e.message}"
                println("Error initializing at $currentTime: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun initializeWithUserLibraryId(userLibraryId: Long, token: String) {
        this.userLibraryId = userLibraryId
        viewModelScope.launch {
            try {
                syncDataFromServer(token)
                loadReadingDataByUserLibraryId(userLibraryId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize: ${e.message}"
                println("Error initializing with userLibraryId=$userLibraryId: ${e.message}")
            }
        }
    }

    private suspend fun syncDataFromServer(token: String) {
        println("Syncing data from server at ${SimpleDateFormat("hh:mm a z", Locale.getDefault()).format(Date())}")
        try {
            val userLibraryResponse = apiService.getUserLibrary(status = BookStatus.READING.name) // Ubah ke .name
            if (userLibraryResponse.isSuccessful) {
                val userLibraries = userLibraryResponse.body() ?: emptyList()
                println("Fetched ${userLibraries.size} user libraries from server")

                val matchingUserLibrary = userLibraries.find { it.userId == userId && it.bookId == bookId }
                if (matchingUserLibrary != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val updatedAt = try {
                        dateFormat.parse(matchingUserLibrary.updatedAt.toString()) ?: Date()
                    } catch (e: Exception) {
                        println("Failed to parse updatedAt: ${e.message}, using current date")
                        Date()
                    }
                    val localUserLibrary = UserLibrary(
                        id = matchingUserLibrary.id,
                        userId = matchingUserLibrary.userId,
                        bookId = matchingUserLibrary.bookId,
                        status = BookStatus.valueOf(matchingUserLibrary.status), // Konversi String ke BookStatus
                        lastPageRead = matchingUserLibrary.lastPageRead,
                        updatedAt = updatedAt,
                        rating = matchingUserLibrary.rating,
                        createdAt = null,
                        isSynced = true,
                        book = matchingUserLibrary.book
                    )
                    userLibraryRepository.insert(localUserLibrary)
                    println("Saved UserLibrary to local database: $localUserLibrary")
                } else {
                    println("No matching UserLibrary found for userId=$userId, bookId=$bookId")
                }
            } else {
                println("Failed to fetch user library from server: ${userLibraryResponse.code()} - ${userLibraryResponse.message()}")
            }

            bookRepository.syncBooksFromServer()
            readingProgressRepository.syncReadingProgressFromServer()
        } catch (e: Exception) {
            println("Error syncing data from server: ${e.message}")
        }
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

    fun loadReadingBooks(userId: String, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            println("Loading reading books for userId: $userId at ${SimpleDateFormat("hh:mm a z", Locale.getDefault()).format(Date())}")
            try {
                syncDataFromServer(token)

                userLibraryRepository.getUserLibrary(userId).collect { userLibraries ->
                    val readingBooks = mutableListOf<ReadingBook>()
                    for (userLibrary in userLibraries) {
                        if (userLibrary.status == BookStatus.READING) {
                            val book = bookRepository.getBookById(userLibrary.bookId).firstOrNull()
                            if (book != null) {
                                readingBooks.add(
                                    ReadingBook(
                                        bookId = userLibrary.bookId,
                                        bookTitle = book.title ?: "Unknown Title",
                                        coverPhotoPath = book.coverPhotoPath,
                                        lastPageRead = userLibrary.lastPageRead,
                                        totalPages = book.pages
                                    )
                                )
                            }
                        }
                    }
                    _readingBooks.value = readingBooks.sortedBy { it.bookTitle }
                    println("Loaded reading books: $readingBooks at ${SimpleDateFormat("hh:mm a z", Locale.getDefault()).format(Date())}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load reading books: ${e.message}"
                println("Error loading reading books at ${SimpleDateFormat("hh:mm a z", Locale.getDefault()).format(Date())}: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertStartReadingProgress(
        userLibraryId: Long,
        startDate: Date,
        lastReadingDate: Date,
        totalPages: Int,
        token: String
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
                _errorMessage.value = "Failed to save reading progress: ${e.message}"
                return@launch
            }

            var currentUserLibrary = _userLibrary.value
            if (currentUserLibrary == null && userId != null && bookId != null) {
                println("UserLibrary not found, creating a new one")
                val newUserLibrary = UserLibrary(
                    id = userLibraryId,
                    userId = userId!!,
                    bookId = bookId!!,
                    status = BookStatus.READING,
                    lastPageRead = 0,
                    updatedAt = startDate,
                    rating = null,
                    createdAt = null,
                    isSynced = false
                )
                currentUserLibrary = newUserLibrary
                userLibraryRepository.insert(newUserLibrary)
                println("Inserted new UserLibrary locally: $newUserLibrary")
            } else if (currentUserLibrary != null) {
                val updatedUserLibrary = currentUserLibrary.copy(
                    lastPageRead = 0,
                    updatedAt = lastReadingDate,
                    status = BookStatus.READING
                )
                userLibraryRepository.update(updatedUserLibrary)
                _userLibrary.value = updatedUserLibrary
                println("Updated existing UserLibrary: $updatedUserLibrary")
            }

            if (currentUserLibrary != null) {
                try {
                    val userLibraryForServer = UserLibraryResponse(
                        id = 0, // Server akan menghasilkan ID baru
                        userId = currentUserLibrary.userId,
                        bookId = currentUserLibrary.bookId,
                        status = currentUserLibrary.status.name,
                        lastPageRead = currentUserLibrary.lastPageRead,
                        rating = currentUserLibrary.rating,
                        createdAt = null,
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(currentUserLibrary.updatedAt),
                        book = currentUserLibrary.book
                    )
                    val response = apiService.createUserLibrary(userLibraryForServer)
                    if (response.isSuccessful) {
                        val createdUserLibrary = response.body()
                        println("Successfully created UserLibrary on server: $createdUserLibrary")
                        createdUserLibrary?.let {
                            userLibraryRepository.update(it.toUserLibrary().copy(isSynced = true))
                            _userLibrary.value = it.toUserLibrary().copy(isSynced = true)
                            this@ReadingProgressViewModel.userLibraryId = it.id
                        }
                    } else {
                        println("Failed to create UserLibrary on server: ${response.code()} - ${response.message()}")
                        _errorMessage.value = "Failed to sync new book to server: ${response.message()}"
                    }
                } catch (e: Exception) {
                    println("Error creating UserLibrary on server: ${e.message}")
                    _errorMessage.value = "Error syncing new book to server: ${e.message}"
                }

                println("Fetching First ReadingProgress for userLibraryId: $userLibraryId")
                val firstProgress = readingProgressRepository.getFirstReadingProgress(userLibraryId)
                _firstReadingProgress.value = firstProgress
                println("Updated FirstReadingProgress: $firstProgress")

                loadReadingDataByUserLibraryId(userLibraryId)
            } else {
                println("UserLibrary is still null after reload, cannot update")
                _errorMessage.value = "Failed to start reading: User library not found"
            }
        }
    }

    fun updateReadingProgress(
        userLibraryId: Long,
        pageRead: Int,
        recordedAt: Date,
        lastReadingDate: Date,
        isFinished: Boolean,
        totalPages: Int,
        token: String
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
                    rating = null,
                    createdAt = null,
                    isSynced = false
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

                try {
                    val userLibraryForServer = UserLibraryResponse(
                        id = updatedUserLibrary.id,
                        userId = updatedUserLibrary.userId,
                        bookId = updatedUserLibrary.bookId,
                        status = updatedUserLibrary.status.name,
                        lastPageRead = updatedUserLibrary.lastPageRead,
                        rating = updatedUserLibrary.rating,
                        createdAt = null,
                        updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(updatedUserLibrary.updatedAt),
                        book = updatedUserLibrary.book
                    )
                    val response = apiService.updateUserLibrary(
                        id = userLibraryId,
                        userLibrary = userLibraryForServer
                    )
                    if (response.isSuccessful) {
                        println("Successfully synced updated UserLibrary to server: ${response.body()}")
                    } else {
                        println("Failed to sync updated UserLibrary to server: ${response.code()} - ${response.message()}")
                    }
                } catch (e: Exception) {
                    println("Error syncing updated UserLibrary to server: ${e.message}")
                }

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

// Tambahkan ekstensi untuk konversi UserLibraryResponse ke UserLibrary
fun UserLibraryResponse.toUserLibrary(): UserLibrary {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    return UserLibrary(
        id = this.id,
        userId = this.userId,
        bookId = this.bookId,
        status = BookStatus.valueOf(this.status),
        lastPageRead = this.lastPageRead,
        updatedAt = this.updatedAt?.let { dateFormat.parse(it) } ?: Date(),
        rating = this.rating,
        createdAt = this.createdAt?.let { dateFormat.parse(it) },
        isSynced = true,
        book = this.book
    )
}