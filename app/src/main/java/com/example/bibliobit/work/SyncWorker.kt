package com.example.bibliobit.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bibliobit.data.model.User
import com.example.bibliobit.data.repository.*
import com.example.bibliobit.utils.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Checking network status")
        if (!NetworkUtils.isOnline(applicationContext)) {
            Log.d("SyncWorker", "Device offline, retrying later")
            return Result.retry()
        }

        Log.d("SyncWorker", "Starting sync process")
        return try {
            authRepository.syncLocalUser(User("", "", "", ""))
            bookRepository.syncUnsyncedBooks()
            bookRepository.syncBooksFromServer()
            userLibraryRepository.syncUnsyncedUserLibrary()
            userLibraryRepository.syncUserLibraryFromServer()
            readingProgressRepository.syncUnsyncedReadingProgress()
            readingProgressRepository.syncReadingProgressFromServer()
            noteRepository.syncUnsyncedNotes()
            noteRepository.syncNotesFromServer()

            Log.d("SyncWorker", "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed: ${e.message}")
            if (runAttemptCount >= 3) {
                Log.e("SyncWorker", "Max retry attempts reached, failing")
                return Result.failure()
            }
            if (e is HttpException) {
                val statusCode = e.response()?.code()
                if (statusCode == 401) {
                    Log.e("SyncWorker", "Unauthorized (401), failing")
                    return Result.failure()
                }
            }
            Log.d("SyncWorker", "Retrying sync (attempt ${runAttemptCount + 1})")
            Result.retry()
        }
    }
}